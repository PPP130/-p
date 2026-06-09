const { getOrderList } = require('../../../utils/api')

Component({
  data: {
    userInfo: null,
    shopName: '',
    shopId: '',
    orderCounts: {
      unpaid: 0,
      processing: 0,
      completed: 0
    }
  },

  pageLifetimes: {
    show() {
      this.loadShopInfo()
      this.loadOrderCounts()
    }
  },

  methods: {
    loadShopInfo() {
      const app = getApp()
      this.setData({
        shopId: app.globalData.shopId || '',
        shopName: app.globalData.shopName || '未选择店铺'
      })
    },

    onSwitchShop() {
      wx.navigateTo({ url: '/pages/shop/list/list?switch=1' })
    },

    async loadOrderCounts() {
      try {
        const [unpaid, processing, completed] = await Promise.all([
          getOrderList({ page: 1, pageSize: 1, status: 1 }),
          getOrderList({ page: 1, pageSize: 1, status: 2 }),
          getOrderList({ page: 1, pageSize: 1, status: 5 })
        ])
        this.setData({
          'orderCounts.unpaid': unpaid.total || 0,
          'orderCounts.processing': processing.total || 0,
          'orderCounts.completed': completed.total || 0
        })
      } catch (e) {
        console.error('获取订单统计失败', e)
      }
    },

    onOrdersTap(e) {
      const status = e.currentTarget.dataset.status
      wx.switchTab({ url: '/pages/order/list/list' })
    },

    onAddressTap() {
      wx.navigateTo({ url: '/pages/address/list/list' })
    },

    onAboutTap() {
      wx.showModal({
        title: '关于',
        content: '苍穹外卖 v1.0.0\nPowered by Sky Take-Out',
        showCancel: false
      })
    }
  }
})
