<template>
  <div class="page-container">
    <!-- 营业状态 -->
    <el-card shadow="never" class="status-card">
      <template #header>
        <span class="section-title">营业状态</span>
      </template>
      <div class="shop-status">
        <div class="status-info">
          <el-icon :size="48" :color="businessStatus === 1 ? '#67c23a' : '#909399'">
            <Shop />
          </el-icon>
          <div class="status-text">
            <h3>{{ businessStatus === 1 ? '营业中' : '已打烊' }}</h3>
            <p>{{ businessStatus === 1 ? '店铺正在正常营业，可以接收订单' : '店铺已关闭，暂停接收订单' }}</p>
          </div>
        </div>
        <el-switch
          v-model="businessStatus"
          :active-value="1"
          :inactive-value="0"
          active-text="营业"
          inactive-text="打烊"
          size="large"
          @change="handleStatusChange"
        />
      </div>
    </el-card>

    <!-- 店铺信息 -->
    <el-card shadow="never" class="info-card">
      <template #header>
        <span class="section-title">店铺信息</span>
      </template>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="shop-form"
      >
        <el-form-item label="店铺图片">
          <div class="image-upload-wrap">
            <el-image
              v-if="form.image"
              :src="form.image"
              fit="cover"
              class="shop-preview"
            />
            <div v-else class="image-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <span>上传图片</span>
            </div>
            <el-upload
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              :http-request="handleImageUpload"
              accept="image/*"
            >
              <el-button type="primary" size="small">{{ form.image ? '更换图片' : '上传图片' }}</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <el-form-item label="店铺名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入店铺名称" maxlength="30" show-word-limit />
        </el-form-item>

        <el-form-item label="店主姓名" prop="ownerName">
          <el-input v-model="form.ownerName" placeholder="请输入店主姓名" maxlength="20" />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="15" />
        </el-form-item>

        <el-form-item label="店铺地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入店铺地址" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="店铺简介">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入店铺简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { getShopByShopId, updateShop, setBusinessStatus } from '@/api/shop'
import { uploadFile } from '@/api/common'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 营业状态
const businessStatus = ref(1)

// 表单
const formRef = ref<FormInstance>()
const saving = ref(false)
const form = reactive({
  name: '',
  ownerName: '',
  phone: '',
  address: '',
  image: '',
  description: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }],
}

// 加载店铺信息（含营业状态）
async function loadShopInfo() {
  const shopId = userStore.shopId
  if (!shopId) return
  try {
    const res: any = await getShopByShopId(shopId)
    const shop = res.data
    if (shop) {
      businessStatus.value = shop.businessStatus ?? 1
      form.name = shop.name || ''
      form.ownerName = shop.ownerName || ''
      form.phone = shop.phone || ''
      form.address = shop.address || ''
      form.image = shop.image || ''
      form.description = shop.description || ''
    }
  } catch (e) {
    console.error('获取店铺信息失败', e)
  }
}

// 切换营业状态
async function handleStatusChange(val: number) {
  const shopId = userStore.shopId
  if (!shopId) return
  try {
    await setBusinessStatus(shopId, val)
    ElMessage.success(val === 1 ? '已开始营业' : '已打烊')
  } catch {
    businessStatus.value = val === 1 ? 0 : 1
  }
}

// 图片上传前校验
function beforeImageUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义图片上传
async function handleImageUpload(options: UploadRequestOptions) {
  try {
    const res: any = await uploadFile(options.file)
    form.image = res.data
    ElMessage.success('图片上传成功')
  } catch {
    ElMessage.error('图片上传失败')
  }
}

// 保存店铺信息
async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  const shopId = userStore.shopId
  if (!shopId) {
    ElMessage.error('未找到店铺信息')
    return
  }
  saving.value = true
  try {
    await updateShop(shopId, {
      name: form.name,
      ownerName: form.ownerName,
      phone: form.phone,
      address: form.address,
      image: form.image,
      description: form.description,
    })
    // 同步更新 store 中的店铺名
    userStore.shopName = form.name
    localStorage.setItem('shopName', form.name)
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('保存店铺信息失败', e)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShopInfo()
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.shop-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-text h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 4px;
}

.status-text p {
  font-size: 14px;
  color: #999;
}

.shop-form {
  max-width: 600px;
  padding: 10px 0;
}

.image-upload-wrap {
  display: flex;
  align-items: flex-end;
  gap: 16px;
}

.shop-preview {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
}

.image-placeholder {
  width: 120px;
  height: 120px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;
}

.image-placeholder span {
  font-size: 12px;
}
</style>
