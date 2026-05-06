<template>
  <div class="login-screen admin-login">
    <header class="login-topbar">
      <RouterLink to="/" class="login-logo" aria-label="返回 KKMall 首页">
        <span>K</span>
        <strong>KKMall</strong>
      </RouterLink>
      <RouterLink to="/login" class="login-toplink">用户登录</RouterLink>
    </header>

    <main class="login-shell">
      <section class="login-hero admin-login-hero" aria-label="KKMall 后台能力">
        <div>
          <p class="login-kicker">Merchant Console</p>
          <h1>商品、订单、发货集中处理。</h1>
          <p class="login-copy">面向单商家自营商城的轻量运营后台，快速完成上架、查单和物流录入。</p>
        </div>

        <div class="login-benefits">
          <div class="login-benefit">
            <strong>商品管理</strong>
            <span>SKU、价格、库存</span>
          </div>
          <div class="login-benefit">
            <strong>订单管理</strong>
            <span>状态筛选与详情</span>
          </div>
          <div class="login-benefit">
            <strong>发货管理</strong>
            <span>物流公司与单号</span>
          </div>
        </div>
      </section>

      <section class="login-panel admin-login-panel" aria-label="后台登录">
        <div class="login-panel-head">
          <p>KKMall 商家后台</p>
          <h2>管理员登录</h2>
        </div>

        <a-form layout="vertical" @submit.prevent="login">
          <a-form-item label="管理员手机号">
            <a-input
              v-model:value="phone"
              size="large"
              placeholder="请输入管理员手机号"
              autocomplete="tel"
            >
              <template #prefix><UserOutlined /></template>
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
            进入后台
          </a-button>
        </a-form>

        <div class="login-footnote">
          <span>测试管理员已预填</span>
          <RouterLink to="/">返回商城</RouterLink>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons-vue'
import { mallApi } from '../../api/mall'
import { useAuthStore } from '../../stores/auth'

const phone = ref('13900000000')
const code = ref('123456')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function login() {
  if (loading.value) return
  loading.value = true
  try {
    const data = await mallApi.adminLogin(phone.value, code.value) as any
    auth.setAdmin(data.token, phone.value)
    router.push('/admin')
  } finally {
    loading.value = false
  }
}
</script>
