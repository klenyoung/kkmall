import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { router } from '../router'

export const useAuthStore = defineStore('auth', () => {
  const userToken = ref(localStorage.getItem('kkmall_user_token') || '')
  const userPhone = ref(localStorage.getItem('kkmall_user_phone') || '')
  const userNickname = ref(localStorage.getItem('kkmall_user_nickname') || '')
  const userAvatarUrl = ref(localStorage.getItem('kkmall_user_avatar_url') || '')
  const adminToken = ref(localStorage.getItem('kkmall_admin_token') || '')
  const adminPhone = ref(localStorage.getItem('kkmall_admin_phone') || '')
  const mode = ref<'user' | 'admin'>('user')

  const activeToken = computed(() => mode.value === 'admin' ? adminToken.value : userToken.value)

  function setUser(token: string, user?: { phone?: string; nickname?: string; avatarUrl?: string } | string) {
    mode.value = 'user'
    userToken.value = token
    const phone = typeof user === 'string' ? user : user?.phone
    const nickname = typeof user === 'string' ? '' : user?.nickname
    const avatarUrl = typeof user === 'string' ? '' : user?.avatarUrl
    if (phone) {
      userPhone.value = phone
      localStorage.setItem('kkmall_user_phone', phone)
    }
    if (nickname) {
      userNickname.value = nickname
      localStorage.setItem('kkmall_user_nickname', nickname)
    }
    if (avatarUrl !== undefined) {
      userAvatarUrl.value = avatarUrl || ''
      if (avatarUrl) localStorage.setItem('kkmall_user_avatar_url', avatarUrl)
      else localStorage.removeItem('kkmall_user_avatar_url')
    }
    localStorage.setItem('kkmall_user_token', token)
  }

  function updateUserSummary(user: { phone?: string; nickname?: string; avatarUrl?: string }) {
    if (user.phone) {
      userPhone.value = user.phone
      localStorage.setItem('kkmall_user_phone', user.phone)
    }
    if (user.nickname) {
      userNickname.value = user.nickname
      localStorage.setItem('kkmall_user_nickname', user.nickname)
    }
    userAvatarUrl.value = user.avatarUrl || ''
    if (user.avatarUrl) localStorage.setItem('kkmall_user_avatar_url', user.avatarUrl)
    else localStorage.removeItem('kkmall_user_avatar_url')
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
    userNickname.value = ''
    userAvatarUrl.value = ''
    localStorage.removeItem('kkmall_user_token')
    localStorage.removeItem('kkmall_user_phone')
    localStorage.removeItem('kkmall_user_nickname')
    localStorage.removeItem('kkmall_user_avatar_url')
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
      userNickname.value = ''
      userAvatarUrl.value = ''
      localStorage.removeItem('kkmall_user_token')
      localStorage.removeItem('kkmall_user_phone')
      localStorage.removeItem('kkmall_user_nickname')
      localStorage.removeItem('kkmall_user_avatar_url')
      router.push('/login')
    }
  }

  return { userToken, userPhone, userNickname, userAvatarUrl, adminToken, adminPhone, mode, activeToken, setUser, updateUserSummary, setAdmin, useAdminMode, useUserMode, logoutUser, logoutAdmin, logoutExpired }
})
