import request from '@/utils/request'

export interface Employee {
  id?: number
  username: string
  name: string
  phone: string
  sex: string
  idNumber: string
  status?: number
  createTime?: string
}

export function login(data: { shopId: string; username: string; password: string }) {
  return request.post('/admin/employee/login', data)
}

export function getEmployeePage(params: { name?: string; page: number; pageSize: number }) {
  return request.get('/admin/employee/page', { params })
}

export function addEmployee(data: Employee) {
  return request.post('/admin/employee', data)
}

export function updateEmployee(data: Employee) {
  return request.put('/admin/employee', data)
}

export function getEmployeeById(id: number) {
  return request.get(`/admin/employee/${id}`)
}

export function enableEmployee(status: number, id: number) {
  return request.post(`/admin/employee/status/${status}`, null, { params: { id } })
}

export function editPassword(data: { empId: number; oldPassword: string; newPassword: string }) {
  return request.put('/admin/employee/editPassword', data)
}
