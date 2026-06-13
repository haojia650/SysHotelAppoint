<template>
    <div>
        <!-- 搜索表单卡片 -->
        <el-card class="box-card">
            <div slot="header" class="clearfix">
                <el-row>
                    <el-button type="primary" size="default" @click="SearchClick">
                        <el-icon>
                            <Search />
                        </el-icon>查询
                    </el-button>
                    <el-button type="warning" size="default" @click="ResetClick">
                        <el-icon>
                            <Refresh />
                        </el-icon>清空条件
                    </el-button>
                </el-row>
            </div>
            <div class="margin-top-sm">
                <el-form :inline="true" :model="searchForm" size="default">
                    <el-form-item label="设施信息" prop="EquipmentInfoIds">
                        <el-input v-model.trim="searchForm.EquipmentInfoIds"  placeholder="请输入设施信息"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="标题" prop="Title">
                        <el-input v-model.trim="searchForm.Title"  placeholder="请输入标题"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="特色房型">
                        <SigleSelect url="/Select/FeaturedRoomShapeEnum" class="search-input" columnName="Name" :clearable="true" columnValue="Code"
                            v-model="searchForm.FeaturedRoomShape">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="酒店">
                        <SigleSelect url="/Hotel/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.HotelId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="早餐类型">
                        <SigleSelect url="/Select/BreakfastTypeEnum" class="search-input" columnName="Name" :clearable="true" columnValue="Code"
                            v-model="searchForm.BreakfastType">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="房型">
                        <SigleSelect url="/Select/RoomShapeEnum" class="search-input" columnName="Name" :clearable="true" columnValue="Code"
                            v-model="searchForm.RoomShape">
                        </SigleSelect>
                    </el-form-item>                
 							 <el-form-item label="是否免费取消" class="search-input">
                 		 <el-select v-model="searchForm.IsFreeCancel" class="search-input" :clearable="true" placeholder="请选择是否免费取消" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
 							 <el-form-item label="是否立即确定" class="search-input">
                 		 <el-select v-model="searchForm.IsImmediatelyConfirm" class="search-input" :clearable="true" placeholder="请选择是否立即确定" >
                          <el-option  key="true"  label="是"  value="true">
                          </el-option>
                           <el-option  key="false"  label="否"  value="false">
                          </el-option>
                        </el-select> 
                    </el-form-item>   
                </el-form>
            </div>
        </el-card>
<!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改房间' : '添加房间'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                <el-col :span="24">
                        <el-form-item label="酒店" prop="HotelId">
                          <SigleSelect url="/Hotel/List" columnName="Name" columnValue="Id"  v-model="formData.HotelId" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                <el-col :span="24">
                        <el-form-item label="标题" prop="Title">
                            <el-input type="text" v-model="formData.Title"  placeholder="请输入标题"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="封面" prop="Cover">
                           <UploadImages :limit="1" v-model="formData.Cover"></UploadImages>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="房间主图" prop="ImageUrls">
                           <UploadImages :limit="3" v-model="formData.ImageUrls"></UploadImages>
                        </el-form-item>
                    </el-col>

                    
                    <el-col :span="24">
                        <el-form-item label="特色房型" prop="FeaturedRoomShape">
                          <SigleSelect url="/Select/FeaturedRoomShapeEnum" columnName="Name" columnValue="Code"  v-model="formData.FeaturedRoomShape" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="房型" prop="RoomShape">
                          <SigleSelect url="/Select/RoomShapeEnum" columnName="Name" columnValue="Code"  v-model="formData.RoomShape" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="房间大小" prop="AreaSize">
                            <el-input-number  v-model="formData.AreaSize"  placeholder="请输入房间大小"     :clearable="true"  :min="0" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="早餐类型" prop="BreakfastType">
                          <SigleSelect url="/Select/BreakfastTypeEnum" columnName="Name" columnValue="Code"  v-model="formData.BreakfastType" >
                          </SigleSelect>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="最小价格" prop="MinPrice">
                            <el-input-number  v-model="formData.MinPrice"  placeholder="请输入最小价格"     :clearable="true"  :min="0" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="最大价格" prop="MaxPrice">
                            <el-input-number  v-model="formData.MaxPrice"  placeholder="请输入最大价格"     :clearable="true"  :min="0" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="积分可抵金额" prop="IntegralExchangeMoney">
                            <el-input-number  v-model="formData.IntegralExchangeMoney"  placeholder="请输入积分可抵金额"     :clearable="true"  :min="0" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="可抵积分" prop="Integral">
                            <el-input-number  v-model="formData.Integral"  placeholder="请输入可抵积分"     :clearable="true"  :min="0" :max="1000000"></el-input-number>
                        </el-form-item>
                    </el-col>
                    
                    <!-- <el-col :span="24">
                        <el-form-item label="设施信息" prop="EquipmentInfoIds">
                            <el-input type="text" v-model="formData.EquipmentInfoIds"  placeholder="请输入设施信息"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col> -->


                    <el-col :span="12">
                        <el-form-item label="是否免费取消" prop="IsFreeCancel">
                       		  <el-switch v-model="formData.IsFreeCancel"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <el-col :span="12">
                        <el-form-item label="是否立即确定" prop="IsImmediatelyConfirm">
                       		  <el-switch v-model="formData.IsImmediatelyConfirm"  :active-value="true" :inactive-value="false">
                            </el-switch>                          
                        </el-form-item>
                    </el-col>

                    <!-- 价格设置-->
                    <el-col :span="24">
                        <el-form-item label="价格设置" prop="RoomPriceDtos">
                            <div style="width: 100%;">
                                <el-table
                                    :data="formData.RoomPriceDtos"
                                    border
                                    style="width: 100%; margin-bottom: 10px;"
                                    size="small"
                                >
                                    <el-table-column label="起始有效时间" width="220">
                                        <template #default="{ row }">
                                            <el-date-picker
                                                v-model="row.BeginActiveTime"
                                                type="datetime"
                                                value-format="YYYY-MM-DDTHH:mm:ss"
                                                placeholder="选择开始时间"
                                                style="width: 100%;"
                                            />
                                        </template>
                                    </el-table-column>

                                    <el-table-column label="截至有效时间" width="220">
                                        <template #default="{ row }">
                                            <el-date-picker
                                                v-model="row.EndActiveTime"
                                                type="datetime"
                                                value-format="YYYY-MM-DDTHH:mm:ss"
                                                placeholder="选择结束时间"
                                                style="width: 100%;"
                                            />
                                        </template>
                                    </el-table-column>

                                    <el-table-column label="价格" width="160">
                                        <template #default="{ row }">
                                            <el-input-number
                                                v-model="row.Price"
                                                :min="0"
                                                :max="1000000"
                                                :precision="2"
                                                controls-position="right"
                                                style="width: 100%;"
                                            />
                                        </template>
                                    </el-table-column>

                                    <el-table-column label="操作" width="100" fixed="right">
                                        <template #default="{ $index }">
                                            <el-button
                                                type="danger"
                                                link
                                                size="small"
                                                @click="removeRoomPrice($index)"
                                            >删 除</el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>

                                <el-button type="primary" size="small" @click="addRoomPrice">
                                    新增价格
                                </el-button>
                            </div>
                        </el-form-item>
                    </el-col>

                    <!-- 优惠设置-->
                    <el-col :span="24">
                        <el-form-item label="优惠设置" prop="FavourableSettingDtos">
                            <div style="width: 100%;">
                                <el-table
                                    :data="formData.FavourableSettingDtos"
                                    border
                                    style="width: 100%; margin-bottom: 10px;"
                                    size="small"
                                >
                                    <el-table-column label="起始有效时间" width="220">
                                        <template #default="{ row }">
                                            <el-date-picker
                                                v-model="row.BeginActiveTime"
                                                type="datetime"
                                                value-format="YYYY-MM-DDTHH:mm:ss"
                                                placeholder="选择开始时间"
                                                style="width: 100%;"
                                            />
                                        </template>
                                    </el-table-column>

                                    <el-table-column label="截至有效时间" width="220">
                                        <template #default="{ row }">
                                            <el-date-picker
                                                v-model="row.EndActiveTime"
                                                type="datetime"
                                                value-format="YYYY-MM-DDTHH:mm:ss"
                                                placeholder="选择结束时间"
                                                style="width: 100%;"
                                            />
                                        </template>
                                    </el-table-column>

                                    <el-table-column label="折扣(0-1)" width="160">
                                        <template #default="{ row }">
                                            <el-input-number
                                                v-model="row.Discount"
                                                :min="0"
                                                :max="1"
                                                :precision="2"
                                                :step="0.1"
                                                controls-position="right"
                                                style="width: 100%;"
                                            />
                                        </template>
                                    </el-table-column>

                                    <el-table-column label="操作" width="100" fixed="right">
                                        <template #default="{ $index }">
                                            <el-button
                                                type="danger"
                                                link
                                                size="small"
                                                @click="removeFavourableSetting($index)"
                                            >删 除</el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>

                                <el-button type="primary" size="small" @click="addFavourableSetting">
                                    新增优惠
                                </el-button>
                            </div>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="房间介绍" prop="Content">
                             <AIRichText v-model="formData.Content" ></AIRichText>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row type="flex" justify="end" align="bottom">
                    <el-form-item>
                        <el-button size="default" type="primary" plain @click="CreateOrEditForm()">确 定</el-button>
                        <el-button size="default" @click="editorShow = false">取 消</el-button>
                    </el-form-item>
                </el-row>
            </el-form>
        </el-dialog>




        <!-- 数据表格 -->
        <PaginationTable ref="PaginationTableId" url="/Room/List" :column="columnList" :where="where">
            <template v-slot:header>
                <el-button type="primary" size="default" @click="ShowEditModal()">
                    <el-icon>
                        <Edit />
                    </el-icon>新 增
                </el-button>
                <el-button type="danger" size="default" @click="BatchDelete">
                    <el-icon>
                        <Delete />
                    </el-icon>批量删除
                </el-button>
            </template>
            <template v-slot:Operate="scope">
              <el-button type="primary" size="default" class="margin-top-xs" @click="ShowEditModal(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>修 改
                </el-button>
                <el-button type="success" size="default" class="margin-top-xs" @click="ShowEquipmentInfoSetting(scope.row)">
                    <el-icon>
                        <Setting />
                    </el-icon>设施信息
                </el-button>
                <el-button type="danger" size="default" class="margin-top-xs" @click="ShowDeleteModal(scope.row.Id)">
                    <el-icon>
                        <Delete />
                    </el-icon>删 除
                </el-button>
            </template>
        </PaginationTable>
        
              
              
    </div>
</template>

<script setup>
import { Post,PostSingleUpdate } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";

import { Delete, Edit, Refresh, Search, Setting } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {onMounted,onBeforeMount,nextTick, watch,computed, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
// 计算属性
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)
const HotelDto = computed(() => commonStore.HotelDto)
const HotelId = computed(() => commonStore.HotelId)
  
//表格条件
const where = reactive({});
// 搜索表单数据
const searchForm = reactive({});



// 编辑表单数据
const formData = reactive({});

// 校验价格时间段（开始<=结束，且各段不交叉）
const validateRoomPriceDtos = () => {
  if (!Array.isArray(formData.RoomPriceDtos)) {
    return { ok: true };
  }

  // 过滤掉没填完整时间的行
  const rows = formData.RoomPriceDtos
    .map((x, idx) => ({
      index: idx,
      begin: x.BeginActiveTime,
      end: x.EndActiveTime,
    }))
    .filter(x => x.begin && x.end);

  // 单条校验：起始时间不能大于截至时间
  for (const r of rows) {
    if (r.begin > r.end) {
      return {
        ok: false,
        message: `第 ${r.index + 1} 条价格的起始时间不能大于截至时间`,
      };
    }
  }

  // 排序后检查是否交叉（这里只允许 [a,b] 和 [b,c] 这种首尾相接，不允许重叠）
  const sorted = [...rows].sort((a, b) => (a.begin > b.begin ? 1 : (a.begin < b.begin ? -1 : 0)));
  for (let i = 1; i < sorted.length; i++) {
    const prev = sorted[i - 1];
    const cur = sorted[i];
    if (cur.begin < prev.end) {
      return {
        ok: false,
        message: `第 ${prev.index + 1} 条与第 ${cur.index + 1} 条的时间区间存在交叉`,
      };
    }
  }

  return { ok: true };
};

// 校验优惠设置时间段（开始<=结束，且各段不交叉）
const validateFavourableSettingDtos = () => {
  if (!Array.isArray(formData.FavourableSettingDtos)) {
    return { ok: true };
  }

  const rows = formData.FavourableSettingDtos
    .map((x, idx) => ({
      index: idx,
      begin: x.BeginActiveTime,
      end: x.EndActiveTime,
    }))
    .filter(x => x.begin && x.end);

  for (const r of rows) {
    if (r.begin > r.end) {
      return {
        ok: false,
        message: `第 ${r.index + 1} 条优惠的起始时间不能大于截至时间`,
      };
    }
  }

  const sorted = [...rows].sort((a, b) => (a.begin > b.begin ? 1 : (a.begin < b.begin ? -1 : 0)));
  for (let i = 1; i < sorted.length; i++) {
    const prev = sorted[i - 1];
    const cur = sorted[i];
    if (cur.begin < prev.end) {
      return {
        ok: false,
        message: `第 ${prev.index + 1} 条优惠与第 ${cur.index + 1} 条优惠的时间区间存在交叉`,
      };
    }
  }

  return { ok: true };
};

// 确保价格列表字段存在
const initRoomPriceDtos = () => {
  if (!Array.isArray(formData.RoomPriceDtos)) {
    formData.RoomPriceDtos = [];
  }
};

// 确保优惠设置列表字段存在
const initFavourableSettingDtos = () => {
  if (!Array.isArray(formData.FavourableSettingDtos)) {
    formData.FavourableSettingDtos = [];
  }
};

// 新增一条价格
const addRoomPrice = () => {
  initRoomPriceDtos();
  formData.RoomPriceDtos.push({
    BeginActiveTime: null,
    EndActiveTime: null,
    Price: null,
    HotelId: formData.HotelId || HotelId.value || null,
    RoomId: formData.Id || null,
  });
};

// 删除一条价格
const removeRoomPrice = (index) => {
  if (!Array.isArray(formData.RoomPriceDtos)) return;
  formData.RoomPriceDtos.splice(index, 1);
};

// 新增一条优惠设置
const addFavourableSetting = () => {
  initFavourableSettingDtos();
  formData.FavourableSettingDtos.push({
    BeginActiveTime: null,
    EndActiveTime: null,
    Discount: null,
    HotelId: formData.HotelId || HotelId.value || null,
    RoomId: formData.Id || null,
  });
};

// 删除一条优惠设置
const removeFavourableSetting = (index) => {
  if (!Array.isArray(formData.FavourableSettingDtos)) return;
  formData.FavourableSettingDtos.splice(index, 1);
};

// 编辑对话框显示状态
const editorShow = ref(false);

// 表单引用
const editModalForm = ref(null);

// 表格列配置（按客人浏览逻辑排序）
const columnList = ref([
  // 隐藏字段
  {
    key: "Id",
    hidden: true,
  },
  {
    key: "HotelId",
    hidden: true,
  },
  {
    key: "FeaturedRoomShape",
    hidden: true,
  },
  {
    key: "BreakfastType",
    hidden: true,
  },
  {
    key: "RoomShape",
    hidden: true,
  },
  {
    key: "HotelDto.Name",
    title: "酒店",
    width: "220px",
    type: ColumnType.SHORTTEXT,
  },

  // 首屏视觉信息
  {
    key: "Cover",
    title: "封面",
    type: ColumnType.IMAGES,
  },
  {
    key: "ImageUrls",
    title: "房间主图",
    type: ColumnType.IMAGES,
    width: "360px",
  },

  // 基本识别信息
  {
    key: "Title",
    title: "标题",
    width: "200px",
    type: ColumnType.SHORTTEXT,
  },

  {
    key: "RoomShapeFormat",
    title: "房型",
    width: "160px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "FeaturedRoomShapeFormat",
    title: "特色房型",
    width: "160px",
    type: ColumnType.SHORTTEXT,
  },

  // 价格与优惠信息
  {
    key: "MinPrice",
    title: "最小价格",
    width: "140px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "MaxPrice",
    title: "最大价格",
    width: "140px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "IntegralExchangeMoney",
    title: "积分可抵金额",
    width: "160px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "Integral",
    title: "可抵积分",
    width: "140px",
    type: ColumnType.SHORTTEXT,
  },

  // 预订规则与体验
  {
    key: "IsFreeCancel",
    title: "是否免费取消",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },
  {
    key: "IsImmediatelyConfirm",
    title: "是否立即确定",
    width: "140px",
    type: ColumnType.JUDGMENTTAG,
  },

  // 房间/设施详情
  {
    key: "BreakfastTypeFormat",
    title: "早餐类型",
    width: "140px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "AreaSize",
    title: "房间大小",
    width: "140px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "EquipmentInfoIds",
    title: "设施信息",
    width: "180px",
    type: ColumnType.SHORTTEXT,
  },
  {
    key: "Content",
    title: "房间介绍",
    width: "200px",
    type: ColumnType.RICHTEXT,
  },

  // 操作列
  {
    title: "操作",
    width: "300px",
    key: "Operate",
    type: ColumnType.USERDEFINED,
  },
]);

// 表单验证规则
const editModalFormRules = reactive({
"MinPrice":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Integral":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AreaSize":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"FeaturedRoomShape":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"HotelId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"EquipmentInfoIds":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsFreeCancel":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IsImmediatelyConfirm":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"IntegralExchangeMoney":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"BreakfastType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"MaxPrice":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Title":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ImageUrls":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Cover":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Content":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RoomShape":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RoomPriceDtos":[
  {
    validator: (rule, value, callback) => {
      const res = validateRoomPriceDtos();
      if (!res.ok) {
        callback(new Error(res.message));
      } else {
        callback();
      }
    },
    trigger: 'change'
  }
],           
"FavourableSettingDtos":[
  {
    validator: (rule, value, callback) => {
      const res = validateFavourableSettingDtos();
      if (!res.ok) {
        callback(new Error(res.message));
      } else {
        callback();
      }
    },
    trigger: 'change'
  }
],           
 });

const ShowEquipmentInfoSetting = (row) => {
    router.push({
      path: "/Admin/RoomEquipmentInfoSetting",
      query: { EquipmentInfoIds: row.EquipmentInfoIds, RoomId: row.Id }
    })
}


// 表格引用
const PaginationTableId = ref(null);

ShowEquipmentInfoSetting

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/Room/Get`, { Id: Id });
    // 记录一个优先级顺序的酒店Id：表单里已有的 -> 搜索条件里的 -> 全局酒店
    const fallbackHotelId = formData.HotelId || searchForm.HotelId || HotelId.value;

    if (!Array.isArray(Data.RoomPriceDtos)) {
        Data.RoomPriceDtos = [];
    }

    if (!Array.isArray(Data.FavourableSettingDtos)) {
        Data.FavourableSettingDtos = [];
    }

    Object.assign(formData, Data);

    // 如果后端返回的数据里没有 HotelId，就用我们算好的 fallback 值，避免每次都要重新选
    if (!formData.HotelId && fallbackHotelId) {
        formData.HotelId = fallbackHotelId;
    }

    initRoomPriceDtos();
    initFavourableSettingDtos();

    editorShow.value = true;

};

// 创建或编辑表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    // 先校验价格时间逻辑
    const check = validateRoomPriceDtos();
    if (!check.ok) {
        ElMessage.error(check.message || '价格时间段设置不合法');
        return;
    }

    // 再校验优惠时间逻辑
    const favCheck = validateFavourableSettingDtos();
    if (!favCheck.ok) {
        ElMessage.error(favCheck.message || '优惠时间段设置不合法');
        return;
    }

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/Room/CreateOrEdit`, formData);

            if (Success) {
                editorShow.value = false;
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('操作成功');
            }
        }
    });
};

// 搜索点击
const SearchClick = () => {
    PaginationTableId.value.Reload(searchForm);
};

// 重置搜索条件
const ResetClick = () => {
    Object.keys(searchForm).forEach(key => {
        searchForm[key] = undefined;
    });
    PaginationTableId.value.Reload(searchForm);
};

// 显示删除确认框
const ShowDeleteModal = async (Id) => {

    try {
        await ElMessageBox.confirm('确认删除该信息吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        const { Success } = await Post(`/Room/Delete`, { Id: Id });
        if (Success) {
            PaginationTableId.value.Reload(searchForm);
            ElMessage.success('删除成功');
        }
    }
    catch (error) {
        ElMessage.warning('用户放弃了操作');
    }
};

// 批量删除
const BatchDelete = async () => {
    const ids = PaginationTableId.value.GetSelectionRow().map(x => x.Id);
    if (ids.length > 0) {
        try {
            await ElMessageBox.confirm('确认删除所选的行数据吗？', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            })


            const { Success } = await Post(`/Room/BatchDelete`, { Ids: ids });
            if (Success) {
                PaginationTableId.value.Reload(searchForm);
                ElMessage.success('删除成功');
            }
        }
        catch (error) {
            ElMessage.warning('用户放弃了操作');
        }
    }
    else {
        ElMessage.warning('用户放弃了操作');
    }
};
onBeforeMount(() => {
    console.log(RoleType.value)
   if(RoleType == "酒店") {
        where.HotelId = HotelId.value;
   }
});
</script>

<style scoped>

</style>