const { addCart, subCart } = require('../../utils/api')

Component({
  properties: {
    dish: { type: Object, value: {} },
    cartCount: { type: Number, value: 0 }
  },

  data: {
    showFlavor: false,
    flavors: [],
    selectedFlavor: ''
  },

  methods: {
    onAddToCart() {
      const { dish } = this.data
      const flavors = dish.flavors || []
      if (flavors.length > 0) {
        const groups = flavors.map((f, groupIdx) => {
          const values = f.value ? f.value.split(',') : []
          return {
            groupIdx,
            name: f.name,
            selected: '',
            options: values.map(val => ({ groupIdx, value: val }))
          }
        })
        this.setData({
          showFlavor: true,
          flavorGroups: groups
        })
      } else {
        this._addToCart('')
      }
    },

    onFlavorSelect(e) {
      const groupIdx = Number(e.currentTarget.dataset.groupIdx)
      const value = e.currentTarget.dataset.value
      const flavorGroups = this.data.flavorGroups.map((f, i) => {
        if (i === groupIdx) {
          return { ...f, selected: value }
        }
        return f
      })
      this.setData({ flavorGroups })
    },

    onFlavorConfirm() {
      const { flavorGroups } = this.data
      const allSelected = flavorGroups.every(f => f.selected)
      if (!allSelected) {
        wx.showToast({ title: '请选择口味', icon: 'none' })
        return
      }
      const flavorStr = flavorGroups.map(f => `${f.name}:${f.selected}`).join(',')
      this._addToCart(flavorStr)
      this.setData({ showFlavor: false })
    },

    onFlavorClose() {
      this.setData({ showFlavor: false })
    },

    onPopupTap() {
      // 阻止事件冒泡到遮罩层，防止弹窗关闭
    },

    _addToCart(dishFlavor) {
      const { dish } = this.data
      const data = {}
      if (dish.dishId) {
        data.dishId = dish.dishId
      } else if (dish.setmealId) {
        data.setmealId = dish.setmealId
      } else {
        data.dishId = dish.id
      }
      if (dishFlavor) data.dishFlavor = dishFlavor
      addCart(data).then(() => {
        this.triggerEvent('cartchange')
      }).catch(() => {
        wx.showToast({ title: '添加失败', icon: 'none' })
      })
    },

    onSubtract() {
      const { dish, cartCount } = this.data
      if (cartCount <= 0) return
      const data = {}
      if (dish.dishId) {
        data.dishId = dish.dishId
      } else if (dish.setmealId) {
        data.setmealId = dish.setmealId
      } else {
        data.dishId = dish.id
      }
      subCart(data).then(() => {
        this.triggerEvent('cartchange')
      })
    },

    onAdd() {
      this.onAddToCart()
    },

    onShowDetail() {
      const { dish } = this.data
      this.triggerEvent('detail', { dish })
    }
  }
})
