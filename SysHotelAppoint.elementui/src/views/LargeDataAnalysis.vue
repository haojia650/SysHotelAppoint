<template>
  <!-- 全屏可视化容器 -->
  <div class="data-screen-container">
    <dv-full-screen-container class="full-container">
      <div class="title-wrapper">
        <dv-title-box class="title-box">酒店预订系统数据分析大屏</dv-title-box>
      </div>

      <!-- 第一行核心数据 -->
      <div class="data-card-row">
        <dv-border-box1 class="data-card" color="#1381e0,#67cdf7">
          <div class="card-content">
            <p class="card-label">总预约数</p>
            <div class="card-num">{{ totalBooking }}</div>
          </div>
        </dv-border-box1>

        <dv-border-box1 class="data-card" color="#13c391,#67e0c3">
          <div class="card-content">
            <p class="card-label">总收入(元)</p>
            <div class="card-num">{{ totalIncome.toFixed(2) }}</div>
          </div>
        </dv-border-box1>

        <dv-border-box1 class="data-card" color="#f0a829,#f7d167">
          <div class="card-content">
            <p class="card-label">平均入住天数</p>
            <div class="card-num">{{ avgStayDays }}</div>
          </div>
        </dv-border-box1>

        <dv-border-box1 class="data-card" color="#e63946,#f78181">
          <div class="card-content">
            <p class="card-label">完成预约率</p>
            <div class="card-num">{{ completionRate.toFixed(2) }}%</div>
          </div>
        </dv-border-box1>
      </div>

      <!-- 图表区域 -->
      <div class="chart-row">
        <dv-border-box3 class="chart-box" color="#1381e0,#67cdf7">
          <p class="chart-title">
            <span class="title-line"></span>预约状态分布
          </p>
          <div ref="bookingStatusChart" class="chart"></div>
        </dv-border-box3>

        <dv-border-box3 class="chart-box" color="#e63946,#f78181">
          <p class="chart-title">
            <span class="title-line"></span>月度预约与收入趋势
          </p>
          <div ref="monthlyTrendChart" class="chart"></div>
        </dv-border-box3>
      </div>

      <div class="chart-row-three">
        <dv-border-box3 class="chart-box" color="#13c391,#67e0c3">
          <p class="chart-title">
            <span class="title-line"></span>预约时段分布
          </p>
          <div ref="timeSlotChart" class="chart"></div>
        </dv-border-box3>

        <dv-border-box3 class="chart-box" color="#f0a829,#f7d167">
          <p class="chart-title">
            <span class="title-line"></span>客户分析
          </p>
          <div ref="customerAnalysisChart" class="chart"></div>
        </dv-border-box3>

        <dv-border-box3 class="chart-box" color="#8e44ad,#c391e6">
          <p class="chart-title">
            <span class="title-line"></span>预订提前时间分析
          </p>
          <div ref="advanceBookingChart" class="chart"></div>
        </dv-border-box3>
      </div>
    </dv-full-screen-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { Post } from '@/api/http';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';

// 核心数据（纯数字响应式）
const totalBooking = ref(0);
const totalIncome = ref(0);
const avgStayDays = ref(0);
const completionRate = ref(0);

// 图表DOM
const bookingStatusChart = ref(null);
const monthlyTrendChart = ref(null);
const timeSlotChart = ref(null);
const customerAnalysisChart = ref(null);
const advanceBookingChart = ref(null);

let chartInstances = [];

// 工具函数
const objToChartData = (obj) => {
  if (!obj) return [];
  return Object.entries(obj).map(([name, value]) => ({ name, value: Number(value) }));
};
const formatMonthData = (monthObj) => {
  let data = [];
  for (let i = 1; i <= 12; i++) data.push(monthObj[i] || 0);
  return data;
};

// 初始化图表
const initCharts = (data = {}) => {
  chartInstances.forEach(item => item.dispose());
  chartInstances = [];
  const { bookingStatusStats, monthlyBookingCount, monthlyIncomeCount, timeSlotStats, customerStats, advanceBookingStats } = data;

  // 1.预约状态
  const bookingStatusIns = echarts.init(bookingStatusChart.value);
  bookingStatusIns.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 单 ({d}%)',
      backgroundColor: 'rgba(0,0,0,0.75)',
      textStyle: { color: '#fff' },
      padding: [8,12]
    },
    legend: {
      bottom: 5,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#fff', fontSize: 12 }
    },
    series: [{
      name: '预约状态', type: 'pie', radius: ['35%', '65%'], center: ['50%', '45%'],
      data: [
        { name: '待确定', value: bookingStatusStats?.待确定 || 0 },
        { name: '待入住', value: bookingStatusStats?.待入住 || 0 },
        { name: '已完成', value: bookingStatusStats?.已完成 || 0 },
        { name: '用户取消', value: bookingStatusStats?.用户取消 || 0 },
        { name: '酒店取消', value: bookingStatusStats?.酒店取消 || 0 },
        { name: '系统取消', value: bookingStatusStats?.系统取消 || 0 }
      ],
      color: ['#4A90E2','#8BC34A','#FFCA28','#EF5350','#64B5F6','#26A69A'],
      label: { color: '#fff', fontSize: 12, formatter: '{b}: {d}%' },
      labelLine: { lineStyle: { color: 'rgba(255,255,255,0.4)' } },
      animationDuration: 1200
    }]
  });
  chartInstances.push(bookingStatusIns);

  // 2.月度趋势
  const monthlyTrendIns = echarts.init(monthlyTrendChart.value);
  monthlyTrendIns.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.75)', textStyle: { color: '#fff' } },
    legend: { textStyle: { color: '#fff' }, itemGap: 20 },
    grid: { left: '3%', right: '4%', bottom: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
      axisLabel: { color: '#fff' },
      axisTick: { show: false }
    },
    yAxis: [
      {
        type: 'value', name: '预约数',
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
        axisLabel: { color: '#fff' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
      },
      {
        type: 'value', name: '收入(元)',
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
        axisLabel: { color: '#fff' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '预约数量', type: 'bar', barWidth: '40%',
        data: formatMonthData(monthlyBookingCount),
        itemStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#4A90E2'},{offset:1,color:'#2196F3'}]) }
      },
      {
        name: '收入(元)', type: 'line', yAxisIndex: 1,
        data: formatMonthData(monthlyIncomeCount),
        symbol: 'circle', symbolSize: 6,
        lineStyle: { color: '#FFCA28', width: 3 },
        itemStyle: { color: '#FF9800' }
      }
    ]
  });
  chartInstances.push(monthlyTrendIns);

  // 3.时段分布
  const timeSlotIns = echarts.init(timeSlotChart.value);
  timeSlotIns.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: 'rgba(0,0,0,0.75)', textStyle: { color: '#fff' } },
    legend: { bottom: 5, textStyle: { color: '#fff' } },
    series: [{
      name: '预约时段', type: 'pie', radius: ['35%', '65%'], center: ['50%', '45%'],
      data: objToChartData(timeSlotStats),
      color: ['#4A90E2','#8BC34A','#FFCA28','#EF5350'],
      label: { color: '#fff', formatter: '{b}: {d}%' }
    }]
  });
  chartInstances.push(timeSlotIns);

  // 4.客户分析
  const customerIns = echarts.init(customerAnalysisChart.value);
  customerIns.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.75)', textStyle: { color: '#fff' } },
    xAxis: {
      type: 'category', data: ['新客户','回头客'],
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
      axisLabel: { color: '#fff', fontSize: 14 },
      axisTick: { show: false }
    },
    yAxis: {
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
    },
    series: [{
      type: 'bar', barWidth: '50%',
      data: [customerStats?.新客户||0, customerStats?.回头客||0],
      itemStyle: {
        color: (p) => p.dataIndex === 0
          ? new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#8BC34A'},{offset:1,color:'#4CAF50'}])
          : new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#FFCA28'},{offset:1,color:'#FF9800'}])
      },
      label: { show: true, position: 'top', color: '#fff', fontSize: 14 }
    }]
  });
  chartInstances.push(customerIns);

  // 5.提前预订
  const advanceBookingIns = echarts.init(advanceBookingChart.value);
  advanceBookingIns.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.75)', textStyle: { color: '#fff' } },
    xAxis: {
      type: 'category',
      data: ['当天预订','提前1-3天','提前4-7天','提前8-14天','提前15-30天','提前30天以上'],
      axisLabel: { color: '#fff', rotate: 30, fontSize: 11 },
      axisLine: { lineStyle: { color: 'rgba(255,255,255,0.25)' } },
      axisTick: { show: false }
    },
    yAxis: {
      axisLabel: { color: '#fff' },
      splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } }
    },
    series: [{
      type: 'bar',
      data: objToChartData(advanceBookingStats).map(i=>i.value),
      itemStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#4A90E2'},{offset:1,color:'#2196F3'}]) },
      label: { show: true, position: 'top', color: '#fff' }
    }]
  });
  chartInstances.push(advanceBookingIns);
};

// 核心接口请求
const AdminHotelDataAnalysisApi = async () => {
  try {
    const res = await Post(`/DataAnalysis/SynthesizeStatistics`, {});
    const apiData = res?.Data?.Data;
    const isSuccess = res?.Data?.Success;

    if (isSuccess && apiData) {
      totalBooking.value = Number(apiData.totalBooking) || 0;
      totalIncome.value = Number(apiData.totalIncome) || 0;
      avgStayDays.value = Number(apiData.avgStayDays) || 0;
      completionRate.value = Number(apiData.completionRate) || 0;
      nextTick(() => initCharts(apiData));
    } else {
      ElMessage.warning('加载模拟数据');
      loadMockData();
    }
  } catch (error) {
    ElMessage.error('请求失败，加载模拟数据');
    loadMockData();
  }
};

// 模拟数据
const loadMockData = () => {
  const mockData = {
    totalBooking:9,totalIncome:54.00,avgStayDays:4,completionRate:33.33,
    bookingStatusStats:{待确定:0,待入住:2,已完成:1,用户取消:4,酒店取消:0,系统取消:2},
    monthlyBookingCount:{7:9},monthlyIncomeCount:{7:54},
    timeSlotStats:{"早晨预约(6:00-12:00)":8,"下午预约(12:00-18:00)":0,"晚间预约(18:00-22:00)":0,"夜间预约(22:00-6:00)":1},
    customerStats:{新客户:0,回头客:9},
    advanceBookingStats:{"当天预订":0,"提前1-3天":0,"提前4-7天":0,"提前8-14天":0,"提前15-30天":0,"提前30天以上":9}
  };
  totalBooking.value = mockData.totalBooking;
  totalIncome.value = mockData.totalIncome;
  avgStayDays.value = mockData.avgStayDays;
  completionRate.value = mockData.completionRate;
  nextTick(() => initCharts(mockData));
};

// 自适应
const resizeCharts = () => chartInstances.forEach(c => c.resize());
onMounted(() => { AdminHotelDataAnalysisApi(); window.addEventListener('resize', resizeCharts); });
onUnmounted(() => { window.removeEventListener('resize', resizeCharts); chartInstances.forEach(c => c.dispose()); });
</script>

<style scoped lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: "PingFang SC","Microsoft YaHei","Helvetica Neue",Arial,sans-serif;
}
/* 全屏科技背景 + 径向光晕 */
.data-screen-container {
  width: 100vw;
  height: 100vh;
  background: radial-gradient(circle at 50% 10%, #0a193d 0%, #050d20 60%);
  overflow: hidden;
  padding: 20px;
}
.full-container {
  width: 100% !important;
  height: 100% !important;
}

/* 标题区域美化 */
.title-wrapper {
  margin-bottom: 30px;
  position: relative;
}
.title-box {
  text-align: center;
  font-size: 34px;
  font-weight: bold;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #67f0ff, #1381e0, #f0a829, #ffd600);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 20px rgba(103, 205, 247, 0.4);
  padding: 12px 0;
  position: relative;
  animation: titleGlow 3s ease infinite alternate;
}
/* 标题装饰线 */
.title-box::before,
.title-box::after {
  content: "";
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  height: 2px;
  border-radius: 1px;
}
.title-box::before {
  top: 0;
  width: 400px;
  background: linear-gradient(90deg, transparent, #1381e0, transparent);
}
.title-box::after {
  bottom: 0;
  width: 320px;
  background: linear-gradient(90deg, transparent, #67cdf7, transparent);
}
@keyframes titleGlow {
  0% { filter: brightness(1); }
  100% { filter: brightness(1.2); }
}

/* 数据卡片行 */
.data-card-row {
  display: flex;
  gap: 24px;
  margin-bottom: 30px;
  height: 150px;
}
.data-card {
  flex: 1;
  height: 100%;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 8px;
}
/* 卡片悬浮效果 */
.data-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}
.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 10px;
}
.card-label {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 12px;
  letter-spacing: 1px;
  font-weight: 500;
}
.card-num {
  font-size: 40px;
  font-weight: bold;
  background: linear-gradient(90deg, #ffffff, #67f0ff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 0 10px rgba(103, 205, 247, 0.3);
  letter-spacing: 1px;
}

/* 图表布局 */
.chart-row {
  display: flex;
  gap: 24px;
  margin-bottom: 30px;
  height: 360px;
}
.chart-row-three {
  display: flex;
  gap: 24px;
  height: 330px;
}
.chart-box {
  flex: 1;
  height: 100%;
  position: relative;
  transition: all 0.25s ease;
  border-radius: 8px;
}
.chart-box:hover {
  transform: translateY(-3px);
}

/* 图表标题装饰 */
.chart-title {
  position: absolute;
  top: 20px;
  left: 30px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: 0.5px;
}
.title-line {
  display: inline-block;
  width: 5px;
  height: 20px;
  background: linear-gradient(180deg, #1381e0, #67cdf7);
  border-radius: 3px;
  box-shadow: 0 0 8px rgba(19, 129, 224, 0.4);
}

/* 图表容器 */
.chart {
  width: 100%;
  height: calc(100% - 60px);
  margin-top: 50px;
  padding: 0 15px;
}

/* 隐藏滚动条 */
::-webkit-scrollbar {
  width: 0;
  height: 0;
}
</style>