<template>
  <!-- 顶部导航：面包屑 + 返回按钮 -->
  <div class="top-nav">
    <div class="breadcrumb">
      <span>控制台</span>
      <span class="separator">/</span>
      <span>房间分配</span>
      <span class="separator">/</span>
    </div>
    <el-button type="text" @click="handleBack" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>
  </div>

  <!-- 日期选择按钮组 -->
  <div class="date-selector">
    <el-button-group>
      <el-button
        v-for="dateItem in floorRooms"
        :key="dateItem.Date || dateItem.date"
        :type="activeDate === (dateItem.Date || dateItem.date) ? 'primary' : ''"
        @click="handleDateChange(dateItem.Date || dateItem.date)"
        class="date-btn"
      >
        {{ formatDateWithWeek(dateItem.Date || dateItem.date) }}
      </el-button>
    </el-button-group>
  </div>

  <!-- 信息栏：入住日期、房型、已选择数量、每天需选数 -->
  <div class="info-bar">
    <div class="info-left">
      <span class="info-item">
        <el-icon><Calendar /></el-icon>
        入住日期: {{ formatDateWithWeek(activeDate) }}
      </span>
      <span class="info-item">
        <el-icon><House /></el-icon>
        房型: {{ currentRoomType }}
      </span>
      <span class="info-item">
        <el-icon><Clock /></el-icon>
        预订天数: {{ daysDiff }} 天
      </span>
      <span class="info-item">
        <el-icon><List /></el-icon>
        每天需选: {{ dailyRequiredCount }} 间
      </span>
    </div>
    <div class="info-right">
      <el-icon><Check /></el-icon>
      已选择总数: {{ totalSelectedCount }}/{{ totalRoomNeeded }}
    </div>
  </div>

  <!-- 楼层房间展示区域 -->
  <div class="room-floor-container">
    <el-skeleton v-if="loading" :rows="5" animated />
    <div v-else-if="!floorRooms || floorRooms.length === 0" class="no-data">
      <el-empty description="暂无房间数据"></el-empty>
    </div>
    <div v-else>
      <template v-if="currentDateData">
        <div v-if="(currentDateData.RoomViews || currentDateData.roomViews || []).length === 0" class="no-room-data">
          <el-empty description="该日期暂无房间数据" :image-size="80"></el-empty>
        </div>
        <div v-else>
          <div
            v-for="(floorItem, floorIndex) in (currentDateData.RoomViews || currentDateData.roomViews || [])"
            :key="`floor-${floorIndex}`"
            class="floor-section"
          >
            <div class="floor-header">
              <div class="floor-title">{{ floorItem.floor || floorItem.Floor }}楼</div>
              <div class="status-legend">
                <span class="legend-item"><span class="legend-dot green"></span> 空闲</span>
                <span class="legend-item"><span class="legend-dot orange"></span> 预约中</span>
                <span class="legend-item"><span class="legend-dot red"></span> 入住中</span>
                <span class="legend-item"><span class="legend-dot gray"></span> 已退房</span>
              </div>
            </div>
            <div class="room-list">
              <div
                v-for="(room, roomIndex) in (floorItem.roomDetViews || floorItem.RoomDetViews || [])"
                :key="`room-${activeDate}-${floorIndex}-${roomIndex}-${room.no || room.No}`"
                class="room-card"
                :class="{ 'selected': isRoomSelected(activeDate, room) }"
                @click="toggleRoomSelection(activeDate, room)"
              >
                <div class="room-number">{{ room.no || room.No }}</div>
                <div class="room-type">
                  {{ room.roomDto?.roomShapeFormat || room.RoomDto?.RoomShapeFormat || '大床房' }}
                </div>
                <div class="room-status" :class="getStatusClass(room.roomStatus || room.RoomStatus)">
                  {{ getRoomStatusText(room.roomStatus || room.RoomStatus) || '空闲' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>

  <div class="bottom-actions">
    <el-button size="large" @click="handleCancel">取消</el-button>
    <el-button type="primary" size="large" @click="handleConfirm">
      确认选择 ({{ totalSelectedCount }}/{{ totalRoomNeeded }})
    </el-button>
  </div>
</template>

<script setup>
import { Post } from '@/api/http';
import { ref, computed, onBeforeMount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCommonStore } from "@/store";
import { ElMessage, ElEmpty, ElSkeleton, ElButton, ElButtonGroup } from 'element-plus';
import { ArrowLeft, Calendar, House, Check, Clock, List } from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const commonStore = useCommonStore();

const loading = ref(true);
const floorRooms = ref([]);
const activeDate = ref('');
const selectedRooms = ref([]);
const currentRoomType = ref('城际大床房');
const beginDate = ref('');
const endDate = ref('');
const dailyRequiredCount = ref(1); // ✅ 先默认1，后面从订单获取真实值
const appointId = ref('');
const appointInfo = ref({}); // ✅ 新增：存储订单详情

const Token = computed(() => commonStore.Token);

const currentDateData = computed(() => {
  if (!activeDate.value || !floorRooms.value.length) return null;
  return floorRooms.value.find(item => (item.Date || item.date) === activeDate.value);
});

const totalSelectedCount = computed(() => selectedRooms.value.length);

const daysDiff = computed(() => {
  if (!beginDate.value || !endDate.value) return 0;
  const begin = new Date(beginDate.value);
  const end = new Date(endDate.value);
  const diffTime = end.getTime() - begin.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays > 0 ? diffDays : 1;
});

const totalRoomNeeded = computed(() => daysDiff.value * dailyRequiredCount.value);

const selectedCountForDate = (date) => {
  return selectedRooms.value.filter(item => item.date === date).length;
};

onBeforeMount(async () => {
  // 1. 先获取订单ID
  if (route.query.appointId) {
    appointId.value = route.query.appointId;
  } else if (route.query.AppointId) {
    appointId.value = route.query.AppointId;
  } else {
    ElMessage.error('缺少订单ID，无法分配房间');
    return;
  }

  // ✅ 2. 关键修复：先获取订单详情，得到真实的预订数量和日期
  await getAppointDetail();

  // 3. 再加载房间数据
  await GetAllRoomViewApi();
});

// ✅ 新增：获取订单详情（核心修复）
const getAppointDetail = async () => {
  try {
    const response = await Post(`/Appoint/Get`, {
      Id: Number(appointId.value) // ✅ 严格匹配@JsonProperty("Id")
    });

    if (response && response.Success === true) {
      appointInfo.value = response.Data;
      
      // 从订单获取真实的每天预订数量
      dailyRequiredCount.value = appointInfo.value.qty || appointInfo.value.Qty || 1;
      
      // 从订单获取真实的入住和离店日期
      beginDate.value = appointInfo.value.beginAppointTime || appointInfo.value.BeginAppointTime;
      endDate.value = appointInfo.value.endAppointTime || appointInfo.value.EndAppointTime;
      activeDate.value = beginDate.value;
      
      // 从订单获取房型名称
      currentRoomType.value = appointInfo.value.roomDto?.title || appointInfo.value.RoomDto?.Title || '大床房';
      
      ElMessage.success(`订单信息加载成功，每天需要分配 ${dailyRequiredCount.value} 间房`);
    } else {
      ElMessage.error(response?.Msg || '获取订单详情失败');
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败，请刷新页面重试');
  }
};

const formatDateWithWeek = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const month = date.getMonth() + 1;
  const day = date.getDate();
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  const week = weekDays[date.getDay()];
  return `${month}月${day}日(${week})`;
};

const getRoomStatusText = (statusCode) => {
  const statusMap = { 1: '空闲', 2: '预约中', 3: '入住中', 4: '已退房' };
  return statusMap[statusCode] || '空闲';
};

const getStatusClass = (statusCode) => {
  const statusMap = {
    1: 'status-free',
    2: 'status-booked',
    3: 'status-in',
    4: 'status-checkout',
  };
  return statusMap[statusCode] || 'status-free';
};

const isRoomSelected = (date, room) => {
  const roomDetId = room.roomDetId || room.RoomDetId;
  if (!roomDetId) return false;
  return selectedRooms.value.some(item => item.date === date && item.roomDetId === roomDetId);
};

const toggleRoomSelection = (date, room) => {
  const roomDetId = room.roomDetId || room.RoomDetId;
  const roomNo = room.no || room.No;
  
  if (!roomDetId) {
    ElMessage.error('房间ID缺失，无法选中');
    return;
  }

  const isSelected = isRoomSelected(date, room);
  if (isSelected) {
    const index = selectedRooms.value.findIndex(item => item.date === date && item.roomDetId === roomDetId);
    if (index !== -1) {
      selectedRooms.value.splice(index, 1);
      ElMessage.info(`取消选中 ${roomNo} (${formatDateWithWeek(date)})`);
    }
  } else {
    const currentCount = selectedCountForDate(date);
    if (currentCount >= dailyRequiredCount.value) {
      ElMessage.warning(`日期 ${formatDateWithWeek(date)} 已选满 ${dailyRequiredCount.value} 间，无法再选`);
      return;
    }
    
    const status = room.roomStatus || room.RoomStatus;
    if (status === 1) { // 只有空闲状态可以选
      selectedRooms.value.push({
        date,
        roomDetId,
        roomId: room.roomId || room.RoomId,
        roomNo
      });
      ElMessage.info(`选中 ${roomNo} (${formatDateWithWeek(date)})`);
    } else {
      ElMessage.warning(`房间 ${roomNo} 非空闲状态，无法选中`);
    }
  }
};

const handleDateChange = (date) => {
  activeDate.value = date;
};

const handleBack = () => {
  router.back();
};

const handleCancel = () => {
  selectedRooms.value = [];
  ElMessage.info('已取消所有选择');
};

/**
 * 格式化日期为后端需要的 LocalDateTime 格式
 */
const formatToLocalDateTime = (dateStr, isEnd = false) => {
  if (!dateStr) return '';
  const pureDate = dateStr.split('T')[0] || dateStr.split(' ')[0] || dateStr;
  let targetDate = new Date(pureDate);
  if (isEnd) {
    targetDate.setDate(targetDate.getDate() + 1);
  }
  const year = targetDate.getFullYear();
  const month = String(targetDate.getMonth() + 1).padStart(2, '0');
  const day = String(targetDate.getDate()).padStart(2, '0');
  return `${year}-${month}-${day} 12:00:00`;
};

const handleConfirm = async () => {
  if (!appointId.value) {
    ElMessage.error('订单ID缺失');
    return;
  }

  // 校验每天都选够了数量
  const allDates = floorRooms.value.map(item => item.Date || item.date);
  let hasError = false;
  for (const date of allDates) {
    const selected = selectedCountForDate(date);
    if (selected !== dailyRequiredCount.value) {
      hasError = true;
      ElMessage.warning(`日期 ${formatDateWithWeek(date)} 需要选 ${dailyRequiredCount.value} 间，当前选了 ${selected} 间`);
    }
  }
  if (hasError) return;

  if (totalSelectedCount.value !== totalRoomNeeded.value) {
    ElMessage.warning(`总共需要 ${totalRoomNeeded.value} 间，当前选了 ${totalSelectedCount.value} 间`);
    return;
  }

  try {
    // 构建提交数据（严格匹配@JsonProperty）
    const submitData = selectedRooms.value.map(item => ({
      AppointId: Number(appointId.value),
      RoomDetId: item.roomDetId,
      RoomId: item.roomId,
      BeginActiveTime: formatToLocalDateTime(item.date),
      EndActiveTime: formatToLocalDateTime(item.date, true),
      RoomStatus: 3 // ✅ 直接设置为已入住状态
    }));

    const response = await Post(`/RoomMatch/BatchCreateOrEdit`, submitData);

    if (response && response.Success === true) {
      ElMessage.success(`成功分配 ${totalSelectedCount.value} 间房！`);
      setTimeout(() => router.back(), 1500);
    } else {
      ElMessage.error(response?.Msg || '提交失败');
    }
  } catch (error) {
    console.error('提交失败:', error);
    ElMessage.error(error.response?.data?.Msg || '网络异常');
  }
};

const GetAllRoomViewApi = async () => {
  try {
    loading.value = true;
    const { Data } = await Post(`/RoomDet/GetAllRoomView`, {
      HotelId: appointInfo.value.hotelId || appointInfo.value.HotelId || route.query.HotelId,
      BeginDate: beginDate.value,
      EndDate: endDate.value
    });

    floorRooms.value = (Data || []).map(item => ({
      ...item,
      RoomViews: item.RoomViews || item.roomViews || []
    }));

    if (floorRooms.value.length > 0 && !activeDate.value) {
      activeDate.value = floorRooms.value[0].Date || floorRooms.value[0].date;
    }
  } catch (error) {
    console.error('加载房间失败:', error);
    ElMessage.error('加载房间数据失败');
    floorRooms.value = [];
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  .breadcrumb {
    font-size: 16px;
    color: #606266;
    .separator {
      margin: 0 8px;
      color: #c0c4cc;
    }
  }
  .back-btn {
    font-size: 16px;
    color: #409eff;
  }
}

.date-selector {
  padding: 16px 20px;
  background: #fff;
  .date-btn {
    border-radius: 4px !important;
    &:first-child {
      border-top-left-radius: 4px !important;
      border-bottom-left-radius: 4px !important;
    }
    &:last-child {
      border-top-right-radius: 4px !important;
      border-bottom-right-radius: 4px !important;
    }
  }
}

.info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  .info-left {
    display: flex;
    gap: 32px;
    .info-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      color: #606266;
    }
  }
  .info-right {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #606266;
    .el-icon {
      color: #67c23a;
    }
  }
}

.room-floor-container {
  padding: 20px;
  background: #fff;
  .no-data, .no-room-data {
    padding: 40px 0;
    text-align: center;
  }
}

.floor-section {
  margin-bottom: 32px;
  .floor-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    .floor-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
    .status-legend {
      display: flex;
      gap: 16px;
      .legend-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 14px;
        color: #606266;
        .legend-dot {
          width: 10px;
          height: 10px;
          border-radius: 50%;
          display: inline-block;
          &.green { background: #67c23a; }
          &.orange { background: #e6a23c; }
          &.red { background: #f56c6c; }
          &.gray { background: #909399; }
        }
      }
    }
  }

  .room-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    .room-card {
      width: 120px;
      padding: 12px 8px;
      border: 1px solid #e4e7ed;
      border-radius: 6px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      background: #fff;
      &.selected {
        border: 2px solid #409eff;
        background-color: #f5f9ff;
      }
      &:hover:not(.selected) {
        border-color: #c0c4cc;
        background-color: #f8f9fa;
      }

      .room-number {
        font-size: 24px;
        font-weight: 700;
        color: #409eff;
        margin-bottom: 8px;
      }

      .room-type {
        font-size: 14px;
        color: #606266;
        margin-bottom: 8px;
      }

      .room-status {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;
        display: inline-block;
        &.status-free {
          color: #67c23a;
          background: #f0f9eb;
        }
        &.status-booked {
          color: #e6a23c;
          background: #fdf6ec;
        }
        &.status-in {
          color: #f56c6c;
          background: #fef0f0;
        }
        &.status-checkout {
          color: #909399;
          background: #f4f4f5;
        }
      }
    }
  }
}

.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
}

@media (max-width: 768px) {
  .info-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    .info-left {
      flex-direction: column;
      gap: 8px;
    }
  }
  .floor-section .room-list .room-card {
    width: calc(50% - 8px);
  }
}
@media (max-width: 480px) {
  .floor-section .room-list .room-card {
    width: 100%;
  }
}
</style>