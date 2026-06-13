<template>
  <div class="order-detail-page">
    <div class="container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/Front/MyAppointOrderList' }">我的预约订单</el-breadcrumb-item>
          <el-breadcrumb-item>订单详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 主要内容区 -->
      <div class="detail-grid" v-if="Object.keys(AppointDetail).length">
        <!-- 左侧：酒店 & 房型 & 入住人 -->
        <div class="left-col">
          <!-- 酒店信息卡片 -->
          <div class="card">
            <div class="card-header">
              <h2><i class="el-icon-office-building"></i> 酒店信息</h2>
              <div class="status-badge" :class="statusClass">
                <span>{{ AppointDetail.AppointStatusFormat }}</span>
              </div>
            </div>
            <div class="hotel-content">
              <div class="hotel-cover">
                <el-image 
                  :src="AppointDetail.HotelDto?.Cover || NoImage" 
                  fit="cover"
                  :preview-src-list="[AppointDetail.HotelDto?.Cover]"
                />
              </div>
              <div class="hotel-info">
                <h3>{{ AppointDetail.HotelDto?.Name || '未知酒店' }}</h3>
                <div class="address">
                  <i class="el-icon-location"></i>
                  {{ AppointDetail.HotelDto?.ProviceCityArea || '' }} {{ AppointDetail.HotelDto?.Address || '暂无地址' }}
                </div>
                <div class="services">
                  <span v-for="s in (AppointDetail.HotelDto?.Services || '').split(',')" :key="s" class="service-tag">
                    {{ s }}
                  </span>
                </div>
                <div class="hotel-features">
                  <span v-if="AppointDetail.HotelDto?.IsPet"><i class="el-icon-dog"></i> 允许携带宠物</span>
                  <span><i class="el-icon-check"></i> 酒店已认证</span>
                </div>
              </div>
            </div>
            <!-- 酒店相册轮播 -->
            <div class="hotel-gallery" v-if="hotelImages.length">
              <h4><i class="el-icon-picture"></i> 酒店相册</h4>
              <el-carousel height="200px" indicator-position="outside">
                <el-carousel-item v-for="(img, idx) in hotelImages" :key="idx">
                  <el-image :src="img" fit="cover" class="gallery-img" :preview-src-list="hotelImages" />
                </el-carousel-item>
              </el-carousel>
            </div>
            <!-- 酒店介绍（富文本） -->
            <div class="hotel-desc" v-if="AppointDetail.HotelDto?.Content">
              <h4><i class="el-icon-document"></i> 酒店介绍</h4>
              <div class="rich-text" v-html="AppointDetail.HotelDto.Content"></div>
            </div>
          </div>

          <!-- 房型信息卡片 -->
          <div class="card">
            <div class="card-header">
              <h2><i class="el-icon-s-home"></i> 房型信息</h2>
            </div>
            <div class="room-content">
              <div class="room-gallery">
                <el-carousel height="240px" indicator-position="outside">
                  <el-carousel-item v-for="(img, idx) in roomImages" :key="idx">
                    <el-image :src="img" fit="cover" class="gallery-img" :preview-src-list="roomImages" />
                  </el-carousel-item>
                </el-carousel>
              </div>
              <div class="room-info">
                <h3>{{ AppointDetail.RoomDto?.Title || '未知房型' }}</h3>
                <div class="room-tags">
                  <span class="tag">{{ AppointDetail.RoomDto?.RoomShapeFormat || '' }}</span>
                  <span class="tag">{{ AppointDetail.RoomDto?.FeaturedRoomShapeFormat || '' }}</span>
                  <span class="tag">{{ AppointDetail.RoomDto?.BreakfastTypeFormat || '' }}</span>
                </div>
                <div class="room-meta-grid">
                  <div class="meta-item">
                    <span class="label">面积</span>
                    <span class="value">{{ AppointDetail.RoomDto?.AreaSize || 0 }}㎡</span>
                  </div>
                  <div class="meta-item">
                    <span class="label">免费取消</span>
                    <span class="value">{{ AppointDetail.RoomDto?.IsFreeCancel ? '支持' : '不支持' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="label">立即确认</span>
                    <span class="value">{{ AppointDetail.RoomDto?.IsImmediatelyConfirm ? '是' : '否' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="label">积分抵现</span>
                    <span class="value">{{ AppointDetail.RoomDto?.Integral || 0 }}分 (¥{{ AppointDetail.RoomDto?.IntegralExchangeMoney || 0 }})</span>
                  </div>
                </div>
                <div class="facilities" v-if="facilityList.length">
                  <span class="label"><i class="el-icon-star-on"></i> 设施配备：</span>
                  <div class="facility-icons">
                    <span v-for="(f, idx) in facilityList" :key="idx" class="facility-item">
                      <i :class="getFacilityIcon(f)"></i> {{ f }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <!-- 房间介绍（富文本） -->
            <div class="room-desc" v-if="AppointDetail.RoomDto?.Content">
              <h4><i class="el-icon-reading"></i> 房间介绍</h4>
              <div class="rich-text" v-html="AppointDetail.RoomDto.Content"></div>
            </div>
          </div>

          <!-- 入住人信息 -->
          <div class="card" v-if="AppointDetail.AppointDetDtos?.length">
            <div class="card-header">
              <h2><i class="el-icon-user"></i> 入住人信息</h2>
            </div>
            <div class="guest-list">
              <div v-for="(guest, idx) in AppointDetail.AppointDetDtos" :key="idx" class="guest-item">
                <div class="guest-avatar">
                  <i class="el-icon-user-solid"></i>
                </div>
                <div class="guest-detail">
                  <div class="guest-name">{{ guest.Name }}</div>
                  <div class="guest-meta">
                    <span>性别：{{ guest.Sex || '未知' }}</span>
                    <span>手机：{{ guest.Phone || '未填' }}</span>
                    <span v-if="guest.IdCard">身份证：{{ guest.IdCard }}</span>
                    <span>年龄：{{ guest.Age || '未知' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：订单摘要 & 价格 & 温馨提示 -->
        <div class="right-col">
          <!-- 订单状态卡片 -->
          <div class="card order-summary">
            <div class="summary-header">
              <i class="el-icon-info"></i>
              <span>订单状态</span>
            </div>
            <div class="status-display" :class="statusClass">
              {{ AppointDetail.AppointStatusFormat }}
            </div>
            <div class="order-code">
              <span>订单号：</span>
              <span class="code">{{ AppointDetail.No }}</span>
            </div>
            <div class="create-time">
              <i class="el-icon-time"></i> 创建时间：{{ formatDateTime(AppointDetail.CreationTime) }}
            </div>
          </div>

          <!-- 价格明细卡片 -->
          <div class="card price-card">
            <div class="summary-header">
              <i class="el-icon-money"></i>
              <span>费用明细</span>
            </div>
            <div class="price-detail">
              <div class="price-row">
                <span>房型单价</span>
                <span>¥{{ (AppointDetail.RoomDto?.MinPrice || 0).toFixed(2) }}</span>
              </div>
              <div class="price-row">
                <span>入住天数</span>
                <span>{{ stayDays }}晚</span>
              </div>
              <div class="price-row">
                <span>房间数量</span>
                <span>× {{ AppointDetail.Qty || 1 }}</span>
              </div>
              <div class="price-row total">
                <span>订单总额</span>
                <span class="total-price">¥{{ (AppointDetail.TotalMoney || 0).toFixed(2) }}</span>
              </div>
              <div class="price-row" v-if="AppointDetail.PayTime">
                <span>支付时间</span>
                <span>{{ formatDateTime(AppointDetail.PayTime) }}</span>
              </div>
              <div class="price-row">
                <span>支付方式</span>
                <span>{{ getPayTypeName(AppointDetail.PayType) }}</span>
              </div>
            </div>
          </div>

          <!-- 预订信息卡片 -->
          <div class="card booking-info">
            <div class="summary-header">
              <i class="el-icon-calendar"></i>
              <span>行程信息</span>
            </div>
            <div class="date-range">
              <div class="date-item">
                <div class="date-label">入住</div>
                <div class="date-value">{{ formatDate(AppointDetail.BeginAppointTime) }}</div>
                <div class="time-value">{{ formatTime(AppointDetail.BeginAppointTime) }}</div>
              </div>
              <div class="date-arrow">→</div>
              <div class="date-item">
                <div class="date-label">离店</div>
                <div class="date-value">{{ formatDate(AppointDetail.EndAppointTime) }}</div>
                <div class="time-value">{{ formatTime(AppointDetail.EndAppointTime) }}</div>
              </div>
            </div>
            <div class="other-info">
              <div><i class="el-icon-user"></i> 入住人数：{{ AppointDetail.TotalPeopleNum || 0 }}人</div>
              <div><i class="el-icon-receiving"></i> 预订数量：{{ AppointDetail.Qty || 1 }}间</div>
            </div>
          </div>

          <!-- 酒店温馨提示 -->
          <div class="card notice-card" v-if="AppointDetail.HotelDto?.CheckInTip || AppointDetail.HotelDto?.MustRead || AppointDetail.HotelDto?.Notice">
            <div class="summary-header">
              <i class="el-icon-warning-outline"></i>
              <span>温馨提示</span>
            </div>
            <div class="notice-content">
              <div v-if="AppointDetail.HotelDto?.CheckInTip">
                <i class="el-icon-check"></i> 入住须知：{{ AppointDetail.HotelDto.CheckInTip }}
              </div>
              <div v-if="AppointDetail.HotelDto?.MustRead">
                <i class="el-icon-star-on"></i> 必读：{{ AppointDetail.HotelDto.MustRead }}
              </div>
              <div v-if="AppointDetail.HotelDto?.Notice">
                <i class="el-icon-bell"></i> 通知：{{ AppointDetail.HotelDto.Notice }}
              </div>
            </div>
          </div>

           <!-- 用户评价模块 -->
           <div class="card evaluate-card" v-if="evaluateInfo">
            <div class="summary-header">
              <i class="el-icon-star-on"></i>
              <span>入住人评价</span>
            </div>
            <div class="evaluate-content">
              <!-- 星级评分 -->
              <div class="evaluate-score">
                <i 
                  v-for="num in 5" 
                  :key="num"
                  class="el-icon-star-on"
                  :class="{ active: num <= evaluateInfo.Score }"
                ></i>
                <span class="score-num">{{ evaluateInfo.Score }}.0分</span>
              </div>
              <!-- 评价内容 -->
              <div class="evaluate-text">
                {{ evaluateInfo.Content || '该用户暂无文字评价' }}
              </div>
              <!-- 评价人信息 -->
              <div class="evaluate-user">
                <i class="el-icon-user-solid"></i>
                <span>{{ evaluateInfo.UserName || '匿名用户' }}</span>
                <span class="evaluate-time">{{ formatDateTime(evaluateInfo.CreationTime) }}</span>
              </div>
            </div>
          </div>
          

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="warning" round v-if="canCancel" @click="cancelOrder">
              <i class="el-icon-refresh-left"></i> 取消订单
            </el-button>
            <el-button type="primary" round v-if="canPay" @click="goPay">
              <i class="el-icon-circle-check"></i> 去支付
            </el-button>
          </div>
        </div>
      </div>

      <!-- 加载中/空状态 -->
      <div v-else class="loading-state">
        <el-skeleton :rows="8" animated />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onBeforeMount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCommonStore } from "@/store"
import { Post } from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import NoImage from '@/assets/图片失效.png'

const router = useRouter()
const route = useRoute()
const commonStore = useCommonStore()

const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)

const AppointId = ref(route.query.AppointId)
const AppointDetail = ref({})

// 酒店相册图片列表
const hotelImages = computed(() => {
  if (!AppointDetail.value.HotelDto?.HotelImages) return []
  return AppointDetail.value.HotelDto.HotelImages.split(',').filter(url => url && url.trim())
})

// 房间图片列表
const roomImages = computed(() => {
  if (!AppointDetail.value.RoomDto?.ImageUrls) return []
  return AppointDetail.value.RoomDto.ImageUrls.split(',').filter(url => url && url.trim())
})

// 设施列表
const facilityList = computed(() => {
  const ids = AppointDetail.value.RoomDto?.EquipmentInfoIds
  if (!ids) return []
  const mapping = {
    '1': 'WiFi', '2': '空调', '3': '电视', '4': '冰箱', '6': '浴缸', '8': '吹风机',
    '9': '保险箱', '12': '阳台', '14': '智能马桶', '16': '沙发', '17': '书桌', '18': '衣柜',
    '19': '茶具', '20': '迷你吧', '21': '咖啡机', '22': '空气净化器', '23': '加湿器',
    '24': '瑜伽垫', '26': '智能音箱', '27': '按摩椅', '28': '儿童床'
  }
  return ids.split(',').map(id => mapping[id.trim()] || `设施${id}`).filter(Boolean)
})

// 入住天数
const stayDays = computed(() => {
  if (!AppointDetail.value.BeginAppointTime || !AppointDetail.value.EndAppointTime) return 1
  const begin = new Date(AppointDetail.value.BeginAppointTime)
  const end = new Date(AppointDetail.value.EndAppointTime)
  const diff = Math.ceil((end - begin) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
})

// 状态样式类
const statusClass = computed(() => {
  const status = AppointDetail.value.AppointStatusFormat
  if (status === '待支付') return 'status-warning'
  if (status === '待入住') return 'status-info'
  if (status === '已入住') return 'status-success'
  if (status === '用户取消' || status === '酒店取消' || status === '系统取消') return 'status-danger'
  if (status === '完成' || status === '完成退房') return 'status-primary'
  return 'status-default'
})

// 是否可以取消订单
const canCancel = computed(() => {
  const status = AppointDetail.value.AppointStatus
  return [1, 6, 9].includes(status)
})

// 是否可以支付
const canPay = computed(() => {
  return AppointDetail.value.AppointStatusFormat === '待支付'
})

// 获取设施图标
const getFacilityIcon = (name) => {
  const map = {
    'WiFi': 'el-icon-wifi',
    '空调': 'el-icon-fan',
    '电视': 'el-icon-tv',
    '冰箱': 'el-icon-freezing',
    '浴缸': 'el-icon-swim',
    '吹风机': 'el-icon-blow-dryer',
    '沙发': 'el-icon-sofa',
    '书桌': 'el-icon-office',
    '咖啡机': 'el-icon-coffee-cup'
  }
  return map[name] || 'el-icon-star-on'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[1]?.substring(0, 5) || ''
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 16)
}

// 支付方式名称
const getPayTypeName = (type) => {
  const map = { WECHAT: '微信支付', ALIPAY: '支付宝支付', BANKCARD: '银行卡支付' }
  return map[type] || '未知'
}

// 获取订单详情
const GetAppointDetailApi = async () => {
  if (!AppointId.value) {
    ElMessage.error('订单参数缺失')
    router.push('/Front/MyAppointOrderList')
    return
  }
  const { Success, Data, Msg } = await Post('/Appoint/Get', { Id: AppointId.value })
  if (Success) {
    AppointDetail.value = Data
  } else {
    ElMessage.error(Msg || '获取订单详情失败')
    router.push('/Front/MyAppointOrderList')
  }
}

// 去支付
const goPay = () => {
  router.push({
    path: '/Front/Payment',
    query: { AppointId: AppointId.value, TotalMoney: AppointDetail.value.TotalMoney }
  })
}

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const { Success, Msg } = await Post('/Appoint/UserCancel', {
      Id: AppointDetail.value.Id,
      ToUserId: UserId.value
    })
    if (Success) {
      ElMessage.success('订单已取消')
      GetAppointDetailApi()
    } else {
      ElMessage.error(Msg || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}


onBeforeMount(() => {
  GetAppointDetailApi()
})
</script>

<style scoped lang="scss">
// 商务简约风主题
.order-detail-page {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 32px 0 48px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;

  .container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 24px;
  }

  // 面包屑
  .breadcrumb {
    margin-bottom: 24px;
    :deep(.el-breadcrumb__item) {
      .el-breadcrumb__inner {
        color: #606266;
        font-size: 14px;
        &:hover {
          color: #1890ff;
        }
      }
      &:last-child .el-breadcrumb__inner {
        color: #303133;
        font-weight: 500;
      }
    }
  }

  // 网格布局
  .detail-grid {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 24px;

    @media (max-width: 992px) {
      grid-template-columns: 1fr;
    }
  }

  // 基础卡片样式
  .card {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e8e8e8;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    transition: all 0.2s ease;
    overflow: hidden;
    margin-bottom: 24px;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .card-header {
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: #fafafa;
      h2 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        i {
          color: #1890ff;
          margin-right: 8px;
          font-size: 16px;
        }
      }
    }
  }

  // 酒店区域
  .hotel-content {
    display: flex;
    padding: 20px;
    gap: 20px;
    flex-wrap: wrap;
    .hotel-cover {
      width: 180px;
      height: 180px;
      border-radius: 6px;
      overflow: hidden;
      flex-shrink: 0;
      .el-image {
        width: 100%;
        height: 100%;
      }
    }
    .hotel-info {
      flex: 1;
      h3 {
        font-size: 20px;
        font-weight: 600;
        margin: 0 0 12px 0;
        color: #303133;
      }
      .address {
        color: #606266;
        margin-bottom: 12px;
        font-size: 14px;
        i {
          margin-right: 4px;
          color: #909399;
        }
      }
      .services {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 12px;
        .service-tag {
          background: #f5f7fa;
          padding: 2px 10px;
          border-radius: 4px;
          font-size: 12px;
          color: #606266;
          border: 1px solid #e8e8e8;
        }
      }
      .hotel-features {
        display: flex;
        gap: 20px;
        color: #909399;
        font-size: 13px;
        span {
          i {
            margin-right: 4px;
            color: #67c23a;
          }
        }
      }
    }
  }

  // 相册轮播
  .hotel-gallery, .room-content .room-gallery {
    padding: 0 20px 20px;
    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 14px;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 6px;
      i {
        color: #1890ff;
      }
    }
    .gallery-img {
      width: 100%;
      height: 100%;
      border-radius: 4px;
    }
  }

  // 富文本样式
  .rich-text {
    color: #606266;
    line-height: 1.6;
    font-size: 14px;
    padding: 8px 0;
    :deep(p) {
      margin: 0 0 10px;
    }
  }

  // 房型信息
  .room-content {
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 20px;
    .room-gallery {
      padding: 0;
    }
    .room-info {
      h3 {
        font-size: 18px;
        font-weight: 600;
        margin: 0 0 12px;
        color: #303133;
      }
      .room-tags {
        display: flex;
        gap: 10px;
        margin-bottom: 16px;
        .tag {
          background: #f0f7ff;
          padding: 3px 12px;
          border-radius: 4px;
          font-size: 12px;
          color: #1890ff;
        }
      }
      .room-meta-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
        margin-bottom: 20px;
        .meta-item {
          display: flex;
          justify-content: space-between;
          padding: 8px 0;
          border-bottom: 1px solid #f0f0f0;
          .label {
            color: #909399;
            font-size: 14px;
          }
          .value {
            color: #303133;
            font-weight: 500;
            font-size: 14px;
          }
        }
      }
      .facilities {
        .label {
          display: block;
          margin-bottom: 10px;
          color: #606266;
          font-size: 14px;
        }
        .facility-icons {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          .facility-item {
            background: #fafafa;
            padding: 5px 12px;
            border-radius: 4px;
            font-size: 13px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: #606266;
            i {
              color: #1890ff;
              font-size: 14px;
            }
          }
        }
      }
    }
  }

  // 入住人列表
  .guest-list {
    padding: 4px 20px 20px;
    .guest-item {
      display: flex;
      gap: 14px;
      padding: 14px 0;
      border-bottom: 1px solid #f0f0f0;
      &:last-child {
        border-bottom: none;
      }
      .guest-avatar {
        width: 40px;
        height: 40px;
        background: #f5f7fa;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        i {
          font-size: 20px;
          color: #909399;
        }
      }
      .guest-detail {
        flex: 1;
        .guest-name {
          font-size: 15px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 6px;
        }
        .guest-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 14px;
          font-size: 13px;
          color: #606266;
          span {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
    }
  }

  // 右侧卡片样式
  .order-summary, .price-card, .booking-info, .notice-card, .evaluate-card {
    .summary-header {
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      background: #fafafa;
      i {
        color: #1890ff;
      }
    }
    .status-display {
      text-align: center;
      font-size: 20px;
      font-weight: 600;
      padding: 20px;
      margin: 16px 20px;
      border-radius: 6px;
      letter-spacing: 1px;
      &.status-warning { color: #e6a23c; background: #fdf6ec; }
      &.status-info { color: #1890ff; background: #e6f7ff; }
      &.status-success { color: #67c23a; background: #f0f9ff; }
      &.status-danger { color: #f56c6c; background: #fef0f0; }
      &.status-primary { color: #909399; background: #f5f7fa; }
    }
    .order-code {
      text-align: center;
      padding: 0 20px 10px;
      font-size: 14px;
      color: #606266;
      .code {
        font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
        letter-spacing: 0.5px;
        background: #f5f7fa;
        padding: 3px 8px;
        border-radius: 4px;
        color: #303133;
      }
    }
    .create-time {
      text-align: center;
      padding: 0 20px 20px;
      color: #909399;
      font-size: 13px;
      i {
        margin-right: 4px;
      }
    }
  }

  .price-card .price-detail {
    padding: 10px 20px 20px;
    .price-row {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f5f5f5;
      color: #606266;
      font-size: 14px;
      &.total {
        margin-top: 8px;
        border-bottom: none;
        font-size: 15px;
        font-weight: 500;
        color: #303133;
        .total-price {
          color: #e6a23c;
          font-size: 22px;
          font-weight: 600;
        }
      }
    }
  }

  .booking-info {
    .date-range {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px;
      .date-item {
        text-align: center;
        .date-label {
          font-size: 13px;
          color: #909399;
          margin-bottom: 6px;
        }
        .date-value {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }
        .time-value {
          font-size: 13px;
          color: #606266;
          margin-top: 4px;
        }
      }
      .date-arrow {
        font-size: 18px;
        color: #dcdfe6;
        font-weight: 300;
      }
    }
    .other-info {
      padding: 0 20px 20px;
      display: flex;
      gap: 20px;
      color: #606266;
      font-size: 14px;
      i {
        margin-right: 4px;
        color: #909399;
      }
    }
  }

  .notice-card {
    .notice-content {
      padding: 16px 20px 20px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      color: #606266;
      font-size: 14px;
      div {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        i {
          margin-top: 2px;
          color: #e6a23c;
        }
      }
    }
  }

  // 评价模块
  .evaluate-card {
    .evaluate-content {
      padding: 16px 20px 20px;
      .evaluate-score {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        i {
          font-size: 18px;
          color: #e4e7ed;
          &.active {
            color: #f7ba2a;
          }
        }
        .score-num {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-left: 6px;
        }
      }
      .evaluate-text {
        color: #606266;
        font-size: 14px;
        line-height: 1.6;
        margin-bottom: 12px;
      }
      .evaluate-user {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #909399;
        font-size: 13px;
        i {
          font-size: 14px;
        }
        .evaluate-time {
          margin-left: auto;
          color: #c0c4cc;
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-top: 8px;
    .el-button {
      width: 100%;
      border-radius: 6px;
      height: 44px;
      font-weight: 500;
      font-size: 15px;
    }
  }

  .loading-state {
    background: #fff;
    border-radius: 8px;
    padding: 40px;
    border: 1px solid #e8e8e8;
  }
}
</style>