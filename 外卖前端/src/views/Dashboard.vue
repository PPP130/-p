<template>
  <div class="page-container">
    <!-- 今日经营数据 -->
    <el-card class="section-card" shadow="never">
      <template #header>
        <span class="section-title">今日经营数据</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="4" v-for="item in businessCards" :key="item.label">
          <div class="stat-card">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 菜品总览 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">菜品总览</span>
          </template>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="overview-item">
                <div class="overview-value sold">{{ dishData.sold }}</div>
                <div class="overview-label">已启售</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="overview-item">
                <div class="overview-value stopped">{{ dishData.discontinued }}</div>
                <div class="overview-label">已停售</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 套餐总览 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">套餐总览</span>
          </template>
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="overview-item">
                <div class="overview-value sold">{{ setmealData.sold }}</div>
                <div class="overview-label">已启售</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="overview-item">
                <div class="overview-value stopped">{{ setmealData.discontinued }}</div>
                <div class="overview-label">已停售</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 订单总览 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="section-title">订单总览</span>
          </template>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in orderCards" :key="item.label">
              <div class="overview-item small">
                <div class="overview-value">{{ item.value }}</div>
                <div class="overview-label">{{ item.label }}</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getBusinessData, getOverviewDishes, getOverviewSetmeals, getOverviewOrders } from '@/api/workspace'

const businessData = ref<any>({})
const dishData = ref<any>({ sold: 0, discontinued: 0 })
const setmealData = ref<any>({ sold: 0, discontinued: 0 })
const orderData = ref<any>({})

const businessCards = computed(() => [
  { label: '营业额', value: `¥${businessData.value.turnover ?? 0}` },
  { label: '有效订单数', value: businessData.value.validOrderCount ?? 0 },
  { label: '订单完成率', value: `${((businessData.value.orderCompletionRate ?? 0) * 100).toFixed(1)}%` },
  { label: '平均客单价', value: `¥${businessData.value.unitPrice ?? 0}` },
  { label: '新增用户数', value: businessData.value.newUsers ?? 0 },
])

const orderCards = computed(() => [
  { label: '待接单', value: orderData.value.waitingOrders ?? 0 },
  { label: '待派送', value: orderData.value.deliveredOrders ?? 0 },
  { label: '已完成', value: orderData.value.completedOrders ?? 0 },
  { label: '已取消', value: orderData.value.cancelledOrders ?? 0 },
  { label: '全部', value: orderData.value.allOrders ?? 0 },
])

onMounted(async () => {
  try {
    const [business, dishes, setmeals, orders]: any[] = await Promise.all([
      getBusinessData(),
      getOverviewDishes(),
      getOverviewSetmeals(),
      getOverviewOrders(),
    ])
    businessData.value = business.data || {}
    dishData.value = dishes.data || {}
    setmealData.value = setmeals.data || {}
    orderData.value = orders.data || {}
  } catch {
    // 静默处理
  }
})
</script>

<style scoped>
.section-card {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.stat-card {
  text-align: center;
  padding: 16px 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.overview-item {
  text-align: center;
  padding: 20px 0;
}

.overview-item.small {
  padding: 12px 0;
}

.overview-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.overview-item.small .overview-value {
  font-size: 20px;
}

.overview-value.sold {
  color: #67c23a;
}

.overview-value.stopped {
  color: #999;
}

.overview-label {
  font-size: 13px;
  color: #999;
}
</style>
