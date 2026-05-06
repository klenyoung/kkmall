<template>
  <ShopLayout>
    <section class="hero">
      <h1>今日精选好物，满 99 元包邮</h1>
      <p>企业级前后端分离商城，商品、购物车、订单与后台统一联调。</p>
    </section>
    <a-tabs v-model:active-key="categoryId" @change="load">
      <a-tab-pane key="" tab="全部" />
      <a-tab-pane v-for="item in categories" :key="String(item.id)" :tab="item.name" />
    </a-tabs>
    <a-row :gutter="[16,16]">
      <a-col v-for="item in products" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6">
        <router-link :to="`/products/${item.id}`">
          <a-card hoverable class="product-card">
            <img v-if="isImageUrl(item.coverImage)" class="product-image" :src="item.coverImage" :alt="item.title" />
            <div v-else class="product-placeholder">{{ item.title.slice(0, 1) || 'K' }}</div>
            <a-card-meta :title="item.title">
              <template #description>
                <div class="product-card-desc">
                  <span>{{ item.brand || 'KKMall 自营' }}</span>
                  <span>{{ item.subtitle || '精选商城好物' }}</span>
                  <strong class="price">{{ money(item.minPrice) }}</strong>
                </div>
              </template>
            </a-card-meta>
          </a-card>
        </router-link>
      </a-col>
    </a-row>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ShopLayout from '../components/ShopLayout.vue'
import { Category, mallApi, ProductCard } from '../api/mall'
import { money } from '../utils/money'

const route = useRoute()
const categories = ref<Category[]>([])
const products = ref<ProductCard[]>([])
const categoryId = ref('')

async function load() {
  categories.value = await mallApi.categories()
  const data = await mallApi.products({ categoryId: categoryId.value || undefined, keyword: route.query.keyword })
  products.value = data.items
}

watch(() => route.query.keyword, load)
onMounted(load)

function isImageUrl(value?: string) {
  return Boolean(value && (/^https?:\/\//.test(value) || value.startsWith('/')))
}
</script>
