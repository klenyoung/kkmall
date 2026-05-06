<template>
  <div class="login-screen customer-login">
    <header class="login-topbar">
      <RouterLink to="/" class="login-logo" aria-label="返回 KKMall 首页">
        <span>K</span>
        <strong>KKMall</strong>
      </RouterLink>
      <RouterLink to="/admin/login" class="login-toplink">商家后台</RouterLink>
    </header>

    <main class="login-shell">
      <section class="login-hero" aria-label="KKMall 会员权益">
        <div>
          <p class="login-kicker">百亿加补节 · 今日精选</p>
          <h1>登录后，把好物和订单都装进口袋。</h1>
          <p class="login-copy">从加入购物车到商家发货，KKMall 帮你把每一步都走得清楚、可靠、好买。</p>
        </div>

        <div class="login-benefits">
          <div class="login-benefit">
            <strong>满 99 包邮</strong>
            <span>自动计算运费</span>
          </div>
          <div class="login-benefit">
            <strong>精选好物</strong>
            <span>SKU 库存实时校验</span>
          </div>
          <div class="login-benefit">
            <strong>订单追踪</strong>
            <span>物流信息随时查看</span>
          </div>
        </div>
      </section>

      <section class="login-panel" aria-label="手机号登录">
        <div class="login-panel-head">
          <p>KKMall 会员登录</p>
          <h2>欢迎回来</h2>
        </div>

        <a-form layout="vertical" @submit.prevent="login">
          <a-form-item label="手机号">
            <a-input
              v-model:value="phone"
              size="large"
              placeholder="请输入手机号"
              autocomplete="tel"
            >
              <template #prefix><PhoneOutlined /></template>
            </a-input>
          </a-form-item>

          <a-form-item label="模拟验证码">
            <a-input
              v-model:value="code"
              size="large"
              placeholder="请输入 123456"
              autocomplete="one-time-code"
            >
              <template #prefix><SafetyCertificateOutlined /></template>
            </a-input>
          </a-form-item>

          <a-button
            class="login-submit"
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登录并继续购物
          </a-button>
        </a-form>

        <div class="login-footnote">
          <span>测试账号已预填</span>
          <RouterLink to="/">先逛逛首页</RouterLink>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { PhoneOutlined, SafetyCertificateOutlined } from '@ant-design/icons-vue'
import { mallApi } from '../api/mall'
import { useAuthStore } from '../stores/auth'

const phone = ref('13800138000')
const code = ref('123456')
const loading = ref(false)
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

async function login() {
  if (loading.value) return
  loading.value = true
  try {
    await mallApi.mockCode(phone.value)
    const data = await mallApi.login(phone.value, code.value) as any
    auth.setUser(data.token, data.user || phone.value)
    router.push(String(route.query.redirect || '/'))
  } finally {
    loading.value = false
  }
}
</script>
