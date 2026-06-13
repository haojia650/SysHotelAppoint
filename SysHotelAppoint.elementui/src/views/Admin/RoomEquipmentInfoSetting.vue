<template>
  <div class="page-container">
    <!-- 设施多选 -->
    <el-card shadow="never" class="card">
      <div class="card-header">
        <span>房型配套设施</span>
      </div>

      <el-skeleton v-if="loading" animated :rows="4" />

      <div v-else class="equipment-wrapper">
        <div
          v-for="group in equipmentGroups"
          :key="group.groupName"
          class="equipment-group"
        >
          <div class="group-title">
            {{ group.groupName || '其他' }}
          </div>

          <el-checkbox-group
            v-model="formData.EquipmentInfoIds"
            class="checkbox-group"
          >
            <el-checkbox
              v-for="item in group.items"
              :key="item.Id"
              :label="item.Id"
              class="equipment-item"
            >
              <span>{{ item.Name }}</span>
              <el-tag
                v-if="!item.IsFree"
                size="small"
                type="warning"
                effect="plain"
                class="fee-tag"
              >
                收费
              </el-tag>
              <el-tag
                v-else
                size="small"
                type="success"
                effect="plain"
                class="fee-tag"
              >
                免费
              </el-tag>
            </el-checkbox>
          </el-checkbox-group>
        </div>
      </div>

      <!-- 仅用于查看当前选择的结果，方便你调试，后续可按需删除 -->
      <div class="selected-preview">
        当前选择的设施 Id 集合：
        <span class="ids">
          {{ formData.EquipmentInfoIds.join(',') || '暂无选择' }}
        </span>
      </div>

      <!-- 底部操作按钮 -->
      <div class="footer-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave">保存设置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import defaultAvatar from '@/assets/默认头像.png'; // 导入默认头像图片
import NoImage from '@/assets/图片失效.png';
import { Post, PostSingleUpdate } from '@/api/http';
import { ref, reactive, nextTick, computed, onBeforeMount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from "@/store";
import { ElMessage, ElMessageBox } from 'element-plus';
const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();
const Token = computed(() => commonStore.Token);
const UserInfo = computed(() => commonStore.UserInfo);
const RoleType = computed(() => commonStore.RoleType);
const UserId = computed(() => commonStore.UserId);

// 表单数据，给后端的就是 formData.EquipmentInfoIds
const parseIds = (val) => {
  if (!val) return [];
  if (Array.isArray(val)) {
    return val.map((v) => Number(v));
  }
  if (typeof val === 'string') {
    return val
      .split(',')
      .map((v) => v.trim())
      .filter(Boolean)
      .map((v) => Number(v));
  }
  return [];
};

const formData = reactive({
  // 初始值从路由上带过来的 EquipmentInfoIds 解析
  EquipmentInfoIds: parseIds(route.query.EquipmentInfoIds),
});

// 二级分组之后的数据：[{ groupName: '前台服务', items: [...] }, ...]
const equipmentGroups = ref([]);
const loading = ref(false);

const GetEquipmentInfoListApi = async () => {
  loading.value = true;
  try {
    const res = await Post('/EquipmentInfo/List', {
      EquipmentInfoIds: formData.EquipmentInfoIds,
    });

    if (!res || !res.Success) {
      ElMessage.error(res?.Msg || '获取配套设施失败');
      return;
    }

    const items = res.Data?.Items || [];

    // 根据 GroupName 做二级分组
    const map = new Map();
    items.forEach((item) => {
      const key = item.GroupName || '其他';
      if (!map.has(key)) {
        map.set(key, []);
      }
      map.get(key).push(item);
    });

    equipmentGroups.value = Array.from(map.entries()).map(([groupName, list]) => ({
      groupName,
      items: list,
    }));
  } catch (e) {
    console.error(e);
    ElMessage.error('获取配套设施异常');
  } finally {
    loading.value = false;
  }
};

// 点击“保存设置”
const handleSave = async () => {
  try {
    const roomId = Number(route.query.RoomId);
    if (!roomId) {
      ElMessage.error('房间信息丢失，无法保存');
      return;
    }

    // 1. 先获取当前房间的完整信息，避免只传部分字段把其他字段清空
    const getRes = await Post('/Room/Get', { Id: roomId });
    if (!getRes || !getRes.Success || !getRes.Data) {
      ElMessage.error(getRes?.Msg || '获取房间信息失败，无法保存设施');
      return;
    }

    const currentRoom = getRes.Data;

    // 2. 把当前选中的设备 Id 数组转成逗号分隔字符串（后端字段是 String）
    const equipmentIdsStr = (formData.EquipmentInfoIds || [])
      .filter((x) => x !== null && x !== undefined)
      .join(',');

    // 3. 用当前房间完整数据 + 新的设施字段一起提交，防止其他字段被覆盖为空
    const payload = {
      ...currentRoom,
      EquipmentInfoIds: equipmentIdsStr,
    };

    const saveRes = await Post('/Room/EquipmentInfoSave', payload);

    if (!saveRes || !saveRes.Success) {
      ElMessage.error(saveRes?.Msg || '保存失败');
      return;
    }

    ElMessage.success('保存成功');
    // 保存成功后返回上一页（房间列表），再次点击设备信息即可回显刚才的选择
    router.back();
  } catch (e) {
    console.error(e);
    ElMessage.error('保存异常');
  }
};

// 点击“取消”
const handleCancel = () => {
  router.back();
};

onBeforeMount(async () => {
  await GetEquipmentInfoListApi();
});
</script>

<style scoped lang="scss">
.page-container {
  padding: 16px;
}

.card {
  width: 100%;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.equipment-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.equipment-group {
  min-width: 260px;
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #fafafa;
}

.group-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.equipment-item {
  margin-right: 0;
}

.fee-tag {
  margin-left: 6px;
}

.selected-preview {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 13px;
  color: #666;
}

.selected-preview .ids {
  color: #409eff;
}

.footer-actions {
  margin-top: 20px;
  text-align: right;
}
</style>