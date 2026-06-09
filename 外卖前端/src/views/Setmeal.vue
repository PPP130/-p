<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="套餐名称">
          <el-input v-model="searchForm.name" placeholder="请输入套餐名称" clearable />
        </el-form-item>
        <el-form-item label="套餐分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择" clearable>
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="启售" :value="1" />
            <el-option label="停售" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="header-bar">
      <el-button type="primary" @click="handleAdd">+ 新增套餐</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="套餐名称" min-width="120" />
        <el-table-column prop="categoryName" label="套餐分类" width="120" />
        <el-table-column label="套餐价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="套餐图片" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.image"
              :src="row.image"
              :preview-src-list="[row.image]"
              style="width: 60px; height: 60px; border-radius: 4px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column label="售卖状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后操作时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleStatusChange(row.id, row.status === 1 ? 0 : 1)"
            >
              {{ row.status === 1 ? '停售' : '启售' }}
            </el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑套餐' : '新增套餐'"
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="套餐名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入套餐名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="套餐分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="套餐价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="套餐图片">
              <el-upload
                class="image-uploader"
                action="/api/admin/common/upload"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleUploadSuccess"
                name="file"
              >
                <el-image
                  v-if="form.image"
                  :src="form.image"
                  style="width: 120px; height: 120px; border-radius: 4px"
                  fit="cover"
                />
                <el-icon v-else style="font-size: 28px; color: #999"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="套餐描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入套餐描述" />
        </el-form-item>

        <!-- 套餐菜品 -->
        <el-divider content-position="left">套餐菜品</el-divider>
        <el-button type="primary" size="small" @click="showDishSelect" style="margin-bottom: 12px">+ 添加菜品</el-button>

        <el-table :data="form.setmealDishes" border size="small" v-if="form.setmealDishes.length > 0">
          <el-table-column prop="name" label="菜品名称" min-width="120" />
          <el-table-column label="菜品价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="份数" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.copies" :min="1" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeDish($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 菜品选择弹窗 -->
    <el-dialog v-model="dishSelectVisible" title="选择菜品" width="600px" append-to-body>
      <el-form :inline="true" size="small">
        <el-form-item label="菜品分类">
          <el-select v-model="dishSearchCategoryId" placeholder="全部" clearable>
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品名称">
          <el-input v-model="dishSearchName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchDishList">搜索</el-button>
        </el-form-item>
      </el-form>
      <el-table
        :data="dishList"
        border
        size="small"
        @selection-change="handleDishSelectionChange"
        max-height="300"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="dishSelectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDishSelect">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getSetmealPage,
  addSetmeal,
  updateSetmeal,
  getSetmealById,
  deleteSetmeal,
  enableSetmeal,
} from '@/api/setmeal'
import type { Setmeal, SetmealDish } from '@/api/setmeal'
import { getCategoryList } from '@/api/category'
import type { Category } from '@/api/category'
import { getDishList } from '@/api/dish'
import type { Dish } from '@/api/dish'

const searchForm = reactive({ name: '', categoryId: undefined as number | undefined, status: undefined as number | undefined })
const tableData = ref<Setmeal[]>([])
const categoryList = ref<Category[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const selectedIds = ref<number[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<Setmeal>({
  categoryId: 0,
  name: '',
  price: 0,
  description: '',
  image: '',
  setmealDishes: [],
})

const dishSelectVisible = ref(false)
const dishList = ref<Dish[]>([])
const selectedDishes = ref<Dish[]>([])
const dishSearchCategoryId = ref<number | undefined>()
const dishSearchName = ref('')

const uploadHeaders = computed(() => ({
  token: localStorage.getItem('token') || '',
}))

const rules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择套餐分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入套餐价格', trigger: 'blur' }],
}

async function fetchData() {
  try {
    const res: any = await getSetmealPage({
      name: searchForm.name,
      categoryId: searchForm.categoryId,
      status: searchForm.status,
      page: pagination.page,
      pageSize: pagination.pageSize,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

async function fetchCategories() {
  try {
    const res: any = await getCategoryList(2)
    categoryList.value = res.data
  } catch {}
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  Object.assign(searchForm, { name: '', categoryId: undefined, status: undefined })
  handleSearch()
}

function handleSelectionChange(rows: Setmeal[]) {
  selectedIds.value = rows.map((r) => r.id!)
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: undefined, categoryId: 0, name: '', price: 0, description: '', image: '', setmealDishes: [] })
  dialogVisible.value = true
}

async function handleEdit(row: Setmeal) {
  isEdit.value = true
  try {
    const res: any = await getSetmealById(row.id!)
    const data = res.data
    Object.assign(form, {
      id: data.id,
      categoryId: data.categoryId,
      name: data.name,
      price: data.price,
      description: data.description,
      image: data.image,
      setmealDishes: data.setmealDishes || [],
    })
    dialogVisible.value = true
  } catch {}
}

function handleUploadSuccess(response: any) {
  if (response.code === 1) {
    form.image = response.data
    ElMessage.success('上传成功')
  }
}

async function showDishSelect() {
  dishSearchCategoryId.value = undefined
  dishSearchName.value = ''
  await fetchDishList()
  dishSelectVisible.value = true
}

async function fetchDishList() {
  try {
    const res: any = await getDishList(dishSearchCategoryId.value || 0)
    let list = res.data || []
    if (dishSearchName.value) {
      list = list.filter((d: Dish) => d.name.includes(dishSearchName.value))
    }
    dishList.value = list
  } catch {}
}

function handleDishSelectionChange(rows: Dish[]) {
  selectedDishes.value = rows
}

function confirmDishSelect() {
  for (const dish of selectedDishes.value) {
    if (!form.setmealDishes.some((d) => d.dishId === dish.id)) {
      form.setmealDishes.push({
        dishId: dish.id!,
        name: dish.name,
        price: dish.price,
        copies: 1,
      })
    }
  }
  dishSelectVisible.value = false
}

function removeDish(index: number) {
  form.setmealDishes.splice(index, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.setmealDishes.length === 0) {
    ElMessage.warning('请至少添加一个菜品')
    return
  }

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateSetmeal(form)
      ElMessage.success('编辑成功')
    } else {
      await addSetmeal(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {} finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: Setmeal) {
  await ElMessageBox.confirm('确认删除该套餐？', '提示', { type: 'warning' })
  try {
    await deleteSetmeal(String(row.id))
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个套餐？`, '提示', { type: 'warning' })
  try {
    await deleteSetmeal(selectedIds.value.join(','))
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

async function handleStatusChange(id: number, status: number) {
  try {
    await enableSetmeal(status, id)
    ElMessage.success(status === 1 ? '已启售' : '已停售')
    fetchData()
  } catch {}
}

onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<style scoped>
.image-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.image-uploader:hover {
  border-color: #409eff;
}
</style>
