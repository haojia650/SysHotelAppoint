<template>
  <!-- 页面整体容器 -->
  <div class="feedback-page">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-wrapper">
      <span class="breadcrumb-tip">您当前的位置:</span>
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我要反馈</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 页面标题区域 -->
    <div class="page-header">
      <h1 class="page-title">用户反馈提交</h1>
      <p class="page-desc">我们十分重视您的意见与建议,将在1-3个工作日内处理并回复您</p>
    </div>

    <!-- 表单卡片容器 -->
    <div class="form-card">
      <!-- 温馨提示栏 -->
      <div class="tip-box">
        <el-icon color="#f59e0b" style="margin-right:8px;font-size:16px"><Warning /></el-icon>
        <span>温馨提示：请如实填写反馈内容，我们会严格保护您的隐私信息</span>
      </div>

      <el-form
        ref="editModalForm"
        :rules="editModalFormRules"
        :model="formData"
        label-width="120px"
        size="default"
        class="feedback-form"
      >
        <el-row :gutter="20" class="form-body">
          <el-col :span="24">
            <el-form-item label="反馈标题" prop="Title">
              <el-input
                v-model="formData.Title"
                placeholder="请简要描述您的反馈主题（1-20字）"
                :clearable="true"
                class="input-large"
              />
              <div class="form-tip">标题简洁明了，便于我们快速定位问题</div>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="反馈内容" prop="Content">
              <AIRichText v-model="formData.Content" class="rich-text-box" />
              <div class="form-tip">请详细描述您遇到的问题、建议或需求</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row class="btn-row">
          <el-form-item>
            <el-button size="default" type="primary" @click="CreateOrEditForm()">
              <el-icon><Check /></el-icon>
              提交反馈
            </el-button>
            <el-button size="default" @click="resetForm()">
              <el-icon><Close /></el-icon>
              重置填写
            </el-button>
          </el-form-item>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { Post, PostSingleUpdate } from '@/api/http';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from '@/store';
// 引入Element Plus图标
import { Check, Close, Warning } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();

// 表单数据
const formData = reactive({
  Id: '',
  Title: '',
  Content: '',
  FeedbackUserId: '',
  HotelId: '',
  DoWithUserId: '',
  ReplayContent: '',
  IsDoWith: false,
});

// 表单引用
const editModalForm = ref(null);

// 表单验证规则
const editModalFormRules = reactive({
  Content: [{ required: true, message: '反馈内容为必填项', trigger: 'blur' }],
  DoWithUserId: [{ required: true, message: '处理人为必填项', trigger: 'change' }],
  Title: [{ required: true, message: '反馈标题为必填项', trigger: 'blur' }],
  ReplayContent: [{ required: true, message: '回复内容为必填项', trigger: 'blur' }],
  FeedbackUserId: [{ required: true, message: '反馈人为必填项', trigger: 'change' }],
  HotelId: [{ required: true, message: '酒店为必填项', trigger: 'change' }],
});

// 页面加载
onMounted(() => {
  const id = route.query.Id;
  if (id) {
    ShowEditModal(id);
  }
});

// 编辑数据回显（修复原代码未定义变量）
const ShowEditModal = async (Id) => {
  try {
    const { Data } = await Post(`/LeaveFeedback/Get`, { Id: Id });
    // 修复未定义变量，注释或根据实际业务修改
    // Data.FeedbackUserId = UserId.value;
    if (route.query.HotelId) {
      Data.HotelId = route.query.HotelId;
    }
    Object.assign(formData, Data);
  } catch (error) {
    ElMessage.error('数据加载失败');
  }
};

// 提交表单
const CreateOrEditForm = async () => {
  if (!editModalForm.value) return;

  editModalForm.value.validate(async (valid) => {
    if (valid) {
      try {
        const { Success } = await Post(`/LeaveFeedback/CreateOrEdit`, formData);
        if (Success) {
          ElMessage.success('您的反馈已成功提交，我们将尽快处理');
          router.push('/Front/UserCenter?currentMenu=MyLeaveFeedbackList')
        }
      } catch (error) {
        ElMessage.error('提交失败，请稍后重试');
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  editModalForm.value?.resetFields();
  Object.assign(formData, {
    Title: '',
    Content: '',
    FeedbackUserId: '',
    HotelId: '',
    DoWithUserId: '',
    ReplayContent: '',
    IsDoWith: false,
  });
};
</script>

<style scoped>
/* 页面整体样式 */
.feedback-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 80px);
}

/* 面包屑 */
.breadcrumb-wrapper {
  display: flex;
  align-items: center;
  padding: 12px 0;
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}
.breadcrumb-tip {
  margin-right: 12px;
  font-weight: 500;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}
.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px;
}
.page-desc {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

/* 表单卡片 */
.form-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #e5e7eb;
}

/* 温馨提示框 */
.tip-box {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #fffbeb;
  border-left: 4px solid #f59e0b;
  border-radius: 8px;
  margin-bottom: 28px;
  font-size: 14px;
  color: #92400e;
}

/* 表单主体 */
.feedback-form {
  width: 100%;
}
.form-body {
  margin-bottom: 20px;
}

/* 输入框放大 */
.input-large {
  height: 44px;
}

/* 富文本框 */
.rich-text-box {
  min-height: 200px !important;
}

/* 表单项提示文字 */
.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #9ca3af;
}

/* 按钮行 */
.btn-row {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  gap: 20px;
}
.btn-row :deep(.el-button) {
  padding: 10px 28px;
  font-size: 15px;
  border-radius: 6px;
}
.btn-row :deep(.el-button--primary) {
  background-color: #165dff;
  border-color: #165dff;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .form-card {
    padding: 20px;
  }
  .page-title {
    font-size: 22px;
  }
  .btn-row {
    flex-direction: column;
    align-items: center;
  }
}
</style>