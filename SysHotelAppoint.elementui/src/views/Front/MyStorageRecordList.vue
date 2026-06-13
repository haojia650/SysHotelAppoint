<template>
    <!-- 面包屑导航 -->
    <div class="breadcrumb-wrapper">
      <span class="breadcrumb-label">📍 您当前的位置:</span>
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="{ path: '/Front/Home' }">🏠 首页</el-breadcrumb-item>
        <el-breadcrumb-item>📦 我的寄存</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  
    <!-- 寄存记录列表 -->
    <PaginationBox 
      ref="StorageRecordPaginationBox" 
      :where="{ RecordUserId: UserId }" 
      url="/StorageRecord/List" 
      :limit="8"
    >
      <template v-slot:content="{ data }">
        <!-- 网格布局容器 宽度100% -->
        <div class="storage-grid-container">
          <!-- 寄存记录卡片 -->
          <div class="storage-card" v-for="(item, index) in data" :key="index">
            <!-- 卡片头部 -->
            <div class="card-header">
              <div class="card-no">📌 寄存单号：{{ item.No || '未知' }}</div>
              <div class="card-status" :class="item.TakeTime ? 'status-take' : 'status-stay'">
                {{ item.TakeTime ? '✅ 已取件' : '⏳ 寄存中' }}
              </div>
            </div>
  
            <!-- 卡片主体 -->
            <div class="card-body">
              <!-- 个人信息 -->
              <div class="info-group">
                <div class="info-item"><span>📦 物品信息：</span>{{ item.Name || '未知' }}</div>
                <div class="info-item"><span>📞 联系电话：</span>{{ item.Phone || '未知' }}</div>
                <div class="info-item"><span>⏰ 寄存时间：</span>{{ item.CreationTime }}</div>
                <div class="info-item" v-if="item.TakeTime">
                  <span>📤 取件时间：</span>{{ item.TakeTime }}
                </div>
              </div>
  
              <!-- 酒店信息 -->
              <div class="hotel-group" v-if="item.HotelDto">
                <div class="hotel-title">🏨 寄存酒店信息</div>
                <div class="hotel-info">
                  <div class="info-item"><span>🏛️ 酒店名称：</span>{{ item.HotelDto.Name }}</div>
                  <div class="info-item"><span>📍 酒店地址：</span>{{ item.HotelDto.Address }}</div>
                  <div class="info-item"><span>📝 入住提示：</span>{{ item.HotelDto.CheckInTip }}</div>
                  <div class="info-item"><span>🐾 宠物政策：</span>{{ item.HotelDto.IsPet ? '允许携带宠物' : '禁止携带宠物' }}</div>
                  <div class="info-item rich-text" v-if="item.HotelDto.Content">
                    <span>📖 酒店介绍：</span>
                    <div v-html="item.HotelDto.Content"></div>
                  </div>
                </div>
              </div>
  
              <!-- 取件凭证 -->
              <div class="image-group" v-if="item.TakePicture">
                <div class="image-title">📸 取件凭证</div>
                <div class="image-box">
                  <img 
                    :src="item.TakePicture" 
                    :onerror="`this.src='${NoImage}'`" 
                    alt="取件凭证"
                    class="certificate-img"
                  >
                </div>
              </div>
            </div>
  
            <!-- 卡片底部 -->
            <div class="card-footer">
              <el-button type="primary" plain size="small" @click="refreshData">
                🔄 刷新记录
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </PaginationBox>
  </template>
  
  <script setup>
  import defaultAvatar from '@/assets/默认头像.png';
  import NoImage from '@/assets/图片失效.png';
  import { Post, PostSingleUpdate } from '@/api/http';
  import { ref, reactive, nextTick, computed, onBeforeMount } from 'vue';
  import { useRouter, useRoute } from 'vue-router';
  import { useCommonStore } from "@/store";
  import { ElMessage, ElMessageBox } from 'element-plus';
  
  const router = useRouter();
  const route = useRoute();
  const commonStore = useCommonStore();
  const Token = computed(() => commonStore.Token);
  const UserInfo = computed(() => commonStore.UserInfo);
  const RoleType = computed(() => commonStore.RoleType);
  const UserId = computed(() => commonStore.UserId);
  
  // 分页组件实例
  const StorageRecordPaginationBox = ref(null);
  
  // 刷新数据
  const refreshData = () => {
    StorageRecordPaginationBox.value?.Reload();
    ElMessage.success('🔄 记录刷新成功');
  };
  
  onBeforeMount(async () => {});
  </script>
  
  <style scoped lang="scss">
  // 科技感主题色
  $primary: #165DFF;
  $success: #36CFC9;
  $warning: #FF7D00;
  $purple: #722ED1;
  $dark: #0F172A;
  $light: #F8FAFC;
  
  // 面包屑
  .breadcrumb-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #fff 0%, #f5f7fa 100%);
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
    
    .breadcrumb-label {
      font-size: 14px;
      color: #64748B;
      font-weight: 500;
    }
  }
  
  // 网格容器 必须width:100%
  .storage-grid-container {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
    gap: 20px;
    padding: 10px 0;
  }
  
  // 科技感卡片
  .storage-card {
    background: linear-gradient(135deg, #ffffff 0%, #f8faff 100%);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.1);
    border: 1px solid rgba(22, 93, 255, 0.1);
    overflow: hidden;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 12px 32px rgba(22, 93, 255, 0.15);
      border-color: $primary;
    }
  }
  
  // 卡片头部
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 20px;
    background: linear-gradient(90deg, $primary 0%, $purple 100%);
    color: white;
    
    .card-no {
      font-size: 16px;
      font-weight: 600;
    }
    
    .card-status {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;
      
      &.status-stay {
        background: rgba(255,255,255,0.2);
      }
      
      &.status-take {
        background: rgba($success, 0.3);
      }
    }
  }
  
  // 卡片主体
  .card-body {
    padding: 20px;
  }
  
  // 信息组
  .info-group {
    margin-bottom: 20px;
    
    .info-item {
      display: flex;
      align-items: center;
      padding: 8px 0;
      font-size: 14px;
      color: #334155;
      
      span {
        font-weight: 500;
        color: $primary;
        min-width: 100px;
      }
    }
  }
  
  // 酒店组
  .hotel-group {
    margin-bottom: 20px;
    padding: 15px;
    background: rgba(22, 93, 255, 0.05);
    border-radius: 12px;
    border-left: 3px solid $primary;
    
    .hotel-title {
      font-size: 15px;
      font-weight: 600;
      color: $dark;
      margin-bottom: 12px;
    }
    
    .hotel-info {
      .info-item {
        padding: 6px 0;
        font-size: 13px;
        
        span {
          min-width: 90px;
          color: $purple;
        }
      }
      
      .rich-text {
        line-height: 1.6;
        div {
          margin-top: 5px;
          color: #475569;
        }
      }
    }
  }
  
  // 图片组
  .image-group {
    .image-title {
      font-size: 15px;
      font-weight: 600;
      color: $dark;
      margin-bottom: 12px;
    }
    
    .image-box {
      width: 100%;
      height: 180px;
      border-radius: 12px;
      overflow: hidden;
      border: 1px dashed #CBD5E1;
      
      .certificate-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
  
  // 卡片底部
  .card-footer {
    padding: 15px 20px;
    border-top: 1px solid #F1F5F9;
    display: flex;
    justify-content: flex-end;
  }
  
  // 响应式
  @media (max-width: 768px) {
    .storage-grid-container {
      grid-template-columns: 1fr;
    }
  }
  </style>