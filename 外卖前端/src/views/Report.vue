<template>
  <div class="page-container">
    <!-- 日期范围选择 -->
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单统计概览 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">总订单数</div>
            <div class="stat-value">{{ orderStats.totalOrderCount ?? 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">有效订单数</div>
            <div class="stat-value primary">{{ orderStats.validOrderCount ?? 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-label">订单完成率</div>
            <div class="stat-value success">{{ orderStats.orderCompletionRate ?? '0%' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 营业额统计 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">营业额统计</span>
          </template>
          <div ref="turnoverChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">订单统计</span>
          </template>
          <div ref="orderChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">用户统计</span>
          </template>
          <div ref="userChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">销量Top10</span>
          </template>
          <div ref="top10ChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  getTurnoverStatistics,
  getOrdersStatistics,
  getUsersStatistics,
  getTop10,
  exportReport,
} from '@/api/report'

// 默认最近30天
const now = new Date()
const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
const dateRange = ref<string[]>([
  thirtyDaysAgo.toISOString().split('T')[0],
  now.toISOString().split('T')[0],
])

const orderStats = ref<any>({})
const turnoverChartRef = ref<HTMLElement>()
const orderChartRef = ref<HTMLElement>()
const userChartRef = ref<HTMLElement>()
const top10ChartRef = ref<HTMLElement>()

let turnoverChart: echarts.ECharts | null = null
let orderChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null
let top10Chart: echarts.ECharts | null = null

function initCharts() {
  if (turnoverChartRef.value) turnoverChart = echarts.init(turnoverChartRef.value)
  if (orderChartRef.value) orderChart = echarts.init(orderChartRef.value)
  if (userChartRef.value) userChart = echarts.init(userChartRef.value)
  if (top10ChartRef.value) top10Chart = echarts.init(top10ChartRef.value)
}

async function fetchAllData() {
  if (!dateRange.value) return
  const [begin, end] = dateRange.value

  try {
    const [turnover, orders, users, top10]: any[] = await Promise.all([
      getTurnoverStatistics({ begin, end }),
      getOrdersStatistics({ begin, end }),
      getUsersStatistics({ begin, end }),
      getTop10({ begin, end }),
    ])

    // 营业额统计
    if (turnoverChart) {
      const dateList = turnover.data.dateList.split(',')
      const turnoverList = turnover.data.turnoverList.split(',').map(Number)
      turnoverChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: dateList, axisLabel: { rotate: 45, fontSize: 10 } },
        yAxis: { type: 'value', name: '营业额(元)' },
        series: [{ name: '营业额', type: 'line', data: turnoverList, smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409eff' } }],
        grid: { left: 60, right: 20, bottom: 60, top: 30 },
      })
    }

    // 订单统计
    if (orderChart) {
      const dateList = orders.data.dateList.split(',')
      orderStats.value = {
        totalOrderCount: orders.data.totalOrderCount,
        validOrderCount: orders.data.validOrderCount,
        orderCompletionRate: orders.data.orderCompletionRate,
      }
      orderChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['总订单数', '有效订单数'] },
        xAxis: { type: 'category', data: dateList, axisLabel: { rotate: 45, fontSize: 10 } },
        yAxis: { type: 'value' },
        series: [
          { name: '总订单数', type: 'bar', data: orders.data.orderCountList.split(',').map(Number), itemStyle: { color: '#409eff' } },
          { name: '有效订单数', type: 'bar', data: orders.data.validOrderCountList.split(',').map(Number), itemStyle: { color: '#67c23a' } },
        ],
        grid: { left: 60, right: 20, bottom: 60, top: 40 },
      })
    }

    // 用户统计
    if (userChart) {
      const dateList = users.data.dateList.split(',')
      userChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['总用户数', '新增用户数'] },
        xAxis: { type: 'category', data: dateList, axisLabel: { rotate: 45, fontSize: 10 } },
        yAxis: { type: 'value' },
        series: [
          { name: '总用户数', type: 'line', data: users.data.totalUserList.split(',').map(Number), smooth: true, itemStyle: { color: '#409eff' } },
          { name: '新增用户数', type: 'bar', data: users.data.newUserList.split(',').map(Number), itemStyle: { color: '#e6a23c' } },
        ],
        grid: { left: 60, right: 20, bottom: 60, top: 40 },
      })
    }

    // 销量Top10
    if (top10Chart) {
      top10Chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: top10.data.nameList.split(','), axisLabel: { rotate: 30, fontSize: 10 } },
        yAxis: { type: 'value', name: '销量' },
        series: [{ name: '销量', type: 'bar', data: top10.data.numberList.split(',').map(Number), itemStyle: { color: '#67c23a' } }],
        grid: { left: 60, right: 20, bottom: 60, top: 30 },
      })
    }
  } catch {}
}

function handleSearch() {
  fetchAllData()
}

async function handleExport() {
  try {
    const res: any = await exportReport()
    const url = window.URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    a.download = '报表.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch {}
}

function handleResize() {
  turnoverChart?.resize()
  orderChart?.resize()
  userChart?.resize()
  top10Chart?.resize()
}

onMounted(async () => {
  await nextTick()
  initCharts()
  fetchAllData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  turnoverChart?.dispose()
  orderChart?.dispose()
  userChart?.dispose()
  top10Chart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}
</style>
