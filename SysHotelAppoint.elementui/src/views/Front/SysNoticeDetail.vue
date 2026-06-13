<template>
    <!-- 页面根容器 -->
    <div class="notice-detail-page">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/Front/Notice' }">系统通知</el-breadcrumb-item>
          <el-breadcrumb-item>通知详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
  
      <!-- 加载状态 -->
      <div v-loading="loading" element-loading-text="加载中..." class="loading-box">
        <!-- 详情内容卡片 -->
        <div class="detail-card" v-if="SysNotice">
          <!-- 通知标题 -->
          <h1 class="detail-title">{{ SysNotice.Title }}</h1>
          <!-- 发布时间 -->
          <div class="detail-time">
            <i class="el-icon-time"></i>
            发布时间：{{ formatTime(SysNotice.CreationTime) }}
          </div>
          <!-- 分割线 -->
          <div class="divider"></div>
          <!-- 通知内容 -->
          <div class="detail-content">{{ SysNotice.Content }}</div>
        </div>
  
        <!-- 无数据状态 -->
        <el-empty v-else description="通知不存在或已删除" :image-size="120" class="empty-box" />
      </div>
    </div>
  </template>
  
  <script setup>
  import defaultAvatar from '@/assets/默认头像.png';
  import NoImage from '@/assets/图片失效.png';
  import { Post, PostSingleUpdate } from '@/api/http';
  import { ref, reactive, nextTick, computed, onBeforeMount } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useCommonStore } from "@/store";
  import { ElMessage, ElMessageBox, ElEmpty } from 'element-plus';
  
  const router = useRouter();
  const route = useRoute();
  const commonStore = useCommonStore();
  
  // 全局状态
  const Token = computed(() => commonStore.Token);
  const UserInfo = computed(() => commonStore.UserInfo);
  const RoleType = computed(() => commonStore.RoleType);
  const UserId = computed(() => commonStore.UserId);
  
  // 数据定义
  const SysNotice = ref(null);
  const loading = ref(true);
  
  // 获取通知详情
  const GetSysNoticeApi = async () => {
    try {
      loading.value = true;
      // 校验参数
      if (!route.query.SysNoticeId) {
        ElMessage.warning('通知ID不存在');
        return;
      }
      const { Data, Success } = await Post("/SysNotice/Get", { Id: route.query.SysNoticeId });
      if (Success) {
        SysNotice.value = Data; // 详情接口返回单条数据
      } else {
        ElMessage.error('获取通知详情失败');
      }
    } catch (error) {
      ElMessage.error('网络异常，请重试');
      console.error(error);
    } finally {
      loading.value = false;
    }
  };
  
  // 时间格式化
  const formatTime = (timeStr) => {
    if (!timeStr) return '';
    const date = new Date(timeStr);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  };
  
  // 生命周期
  onBeforeMount(async () => {
    await GetSysNoticeApi();
  })
  </script>
  
  <style scoped lang="scss">
  // 设计变量（与列表页保持一致）
  $primary-color: #1989fa;
  $card-bg: #ffffff;
  $text-primary: #303133;
  $text-secondary: #606266;
  $text-time: #909399;
  $border-radius: 12px;
  $shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  
  // 页面容器
  .notice-detail-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 20px;
    min-height: calc(100vh - 100px);
    background-color: #f5f7fa;
  }
  
  // 面包屑样式
  .breadcrumb {
    display: flex;
    align-items: center;
    margin-bottom: 32px;
    font-size: 14px;
    color: $text-secondary;
  
    .breadcrumb-label {
      margin-right: 12px;
      font-weight: 500;
    }
  }
  
  // 加载容器
  .loading-box {
    min-height: 400px;
  }
  
  // 空数据样式
  .empty-box {
    padding: 60px 0;
    background: $card-bg;
    border-radius: $border-radius;
  }
  
  // 详情卡片
  .detail-card {
    background: $card-bg;
    border-radius: $border-radius;
    padding: 48px 56px;
    box-shadow: $shadow-light;
    transition: all 0.3s ease;
  }
  
  // 标题
  .detail-title {
    font-size: 24px;
    font-weight: 600;
    color: $text-primary;
    text-align: center;
    margin: 0 0 20px 0;
    line-height: 1.4;
  }
  
  // 时间
  .detail-time {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: $text-time;
    margin-bottom: 32px;
  
    i {
      margin-right: 8px;
      font-size: 16px;
    }
  }
  
  // 分割线
  .divider {
    height: 1px;
    background-color: #f0f0f0;
    margin-bottom: 32px;
  }
  
  // 内容
  .detail-content {
    font-size: 16px;
    color: $text-secondary;
    line-height: 2;
    word-break: break-all;
    text-align: justify;
  }
  
  // 响应式适配
  @media (max-width: 768px) {
    .notice-detail-page {
      padding: 16px 12px;
    }
  
    .detail-card {
      padding: 28px 24px;
    }
  
    .detail-title {
      font-size: 20px;
    }
  
    .detail-content {
      font-size: 15px;
    }
  }
  </style>