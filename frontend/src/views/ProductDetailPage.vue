<template>
  <ShopLayout>
    <a-spin :spinning="loading">
      <a-result v-if="error" status="warning" title="商品暂时无法查看" :sub-title="error">
        <template #extra><a-button type="primary" @click="load">重新加载</a-button></template>
      </a-result>

      <div v-else-if="product" class="tmall-detail-page">
        <section class="detail-hero-panel">
          <div class="detail-gallery">
            <img v-if="isImageUrl(activeImage) && !brokenImages.has(activeImage)" class="detail-main-image" :src="activeImage" :alt="product.title" @error="brokenImages.add(activeImage)" />
            <div v-else class="product-placeholder detail">{{ product.title.slice(0, 1) || 'K' }}</div>
            <div class="detail-thumbs" v-if="galleryImages.length">
              <button v-for="image in galleryImages" :key="image" :class="{ active: image === activeImage }" @click="activeImage = image">
                <img :src="image" :alt="product.title" @error="brokenImages.add(image)" />
              </button>
            </div>
          </div>

          <div class="detail-buy-panel">
            <div class="detail-meta-row">
              <a-tag color="red">{{ product.brand || 'KKMall 自营' }}</a-tag>
              <span>已售 {{ product.salesCount || 0 }} 件</span>
              <span v-if="selectedSku?.stock !== undefined">{{ selectedSku.stock > 0 ? '有货' : '暂时缺货' }}</span>
            </div>
            <h1>{{ product.title }}</h1>
            <p class="detail-subtitle">{{ product.subtitle || product.description }}</p>

            <div class="detail-price-box">
              <span class="price-label">店铺优惠后</span>
              <strong>{{ money(displayPrice) }}</strong>
              <del v-if="displayOriginPrice > displayPrice">{{ money(displayOriginPrice) }}</del>
              <a-tag v-for="promo in promotions" :key="promo.title" color="volcano">{{ promo.label || promo.title }}</a-tag>
            </div>

            <div class="detail-service-line">
              <span v-for="promise in servicePromises" :key="promise.title">{{ promise.title }}</span>
              <span v-if="!servicePromises.length">满 99 包邮 · 售后请联系客服</span>
            </div>

            <div v-if="product.sellingPoints?.length" class="selling-points">
              <a-tag v-for="point in product.sellingPoints" :key="point" color="red">{{ point }}</a-tag>
            </div>

            <div class="detail-section">
              <div class="section-label">规格</div>
              <div class="sku-grid">
                <button
                  v-for="sku in product.skus"
                  :key="sku.id"
                  class="sku-option"
                  :class="{ active: sku.id === skuId, disabled: !isSkuSellable(sku) }"
                  :disabled="!isSkuSellable(sku)"
                  @click="selectSku(sku)"
                >
                  <img v-if="isImageUrl(sku.imageUrl)" :src="sku.imageUrl" :alt="sku.specValue" />
                  <span>{{ skuLabel(sku) }}</span>
                  <small>{{ money(sku.price) }} · 库存 {{ sku.stock }}</small>
                </button>
              </div>
            </div>

            <div class="detail-section quantity-row">
              <div class="section-label">数量</div>
              <a-input-number v-model:value="quantity" :min="1" :max="quantityMax" />
              <span class="muted">库存 {{ quantityMax }} {{ product.unit || '件' }}</span>
            </div>

            <div class="detail-actions">
              <a-button size="large" :disabled="!canBuy" :loading="addingCart" @click="addCart">加入购物车</a-button>
              <a-button size="large" type="primary" :disabled="!canBuy" :loading="buyingNow" @click="buyNow">立即购买</a-button>
            </div>
          </div>
        </section>

        <section class="detail-content-layout">
          <div class="detail-tabs-panel">
            <a-tabs>
              <a-tab-pane key="reviews" tab="用户评价">
                <div class="review-summary">
                  <strong>{{ reviewSummary.reviewCount }}</strong>
                  <span>条评价 · 好评率 {{ reviewSummary.goodRate }}% · 平均 {{ reviewSummary.averageRating }} 分</span>
                  <a-tag v-for="tag in reviewSummary.tags" :key="tag" color="red">{{ tag }}</a-tag>
                </div>
                <a-empty v-if="!reviews.length" description="暂无评价，购买后可以分享使用体验" />
                <div v-for="review in reviews" :key="review.id || review.content" class="review-item">
                  <div class="review-avatar">{{ review.userNickname.slice(0, 1) }}</div>
                  <div>
                    <strong>{{ review.userNickname }}</strong>
                    <a-rate :value="review.rating" disabled allow-half />
                    <p>{{ review.content }}</p>
                    <div class="review-tags"><a-tag v-for="tag in review.tags" :key="tag">{{ tag }}</a-tag></div>
                  </div>
                </div>
              </a-tab-pane>
              <a-tab-pane key="params" tab="参数信息">
                <a-descriptions v-if="parameters.length" bordered :column="{ xs: 1, md: 2 }">
                  <a-descriptions-item v-for="param in parameters" :key="param.name" :label="param.name">{{ param.value }}</a-descriptions-item>
                </a-descriptions>
                <a-empty v-else description="暂无参数信息" />
              </a-tab-pane>
              <a-tab-pane key="detail" tab="图文详情">
                <div v-if="product.detailHtml" class="rich-detail" v-html="product.detailHtml"></div>
                <a-empty v-else description="暂无图文详情" />
              </a-tab-pane>
              <a-tab-pane key="notice" tab="购买须知">
                <a-list :data-source="servicePromises">
                  <template #renderItem="{ item }">
                    <a-list-item><a-list-item-meta :title="item.title" :description="item.description" /></a-list-item>
                  </template>
                </a-list>
              </a-tab-pane>
            </a-tabs>
          </div>

          <aside class="recommend-panel">
            <RecommendationBlock title="本店推荐" :items="storeRecommendations" />
            <RecommendationBlock title="看了又看" :items="relatedRecommendations" />
          </aside>
        </section>

        <div class="mobile-buy-bar">
          <div>
            <strong>{{ money(displayPrice) }}</strong>
            <span>{{ selectedSku ? skuLabel(selectedSku) : '请选择规格' }}</span>
          </div>
          <a-button :disabled="!canBuy" :loading="addingCart" @click="addCart">加购</a-button>
          <a-button type="primary" :disabled="!canBuy" :loading="buyingNow" @click="buyNow">立即购买</a-button>
        </div>
      </div>
    </a-spin>
  </ShopLayout>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import ShopLayout from '../components/ShopLayout.vue'
import { mallApi, ProductCard, ProductDetail, Sku } from '../api/mall'
import { useAuthStore } from '../stores/auth'
import { money } from '../utils/money'

const RecommendationBlock = defineComponent({
  props: { title: { type: String, required: true }, items: { type: Array as () => ProductCard[], default: () => [] } },
  setup(props) {
    return () => h('div', { class: 'recommend-block' }, [
      h('h3', props.title),
      props.items.length
        ? props.items.map(item => h(RouterLink, { to: `/products/${item.id}`, class: 'recommend-item' }, () => [
          item.coverImage || item.mainImage ? h('img', { src: item.coverImage || item.mainImage, alt: item.title }) : h('div', { class: 'product-placeholder tiny' }, item.title?.slice(0, 1) || 'K'),
          h('span', item.title),
          h('strong', money(item.minPrice))
        ]))
        : h('div', { class: 'recommend-empty' }, '暂无推荐')
    ])
  }
})

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref<ProductDetail>()
const skuId = ref<number>()
const quantity = ref(1)
const activeImage = ref('')
const loading = ref(false)
const error = ref('')
const addingCart = ref(false)
const buyingNow = ref(false)
const brokenImages = ref(new Set<string>())

const galleryImages = computed(() => {
  const images = product.value?.imageUrls?.length ? product.value.imageUrls : product.value?.images || []
  return images.filter(isImageUrl)
})
const selectedSku = computed(() => product.value?.skus.find(sku => sku.id === skuId.value))
const displayPrice = computed(() => selectedSku.value?.price || product.value?.priceRange?.minPrice || product.value?.minPrice || 0)
const displayOriginPrice = computed(() => selectedSku.value?.originPrice || selectedSku.value?.marketPrice || displayPrice.value)
const quantityMax = computed(() => Math.max(1, selectedSku.value?.stock || 0))
const canBuy = computed(() => Boolean(product.value && selectedSku.value && isSkuSellable(selectedSku.value) && quantity.value <= quantityMax.value))
const promotions = computed(() => product.value?.promotions || [])
const servicePromises = computed(() => product.value?.servicePromises || [])
const parameters = computed(() => product.value?.parameters?.length ? product.value.parameters : Object.entries(product.value?.attributes || {}).map(([name, value]) => ({ name, value: String(value) })))
const reviewSummary = computed(() => product.value?.reviewSummary || { reviewCount: 0, goodRate: 0, averageRating: 0, tags: [] })
const reviews = computed(() => product.value?.reviews || [])
const storeRecommendations = computed(() => product.value?.storeRecommendations || [])
const relatedRecommendations = computed(() => product.value?.relatedRecommendations || [])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await mallApi.product(Number(route.params.id))
    product.value = data
    const firstSellable = data.skus.find(isSkuSellable) || data.skus[0]
    skuId.value = firstSellable?.id
    activeImage.value = firstSellable?.imageUrl || galleryImages.value[0] || data.mainImageUrl || data.mainImage || ''
  } catch (err: any) {
    error.value = err.message || '商品加载失败'
  } finally {
    loading.value = false
  }
}

function selectSku(sku: Sku) {
  skuId.value = sku.id
  quantity.value = 1
  if (isImageUrl(sku.imageUrl)) activeImage.value = sku.imageUrl || ''
}

async function addCart() {
  if (!(await ensureLogin())) return
  if (!selectedSku.value?.id) return message.warning('请先选择可购买规格')
  addingCart.value = true
  try {
    await mallApi.addCart(selectedSku.value.id, quantity.value)
    message.success('已加入购物车')
  } finally {
    addingCart.value = false
  }
}

async function buyNow() {
  if (!(await ensureLogin())) return
  if (!selectedSku.value?.id) return message.warning('请先选择可购买规格')
  buyingNow.value = true
  try {
    await mallApi.addCart(selectedSku.value.id, quantity.value)
    router.push('/checkout')
  } finally {
    buyingNow.value = false
  }
}

async function ensureLogin() {
  if (auth.userToken) return true
  message.warning('请先登录后再购买')
  router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
  return false
}

function skuLabel(sku: Sku) {
  const specs = sku.specs || { [sku.specName]: sku.specValue }
  return Object.entries(specs).map(([key, value]) => `${key}：${value}`).join(' / ')
}

function isSkuSellable(sku: Sku) {
  return sku.sellable !== false && sku.enabled !== false && sku.enabled !== 0 && sku.stock > 0
}

function isImageUrl(value?: string) {
  return Boolean(value && (/^https?:\/\//.test(value) || value.startsWith('/')))
}

watch(quantityMax, max => {
  if (quantity.value > max) quantity.value = max
})

onMounted(load)
</script>
