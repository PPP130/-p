const baseUrl = 'http://localhost:8080'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    const app = getApp()
    const shopId = app ? app.globalData.shopId : ''
    wx.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'authentication': token || '',
        'shopId': shopId || '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 401) {
          wx.removeStorageSync('token')
          wx.login({
            success: (loginRes) => {
              login({ code: loginRes.code }).then(data => {
                wx.setStorageSync('token', data.token)
                // 重试原请求
                request(options).then(resolve).catch(reject)
              })
            }
          })
          return
        }
        if (res.data.code === 1) {
          resolve(res.data.data)
        } else {
          wx.showToast({ title: res.data.msg || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}

const login = (data) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: baseUrl + '/user/user/login',
      method: 'POST',
      data,
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        if (res.data.code === 1) {
          resolve(res.data.data)
        } else {
          reject(res.data)
        }
      },
      fail: reject
    })
  })
}

module.exports = { request, login, baseUrl }
