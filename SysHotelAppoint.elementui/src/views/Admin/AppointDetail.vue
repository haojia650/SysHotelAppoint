<template>
    <div class="order-detail-page">
      <!-- 背景装饰粒子效果（科技感） -->
      <div class="bg-particles"></div>
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
            <div class="card glass-card">
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
            <div class="card glass-card">
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
            <div class="card glass-card" v-if="AppointDetail.AppointDetDtos?.length">
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
            <div class="card glass-card order-summary">
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
            <div class="card glass-card price-card">
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
            <div class="card glass-card booking-info">
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
            <div class="card glass-card notice-card" v-if="AppointDetail.HotelDto?.CheckInTip || AppointDetail.HotelDto?.MustRead || AppointDetail.HotelDto?.Notice">
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
  
            <!-- 操作按钮 -->
            <div class="action-buttons">
              <el-button type="warning" round v-if="canCancel" @click="cancelOrder">
                <i class="el-icon-refresh-left"></i> 取消订单
              </el-button>
              <el-button type="success" round v-if="canPay" @click="goPay">
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
  
  // 设施列表（从 EquipmentInfoIds 提取简单展示，这里只做文本美化）
  const facilityList = computed(() => {
    const ids = AppointDetail.value.RoomDto?.EquipmentInfoIds
    if (!ids) return []
    // 假设设施ID映射为常见设施名称（仅为展示效果，实际可后端提供）
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
  
  // 是否可以取消订单（根据状态码，1=待入住,6=待确定,9=待支付）
  const canCancel = computed(() => {
    const status = AppointDetail.value.AppointStatus
    return [1, 6, 9].includes(status)
  })
  
  // 是否可以支付（仅待支付）
  const canPay = computed(() => {
    return AppointDetail.value.AppointStatusFormat === '待支付'
  })
  
  // 获取设施图标（简单映射）
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
  
  // 格式化日期（YYYY-MM-DD）
  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    return dateStr.split(' ')[0]
  }
  
  // 格式化时间（HH:MM）
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
  
  // 返回列表
  const goBack = () => {
    router.push('/Front/MyAppointOrderList')
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
        GetAppointDetailApi() // 刷新状态
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
  // 全局科技感深色主题
  .order-detail-page {
    position: relative;
    background: linear-gradient(135deg, #0a0f1f 0%, #0c1225 100%);
    min-height: 100vh;
    padding: 40px 0 60px;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  
    // 粒子背景装饰
    .bg-particles {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      background-image: radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.08) 1px, transparent 1px),
                        radial-gradient(circle at 80% 70%, rgba(64, 158, 255, 0.08) 1px, transparent 1px);
      background-size: 40px 40px, 60px 60px;
      z-index: 0;
    }
  
    .container {
      position: relative;
      z-index: 1;
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 24px;
    }
  
    // 面包屑
    .breadcrumb {
      margin-bottom: 32px;
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          color: rgba(255, 255, 255, 0.7);
          font-weight: 500;
          &:hover {
            color: #409eff;
          }
        }
        &:last-child .el-breadcrumb__inner {
          color: #409eff;
        }
      }
      :deep(.el-breadcrumb__separator) {
        color: rgba(255, 255, 255, 0.5);
      }
    }
  
    // 网格布局
    .detail-grid {
      display: grid;
      grid-template-columns: 1fr 360px;
      gap: 28px;
  
      @media (max-width: 992px) {
        grid-template-columns: 1fr;
      }
    }
  
    // 玻璃卡片
    .glass-card {
      background: rgba(18, 25, 45, 0.75);
      backdrop-filter: blur(12px);
      border-radius: 28px;
      border: 1px solid rgba(64, 158, 255, 0.2);
      box-shadow: 0 20px 35px -15px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(64, 158, 255, 0.1) inset;
      transition: all 0.3s ease;
      overflow: hidden;
      margin-bottom: 28px;
  
      &:hover {
        transform: translateY(-2px);
        border-color: rgba(64, 158, 255, 0.4);
        box-shadow: 0 25px 40px -15px rgba(0, 0, 0, 0.4);
      }
  
      .card-header {
        padding: 20px 24px;
        border-bottom: 1px solid rgba(64, 158, 255, 0.15);
        display: flex;
        justify-content: space-between;
        align-items: center;
        h2 {
          margin: 0;
          font-size: 1.5rem;
          font-weight: 600;
          background: linear-gradient(135deg, #fff, #a0c4ff);
          -webkit-background-clip: text;
          background-clip: text;
          color: transparent;
          i {
            background: none;
            color: #409eff;
            margin-right: 10px;
            font-size: 1.4rem;
          }
        }
      }
    }
  
    // 酒店区域
    .hotel-content {
      display: flex;
      padding: 24px;
      gap: 24px;
      flex-wrap: wrap;
      .hotel-cover {
        width: 200px;
        height: 200px;
        border-radius: 20px;
        overflow: hidden;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
        flex-shrink: 0;
        .el-image {
          width: 100%;
          height: 100%;
        }
      }
      .hotel-info {
        flex: 1;
        h3 {
          font-size: 1.8rem;
          font-weight: 700;
          margin: 0 0 12px 0;
          background: linear-gradient(120deg, #fff, #c0e0ff);
          -webkit-background-clip: text;
          background-clip: text;
          color: transparent;
        }
        .address {
          color: #b0c4ff;
          margin-bottom: 16px;
          i {
            margin-right: 6px;
          }
        }
        .services {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          margin-bottom: 16px;
          .service-tag {
            background: rgba(64, 158, 255, 0.15);
            padding: 4px 12px;
            border-radius: 30px;
            font-size: 0.8rem;
            color: #b0d4ff;
            border: 1px solid rgba(64, 158, 255, 0.3);
          }
        }
        .hotel-features {
          display: flex;
          gap: 20px;
          color: #8aa9ff;
          font-size: 0.9rem;
          span {
            i {
              margin-right: 6px;
            }
          }
        }
      }
    }
  
    // 相册轮播
    .hotel-gallery, .room-content .room-gallery {
      padding: 0 24px 24px;
      h4 {
        margin: 0 0 12px 0;
        color: #c0e0ff;
        display: flex;
        align-items: center;
        gap: 8px;
        i {
          color: #409eff;
        }
      }
      .gallery-img {
        width: 100%;
        height: 100%;
        border-radius: 16px;
      }
    }
  
    // 富文本样式
    .rich-text {
      color: #cdd9ff;
      line-height: 1.6;
      font-size: 0.95rem;
      padding: 8px 0;
      :deep(p) {
        margin: 0 0 12px;
      }
    }
  
    // 房型信息
    .room-content {
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 24px;
      .room-gallery {
        padding: 0;
      }
      .room-info {
        h3 {
          font-size: 1.6rem;
          font-weight: 600;
          margin: 0 0 12px;
          color: #fff;
        }
        .room-tags {
          display: flex;
          gap: 12px;
          margin-bottom: 20px;
          .tag {
            background: rgba(64, 158, 255, 0.2);
            padding: 4px 16px;
            border-radius: 40px;
            font-size: 0.85rem;
            color: #8bb9ff;
          }
        }
        .room-meta-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;
          margin-bottom: 24px;
          .meta-item {
            display: flex;
            justify-content: space-between;
            border-bottom: 1px dashed rgba(64, 158, 255, 0.2);
            padding: 8px 0;
            .label {
              color: #99b4e6;
            }
            .value {
              color: #fff;
              font-weight: 500;
            }
          }
        }
        .facilities {
          .label {
            display: block;
            margin-bottom: 12px;
            color: #99b4e6;
          }
          .facility-icons {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            .facility-item {
              background: rgba(255, 255, 255, 0.05);
              padding: 6px 14px;
              border-radius: 30px;
              font-size: 0.85rem;
              display: inline-flex;
              align-items: center;
              gap: 8px;
              i {
                color: #409eff;
              }
            }
          }
        }
      }
    }
  
    // 入住人列表
    .guest-list {
      padding: 8px 24px 24px;
      .guest-item {
        display: flex;
        gap: 16px;
        padding: 16px 0;
        border-bottom: 1px solid rgba(64, 158, 255, 0.1);
        &:last-child {
          border-bottom: none;
        }
        .guest-avatar {
          width: 48px;
          height: 48px;
          background: rgba(64, 158, 255, 0.2);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          i {
            font-size: 28px;
            color: #409eff;
          }
        }
        .guest-detail {
          flex: 1;
          .guest-name {
            font-size: 1.1rem;
            font-weight: 600;
            color: #fff;
            margin-bottom: 6px;
          }
          .guest-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
            font-size: 0.85rem;
            color: #b0c4ff;
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
    .order-summary, .price-card, .booking-info, .notice-card {
      .summary-header {
        padding: 20px 24px;
        border-bottom: 1px solid rgba(64, 158, 255, 0.15);
        display: flex;
        align-items: center;
        gap: 10px;
        font-size: 1.2rem;
        font-weight: 600;
        color: #c0e0ff;
        i {
          color: #409eff;
        }
      }
      .status-display {
        text-align: center;
        font-size: 1.6rem;
        font-weight: 700;
        padding: 20px;
        background: rgba(0,0,0,0.2);
        margin: 20px 24px;
        border-radius: 24px;
        letter-spacing: 2px;
        &.status-warning { color: #e6a23c; text-shadow: 0 0 5px rgba(230,162,60,0.5); }
        &.status-info { color: #409eff; text-shadow: 0 0 5px rgba(64,158,255,0.5); }
        &.status-success { color: #67c23a; text-shadow: 0 0 5px rgba(103,194,58,0.5); }
        &.status-danger { color: #f56c6c; text-shadow: 0 0 5px rgba(245,108,108,0.5); }
        &.status-primary { color: #909399; }
      }
      .order-code {
        text-align: center;
        padding: 0 24px 12px;
        font-size: 0.9rem;
        color: #b0c4ff;
        .code {
          font-family: monospace;
          letter-spacing: 1px;
          background: rgba(0,0,0,0.3);
          padding: 4px 8px;
          border-radius: 8px;
        }
      }
      .create-time {
        text-align: center;
        padding: 0 24px 24px;
        color: #9aaec9;
        font-size: 0.85rem;
        i {
          margin-right: 6px;
        }
      }
    }
  
    .price-card .price-detail {
      padding: 12px 24px 24px;
      .price-row {
        display: flex;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px dashed rgba(64, 158, 255, 0.15);
        color: #cdd9ff;
        &.total {
          margin-top: 12px;
          border-bottom: none;
          font-size: 1.2rem;
          font-weight: bold;
          .total-price {
            color: #e6a23c;
            font-size: 1.4rem;
          }
        }
      }
    }
  
    .booking-info {
      .date-range {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px 24px;
        .date-item {
          text-align: center;
          .date-label {
            font-size: 0.8rem;
            color: #8aa9ff;
            margin-bottom: 6px;
          }
          .date-value {
            font-size: 1.2rem;
            font-weight: 600;
            color: #fff;
          }
          .time-value {
            font-size: 0.8rem;
            color: #b0c4ff;
            margin-top: 4px;
          }
        }
        .date-arrow {
          font-size: 1.5rem;
          color: #409eff;
        }
      }
      .other-info {
        padding: 0 24px 24px;
        display: flex;
        gap: 24px;
        color: #b0c4ff;
        i {
          margin-right: 6px;
        }
      }
    }
  
    .notice-card {
      .notice-content {
        padding: 20px 24px 24px;
        display: flex;
        flex-direction: column;
        gap: 14px;
        color: #b0c4ff;
        div {
          display: flex;
          align-items: flex-start;
          gap: 10px;
          i {
            margin-top: 2px;
            color: #e6a23c;
          }
        }
      }
    }
  
    .action-buttons {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-top: 12px;
      .el-button {
        width: 100%;
        border-radius: 40px;
        height: 48px;
        font-weight: 500;
        backdrop-filter: blur(8px);
        transition: all 0.2s;
        &.el-button--primary {
          background: linear-gradient(90deg, #409eff, #1677ff);
          border: none;
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }
        &.el-button--warning {
          background: linear-gradient(90deg, #e6a23c, #cf8e1f);
          border: none;
        }
        &.el-button--success {
          background: linear-gradient(90deg, #67c23a, #5daf2c);
          border: none;
        }
      }
    }
  
    .loading-state {
      background: rgba(18, 25, 45, 0.6);
      backdrop-filter: blur(8px);
      border-radius: 28px;
      padding: 40px;
    }
  }
  </style>