<template>
  <a-layout class="admin-console">
    <a-layout-sider class="admin-sider" width="220">
      <div class="admin-brand">KKMall</div>
      <a-menu v-model:selectedKeys="selectedMenu" theme="dark" mode="inline">
        <a-menu-item key="dashboard"><DashboardOutlined />工作台</a-menu-item>
        <a-menu-item key="products"><ShopOutlined />商品管理</a-menu-item>
        <a-menu-item key="orders"><FileTextOutlined />订单管理</a-menu-item>
        <a-menu-item key="shipments"><CarOutlined />发货管理</a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="admin-header">
        <div>
          <h2>{{ currentTitle }}</h2>
          <p>单商家自营商城运营台</p>
        </div>
        <a-dropdown :trigger="['click']">
          <a-button class="admin-user">
            <template #icon><UserOutlined /></template>
            管理员 {{ adminLabel }}
          </a-button>
          <template #overlay>
            <a-menu @click="handleAdminMenu">
              <a-menu-item key="logout">退出登录</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>

      <a-layout-content class="admin-content">
        <section v-if="activeMenu === 'dashboard'" class="admin-section">
          <a-row :gutter="[16,16]">
            <a-col :xs="24" :md="8"><a-card><a-statistic title="商品数" :value="products.length" /></a-card></a-col>
            <a-col :xs="24" :md="8"><a-card><a-statistic title="待发货订单" :value="pendingShipmentCount" /></a-card></a-col>
            <a-col :xs="24" :md="8"><a-card><a-statistic title="在售商品" :value="onSaleCount" /></a-card></a-col>
          </a-row>
        </section>

        <section v-if="activeMenu === 'products'" class="admin-section">
          <a-card title="商品管理">
            <template #extra><a-button type="primary" @click="openCreateProduct">新增商品</a-button></template>
            <a-table :data-source="products" :pagination="false" row-key="id" :scroll="{ x: 980 }">
              <a-table-column title="主图" width="92">
                <template #default="{ record }">
                  <img v-if="record.coverImage" class="admin-thumb" :src="assetUrl(record.coverImage)" alt="商品主图" />
                  <div v-else class="product-placeholder tiny">{{ record.title?.slice(0, 1) || 'K' }}</div>
                </template>
              </a-table-column>
              <a-table-column title="商品">
                <template #default="{ record }">
                  <strong>{{ record.title }}</strong>
                  <p class="muted">{{ record.subtitle || record.brand }}</p>
                </template>
              </a-table-column>
              <a-table-column title="品牌" data-index="brand" width="120" />
              <a-table-column title="价格" width="110"><template #default="{ record }">{{ money(record.minPrice) }}</template></a-table-column>
              <a-table-column title="库存" data-index="totalStock" width="90" />
              <a-table-column title="销量" data-index="salesCount" width="90" />
              <a-table-column title="状态" width="110">
                <template #default="{ record }"><a-tag :color="record.status === 'ON_SALE' ? 'green' : 'default'">{{ record.status }}</a-tag></template>
              </a-table-column>
              <a-table-column title="操作" fixed="right" width="220">
                <template #default="{ record }">
                  <a-space>
                    <a-button size="small" @click="editProduct(record.id)">编辑</a-button>
                    <a-button size="small" @click="editDetailConfig(record.id)">详情配置</a-button>
                    <a-button size="small" @click="toggle(record)">{{ record.status === 'ON_SALE' ? '下架' : '上架' }}</a-button>
                  </a-space>
                </template>
              </a-table-column>
            </a-table>
          </a-card>

          <a-drawer v-model:open="productDrawerOpen" :title="editingId ? '编辑商品' : '新增商品'" width="820" class="product-editor-drawer" destroy-on-close>
            <a-form layout="vertical">
              <div class="form-section">
                <h3>基础信息</h3>
                <a-row :gutter="16">
                  <a-col :xs="24" :md="12"><a-form-item label="商品标题"><a-input v-model:value="productForm.title" /></a-form-item></a-col>
                  <a-col :xs="24" :md="12"><a-form-item label="副标题"><a-input v-model:value="productForm.subtitle" /></a-form-item></a-col>
                  <a-col :xs="24" :md="12"><a-form-item label="品牌"><a-input v-model:value="productForm.brand" /></a-form-item></a-col>
                  <a-col :xs="24" :md="12"><a-form-item label="分类"><a-select v-model:value="productForm.categoryId" :options="categoryOptions" /></a-form-item></a-col>
                  <a-col :xs="24" :md="12"><a-form-item label="单位"><a-input v-model:value="productForm.unit" /></a-form-item></a-col>
                  <a-col :xs="24" :md="12"><a-form-item label="状态"><a-select v-model:value="productForm.status" :options="statusOptions" /></a-form-item></a-col>
                </a-row>
              </div>

              <div class="form-section">
                <h3>商品图片</h3>
                <a-upload :show-upload-list="false" :custom-request="uploadImage">
                  <a-button :loading="uploading"><UploadOutlined /> 上传商品图片</a-button>
                </a-upload>
                <div class="admin-image-list">
                  <div v-for="(image, index) in productForm.images" :key="image" class="admin-image-item">
                    <img :src="assetUrl(image)" alt="商品图" />
                    <button type="button" @click="removeImage(index)">删除</button>
                  </div>
                  <div v-if="!productForm.images.length" class="product-placeholder small">K</div>
                </div>
              </div>

              <div class="form-section">
                <h3>详情内容</h3>
                <a-form-item label="卖点，一行一个"><a-textarea v-model:value="sellingPointsText" :rows="2" /></a-form-item>
                <a-form-item label="商品描述"><a-textarea v-model:value="productForm.description" :rows="2" /></a-form-item>
                <a-form-item label="图文详情 HTML/文本"><a-textarea v-model:value="productForm.detailHtml" :rows="5" /></a-form-item>
                <a-form-item label="基础属性，格式：属性=值"><a-textarea v-model:value="attributesText" :rows="3" /></a-form-item>
              </div>

              <div class="form-section">
                <div class="section-title-row">
                  <h3>SKU 信息</h3>
                  <a-button size="small" @click="addSku">添加 SKU</a-button>
                </div>
                <div v-for="(sku, index) in productForm.skus" :key="index" class="sku-editor">
                  <a-row :gutter="8">
                    <a-col :xs="24" :md="8"><a-input v-model:value="sku.specName" placeholder="规格名" /></a-col>
                    <a-col :xs="24" :md="8"><a-input v-model:value="sku.specValue" placeholder="规格值" /></a-col>
                    <a-col :xs="24" :md="8"><a-input v-model:value="sku.skuCode" placeholder="SKU 编码" /></a-col>
                    <a-col :xs="24" :md="8"><a-input-number v-model:value="sku.price" :min="0" placeholder="售价(分)" style="width:100%;margin-top:8px" /></a-col>
                    <a-col :xs="24" :md="8"><a-input-number v-model:value="sku.marketPrice" :min="0" placeholder="划线价(分)" style="width:100%;margin-top:8px" /></a-col>
                    <a-col :xs="24" :md="8"><a-input-number v-model:value="sku.stock" :min="0" placeholder="库存" style="width:100%;margin-top:8px" /></a-col>
                    <a-col :xs="24" :md="16"><a-input v-model:value="sku.imageUrl" placeholder="SKU 图片 URL，可从商品图片中复制" style="margin-top:8px" /></a-col>
                    <a-col :xs="24" :md="8"><a-checkbox v-model:checked="sku.enabled" style="margin-top:12px">启用</a-checkbox></a-col>
                  </a-row>
                  <a-button danger size="small" @click="removeSku(index)">删除 SKU</a-button>
                </div>
              </div>
            </a-form>

            <div class="drawer-save-bar">
              <a-space>
                <a-button @click="closeProductDrawer">取消</a-button>
                <a-button type="primary" :loading="saving" @click="saveProduct">{{ editingId ? '保存商品' : '创建商品' }}</a-button>
              </a-space>
            </div>
          </a-drawer>

          <a-drawer v-model:open="detailDrawerOpen" title="详情页配置" width="820" class="product-editor-drawer" destroy-on-close>
            <a-form layout="vertical">
              <div class="form-section">
                <h3>商品参数</h3>
                <a-textarea v-model:value="parametersText" :rows="5" placeholder="材质=尼龙&#10;产地=中国" />
              </div>
              <div class="form-section">
                <h3>服务承诺</h3>
                <a-textarea v-model:value="servicePromisesText" :rows="4" placeholder="满99包邮=订单商品金额满 99 元免运费&#10;自营保障=KKMall 单商家自营商品" />
              </div>
              <div class="form-section">
                <h3>促销文案</h3>
                <a-textarea v-model:value="promotionsText" :rows="4" placeholder="精选=今日精选好物|下单、支付、发货都能体验" />
              </div>
              <div class="form-section">
                <h3>推荐商品 ID</h3>
                <a-row :gutter="16">
                  <a-col :xs="24" :md="12"><a-form-item label="本店推荐，逗号分隔"><a-input v-model:value="storeRecommendationText" /></a-form-item></a-col>
                  <a-col :xs="24" :md="12"><a-form-item label="看了又看，逗号分隔"><a-input v-model:value="relatedRecommendationText" /></a-form-item></a-col>
                </a-row>
              </div>
              <div class="form-section">
                <h3>新增展示评价</h3>
                <a-row :gutter="12">
                  <a-col :xs="24" :md="8"><a-input v-model:value="reviewForm.userNickname" placeholder="昵称" /></a-col>
                  <a-col :xs="24" :md="8"><a-input-number v-model:value="reviewForm.rating" :min="1" :max="5" style="width:100%" /></a-col>
                  <a-col :xs="24" :md="8"><a-input v-model:value="reviewForm.tagsText" placeholder="标签，逗号分隔" /></a-col>
                  <a-col :span="24"><a-textarea v-model:value="reviewForm.content" :rows="3" placeholder="评价内容" style="margin-top:8px" /></a-col>
                </a-row>
                <a-button style="margin-top:12px" @click="addReview">添加评价</a-button>
              </div>
            </a-form>
            <div class="drawer-save-bar">
              <a-space>
                <a-button @click="detailDrawerOpen = false">取消</a-button>
                <a-button type="primary" :loading="saving" @click="saveDetailConfig">保存详情配置</a-button>
              </a-space>
            </div>
          </a-drawer>
        </section>

        <section v-if="activeMenu === 'orders' || activeMenu === 'shipments'" class="admin-section">
          <a-card :title="activeMenu === 'orders' ? '订单管理' : '发货管理'">
            <a-table :data-source="orders" :pagination="false" row-key="id">
              <a-table-column title="订单号" data-index="orderNo" />
              <a-table-column title="状态" data-index="status" />
              <a-table-column title="应付金额"><template #default="{ record }">{{ money(record.payableAmount) }}</template></a-table-column>
              <a-table-column title="操作">
                <template #default="{ record }">
                  <a-button v-if="record.status === 'PAID_PENDING_SHIPMENT'" type="primary" @click="ship(record.id)">录入发货</a-button>
                </template>
              </a-table-column>
            </a-table>
          </a-card>
        </section>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CarOutlined, DashboardOutlined, FileTextOutlined, ShopOutlined, UploadOutlined, UserOutlined } from '@ant-design/icons-vue'
import { Category, mallApi, OrderSummary, ProductDetailConfig, Sku } from '../../api/mall'
import { useAuthStore } from '../../stores/auth'
import { money } from '../../utils/money'

type AdminMenu = 'dashboard' | 'products' | 'orders' | 'shipments'

const router = useRouter()
const auth = useAuthStore()
const selectedMenu = ref<AdminMenu[]>(['dashboard'])
const products = ref<any[]>([])
const orders = ref<OrderSummary[]>([])
const categories = ref<Category[]>([])
const uploading = ref(false)
const saving = ref(false)
const productDrawerOpen = ref(false)
const detailDrawerOpen = ref(false)
const editingId = ref<number>()
const detailEditingId = ref<number>()
const sellingPointsText = ref('自营好物\n满 99 元包邮')
const attributesText = ref('服务=单商家自营\n配送=商家手动发货')
const parametersText = ref('')
const servicePromisesText = ref('')
const promotionsText = ref('')
const storeRecommendationText = ref('')
const relatedRecommendationText = ref('')
const reviewForm = reactive({ userNickname: '匿名买家', rating: 5, tagsText: '质量好,发货快', content: '' })
const productForm = reactive({
  categoryId: 1,
  title: '',
  subtitle: '',
  brand: 'KKMall 自营',
  description: '',
  sellingPoints: [] as string[],
  unit: '件',
  detailHtml: '',
  attributes: {} as Record<string, string>,
  images: [] as string[],
  mainImage: '',
  status: 'ON_SALE',
  skus: [{ specName: '规格', specValue: '默认', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true, imageUrl: '' }] as Sku[]
})

const activeMenu = computed(() => selectedMenu.value[0])
const currentTitle = computed(() => ({ dashboard: '工作台', products: '商品管理', orders: '订单管理', shipments: '发货管理' }[activeMenu.value]))
const adminLabel = computed(() => auth.adminPhone ? auth.adminPhone.slice(-4) : '后台')
const categoryOptions = computed(() => categories.value.map(item => ({ label: item.name, value: item.id })))
const statusOptions = [{ label: '草稿', value: 'DRAFT' }, { label: '上架', value: 'ON_SALE' }, { label: '下架', value: 'OFF_SALE' }]
const pendingShipmentCount = computed(() => orders.value.filter(item => item.status === 'PAID_PENDING_SHIPMENT').length)
const onSaleCount = computed(() => products.value.filter(item => item.status === 'ON_SALE').length)

async function load() {
  categories.value = await mallApi.categories()
  products.value = await mallApi.adminProducts()
  orders.value = (await mallApi.adminOrders()).items
}

async function saveProduct() {
  if (saving.value) return
  saving.value = true
  const payload = {
    ...productForm,
    sellingPoints: lines(sellingPointsText.value),
    attributes: keyValues(attributesText.value),
    mainImage: productForm.images[0] || productForm.mainImage,
    skus: productForm.skus.map(sku => ({ ...sku, specs: { [sku.specName]: sku.specValue }, enabled: sku.enabled !== false }))
  }
  try {
    const result = editingId.value ? await mallApi.updateProduct(editingId.value, payload) : await mallApi.createProduct(payload)
    message.success(editingId.value ? '商品已保存' : '商品已创建')
    closeProductDrawer()
    await load()
    if (!editingId.value && result?.id) await editDetailConfig(result.id)
  } finally {
    saving.value = false
  }
}

async function editProduct(id: number) {
  const detail = await mallApi.adminProduct(id)
  editingId.value = id
  productForm.categoryId = detail.categoryId
  productForm.title = detail.title
  productForm.subtitle = detail.subtitle || ''
  productForm.brand = detail.brand || 'KKMall 自营'
  productForm.description = detail.description || ''
  productForm.unit = detail.unit || '件'
  productForm.detailHtml = detail.detailHtml || ''
  productForm.attributes = (detail.attributes || {}) as Record<string, string>
  productForm.images = detail.images || []
  productForm.mainImage = detail.mainImage || ''
  productForm.status = detail.status || 'DRAFT'
  productForm.skus = detail.skus.length ? detail.skus.map(sku => ({ ...sku, enabled: sku.enabled !== false && sku.enabled !== 0 })) : [{ specName: '规格', specValue: '默认', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true, imageUrl: '' }]
  sellingPointsText.value = (detail.sellingPoints || []).join('\n')
  attributesText.value = Object.entries(productForm.attributes).map(([key, value]) => `${key}=${value}`).join('\n')
  selectedMenu.value = ['products']
  productDrawerOpen.value = true
}

async function editDetailConfig(id: number) {
  const config = await mallApi.adminProductDetailConfig(id) as ProductDetailConfig
  detailEditingId.value = id
  parametersText.value = config.parameters.map(item => `${item.name}=${item.value}`).join('\n')
  servicePromisesText.value = config.servicePromises.map(item => `${item.title}=${item.description || ''}`).join('\n')
  promotionsText.value = config.promotions.map(item => `${item.label || ''}=${item.title}|${item.description || ''}`).join('\n')
  storeRecommendationText.value = config.storeRecommendationIds.join(',')
  relatedRecommendationText.value = config.relatedRecommendationIds.join(',')
  detailDrawerOpen.value = true
}

async function saveDetailConfig() {
  if (!detailEditingId.value) return
  saving.value = true
  try {
    await mallApi.updateProductDetailConfig(detailEditingId.value, {
      parameters: keyValueRows(parametersText.value),
      servicePromises: keyValueRows(servicePromisesText.value).map(row => ({ title: row.name, description: row.value, icon: 'shield', enabled: true })),
      promotions: promotionRows(promotionsText.value),
      storeRecommendationIds: idList(storeRecommendationText.value),
      relatedRecommendationIds: idList(relatedRecommendationText.value)
    })
    message.success('详情配置已保存')
    detailDrawerOpen.value = false
  } finally {
    saving.value = false
  }
}

async function addReview() {
  if (!detailEditingId.value || !reviewForm.content.trim()) return message.warning('请填写评价内容')
  await mallApi.createProductReview(detailEditingId.value, {
    userNickname: reviewForm.userNickname,
    rating: reviewForm.rating,
    content: reviewForm.content,
    tags: reviewForm.tagsText.split(',').map(item => item.trim()).filter(Boolean),
    status: 'VISIBLE'
  })
  message.success('评价已添加')
  reviewForm.content = ''
}

async function uploadImage(options: any) {
  uploading.value = true
  try {
    const result = await mallApi.uploadImage(options.file as File)
    productForm.images.push(result.url)
    options.onSuccess?.(result)
  } catch (error) {
    options.onError?.(error)
  } finally {
    uploading.value = false
  }
}

function addSku() {
  productForm.skus.push({ specName: '规格', specValue: '', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true, imageUrl: '' })
}

function openCreateProduct() {
  resetProductForm()
  productDrawerOpen.value = true
}

function closeProductDrawer() {
  productDrawerOpen.value = false
  resetProductForm()
}

function assetUrl(value?: string) {
  return value || ''
}

function removeSku(index: number) {
  if (productForm.skus.length > 1) productForm.skus.splice(index, 1)
}

function removeImage(index: number) {
  productForm.images.splice(index, 1)
}

async function toggle(record: any) {
  await mallApi.changeProductStatus(record.id, record.status === 'ON_SALE' ? 'OFF_SALE' : 'ON_SALE')
  await load()
}

async function ship(id: number) {
  await mallApi.ship(id, '顺丰速运', `KK${Date.now()}`)
  message.success('已发货')
  await load()
}

function resetProductForm() {
  editingId.value = undefined
  productForm.title = ''
  productForm.subtitle = ''
  productForm.brand = 'KKMall 自营'
  productForm.description = ''
  productForm.unit = '件'
  productForm.detailHtml = ''
  productForm.attributes = {}
  productForm.images = []
  productForm.mainImage = ''
  productForm.status = 'ON_SALE'
  productForm.skus = [{ specName: '规格', specValue: '默认', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true, imageUrl: '' }]
  sellingPointsText.value = '自营好物\n满 99 元包邮'
  attributesText.value = '服务=单商家自营\n配送=商家手动发货'
}

function handleAdminMenu(event: { key: string }) {
  if (event.key === 'logout') {
    auth.logoutAdmin()
    router.push('/admin/login')
  }
}

function lines(value: string) {
  return value.split('\n').map(item => item.trim()).filter(Boolean)
}

function keyValues(value: string) {
  return Object.fromEntries(keyValueRows(value).map(row => [row.name, row.value]))
}

function keyValueRows(value: string) {
  return lines(value).map(item => {
    const [name, ...rest] = item.split('=')
    return { name: name.trim(), value: rest.join('=').trim(), enabled: true }
  }).filter(item => item.name)
}

function promotionRows(value: string) {
  return lines(value).map(item => {
    const [label, body = ''] = item.split('=')
    const [title, description = ''] = body.split('|')
    return { label: label.trim(), title: (title || label).trim(), description: description.trim(), enabled: true }
  }).filter(item => item.title)
}

function idList(value: string) {
  return value.split(',').map(item => Number(item.trim())).filter(Boolean)
}

onMounted(load)
</script>
