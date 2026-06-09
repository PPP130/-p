<template>
  <div class="sidebar-wrapper">
    <div class="logo">
      <span v-if="!isCollapse">{{ title }}</span>
      <span v-else>{{ title.charAt(0) }}</span>
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
      :collapse-transition="false"
    >
      <el-menu-item index="/admin/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>工作台</template>
      </el-menu-item>
      <el-menu-item index="/admin/report">
        <el-icon><DataAnalysis /></el-icon>
        <template #title>数据统计</template>
      </el-menu-item>
      <el-menu-item index="/admin/employee">
        <el-icon><User /></el-icon>
        <template #title>员工管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/category">
        <el-icon><Menu /></el-icon>
        <template #title>分类管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/dish">
        <el-icon><Bowl /></el-icon>
        <template #title>菜品管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/setmeal">
        <el-icon><Tickets /></el-icon>
        <template #title>套餐管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/order">
        <el-icon><Document /></el-icon>
        <template #title>订单管理</template>
      </el-menu-item>
      <el-menu-item index="/admin/shop">
        <el-icon><Shop /></el-icon>
        <template #title>店铺管理</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

defineProps<{ isCollapse: boolean }>()

const route = useRoute()
const activeMenu = computed(() => route.path)
const userStore = useUserStore()
const title = computed(() => userStore.shopName || '点餐平台')
</script>

<style scoped>
.sidebar-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  white-space: nowrap;
}

.el-menu {
  border-right: none;
  flex: 1;
}
</style>
