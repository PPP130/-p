const { request } = require('./request')

// 用户
const userLogin = (data) => request({ url: '/user/user/login', method: 'POST', data })

// 店铺
const getShopStatus = () => request({ url: '/user/shop/status' })
const getShopList = () => request({ url: '/user/shop/list' })
const getShopBusinessStatus = (shopId) => request({ url: `/user/shop/${shopId}/status` })
const getShopInfo = (shopId) => request({ url: `/user/shop/${shopId}/info` })

// 分类
const getCategoryList = (type) => request({ url: '/user/category/list', data: { type } })

// 菜品
const getDishList = (categoryId) => request({ url: '/user/dish/list', data: { categoryId } })

// 套餐
const getSetmealList = (categoryId) => request({ url: '/user/setmeal/list', data: { categoryId } })
const getSetmealDishes = (id) => request({ url: '/user/setmeal/dish/deatils', data: { id } })

// 购物车
const getCartList = () => request({ url: '/user/shoppingCart/list' })
const addCart = (data) => request({ url: '/user/shoppingCart/add', method: 'POST', data })
const subCart = (data) => request({ url: '/user/shoppingCart/sub', method: 'POST', data })
const cleanCart = () => request({ url: '/user/shoppingCart/clean', method: 'DELETE' })

// 地址
const getAddressList = () => request({ url: '/user/addressBook/list' })
const getDefaultAddress = () => request({ url: '/user/addressBook/default' })
const getAddressById = (id) => request({ url: `/user/addressBook/${id}` })
const saveAddress = (data) => request({ url: '/user/addressBook', method: 'POST', data })
const updateAddress = (data) => request({ url: '/user/addressBook', method: 'PUT', data })
const setDefaultAddress = (data) => request({ url: '/user/addressBook/default', method: 'PUT', data })
const deleteAddress = (id) => request({ url: `/user/addressBook/${id}`, method: 'DELETE' })

// 订单
const submitOrder = (data) => request({ url: '/user/order/submit', method: 'POST', data })
const getOrderList = (params) => request({ url: '/user/order/userPage', data: params })
const getOrderDetail = (id) => request({ url: `/user/order/orderDetail/${id}` })
const cancelOrder = (id) => request({ url: `/user/order/cancel/${id}`, method: 'PUT' })
const payOrder = (data) => request({ url: '/user/order/payment', method: 'PUT', data })
const repeatOrder = (id) => request({ url: `/user/order/again/${id}` })
const remindOrder = (id) => request({ url: `/user/order/reminder/${id}` })

module.exports = {
  userLogin, getShopStatus, getShopList, getShopBusinessStatus, getShopInfo,
  getCategoryList, getDishList, getSetmealList, getSetmealDishes,
  getCartList, addCart, subCart, cleanCart,
  getAddressList, getDefaultAddress, getAddressById, saveAddress, updateAddress, setDefaultAddress, deleteAddress,
  submitOrder, getOrderList, getOrderDetail, cancelOrder, payOrder, repeatOrder, remindOrder
}
