import request from '@/utils/request'

export interface OrderDetail {
  id: number
  orderId: number
  dishId: number
  setmealId: number
  name: string
  image: string
  amount: number
  copies: number
}

export interface OrderVO {
  id: number
  number: string
  status: number
  userId: number
  userName: string
  phone: string
  address: string
  consignee: string
  orderTime: string
  amount: number
  payMethod: number
  remark: string
  orderDishes: string
  orderDetailList: OrderDetail[]
}

export function getOrderPage(params: {
  page: number
  pageSize: number
  number?: string
  phone?: string
  status?: number | string
  beginTime?: string
  endTime?: string
}) {
  return request.get('/admin/order/conditionSearch', { params })
}

export function getOrderDetails(id: number) {
  return request.get(`/admin/order/details/${id}`)
}

export function confirmOrder(data: { id: number; status: number }) {
  return request.put('/admin/order/confirm', data)
}

export function rejectOrder(data: { id: number; rejectionReason: string }) {
  return request.put('/admin/order/rejection', data)
}

export function cancelOrder(data: { id: number; cancelReason: string }) {
  return request.put('/admin/order/cancel', data)
}

export function deliverOrder(id: number) {
  return request.put(`/admin/order/delivery/${id}`)
}

export function completeOrder(id: number) {
  return request.put(`/admin/order/complete/${id}`)
}

export function getOrderStatistics() {
  return request.get('/admin/order/statistics')
}
