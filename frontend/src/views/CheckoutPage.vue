<template>
  <ShopLayout>
    <a-row :gutter="16">
      <a-col :xs="24" :md="14">
        <a-card title="收货地址">
          <a-select
            v-model:value="addressId"
            style="width:100%;margin-bottom:16px"
            placeholder="选择地址"
            @change="syncSelectedAddress"
          >
            <a-select-option v-for="item in addresses" :key="item.id" :value="item.id">
              {{ item.receiverName }} {{ item.receiverPhone }} {{ item.region }}{{ item.detail }}
            </a-select-option>
          </a-select>
          <a-form layout="vertical">
            <a-form-item label="收货人"><a-input v-model:value="form.receiverName" /></a-form-item>
            <a-form-item label="手机号"><a-input v-model:value="form.receiverPhone" /></a-form-item>
            <a-form-item label="省市区"><a-input v-model:value="form.region" /></a-form-item>
            <a-form-item label="详细地址"><a-input v-model:value="form.detail" /></a-form-item>
            <a-button @click="saveAddress">保存地址</a-button>
          </a-form>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="10">
        <a-card title="订单金额">
          <p>商品金额：{{ money(cart.productAmount) }}</p>
          <p>运费：{{ money(shippingFee) }}</p>
          <p class="price">应付：{{ money(cart.productAmount + shippingFee) }}</p>
          <a-button type="primary" block @click="submit">提交订单</a-button>
        </a-card>
      </a-col>
    </a-row>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import ShopLayout from '../components/ShopLayout.vue'
import { Address, CartView, mallApi } from '../api/mall'
import { checkoutAddressSelection } from '../utils/checkoutAddress'
import { money } from '../utils/money'

const router = useRouter()
const cart = ref<CartView>({ items: [], productAmount: 0 })
const addresses = ref<Address[]>([])
const addressId = ref<number>()
const form = reactive({ receiverName: '', receiverPhone: '', region: '', detail: '', isDefault: true })
const shippingFee = computed(() => cart.value.productAmount >= 9900 ? 0 : 1000)

async function load() {
  cart.value = await mallApi.cart()
  addresses.value = await mallApi.addresses()
  const selection = checkoutAddressSelection(addresses.value)
  addressId.value = selection.addressId
  Object.assign(form, selection.form)
}

function syncSelectedAddress(selectedId?: number) {
  const selection = checkoutAddressSelection(addresses.value, selectedId || addressId.value)
  addressId.value = selection.addressId
  Object.assign(form, selection.form)
}

async function saveAddress() {
  await mallApi.createAddress(form)
  message.success('地址已保存')
  await load()
}

async function submit() {
  if (!addressId.value) return message.warning('请先选择或保存地址')
  const order = await mallApi.createOrder(addressId.value, cart.value.items.map((item) => item.id)) as any
  router.push(`/pay/${order.id}`)
}

onMounted(load)
</script>
