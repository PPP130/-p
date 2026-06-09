import request from '@/utils/request'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/common/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
