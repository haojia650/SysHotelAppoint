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
                    <el-form-item label="编号" prop="No">
                        <el-input v-model.trim="searchForm.No"  placeholder="请输入编号"  :clearable="true" ></el-input>
                    </el-form-item>
                    <!-- <el-form-item label="评价" prop="Comment">
                        <el-input v-model.trim="searchForm.Comment"  placeholder="请输入评价"  :clearable="true" ></el-input>
                    </el-form-item> -->
                    <!-- <el-form-item label="支付方式" prop="PayType">
                        <el-input v-model.trim="searchForm.PayType"  placeholder="请输入支付方式"  :clearable="true" ></el-input>
                    </el-form-item> -->
                    <!-- <el-form-item label="酒店">
                        <SigleSelect url="/Hotel/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.HotelId">
                        </SigleSelect>
                    </el-form-item>                 -->
                    <el-form-item label="预约状态">
                        <SigleSelect url="/Select/AppointStatusEnum" class="search-input" columnName="Name" :clearable="true" columnValue="Code"
                            v-model="searchForm.AppointStatus">
                        </SigleSelect>
                    </el-form-item>                
                    <!-- <el-form-item label="房间">
                        <SigleSelect url="/Room/List" class="search-input" columnName="Title" :clearable="true" columnValue="Id"
                            v-model="searchForm.RoomId">
                        </SigleSelect>
                    </el-form-item>                 -->
                    <!-- <el-form-item label="房间明细">
                        <SigleSelect url="/RoomDet/List" class="search-input" columnName="No" :clearable="true" columnValue="Id"
                            v-model="searchForm.RoomDetId">
                        </SigleSelect>
                    </el-form-item>                 -->
                    <el-form-item label="下单账号">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.ToUserId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="起始预约时间" class="search-input">
                        <el-date-picker v-model="searchForm.BeginAppointTimeRange" type="datetimerange" start-placeholder="开始起始预约时间"
                            end-placeholder="结束起始预约时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
                    <el-form-item label="支付时间" class="search-input">
                        <el-date-picker v-model="searchForm.PayTimeRange" type="datetimerange" start-placeholder="开始支付时间"
                            end-placeholder="结束支付时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
                    <el-form-item label="截至预约时间" class="search-input">
                        <el-date-picker v-model="searchForm.EndAppointTimeRange" type="datetimerange" start-placeholder="开始截至预约时间"
                            end-placeholder="结束截至预约时间" value-format="YYYY-MM-DD HH:mm:ss" >
                        </el-date-picker>                                       
                    </el-form-item>                
                </el-form>
            </div>
        </el-card>
<!-- 编辑对话框 -->
        <el-dialog :title="'处理取消'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">

                <el-col :span="24">
                        <el-form-item label="退款金额" prop="ReturnMoney">
                            <el-input type="number" v-model="formData.ReturnMoney"  placeholder="请输入退款金额"     :clearable="true"  :min="0"></el-input>
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
        <PaginationTable ref="PaginationTableId" url="/Appoint/List" :column="columnList" :where="where">
             <!-- 🔥 新增：房号列自定义插槽（核心修复） -->
    <template v-slot:RoomMatchDtos="scope">
        <span>{{ scope.row.RoomMatchDtos?.[0]?.RoomDetDto?.No || '-' }}</span>
    </template>

            <template v-slot:header>
                <el-button type="danger" size="default" @click="BatchDelete" v-if="RoleType == '管理员'">
                    <el-icon>
                        <Delete />
                    </el-icon>批量删除
                </el-button>
            </template>
            <template v-slot:Operate="scope">
                <el-button type="success" size="default" class="margin-top-xs" @click="ViewAppointDetail(scope.row)">
                    <el-icon>
                        <View />
                    </el-icon>预约详情
                </el-button>

                <el-button type="success" size="default" v-if="scope.row.AppointStatus == 1" class="margin-top-xs" @click="AssignRoom(scope.row)">
                    <el-icon>
                        <Edit />
                    </el-icon>房间分配
                </el-button>

                <el-button type="success" size="default" class="margin-top-xs" v-if="[6].includes(scope.row.AppointStatus)" @click="HotelConfirm(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>确定接单
                </el-button>

                <el-button type="danger" size="default" class="margin-top-xs" v-if="[6].includes(scope.row.AppointStatus)" @click="HotelCancel(scope.row.Id)">
                    <el-icon>
                        <Close />
                    </el-icon>取消接单
                </el-button>

                <el-button type="primary" size="default" class="margin-top-xs" v-if="[7].includes(scope.row.AppointStatus)" @click="ShowEditModal(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>处理取消
                </el-button>

                <el-button type="primary" size="default" class="margin-top-xs" v-if="[2].includes(scope.row.AppointStatus)" @click="CheckOut(scope.row.Id)">
                    <el-icon>
                        <Edit />
                    </el-icon>退房
                </el-button>

                <el-button type="danger" v-if="RoleType == '管理员'" size="default" class="margin-top-xs" @click="ShowDeleteModal(scope.row.Id) ">
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

import { Close, Delete, Edit, Refresh, Search, View } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {onMounted,onBeforeMount,nextTick, watch,computed, reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
// import AssignRoom from './AssignRoom.vue';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
// 计算属性
const Token = computed(() => commonStore.Token)
const UserInfo = computed(() => commonStore.UserInfo)
const RoleType = computed(() => commonStore.RoleType)
const UserId = computed(() => commonStore.UserId)
const HotelDto = computed(() => commonStore.HotelDto)
const HotelId = computed(() => HotelDto.value.Id)
  
//表格条件
// const where = reactive({});
// 搜索表单数据
const searchForm = reactive({});

// 编辑表单数据
const formData = reactive({});

// 编辑对话框显示状态
const editorShow = ref(false);

// 表单引用
const editModalForm = ref(null);

const where = ref({})

// 表格列配置
const columnList = ref([
  {
    key: "Id",
    hidden: true,

  },
  {
    key: "No",
    title: "编号",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "BeginAppointTime",
    title: "起始预约时间",
    width: "200px",
        
    type: ColumnType.DATETIME, 
  },
  {
    key: "EndAppointTime",
    title: "截至预约时间",
    width: "200px",
        
    type: ColumnType.DATETIME, 
  },
  {
    key: "Qty",
    title: "房间数量",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "PayType",
    title: "支付方式",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "TotalPeopleNum",
    title: "入住总人数",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "HotelDto.Name",
    title: "酒店名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "RoomDto.Title",
    title: "房间标题",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "AppointStatusFormat",
    title: "预约状态",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "PayTime",
    title: "支付时间",
    width: "200px",
    type: ColumnType.DATETIME, 
  },
  {
    key: "Comment",
    title: "评价",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "CommentScore",
    title: "评分",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "ReturnMoney",
    title: "退款金额",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },

  {
    key: "HotelId",
    hidden: true, 
  },

  {
    key: "AppointStatus",
    hidden: true, 
  },  
  
  {
    key: "RoomId",
    hidden: true, 
  },
  {
    key: "RoomDetId",
    hidden: true, 
  },
//   {
//     key: "RoomMatchDtos",
//     title: "房号",
//     width: "160px",
//     type: ColumnType.USERDEFINED, // 关键：改成自定义类型
//   },
  {
    key: "ToUserId",
    hidden: true, 
  },
  {
    key: "ToUserDto.Name",
    title: "名称",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "TotalMoney",
    title: "总金额",
    width: "160px",
        
    type: ColumnType.SHORTTEXT, 
  },
  {
    title: "操作",
    width:"300px",
    key: "Operate",
    type: ColumnType.USERDEFINED,
  },
            ]);

// 表单验证规则
const editModalFormRules = reactive({
"BeginAppointTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"No":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Comment":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ReturnMoney":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"TotalPeopleNum":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"HotelId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"AppointStatus":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RoomId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"Qty":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PayTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"RoomDetId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"EndAppointTime":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"PayType":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"CommentScore":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"ToUserId":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
"TotalMoney":[
{ required: true, message: '该项为必填项', trigger: 'blur' },
              ],           
 });

 const HotelConfirm = async (Id) => {
    ElMessageBox.confirm(`确认接单吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        const { Success } = await Post('/Appoint/HotelConfirm',{ Id: Id });
        if (Success) {
            ElMessage.success('操作成功');
            PaginationTableId.value.Reload(searchForm);
        }
    });
 };

 const HotelCancel = async (Id) => {
    ElMessageBox.confirm(`确认取消接单吗?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        const { Success } = await Post('/Appoint/HotelCancel',{ Id: Id });
        if (Success) {
            ElMessage.success('操作成功');
            PaginationTableId.value.Reload(searchForm);
        }
    });
 };

 const CheckOut = async (Id) => {
    ElMessageBox.confirm('确认退房吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        const { Success } = await Post(`/Appoint/CheckOut`, { Id: Id });
        if (Success) {
            ElMessage.success('操作成功');
            PaginationTableId.value.Reload(searchForm);
        }
    });
 }

const ViewAppointDetail = (item) => {
    router.push({
        path: '/Admin/AppointDetail',
        query: {
            AppointId: item.Id,
        }
    });
};

const AssignRoom = (item) => {
    router.push({
        path: `/Admin/AssignRoom`,
        query: {
            AppointId: item.Id,
            HotelId: item.HotelId,
            RoomId: item.RoomId,
            BeginAppointTime: item.BeginAppointTime,
            EndAppointTime: item.EndAppointTime,
            Qty: item.Qty,
        }
    });
};

// 表格引用
const PaginationTableId = ref(null);

// 显示编辑对话框
const ShowEditModal = async (Id) => {

    const { Data } = await Post(`/Appoint/Get`, { Id: Id });

     // 🔥 核心：打印后端返回的全部数据
     console.log("【后端完整返回数据】", Data);
    console.log("【TotalMoney 字段】", Data.TotalMoney);

    // 核心修复：默认退款金额 = 总金额 × 0.9，空值时默认0
    const totalMoney = Data.TotalMoney || 0; 
    // 优先取后端已计算的退款金额，没有则自动计算90%
    Data.ReturnMoney = Data.ReturnMoney || (totalMoney * 0.9);
    
    Object.assign(formData, Data);
    editorShow.value = true;

};

// 退款确认逻辑
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;

    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/Appoint/HotelDowithUserCancel`, formData);
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
        const { Success } = await Post(`/Appoint/Delete`, { Id: Id });
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


            const { Success } = await Post(`/Appoint/BatchDelete`, { Ids: ids });
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
   if (RoleType.value == '酒店') {
        where.value.HotelId = HotelId.value;
   }
});
</script>

<style scoped>

</style>