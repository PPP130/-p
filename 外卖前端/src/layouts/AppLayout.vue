<template>
  <el-container class="app-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <Sidebar :is-collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header class="header">
        <Navbar :is-collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <!-- AI客服组件 -->
    <AIChat :shop-id="currentShopId" :logged-in="!!currentShopId" :token="currentToken" />
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import AIChat from '@/components/AIChat.vue'

const isCollapse = ref(false)

// 获取当前店铺ID（从登录状态或localStorage）
const currentShopId = computed(() => {
  return localStorage.getItem('shopId') || ''
})

// 获取当前JWT token
const currentToken = computed(() => {
  return localStorage.getItem('token') || ''
})
</script>

<style scoped>
.app-layout {
  height: 100vh;
}

.sidebar {
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 56px;
  z-index: 1;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
