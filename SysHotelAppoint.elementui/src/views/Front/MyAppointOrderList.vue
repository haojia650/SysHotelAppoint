<template>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>我的预约订单</el-breadcrumb-item>
        </el-breadcrumb>
    </div>

    <!-- 订单列表容器 -->
    <div class="appoint-list-container">
        <PaginationBox 
            ref="AppointPaginationBox" 
            :where="where" 
            url="/Appoint/List" 
            :limit="8"
        >
            <template v-slot:content="{ data }">
                <div class="appoint-card-grid" style="width: 100%">
                    <!-- 空状态 -->
                    <div class="empty-state" v-if="!data || data.length === 0">
                        <el-empty description="暂无预约订单"></el-empty>
                    </div>

                    <!-- 订单卡片 -->
                    <div class="appoint-card" v-for="(item, index) in data" :key="index">
                        <!-- 订单头部 -->
                        <div class="card-header">
                            <div class="order-base-info">
                                <span class="order-no">订单编号：{{ item.No || '未知' }}</span>
                                <span class="create-time">创建时间：{{ formatDate(item.CreationTime) }}</span>
                            </div>
                            <el-tag 
                                :type="getStatusTagType(item.AppointStatusFormat)" 
                                size="default"
                                class="status-tag"
                            >
                                {{ item.AppointStatusFormat || '未知状态' }}
                            </el-tag>
                        </div>

                        <!-- 酒店信息区域 -->
                        <div class="hotel-info-section">
                            <img 
                                :src="item.HotelDto?.Cover || NoImage" 
                                alt="酒店封面" 
                                class="hotel-cover"
                                @error="$event.target.src = NoImage"
                            >
                            <div class="hotel-detail">
                                <h3 class="hotel-name">{{ item.HotelDto?.Name || '未知酒店' }}</h3>
                                <div class="hotel-address">
                                    <i class="el-icon-location"></i>
                                    {{ item.HotelDto?.ProviceCityArea || '' }} {{ item.HotelDto?.Address || '暂无地址' }}
                                </div>
                                <div class="hotel-services" v-if="item.HotelDto?.Services">
                                    <el-tag 
                                        v-for="service in item.HotelDto.Services.split(',')" 
                                        :key="service" 
                                        size="small" 
                                        plain
                                    >
                                        {{ service }}
                                    </el-tag>
                                </div>
                            </div>
                        </div>

                        <!-- 房型信息区域 -->
                        <div class="room-info-section">
                            <div class="room-header">
                                <h4 class="room-title">
                                    {{ item.RoomDto?.Title || '未知房型' }}
                                    <span class="room-type-tag">
                                        {{ item.RoomDto?.RoomShapeFormat || '' }} | {{ item.RoomDto?.FeaturedRoomShapeFormat || '' }}
                                    </span>
                                </h4>
                            </div>
                            <div class="room-meta">
                                <div class="meta-item">
                                    <span class="label">房间面积：</span>
                                    <span class="value">{{ item.RoomDto?.AreaSize || 0 }}㎡</span>
                                </div>
                                <div class="meta-item">
                                    <span class="label">早餐配置：</span>
                                    <span class="value">{{ item.RoomDto?.BreakfastTypeFormat || '无早餐' }}</span>
                                </div>
                                <div class="meta-item">
                                    <span class="label">积分抵扣：</span>
                                    <span class="value">{{ item.RoomDto?.Integral || 0 }}分 (可抵¥{{ item.RoomDto?.IntegralExchangeMoney || 0 }})</span>
                                </div>
                                <div class="meta-item">
                                    <span class="label">免费取消：</span>
                                    <span class="value">{{ item.RoomDto?.IsFreeCancel ? '支持' : '不支持' }}</span>
                                </div>
                            </div>
                            <div class="room-desc" v-if="item.RoomDto?.Content">
                                <div class="desc-content" v-html="item.RoomDto.Content"></div>
                            </div>
                        </div>

                        <!-- 预订信息区域 -->
                        <div class="appoint-meta-section">
                            <div class="meta-grid">
                                <div class="meta-group">
                                    <div class="meta-label">入住时间</div>
                                    <div class="meta-value">{{ formatDateTime(item.BeginAppointTime) }}</div>
                                </div>
                                <div class="meta-group">
                                    <div class="meta-label">离店时间</div>
                                    <div class="meta-value">{{ formatDateTime(item.EndAppointTime) }}</div>
                                </div>
                                <div class="meta-group">
                                    <div class="meta-label">预订数量</div>
                                    <div class="meta-value">{{ item.Qty || 0 }}间</div>
                                </div>
                                <div class="meta-group">
                                    <div class="meta-label">入住人数</div>
                                    <div class="meta-value">{{ item.TotalPeopleNum || 0 }}人</div>
                                </div>
                                <div class="meta-group">
                                    <div class="meta-label">支付方式</div>
                                    <div class="meta-value">
                                        <i class="el-icon-wechat" v-if="item.PayType === 'WECHAT'"></i>
                                        <i class="el-icon-alipay" v-else-if="item.PayType === 'ALIPAY'"></i>
                                        <i class="el-icon-bank-card" v-else-if="item.PayType === 'BANKCARD'"></i>
                                        {{ getPayTypeName(item.PayType) }}
                                    </div>
                                </div>
                                <div class="meta-group">
                                    <div class="meta-label">订单金额</div>
                                    <div class="meta-value price">¥{{ calculateOrderAmount(item) }}</div>
                                </div>
                            </div>
                        </div>

                        <!-- 入住人信息区域 -->
                        <div class="guest-info-section" v-if="item.AppointDetDtos && item.AppointDetDtos.length">
                            <h4 class="section-title">
                                <i class="el-icon-user"></i> 入住人信息
                            </h4>
                            <div class="guest-list">
                                <div class="guest-item" v-for="(guest, gIndex) in item.AppointDetDtos" :key="gIndex">
                                    <div class="guest-detail">
                                        <span class="guest-name">姓名：{{ guest.Name || '未填写' }}</span>
                                        <span class="guest-sex">性别：{{ guest.Sex || '未知' }}</span>
                                        <span class="guest-phone">手机号：{{ guest.Phone || '未填写' }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 操作按钮区域 -->
                        <div class="card-actions">
                            <el-button 
                                type="primary" 
                                icon="el-icon-circle-check"
                                @click="toPay(item)"
                                v-if="item.AppointStatusFormat === '待支付'"
                            >
                                💳 去支付
                            </el-button>
                            <el-button 
                                type="danger" 
                                icon="el-icon-refresh-left"
                                @click="handleCancelOrder(item)"
                                v-if="canCancelOrder(item.AppointStatus)"
                            >
                                🚫 取消预约
                            </el-button>
                            <el-button 
                                type="text" 
                                icon="el-icon-delete"
                                @click="handleDeleteOrder(item)"
                                v-if="canDeleteOrder(item.AppointStatusFormat)"
                            >
                                🗑️ 删除订单
                            </el-button>
                            <el-button 
                                type="primary" 
                                icon="el-icon-star-off"
                                @click="viewDetail(item.Id)"
                            >
                                📄 查看详情
                            </el-button>
                            <el-button 
                                type="success" 
                                icon="el-icon-star-off"
                                @click="addComment(item.Id)"
                                v-if="item.AppointStatus === 5 || item.AppointStatus === 8"
                            >
                                ⭐ 评价
                            </el-button>
                            <el-button 
                                type="info"
                                icon="el-icon-phone"
                                @click="contactHotel(item.HotelDto)"
                            >
                                ☎️ 联系酒店
                            </el-button>
                            <el-button 
                                type="warning"
                                icon="el-icon-house"
                                @click="viewHotel(item.HotelDto.Id)"
                            >
                                🏨 查看酒店
                            </el-button>
                        </div>
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

const Token = computed(() => commonStore.Token);
const UserInfo = computed(() => commonStore.UserInfo);
const RoleType = computed(() => commonStore.RoleType);
const UserId = computed(() => commonStore.UserId);

const AppointPaginationBox = ref(null);

const where = ref({
    ToUserId: UserId.value
});

const formatDate = (dateStr) => {
    if (!dateStr) return '未填写';
    return dateStr.split('T')[0] || dateStr.split(' ')[0] || dateStr;
};

const formatDateTime = (dateStr) => {
    if (!dateStr) return '未填写';
    return dateStr.replace('T', ' ').substring(0, 16) || dateStr;
};

const getPayTypeName = (payType) => {
    const payTypeMap = {
        'WECHAT': '微信支付',
        'ALIPAY': '支付宝支付',
        'BANKCARD': '银行卡支付',
        '': '未选择'
    };
    return payTypeMap[payType] || '未知';
};

const calculateOrderAmount = (item) => {
    if (item.TotalMoney) {
        return Number(item.TotalMoney).toFixed(2);
    }
    const price = item.RoomDto?.ActualPrice || item.RoomDto?.MinPrice || 0;
    const qty = item.Qty || 1;
    return (price * qty).toFixed(2);
};

const contactHotel = async (hotelDto) => {
    let { Success } = await Post("/WechatCollection/InsertWechatCollection",
        {
            SelfUserId: UserId.value,
            OtherUserId: hotelDto.HotelUserId,
        });
    if (Success) {
        router.push({
            path: "/Front/Wechat",
            query: {
                OtherUserId: hotelDto.HotelUserId
            }
        });
        ElMessage.success('已进入与酒店的聊天界面');
    } else {
        ElMessage.error('联系酒店失败，请稍后重试');
    }
};

const Hotel = ref(null);
const imageList = ref([]);
const serviceList = ref([]);

const viewHotel = async (hotelId) => {
  try {
    const { Success, Data, Msg } = await Post("/Hotel/Get", { Id: hotelId });
    
    if (!Success) {
      ElMessage.error(Msg || '获取酒店详情失败');
      return;
    }
    if (!Data) {
      ElMessage.warning('未查询到该酒店信息');
      return;
    }

    Hotel.value = Data;
    imageList.value = Data.HotelImages?.split(',').filter(url => url.trim() !== '') || [];
    serviceList.value = Data.Services?.split(',').filter(s => s.trim() !== '') || [];

    router.push({
      path: '/Front/HotelDetail',
      query: { HotelId: hotelId }
    });

  } catch (error) {
    console.error('获取酒店详情异常：', error);
    ElMessage.error('网络异常，获取酒店详情失败，请稍后重试');
  }
};

const addComment = (id) => {
    if (!id) {
        ElMessage.warning('订单ID不存在，无法评价');
        return;
    }
    router.push({
        path: '/Front/AddComment',
        query: { AppointId: id }
    });
};

const getStatusTagType = (status) => {
    const statusMap = {
        '待入住': 'info',
        '已入住': 'success',
        '用户取消': 'danger',
        '酒店取消': 'danger',
        '完成': 'primary',
        '待确定': 'warning',
        '申请取消': 'info',
        '完成退房': 'primary',
        '待支付': 'warning',
        '系统取消': 'danger',
        '已取消': 'danger',
        '已完成': 'primary'
    };
    return statusMap[status] || 'default';
};

const canCancelOrder = (statusCode) => {
    const cancelableStatus = [1, 6, 9];
    return cancelableStatus.includes(statusCode);
};

const canDeleteOrder = (status) => {
    const deletableStatus = ['用户取消', '酒店取消', '系统取消', '完成', '完成退房', '已取消', '已完成'];
    return deletableStatus.includes(status);
};

const toPay = (item) => {
    const totalMoney = calculateOrderAmount(item);
    router.push({
        path: '/Front/Payment',
        query: {
            AppointId: item.Id,
            TotalMoney: totalMoney,
            PayType: item.PayType
        }
    });
};

const handleCancelOrder = async (item) => {
    try {
        await ElMessageBox.confirm(
            '确定要取消该订单吗？',
            '取消订单',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );
        
        const { Success, Msg } = await Post("/Appoint/UserCancel", {
            Id: item.Id,
            ToUserId: UserId.value
        });
        
        if (Success) {
            ElMessage.success(Msg || '订单取消成功');
            AppointPaginationBox.value.Reload();
        } else {
            ElMessage.error(Msg || '订单取消失败');
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('取消订单异常：', error);
            ElMessage.error('取消订单失败，请稍后重试');
        }
    }
};

const viewDetail = (Id) => {
    router.push({
        path: '/Front/OrderDetail',
        query: { AppointId: Id }
    });
};

const handleDeleteOrder = async (item) => {
    try {
        await ElMessageBox.confirm(
            '确定要删除该订单吗？删除后不可恢复',
            '删除订单',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'danger'
            }
        );
        
        const { Success, Msg } = await Post("/Appoint/Delete", {
            Id: item.Id,
            ToUserId: UserId.value
        });
        
        if (Success) {
            ElMessage.success(Msg || '订单删除成功');
            AppointPaginationBox.value.Reload();
        } else {
            ElMessage.error(Msg || '订单删除失败');
        }
    } catch (error) {
        if (error !== 'cancel') {
            console.error('删除订单异常：', error);
            ElMessage.error('删除订单失败，请稍后重试');
        }
    }
};

onBeforeMount(async () => {
    where.value.ToUserId = UserId.value;
});
</script>

<style scoped lang="scss">
// 全局容器
.appoint-list-container {
    width: 95%;
    max-width: 1400px;
    margin: 20px auto;
    padding: 0 10px;
}

// 面包屑
.breadcrumb {
    width: 95%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px 10px 10px;
    .breadcrumb-label {
        margin-right: 10px;
        color: #666;
        font-weight: 500;
    }
    :deep(.el-breadcrumb) {
        --el-breadcrumb-item-color: #666;
        --el-breadcrumb-item-active-color: #165DFF;
    }
}

// 卡片网格：垂直单列，删除靠左属性，卡片自动占满
.appoint-card-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 10px 0;
}

// 空状态
.empty-state {
    grid-column: 1 / -1;
    padding: 60px 0;
    text-align: center;
}

// 简约商务卡片 - 100%占满，无宽度限制
.appoint-card {
    width: 100%;
    background: #ffffff;
    border: 1px solid #E5E6EB;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    transition: all 0.2s ease;
    &:hover {
        border-color: #D0D3D9;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }
}

// 卡片头部
.card-header {
    padding: 14px 20px;
    background: #F7F8FA;
    border-bottom: 1px solid #E5E6EB;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .order-base-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
        .order-no {
            font-size: 14px;
            font-weight: 500;
            color: #1D2129;
        }
        .create-time {
            font-size: 12px;
            color: #86909C;
        }
    }
    .status-tag {
        font-size: 12px;
    }
}

// 酒店信息
.hotel-info-section {
    display: flex;
    padding: 16px 20px;
    gap: 16px;
    border-bottom: 1px solid #E5E6EB;
    .hotel-cover {
        width: 80px;
        height: 80px;
        border-radius: 4px;
        object-fit: cover;
        flex-shrink: 0;
    }
    .hotel-detail {
        flex: 1;
        .hotel-name {
            font-size: 16px;
            font-weight: 500;
            color: #1D2129;
            margin: 0 0 8px 0;
        }
        .hotel-address {
            display: flex;
            align-items: center;
            font-size: 13px;
            color: #86909C;
            margin-bottom: 8px;
            i {
                color: #165DFF;
                margin-right: 4px;
            }
        }
        .hotel-services {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
        }
    }
}

// 房型信息
.room-info-section {
    padding: 16px 20px;
    border-bottom: 1px solid #E5E6EB;
    .room-header {
        margin-bottom: 12px;
        .room-title {
            font-size: 15px;
            font-weight: 500;
            color: #1D2129;
            margin: 0;
            .room-type-tag {
                font-size: 12px;
                color: #86909C;
                margin-left: 8px;
            }
        }
    }
    .room-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 12px;
        .meta-item {
            font-size: 13px;
            .label {
                color: #86909C;
            }
            .value {
                color: #4E5969;
                font-weight: 500;
            }
        }
    }
    .room-desc {
        .desc-content {
            font-size: 13px;
            color: #6F7785;
            line-height: 1.6;
            padding: 8px;
            background: #F7F8FA;
            border-radius: 4px;
        }
    }
}

// 预订信息
.appoint-meta-section {
    padding: 16px 20px;
    border-bottom: 1px solid #E5E6EB;
    .meta-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
        gap: 12px;
        .meta-group {
            .meta-label {
                font-size: 12px;
                color: #86909C;
                margin-bottom: 4px;
            }
            .meta-value {
                font-size: 13px;
                color: #1D2129;
                font-weight: 500;
                &.price {
                    color: #FA7D00;
                    font-size: 14px;
                }
                i {
                    margin-right: 4px;
                    font-size: 13px;
                    &.el-icon-wechat { color: #07C160; }
                    &.el-icon-alipay { color: #1677FF; }
                    &.el-icon-bank-card { color: #F53F3F; }
                }
            }
        }
    }
}

// 入住人信息
.guest-info-section {
    padding: 16px 20px;
    border-bottom: 1px solid #E5E6EB;
    .section-title {
        font-size: 14px;
        font-weight: 500;
        color: #1D2129;
        margin: 0 0 12px 0;
        i {
            color: #165DFF;
            margin-right: 6px;
        }
    }
    .guest-list {
        .guest-item {
            padding: 6px 0;
            border-bottom: 1px dashed #E5E6EB;
            &:last-child { border: none; }
            .guest-detail {
                display: flex;
                flex-wrap: wrap;
                gap: 16px;
                font-size: 13px;
                color: #6F7785;
            }
        }
    }
}

// 操作按钮
.card-actions {
    padding: 16px 20px;
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    flex-wrap: wrap;
    :deep(.el-button) {
        border-radius: 4px;
        padding: 6px 14px;
        font-size: 13px;
    }
}

// 响应式
@media (max-width: 768px) {
    .hotel-info-section {
        flex-direction: column;
        .hotel-cover {
            width: 100%;
            height: 160px;
        }
    }
    .meta-grid {
        grid-template-columns: 1fr 1fr;
    }
    .card-actions {
        justify-content: center;
    }
}

@media (max-width: 480px) {
    .meta-grid {
        grid-template-columns: 1fr;
    }
    .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }
}
</style>