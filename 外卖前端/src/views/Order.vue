<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.number" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 订单统计 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <span class="stat-label">待接单</span>
            <span class="stat-value warning">{{ statistics.waitingOrders ?? 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <span class="stat-label">待派送</span>
            <span class="stat-value primary">{{ statistics.deliveryInProgress ?? 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <span class="stat-label">已完成</span>
            <span class="stat-value success">{{ statistics.completed ?? 0 }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="number" label="订单号" width="200" />
        <el-table-column prop="consignee" label="收货人" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="订单金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 2"
              type="success"
              link
              @click="handleConfirm(row)"
            >接单</el-button>
            <el-button
              v-if="row.status === 3"
              type="primary"
              link
              @click="handleDeliver(row)"
            >派送</el-button>
            <el-button
              v-if="row.status === 4"
              type="success"
              link
              @click="handleComplete(row)"
            >完成</el-button>
            <el-button
              v-if="row.status === 2"
              type="danger"
              link
              @click="handleReject(row)"
            >拒单</el-button>
            <el-button
              v-if="[2, 3].includes(row.status)"
              type="warning"
              link
              @click="handleCancel(row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="650px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单号">{{ currentOrder.number }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusLabel(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ currentOrder.consignee }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentOrder.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentOrder.orderTime }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentOrder.payMethod === 1 ? '微信支付' : '支付宝' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>菜品明细</el-divider>
      <el-table :data="currentOrder?.orderDetailList || []" border size="small">
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="copies" label="份数" width="80" />
      </el-table>
    </el-dialog>

    <!-- 拒单/取消原因弹窗 -->
    <el-dialog v-model="reasonVisible" :title="reasonType === 'reject' ? '拒单原因' : '取消原因'" width="450px">
      <el-input v-model="reasonText" type="textarea" :rows="4" placeholder="请输入原因" />
      <template #footer>
        <el-button @click="reasonVisible = false">取消</el-button>
        <el-button type="primary" :loading="reasonLoading" @click="submitReason">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderPage,
  getOrderDetails,
  confirmOrder,
  rejectOrder,
  cancelOrder,
  deliverOrder,
  completeOrder,
  getOrderStatistics,
} from '@/api/order'
import type { OrderVO } from '@/api/order'

const statusOptions = [
  { label: '待付款', value: 1 },
  { label: '待接单', value: 2 },
  { label: '已接单', value: 3 },
  { label: '派送中', value: 4 },
  { label: '已完成', value: 5 },
  { label: '已取消', value: 6 },
]

const searchForm = reactive({
  number: '',
  phone: '',
  status: undefined as number | undefined,
  dateRange: null as string[] | null,
})

const tableData = ref<OrderVO[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const statistics = ref<any>({})
const detailVisible = ref(false)
const currentOrder = ref<OrderVO | null>(null)
const reasonVisible = ref(false)
const reasonType = ref<'reject' | 'cancel'>('reject')
const reasonText = ref('')
const reasonLoading = ref(false)
const reasonOrderId = ref(0)

function getStatusLabel(status: number) {
  return statusOptions.find((s) => s.value === status)?.label || '未知'
}

function getStatusType(status: number) {
  const map: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: '',
    4: 'primary',
    5: 'success',
    6: 'danger',
  }
  return map[status] || 'info'
}

async function fetchData() {
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      number: searchForm.number,
      phone: searchForm.phone,
      status: searchForm.status,
    }
    if (searchForm.dateRange) {
      params.beginTime = searchForm.dateRange[0]
      params.endTime = searchForm.dateRange[1]
    }
    const res: any = await getOrderPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

async function fetchStatistics() {
  try {
    const res: any = await getOrderStatistics()
    statistics.value = res.data || {}
  } catch {}
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  Object.assign(searchForm, { number: '', phone: '', status: undefined, dateRange: null })
  handleSearch()
}

async function handleViewDetail(row: OrderVO) {
  try {
    const res: any = await getOrderDetails(row.id)
    currentOrder.value = res.data
    detailVisible.value = true
  } catch {}
}

async function handleConfirm(row: OrderVO) {
  await ElMessageBox.confirm('确认接单？', '提示')
  try {
    await confirmOrder({ id: row.id, status: 3 })
    ElMessage.success('接单成功')
    fetchData()
    fetchStatistics()
  } catch {}
}

async function handleDeliver(row: OrderVO) {
  await ElMessageBox.confirm('确认派送？', '提示')
  try {
    await deliverOrder(row.id)
    ElMessage.success('派送成功')
    fetchData()
    fetchStatistics()
  } catch {}
}

async function handleComplete(row: OrderVO) {
  await ElMessageBox.confirm('确认完成？', '提示')
  try {
    await completeOrder(row.id)
    ElMessage.success('订单已完成')
    fetchData()
    fetchStatistics()
  } catch {}
}

function handleReject(row: OrderVO) {
  reasonType.value = 'reject'
  reasonOrderId.value = row.id
  reasonText.value = ''
  reasonVisible.value = true
}

function handleCancel(row: OrderVO) {
  reasonType.value = 'cancel'
  reasonOrderId.value = row.id
  reasonText.value = ''
  reasonVisible.value = true
}

async function submitReason() {
  if (!reasonText.value.trim()) {
    ElMessage.warning('请输入原因')
    return
  }
  reasonLoading.value = true
  try {
    if (reasonType.value === 'reject') {
      await rejectOrder({ id: reasonOrderId.value, rejectionReason: reasonText.value })
      ElMessage.success('已拒单')
    } else {
      await cancelOrder({ id: reasonOrderId.value, cancelReason: reasonText.value })
      ElMessage.success('已取消')
    }
    reasonVisible.value = false
    fetchData()
    fetchStatistics()
  } catch {} finally {
    reasonLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchStatistics()
})
</script>

<style scoped>
.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}
</style>
