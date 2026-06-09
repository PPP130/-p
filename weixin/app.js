const { userLogin } = require('./utils/api')

App({
  onLaunch() {
    // 恢复已保存的店铺信息
    const shopId = wx.getStorageSync('shopId')
    const shopName = wx.getStorageSync('shopName')
    if (shopId) {
      this.globalData.shopId = shopId
      this.globalData.shopName = shopName
    }

    this.login()
  },

  login() {
    wx.login({
      success: (res) => {
        if (res.code) {
          userLogin({ code: res.code }).then(data => {
            wx.setStorageSync('token', data.token)
            wx.setStorageSync('userId', data.id)
            this.globalData.userId = data.id
          }).catch(err => {
            console.error('登录失败', err)
          })
        }
      }
    })
  },

  globalData: {
    userId: null,
    shopId: null,
    shopName: '',
    baseUrl: 'http://localhost:8080'
  }
})
