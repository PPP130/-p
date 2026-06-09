<template>
  <div class="navbar">
    <div class="left">
      <el-icon class="collapse-btn" @click="$emit('toggle-collapse')">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
      </el-icon>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="right">
      <el-tag v-if="userStore.shopId" size="small" type="info" class="shop-tag">
        {{ userStore.shopName || userStore.shopId }}
      </el-tag>
      <span class="user-name">{{ userStore.name || userStore.userName }}</span>
      <el-dropdown @command="handleCommand">
        <el-icon class="el-dropdown-link"><ArrowDown /></el-icon>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

defineProps<{ isCollapse: boolean }>()
defineEmits(['toggle-collapse'])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.navbar {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #666;
}

.collapse-btn:hover {
  color: #409eff;
}

.right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-tag {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.el-dropdown-link {
  cursor: pointer;
  color: #666;
}
</style>
