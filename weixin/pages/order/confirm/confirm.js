const { getDefaultAddress, getCartList, submitOrder } = require('../../../utils/api')

Component({
  data: {
    address: null,
    cartItems: [],
    totalAmount: 0,
    packAmount: 0,
    remark: '',
    tablewareNumber: 1,
    tablewareStatus: 0, // 0=按餐量提供 1=选择数量
    payMethod: 1, // 1=微信支付
    deliveryStatus: 0 // 0=立即配送
  },

  lifetimes: {
    attached() {
      this.loadData()
    }
  },

  methods: {
    async loadData() {
      try {
        const [address, cartList] = await Promise.all([
          getDefaultAddress(),
          getCartList()
        ])
        const cartItems = cartList || []
        let totalAmount = 0
        cartItems.forEach(item => {
          totalAmount += item.amount * item.number
        })
        // 计算打包费
        const packAmount = Math.ceil(cartItems.length / 2) * 2
        this.setData({
          address,
          cartItems,
          totalAmount,
          packAmount
        })
      } catch (e) {
        console.error('加载数据失败', e)
      }
    },

    onSelectAddress() {
      wx.navigateTo({ url: '/pages/address/list/list?select=1' })
    },

    onRemarkInput(e) {
      this.setData({ remark: e.detail.value })
    },

    onTablewareChange(e) {
      this.setData({ tablewareNumber: e.detail.value })
    },

    async onSubmit() {
      const { address, cartItems, remark, tablewareNumber, tablewareStatus, payMethod, deliveryStatus, packAmount, totalAmount } = this.data
      if (!address) {
        wx.showToast({ title: '请选择收货地址', icon: 'none' })
        return
      }
      if (cartItems.length === 0) {
        wx.showToast({ title: '购物车为空', icon: 'none' })
        return
      }

      const data = {
        addressBookId: address.id,
        payMethod,
        remark,
        estimatedDeliveryTime: this._getDeliveryTime(),
        deliveryStatus,
        tablewareNumber,
        tablewareStatus,
        packAmount,
        amount: totalAmount + packAmount
      }

      try {
        const result = await submitOrder(data)
        wx.showToast({ title: '下单成功', icon: 'success' })
        // 模拟支付：直接标记为已支付
        setTimeout(() => {
          wx.navigateTo({
            url: `/pages/order/detail/detail?id=${result.id}`
          })
        }, 1500)
      } catch (e) {
        console.error('下单失败', e)
      }
    },

    _getDeliveryTime() {
      const now = new Date()
      now.setMinutes(now.getMinutes() + 45)
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const h = String(now.getHours()).padStart(2, '0')
      const min = String(now.getMinutes()).padStart(2, '0')
      const s = String(now.getSeconds()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${min}:${s}`
    }
  }
})
