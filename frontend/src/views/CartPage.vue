<template>
  <ShopLayout>
    <a-row :gutter="16">
      <a-col :xs="24" :md="16">
        <a-list :data-source="cart.items">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta :title="item.title" :description="item.specText" />
              <a-space>
                <span class="price">{{ money(item.price) }}</span>
                <a-input-number :value="item.quantity" :min="1" @change="(v: number | null) => update(item.id, Number(v || 1))" />
                <a-button danger @click="remove(item.id)">删除</a-button>
              </a-space>
            </a-list-item>
          </template>
        </a-list>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card>
          <h2>结算</h2>
          <p>商品金额：{{ money(cart.productAmount) }}</p>
          <p>运费规则：满 99 元包邮</p>
          <router-link to="/checkout"><a-button type="primary" block>去结算</a-button></router-link>
        </a-card>
      </a-col>
    </a-row>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ShopLayout from '../components/ShopLayout.vue'
import { CartView, mallApi } from '../api/mall'
import { money } from '../utils/money'

const cart = ref<CartView>({ items: [], productAmount: 0 })
async function load() { cart.value = await mallApi.cart() }
async function update(id: number, quantity: number) { await mallApi.updateCart(id, quantity); await load() }
async function remove(id: number) { await mallApi.deleteCart(id); await load() }
onMounted(load)
</script>
