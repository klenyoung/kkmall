import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'
import HomePage from './views/HomePage.vue'
import ProductDetailPage from './views/ProductDetailPage.vue'
import LoginPage from './views/LoginPage.vue'
import CartPage from './views/CartPage.vue'
import CheckoutPage from './views/CheckoutPage.vue'
import OrdersPage from './views/OrdersPage.vue'
import OrderDetailPage from './views/OrderDetailPage.vue'
import PayPage from './views/PayPage.vue'
import AccountPage from './views/AccountPage.vue'
import AdminLoginPage from './views/admin/AdminLoginPage.vue'
import AdminDashboardPage from './views/admin/AdminDashboardPage.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomePage },
    { path: '/products/:id', component: ProductDetailPage },
    { path: '/login', component: LoginPage },
    { path: '/cart', component: CartPage, meta: { auth: 'user' } },
    { path: '/checkout', component: CheckoutPage, meta: { auth: 'user' } },
    { path: '/pay/:id', component: PayPage, meta: { auth: 'user' } },
    { path: '/account', component: AccountPage, meta: { auth: 'user' } },
    { path: '/orders', component: OrdersPage, meta: { auth: 'user' } },
    { path: '/orders/:id', component: OrderDetailPage, meta: { auth: 'user' } },
    { path: '/admin/login', component: AdminLoginPage },
    { path: '/admin', component: AdminDashboardPage, meta: { auth: 'admin' } }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path.startsWith('/admin')) auth.useAdminMode()
  else auth.useUserMode()
  if (to.meta.auth === 'user' && !auth.userToken) return `/login?redirect=${encodeURIComponent(to.fullPath)}`
  if (to.meta.auth === 'admin' && !auth.adminToken) return '/admin/login'
})
