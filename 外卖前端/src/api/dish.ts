import request from '@/utils/request'

export interface Flavor {
  id?: number
  dishId?: number
  name: string
  value: string
}

export interface Dish {
  id?: number
  name: string
  categoryId: number
  categoryName?: string
  price: number
  image: string
  description: string
  status?: number
  updateTime?: string
  flavors: Flavor[]
}

export function getDishPage(params: { name?: string; categoryId?: number; status?: number; page: number; pageSize: number }) {
  return request.get('/admin/dish/page', { params })
}

export function addDish(data: Dish) {
  return request.post('/admin/dish', data)
}

export function updateDish(data: Dish) {
  return request.put('/admin/dish', data)
}

export function getDishById(id: number) {
  return request.get(`/admin/dish/${id}`)
}

export function deleteDish(ids: string) {
  return request.delete('/admin/dish', { params: { ids } })
}

export function enableDish(status: number, id: number) {
  return request.post(`/admin/dish/status/${status}`, null, { params: { id } })
}

export function getDishList(categoryId: number) {
  return request.get('/admin/dish/list', { params: { categoryId } })
}
