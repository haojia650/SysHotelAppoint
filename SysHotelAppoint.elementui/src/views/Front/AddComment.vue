<template>
    <!-- 页面整体容器 -->
    <div class="comment-page-container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-wrapper">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/Front/MyAppointOrderList' }">我的预约订单</el-breadcrumb-item>
          <el-breadcrumb-item>订单评价</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
  
      <!-- 评价表单卡片 -->
      <el-card v-if="editorShow" class="form-card" shadow="hover">
        <el-form
          ref="editModalForm"
          :rules="editModalFormRules"
          :model="formData"
          label-width="120px"
          size="default"
          class="comment-form"
        >
          <el-row :gutter="20" class="form-body">
            <el-col :span="24">
              <!-- 评价内容（修复：数字框改为多行文本域） -->
              <el-form-item label="评价内容" prop="Comment">
                <el-input
                  v-model="formData.Comment"
                  type="textarea"
                  :rows="5"
                  placeholder="请输入您的评价内容"
                  :clearable="true"
                  class="comment-input"
                />
              </el-form-item>
  
              <!-- 评分组件 -->
              <el-form-item label="服务评分" prop="CommentScore">
                <el-rate
                  v-model="formData.CommentScore"
                  :texts="texts"
                  allow-half
                  show-text
                  class="score-rate"
                />
              </el-form-item>
            </el-col>
          </el-row>
  
          <!-- 提交按钮 -->
          <el-row class="btn-row">
            <el-col :span="24" class="btn-col">
              <el-form-item>
                <el-button size="default" type="primary" @click="CreateOrEditForm()">
                  提交评价
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import defaultAvatar from '@/assets/默认头像.png';
  import NoImage from '@/assets/图片失效.png';
  import { Post, PostSingleUpdate } from '@/api/http';
  import { ref, reactive, nextTick, computed, onBeforeMount } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useCommonStore } from "@/store";
  import { ElMessage } from 'element-plus';
  
  const router = useRouter();
  const route = useRoute();
  const commonStore = useCommonStore();
  const Token = computed(() => commonStore.Token);
  const UserInfo = computed(() => commonStore.UserInfo);
  const RoleType = computed(() => commonStore.RoleType);
  const UserId = computed(() => commonStore.UserId);
  
  // 表单数据
  const formData = reactive({});
  // 表单显示状态
  const editorShow = ref(false);
  // 表单引用
  const editModalForm = ref(null);
  // 评分展示文字
  const texts = ref(['极差', '失望', '一般', '满意', '超赞']);
  
  // 表单验证规则（保留原有必填，优化格式）
  const editModalFormRules = reactive({
    BeginAppointTime: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    No: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    Comment: [{ required: true, message: '请输入评价内容', trigger: 'blur' }],
    ReturnMoney: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    TotalPeopleNum: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    HotelId: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    AppointStatus: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    RoomId: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    Qty: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    PayTime: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    RoomDetId: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    EndAppointTime: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    PayType: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    CommentScore: [{ required: true, message: '请选择评分', trigger: 'change' }],
    ToUserId: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
    TotalMoney: [{ required: true, message: '该项为必填项', trigger: 'blur' }],
  });
  
  onBeforeMount(async () => {
    ShowEditModal(route.query.AppointId);
  });
  
  // 加载订单数据
  const ShowEditModal = async (Id) => {
    const { Data } = await Post(`/Appoint/Get`, { Id: Id });
    Object.assign(formData, Data);
    editorShow.value = true;
  };
  
  // 提交评价
  const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;
  
    await editModalForm.value.validate(async (valid) => {
      if (valid) {
        const { Success } = await Post(`/Appoint/Comment`, formData);
        if (Success) {
          editorShow.value = false;
          router.go(-1);
          ElMessage.success('评价提交成功！');
        }
      }
    });
  };
  </script>
  
  <style scoped lang="scss">
  // 页面整体容器
  .comment-page-container {
    max-width: 900px;
    margin: 30px auto;
    padding: 0 20px;
  }
  
  // 面包屑样式
  .breadcrumb-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-bottom: 16px;
    margin-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;
  
    .breadcrumb-label {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }
  
    :deep(.el-breadcrumb__item) {
      font-size: 14px;
    }
  }
  
  // 表单卡片
  .form-card {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  
    :deep(.el-card__body) {
      padding: 30px;
    }
  }
  
  // 表单主体
  .comment-form {
    .form-body {
      margin-bottom: 20px;
    }
  
    // 评价输入框
    .comment-input {
      border-radius: 8px;
      resize: none;
    }
  
    // 评分组件
    .score-rate {
      margin-top: 10px;
      font-size: 14px;
    }
  
    // 按钮行
    .btn-row {
      text-align: center;
    }
  
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: #333;
    }
  
    :deep(.el-button--primary) {
      min-width: 120px;
      border-radius: 6px;
      font-weight: 500;
    }
  }
  </style>