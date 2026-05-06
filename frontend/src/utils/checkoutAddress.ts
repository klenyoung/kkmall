import { Address } from '../api/mall'

export interface CheckoutAddressForm {
  receiverName: string
  receiverPhone: string
  region: string
  detail: string
  isDefault: boolean
}

export function checkoutAddressSelection(addresses: Address[], preferredId?: number): { addressId?: number; form: CheckoutAddressForm } {
  const preferred = preferredId ? addresses.find((item) => item.id === preferredId) : undefined
  const defaultAddress = addresses.find((item) => item.isDefault === true || item.isDefault === 1)
  const selected = preferred || defaultAddress || addresses[0]
  return {
    addressId: selected?.id,
    form: {
      receiverName: selected?.receiverName || '',
      receiverPhone: selected?.receiverPhone || '',
      region: selected?.region || '',
      detail: selected?.detail || '',
      isDefault: selected ? selected.isDefault === true || selected.isDefault === 1 : true
    }
  }
}
