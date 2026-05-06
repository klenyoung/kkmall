import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { router } from '../router'

export const useAuthStore = defineStore('auth', () => {
  const userToken = ref(localStorage.getItem('kkmall_user_token') || '')
  const userPhone = ref(localStorage.getItem('kkmall_user_phone') || '')
  const adminToken = ref(localStorage.getItem('kkmall_admin_token') || '')
  const adminPhone = ref(localStorage.getItem('kkmall_admin_phone') || '')
  const mode = ref<'user' | 'admin'>('user')

  const activeToken = computed(() => mode.value === 'admin' ? adminToken.value : userToken.value)

  function setUser(token: string, phone?: string) {
    mode.value = 'user'
    userToken.value = token
    if (phone) {
      userPhone.value = phone
      localStorage.setItem('kkmall_user_phone', phone)
    }
    localStorage.setItem('kkmall_user_token', token)
  }

  function setAdmin(token: string, phone?: string) {
    mode.value = 'admin'
    adminToken.value = token
    if (phone) {
      adminPhone.value = phone
      localStorage.setItem('kkmall_admin_phone', phone)
    }
    localStorage.setItem('kkmall_admin_token', token)
  }

  function useAdminMode() { mode.value = 'admin' }
  function useUserMode() { mode.value = 'user' }

  function logoutUser() {
    userToken.value = ''
    userPhone.value = ''
    localStorage.removeItem('kkmall_user_token')
    localStorage.removeItem('kkmall_user_phone')
  }

  function logoutAdmin() {
    adminToken.value = ''
    adminPhone.value = ''
    localStorage.removeItem('kkmall_admin_token')
    localStorage.removeItem('kkmall_admin_phone')
  }

  function logoutExpired() {
    if (mode.value === 'admin') {
      adminToken.value = ''
      adminPhone.value = ''
      localStorage.removeItem('kkmall_admin_token')
      localStorage.removeItem('kkmall_admin_phone')
      router.push('/admin/login')
    } else {
      userToken.value = ''
      userPhone.value = ''
      localStorage.removeItem('kkmall_user_token')
      localStorage.removeItem('kkmall_user_phone')
      router.push('/login')
    }
  }

  return { userToken, userPhone, adminToken, adminPhone, mode, activeToken, setUser, setAdmin, useAdminMode, useUserMode, logoutUser, logoutAdmin, logoutExpired }
})
