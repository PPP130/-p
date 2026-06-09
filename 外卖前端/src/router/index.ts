import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '店铺加盟' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AppLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis' },
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/Employee.vue'),
        meta: { title: '员工管理', icon: 'User' },
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category.vue'),
        meta: { title: '分类管理', icon: 'Menu' },
      },
      {
        path: 'dish',
        name: 'Dish',
        component: () => import('@/views/Dish.vue'),
        meta: { title: '菜品管理', icon: 'Bowl' },
      },
      {
        path: 'setmeal',
        name: 'Setmeal',
        component: () => import('@/views/Setmeal.vue'),
        meta: { title: '套餐管理', icon: 'Tickets' },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/Order.vue'),
        meta: { title: '订单管理', icon: 'Document' },
      },
      {
        path: 'shop',
        name: 'Shop',
        component: () => import('@/views/Shop.vue'),
        meta: { title: '店铺管理', icon: 'Shop' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 公开页面白名单
const publicPaths = ['/', '/login', '/register']

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (publicPaths.includes(to.path)) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
