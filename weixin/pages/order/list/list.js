const { getOrderList, cancelOrder, payOrder, repeatOrder } = require('../../../utils/api')

const STATUS_MAP = {
  1: '待付款',
  2: '待接单',
  3: '待配送',
  4: '配送中',
  5: '已完成',
  6: '已取消'
}

Component({
  data: {
    tabs: [
      { label: '全部', value: '' },
      { label: '待付款', value: 1 },
      { label: '待接单', value: 2 },
      { label: '配送中', value: 4 },
      { label: '已完成', value: 5 },
      { label: '已取消', value: 6 }
    ],
    activeTab: '',
    orders: [],
    page: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: true
  },

  lifetimes: {
    attached() {
      this.loadOrders(true)
    }
  },

  pageLifetimes: {
    show() {
      this.loadOrders(true)
    }
  },

  methods: {
    onTabTap(e) {
      const value = e.currentTarget.dataset.value
      if (value === this.data.activeTab) return
      this.setData({ activeTab: value })
      this.loadOrders(true)
    },

    async loadOrders(reset = false) {
      if (this.data.loading) return
      if (reset) {
        this.setData({ page: 1, orders: [], hasMore: true })
      }
      if (!this.data.hasMore) return

      this.setData({ loading: true })
      try {
        const params = {
          page: this.data.page,
          pageSize: this.data.pageSize
        }
        if (this.data.activeTab !== '') {
          params.status = this.data.activeTab
        }
        const result = await getOrderList(params)
        const records = (result.records || []).map(order => ({
          ...order,
          statusText: STATUS_MAP[order.status] || '未知'
        }))
        const orders = reset ? records : [...this.data.orders, ...records]
        this.setData({
          orders,
          total: result.total,
          page: this.data.page + 1,
          hasMore: orders.length < result.total,
          loading: false
        })
      } catch (e) {
        this.setData({ loading: false })
        console.error('获取订单失败', e)
      }
    },

    onOrderTap(e) {
      const id = e.currentTarget.dataset.id
      wx.navigateTo({ url: `/pages/order/detail/detail?id=${id}` })
    },

    onPay(e) {
      const id = e.currentTarget.dataset.id
      const number = e.currentTarget.dataset.number
      // 模拟支付
      payOrder({ orderNumber: number }).then(() => {
        wx.showToast({ title: '支付成功', icon: 'success' })
        this.loadOrders(true)
      })
    },

    onCancel(e) {
      const id = e.currentTarget.dataset.id
      wx.showModal({
        title: '提示',
        content: '确定取消订单吗？',
        success: (res) => {
          if (res.confirm) {
            cancelOrder(id).then(() => {
              wx.showToast({ title: '已取消', icon: 'success' })
              this.loadOrders(true)
            })
          }
        }
      })
    },

    onRepeat(e) {
      const id = e.currentTarget.dataset.id
      repeatOrder(id).then(() => {
        wx.showToast({ title: '已加入购物车', icon: 'success' })
        wx.switchTab({ url: '/pages/index/index' })
      })
    },

    onReachBottom() {
      this.loadOrders()
    }
  }
})
