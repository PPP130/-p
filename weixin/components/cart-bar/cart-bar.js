Component({
  properties: {
    totalCount: { type: Number, value: 0 },
    totalAmount: { type: Number, value: 0 },
    shopStatus: { type: Number, value: 1 }
  },

  methods: {
    onCartTap() {
      this.triggerEvent('toggle')
    },

    onCheckout() {
      if (this.data.totalCount === 0) {
        wx.showToast({ title: '请先添加商品', icon: 'none' })
        return
      }
      if (this.data.shopStatus !== 1) {
        wx.showToast({ title: '店铺已打烊', icon: 'none' })
        return
      }
      this.triggerEvent('checkout')
    }
  }
})
