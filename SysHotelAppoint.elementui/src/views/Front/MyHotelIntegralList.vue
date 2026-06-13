<template>
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
        <span class="breadcrumb-label">您当前的位置:</span>
        <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>我的酒店积分</el-breadcrumb-item>
        </el-breadcrumb>
    </div>

    <!-- 页面标题 + 总积分 -->
    <div class="page-header">
        <h2 class="page-title">我的酒店积分</h2>
        <div class="total-points">总积分: <span class="points-num">{{ totalPoints.toFixed(0) }}</span></div>
    </div>

    <!-- 积分列表主区域 -->
    <div class="integral-container">
        <div v-loading="loading" class="loading-wrapper" v-if="loading">
            <span>正在加载您的积分数据...</span>
        </div>

        <el-empty v-else-if="!loading && hotelIntegralList.length === 0" description="暂无酒店积分记录" :image-size="120" />

        <div v-else class="integral-list">
            <div 
                v-for="item in hotelIntegralList" 
                :key="item.hotelId" 
                class="integral-card"
                @click="goToHotelDetail(item.Hotel?.Id || item.hotelId)"
            >
                <!-- 左侧：酒店图片 -->
                <div class="card-left">
                    <img 
                        :src="getHotelCover(item.Hotel)" 
                        :alt="item.Hotel?.Name || '酒店图片'"
                        @error="handleImageError"
                        class="hotel-img"
                    />
                </div>

                <!-- 中间：酒店信息 -->
                <div class="card-middle">
                    <h3 class="hotel-name">{{ item.Hotel?.Name || '酒店名称' }}</h3>
                    <div class="hotel-address">{{ item.Hotel?.Address || '地址信息待更新' }}</div>
                    <div class="service-tags" v-if="serviceList(item.Hotel).length">
                        <span 
                            v-for="(service, idx) in serviceList(item.Hotel).slice(0, 3)" 
                            :key="idx"
                            class="service-tag"
                        >
                            {{ service }}
                        </span>
                    </div>
                </div>

                <!-- 右侧：圆形积分 + 查看流水按钮 -->
                <div class="card-right">
                    <div class="integral-circle">
                        <span class="integral-num">{{ item.TotalIntegral?.toFixed(0) }}</span>
                        <span class="integral-unit">积分</span>
                    </div>
                    <el-button 
                        type="primary" 
                        class="flow-btn"
                        @click.stop="goToHotelIntegralDetList(item.hotelId)"
                    >
                        查看积分流水
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import NoImage from '@/assets/图片失效.png';
import { Post } from '@/api/http';
import { ref, computed, onBeforeMount } from 'vue';
import { useRouter } from 'vue-router';
import { useCommonStore } from "@/store";
import { ElMessage } from 'element-plus';

const router = useRouter();
const commonStore = useCommonStore();

const UserId = computed(() => commonStore.UserId);
const hotelIntegralList = ref([]);
const loading = ref(false);

const totalPoints = computed(() => {
    if (!hotelIntegralList.value.length) return 0;
    return hotelIntegralList.value.reduce((sum, item) => sum + (item.TotalIntegral || 0), 0);
});

// 获取酒店封面图（传入 Hotel 对象）
const getHotelCover = (hotel) => {
    if (!hotel) return NoImage;
    if (hotel.Cover && hotel.Cover.trim() !== '') {
        return hotel.Cover;
    }
    if (hotel.HotelImages) {
        const images = hotel.HotelImages.split(',');
        if (images.length && images[0].trim() !== '') {
            return images[0].trim();
        }
    }
    return NoImage;
};

const handleImageError = (event) => {
    event.target.src = NoImage;
};

const serviceList = (hotel) => {
    if (!hotel || !hotel.Services) return [];
    return hotel.Services.split(',').map(s => s.trim()).filter(s => s);
};

// 🔴 核心修改1：跳转酒店详情的参数名从 id 改为 HotelId，匹配后端路由
const goToHotelDetail = (hotelId) => {
    if (!hotelId) {
        ElMessage.warning('酒店信息缺失，无法查看详情');
        return;
    }
    router.push({
        path: '/Front/HotelDetail',
        query: { HotelId: hotelId }
    });
};

// 🔴 核心修改2：去掉错误的 .value（hotelId 是普通数值，不是 ref 对象）
const goToHotelIntegralDetList = (hotelId) => {
    router.push({
        path: '/Front/HotelIntegralDetList',
        query: { 
            userId: UserId.value,
            HotelId: hotelId
        }
    });
};

const GetUserAllHotelIntegral = async () => {
    if (!UserId.value) {
        ElMessage.warning('用户信息缺失，请重新登录');
        return;
    }
    loading.value = true;
    try {
        const { Success, Data, Msg } = await Post(`/HotelIntegral/GetUserAllHotelIntegral`, { UserId: UserId.value });
        if (Success) {
            hotelIntegralList.value = Data || [];
        } else {
            ElMessage.error(Msg || '获取积分列表失败');
        }
    } catch (error) {
        console.error('积分接口异常:', error);
        ElMessage.error('网络异常，请稍后重试');
    } finally {
        loading.value = false;
    }
};

onBeforeMount(() => {
    GetUserAllHotelIntegral();
});
</script>

<style scoped lang="scss">
$primary-color: #409eff;
$danger-color: #f56c6c;
$tag-bg: #e6f2ff;
$tag-text: #409eff;
$text-dark: #303133;
$text-gray: #606266;
$border-light: #e4e7ed;
$bg-light: #f5f7fa;
$shadow-sm: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

.breadcrumb {
    padding: 16px 0;
    background: transparent;
    margin-bottom: 16px;

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
                &:hover {
                    color: $primary-color;
                }
            }
            &:last-child .el-breadcrumb__inner {
                color: $text-dark;
                font-weight: 500;
            }
        }
    }
}

.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding-bottom: 12px;
    border-bottom: 1px solid $border-light;

    .page-title {
        font-size: 24px;
        font-weight: 700;
        color: $text-dark;
        margin: 0;
    }

    .total-points {
        font-size: 18px;
        color: $text-gray;
        .points-num {
            color: $primary-color;
            font-weight: 700;
            font-size: 24px;
        }
    }
}

.integral-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 0 40px;
}

.loading-wrapper {
    text-align: center;
    padding: 80px 0;
    color: $text-gray;
    font-size: 14px;
}

.integral-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.integral-card {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: $shadow-sm;
    display: flex;
    align-items: center;
    gap: 20px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
        box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
    }

    .card-left {
        flex-shrink: 0;
        width: 180px;
        height: 180px;
        border-radius: 8px;
        overflow: hidden;
        background: $bg-light;

        .hotel-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .card-middle {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 12px;

        .hotel-name {
            font-size: 20px;
            font-weight: 700;
            color: $text-dark;
            margin: 0;
            line-height: 1.4;
        }

        .hotel-address {
            font-size: 14px;
            color: $text-gray;
            line-height: 1.5;
        }

        .service-tags {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;

            .service-tag {
                background: $tag-bg;
                color: $tag-text;
                padding: 4px 12px;
                border-radius: 4px;
                font-size: 14px;
                line-height: 1.5;
            }
        }
    }

    .card-right {
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 16px;

        .integral-circle {
            width: 100px;
            height: 100px;
            border: 2px solid $danger-color;
            border-radius: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: $danger-color;

            .integral-num {
                font-size: 28px;
                font-weight: 700;
                line-height: 1.2;
            }

            .integral-unit {
                font-size: 14px;
                line-height: 1.2;
            }
        }

        .flow-btn {
            border-radius: 4px;
            padding: 8px 20px;
            font-size: 14px;
            font-weight: 500;
        }
    }
}

@media (max-width: 768px) {
    .integral-card {
        flex-direction: column;
        align-items: flex-start;
        .card-left {
            width: 100%;
            height: 200px;
        }
        .card-right {
            width: 100%;
            flex-direction: row;
            justify-content: space-between;
        }
    }
    .page-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
    }
}
</style>