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
                    <el-form-item label="地址" prop="Address">
                        <el-input v-model.trim="searchForm.Address"  placeholder="请输入地址"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="姓名" prop="Name">
                        <el-input v-model.trim="searchForm.Name"  placeholder="请输入姓名"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="薪资" prop="Salary">
                        <el-input v-model.trim="searchForm.Salary"  placeholder="请输入薪资"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="性别" prop="Sex">
                        <el-input v-model.trim="searchForm.Sex"  placeholder="请输入性别"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="身份证" prop="IdCard">
                        <el-input v-model.trim="searchForm.IdCard"  placeholder="请输入身份证"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="电话" prop="Phone">
                        <el-input v-model.trim="searchForm.Phone"  placeholder="请输入电话"  :clearable="true" ></el-input>
                    </el-form-item>
                    <el-form-item label="酒店">
                        <SigleSelect url="/Hotel/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.HotelId">
                        </SigleSelect>
                    </el-form-item>                
                    <el-form-item label="关联账号">
                        <SigleSelect url="/User/List" class="search-input" columnName="Name" :clearable="true" columnValue="Id"
                            v-model="searchForm.UserId">
                        </SigleSelect>
                    </el-form-item>                
                </el-form>
            </div>
        </el-card>
        <!-- 编辑对话框 -->
        <el-dialog :title="formData.Id ? '修改员工' : '添加员工'" v-model="editorShow" width="50%" :lock-scroll="true"
            min-height="500px">
            <el-form v-if="editorShow" ref="editModalForm" :rules="editModalFormRules" :model="formData" label-width="140px"
                size="default">             
              <el-row :gutter="10" class="edit-from-body">
                    <el-col :span="24">
                        <el-form-item label="姓名" prop="Name">
                            <el-input type="text" v-model="formData.Name"  placeholder="请输入姓名"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="性别" prop="Sex">
                            <el-select v-model="formData.Sex" placeholder="请选择性别">
                                <el-option label="男" value="男"></el-option>
                                <el-option label="女" value="女"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="身份证" prop="IdCard">
                            <el-input type="text" v-model="formData.IdCard"  placeholder="请输入身份证"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="出生年月" prop="BirthDay">
                            <el-date-picker v-model="formData.BirthDay" align="right" type="date" placeholder="选择出生时间" format="YYYY-MM-DD HH:mm:ss"
                                value-format="YYYY-MM-DD HH:mm:ss">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="照片" prop="Photo">
                           <UploadImages :limit="1" v-model="formData.Photo"></UploadImages>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="电话" prop="Phone">
                            <el-input type="text" v-model="formData.Phone"  placeholder="请输入电话"     :clearable="true"></el-input>
                        </el-form-item>
                    </el-col>

                    <!-- 【修改】地址改成纯文字可编辑输入框，去掉地图选点按钮 -->
                    <el-col :span="24">
  									 <el-form-item label="住址" prop="Address">
                            <el-input 
                                type="text" 
                                v-model="formData.Address" 
                                placeholder="请输入详细住址" 
                                :clearable="true"
                                maxlength="255"
                                show-word-limit
                            ></el-input>
                        </el-form-item>
                    </el-col>

                    <el-col :span="24">
                        <el-form-item label="薪资" prop="Salary">
                            <el-input type="text" v-model="formData.Salary"  placeholder="请输入薪资"     :clearable="true"></el-input>
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
        <PaginationTable ref="PaginationTableId" url="/Employee/List" :where="{ HotelId: HotelDto.Id}" :column="columnList">
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
import { Post } from '@/api/http';
import { ColumnType } from '@/components/Tables/columnTypes';
import { useCommonStore } from "@/store";
import { Delete, Edit, Refresh, Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { reactive, ref, computed } from 'vue';

const commonStore = useCommonStore();
const HotelDto = computed(() => commonStore.HotelDto);

// 搜索表单
const searchForm = reactive({});
// 表单数据 - 去掉经纬度字段，完全匹配原有员工表
const formData = reactive({
    Id: null,
    HotelId: null,
    Address: "",
    Name: "",
    Photo: "",
    Salary: "",
    No: "",
    Sex: "",
    BirthDay: "",
    UserId: null,
    IdCard: "",
    Phone: ""
});

const editorShow = ref(false);
const editModalForm = ref(null);
const PaginationTableId = ref(null);

// 表格列配置
const columnList = ref([
  {
    key: "Id",
    hidden: true,
  },
  {
    key: "HotelId",
    hidden: true, 
  },
  {
    key: "HotelDto.Name",
    title: "酒店名称",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Address",
    title: "地址",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Name",
    title: "姓名",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Photo",
    title: "照片",
    type:ColumnType.IMAGES,
  },
  {
    key: "Salary",
    title: "薪资",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "No",
    title: "工号",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Sex",
    title: "性别",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "BirthDay",
    title: "出生年月",
    width: "160px",
    type: ColumnType.DATE, 
  },
  {
    key: "UserId",
    hidden: true, 
  },
  {
    key: "IdCard",
    title: "身份证",
    width: "160px",
    type: ColumnType.SHORTTEXT, 
  },
  {
    key: "Phone",
    title: "电话",
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

// 【修复】表单验证规则，去掉无效必填项，彻底解决提交卡住问题
const editModalFormRules = reactive({
    Name: [
        { required: true, message: '姓名为必填项', trigger: 'blur' },
    ],
    Sex: [
        { required: true, message: '性别为必填项', trigger: 'change' },
    ],
    IdCard: [
        { required: true, message: '身份证为必填项', trigger: 'blur' },
    ],
    BirthDay: [
        { required: true, message: '出生年月为必填项', trigger: 'change' },
    ],
    Phone: [
        { required: true, message: '电话为必填项', trigger: 'blur' },
        {
            validator: (rule, value, callback) => {
                const reg =/^1[3-9]\d{9}$/;
                if (!value || !reg.test(value)) {
                    callback(new Error('请输入正确的手机号'));
                } else {
                    callback();
                }
            }, trigger: 'blur'
        },
    ],
    Address: [
        { required: true, message: '住址为必填项', trigger: 'blur' },
    ],
    Salary: [
        { required: true, message: '薪资为必填项', trigger: 'blur' },
    ]
});

// 显示编辑弹窗
const ShowEditModal = async (Id) => {
    const { Data } = await Post(`/Employee/Get`, { Id: Id });
    Data.HotelId = HotelDto.value.Id;
    Object.assign(formData, Data);
    editorShow.value = true;
};

// 提交表单
const CreateOrEditForm = async () => {
    if (!editModalForm.value) return;
    await editModalForm.value.validate(async valid => {
        if (valid) {
            const { Success } = await Post(`/Employee/CreateOrEdit`, formData);
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
        const { Success } = await Post(`/Employee/Delete`, { Id: Id });
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
            const { Success } = await Post(`/Employee/BatchDelete`, { Ids: ids });
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
</script>

<style scoped>
/* 删掉所有腾讯地图水印相关的CSS，不需要了 */
</style>