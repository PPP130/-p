const { getAddressById, saveAddress, updateAddress } = require('../../../utils/api')

Component({
  data: {
    isEdit: false,
    addressId: null,
    form: {
      consignee: '',
      phone: '',
      sex: 0, // 0=先生 1=女士
      provinceCode: '',
      provinceName: '',
      cityCode: '',
      cityName: '',
      districtCode: '',
      districtName: '',
      detail: '',
      label: '',
      isDefault: 0
    },
    region: [],
    labelOptions: ['家', '公司', '学校']
  },

  lifetimes: {
    attached() {
      const pages = getCurrentPages()
      const page = pages[pages.length - 1]
      const id = page.options && page.options.id
      if (id) {
        this.setData({ isEdit: true, addressId: id })
        this.loadAddress(id)
      }
    }
  },

  methods: {
    async loadAddress(id) {
      try {
        const address = await getAddressById(id)
        this.setData({
          form: {
            consignee: address.consignee || '',
            phone: address.phone || '',
            sex: address.sex || 0,
            provinceCode: address.provinceCode || '',
            provinceName: address.provinceName || '',
            cityCode: address.cityCode || '',
            cityName: address.cityName || '',
            districtCode: address.districtCode || '',
            districtName: address.districtName || '',
            detail: address.detail || '',
            label: address.label || '',
            isDefault: address.isDefault || 0
          },
          region: [address.provinceName, address.cityName, address.districtName].filter(Boolean)
        })
      } catch (e) {
        console.error('获取地址失败', e)
      }
    },

    onInput(e) {
      const field = e.currentTarget.dataset.field
      this.setData({ [`form.${field}`]: e.detail.value })
    },

    onSexChange(e) {
      this.setData({ 'form.sex': Number(e.currentTarget.dataset.sex) })
    },

    onRegionChange(e) {
      const [provinceName, cityName, districtName] = e.detail.value
      this.setData({
        region: e.detail.value,
        'form.provinceName': provinceName,
        'form.cityName': cityName,
        'form.districtName': districtName
      })
    },

    onLabelTap(e) {
      const label = e.currentTarget.dataset.label
      this.setData({ 'form.label': this.data.form.label === label ? '' : label })
    },

    onDefaultChange(e) {
      this.setData({ 'form.isDefault': e.detail.value ? 1 : 0 })
    },

    async onSubmit() {
      const { form, isEdit, addressId } = this.data
      if (!form.consignee) {
        wx.showToast({ title: '请输入收货人', icon: 'none' })
        return
      }
      if (!form.phone) {
        wx.showToast({ title: '请输入手机号', icon: 'none' })
        return
      }
      if (!form.detail) {
        wx.showToast({ title: '请输入详细地址', icon: 'none' })
        return
      }

      try {
        if (isEdit) {
          await updateAddress({ ...form, id: addressId })
          wx.showToast({ title: '修改成功', icon: 'success' })
        } else {
          await saveAddress(form)
          wx.showToast({ title: '添加成功', icon: 'success' })
        }
        setTimeout(() => wx.navigateBack(), 1500)
      } catch (e) {
        console.error('保存地址失败', e)
      }
    }
  }
})
