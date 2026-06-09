const { getShopBusinessStatus, getShopInfo, getCategoryList, getDishList, getSetmealList, getCartList, addCart, subCart, cleanCart } = require('../../utils/api')

Component({
  data: {
    shopStatus: 1,
    shopName: '',
    shopId: '',
    shopImage: '',
    categories: [],
    activeCategoryIdx: 0,
    dishes: [],
    cartItems: [],
    cartTotalCount: 0,
    cartTotalAmount: 0,
    loading: true,
    dishType: 1,
    showCartPanel: false
  },

  lifetimes: {
    attached() {
      this.checkShop()
    }
  },

  pageLifetimes: {
    show() {
      this.checkShop()
    }
  },

  methods: {
    checkShop() {
      const app = getApp()
      const shopId = app.globalData.shopId
      if (!shopId) {
        wx.navigateTo({ url: '/pages/shop/list/list' })
        return
      }
      this.setData({ shopId, shopName: app.globalData.shopName || '外卖店铺' })
      this.init()
    },

    async init() {
      try {
        const status = await getShopBusinessStatus(this.data.shopId)
        this.setData({ shopStatus: status })
        if (status !== 1) {
          wx.showToast({ title: '店铺已打烊', icon: 'none' })
        }
      } catch (e) {
        console.error('获取店铺状态失败', e)
      }

      // 加载店铺图片
      try {
        const shop = await getShopInfo(this.data.shopId)
        if (shop && shop.image) {
          this.setData({ shopImage: shop.image })
        }
      } catch (e) {
        console.error('获取店铺信息失败', e)
      }

      await this.loadCategories()
      await this.loadDishes()
      this.loadCart()
    },

    async loadCategories() {
      try {
        const list = await getCategoryList(1)
        this.setData({ categories: list || [] })
      } catch (e) {
        console.error('获取分类失败', e)
      }
    },

    async loadDishes() {
      const { categories, activeCategoryIdx, dishType } = this.data
      if (categories.length === 0) {
        this.setData({ dishes: [], loading: false })
        return
      }
      this.setData({ loading: true })
      const categoryId = categories[activeCategoryIdx].id
      try {
        let list
        if (dishType === 1) {
          list = await getDishList(categoryId)
        } else {
          list = await getSetmealList(categoryId)
        }
        this.setData({ dishes: list || [], loading: false })
      } catch (e) {
        this.setData({ dishes: [], loading: false })
        console.error('获取菜品失败', e)
      }
    },

    async loadCart() {
      try {
        const list = await getCartList()
        const cartItems = list || []
        let totalCount = 0
        let totalAmount = 0
        cartItems.forEach(item => {
          totalCount += item.number
          totalAmount += item.amount * item.number
        })
        // 同步菜品列表上的购物车数量
        const dishes = this.data.dishes.map(dish => {
          const cartItem = cartItems.find(c =>
            (c.dishId && c.dishId === dish.id) || (c.setmealId && c.setmealId === dish.id)
          )
          return { ...dish, _cartCount: cartItem ? cartItem.number : 0 }
        })
        this.setData({ dishes, cartItems, cartTotalCount: totalCount, cartTotalAmount: totalAmount })
      } catch (e) {
        console.error('获取购物车失败', e)
      }
    },

    onCategoryTap(e) {
      const idx = e.currentTarget.dataset.idx
      if (idx === this.data.activeCategoryIdx) return
      this.setData({ activeCategoryIdx: idx })
      this.loadDishes()
    },

    onDishTypeChange(e) {
      const type = e.currentTarget.dataset.type
      if (type === this.data.dishType) return
      this.setData({ dishType: type, activeCategoryIdx: 0 })
      this.loadCategories().then(() => this.loadDishes())
    },

    onCartChange() {
      this.loadCart()
    },

    // 购物车面板
    onToggleCartPanel() {
      if (this.data.cartTotalCount === 0) return
      this.setData({ showCartPanel: !this.data.showCartPanel })
    },

    onCloseCartPanel() {
      this.setData({ showCartPanel: false })
    },

    onPanelAdd(e) {
      const { dishid, setmealid, flavor } = e.currentTarget.dataset
      const data = {}
      if (dishid) data.dishId = dishid
      if (setmealid) data.setmealId = setmealid
      if (flavor) data.dishFlavor = flavor
      addCart(data).then(() => this.loadCart())
    },

    onPanelSub(e) {
      const { dishid, setmealid, flavor } = e.currentTarget.dataset
      const data = {}
      if (dishid) data.dishId = dishid
      if (setmealid) data.setmealId = setmealid
      if (flavor) data.dishFlavor = flavor
      subCart(data).then(() => {
        this.loadCart()
        if (this.data.cartTotalCount <= 0) {
          this.setData({ showCartPanel: false })
        }
      })
    },

    onPanelDelete(e) {
      const { dishid, setmealid, flavor, number } = e.currentTarget.dataset
      const data = {}
      if (dishid) data.dishId = dishid
      if (setmealid) data.setmealId = setmealid
      if (flavor) data.dishFlavor = flavor
      let chain = Promise.resolve()
      for (let i = 0; i < number; i++) {
        chain = chain.then(() => subCart(data))
      }
      chain.then(() => {
        this.loadCart().then(() => {
          if (this.data.cartTotalCount <= 0) {
            this.setData({ showCartPanel: false })
          }
        })
      })
    },

    onCartClean() {
      wx.showModal({
        title: '提示',
        content: '确定清空购物车吗？',
        success: (res) => {
          if (res.confirm) {
            cleanCart().then(() => {
              this.loadCart()
              this.setData({ showCartPanel: false })
            })
          }
        }
      })
    },

    onCheckout() {
      this.setData({ showCartPanel: false })
      wx.navigateTo({ url: '/pages/order/confirm/confirm' })
    },

    onSwitchShop() {
      wx.navigateTo({ url: '/pages/shop/list/list?switch=1' })
    },

    getCartCount(dishId) {
      const item = this.data.cartItems.find(c => c.dishId === dishId)
      return item ? item.number : 0
    }
  }
})
