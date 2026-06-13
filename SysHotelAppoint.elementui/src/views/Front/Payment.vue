<template>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/Front/MyAppointOrderList' }">我的预约订单</el-breadcrumb-item>
            <el-breadcrumb-item>订单支付</el-breadcrumb-item>
        </el-breadcrumb>
    </div>

    <!-- 支付步骤条 -->
    <div class="pay-steps">
        <el-steps :active="3" finish-status="success" align-center>
            <el-step title="选择房型" icon="el-icon-house" />
            <el-step title="填写预订信息" icon="el-icon-user" />
            <el-step title="确认订单" icon="el-icon-check" />
            <el-step title="支付完成" icon="el-icon-circle-check" />
        </el-steps>
    </div>

    <!-- 核心支付容器 -->
    <div class="pay-container">
        <!-- 订单信息卡片 -->
        <div class="order-info-card" v-if="AppointOrder">
            <!-- 订单头部：编号 + 倒计时 -->
            <div class="order-header">
                <div class="order-base">
                    <span class="order-no">订单编号：{{ AppointOrder.No || '未知' }}</span>
                    <span class="order-status">
                        <el-tag type="warning">{{ AppointOrder.AppointStatusFormat || '待支付' }}</el-tag>
                    </span>
                </div>
                
                <!-- 支付倒计时：仅待支付状态显示 -->
                <div class="countdown-section" v-if="AppointOrder.AppointStatusFormat === '待支付'">
                    <span class="countdown-tip">
                        <i class="el-icon-clock"></i> 
                        请在{{ countdownText }}内完成支付，超时订单将自动取消
                    </span>
                    <div class="countdown-box" :class="{ expired: isCountdownExpired }">
                        {{ countdownText }}
                    </div>
                </div>
            </div>

            <!-- 酒店信息 -->
            <div class="hotel-info-section">
                <h3 class="section-title">
                    <i class="el-icon-building"></i> 酒店信息
                </h3>
                <div class="hotel-content">
                    <img 
                        :src="AppointOrder.HotelDto?.Cover || NoImage" 
                        alt="酒店封面" 
                        class="hotel-cover"
                        @error="$event.target.src = NoImage"
                    >
                    <div class="hotel-detail">
                        <h4 class="hotel-name">{{ AppointOrder.HotelDto?.Name || '未知酒店' }}</h4>
                        <div class="hotel-address">
                            <i class="el-icon-location"></i>
                            {{ AppointOrder.HotelDto?.ProviceCityArea || '' }} {{ AppointOrder.HotelDto?.Address || '暂无地址' }}
                        </div>
                        <div class="hotel-services" v-if="AppointOrder.HotelDto?.Services">
                            <el-tag 
                                v-for="service in AppointOrder.HotelDto.Services.split(',')" 
                                :key="service" 
                                size="small" 
                                type="info"
                            >
                                {{ service }}
                            </el-tag>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 房型信息 -->
            <div class="room-info-section">
                <h3 class="section-title">
                    <i class="el-icon-s-home"></i> 房型信息
                </h3>
                <div class="room-content">
                    <div class="room-title">
                        {{ AppointOrder.RoomDto?.Title || '未知房型' }}
                        <span class="room-type-tag">
                            {{ AppointOrder.RoomDto?.RoomShapeFormat || '' }} | {{ AppointOrder.RoomDto?.FeaturedRoomShapeFormat || '' }}
                        </span>
                    </div>
                    <div class="room-meta">
                        <div class="meta-item">
                            <span class="label">房间面积：</span>
                            <span class="value">{{ AppointOrder.RoomDto?.AreaSize || 0 }}㎡</span>
                        </div>
                        <div class="meta-item">
                            <span class="label">早餐配置：</span>
                            <span class="value">{{ AppointOrder.RoomDto?.BreakfastTypeFormat || '无早餐' }}</span>
                        </div>
                        <div class="meta-item">
                            <span class="label">积分抵扣：</span>
                            <span class="value">{{ AppointOrder.RoomDto?.Integral || 0 }}分 (可抵¥{{ AppointOrder.RoomDto?.IntegralExchangeMoney || 0 }})</span>
                        </div>
                        <div class="meta-item">
                            <span class="label">免费取消：</span>
                            <span class="value">{{ AppointOrder.RoomDto?.IsFreeCancel ? '支持' : '不支持' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 预订信息 -->
            <div class="appoint-info-section">
                <h3 class="section-title">
                    <i class="el-icon-calendar"></i> 预订信息
                </h3>
                <div class="appoint-meta">
                    <div class="meta-grid">
                        <div class="meta-group">
                            <div class="meta-label">入住时间</div>
                            <div class="meta-value">{{ formatDateTime(AppointOrder.BeginAppointTime) }}</div>
                        </div>
                        <div class="meta-group">
                            <div class="meta-label">离店时间</div>
                            <div class="meta-value">{{ formatDateTime(AppointOrder.EndAppointTime) }}</div>
                        </div>
                        <div class="meta-group">
                            <div class="meta-label">预订数量</div>
                            <div class="meta-value">{{ AppointOrder.Qty || 0 }}间</div>
                        </div>
                        <div class="meta-group">
                            <div class="meta-label">入住人数</div>
                            <div class="meta-value">{{ AppointOrder.TotalPeopleNum || 0 }}人</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 入住人信息 -->
            <div class="guest-info-section" v-if="AppointOrder.AppointDetDtos && AppointOrder.AppointDetDtos.length">
                <h3 class="section-title">
                    <i class="el-icon-user"></i> 入住人信息
                </h3>
                <div class="guest-list">
                    <div class="guest-item" v-for="(guest, index) in AppointOrder.AppointDetDtos" :key="index">
                        <div class="guest-detail">
                            <span class="guest-name">姓名：{{ guest.Name || '未填写' }}</span>
                            <span class="guest-sex">性别：{{ guest.Sex || '未知' }}</span>
                            <span class="guest-phone">手机号：{{ guest.Phone || '未填写' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 订单金额 -->
            <div class="amount-section">
                <h3 class="section-title">
                    <i class="el-icon-money"></i> 订单金额
                </h3>
                <div class="amount-content">
                    <div class="amount-item">
                        <span class="label">房型单价：</span>
                        <span class="value">¥{{ AppointOrder.RoomDto?.ActualPrice || AppointOrder.RoomDto?.MinPrice || 0 }}</span>
                    </div>
                    <div class="amount-item">
                        <span class="label">预订数量：</span>
                        <span class="value">× {{ AppointOrder.Qty || 1 }}</span>
                    </div>
                    <div class="amount-item total-amount">
                        <span class="label">订单总价：</span>
                        <span class="value">¥{{ calculateTotalAmount() }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 支付方式选择 -->
        <div class="pay-type-card" v-if="AppointOrder">
            <h3 class="pay-type-title">
                <i class="el-icon-pay"></i> 选择支付方式
            </h3>
            
            <div class="pay-card-list">
                <!-- 微信支付 -->
                <div
                    class="pay-card"
                    :class="{ selected: selectedPayType === 'WECHAT' }"
                    @click="selectedPayType = 'WECHAT'"
                    :disabled="isPayDisabled"
                >
                    <div class="pay-icon">
                        <i class="el-icon-wechat"></i>
                    </div>
                    <div class="pay-name">微信支付</div>
                    <el-radio
                        v-model="selectedPayType"
                        label="WECHAT"
                        class="pay-radio"
                        :disabled="isPayDisabled"
                    />
                </div>

                <!-- 支付宝支付 -->
                <div
                    class="pay-card"
                    :class="{ selected: selectedPayType === 'ALIPAY' }"
                    @click="selectedPayType = 'ALIPAY'"
                    :disabled="isPayDisabled"
                >
                    <div class="pay-icon alipay-icon">
                        <i class="el-icon-alipay"></i>
                    </div>
                    <div class="pay-name">支付宝支付</div>
                    <el-radio
                        v-model="selectedPayType"
                        label="ALIPAY"
                        class="pay-radio"
                        :disabled="isPayDisabled"
                    />
                </div>

                <!-- 银行卡支付 -->
                <div
                    class="pay-card"
                    :class="{ selected: selectedPayType === 'BANKCARD' }"
                    @click="selectedPayType = 'BANKCARD'"
                    :disabled="isPayDisabled"
                >
                    <div class="pay-icon bank-icon">
                        <i class="el-icon-bank-card"></i>
                    </div>
                    <div class="pay-name">银行卡支付</div>
                    <el-radio
                        v-model="selectedPayType"
                        label="BANKCARD"
                        class="pay-radio"
                        :disabled="isPayDisabled"
                    />
                </div>
            </div>

            <!-- 支付操作按钮 -->
            <div class="pay-actions">
                <el-button 
                    type="primary" 
                    size="large"
                    class="pay-btn"
                    @click="handlePay"
                    :disabled="isPayDisabled || !selectedPayType"
                >
                    <i class="el-icon-check-circle"></i>立即支付 ¥{{ calculateTotalAmount() }}
                </el-button>
                
                <el-button 
                    type="warning" 
                    size="large"
                    class="cancel-btn"
                    @click="handleCancelOrder"
                    :disabled="isPayDisabled"
                >
                    <i class="el-icon-close"></i> 取消订单
                </el-button>
                
                <el-button 
                    type="text" 
                    class="back-btn"
                    @click="handleBack"
                >
                    <i class="el-icon-arrow-left"></i> 返回订单列表
                </el-button>
            </div>
        </div>

        <!-- 加载/空状态 -->
        <div class="empty-state" v-if="!AppointOrder">
            <el-empty description="暂无订单信息"></el-empty>
        </div>

        <!-- 倒计时结束提示弹窗（仅提示，不自动跳转） -->
        <el-dialog 
            title="支付超时" 
            v-model="showExpiredDialog" 
            width="400px"
            :close-on-click-modal="false"
            :close-on-press-escape="false"
            :show-close="false"
        >
            <div class="expired-dialog-content">
                <i class="el-icon-warning"></i>
                <p>订单支付时间已超过15分钟，该订单已自动取消！</p>
                <p>您可以返回订单列表重新下单。</p>
            </div>
            <template v-slot:footer>
                <el-button type="primary" @click="handleBack">返回订单列表</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import NoImage from '@/assets/图片失效.png';
import { ref, computed, onBeforeMount, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from "@/store";
import { Post } from '@/api/http';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();

// 状态管理数据
const Token = computed(() => commonStore.Token);
const UserInfo = computed(() => commonStore.UserInfo);
const RoleType = computed(() => commonStore.RoleType);
const UserId = computed(() => commonStore.UserId);

// 订单信息
const AppointOrder = ref(null);
// 选中的支付方式
const selectedPayType = ref('');
// 倒计时相关
const countdown = ref(15 * 60); // 初始15分钟（秒）
const timer = ref(null);
// 超时弹窗
const showExpiredDialog = ref(false);

// 是否禁用支付/取消相关操作（超时 或 订单状态非待支付）
const isPayDisabled = computed(() => {
    if (!AppointOrder.value) return true;
    // 倒计时结束 或 订单状态不是待支付
    return countdown.value <= 0 || AppointOrder.value.AppointStatusFormat !== '待支付';
});

// 倒计时显示文本
const countdownText = computed(() => {
    if (countdown.value <= 0) return '00:00';
    const minutes = Math.floor(countdown.value / 60);
    const seconds = countdown.value % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
});

// 获取订单详情
const GetAppointOrderApi = async () => {
    // 校验订单ID
    const appointId = route.query.AppointId;
    if (!appointId) {
        ElMessage.error('订单参数缺失，请返回重试');
        router.push({ path: '/Front/MyAppointOrderList' });
        return;
    }

    try {
        const { Data, Success, Msg } = await Post("/Appoint/Get", {
            Id: appointId
        });
        if (Success) {
            AppointOrder.value = Data;
            // 默认选中订单原有支付方式，若无效则默认支付宝
            selectedPayType.value = (Data.PayType && ['WECHAT', 'ALIPAY', 'BANKCARD'].includes(Data.PayType)) 
                ? Data.PayType 
                : 'ALIPAY';
            
            // 仅当订单状态为待支付时启动倒计时
            if (Data.AppointStatusFormat === '待支付') {
                initCountdown(Data.CreationTime);
            } else {
                // 非待支付状态，确保倒计时不启动且禁用操作
                if (timer.value) clearInterval(timer.value);
                countdown.value = 0;
            }
        } else {
            ElMessage.error(Msg || '获取订单信息失败');
        }
    } catch (error) {
        console.error('获取订单信息异常：', error);
        ElMessage.error('网络异常，获取订单信息失败');
    }
};

// 初始化倒计时（基于订单创建时间）
const initCountdown = (creationTime) => {
    if (!creationTime) return;
    
    // 计算订单创建时间 + 15分钟 与当前时间的差值
    const createTime = new Date(creationTime);
    const expireTime = new Date(createTime.getTime() + 15 * 60 * 1000);
    const now = new Date();
    
    // 计算剩余秒数
    const remainingSeconds = Math.floor((expireTime - now) / 1000);
    
    if (remainingSeconds > 0) {
        countdown.value = remainingSeconds;
        startCountdown();
    } else {
        countdown.value = 0;
        handleCountdownExpired();
    }
};

// 启动倒计时
const startCountdown = () => {
    // 清除原有定时器
    if (timer.value) clearInterval(timer.value);
    
    timer.value = setInterval(() => {
        if (countdown.value > 0) {
            countdown.value--;
        } else {
            clearInterval(timer.value);
            timer.value = null;
            handleCountdownExpired();
        }
    }, 1000);
};

// 倒计时结束处理（仅调用取消接口，显示弹窗，不自动跳转）
const handleCountdownExpired = async () => {
    // 避免重复调用
    if (countdown.value > 0) return;
    
    // 只有在待支付状态下才执行取消操作和弹窗
    if (AppointOrder.value?.AppointStatusFormat === '待支付') {
        // 调用取消订单接口
        await cancelOrderApi(true); // 传入标识表示超时自动取消
        // 显示超时弹窗（提示用户，但不自动跳转）
        showExpiredDialog.value = true;
    }
};

// 取消订单接口
// isTimeout: 是否为超时自动取消（用于区分用户主动取消）
const cancelOrderApi = async (isTimeout = false) => {
    if (!AppointOrder.value?.Id) return;
    try {
        const { Success, Msg } = await Post("/Appoint/UserCancel", {
            Id: AppointOrder.value.Id,
            ToUserId: UserId.value
        });
        if (Success) {
            if (!isTimeout) {
                ElMessage.success(Msg || '订单已取消');
            }
            // 更新本地订单状态为已取消，避免后续操作
            if (AppointOrder.value) {
                AppointOrder.value.AppointStatusFormat = '已取消';
            }
            // 停止倒计时
            if (timer.value) {
                clearInterval(timer.value);
                timer.value = null;
            }
        } else {
            if (!isTimeout) {
                ElMessage.error(Msg || '取消订单失败');
            }
        }
    } catch (error) {
        console.error('取消订单异常：', error);
        if (!isTimeout) {
            ElMessage.error('网络异常，取消订单失败');
        }
    }
};

// 格式化日期时间
const formatDateTime = (dateStr) => {
    if (!dateStr) return '未填写';
    return dateStr.replace('T', ' ').substring(0, 16) || dateStr;
};

// 计算订单总价
const calculateTotalAmount = () => {
    if (!AppointOrder.value) return '0.00';
    // 优先使用后端返回的总金额
    if (AppointOrder.value.TotalMoney != null) {
        return Number(AppointOrder.value.TotalMoney).toFixed(2);
    }
    // 兜底（旧数据）：单价 × 数量（缺少天数，但仅作备胎）
    const price = AppointOrder.value.RoomDto?.ActualPrice || AppointOrder.value.RoomDto?.MinPrice || 0;
    const qty = AppointOrder.value.Qty || 1;
    return (price * qty).toFixed(2);
};

// 处理支付
const handlePay = async () => {
    // 二次校验支付状态
    if (isPayDisabled.value) {
        ElMessage.warning('订单当前无法支付，请刷新页面重试');
        return;
    }
    
    try {
        await ElMessageBox.confirm(
            `确认使用${getPayTypeName(selectedPayType.value)}支付¥${calculateTotalAmount()}吗？`,
            '支付确认',
            {
                confirmButtonText: '确认支付',
                cancelButtonText: '取消',
                type: 'primary'
            }
        );
        
        // 调用支付接口
        const { Success, Msg, Data } = await Post("/Appoint/Payment", {
            Id: AppointOrder.value.Id,
            PayType: selectedPayType.value,
            ToUserId: UserId.value
        });
        
        if (Success) {
            ElMessage.success('支付成功！');
            // 清除倒计时，避免后续干扰
            if (timer.value) {
                clearInterval(timer.value);
                timer.value = null;
            }
            // 跳转到订单列表
            router.push({ path: '/Front/MyAppointOrderList' });
        } else {
            ElMessage.error(Msg || '支付失败');
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('支付异常：', error);
            ElMessage.error('支付处理失败，请稍后重试');
        }
    }
};

// 处理用户主动取消订单
const handleCancelOrder = () => {
    if (isPayDisabled.value) {
        ElMessage.warning('订单已超时或已取消，无需重复操作');
        return;
    }
    
    ElMessageBox.confirm(
        '确定要取消该订单吗？取消后将无法恢复',
        '取消订单',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(async () => {
        await cancelOrderApi(false);
        // 取消成功后跳回订单列表
        router.push({ path: '/Front/MyAppointOrderList' });
    }).catch(() => {});
};

// 处理返回
const handleBack = () => {
    // 清除定时器避免内存占用
    if (timer.value) {
        clearInterval(timer.value);
        timer.value = null;
    }
    router.push({ path: '/Front/MyAppointOrderList' });
};

// 根据支付类型获取名称
const getPayTypeName = (payType) => {
    const payTypeMap = {
        'WECHAT': '微信支付',
        'ALIPAY': '支付宝支付',
        'BANKCARD': '银行卡支付'
    };
    return payTypeMap[payType] || '未知支付方式';
};

// 页面挂载前加载数据
onBeforeMount(async () => {
    await GetAppointOrderApi();
});

// 页面卸载时清除定时器
onUnmounted(() => {
    if (timer.value) {
        clearInterval(timer.value);
        timer.value = null;
    }
});
</script>

<style scoped lang="scss">
// 全局样式
.pay-container {
    width: 95%;
    max-width: 1200px;
    margin: 20px auto;
    padding: 0 10px;
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;

    @media (max-width: 768px) {
        grid-template-columns: 1fr;
    }
}

// 面包屑样式
.breadcrumb {
    width: 95%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 10px 10px;
    
    .breadcrumb-label {
        margin-right: 10px;
        color: #666;
        font-weight: 500;
    }

    :deep(.el-breadcrumb) {
        --el-breadcrumb-item-color: #666;
        --el-breadcrumb-item-active-color: #409eff;
    }
}

// 步骤条样式
.pay-steps {
    width: 95%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 10px;
    
    :deep(.el-steps) {
        --el-steps-finish-color: #409eff;
        --el-steps-process-color: #e6a23c;
        --el-steps-wait-color: #c0c4cc;
        padding: 10px 0;
    }
}

// 订单信息卡片
.order-info-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);
    overflow: hidden;

    .order-header {
        background: linear-gradient(90deg, #409eff 0%, #1677ff 100%);
        color: #fff;
        padding: 15px 20px;
        display: flex;
        flex-direction: column;
        gap: 10px;

        .order-base {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .order-no {
                font-size: 14px;
                font-weight: 500;
            }
        }

        .countdown-section {
            .countdown-tip {
                font-size: 13px;
                opacity: 0.9;
                display: block;
                margin-bottom: 5px;
            }

            .countdown-box {
                display: inline-block;
                padding: 4px 12px;
                background: rgba(255, 255, 255, 0.2);
                border-radius: 4px;
                font-size: 16px;
                font-weight: 600;

                &.expired {
                    color: #f56c6c;
                    background: rgba(245, 108, 108, 0.2);
                }
            }
        }
    }

    .section-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0;
        padding: 15px 20px 10px;
        border-bottom: 1px solid #f0f0f0;
        display: flex;
        align-items: center;

        i {
            color: #409eff;
            margin-right: 8px;
            font-size: 18px;
        }
    }

    // 酒店信息样式
    .hotel-info-section {
        .hotel-content {
            padding: 15px 20px;
            display: flex;
            gap: 15px;
            flex-wrap: wrap;

            .hotel-cover {
                width: 100px;
                height: 100px;
                border-radius: 8px;
                object-fit: cover;
                flex-shrink: 0;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            }

            .hotel-detail {
                flex: 1;
                min-width: 200px;

                .hotel-name {
                    font-size: 18px;
                    font-weight: 600;
                    color: #1f2937;
                    margin: 0 0 8px 0;
                }

                .hotel-address {
                    display: flex;
                    align-items: center;
                    color: #666;
                    font-size: 14px;
                    margin-bottom: 10px;

                    i {
                        color: #409eff;
                        margin-right: 5px;
                    }
                }

                .hotel-services {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 6px;
                }
            }
        }
    }

    // 房型信息样式
    .room-info-section {
        .room-content {
            padding: 15px 20px;

            .room-title {
                font-size: 16px;
                font-weight: 600;
                color: #333;
                margin-bottom: 10px;

                .room-type-tag {
                    font-size: 12px;
                    font-weight: normal;
                    color: #666;
                    margin-left: 8px;
                }
            }

            .room-meta {
                display: flex;
                flex-wrap: wrap;
                gap: 15px;

                .meta-item {
                    display: flex;
                    align-items: center;
                    font-size: 14px;

                    .label {
                        color: #666;
                        min-width: 80px;
                    }

                    .value {
                        color: #333;
                        font-weight: 500;
                    }
                }
            }
        }
    }

    // 预订信息样式
    .appoint-info-section {
        .appoint-meta {
            padding: 15px 20px;

            .meta-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
                gap: 12px;

                .meta-group {
                    .meta-label {
                        font-size: 12px;
                        color: #666;
                        margin-bottom: 4px;
                    }

                    .meta-value {
                        font-size: 14px;
                        color: #333;
                        font-weight: 500;
                    }
                }
            }
        }
    }

    // 入住人信息样式
    .guest-info-section {
        .guest-list {
            padding: 15px 20px;

            .guest-item {
                padding: 8px 0;
                border-bottom: 1px dashed #f0f0f0;

                &:last-child {
                    border-bottom: none;
                }

                .guest-detail {
                    display: flex;
                    flex-wrap: wrap;
                    gap: 15px;
                    font-size: 14px;

                    .guest-name, .guest-sex, .guest-phone {
                        color: #666;

                        &::before {
                            content: '';
                            display: inline-block;
                            width: 4px;
                            height: 4px;
                            border-radius: 50%;
                            background: #409eff;
                            margin-right: 4px;
                        }
                    }
                }
            }
        }
    }

    // 金额信息样式
    .amount-section {
        .amount-content {
            padding: 15px 20px;

            .amount-item {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 8px 0;
                border-bottom: 1px dashed #f0f0f0;

                &.total-amount {
                    border-bottom: none;
                    padding-top: 15px;

                    .label {
                        font-size: 16px;
                        font-weight: 600;
                        color: #333;
                    }

                    .value {
                        font-size: 20px;
                        font-weight: 600;
                        color: #e6a23c;
                    }
                }

                .label {
                    color: #666;
                    font-size: 14px;
                }

                .value {
                    color: #333;
                    font-size: 14px;
                    font-weight: 500;
                }
            }
        }
    }
}

// 支付方式卡片
.pay-type-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(64, 158, 255, 0.1);
    overflow: hidden;
    height: fit-content;

    .pay-type-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0;
        padding: 15px 20px;
        border-bottom: 1px solid #f0f0f0;
        display: flex;
        align-items: center;

        i {
            color: #409eff;
            margin-right: 8px;
            font-size: 18px;
        }
    }

    .pay-card-list {
        padding: 20px;
        display: flex;
        flex-direction: column;
        gap: 15px;

        .pay-card {
            width: 100%;
            height: 80px;
            background: #f8f9fa;
            border: 2px solid #e4e7ed;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            position: relative;
            transition: all 0.2s;

            &:hover:not([disabled]) {
                border-color: #c0c4cc;
                background: #f0f7ff;
            }

            &.selected:not([disabled]) {
                border-color: #409eff;
                background: #ecf5ff;
            }

            &[disabled] {
                opacity: 0.6;
                cursor: not-allowed;
                background: #f5f7fa;
            }

            .pay-icon {
                font-size: 32px;
                color: #409eff;
                margin-right: 15px;

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
                font-weight: 500;
            }

            .pay-radio {
                position: absolute;
                top: 10px;
                right: 10px;
            }
        }
    }

    .pay-actions {
        padding: 0 20px 20px;
        display: flex;
        flex-direction: column;
        gap: 15px;

        .pay-btn {
            width: 100%;
            height: 48px;
            font-size: 16px;
            border-radius: 8px;
            background: linear-gradient(90deg, #409eff 0%, #1677ff 100%);
            border: none;
        }

        .cancel-btn {
            width: 100%;
            height: 48px;
            font-size: 16px;
            border-radius: 8px;
        }

        .back-btn {
            color: #666;
            text-align: center;
            padding: 10px 0;
        }
    }
}

// 空状态
.empty-state {
    grid-column: 1 / -1;
    padding: 60px 0;
    text-align: center;
}

// 超时弹窗样式
:deep(.expired-dialog-content) {
    text-align: center;
    padding: 20px 0;

    i {
        font-size: 48px;
        color: #e6a23c;
        margin-bottom: 15px;
    }

    p {
        font-size: 16px;
        color: #666;
        margin: 8px 0;
    }
}

// 响应式适配
@media (max-width: 480px) {
    .order-header {
        .countdown-section {
            .countdown-tip {
                font-size: 12px;
            }
        }
    }

    .hotel-content {
        .hotel-cover {
            width: 100%;
            height: 180px;
        }
    }

    .room-meta {
        flex-direction: column;
        gap: 10px;
    }
}
</style>