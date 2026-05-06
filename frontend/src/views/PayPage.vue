<template>
  <ShopLayout>
    <a-card v-if="order" style="max-width:520px;margin:60px auto">
      <h2>模拟支付</h2>
      <p>订单号：{{ order.orderNo }}</p>
      <p>状态：{{ order.status }}</p>
      <p class="price">应付金额：{{ money(order.payableAmount) }}</p>
      <a-button type="primary" block @click="pay">确认模拟支付</a-button>
    </a-card>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ShopLayout from '../components/ShopLayout.vue'
import { mallApi, OrderDetail } from '../api/mall'
import { money } from '../utils/money'

const route = useRoute()
const router = useRouter()
const order = ref<OrderDetail>()
async function load() { order.value = await mallApi.order(Number(route.params.id)) }
async function pay() { await mallApi.pay(Number(route.params.id)); router.push(`/orders/${route.params.id}`) }
onMounted(load)
</script>
