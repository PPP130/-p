import request from '@/utils/request'

export function getShopStatus() {
  return request.get('/admin/shop/status')
}

export function setShopStatus(status: number) {
  return request.put(`/admin/shop/status`, null, { params: { status } })
}

export function registerShop(data: {
  name: string
  ownerName: string
  phone: string
  address: string
  description?: string
}) {
  return request.post('/admin/shop/register', data)
}

export function getShopPage(params: { page: number; pageSize: number; name?: string; status?: number }) {
  return request.get('/admin/shop/page', { params })
}

export function approveShop(shopId: string) {
  return request.put(`/admin/shop/approve/${shopId}`)
}

export function rejectShop(shopId: string) {
  return request.put(`/admin/shop/reject/${shopId}`)
}

export function getShopByShopId(shopId: string) {
  return request.get(`/admin/shop/info/${shopId}`)
}

export function updateShop(shopId: string, data: {
  name?: string
  image?: string
  address?: string
  description?: string
  phone?: string
  ownerName?: string
}) {
  return request.put(`/admin/shop/info/${shopId}`, data)
}

export function setBusinessStatus(shopId: string, status: number) {
  return request.put(`/admin/shop/businessStatus`, null, { params: { shopId, status } })
}
