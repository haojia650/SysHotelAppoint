<template>
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
        <div class="breadcrumb">
            <span class="breadcrumb-label">您当前的位置:</span>
            <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/Front/Home' }">首页</el-breadcrumb-item>
                <el-breadcrumb-item>酒店列表</el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <!-- 搜索区域：关键词搜索 + 记忆标签 -->
        <div class="search-section">
            <div class="search-wrapper">
                <el-input
                    v-model="searchKeyword"
                    placeholder="输入酒店名称搜索"
                    class="search-input"
                    clearable
                    @keyup.enter="handleSearch"
                >
                    <template #append>
                        <el-button @click="handleSearch" :icon="Search">搜索</el-button>
                    </template>
                </el-input>
            </div>
            <!-- 最近搜索标签 -->
            <div class="recent-tags" v-if="recentKeywords.length">
                <span class="tags-label">最近搜索：</span>
                <el-tag
                    v-for="(kw, idx) in recentKeywords"
                    :key="idx"
                    :closable="true"
                    @close="removeKeyword(kw)"
                    @click="selectKeyword(kw)"
                    class="recent-tag"
                    size="small"
                    effect="plain"
                >
                    {{ kw }}
                </el-tag>
                <el-button type="text" class="clear-tags" @click="clearAllKeywords">清空</el-button>
            </div>
        </div>

        <!-- 分页组件 -->
        <PaginationBox ref="HotelPaginationBox" :where="{ IsAudit: true, KeyWord, GlobalAddress: GlobalAddress }" url="/Hotel/List" :limit="8">
            <template v-slot:content="{ data }">
                <div class="hotel-list-container" style="width: 100%">
                    <!-- 酒店卡片列表 -->
                    <div 
                        class="hotel-card" 
                        v-for="(item, index) in data" 
                        :key="item.Id"
                        @click="ToHotelDetail(item.Id)"
                    >
                        <!-- 酒店封面图 -->
                        <div class="hotel-cover">
                            <img 
                                :src="item.Cover || NoImage" 
                                alt="酒店封面" 
                                class="cover-img"
                                @error="imgError($event)"
                            >
                            <div class="hotel-tags">
                                <el-tag type="success" v-if="item.IsAudit">
                                    <i class="el-icon-check"></i> 已认证
                                </el-tag>
                                <el-tag type="primary" v-if="item.IsPet">
                                    <i class="el-icon-pets"></i> 可携宠物
                                </el-tag>
                            </div>
                        </div>

                        <!-- 酒店基础信息 -->
                        <div class="hotel-info">
                            <h3 class="hotel-name">{{ item.Name }}</h3>
                            <div class="hotel-address">
                                <i class="el-icon-location"></i>
                                <span>{{ item.Address }} ({{ item.ProviceCityArea }})</span>
                            </div>

                            <!-- 酒店服务标签 -->
                            <div class="hotel-services">
                                <span class="service-tag" v-for="service in item.Services.split(',')" :key="service">
                                    {{ service }}
                                </span>
                            </div>

                            <!-- 入住提示 -->
                            <div class="hotel-checkin-tip">
                                <i class="el-icon-info"></i>
                                <span>{{ item.CheckInTip }}</span>
                            </div>
                        </div>

                        <!-- 酒店详情扩展 -->
                        <div class="hotel-expand">
                            <div class="hotel-content" v-html="item.Content"></div>
                            
                            <div class="hotel-notice">
                                <div class="must-read">
                                    <span class="title">必读:</span>
                                    <span class="content">{{ item.MustRead }}</span>
                                </div>
                                <div class="hotel-announce">
                                    <span class="title">公告:</span>
                                    <span class="content">{{ item.Notice }}</span>
                                </div>
                            </div>

                            <div class="hotel-images-preview">
                                <el-button 
                                    type="text" 
                                    icon="el-icon-picture-outline"
                                    @click.stop="previewHotelImages(item.HotelImages)"
                                >
                                    查看{{ item.HotelImages?.split(',').length || 0 }}张酒店图片
                                </el-button>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
        </PaginationBox>
    </div>

    <!-- 图片预览弹窗 -->
    <el-dialog 
        v-model="imagePreviewVisible" 
        title="酒店图片预览" 
        width="80%"
        class="image-preview-dialog"
    >
        <el-carousel height="500px" indicator-position="outside">
            <el-carousel-item v-for="(img, idx) in previewImages" :key="idx">
                <img 
                    :src="img || NoImage" 
                    alt="酒店图片" 
                    class="preview-img"
                    @error="imgError($event)"
                >
            </el-carousel-item>
        </el-carousel>
    </el-dialog>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue';
import defaultAvatar from '@/assets/默认头像.png';
import NoImage from '@/assets/图片失效.png';
import { Post, PostSingleUpdate } from '@/api/http';
import { ref, reactive, nextTick, computed, onBeforeMount, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from "@/store";
import { ElMessage, ElMessageBox, ElImageViewer } from 'element-plus';
import PaginationBox from '../../components/Pagination/PaginationBox.vue';

const HotelPaginationBox = ref(null);
const imagePreviewVisible = ref(false);
const previewImages = ref([]);

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
const Token = computed(() => commonStore.Token);
const UserInfo = computed(() => commonStore.UserInfo);
const RoleType = computed(() => commonStore.RoleType);
const UserId = computed(() => commonStore.UserId);

const searchKeyword = ref('');
const recentKeywords = ref([]);
const STORAGE_KEY = 'recent_hotel_keywords';

const searchParams = reactive({
    IsAudit: true,
    KeyWord: ''
});

onMounted(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
        try {
            recentKeywords.value = JSON.parse(stored);
        } catch (e) {
            console.error('解析最近搜索关键词失败', e);
        }
    }
});

const GlobalAddress = computed(() => {
    if (commonStore.GlobalAddress) {
        if (commonStore.GlobalAddress.area) return commonStore.GlobalAddress.area;
        else if (commonStore.GlobalAddress.city) return commonStore.GlobalAddress.city;
        else if (commonStore.GlobalAddress.province == "全国") return null;
        else return commonStore.GlobalAddress.province;
    } else return null;
});

const saveRecentKeywords = () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(recentKeywords.value));
};

const handleSearch = () => {
    const kw = searchKeyword.value.trim();
    searchParams.KeyWord = kw;
    if (kw) {
        const index = recentKeywords.value.indexOf(kw);
        if (index !== -1) recentKeywords.value.splice(index, 1);
        recentKeywords.value.unshift(kw);
        if (recentKeywords.value.length > 5) recentKeywords.value = recentKeywords.value.slice(0, 5);
        saveRecentKeywords();
    }
    if (HotelPaginationBox.value) HotelPaginationBox.value.Reload();
};

const selectKeyword = (kw) => {
    searchKeyword.value = kw;
    handleSearch();
};

const removeKeyword = (kw) => {
    const index = recentKeywords.value.indexOf(kw);
    if (index !== -1) {
        recentKeywords.value.splice(index, 1);
        saveRecentKeywords();
    }
};

const clearAllKeywords = () => {
    recentKeywords.value = [];
    saveRecentKeywords();
};

const ToHotelDetail = (Id) => {
    router.push({ path: '/Front/HotelDetail', query: { HotelId: Id } })
}

const imgError = (e) => {
    e.target.src = NoImage;
};

const previewHotelImages = (imagesStr) => {
    if (imagesStr) {
        previewImages.value = imagesStr.split(',').filter(img => img);
        imagePreviewVisible.value = true;
    } else {
        ElMessage.info('该酒店暂无图片');
    }
};
</script>

<style scoped lang="scss">
// 全局容器 - 商务简约浅灰背景
.breadcrumb-container {
    width: 100%;
    padding: 24px;
    box-sizing: border-box;
    background: #F5F7FA;
    min-height: 100vh;
}

// 面包屑 - 简约商务样式
.breadcrumb {
    margin-bottom: 20px;
    .breadcrumb-label {
        margin-right: 10px;
        font-size: 14px;
        color: #86909C;
    }
    :deep(.el-breadcrumb) {
        --el-breadcrumb-item-color: #86909C;
        --el-breadcrumb-item-active-color: #165DFF;
    }
}

// 搜索区域 - 简约卡片式
.search-section {
    margin-bottom: 24px;
    background: #ffffff;
    border-radius: 8px;
    padding: 16px 20px;
    border: 1px solid #E5E6EB;

    .search-wrapper {
        max-width: 500px;
        .search-input {
            --el-button-font-weight: 500;
        }
    }

    .recent-tags {
        margin-top: 12px;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
        .tags-label {
            color: #86909C;
            font-size: 13px;
        }
        .recent-tag {
            cursor: pointer;
            transition: all 0.2s;
            &:hover {
                border-color: #165DFF;
                color: #165DFF;
            }
        }
        .clear-tags {
            color: #F53F3F;
            font-size: 12px;
            &:hover {
                color: #FF7875;
            }
        }
    }
}

// 酒店列表网格布局
.hotel-list-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 24px;
    padding: 10px 0;
    width: 100%;
}

// 酒店卡片 - 商务简约核心
.hotel-card {
    background: #ffffff;
    border: 1px solid #E5E6EB;
    border-radius: 10px;
    overflow: hidden;
    transition: all 0.25s ease;
    cursor: pointer;

    &:hover {
        border-color: #D0D3D9;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    // 封面图
    .hotel-cover {
        position: relative;
        height: 220px;
        overflow: hidden;
        .cover-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
        }
        &:hover .cover-img {
            transform: scale(1.03);
        }
        .hotel-tags {
            position: absolute;
            top: 15px;
            right: 15px;
            display: flex;
            gap: 8px;
        }
    }

    // 基础信息
    .hotel-info {
        padding: 18px;
        .hotel-name {
            font-size: 18px;
            font-weight: 500;
            color: #1D2129;
            margin: 0 0 10px 0;
            border-bottom: 1px dashed #E5E6EB;
            padding-bottom: 8px;
        }
        .hotel-address {
            display: flex;
            align-items: center;
            color: #86909C;
            font-size: 14px;
            margin-bottom: 12px;
            i {
                margin-right: 6px;
                color: #165DFF;
            }
        }
        .hotel-services {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            margin-bottom: 10px;
            .service-tag {
                background: #F7F8FA;
                color: #4E5969;
                padding: 3px 8px;
                border-radius: 4px;
                font-size: 12px;
                border: 1px solid #E5E6EB;
            }
        }
        .hotel-checkin-tip {
            display: flex;
            align-items: center;
            color: #4E5969;
            font-size: 13px;
            i {
                margin-right: 6px;
                color: #165DFF;
            }
        }
    }

    // 扩展信息
    .hotel-expand {
        padding: 0 18px 18px;
        border-top: 1px solid #F2F3F5;
        .hotel-content {
            color: #4E5969;
            font-size: 14px;
            line-height: 1.6;
            margin-bottom: 12px;
            max-height: 60px;
            overflow: hidden;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
        }
        .hotel-notice {
            margin-bottom: 12px;
            .must-read, .hotel-announce {
                display: flex;
                font-size: 13px;
                margin-bottom: 4px;
                .title {
                    color: #165DFF;
                    font-weight: 500;
                    margin-right: 6px;
                    min-width: 40px;
                }
                .content {
                    color: #86909C;
                    flex: 1;
                }
            }
        }
        .hotel-images-preview {
            :deep(.el-button) {
                color: #165DFF;
                padding: 0;
                font-size: 13px;
                &:hover {
                    color: #4080FF;
                }
            }
        }
    }
}

// 弹窗简约样式
.image-preview-dialog {
    .preview-img {
        width: 100%;
        height: 100%;
        object-fit: contain;
    }
}

// 响应式适配
@media (max-width: 768px) {
    .hotel-list-container {
        grid-template-columns: 1fr;
    }
    .image-preview-dialog :deep(.el-carousel) {
        height: 300px !important;
    }
}
</style>