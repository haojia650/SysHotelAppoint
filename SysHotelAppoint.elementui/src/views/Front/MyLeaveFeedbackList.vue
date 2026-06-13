<template>
    <div class="feedback-management-page">
      <!-- 搜索表单卡片 -->
      <el-card class="search-card tech-card">
        <div slot="header" class="card-header">
          <span class="card-title">🔍 反馈查询</span>
          <div class="header-btns">
            <el-button type="primary" size="default" @click="SearchClick">
              🔍 查询
            </el-button>
            <el-button type="warning" size="default" @click="ResetClick">
              🔄 清空条件
            </el-button>
          </div>
        </div>
        <div class="search-form-box">
          <el-form :inline="true" :model="searchForm" size="default" class="search-form">
            <el-form-item label="📝 反馈标题" prop="Title">
              <el-input v-model.trim="searchForm.Title" placeholder="请输入反馈标题" :clearable="true"></el-input>
            </el-form-item>
            <el-form-item label="⚙️ 是否处理" class="search-input">
              <el-select v-model="searchForm.IsDoWith" class="search-input" :clearable="true" placeholder="请选择是否处理">
                <el-option key="true" label="✅ 已处理" value="true"></el-option>
                <el-option key="false" label="⏳ 未处理" value="false"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
  
      <!-- 数据列表区域 -->
      <div class="list-wrapper">
        <PaginationBox ref="LeaveFeedbackPaginationBox" :where="{}" url="/LeaveFeedback/List" :limit="8">
          <template v-slot:content="{ data }">
            <div class="feedback-grid" v-if="data.length">
              <div class="grid-item" :class="item.IsDoWith ? 'border-done' : 'border-wait'" v-for="(item, index) in data" :key="index">
                <!-- 顶部：标题 + 状态 + 时间 -->
                <div class="item-top">
                  <h3 class="item-title">📝 {{ item.Title }}</h3>
                  <div class="top-right">
                    <span class="item-status" :class="item.IsDoWith ? 'status-done' : 'status-wait'">
                      {{ item.IsDoWith ? '✅ 已处理√' : '⏳ 未处理⏳' }}
                    </span>
                    <span class="item-time">⏰ {{ formatTime(item.CreationTime) }}</span>
                  </div>
                </div>

                <!-- 反馈用户 + 处理人员 -->
                <div class="user-info">
                  <span class="info-item">👤 反馈用户：<span class="user-name">{{ UserInfo?.Name || UserInfo?.UserName || '未知用户' }}</span></span>
                  <span class="info-item" v-if="item.IsDoWith">🛠️ 处理人员：<span class="user-name">{{ item.DoWithUserDto?.Name || '未知管理员' }}</span></span>
                </div>

                <!-- 反馈内容 -->
                <div class="content-section">
                  <div class="section-title">📄 反馈内容</div>
                  <div class="rich-content" v-html="item.Content"></div>
                </div>

                <!-- 回复内容（仅已处理/有回复时显示） -->
                <div class="reply-section" v-if="item.IsDoWith || item.ReplayContent">
                  <div class="section-title">💬 回复内容</div>
                  <div class="reply-content" v-html="item.ReplayContent || '暂无回复'"></div>
                </div>

                <!-- 删除按钮（仅未处理时显示，右下角红色） -->
                <div class="delete-btn" v-if="!item.IsDoWith" @click="ShowDeleteModal(item.Id)">
                  🗑️ 删除
                </div>
              </div>
            </div>
            <div class="empty-tip" v-else>
              📭 暂无反馈数据
            </div>
          </template>
        </PaginationBox>
      </div>
  
      <!-- 编辑对话框 -->
      <el-dialog :title="formData.Id ? '✏️ 修改留言反馈' : '➕ 添加留言反馈'" v-model="editorShow" 
        width="600px" :lock-scroll="true" class="edit-dialog">
        <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" 
          label-width="120px" size="default" class="edit-form">
          <el-row :gutter="15" class="edit-form-body">
            <el-col :span="24">
              <el-form-item label="📝 反馈标题" prop="Title">
                <el-input type="text" v-model="formData.Title" placeholder="请输入反馈标题" :clearable="true"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="💬 回复内容" prop="ReplayContent">
                <el-input type="textarea" v-model="formData.ReplayContent" placeholder="请输入回复内容" :rows="4" :clearable="true"></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="⚙️ 是否处理" prop="IsDoWith">
                <el-switch v-model="formData.IsDoWith" :active-value="true" :inactive-value="false">
                </el-switch>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row type="flex" justify="end" align="middle" class="edit-form-footer">
            <el-form-item>
              <el-button size="default" type="primary" @click="CreateOrEditForm()">✅ 确定</el-button>
              <el-button size="default" @click="editorShow = false">❌ 取消</el-button>
            </el-form-item>
          </el-row>
        </el-form>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
import { Post, PostSingleUpdate } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, onBeforeMount, nextTick, watch, computed, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
  
const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
  
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)

  
const where = ref({});
const searchForm = reactive({
  Title: '',
  IsDoWith: ''
});
  
const formData = reactive({
  Id: '',
  Title: '',
  Content: '',
  IsDoWith: false,
  FeedbackUserId: UserId.value,
  DoWithUserId: '',
  ReplayContent: '',
  HotelId: ''
});
  
const editorShow = ref(false);
const editModalForm = ref(null);
const LeaveFeedbackPaginationBox = ref(null);
  
const editModalFormRules = reactive({
  Title: [{ required: true, message: '反馈标题为必填项', trigger: 'blur' }],
  Content: [{ required: true, message: '反馈内容为必填项', trigger: 'blur' }],
  IsDoWith: [{ required: true, message: '请选择处理状态', trigger: 'change' }],
  ReplayContent: [{ required: false, message: '请输入回复内容', trigger: 'blur' }]
});

const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return timeStr.replace('T', ' ');
};
  
const ShowEditModal = async (Id) => {
  const { Data } = await Post(`/LeaveFeedback/Get`, { Id: Id });
  Object.assign(formData, Data);
  editorShow.value = true;
};
  
const CreateOrEditForm = async () => {
  if (!editModalForm.value) return;
  await editModalForm.value.validate(async valid => {
    if (valid) {
      const { Success } = await Post(`/LeaveFeedback/CreateOrEdit`, formData);
      if (Success) {
        editorShow.value = false;
        LeaveFeedbackPaginationBox.value.Reload(searchForm);
        ElMessage.success('操作成功');
      }
    }
  });
};
  
const SearchClick = () => {
  LeaveFeedbackPaginationBox.value.Reload(searchForm);
};
  
const ResetClick = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = undefined;
  });
  LeaveFeedbackPaginationBox.value.Reload(searchForm);
};
  
const ShowDeleteModal = async (Id) => {
  try {
    await ElMessageBox.confirm('确认删除该信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { Success } = await Post(`/LeaveFeedback/Delete`, { Id: Id });
    if (Success) {
      LeaveFeedbackPaginationBox.value.Reload(searchForm);
      ElMessage.success('删除成功');
    }
  }
  catch (error) {
    ElMessage.warning('用户放弃了操作');
  }
};
  
const BatchDelete = async () => {
  const ids = LeaveFeedbackPaginationBox.value.GetSelectionRow().map(x => x.Id);
  if (ids.length > 0) {
    try {
      await ElMessageBox.confirm('确认删除所选的行数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const { Success } = await Post(`/LeaveFeedback/BatchDelete`, { Ids: ids });
      if (Success) {
        LeaveFeedbackPaginationBox.value.Reload(searchForm);
        ElMessage.success('删除成功');
      }
    }
    catch (error) {
      ElMessage.warning('用户放弃了操作');
    }
  }
  else {
    ElMessage.warning('请选择需要删除的数据');
  }
};
  
onBeforeMount(() => { 
  where.value.FeedbackUserId = UserId.value;
});
</script>
  
<style scoped>
/* 整体页面 */
.feedback-management-page {
  width: 100%;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf5 100%);
  min-height: 100vh;
}
  
/* 科技感卡片 */
.tech-card {
  border-radius: 12px;
  border: 1px solid rgba(22, 93, 255, 0.1);
  box-shadow: 0 4px 20px rgba(22, 93, 255, 0.08);
  overflow: hidden;
  margin-bottom: 25px;
}
  
/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
  
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #165dff;
}
  
.header-btns {
  display: flex;
  gap: 10px;
}
  
/* 搜索表单 */
.search-form-box {
  padding: 10px 0;
}
  
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}
  
/* 列表容器 */
.list-wrapper {
  width: 100%;
}
  
/* 网格布局 */
.feedback-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(600px, 1fr));
  gap: 20px;
}
  
/* 网格项卡片 */
.grid-item {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border-left: 4px solid transparent;
}

.border-done {
  border-left-color: #67c23a;
}

.border-wait {
  border-left-color: #e6a23c;
}

.grid-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}
  
/* 顶部区域 */
.item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}
  
.item-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 15px;
}
  
.item-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}
  
.status-done {
  background: #f0f9eb;
  color: #67c23a;
}
  
.status-wait {
  background: #fdf6ec;
  color: #e6a23c;
}

.item-time {
  font-size: 14px;
  color: #666;
}
  
/* 用户信息区域 */
.user-info {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.info-item {
  font-size: 15px;
  color: #333;
}

.user-name {
  color: #409eff;
  font-weight: 500;
  margin-left: 4px;
}
  
/* 内容区域通用 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

/* 反馈内容 */
.content-section {
  margin-bottom: 20px;
}
  
.rich-content {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  max-height: 250px;
  overflow-y: auto;
}
  
/* 回复内容 */
.reply-section {
  margin-bottom: 40px;
}

.reply-content {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  min-height: 40px;
}
  
/* 删除按钮 */
.delete-btn {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background: #f56c6c;
  color: #fff;
  padding: 6px 14px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  background: #f78989;
}
  
/* 空数据提示 */
.empty-tip {
  width: 100%;
  text-align: center;
  padding: 60px 0;
  font-size: 16px;
  color: #999;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}
  
/* 编辑对话框 */
:deep(.edit-dialog .el-dialog__header) {
  background: linear-gradient(90deg, #165dff, #4080ff);
  color: #fff;
  border-radius: 12px 12px 0 0;
}
  
:deep(.edit-dialog .el-dialog__title) {
  color: #fff;
}
  
.edit-form {
  padding: 10px 0;
}
  
.edit-form-body {
  margin-bottom: 20px;
}
  
.edit-form-footer {
  padding-top: 15px;
  border-top: 1px solid #eee;
}
  
/* 响应式 */
@media (max-width: 768px) {
  .feedback-grid {
    grid-template-columns: 1fr;
  }
  
  .search-form {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-info {
    flex-direction: column;
    gap: 8px;
  }

  .item-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .top-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>