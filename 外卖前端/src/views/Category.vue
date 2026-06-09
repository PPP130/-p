<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="分类名称">
          <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable>
            <el-option label="菜品分类" :value="1" />
            <el-option label="套餐分类" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="header-bar">
      <el-button type="primary" @click="handleAdd">+ 新增分类</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe>
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="type" label="分类类型" width="120">
          <template #default="{ row }">{{ row.type === 1 ? '菜品分类' : '套餐分类' }}</template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val: boolean) => handleStatusChange(row.id, val ? 1 : 0)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="450px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">菜品分类</el-radio>
            <el-radio :value="2">套餐分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCategoryPage,
  addCategory,
  updateCategory,
  deleteCategory,
  enableCategory,
} from '@/api/category'
import type { Category } from '@/api/category'

const searchForm = reactive({ name: '', type: undefined as number | undefined })
const tableData = ref<Category[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<Category>({
  type: 1,
  name: '',
  sort: 0,
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
}

async function fetchData() {
  try {
    const res: any = await getCategoryPage({
      name: searchForm.name,
      type: searchForm.type,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.type = undefined
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { type: 1, name: '', sort: 0 })
  dialogVisible.value = true
}

function handleEdit(row: Category) {
  isEdit.value = true
  Object.assign(form, { id: row.id, type: row.type, name: row.name, sort: row.sort })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCategory(form)
      ElMessage.success('编辑成功')
    } else {
      await addCategory(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {} finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: Category) {
  await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
  try {
    await deleteCategory(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleStatusChange(id: number, status: number) {
  try {
    await enableCategory(status, id)
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>
