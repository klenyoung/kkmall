import { describe, expect, it } from 'vitest'
import { checkoutAddressSelection } from './checkoutAddress'
import { Address } from '../api/mall'

describe('checkoutAddressSelection', () => {
  it('selects the default address and copies it into the checkout form', () => {
    const addresses: Address[] = [
      { id: 1, receiverName: '李四', receiverPhone: '13900139000', region: '北京市 朝阳区', detail: '建国路 1 号', isDefault: false },
      { id: 2, receiverName: '张三', receiverPhone: '13800138000', region: '上海市 浦东新区', detail: '世纪大道 100 号', isDefault: true }
    ]

    const selection = checkoutAddressSelection(addresses)

    expect(selection.addressId).toBe(2)
    expect(selection.form).toEqual({
      receiverName: '张三',
      receiverPhone: '13800138000',
      region: '上海市 浦东新区',
      detail: '世纪大道 100 号',
      isDefault: true
    })
  })
})
