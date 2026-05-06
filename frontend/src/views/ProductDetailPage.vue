<template>
  <ShopLayout>
    <a-row v-if="product" :gutter="24">
      <a-col :xs="24" :md="10">
        <div class="product-gallery">
          <img v-if="isImageUrl(activeImage)" class="product-detail-image" :src="activeImage" :alt="product.title" />
          <div v-else class="product-placeholder detail">{{ product.title.slice(0, 1) || 'K' }}</div>
          <div class="product-thumbs" v-if="realImages.length">
            <button v-for="image in realImages" :key="image" :class="{ active: image === activeImage }" @click="activeImage = image">
              <img :src="image" :alt="product.title" />
            </button>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :md="14">
        <a-card>
          <a-space>
            <a-tag color="red">{{ product.brand || 'KKMall 自营' }}</a-tag>
            <span class="muted">销量 {{ product.salesCount || 0 }}</span>
          </a-space>
          <h1>{{ product.title }}</h1>
          <p class="product-subtitle">{{ product.subtitle }}</p>
          <p>{{ product.description }}</p>
          <div class="selling-points" v-if="product.sellingPoints?.length">
            <a-tag v-for="point in product.sellingPoints" :key="point" color="volcano">{{ point }}</a-tag>
          </div>
          <a-descriptions v-if="product.attributes" size="small" bordered :column="2" style="margin:16px 0">
            <a-descriptions-item v-for="(value, key) in product.attributes" :key="String(key)" :label="String(key)">
              {{ value }}
            </a-descriptions-item>
          </a-descriptions>
          <a-space wrap>
            <a-button v-for="sku in product.skus" :key="sku.id" :type="sku.id === skuId ? 'primary' : 'default'" @click="skuId = sku.id">
              {{ sku.specName }}：{{ sku.specValue }} · {{ money(sku.price) }}
              <span v-if="sku.marketPrice && sku.marketPrice > sku.price" class="market-price">{{ money(sku.marketPrice) }}</span>
              · 库存 {{ sku.stock }}
            </a-button>
          </a-space>
          <a-divider />
          <a-space>
            <a-input-number v-model:value="quantity" :min="1" />
            <a-button type="primary" @click="addCart">加入购物车</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import ShopLayout from '../components/ShopLayout.vue'
import { mallApi, ProductDetail } from '../api/mall'
import { money } from '../utils/money'

const route = useRoute()
const router = useRouter()
const product = ref<ProductDetail>()
const skuId = ref<number>()
const quantity = ref(1)
const activeImage = ref('')
const realImages = computed(() => (product.value?.images || []).filter(isImageUrl))

async function load() {
  const data = await mallApi.product(Number(route.params.id))
  product.value = data
  skuId.value = data.skus[0]?.id
  activeImage.value = realImages.value[0] || data.mainImage || ''
}

async function addCart() {
  if (!skuId.value) return
  await mallApi.addCart(skuId.value, quantity.value)
  message.success('已加入购物车')
  router.push('/cart')
}

onMounted(load)

function isImageUrl(value?: string) {
  return Boolean(value && (/^https?:\/\//.test(value) || value.startsWith('/')))
}
</script>
