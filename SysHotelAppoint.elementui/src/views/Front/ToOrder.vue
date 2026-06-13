<template>
  <div class="hotel-book-container">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <span class="breadcrumb-label">您当前的位置:</span>
      <el-breadcrumb separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/Front/HotelDetail', query: { HotelId: route.query.HotelId } }">酒店详情</el-breadcrumb-item>
        <el-breadcrumb-item>酒店预订</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 预订步骤条 -->
    <div class="book-steps">
      <el-steps :active="1" finish-status="success" align-center>
        <el-step title="选择房型" icon="el-icon-house" />
        <el-step title="填写预订信息" icon="el-icon-user" />
        <el-step title="确认订单" icon="el-icon-check" />
        <el-step title="支付完成" icon="el-icon-circle-check" />
      </el-steps>
    </div>

    <!-- 核心信息展示区域 -->
    <div class="main-info-card">
      <!-- 酒店基础信息 -->
      <div class="hotel-base-info" v-if="Room.HotelDto">
        <div class="hotel-header">
          <img
            :src="Room.HotelDto.Cover || NoImage"
            alt="酒店封面"
            class="hotel-cover"
            @error="$event.target.src = NoImage"
          >
          <div class="hotel-info">
            <h2 class="hotel-name">{{ Room.HotelDto.Name || '未知酒店' }}</h2>
            <div class="hotel-address">
              <i class="el-icon-location"></i>
              {{ Room.HotelDto.ProviceCityArea || '' }} {{ Room.HotelDto.Address || '暂无地址' }}
            </div>
            <div class="hotel-tips">
              <el-tag size="small" type="info" v-if="Room.HotelDto.IsPet">可携带宠物</el-tag>
              <el-tag size="small" type="warning">{{ Room.HotelDto.CheckInTip || '暂无入住提示' }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 房型核心信息 -->
      <div class="room-core-info" v-if="Room.Id">
        <div class="room-header">
          <h3 class="room-title">
            {{ Room.Title || '未知房型' }}
            <span class="room-type">({{ Room.RoomShapeFormat || '未知房型' }} | {{ Room.FeaturedRoomShapeFormat || '普通房型' }})</span>
          </h3>
        </div>

        <div class="room-meta">
          <div class="meta-item">
            <span class="label">当前价格：</span>
            <span class="value price">¥{{ Room.ActualPrice || 0 }}</span>
            <span class="original-price" v-if="Room.OriginalPrice > Room.ActualPrice">原价 ¥{{ Room.OriginalPrice || 0 }}</span>
          </div>
          <div class="meta-item">
            <span class="label">剩余房间：</span>
            <span class="value">{{ Room.RemainingRoomNumber || 0 }}间</span>
          </div>
          <div class="meta-item">
            <span class="label">房间面积：</span>
            <span class="value">{{ Room.AreaSize || 0 }}㎡</span>
          </div>
          <div class="meta-item">
            <span class="label">早餐配置：</span>
            <span class="value">{{ Room.BreakfastTypeFormat || '无早餐' }}</span>
          </div>
          <div class="meta-item">
            <span class="label">积分：</span>
            <span class="value">{{ Room.Integral || 0 }}分/间 (可抵¥{{ Room.IntegralExchangeMoney || 0 }}/间)</span>
          </div>
        </div>

        <div class="room-benefits">
          <el-tag type="success" size="small" v-if="Room.IsFreeCancel">
            <i class="el-icon-refresh"></i> 免费取消
          </el-tag>
          <el-tag type="primary" size="small" v-if="Room.IsImmediatelyConfirm">
            <i class="el-icon-clock"></i> 立即确认
          </el-tag>
        </div>

        <!-- 房型图片预览 -->
        <div class="room-images" v-if="roomImageList.length">
          <h4 class="image-title">房型图片</h4>
          <div class="image-list">
            <img
              v-for="(img, index) in roomImageList"
              :key="index"
              :src="img"
              alt="房型图片"
              class="room-img"
              @error="$event.target.src = NoImage"
            >
          </div>
        </div>

        <!-- 房型描述 -->
        <div class="room-desc" v-if="Room.Content">
          <h4 class="desc-title">房型介绍</h4>
          <div class="desc-content" v-html="Room.Content"></div>
        </div>

        <!-- 预订数量选择 -->
        <div class="book-qty" v-if="Room.RemainingRoomNumber > 0">
          <h4 class="section-title">预订数量</h4>
          <el-input-number
            v-model="bookQty"
            :min="1"
            :max="Room.RemainingRoomNumber"
            :disabled="Room.RemainingRoomNumber <= 0"
            size="default"
            @change="handleQtyChange"
          />
          <span class="qty-tips">剩余可订：{{ Room.RemainingRoomNumber }}间</span>
        </div>

        <!-- 积分抵扣区域（按房间数量倍增） -->
        <div class="points-deduction-section" v-if="totalMaxDeductMoney > 0">
          <h4 class="section-title">积分抵扣</h4>
          <div class="points-info">
            <div class="points-summary">
              <div class="points-balance">
                <i class="el-icon-medal"></i>
                我的积分：<span class="points-num">{{ userPoints }}</span> 分
              </div>
              <div class="points-rate">
                兑换规则：每间房可用 {{ Room.Integral }} 积分抵扣 ¥{{ Room.IntegralExchangeMoney }}
                <span v-if="bookQty > 1"> ({{ bookQty }}间房共可用 {{ totalMaxPoints }} 积分抵扣 ¥{{ totalMaxDeductMoney }})</span>
              </div>
            </div>
            
            <div class="deduction-control" v-if="isPointsSufficient">
              <el-checkbox v-model="usePoints" @change="handleUsePointsChange">
                使用积分抵扣
              </el-checkbox>
              <div class="deduction-amount">
                可抵扣金额：<span class="deduction-value">¥{{ actualDeductMoney }}</span>
                <span class="deduction-tip" v-if="maxDeductMoneyByPoints < totalMaxDeductMoney">
                  (积分不足，仅可抵扣¥{{ maxDeductMoneyByPoints }})
                </span>
              </div>
              <div class="points-cost" v-if="usePoints">
                消耗积分：<span class="cost-value">{{ pointsCost }}</span> 分
              </div>
            </div>
            
            <div class="points-insufficient" v-else>
              <el-alert
                title="积分不足"
                type="warning"
                :closable="false"
                show-icon
              >
                <template #default>
                  当前积分 {{ userPoints }} 分，抵扣 ¥{{ totalMaxDeductMoney }} 需要 {{ requiredPoints }} 积分
                </template>
              </el-alert>
            </div>
          </div>
        </div>

        <!-- 旅客信息选择区域 -->
        <div class="guest-info-section">
          <h4 class="section-title">
            选择入住人
            <el-button type="text" size="small" class="add-guest-btn" @click="handleAddGuest">+ 添加新入住人</el-button>
          </h4>
          
          <!-- 旅客列表 -->
          <div class="guest-list" v-if="GuestInfoList.length">
            <div
              class="guest-item"
              v-for="guest in GuestInfoList"
              :key="guest.Id"
              :class="{ selected: selectedGuestIds.includes(guest.Id) }"
              @click="toggleGuestSelect(guest.Id)"
            >
              <el-checkbox
                v-model="selectedGuestIds"
                :label="guest.Id"
                class="guest-checkbox"
              />
              <div class="guest-detail">
                <div class="guest-name">
                  <span class="label">姓名：</span>
                  <span class="value">{{ guest.Name || '未填写' }}</span>
                </div>
                <div class="guest-idcard">
                  <span class="label">身份证：</span>
                  <span class="value">{{ formatIdCard(guest.IdCard) }}</span>
                </div>
                <div class="guest-phone">
                  <span class="label">手机号：</span>
                  <span class="value">{{ formatPhone(guest.Phone) }}</span>
                </div>
                <div class="guest-sex-birth">
                  <span class="value">{{ guest.Sex || '未知' }} | {{ formatDate(guest.Birth) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div class="empty-guest" v-else>
            <el-empty description="暂无入住人信息，请先添加"></el-empty>
          </div>
        </div>

        <!-- 支付方式选择区域 -->
        <div class="pay-type-section">
          <h4 class="section-title">选择支付方式</h4>
          <div class="pay-card-list">
            <!-- 微信支付 -->
            <div
              class="pay-card"
              :class="{ selected: selectedPayType === 'WECHAT' }"
              @click="selectedPayType = 'WECHAT'"
            >
              <div class="pay-icon">
                <i class="el-icon-wechat"></i>
              </div>
              <div class="pay-name">微信支付</div>
              <el-radio
                v-model="selectedPayType"
                label="WECHAT"
                class="pay-radio"
              />
            </div>

            <!-- 支付宝支付 -->
            <div
              class="pay-card"
              :class="{ selected: selectedPayType === 'ALIPAY' }"
              @click="selectedPayType = 'ALIPAY'"
            >
              <div class="pay-icon alipay-icon">
                <i class="el-icon-alipay"></i>
              </div>
              <div class="pay-name">支付宝支付</div>
              <el-radio
                v-model="selectedPayType"
                label="ALIPAY"
                class="pay-radio"
              />
            </div>

            <!-- 银行卡支付 -->
            <div
              class="pay-card"
              :class="{ selected: selectedPayType === 'BANKCARD' }"
              @click="selectedPayType = 'BANKCARD'"
            >
              <div class="pay-icon bank-icon">
                <i class="el-icon-bank-card"></i>
              </div>
              <div class="pay-name">银行卡支付</div>
              <el-radio
                v-model="selectedPayType"
                label="BANKCARD"
                class="pay-radio"
              />
            </div>
          </div>
        </div>

        <!-- 提交区域 -->
        <div class="submit-section">
          <div class="total-amount">
            <span class="label">订单总价：</span>
            <span class="amount">¥{{ finalAmount || 0 }}</span>
            <span class="original-amount" v-if="usePoints && actualDeductMoney > 0">
              原价 ¥{{ totalAmount }}
            </span>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            @click="submitOrder"
            :disabled="!canSubmit"
          >
            确认提交订单
          </el-button>
        </div>
      </div>

      <!-- 加载/空状态 -->
      <div class="empty-state" v-else>
        <el-empty description="暂无房型信息，请稍后重试"></el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import NoImage from '@/assets/图片失效.png';
import { Post } from '@/api/http';
import { ref, computed, onBeforeMount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from "@/store";
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();

// 状态管理数据
const UserId = computed(() => commonStore.UserId);
const UserInfo = computed(() => commonStore.UserInfo);

// 房间信息（包含酒店嵌套信息）
const Room = ref({});
// 旅客信息列表
const GuestInfoList = ref([]);
// 房型图片列表
const roomImageList = ref([]);

// 预订相关响应式数据
const bookQty = ref(1);
const selectedGuestIds = ref([]);
const selectedPayType = ref('');

// 积分相关
const userPoints = ref(0);      // 用户当前积分
const usePoints = ref(false);   // 是否使用积分抵扣

// ---------- 积分抵扣计算（基于房间数量倍增）----------
// 总最大可用积分（所有房间累加）
const totalMaxPoints = computed(() => {
  return (Room.value.Integral || 0) * bookQty.value;
});

// 总最大抵扣金额（所有房间累加）
const totalMaxDeductMoney = computed(() => {
  return (Room.value.IntegralExchangeMoney || 0) * bookQty.value;
});

// 每元所需积分（房型自己的兑换比例）
const pointsPerYuan = computed(() => {
  if (!Room.value.IntegralExchangeMoney || Room.value.IntegralExchangeMoney === 0) return 0;
  return (Room.value.Integral || 0) / Room.value.IntegralExchangeMoney;
});

// 根据用户积分计算最大可抵扣金额（不超过总最大抵扣金额）
const maxDeductMoneyByPoints = computed(() => {
  if (pointsPerYuan.value === 0) return 0;
  const maxByPoints = Math.floor(userPoints.value / pointsPerYuan.value);
  return Math.min(maxByPoints, totalMaxDeductMoney.value);
});

// 实际抵扣金额（勾选后使用最大值）
const actualDeductMoney = computed(() => {
  if (!usePoints.value) return 0;
  return maxDeductMoneyByPoints.value;
});

// 消耗的积分（实际抵扣金额 × 每元所需积分）
const pointsCost = computed(() => {
  return Math.floor(actualDeductMoney.value * pointsPerYuan.value);
});

// 所需积分（抵扣总最大抵扣金额需要的积分）
const requiredPoints = computed(() => {
  return totalMaxPoints.value;
});

// 积分是否充足（用户积分 >= 总最大可用积分）
const isPointsSufficient = computed(() => {
  return userPoints.value >= totalMaxPoints.value;
});

// ---------- 订单金额相关 ----------
// 订单总价（未抵扣）
const totalAmount = computed(() => {
  return (Room.value.ActualPrice || 0) * bookQty.value;
});

// 最终支付金额（抵扣后）
const finalAmount = computed(() => {
  let amount = totalAmount.value - actualDeductMoney.value;
  return amount > 0 ? amount.toFixed(2) : '0.00';
});

// 是否可以提交订单
const canSubmit = computed(() => {
  return (
    Room.value.Id &&
    selectedGuestIds.value.length > 0 &&
    selectedPayType.value &&
    bookQty.value >= 1 &&
    bookQty.value <= (Room.value.RemainingRoomNumber || 0)
  );
});

// ---------- API 调用 ----------
// 获取房间信息
const GetRoomByUserApi = async () => {
  try {
    const { Data, Success, Msg } = await Post("/Room/UserGet", {
      Id: route.query.RoomId, // ✅ 关键：必须传大写I的Id，对应@JsonProperty("Id")
      HotelId: route.query.HotelId, // ✅ 必须传大写H的HotelId，对应@JsonProperty("HotelId")
      SelectDateList: [route.query.CheckInDate, route.query.CheckOutDate] // ✅ 必须传大写S的SelectDateList
    });

    if (Success) {
      Room.value = Data;
      if (Data.ImageUrls) {
        roomImageList.value = Data.ImageUrls.split(',').filter(url => url.trim() !== '');
      }
    } else {
      ElMessage.error(Msg || '获取房型信息失败');
    }
  } catch (error) {
    console.error('获取房型信息异常：', error);
    ElMessage.error('网络异常，获取房型信息失败');
  }
};

// 获取旅客信息列表
const GetGuestInfoListApi = async () => {
  try {
    const { Data, Success, Msg } = await Post("/GuestInfo/List", {
      BelongUserId: UserId.value
    });
    if (Success) {
      GuestInfoList.value = Data.Items || [];
    } else {
      ElMessage.error(Msg || '获取入住人信息失败');
    }
  } catch (error) {
    console.error('获取入住人信息异常：', error);
    ElMessage.error('网络异常，获取入住人信息失败');
  }
};

// 获取用户当前积分
const GetUserPointsApi = async () => {
  try {
    if (UserInfo.value && UserInfo.value.Points !== undefined) {
      userPoints.value = UserInfo.value.Points;
      return;
    }
    const { Data, Success, Msg } = await Post("/HotelIntegral/GetUserHotelIntegral", {
      UserId: UserId.value
    });
    if (Success && Data) {
      userPoints.value = Data.Points || Data || 0;
    } else {
      console.warn('获取用户积分失败：', Msg);
      userPoints.value = 0;
    }
  } catch (error) {
    console.error('获取用户积分异常：', error);
    userPoints.value = 0;
  }
};

// ---------- 交互事件 ----------
// 切换旅客选中
const toggleGuestSelect = (guestId) => {
  const index = selectedGuestIds.value.indexOf(guestId);
  if (index > -1) {
    selectedGuestIds.value.splice(index, 1);
  } else {
    selectedGuestIds.value.push(guestId);
  }
};

// 预订数量变化
const handleQtyChange = (val) => {
  bookQty.value = val || 1;
  // 数量变化后，如果已勾选积分抵扣但积分不足，自动取消勾选
  if (usePoints.value && !isPointsSufficient.value) {
    usePoints.value = false;
    ElMessage.warning(`预订${bookQty.value}间房需要${totalMaxPoints.value}积分，当前积分不足，已取消积分抵扣`);
  }
};

// 积分抵扣勾选变化
const handleUsePointsChange = (val) => {
  if (val && !isPointsSufficient.value) {
    ElMessage.warning(`积分不足，需要${totalMaxPoints.value}积分，当前仅有${userPoints.value}积分`);
    usePoints.value = false;
  } else if (val && maxDeductMoneyByPoints.value <= 0) {
    ElMessage.warning('无可抵扣金额');
    usePoints.value = false;
  }
};

// 添加新入住人
const handleAddGuest = () => {
  router.push({
    path: '/Front/GuestInfoAdd',
    query: { returnUrl: route.fullPath }
  });
};

// 格式化身份证号
const formatIdCard = (idCard) => {
  if (!idCard) return '未填写';
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2');
};

// 格式化手机号
const formatPhone = (phone) => {
  if (!phone) return '未填写';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未填写';
  return dateStr.split(' ')[0] || dateStr;
};

// 提交订单
const submitOrder = () => {
  if (bookQty.value < 1) {
    ElMessage.warning('预订房间数量不能小于1');
    return;
  }

  if (usePoints.value && !isPointsSufficient.value) {
    ElMessage.warning('积分不足，无法使用积分抵扣');
    return;
  }

  let confirmMessage = '确认提交订单吗？';
  if (usePoints.value && actualDeductMoney.value > 0) {
    confirmMessage = `确认提交订单吗？将使用${pointsCost.value}积分抵扣¥${actualDeductMoney.value}，实际支付¥${finalAmount.value}`;
  }
  
  ElMessageBox.confirm(confirmMessage, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const orderParams = {
      ToUserId: UserId.value,
      RoomId: route.query.RoomId,
      HotelId: route.query.HotelId,
      BeginAppointTime: route.query.CheckInDate,
      EndAppointTime: route.query.CheckOutDate,
      GuestIds: selectedGuestIds.value,
      PayType: selectedPayType.value,
      Qty: bookQty.value,
      TotalPeopleNum: selectedGuestIds.value.length,
    };
    
    if (usePoints.value && actualDeductMoney.value > 0) {
      orderParams.UsePoints = true;
      orderParams.PointsDeductAmount = actualDeductMoney.value;
      orderParams.PointsCost = pointsCost.value;
      // 可选的：传递每间房的积分使用量（如果需要后端校验）
      orderParams.PointsPerRoom = Room.value.Integral;
      orderParams.PointsDeductPerRoom = Room.value.IntegralExchangeMoney;
    }
    
    const { Data, Success, Msg } = await Post("/Appoint/ToOrder", orderParams);

    if (Success) {
      router.push({
        path: '/Front/Payment',
        query: {
          AppointId: Data.Id,
          HotelId: route.query.HotelId,
          RoomId: route.query.RoomId,
          FinalAmount: finalAmount.value,
          UsedPoints: usePoints.value ? pointsCost.value : 0
        }
      });
      ElMessage.success('订单提交成功，即将跳转到确认订单页');
    } else {
      ElMessage.error(Msg || '订单提交失败');
    }
  }).catch(() => {
    ElMessage.info('已取消订单提交');
  });
};

// 页面初始化
onBeforeMount(async () => {
  await Promise.all([
    GetRoomByUserApi(),
    GetGuestInfoListApi(),
    GetUserPointsApi()
  ]);
});
</script>

<style scoped lang="scss">
.hotel-book-container {
  width: 95%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
}

.breadcrumb {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  .breadcrumb-label {
    margin-right: 10px;
    color: #666;
  }
}

.book-steps {
  margin-bottom: 30px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  :deep(.el-steps) {
    --el-steps-finish-color: #409eff;
    --el-steps-process-color: #409eff;
  }
}

.main-info-card {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.hotel-base-info {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  .hotel-header {
    display: flex;
    align-items: center;
    gap: 20px;
    flex-wrap: wrap;
    .hotel-cover {
      width: 120px;
      height: 120px;
      border-radius: 8px;
      object-fit: cover;
    }
    .hotel-info {
      flex: 1;
      .hotel-name {
        font-size: 22px;
        font-weight: 600;
        color: #333;
        margin: 0 0 10px 0;
      }
      .hotel-address {
        display: flex;
        align-items: center;
        color: #666;
        margin-bottom: 10px;
        i {
          color: #409eff;
          margin-right: 5px;
        }
      }
      .hotel-tips {
        display: flex;
        gap: 10px;
      }
    }
  }
}

.room-core-info {
  .room-header {
    margin-bottom: 20px;
    .room-title {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
      .room-type {
        font-size: 14px;
        font-weight: normal;
        color: #666;
        margin-left: 10px;
      }
    }
  }

  .room-meta {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
    margin-bottom: 20px;
    .meta-item {
      display: flex;
      align-items: center;
      .label {
        color: #666;
        margin-right: 8px;
        min-width: 80px;
      }
      .value {
        color: #333;
        font-weight: 500;
      }
      .price {
        color: #e6a23c;
        font-size: 16px;
      }
      .original-price {
        color: #999;
        font-size: 12px;
        text-decoration: line-through;
        margin-left: 8px;
      }
    }
  }

  .room-benefits {
    margin-bottom: 25px;
    display: flex;
    gap: 10px;
  }

  .room-images {
    margin-bottom: 25px;
    .image-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 15px;
    }
    .image-list {
      display: flex;
      gap: 10px;
      flex-wrap: wrap;
      .room-img {
        width: 150px;
        height: 100px;
        border-radius: 4px;
        object-fit: cover;
        cursor: pointer;
        transition: all 0.3s;
        &:hover {
          transform: scale(1.05);
        }
      }
    }
  }

  .room-desc {
    margin-bottom: 25px;
    .desc-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin-bottom: 15px;
    }
    .desc-content {
      color: #666;
      line-height: 1.8;
    }
  }

  .book-qty {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 15px 0;
    }
    .qty-tips {
      margin-left: 15px;
      color: #666;
      font-size: 14px;
    }
  }

  // 积分抵扣样式（按房间数量倍增）
  .points-deduction-section {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 15px 0;
    }
    .points-info {
      .points-summary {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px;
        background: #fff;
        border-radius: 6px;
        margin-bottom: 15px;
        flex-wrap: wrap;
        gap: 10px;
        .points-balance {
          font-size: 14px;
          color: #666;
          i {
            color: #e6a23c;
            margin-right: 5px;
          }
          .points-num {
            font-size: 18px;
            font-weight: 600;
            color: #e6a23c;
            margin: 0 3px;
          }
        }
        .points-rate {
          font-size: 12px;
          color: #999;
          .rate-value {
            color: #409eff;
            font-weight: 500;
          }
        }
      }
      .deduction-control {
        padding: 10px;
        background: #fff;
        border-radius: 6px;
        :deep(.el-checkbox) {
          margin-bottom: 10px;
        }
        .deduction-amount {
          margin-top: 10px;
          font-size: 14px;
          color: #666;
          .deduction-value {
            font-size: 18px;
            font-weight: 600;
            color: #e6a23c;
            margin: 0 5px;
          }
          .deduction-tip {
            margin-left: 10px;
            font-size: 12px;
            color: #f56c6c;
          }
        }
        .points-cost {
          margin-top: 8px;
          font-size: 13px;
          color: #909399;
          .cost-value {
            color: #409eff;
            font-weight: 500;
          }
        }
      }
      .points-insufficient {
        :deep(.el-alert) {
          background-color: #fdf6ec;
        }
      }
    }
  }

  .guest-info-section {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 15px 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      .add-guest-btn {
        color: #409eff;
        font-size: 14px;
        &:hover {
          color: #66b1ff;
        }
      }
    }
    .guest-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .guest-item {
      padding: 15px;
      background: #fff;
      border-radius: 6px;
      border: 1px solid #e4e7ed;
      display: flex;
      align-items: center;
      cursor: pointer;
      transition: all 0.2s;
      &:hover {
        border-color: #409eff;
      }
      &.selected {
        border-color: #409eff;
        background: #ecf5ff;
      }
      .guest-checkbox {
        margin-right: 15px;
      }
      .guest-detail {
        flex: 1;
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 8px;
        .guest-name, .guest-idcard, .guest-phone {
          display: flex;
          align-items: center;
          .label {
            color: #666;
            min-width: 60px;
            font-size: 14px;
          }
          .value {
            color: #333;
            font-size: 14px;
          }
        }
        .guest-sex-birth {
          grid-column: 1 / 3;
          color: #999;
          font-size: 12px;
          margin-top: 5px;
        }
      }
    }
    .empty-guest {
      padding: 30px 0;
      text-align: center;
    }
  }

  .pay-type-section {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 20px 0;
    }
    .pay-card-list {
      display: flex;
      gap: 20px;
      flex-wrap: wrap;
    }
    .pay-card {
      width: 180px;
      height: 120px;
      background: #fff;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      position: relative;
      transition: all 0.2s;
      &:hover {
        border-color: #c0c4cc;
      }
      &.selected {
        border-color: #409eff;
        background: #ecf5ff;
      }
      .pay-icon {
        font-size: 36px;
        color: #409eff;
        margin-bottom: 10px;
        &.alipay-icon {
          color: #1677ff;
        }
        &.bank-icon {
          color: #f56c6c;
        }
      }
      .pay-name {
        font-size: 16px;
        color: #333;
      }
      .pay-radio {
        position: absolute;
        top: 10px;
        right: 10px;
      }
    }
  }

  .submit-section {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .total-amount {
      .label {
        font-size: 16px;
        color: #666;
        margin-right: 8px;
      }
      .amount {
        font-size: 24px;
        font-weight: 600;
        color: #e6a23c;
      }
      .original-amount {
        margin-left: 10px;
        font-size: 14px;
        color: #999;
        text-decoration: line-through;
      }
    }
    .submit-btn {
      width: 200px;
      height: 48px;
      font-size: 16px;
    }
  }
}

.empty-state {
  padding: 50px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .main-info-card {
    padding: 15px;
  }
  .hotel-header {
    flex-direction: column;
    align-items: flex-start !important;
  }
  .room-meta {
    grid-template-columns: 1fr;
  }
  .points-summary {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 8px;
  }
  .guest-detail {
    grid-template-columns: 1fr !important;
    .guest-sex-birth {
      grid-column: 1 / 2 !important;
    }
  }
  .pay-card-list {
    justify-content: center;
  }
  .submit-section {
    flex-direction: column;
    gap: 20px;
    align-items: stretch !important;
    .submit-btn {
      width: 100%;
    }
  }
}
</style>