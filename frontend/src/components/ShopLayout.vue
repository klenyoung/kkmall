<template>
  <div>
    <div class="promo-strip"><strong>百亿加补节</strong><span>叠券 7.7 折起 · 抢购最后 2 天</span></div>
    <header class="shell-header">
      <a-flex class="shell-header-inner" justify="space-between" align="center" gap="middle">
        <router-link class="logo" to="/">KKMall</router-link>
        <a-input-search v-model:value="keyword" placeholder="搜索商品、分类、好物" enter-button="搜索" style="max-width:520px" @search="search" />
        <a-space>
          <router-link to="/cart"><a-button>购物车</a-button></router-link>
          <router-link to="/orders"><a-button>我的订单</a-button></router-link>
          <a-dropdown v-if="isLoggedIn" placement="bottomRight" :trigger="['click']">
            <a-button class="user-chip">
              <template #icon><UserOutlined /></template>
              {{ userLabel }}
            </a-button>
            <template #overlay>
              <a-menu @click="handleUserMenu">
                <a-menu-item key="orders">我的订单</a-menu-item>
                <a-menu-item key="logout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <router-link v-else to="/login"><a-button>登录</a-button></router-link>
          <router-link to="/admin"><a-button type="primary">后台</a-button></router-link>
        </a-space>
      </a-flex>
    </header>
    <main class="page"><slot /></main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '../stores/auth'

const keyword = ref('')
const router = useRouter()
const auth = useAuthStore()
const isLoggedIn = computed(() => Boolean(auth.userToken))
const userLabel = computed(() => {
  const phone = auth.userPhone
  return phone && phone.length >= 4 ? `会员 ${phone.slice(-4)}` : '我的账户'
})

function search(value: string) {
  router.push({ path: '/', query: value ? { keyword: value } : {} })
}

function handleUserMenu(event: { key: string }) {
  if (event.key === 'orders') {
    router.push('/orders')
    return
  }
  if (event.key === 'logout') {
    auth.logoutUser()
    if (router.currentRoute.value.meta.auth === 'user') router.push('/login')
  }
}
</script>
