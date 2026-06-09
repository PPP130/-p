import request from '@/utils/request'

export interface Category {
  id?: number
  type: number // 1菜品分类 2套餐分类
  name: string
  sort: number
  status?: number
  createTime?: string
}

export function getCategoryPage(params: { name?: string; type?: number; page: number; pageSize: number }) {
  return request.get('/admin/category/page', { params })
}

export function addCategory(data: Category) {
  return request.post('/admin/category', data)
}

export function updateCategory(data: Category) {
  return request.put('/admin/category', data)
}

export function deleteCategory(id: number) {
  return request.delete('/admin/category', { params: { id } })
}

export function enableCategory(status: number, id: number) {
  return request.post(`/admin/category/status/${status}`, null, { params: { id } })
}

export function getCategoryList(type: number) {
  return request.get('/admin/category/list', { params: { type } })
}
