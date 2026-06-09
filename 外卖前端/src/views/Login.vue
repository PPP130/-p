<template>
  <div class="login-page">
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
          <p class="brand-sub">管理端</p>
        </div>
        <div class="left-quote">
          <p>好好做菜，其他的事交给我们</p>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="right-panel">
        <div class="form-wrap">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录你的店铺管理后台</p>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            @submit.prevent="handleLogin"
          >
            <div class="form-group">
              <label class="field-label">店铺编号</label>
              <el-form-item prop="shopId">
                <el-input
                  v-model="form.shopId"
                  placeholder="例如：SHOP-001"
                  size="large"
                  class="styled-input"
                />
              </el-form-item>
            </div>

            <div class="form-group">
              <label class="field-label">用户名</label>
              <el-form-item prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="你的用户名"
                  size="large"
                  class="styled-input"
                />
              </el-form-item>
            </div>

            <div class="form-group">
              <label class="field-label">密码</label>
              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="输入密码"
                  size="large"
                  show-password
                  class="styled-input"
                />
              </el-form-item>
            </div>

            <el-form-item class="submit-wrap">
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-submit"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="form-footer">
            还没有店铺？<router-link to="/register">申请加盟</router-link>
            <span class="back-link" @click="$router.push('/')">返回首页</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/employee'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  shopId: '',
  username: '',
  password: '',
})

const rules: FormRules = {
  shopId: [{ required: true, message: '请输入店铺编号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res: any = await login(form)
    userStore.setLoginInfo({
      token: res.data.token,
      userName: res.data.userName,
      name: res.data.name,
      shopId: res.data.shopId || form.shopId,
      shopName: res.data.shopName || '',
    })
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
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
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 40px 48px;
  position: relative;
  overflow: hidden;
  max-width: 45%;
}

.left-panel::before {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(198, 123, 92, 0.08);
}

.left-panel::after {
  content: '';
  position: absolute;
  bottom: -60px;
  left: -30px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: rgba(198, 123, 92, 0.06);
}

.brand-area {
  display: flex;
  align-items: center;
  gap: 12px;
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

.left-quote {
  position: relative;
  z-index: 1;
}

.left-quote p {
  font-size: 18px;
  color: #6b5d50;
  font-style: italic;
  line-height: 1.6;
  padding-left: 16px;
  border-left: 3px solid #c67b5c;
}

/* 右侧表单 */
.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
  border-left: 1px solid #ede8df;
}

.form-wrap {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 36px;
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

.submit-wrap {
  margin-top: 28px;
  margin-bottom: 0;
}

.login-submit {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  background: #c67b5c !important;
  border: none !important;
  letter-spacing: 1px;
}

.login-submit:hover {
  background: #b56b4c !important;
}

.form-footer {
  margin-top: 24px;
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

/* 响应式 */
@media (max-width: 768px) {
  .left-panel {
    display: none;
  }

  .right-panel {
    padding: 32px 24px;
  }

  .form-wrap {
    max-width: 100%;
  }

  .form-header h2 {
    font-size: 22px;
  }
}
</style>
