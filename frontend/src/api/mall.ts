import { http, PageResult } from './http'

export interface Category { id: number; name: string; sortOrder: number; enabled: number }
export interface ProductCard { id: number; title: string; brand?: string; subtitle?: string; coverImage?: string; mainImage?: string; minPrice: number; salesCount?: number; status: string }
export interface Sku { id?: number; skuCode?: string; specName: string; specValue: string; specs?: Record<string, string>; price: number; marketPrice?: number; costPrice?: number; stock: number; weightGrams?: number; barcode?: string; enabled?: number | boolean }
export interface ProductDetail extends ProductCard { description: string; sellingPoints?: string[]; unit?: string; detailHtml?: string; attributes?: Record<string, unknown>; images: string[]; categoryId: number; skus: Sku[] }
export interface CartItem { id: number; productId: number; skuId: number; title: string; image?: string; specText: string; price: number; quantity: number; stock: number; subtotal: number; settleable: boolean }
export interface CartView { items: CartItem[]; productAmount: number }
export interface Address { id: number; receiverName: string; receiverPhone: string; region: string; detail: string; isDefault: number | boolean }
export interface OrderSummary { id: number; orderNo: string; payableAmount: number; status: string; createdAt?: string }
export interface OrderDetail extends OrderSummary { productAmount: number; shippingFee: number; items: any[]; shipment?: { logisticsCompany: string; trackingNo: string; shippedAt: string } }
export interface UploadResult { url: string; objectName: string }

export const mallApi = {
  mockCode: (phone: string) => http.post('/api/v1/auth/mock-code', { phone }) as unknown as Promise<any>,
  login: (phone: string, code: string) => http.post('/api/v1/auth/login', { phone, code }) as unknown as Promise<any>,
  adminLogin: (phone: string, code: string) => http.post('/api/v1/admin/auth/login', { phone, code }) as unknown as Promise<any>,
  categories: () => http.get('/api/v1/categories') as unknown as Promise<Category[]>,
  products: (params?: Record<string, unknown>) => http.get('/api/v1/products', { params }) as unknown as Promise<PageResult<ProductCard>>,
  product: (id: number) => http.get(`/api/v1/products/${id}`) as unknown as Promise<ProductDetail>,
  cart: () => http.get('/api/v1/cart') as unknown as Promise<CartView>,
  addCart: (skuId: number, quantity: number) => http.post('/api/v1/cart/items', { skuId, quantity }) as unknown as Promise<any>,
  updateCart: (id: number, quantity: number) => http.patch(`/api/v1/cart/items/${id}`, { quantity }) as unknown as Promise<any>,
  deleteCart: (id: number) => http.delete(`/api/v1/cart/items/${id}`) as unknown as Promise<any>,
  addresses: () => http.get('/api/v1/addresses') as unknown as Promise<Address[]>,
  createAddress: (data: Partial<Address>) => http.post('/api/v1/addresses', data) as unknown as Promise<any>,
  createOrder: (addressId: number, cartItemIds: number[]) => http.post('/api/v1/orders', { addressId, cartItemIds }) as unknown as Promise<any>,
  pay: (orderId: number) => http.post('/api/v1/payments/mock', { orderId }) as unknown as Promise<any>,
  orders: () => http.get('/api/v1/orders') as unknown as Promise<PageResult<OrderSummary>>,
  order: (id: number) => http.get(`/api/v1/orders/${id}`) as unknown as Promise<OrderDetail>,
  adminProducts: () => http.get('/api/v1/admin/products') as unknown as Promise<any[]>,
  adminProduct: (id: number) => http.get(`/api/v1/admin/products/${id}`) as unknown as Promise<ProductDetail>,
  createProduct: (data: any) => http.post('/api/v1/admin/products', data) as unknown as Promise<any>,
  updateProduct: (id: number, data: any) => http.put(`/api/v1/admin/products/${id}`, data) as unknown as Promise<any>,
  changeProductStatus: (id: number, status: string) => http.patch(`/api/v1/admin/products/${id}/status`, { status }) as unknown as Promise<any>,
  uploadImage: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return http.post('/api/v1/admin/uploads/images', form, { headers: { 'Content-Type': 'multipart/form-data' } }) as unknown as Promise<UploadResult>
  },
  adminOrders: () => http.get('/api/v1/admin/orders') as unknown as Promise<PageResult<OrderSummary>>,
  ship: (id: number, logisticsCompany: string, trackingNo: string) => http.post(`/api/v1/admin/orders/${id}/shipment`, { logisticsCompany, trackingNo }) as unknown as Promise<any>
}
