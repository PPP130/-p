import request from '@/utils/request'

export function getTurnoverStatistics(params: { begin: string; end: string }) {
  return request.get('/admin/report/turnoverStatistics', { params })
}

export function getOrdersStatistics(params: { begin: string; end: string }) {
  return request.get('/admin/report/ordersStatistics', { params })
}

export function getUsersStatistics(params: { begin: string; end: string }) {
  return request.get('/admin/report/usersStatistics', { params })
}

export function getTop10(params: { begin: string; end: string }) {
  return request.get('/admin/report/top10', { params })
}

export function exportReport() {
  return request.get('/admin/report/export', { responseType: 'blob' })
}
