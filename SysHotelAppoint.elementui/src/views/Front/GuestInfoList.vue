<template>
    <div class="guest-info-container">
        <!-- 搜索表单卡片 -->
        <el-card class="search-card box-card">
            <div slot="header" class="clearfix header-container">
                <el-row align="middle" justify="space-between">
                    <h3 class="title">旅客信息管理</h3>
                    <el-row>
                        <el-button type="primary" size="default" @click="SearchClick" class="operate-btn">
                            <el-icon><Search /></el-icon>查询
                        </el-button>
                        <el-button type="warning" size="default" @click="ResetClick" class="operate-btn">
                            <el-icon><Refresh /></el-icon>清空条件
                        </el-button>
                        <el-button type="success" size="default" @click="ShowEditModal()" class="operate-btn">
                            <el-icon><Edit /></el-icon>新增
                        </el-button>
                    </el-row>
                </el-row>
            </div>
            <div class="margin-top-sm search-form-container">
                <el-form :inline="true" :model="searchForm" size="default">
                    <el-form-item label="关键词" prop="KeyWord">
                        <el-input 
                            v-model.trim="searchForm.KeyWord"  
                            placeholder="请输入姓名/手机号/身份证"  
                            :clearable="true"
                            class="search-input"
                        ></el-input>
                    </el-form-item>
                    <el-form-item label="所属账号">
                        <SigleSelect 
                            url="/User/List" 
                            class="search-input" 
                            columnName="Name" 
                            :clearable="true" 
                            columnValue="Id"
                            v-model="searchForm.BelongUserId"
                        ></SigleSelect>
                    </el-form-item>
                </el-form>
            </div>
        </el-card>

        <!-- 编辑对话框 -->
        <el-dialog 
            :title="formData.Id ? '修改旅客信息' : '添加旅客信息'" 
            v-model="editorShow" 
            width="60%" 
            :lock-scroll="true"
            class="edit-dialog"
            :close-on-click-modal="false"
        >
            <el-form 
                v-if="editorShow" 
                ref="editModalForm" 
                :rules="editModalFormRules" 
                :model="formData" 
                label-width="140px"
                size="default"
                class="edit-form"
            >             
              <el-row :gutter="20" class="edit-from-body">
                    <el-col :span="12">
                        <el-form-item label="姓名" prop="Name">
                            <el-input 
                                type="text" 
                                v-model="formData.Name"  
                                placeholder="请输入姓名"     
                                :clearable="true"
                                class="form-input"
                            ></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="手机号" prop="Phone">
                            <el-input 
                                type="text" 
                                v-model="formData.Phone"  
                                placeholder="请输入手机号"     
                                :clearable="true"
                                class="form-input"
                            ></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="性别" prop="Sex">
                            <el-select 
                                v-model="formData.Sex" 
                                placeholder="请选择性别"
                                class="form-input"
                            >
                                <el-option label="男" value="男"></el-option>
                                <el-option label="女" value="女"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="出生年月" prop="Birth">
                            <el-date-picker 
                                v-model="formData.Birth" 
                                align="right" 
                                type="date" 
                                placeholder="选择出生时间" 
                                format="YYYY-MM-DD"
                                value-format="YYYY-MM-DD HH:mm:ss"
                                class="form-input"
                            >
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="身份证号" prop="IdCard">
                            <el-input 
                                type="text" 
                                v-model="formData.IdCard"  
                                placeholder="请输入身份证号"     
                                :clearable="true"
                                class="form-input"
                            ></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="所属账号" prop="BelongUserId">
                          <SigleSelect 
                              url="/User/List" 
                              columnName="Name" 
                              columnValue="Id"  
                              v-model="formData.BelongUserId"
                              class="form-input"
                          >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row type="flex" justify="end" align="bottom" class="form-btn-group">
                    <el-form-item>
                        <el-button size="default" type="primary" @click="CreateOrEditForm()">确 定</el-button>
                        <el-button size="default" @click="editorShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>

        <!-- 分页列表展示 -->
        <PaginationBox 
            ref="GuestInfoPaginationBox" 
            :where="where" 
            url="/GuestInfo/List" 
            :limit="8"
            class="pagination-container"
        >
            <template v-slot:content="{ data }">
                <!-- Grid布局容器 - 设置width:100%满足要求 -->
                <div class="guest-card-grid" style="width: 100%;">
                    <!-- 旅客信息卡片 -->
                    <div 
                        v-for="(item, index) in data" 
                        :key="item.Id || index" 
                        class="guest-card"
                        :class="{ 'even-card': index % 2 === 0 }"
                    >
                        <!-- 卡片头部 -->
                        <div class="card-header">
                            <div class="guest-name">
                                <el-avatar :icon="User" class="name-avatar"></el-avatar>
                                <span class="name-text">{{ item.Name }}</span>
                                <el-tag :type="item.Sex === '男' ? 'primary' : 'success'" size="small" class="sex-tag">
                                    {{ item.Sex }}
                                </el-tag>
                            </div>
                            <div class="create-time">
                                <el-icon><Clock /></el-icon>
                                <span>{{ formatTime(item.CreationTime) }}</span>
                            </div>
                        </div>
                        
                        <!-- 卡片主体 -->
                        <div class="card-body">
                            <el-row :gutter="15" class="info-row">
                                <el-col :span="12" class="info-col">
                                    <div class="info-item">
                                        <span class="info-label">手机号：</span>
                                        <span class="info-value">{{ item.Phone || '-' }}</span>
                                    </div>
                                </el-col>
                                <el-col :span="12" class="info-col">
                                    <div class="info-item">
                                        <span class="info-label">出生日期：</span>
                                        <span class="info-value">{{ item.Birth ? item.Birth.split(' ')[0] : '-' }}</span>
                                    </div>
                                </el-col>
                                <el-col :span="24" class="info-col">
                                    <div class="info-item">
                                        <span class="info-label">身份证号：</span>
                                        <span class="info-value">{{ maskIdCard(item.IdCard) || '-' }}</span>
                                    </div>
                                </el-col>
                                <el-col :span="24" class="info-col">
                                    <div class="info-item belong-user">
                                        <span class="info-label">所属账号：</span>
                                        <div class="user-info">
                                            <span class="user-name">{{ item.BelongUserDto?.Name || '-' }}</span>
                                            <el-tag size="mini" class="user-role">
                                                {{ item.BelongUserDto?.RoleTypeFormat || '-' }}
                                            </el-tag>
                                            <span class="user-phone">{{ item.BelongUserDto?.PhoneNumber || '-' }}</span>
                                        </div>
                                    </div>
                                </el-col>
                            </el-row>
                        </div>
                        
                        <!-- 卡片底部操作区 -->
                        <div class="card-footer">
                            <el-button 
                                type="primary" 
                                size="small" 
                                @click="ShowEditModal(item.Id)"
                                class="card-btn edit-btn"
                            >
                                <el-icon><Edit /></el-icon>修改
                            </el-button>
                            <el-button 
                                type="danger" 
                                size="small" 
                                @click="ShowDeleteModal(item.Id)"
                                class="card-btn delete-btn"
                            >
                                <el-icon><Delete /></el-icon>删除
                            </el-button>
                        </div>
                        
                        <!-- 科技感装饰 -->
                        <div class="tech-decoration top-decoration"></div>
                        <div class="tech-decoration bottom-decoration"></div>
                    </div>
                    
                    <!-- 空数据提示 -->
                    <div v-if="!data || data.length === 0" class="empty-data">
                        <el-empty description="暂无旅客信息"></el-empty>
                    </div>
                </div>
            </template>
        </PaginationBox>
    </div>
</template>

<script setup>
import { Post, PostSingleUpdate } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";

import { 
    Delete, Edit, Refresh, Search, Clock, User 
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, ElEmpty } from 'element-plus';
import { 
    onMounted, onBeforeMount, nextTick, watch, computed, reactive, ref 
} from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();

// 计算属性
const Token = computed(() => commonStore.Token);
const UserInfo = computed(() => commonStore.UserInfo);
const RoleType = computed(() => commonStore.RoleType);
const UserId = computed(() => commonStore.UserId);
  
// 搜索表单数据
const searchForm = reactive({});

// 分页查询条件
const where = ref({});

// 编辑表单数据
const formData = reactive({});

// 编辑对话框显示状态
const editorShow = ref(false);

// 表单引用
const editModalForm = ref(null);

// 分页组件引用
const GuestInfoPaginationBox = ref(null);

// 表单验证规则
const editModalFormRules = reactive({
    Phone: [
        { required: true, message: '手机号为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg = /^1[3456789]\d{9}$/;
                if (!value) {
                    callback(new Error('请输入手机号'));
                } else if (!reg.test(value)) {
                    callback(new Error('请输入正确的手机号'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    BelongUserId: [
        { required: true, message: '所属账号为必填项', trigger: 'change' },
    ],
    Birth: [
        { required: true, message: '出生年月为必填项', trigger: 'change' },
    ],
    Sex: [
        { required: true, message: '性别为必填项', trigger: 'change' },
    ],
    Name: [
        { required: true, message: '姓名为必填项', trigger: 'blur' },
    ],
    IdCard: [
        { required: true, message: '身份证号为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg = /(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
                if (!value) {
                    callback(new Error('请输入身份证号'));
                } else if (!reg.test(value)) {
                    callback(new Error('请输入正确的身份证号'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
});

// 格式化时间
const formatTime = (timeStr) => {
    if (!timeStr) return '-';
    const date = new Date(timeStr);
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
};

// 身份证号脱敏
const maskIdCard = (idCard) => {
    if (!idCard) return '';
    return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
};

// 显示编辑对话框
const ShowEditModal = async (Id) => {
    // 清空表单数据
    Object.keys(formData).forEach(key => {
        formData[key] = undefined;
    });
    
    // 设置默认所属用户ID
    formData.BelongUserId = UserId.value;
    
    // 如果是编辑，获取详情
    if (Id) {
        try {
            const { Data } = await Post(`/GuestInfo/Get`, { Id });
            Object.assign(formData, Data);
        } catch (error) {
            ElMessage.error('获取旅客信息失败');
            return;
        }
    }
    
    editorShow.value = true;
    // 重置表单验证
    nextTick(() => {
        if (editModalForm.value) {
            editModalForm.value.clearValidate();
        }
    });
};

// 创建或编辑表单提交
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    try {
        await editModalForm.value.validate(async (valid) => {
            if (valid) {
                const { Success, Msg } = await Post(`/GuestInfo/CreateOrEdit`, formData);
                if (Success) {
                    editorShow.value = false;
                    GuestInfoPaginationBox.value.Reload({...where.value, ...searchForm});
                    ElMessage.success(formData.Id ? '修改成功' : '新增成功');
                } else {
                    ElMessage.error(Msg || '操作失败');
                }
            }
        });
    } catch (error) {
        ElMessage.error('提交失败，请重试');
    }
};

// 搜索点击
const SearchClick = () => {
    GuestInfoPaginationBox.value.Reload({...where.value, ...searchForm});
};

// 重置搜索条件
const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    GuestInfoPaginationBox.value.Reload(where.value);
};

// 显示删除确认框
const ShowDeleteModal = async (Id) => {
    try {
        await ElMessageBox.confirm(
            '确认删除该旅客信息吗？此操作不可恢复！', 
            '删除确认', 
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'danger'
            }
        );
        
        const { Success, Msg } = await Post(`/GuestInfo/Delete`, { Id });
        if (Success) {
            GuestInfoPaginationBox.value.Reload({...where.value, ...searchForm});
            ElMessage.success('删除成功');
        } else {
            ElMessage.error(Msg || '删除失败');
        }
    } catch (error) {
        ElMessage.info('已取消删除操作');
    }
};

// 批量删除（预留方法）
const BatchDelete = async () => {
    try {
        const selectionRows = GuestInfoPaginationBox.value.GetSelectionRow?.() || [];
        if (selectionRows.length === 0) {
            ElMessage.warning('请选择要删除的旅客信息');
            return;
        }

        await ElMessageBox.confirm(
            `确认删除选中的${selectionRows.length}条旅客信息吗？此操作不可恢复！`, 
            '批量删除确认', 
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'danger'
            }
        );
        
        const ids = selectionRows.map(x => x.Id);
        const { Success, Msg } = await Post(`/GuestInfo/BatchDelete`, { Ids: ids });
        
        if (Success) {
            GuestInfoPaginationBox.value.Reload({...where.value, ...searchForm});
            ElMessage.success('批量删除成功');
        } else {
            ElMessage.error(Msg || '批量删除失败');
        }
    } catch (error) {
        ElMessage.info('已取消批量删除操作');
    }
};

// 初始化查询条件
onBeforeMount(() => {
    where.value.BelongUserId = UserId.value;
});
</script>

<style scoped>
/* 全局容器 */
.guest-info-container {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
}

/* 搜索卡片样式 */
.search-card {
    background: linear-gradient(135deg, #ffffff 0%, #f8f9fc 100%);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.08);
    margin-bottom: 20px;
    border: 1px solid #e8f3ff;
}

.header-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.title {
    font-size: 18px;
    font-weight: 600;
    color: #165DFF;
    margin: 0;
}

.operate-btn {
    margin-left: 10px;
    border-radius: 6px;
    transition: all 0.3s ease;
}

.operate-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(22, 93, 255, 0.2);
}

.search-form-container {
    padding: 10px 0;
}

.search-input {
    width: 280px;
}

/* 编辑对话框样式 */
.edit-dialog {
    border-radius: 12px;
    overflow: hidden;
}

.edit-form {
    padding: 10px 0;
}

.form-input {
    width: 100%;
}

.edit-from-body {
    margin-bottom: 20px;
}

.form-btn-group {
    margin-top: 20px;
    padding-top: 10px;
    border-top: 1px solid #e6e6e6;
}

/* 卡片网格布局 */
.guest-card-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
    gap: 20px;
    padding: 10px 0;
    width: 100% !important;
}

/* 旅客卡片样式 */
.guest-card {
    background: linear-gradient(135deg, #ffffff 0%, #f0f7ff 100%);
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 6px 16px rgba(22, 93, 255, 0.1);
    border: 1px solid #e8f3ff;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
}

.guest-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 24px rgba(22, 93, 255, 0.15);
    border-color: #165DFF;
}

.even-card {
    background: linear-gradient(135deg, #ffffff 0%, #f9fbff 100%);
}

/* 卡片头部 */
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px dashed #e8f3ff;
}

.guest-name {
    display: flex;
    align-items: center;
}

.name-avatar {
    background-color: #165DFF;
    margin-right: 10px;
}

.name-text {
    font-size: 18px;
    font-weight: 600;
    color: #165DFF;
}

.sex-tag {
    margin-left: 10px;
}

.create-time {
    display: flex;
    align-items: center;
    font-size: 12px;
    color: #666;
}

.create-time el-icon {
    margin-right: 5px;
}

/* 卡片主体 */
.card-body {
    margin-bottom: 20px;
}

.info-row {
    width: 100%;
}

.info-col {
    margin-bottom: 10px;
}

.info-item {
    display: flex;
    align-items: center;
    font-size: 14px;
}

.info-label {
    color: #666;
    min-width: 80px;
    font-weight: 500;
}

.info-value {
    color: #333;
    flex: 1;
}

.belong-user {
    align-items: flex-start;
}

.user-info {
    flex: 1;
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
}

.user-role {
    background-color: #e8f3ff;
    color: #165DFF;
    border: none;
}

/* 卡片底部 */
.card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 15px;
    border-top: 1px dashed #e8f3ff;
}

.card-btn {
    border-radius: 6px;
    transition: all 0.2s ease;
}

.edit-btn {
    background-color: #165DFF;
    border-color: #165DFF;
}

.delete-btn {
    background-color: #ff4d4f;
    border-color: #ff4d4f;
}

/* 科技感装饰元素 */
.tech-decoration {
    position: absolute;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(22, 93, 255, 0.05) 0%, rgba(22, 93, 255, 0) 70%);
    border-radius: 50%;
    z-index: 0;
}

.top-decoration {
    top: -100px;
    right: -100px;
}

.bottom-decoration {
    bottom: -100px;
    left: -100px;
}

/* 空数据样式 */
.empty-data {
    grid-column: 1 / -1;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 60px 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
    .guest-card-grid {
        grid-template-columns: 1fr;
    }
    
    .search-input {
        width: 100%;
        margin-bottom: 10px;
    }
    
    .header-container {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .header-container el-row {
        margin-top: 10px;
        width: 100%;
    }
}
</style>