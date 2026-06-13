<template>
    <!-- 页面容器 -->
    <div class="system-notice-page">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-wrapper">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>系统通知</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
  
      <!-- 通知列表分页组件 -->
      <PaginationBox
        ref="SysNoticePaginationBox"
        :where="{ IsShow: true }"
        url="/SysNotice/List"
        :limit="8"
      >
        <template v-slot:content="{ data }">
          <!-- 空数据状态 -->
          <el-empty
            v-if="!data || data.length === 0"
            description="暂无系统通知"
            :image-size="120"
            class="empty-box"
          />
  
          <!-- 通知列表 -->
          <div class="notice-list" v-else>
            <div
              v-for="(item, index) in data"
              :key="item.Id || index"
              class="notice-card"
            >
              <!-- 通知标题 -->
              <div class="notice-title">{{ item.Title }}</div>
              <!-- 发布时间 -->
              <div class="notice-time">
                <i class="el-icon-time"></i>
                {{ formatTime(item.CreationTime) }}
              </div>
              <!-- 通知内容 -->
              <div class="notice-content">{{ item.Content }}</div>
            </div>
          </div>
        </template>
      </PaginationBox>
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
  
  // 分页组件引用
  const SysNoticePaginationBox = ref(null);
  
  // ------------------------------
  // 工具函数：格式化时间
  // ------------------------------
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
  
  onBeforeMount(async () => {
  });
  </script>
  
  <style scoped lang="scss">
  // 全局页面变量
  $primary-color: #1989fa;
  $card-bg: #ffffff;
  $text-primary: #303133;
  $text-secondary: #606266;
  $text-time: #909399;
  $border-radius: 12px;
  $shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  $shadow-hover: 0 4px 20px 0 rgba(0, 0, 0, 0.12);
  
  // 页面根容器
  .system-notice-page {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 20px;
    min-height: calc(100vh - 100px);
    background-color: #f5f7fa;
  }
  
  // 面包屑样式
  .breadcrumb-wrapper {
    display: flex;
    align-items: center;
    margin-bottom: 32px;
    font-size: 14px;
    color: $text-secondary;
  
    .breadcrumb-label {
      margin-right: 12px;
      font-weight: 500;
    }
  
    :deep(.el-breadcrumb__item) {
      font-size: 14px;
    }
  }
  
  // 空数据容器
  .empty-box {
    padding: 60px 0;
    background: $card-bg;
    border-radius: $border-radius;
  }
  
  // 通知列表容器
  .notice-list {
    display: flex;
    flex-direction: column;
    gap: 16px; // 卡片间距
  }
  
  // 通知卡片主体
  .notice-card {
    padding: 28px 32px;
    background: $card-bg;
    border-radius: $border-radius;
    box-shadow: $shadow-light;
    transition: all 0.3s ease;
    cursor: default;
  
    // 悬停动效（大气核心）
    &:hover {
      transform: translateY(-4px);
      box-shadow: $shadow-hover;
    }
  
    // 标题
    .notice-title {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 12px;
      line-height: 1.4;
    }
  
    // 时间
    .notice-time {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: $text-time;
      margin-bottom: 16px;
  
      i {
        margin-right: 6px;
        font-size: 14px;
      }
    }
  
    // 内容
    .notice-content {
      font-size: 15px;
      color: $text-secondary;
      line-height: 1.8;
      word-break: break-all;
    }
  }
  
  // 响应式适配
  @media (max-width: 768px) {
    .system-notice-page {
      padding: 16px 12px;
    }
  
    .notice-card {
      padding: 20px 24px;
    }
  
    .notice-title {
      font-size: 16px;
    }
  }
  </style>