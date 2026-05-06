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
            <a-col :xs="24" :md="8">
              <a-card><a-statistic title="商品数" :value="products.length" /></a-card>
            </a-col>
            <a-col :xs="24" :md="8">
              <a-card><a-statistic title="待发货订单" :value="pendingShipmentCount" /></a-card>
            </a-col>
            <a-col :xs="24" :md="8">
              <a-card><a-statistic title="在售商品" :value="onSaleCount" /></a-card>
            </a-col>
          </a-row>
        </section>

        <section v-if="activeMenu === 'products'" class="admin-section">
              <a-card title="商品管理">
                <template #extra>
                  <a-button type="primary" @click="openCreateProduct">新增商品</a-button>
                </template>
                <a-table :data-source="products" :pagination="false" row-key="id" :scroll="{ x: 900 }">
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
                  <a-table-column title="品牌" data-index="brand" width="110" />
                  <a-table-column title="价格" width="110">
                    <template #default="{ record }">{{ money(record.minPrice) }}</template>
                  </a-table-column>
                  <a-table-column title="库存" data-index="totalStock" width="90" />
                  <a-table-column title="销量" data-index="salesCount" width="90" />
                  <a-table-column title="状态" width="110">
                    <template #default="{ record }"><a-tag :color="record.status === 'ON_SALE' ? 'green' : 'default'">{{ record.status }}</a-tag></template>
                  </a-table-column>
                  <a-table-column title="操作" fixed="right" width="180">
                    <template #default="{ record }">
                      <a-space>
                        <a-button size="small" @click="editProduct(record.id)">编辑</a-button>
                        <a-button size="small" @click="toggle(record)">{{ record.status === 'ON_SALE' ? '下架' : '上架' }}</a-button>
                      </a-space>
                    </template>
                  </a-table-column>
                </a-table>
              </a-card>

          <a-drawer
            v-model:open="productDrawerOpen"
            :title="editingId ? '编辑商品' : '新增商品'"
            width="760"
            class="product-editor-drawer"
            destroy-on-close
          >
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
                  <a-button :loading="uploading"><UploadOutlined />上传商品图片</a-button>
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
                <h3>详情信息</h3>
                <a-form-item label="卖点">
                  <a-textarea v-model:value="sellingPointsText" :rows="2" placeholder="一行一个卖点" />
                </a-form-item>
                <a-form-item label="商品描述">
                  <a-textarea v-model:value="productForm.description" :rows="2" />
                </a-form-item>
                <a-form-item label="详情内容">
                  <a-textarea v-model:value="productForm.detailHtml" :rows="4" />
                </a-form-item>
                <a-form-item label="商品属性">
                  <a-textarea v-model:value="attributesText" :rows="3" placeholder="材质=纯棉，每行一个属性" />
                </a-form-item>
              </div>

              <div class="form-section">
                <div class="section-title-row">
                  <h3>SKU 信息</h3>
                  <a-button size="small" @click="addSku">添加 SKU</a-button>
                </div>
                <div v-for="(sku, index) in productForm.skus" :key="index" class="sku-editor">
                  <a-row :gutter="8">
                    <a-col :span="8"><a-input v-model:value="sku.specName" placeholder="规格名" /></a-col>
                    <a-col :span="8"><a-input v-model:value="sku.specValue" placeholder="规格值" /></a-col>
                    <a-col :span="8"><a-input v-model:value="sku.skuCode" placeholder="SKU 编码" /></a-col>
                    <a-col :span="8"><a-input-number v-model:value="sku.price" :min="0" placeholder="售价(分)" style="width:100%;margin-top:8px" /></a-col>
                    <a-col :span="8"><a-input-number v-model:value="sku.marketPrice" :min="0" placeholder="划线价(分)" style="width:100%;margin-top:8px" /></a-col>
                    <a-col :span="8"><a-input-number v-model:value="sku.stock" :min="0" placeholder="库存" style="width:100%;margin-top:8px" /></a-col>
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
        </section>

        <section v-if="activeMenu === 'orders' || activeMenu === 'shipments'" class="admin-section">
          <a-card :title="activeMenu === 'orders' ? '订单管理' : '发货管理'">
            <a-table :data-source="orders" :pagination="false" row-key="id">
              <a-table-column title="订单号" data-index="orderNo" />
              <a-table-column title="状态" data-index="status" />
              <a-table-column title="应付金额">
                <template #default="{ record }">{{ money(record.payableAmount) }}</template>
              </a-table-column>
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
import { Category, mallApi, OrderSummary, Sku } from '../../api/mall'
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
const editingId = ref<number>()
const sellingPointsText = ref('自营好物\n满 99 元包邮')
const attributesText = ref('服务=单商家自营\n配送=商家手动发货')
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
  skus: [{ specName: '规格', specValue: '默认', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true }] as Sku[]
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
    if (editingId.value) await mallApi.updateProduct(editingId.value, payload)
    else await mallApi.createProduct(payload)
    message.success(editingId.value ? '商品已保存' : '商品已创建')
    closeProductDrawer()
    await load()
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
  productForm.skus = detail.skus.length ? detail.skus : [{ specName: '规格', specValue: '默认', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true }]
  sellingPointsText.value = (detail.sellingPoints || []).join('\n')
  attributesText.value = Object.entries(productForm.attributes).map(([key, value]) => `${key}=${value}`).join('\n')
  selectedMenu.value = ['products']
  productDrawerOpen.value = true
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
  productForm.skus.push({ specName: '规格', specValue: '', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true })
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
  if (!value) return ''
  if (value.startsWith('/')) return value
  return value
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
  productForm.skus = [{ specName: '规格', specValue: '默认', skuCode: '', price: 9900, marketPrice: 12900, stock: 10, enabled: true }]
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
  return Object.fromEntries(lines(value).map(item => {
    const [key, ...rest] = item.split('=')
    return [key.trim(), rest.join('=').trim()]
  }).filter(([key]) => key))
}

onMounted(load)
</script>
