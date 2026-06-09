import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userName = ref(localStorage.getItem('userName') || '')
  const name = ref(localStorage.getItem('name') || '')
  const shopId = ref(localStorage.getItem('shopId') || '')
  const shopName = ref(localStorage.getItem('shopName') || '')

  function setLoginInfo(data: { token: string; userName: string; name: string; shopId: string; shopName: string }) {
    token.value = data.token
    userName.value = data.userName
    name.value = data.name
    shopId.value = data.shopId
    shopName.value = data.shopName
    localStorage.setItem('token', data.token)
    localStorage.setItem('userName', data.userName)
    localStorage.setItem('name', data.name)
    localStorage.setItem('shopId', data.shopId)
    localStorage.setItem('shopName', data.shopName)
  }

  function logout() {
    token.value = ''
    userName.value = ''
    name.value = ''
    shopId.value = ''
    shopName.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userName')
    localStorage.removeItem('name')
    localStorage.removeItem('shopId')
    localStorage.removeItem('shopName')
  }

  return { token, userName, name, shopId, shopName, setLoginInfo, logout }
})
