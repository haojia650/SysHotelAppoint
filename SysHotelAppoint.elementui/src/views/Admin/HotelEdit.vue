<template>
    <div class="card">
        <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
            size="default">
            <el-row :gutter="10">

                <el-col :span="24">
                    <el-form-item label="名称" prop="Name">
                        <el-input type="text" v-model="formData.Name" placeholder="请输入名称" :clearable="true"></el-input>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="酒店封面" prop="Cover">
                        <UploadImages :limit="1" v-model="formData.Cover"></UploadImages>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="酒店图片" prop="HotelImages">
                        <UploadImages :limit="3" v-model="formData.HotelImages"></UploadImages>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="服务设施" prop="Services">
                        <el-input type="text" v-model="formData.Services" placeholder="请输入服务设施" :clearable="true"></el-input>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="酒店通知" prop="Notice">
                        <el-input type="text" v-model="formData.Notice" placeholder="请输入酒店通知" :clearable="true"></el-input>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="入住提示" prop="CheckInTip">
                        <el-input type="text" v-model="formData.CheckInTip" placeholder="请输入入住提示" :clearable="true"></el-input>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="必读" prop="MustRead">
                        <el-input type="text" v-model="formData.MustRead" placeholder="请输入必读" :clearable="true"></el-input>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="是否可带宠物" prop="IsPet">
                        <el-switch v-model="formData.IsPet" :active-value="true" :inactive-value="false">
                        </el-switch>
                    </el-form-item>
                </el-col>

                <!-- 酒店详细地址 -->
                <el-col :span="24">
                    <el-form-item label="酒店详细地址" prop="Address">
                        <el-input type="text" v-model="formData.Address" placeholder="请选择酒店详细地址" :clearable="true"
                            :disabled="true">
                            <template #append>
                                <el-button type="primary" size="small" @click="AddressOpenLogLatSelectToast">选择地址</el-button>
                            </template>
                        </el-input>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="审核状态" prop="IsAudit">
                        <div class="audit-status">
                            <div v-if="formData.IsAudit == true">
                                <el-tag type="success">通过</el-tag>
                            </div>
                            <div v-if="formData.IsAudit == false">
                                <el-tag type="danger">不通过</el-tag>
                            </div>
                            <div v-if="formData.IsAudit == null">
                                <el-tag type="warning">待审核</el-tag>
                            </div>
                        </div>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="审核结果" prop="AuditResult">
                        <div>
                            {{ formData.AuditResult }}
                        </div>
                    </el-form-item>
                </el-col>

                <el-col :span="24">
                    <el-form-item label="酒店介绍" prop="Content">
                        <AIRichText v-model="formData.Content"></AIRichText>
                    </el-form-item>
                </el-col>

            </el-row>

            <el-row type="flex" justify="end" align="bottom">
                <el-form-item>
                    <el-button v-if="formData.IsAudit == null" size="default" type="primary" plain
                        @click="CreateOrEditForm()">提交审核</el-button>
                    <el-button v-if="formData.IsAudit == true" size="default" type="primary" plain
                        @click="CreateOrEditForm()">提交修改</el-button>
                    <el-button v-if="formData.IsAudit == false" size="default" type="primary" plain
                        @click="CreateOrEditForm()">再次提交审核</el-button>
                </el-form-item>
            </el-row>
        </el-form>

        <LogLatSelectToast ref="AddressLogLatSelectToastRef"></LogLatSelectToast>
    </div>
</template>

<script setup>
import { Post } from '@/api/http';
import { useCommonStore } from "@/store";
import { ElMessage } from 'element-plus';
import { reactive, ref, computed, onBeforeMount, nextTick } from 'vue';

const commonStore = useCommonStore();
const UserId = computed(() => commonStore.UserId);

const formData = reactive({
    Id: null,
    Name: "",
    HotelUserId: null,
    Cover: "",
    HotelImages: "",
    Services: "",
    Notice: "",
    CheckInTip: "",
    MustRead: "",
    IsPet: false,
    Address: "",
    Latitude: "",
    Longitude: "",
    ProviceCityArea: "",
    IsAudit: null,
    AuditResult: "",
    Content: ""
});

const editorShow = ref(false);
const editModalForm = ref(null);
const AddressLogLatSelectToastRef = ref(null);

const editModalFormRules = reactive({
    "Name": [{ required: true, message: '酒店名称为必填项', trigger: 'blur' }],
    "Address": [{ required: true, message: '酒店地址为必填项', trigger: 'blur' }],
    "MustRead": [{ required: true, message: '必读信息为必填项', trigger: 'blur' }],
    "Content": [{ required: true, message: '酒店介绍为必填项', trigger: 'blur' }],
    "Cover": [{ required: true, message: '酒店封面为必填项', trigger: 'blur' }],
    "IsPet": [{ required: true, message: '请选择是否可带宠物', trigger: 'change' }],
    "HotelImages": [{ required: true, message: '酒店图片为必填项', trigger: 'blur' }],
    "CheckInTip": [{ required: true, message: '入住提示为必填项', trigger: 'blur' }],
    "Services": [{ required: true, message: '服务设施为必填项', trigger: 'blur' }],
    "Notice": [{ required: true, message: '酒店通知为必填项', trigger: 'blur' }]
});

/**
 * 打开地址选择器（使用你原来的 LogLatSelectToast 组件）
 * 核心修复：用逆地址解析补全省市区，失败时绝不显示经纬度，降级为原地址
 */
const AddressOpenLogLatSelectToast = () => {
    AddressLogLatSelectToastRef.value.OpenToast(
        formData.Latitude,
        formData.Longitude,
        formData.Address,
        async (data) => {
            console.log("🗺️ 腾讯地图选点原始返回：", JSON.stringify(data, null, 2));

            // 1. 提取经纬度（兼容各种字段名）
            const lat = parseFloat(data.lat || data.latitude || data.location?.lat || data.latlng?.lat);
            const lng = parseFloat(data.lng || data.longitude || data.location?.lng || data.latlng?.lng);

            // 2. 组件返回的原始地址（作为降级方案）
            const originalAddr = data.address || data.fullAddress || data.poiaddress || data.name || "";

            // 先赋值经纬度
            if (!isNaN(lat) && !isNaN(lng)) {
                formData.Latitude = lat;
                formData.Longitude = lng;
            }

            // 3. 尝试通过逆地址解析获取完整省市区地址
            let finalAddress = originalAddr; // 默认用原始地址
            if (!isNaN(lat) && !isNaN(lng)) {
                try {
                    const full = await resolveAddressByLatLng(lat, lng);
                    if (full) {
                        finalAddress = full;
                    } else {
                        console.warn("⚠️ 逆地址解析未返回有效地址，使用原始地址");
                    }
                } catch (err) {
                    console.error("❌ 逆地址解析过程异常：", err);
                    // 失败时保留原始地址，不覆盖为经纬度
                }
            } else {
                console.warn("⚠️ 未获取到有效经纬度，无法逆地址解析");
            }

            // 4. 更新表单地址（绝不会是经纬度字符串）
            formData.Address = finalAddress;

            // 5. 尝试从 finalAddress 中提取省市区存入 ProviceCityArea
            extractProvinceCity(finalAddress);

            // 6. 手动触发校验，强制更新视图
            nextTick(() => {
                if (editModalForm.value) {
                    editModalForm.value.validateField("Address");
                }
            });

            ElMessage.success("地址选择成功");
        }
    );
};

/**
 * 逆地址解析：优先使用 TMap.Geocoder，失败则尝试 REST API（如配置了 MAP_KEY）
 */
const resolveAddressByLatLng = async (lat, lng) => {
    // 方案1：使用腾讯地图 JavaScript API Geocoder
    const TMap = window.TMap;
    if (TMap && TMap.Geocoder) {
        try {
            const geocoder = new TMap.Geocoder();
            const result = await geocoder.getAddress({ location: new TMap.LatLng(lat, lng) });
            if (result.status === 0 && result.result) {
                const comp = result.result.address_component;
                const province = comp.province || "";
                const city = comp.city || "";
                const district = comp.district || "";
                const street = comp.street || "";
                const streetNumber = comp.street_number || "";
                // 拼接完整地址（去重处理）
                const full = [province, city, district, street, streetNumber].filter(Boolean).join("");
                // 如果 result.result.address 已经包含省市区，直接用它
                const addr = result.result.address || full;
                console.log("✅ TMap.Geocoder 解析成功：", addr);
                return addr;
            } else {
                console.warn("TMap.Geocoder 返回异常状态：", result);
            }
        } catch (e) {
            console.warn("TMap.Geocoder 调用失败：", e);
        }
    }

    // 方案2（备用）：使用腾讯地图 REST API（需要你的 key）
    // 如果你有开通 WebService API 的 key，把它填在下面引号里即可启用
    const MAP_KEY = ""; // TODO: 把你的腾讯地图 key 填在这里
    if (MAP_KEY) {
        try {
            const url = `https://apis.map.qq.com/ws/geocoder/v1/?location=${lat},${lng}&key=${MAP_KEY}&get_poi=0`;
            const response = await fetch(url);
            const res = await response.json();
            if (res.status === 0 && res.result) {
                console.log("✅ REST API 解析成功：", res.result.address);
                return res.result.address; // 这个 address 已包含完整省市街道
            } else {
                console.warn("REST API 返回错误：", res);
            }
        } catch (e) {
            console.warn("REST API 请求失败：", e);
        }
    } else {
        console.log("💡 提示：未配置 MAP_KEY，跳过 REST API 逆地址解析。如需使用，请在代码中填写你的腾讯地图 key。");
    }

    return null; // 所有方法都失败，返回 null，外部会降级为 originalAddr
};

/**
 * 简单提取省市区（用于填充 ProviceCityArea 字段）
 */
const extractProvinceCity = (address) => {
    if (!address) return;
    // 简单匹配省、市、区
    const provinceMatch = address.match(/(.+?省|.+?自治区|上海|北京|天津|重庆)/);
    const cityMatch = address.match(/(.+?市|.+?自治州|.+?地区|.+?盟)/);
    const districtMatch = address.match(/(.+?区|.+?县|.+?旗)/);
    const province = provinceMatch ? provinceMatch[0] : "";
    const city = cityMatch ? cityMatch[0] : "";
    const district = districtMatch ? districtMatch[0] : "";
    formData.ProviceCityArea = [province, city, district].filter(Boolean).join(",");
};

// 显示编辑对话框
const ShowEditModal = async (Id) => {
    const { Data } = await Post(`/Hotel/Get`, { Id: 0, HotelUserId: UserId.value });
    Data.HotelUserId = UserId.value;
    Object.assign(formData, Data);
    editorShow.value = true;
};

// 提交表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            if (formData.IsAudit == false) {
                formData.IsAudit = null;
                formData.AuditResult = null;
            }
            const { Success } = await Post(`/Hotel/CreateOrEdit`, formData);
            if (Success) {
                ElMessage.success('操作成功');
                location.reload();
            }
        }
    });
};

onBeforeMount(() => {
    ShowEditModal(null);
});
</script>

<style scoped>
/* 你的原有样式 */
</style>