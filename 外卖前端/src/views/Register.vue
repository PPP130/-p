<template>
  <div class="register-page">
    <div class="page-split">
      <!-- 左侧 -->
      <div class="left-panel">
        <div class="brand-area">
          <div class="brand-logo-small">
            <svg viewBox="0 0 32 32" width="36" height="36" fill="none">
              <circle cx="16" cy="16" r="14" fill="currentColor" opacity="0.08"/>
              <path d="M10 14c0-3 2.5-6 6-6s6 3 6 6c0 4-6 10-6 10s-6-6-6-10z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <circle cx="16" cy="13" r="2" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="brand-heading">点餐平台</h1>
          <span class="brand-sub">加盟申请</span>
        </div>

        <div class="steps-area">
          <h3 class="steps-title">入驻流程</h3>
          <div class="step-item" v-for="(step, i) in steps" :key="i">
            <div class="step-line-wrap">
              <span class="step-num">{{ String(i + 1).padStart(2, '0') }}</span>
              <span class="step-connector" v-if="i < steps.length - 1"></span>
            </div>
            <div class="step-info">
              <h4>{{ step.title }}</h4>
              <p>{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单 -->
      <div class="right-panel">
        <div class="form-wrap">
          <div class="form-header">
            <h2>开一家新店</h2>
            <p>填好这些信息，我们来帮你搭建店铺</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            @submit.prevent="handleRegister"
          >
            <div class="form-group">
              <label class="field-label">店铺名称</label>
              <el-form-item prop="name">
                <el-input v-model="form.name" placeholder="比如：老张面馆" size="large" class="styled-input" />
              </el-form-item>
            </div>

            <div class="form-group">
              <label class="field-label">店主姓名</label>
              <el-form-item prop="ownerName">
                <el-input v-model="form.ownerName" placeholder="你的姓名" size="large" class="styled-input" />
              </el-form-item>
            </div>

            <div class="form-group">
              <label class="field-label">联系电话</label>
              <el-form-item prop="phone">
                <el-input v-model="form.phone" placeholder="用于联系和通知审核结果" size="large" class="styled-input" />
              </el-form-item>
            </div>

            <div class="form-group">
              <label class="field-label">店铺地址</label>
              <el-form-item prop="address">
                <el-input v-model="form.address" placeholder="详细地址" size="large" class="styled-input" />
              </el-form-item>
            </div>

            <div class="form-group">
              <label class="field-label">店铺简介 <span class="label-optional">选填</span></label>
              <el-form-item prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="3"
                  placeholder="说说你家的招牌菜"
                  size="large"
                  class="styled-input"
                />
              </el-form-item>
            </div>

            <el-form-item class="submit-wrap">
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="register-submit"
                @click="handleRegister"
              >
                提交申请
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            已有店铺？<router-link to="/login">去登录</router-link>
            <span class="back-link" @click="$router.push('/')">返回首页</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 成功弹窗 -->
    <el-dialog v-model="showSuccess" title="" width="420px" :close-on-click-modal="false" class="success-dialog">
      <div class="success-content">
        <div class="success-icon">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 11-5.93-9.14"/>
            <path d="M22 4L12 14.01l-3-3"/>
          </svg>
        </div>
        <h3>申请提交成功</h3>
        <p class="success-desc">以下是你的店铺信息，请妥善保存</p>
        <div class="info-card">
          <div class="info-row">
            <span class="info-label">店铺编号</span>
            <span class="info-value highlight">{{ generatedShopId }}</span>
          </div>
          <div class="divider"></div>
          <div class="info-row">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ generatedUsername }}</span>
          </div>
          <div class="divider"></div>
          <div class="info-row">
            <span class="info-label">初始密码</span>
            <span class="info-value">{{ generatedPassword }}</span>
          </div>
        </div>
        <p class="tip">登录后建议尽快修改密码</p>
      </div>
      <template #footer>
        <el-button type="primary" size="large" @click="$router.push('/login')" class="success-btn">去登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { registerShop } from '@/api/shop'

const formRef = ref<FormInstance>()
const loading = ref(false)
const showSuccess = ref(false)
const generatedShopId = ref('')
const generatedUsername = ref('')
const generatedPassword = ref('')

const form = reactive({
  name: '',
  ownerName: '',
  phone: '',
  address: '',
  description: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  ownerName: [{ required: true, message: '请输入店主姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  address: [{ required: true, message: '请输入店铺地址', trigger: 'blur' }],
}

const steps = [
  { title: '填写申请', desc: '填写店铺基本信息提交申请' },
  { title: '平台审核', desc: '1-3 个工作日内完成审核' },
  { title: '开始经营', desc: '审核通过后登录后台配置菜品' },
]

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res: any = await registerShop(form)
    generatedShopId.value = res.data.shopId
    generatedUsername.value = res.data.username
    generatedPassword.value = res.data.password
    showSuccess.value = true
    ElMessage.success('注册成功')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: #fffcf8;
  display: flex;
}

.page-split {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

/* 左侧 */
.left-panel {
  flex: 1;
  background: #faf7f2;
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  max-width: 42%;
}

.left-panel::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(198, 123, 92, 0.07);
}

.brand-area {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}

.brand-logo-small svg {
  color: #c67b5c;
}

.brand-heading {
  font-size: 22px;
  font-weight: 700;
  color: #2d2016;
}

.brand-sub {
  font-size: 14px;
  color: #8b7d6f;
  padding: 3px 10px;
  background: rgba(198, 123, 92, 0.1);
  border-radius: 6px;
  font-weight: 500;
}

/* 步骤区 */
.steps-area {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-bottom: 40px;
}

.steps-title {
  font-size: 14px;
  font-weight: 600;
  color: #6b5d50;
  margin-bottom: 24px;
  letter-spacing: 0.5px;
}

.step-item {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.step-line-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff;
  border: 1.5px solid #d4c4a8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #c67b5c;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.step-connector {
  display: block;
  width: 1.5px;
  height: 28px;
  background: #d4c4a8;
  margin: 6px 0;
}

.step-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: #2d2016;
  margin-bottom: 4px;
}

.step-info p {
  font-size: 13px;
  color: #8b7d6f;
  line-height: 1.5;
}

/* 右侧表单 */
.right-panel {
  flex: 1;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 60px 40px;
  background: #fff;
  border-left: 1px solid #ede8df;
  overflow-y: auto;
}

.form-wrap {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #2d2016;
  margin-bottom: 6px;
}

.form-header p {
  font-size: 14px;
  color: #8b7d6f;
}

.form-group {
  margin-bottom: 4px;
}

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #6b5d50;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.label-optional {
  font-weight: 400;
  color: #bbb0a0;
}

.form-group :deep(.el-form-item) {
  margin-bottom: 0;
}

.form-group :deep(.el-form-item__error) {
  padding-top: 4px;
}

.styled-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 14px;
  background: #faf7f2;
  box-shadow: none;
  border: 1.5px solid transparent;
  transition: all 0.2s ease;
}

.styled-input :deep(.el-input__wrapper:hover) {
  background: #fff;
  border-color: #d4c4a8;
}

.styled-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #c67b5c;
  box-shadow: 0 0 0 3px rgba(198, 123, 92, 0.1);
}

.styled-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #2d2016;
  height: 40px;
}

.styled-input :deep(.el-textarea__inner) {
  font-size: 15px;
  color: #2d2016;
  background: #faf7f2;
  border-radius: 10px;
  padding: 10px 14px;
  box-shadow: none;
  border: 1.5px solid transparent;
  transition: all 0.2s ease;
  resize: vertical;
}

.styled-input :deep(.el-textarea__inner:focus) {
  background: #fff;
  border-color: #c67b5c;
  box-shadow: 0 0 0 3px rgba(198, 123, 92, 0.1);
}

.submit-wrap {
  margin-top: 24px;
  margin-bottom: 0;
}

.register-submit {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: #c67b5c !important;
  border: none !important;
  letter-spacing: 1px;
}

.register-submit:hover {
  background: #b56b4c !important;
}

.form-footer {
  margin-top: 20px;
  font-size: 14px;
  color: #8b7d6f;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-footer a {
  color: #c67b5c;
  text-decoration: none;
  font-weight: 500;
}

.form-footer a:hover {
  text-decoration: underline;
}

.back-link {
  color: #8b7d6f;
  cursor: pointer;
  transition: color 0.2s;
}

.back-link:hover {
  color: #2d2016;
}

/* 成功弹窗 */
.success-content {
  text-align: center;
  padding: 8px 0;
}

.success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #e8f5e9;
  color: #5a9a6b;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.success-content h3 {
  font-size: 20px;
  color: #2d2016;
  margin-bottom: 6px;
}

.success-desc {
  font-size: 14px;
  color: #8b7d6f;
  margin-bottom: 20px;
}

.info-card {
  background: #faf7f2;
  border-radius: 12px;
  padding: 16px 20px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  font-size: 13px;
  color: #8b7d6f;
}

.info-value {
  font-size: 14px;
  color: #2d2016;
  font-weight: 500;
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.info-value.highlight {
  color: #c67b5c;
  font-size: 16px;
  font-weight: 700;
}

.divider {
  height: 1px;
  background: #ede8df;
}

.tip {
  font-size: 12px;
  color: #999;
  margin-top: 12px;
}

.success-btn {
  width: 100%;
  background: #c67b5c !important;
  border: none !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .left-panel {
    display: none;
  }

  .right-panel {
    padding: 32px 24px;
    justify-content: flex-start;
  }

  .form-wrap {
    max-width: 100%;
  }

  .form-header h2 {
    font-size: 22px;
  }
}

@media (max-width: 480px) {
  .register-page {
    background: #fff;
  }

  .right-panel {
    padding: 24px 16px;
  }
}
</style>
