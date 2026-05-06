<template>
  <ShopLayout>
    <a-list :data-source="orders">
      <template #renderItem="{ item }">
        <router-link :to="`/orders/${item.id}`">
          <a-list-item>
            <a-list-item-meta :title="item.orderNo" :description="item.status" />
            <span class="price">{{ money(item.payableAmount) }}</span>
          </a-list-item>
        </router-link>
      </template>
    </a-list>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ShopLayout from '../components/ShopLayout.vue'
import { mallApi, OrderSummary } from '../api/mall'
import { money } from '../utils/money'

const orders = ref<OrderSummary[]>([])
onMounted(async () => { orders.value = (await mallApi.orders()).items })
</script>
