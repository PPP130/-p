import request from '@/utils/request'

export interface SetmealDish {
  id?: number
  setmealId?: number
  dishId: number
  name: string
  price: number
  copies: number
}

export interface Setmeal {
  id?: number
  categoryId: number
  categoryName?: string
  name: string
  price: number
  status?: number
  description: string
  image: string
  updateTime?: string
  setmealDishes: SetmealDish[]
}

export function getSetmealPage(params: { name?: string; categoryId?: number; status?: number; page: number; pageSize: number }) {
  return request.get('/admin/setmeal/page', { params })
}

export function addSetmeal(data: Setmeal) {
  return request.post('/admin/setmeal', data)
}

export function updateSetmeal(data: Setmeal) {
  return request.put('/admin/setmeal', data)
}

export function getSetmealById(id: number) {
  return request.get(`/admin/setmeal/${id}`)
}

export function deleteSetmeal(ids: string) {
  return request.delete('/admin/setmeal', { params: { ids } })
}

export function enableSetmeal(status: number, id: number) {
  return request.post(`/admin/setmeal/status/${status}`, null, { params: { id } })
}
