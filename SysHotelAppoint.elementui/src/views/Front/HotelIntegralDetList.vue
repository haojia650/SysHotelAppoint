<template>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/Front/MyHotelIntegralList' }">我的酒店积分</el-breadcrumb-item>
            <el-breadcrumb-item>积分明细</el-breadcrumb-item>
        </el-breadcrumb>
    </div>

    <!-- 参数缺失提示 -->
    <div v-if="!isParamsValid" class="error-container">
        <el-empty description="参数错误，无法获取积分明细" :image-size="120">
            <el-button type="primary" @click="goBack">返回积分列表</el-button>
        </el-empty>
    </div>

    <!-- 积分流水列表 -->
    <div v-else class="flow-container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
        </div>

        <!-- 数据列表 -->
        <template v-else>
            <el-empty v-if="!flowList.length" description="暂无积分流水记录" :image-size="120" />

            <div class="flow-list" v-else>
                <div class="flow-item" v-for="item in flowList" :key="item.Id">
                    <div class="item-left">
                        <img
                            :src="getHotelCover(item.HotelDto)"
                            :alt="item.HotelDto?.Name || '酒店图片'"
                            @error="handleImageError"
                            class="hotel-avatar"
                        />
                    </div>
                    <div class="item-center">
                        <div class="top-row">
                            <span class="title">{{ item.Title || '积分操作' }}</span>
                            <el-tag :type="getTagType(item.Type)" size="small">
                                {{ item.Type || '其他' }}
                            </el-tag>
                        </div>
                        <div class="middle-row">
                            <span class="hotel-name">
                                <el-icon><OfficeBuilding /></el-icon>
                                {{ item.HotelDto?.Name || '未知酒店' }}
                            </span>
                            <span class="order-no" v-if="item.RelativeNo">
                                <el-icon><Document /></el-icon>
                                单号：{{ item.RelativeNo }}
                            </span>
                        </div>
                        <div class="bottom-row">
                            <el-icon><Clock /></el-icon>
                            <span class="time">{{ formatTime(item.CreationTime) }}</span>
                        </div>
                    </div>
                    <div class="item-right">
                        <span class="integral-value" :class="getIntegralClass(item.IntegralValue)">
                            {{ formatIntegralValue(item.IntegralValue) }}
                        </span>
                        <span class="integral-unit">分</span>
                    </div>
                </div>
            </div>

            <!-- 分页组件 -->
            <div class="pagination-wrapper" v-if="totalCount > 0">
                <el-pagination
                    background
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="totalCount"
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="[5, 10, 20, 50]"
                    @current-change="handlePageChange"
                    @size-change="handleSizeChange"
                />
            </div>
        </template>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from "@/store";
import { Post } from '@/api/http';
import { OfficeBuilding, Clock, Document } from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import NoImage from '@/assets/图片失效.png';
import { ElMessage } from 'element-plus';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();

// 用户ID
const UserId = computed(() => commonStore.UserId);
// 酒店ID：从 query 获取（允许为 0，表示所有酒店）
const HotelId = computed(() => {
    const id = route.query.HotelId ?? route.query.hotelId;
    if (id === undefined || id === null) return null;
    return Number(id);
});

// 参数有效性：UserId 必须存在，HotelId 可以为 0
const isParamsValid = computed(() => {
    const uid = UserId.value;
    return uid && uid !== '';
});

// 列表数据
const flowList = ref([]);
const totalCount = ref(0);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = ref(8);

// 获取积分流水列表
const fetchFlowList = async () => {
    if (!isParamsValid.value) return;
    
    loading.value = true;
    try {
        // 参数名首字母大写，匹配后端 @JsonProperty
        const params = {
            Page: currentPage.value,
            Limit: pageSize.value,
            UserId: UserId.value,
            HotelId: HotelId.value  // 可以为 0
        };
        
        console.log('请求参数：', params);
        const response = await Post('/HotelIntegral/List', params);
        console.log('接口返回：', response);
        
        const { Data, Success, Msg } = response;
        
        if (Success && Data) {
            // 后端返回字段：Items (大写I), TotalCount (大写T)
            flowList.value = Data.Items || [];
            totalCount.value = Data.TotalCount || 0;
            console.log(`成功获取 ${flowList.value.length} 条记录，总计 ${totalCount.value} 条`);
        } else {
            ElMessage.error(Msg || '获取积分明细失败');
            flowList.value = [];
            totalCount.value = 0;
        }
    } catch (error) {
        console.error('获取积分明细异常：', error);
        ElMessage.error('网络异常，获取积分明细失败');
        flowList.value = [];
        totalCount.value = 0;
    } finally {
        loading.value = false;
    }
};

// 分页事件
const handlePageChange = (page) => {
    currentPage.value = page;
    fetchFlowList();
};

const handleSizeChange = (size) => {
    pageSize.value = size;
    currentPage.value = 1;
    fetchFlowList();
};

// 返回积分列表页
const goBack = () => {
    router.push({ path: '/Front/MyHotelIntegralList' });
};

// 图片加载失败处理
const handleImageError = (event) => {
    event.target.src = NoImage;
};

// 获取酒店封面图
const getHotelCover = (hotel) => {
    if (!hotel) return NoImage;
    if (hotel.Cover?.trim()) return hotel.Cover;
    if (hotel.HotelImages) {
        const images = hotel.HotelImages.split(',').filter(img => img?.trim());
        if (images.length) return images[0].trim();
    }
    return NoImage;
};

// 格式化时间
const formatTime = (time) => {
    if (!time) return '未知时间';
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

// 根据类型返回标签样式
const getTagType = (type) => {
    if (!type) return 'info';
    const incomeTypes = ['活动奖励', '消费赠送', '升级奖励', '签到积分', '入住奖励', '退款返还'];
    const expendTypes = ['积分过期', '积分兑换'];
    if (incomeTypes.includes(type)) return 'success';
    if (expendTypes.includes(type)) return 'danger';
    return 'info';
};

// 格式化积分值
const formatIntegralValue = (value) => {
    const num = Number(value);
    if (isNaN(num)) return '0.00';
    const formatted = num.toFixed(2);
    return num > 0 ? `+${formatted}` : formatted;
};

// 积分值样式类
const getIntegralClass = (value) => {
    const num = Number(value);
    if (isNaN(num)) return '';
    if (num > 0) return 'increase';
    if (num < 0) return 'decrease';
    return '';
};

// 监听参数变化重新加载数据
watch([UserId, HotelId, () => route.query], () => {
    if (isParamsValid.value) {
        currentPage.value = 1;
        fetchFlowList();
    }
}, { immediate: true, deep: true });

// 页面挂载时加载数据
onMounted(() => {
    if (isParamsValid.value) {
        fetchFlowList();
    }
});
</script>

<style scoped lang="scss">
$primary-color: #c9a96e;
$primary-light: #e4cfaa;
$text-dark: #2c3e4e;
$text-gray: #5d6e7a;
$border-light: #eef2f6;
$shadow-sm: 0 8px 20px rgba(0, 0, 0, 0.03), 0 2px 6px rgba(0, 0, 0, 0.05);

.breadcrumb {
    padding: 16px 24px;
    background: #fff;
    border-radius: 12px;
    margin-bottom: 24px;
    box-shadow: $shadow-sm;
    .breadcrumb-label {
        font-size: 14px;
        color: $text-gray;
        margin-right: 8px;
    }
    :deep(.el-breadcrumb) {
        display: inline-block;
        .el-breadcrumb__item {
            .el-breadcrumb__inner {
                color: $text-gray;
                &:hover { color: $primary-color; }
            }
            &:last-child .el-breadcrumb__inner {
                color: $text-dark;
                font-weight: 500;
            }
        }
    }
}

.error-container {
    max-width: 1400px;
    margin: 60px auto;
    padding: 0 20px;
    text-align: center;
}

.flow-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px 40px;
}

.loading-container {
    background: #fff;
    border-radius: 16px;
    padding: 20px;
    box-shadow: $shadow-sm;
}

.flow-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.flow-item {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 20px;
    background: #fff;
    border-radius: 16px;
    border: 1px solid $border-light;
    box-shadow: $shadow-sm;
    transition: all 0.3s ease;
    &:hover {
        transform: translateX(4px);
        border-color: $primary-light;
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
    }
    .item-left {
        flex-shrink: 0;
        .hotel-avatar {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            object-fit: cover;
            border: 1px solid $border-light;
        }
    }
    .item-center {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8px;
        .top-row {
            display: flex;
            align-items: center;
            gap: 12px;
            .title {
                font-size: 16px;
                font-weight: 600;
                color: $text-dark;
            }
        }
        .middle-row {
            display: flex;
            align-items: center;
            gap: 24px;
            flex-wrap: wrap;
            .hotel-name, .order-no {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 13px;
                color: $text-gray;
                .el-icon {
                    font-size: 14px;
                    color: $primary-color;
                }
            }
        }
        .bottom-row {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 12px;
            color: #95a5a6;
            .el-icon { font-size: 12px; }
        }
    }
    .item-right {
        flex-shrink: 0;
        display: flex;
        align-items: baseline;
        gap: 4px;
        .integral-value {
            font-size: 22px;
            font-weight: 700;
            &.increase { color: #00b42a; }
            &.decrease { color: #f53f3f; }
        }
        .integral-unit {
            font-size: 14px;
            color: $text-gray;
        }
    }
}

.pagination-wrapper {
    margin-top: 30px;
    display: flex;
    justify-content: center;
    :deep(.el-pagination) {
        .el-pagination__total {
            color: $text-gray;
        }
        .btn-prev, .btn-next, .el-pager li {
            background-color: #fff;
            border-radius: 8px;
            margin: 0 4px;
            &:hover {
                color: $primary-color;
            }
            &.active {
                background-color: $primary-color;
                color: #fff;
            }
        }
    }
}

@media (max-width: 768px) {
    .flow-container { padding: 0 16px 30px; }
    .flow-item {
        flex-wrap: wrap;
        gap: 12px;
        padding: 16px;
    }
    .item-left .hotel-avatar { width: 50px; height: 50px; }
    .item-right { width: 100%; justify-content: flex-end; }
}
</style>