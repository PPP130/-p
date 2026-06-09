import request from '@/utils/request'

export function getBusinessData() {
  return request.get('/admin/workspace/businessData')
}

export function getOverviewDishes() {
  return request.get('/admin/workspace/overviewDishes')
}

export function getOverviewSetmeals() {
  return request.get('/admin/workspace/overviewSetmeals')
}

export function getOverviewOrders() {
  return request.get('/admin/workspace/overviewOrders')
}
