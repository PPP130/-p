const { getAddressList, deleteAddress, setDefaultAddress } = require('../../../utils/api')

Component({
  data: {
    addresses: [],
    selectMode: false // 是否从订单确认页进入的选择模式
  },

  lifetimes: {
    attached() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      if (page.options && page.options.select === '1') {
        this.setData({ selectMode: true })
      }
      this.loadAddresses()
    }
  },

  pageLifetimes: {
    show() {
      this.loadAddresses()
    }
  },

  methods: {
    async loadAddresses() {
      try {
        const list = await getAddressList()
        this.setData({ addresses: list || [] })
      } catch (e) {
        console.error('获取地址列表失败', e)
      }
    },

    onSelect(e) {
      if (!this.data.selectMode) return
      const id = e.currentTarget.dataset.id
      const address = this.data.addresses.find(a => a.id === id)
      // 通过 eventChannel 回传选中的地址
      const eventChannel = this.getOpenerEventChannel()
      if (eventChannel) {
        eventChannel.emit('selectAddress', address)
      }
      wx.navigateBack()
    },

    onAdd() {
      wx.navigateTo({ url: '/pages/address/edit/edit' })
    },

    onEdit(e) {
      const id = e.currentTarget.dataset.id
      wx.navigateTo({ url: `/pages/address/edit/edit?id=${id}` })
    },

    onDelete(e) {
      const id = e.currentTarget.dataset.id
      wx.showModal({
        title: '提示',
        content: '确定删除该地址吗？',
        success: (res) => {
          if (res.confirm) {
            deleteAddress(id).then(() => {
              wx.showToast({ title: '已删除', icon: 'success' })
              this.loadAddresses()
            })
          }
        }
      })
    },

    onSetDefault(e) {
      const id = e.currentTarget.dataset.id
      setDefaultAddress({ id }).then(() => {
        wx.showToast({ title: '已设为默认', icon: 'success' })
        this.loadAddresses()
      })
    }
  }
})
