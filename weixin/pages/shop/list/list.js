const { getShopList } = require('../../../utils/api')

Page({
  data: {
    shops: [],
    loading: true
  },

  onLoad(options) {
    this.isSwitchMode = options && options.switch === '1'
    this.loadShops()
  },

  onShow() {
    if (this.isSwitchMode) return
    const app = getApp()
    if (app && app.globalData && app.globalData.shopId) {
      wx.switchTab({ url: '/pages/index/index' })
    }
  },

  async loadShops() {
    this.setData({ loading: true })
    try {
      const list = await getShopList()
      this.setData({ shops: list || [], loading: false })
    } catch (e) {
      console.error('获取店铺列表失败', e)
      this.setData({ shops: [], loading: false })
      wx.showToast({ title: '获取店铺列表失败', icon: 'none' })
    }
  },

  onShopTap(e) {
    const { shopid, name, status } = e.currentTarget.dataset
    if (status !== 1) {
      wx.showToast({ title: '该店铺暂未营业', icon: 'none' })
      return
    }

    const app = getApp()
    app.globalData.shopId = shopid
    app.globalData.shopName = name
    wx.setStorageSync('shopId', shopid)
    wx.setStorageSync('shopName', name)

    wx.switchTab({ url: '/pages/index/index' })
  },

  onPullDownRefresh() {
    this.loadShops().then(() => wx.stopPullDownRefresh())
  }
})
