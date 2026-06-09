const { getOrderDetail, cancelOrder, payOrder, repeatOrder, remindOrder } = require('../../../utils/api')

const STATUS_MAP = {
  1: { text: '待付款', icon: '💰' },
  2: { text: '待接单', icon: '⏳' },
  3: { text: '待配送', icon: '📋' },
  4: { text: '配送中', icon: '🚗' },
  5: { text: '已完成', icon: '✅' },
  6: { text: '已取消', icon: '❌' }
}

Component({
  data: {
    orderId: null,
    order: null,
    statusInfo: {}
  },

  lifetimes: {
    attached() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      const id = page.options && page.options.id
      if (id) {
        this.setData({ orderId: id })
        this.loadDetail(id)
      }
    }
  },

  methods: {
    async loadDetail(id) {
      try {
        const order = await getOrderDetail(id)
        const statusInfo = STATUS_MAP[order.status] || { text: '未知', icon: '❓' }
        this.setData({ order, statusInfo })
      } catch (e) {
        console.error('获取订单详情失败', e)
      }
    },

    onPay() {
      const { order } = this.data
      payOrder({ orderNumber: order.number }).then(() => {
        wx.showToast({ title: '支付成功', icon: 'success' })
        this.loadDetail(this.data.orderId)
      })
    },

    onCancel() {
      wx.showModal({
        title: '提示',
        content: '确定取消订单吗？',
        success: (res) => {
          if (res.confirm) {
            cancelOrder(this.data.orderId).then(() => {
              wx.showToast({ title: '已取消', icon: 'success' })
              this.loadDetail(this.data.orderId)
            })
          }
        }
      })
    },

    onRemind() {
      remindOrder(this.data.orderId).then(() => {
        wx.showToast({ title: '已催单', icon: 'success' })
      })
    },

    onRepeat() {
      repeatOrder(this.data.orderId).then(() => {
        wx.showToast({ title: '已加入购物车', icon: 'success' })
        wx.switchTab({ url: '/pages/index/index' })
      })
    }
  }
})
