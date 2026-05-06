<template>
  <ShopLayout>
    <a-card v-if="order">
      <h2>订单详情</h2>
      <p>订单号：{{ order.orderNo }}</p>
      <p>状态：{{ order.status }}</p>
      <p>物流：{{ order.shipment ? `${order.shipment.logisticsCompany} ${order.shipment.trackingNo}` : '暂无物流' }}</p>
      <p class="price">应付金额：{{ money(order.payableAmount) }}</p>
      <a-list :data-source="order.items">
        <template #renderItem="{ item }"><a-list-item>{{ item.titleSnapshot }} {{ item.skuSnapshot }} {{ money(item.subtotal) }}</a-list-item></template>
      </a-list>
    </a-card>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ShopLayout from '../components/ShopLayout.vue'
import { mallApi, OrderDetail } from '../api/mall'
import { money } from '../utils/money'

const route = useRoute()
const order = ref<OrderDetail>()
onMounted(async () => { order.value = await mallApi.order(Number(route.params.id)) })
</script>
