<template>
  <ShopLayout>
    <section class="account-page">
      <div class="account-header">
        <a-avatar :size="72" :src="profile.avatarUrl">
          <template #icon><UserOutlined /></template>
        </a-avatar>
        <div>
          <h1>{{ profile.nickname || '我的账户' }}</h1>
          <p>{{ profile.phone }}</p>
        </div>
      </div>

      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :lg="10">
          <a-card title="个人资料" :bordered="false">
            <a-form layout="vertical">
              <a-form-item label="头像">
                <div class="avatar-uploader">
                  <a-avatar :size="64" :src="form.avatarUrl">
                    <template #icon><UserOutlined /></template>
                  </a-avatar>
                  <div>
                    <input ref="avatarInput" type="file" accept="image/*" class="sr-only" @change="uploadAvatar" />
                    <a-button :loading="avatarUploading" @click="avatarInput?.click()">上传头像</a-button>
                    <p>支持图片文件，最大 2MB</p>
                  </div>
                </div>
              </a-form-item>
              <a-form-item label="昵称">
                <a-input v-model:value="form.nickname" maxlength="32" placeholder="请输入昵称" />
              </a-form-item>
              <a-form-item label="手机号">
                <a-input :value="profile.phone" disabled />
              </a-form-item>
              <a-form-item label="性别">
                <a-radio-group v-model:value="form.gender">
                  <a-radio-button value="UNKNOWN">保密</a-radio-button>
                  <a-radio-button value="MALE">男</a-radio-button>
                  <a-radio-button value="FEMALE">女</a-radio-button>
                </a-radio-group>
              </a-form-item>
              <a-form-item label="生日">
                <a-input v-model:value="form.birthday" type="date" />
              </a-form-item>
              <a-button type="primary" block :loading="savingProfile" @click="saveProfile">保存资料</a-button>
            </a-form>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="14">
          <a-card title="收货地址" :bordered="false">
            <template #extra>
              <a-button type="primary" @click="openAddress()">新增地址</a-button>
            </template>

            <a-empty v-if="!addresses.length" description="还没有收货地址" />
            <div v-else class="address-list">
              <article v-for="item in addresses" :key="item.id" class="address-item">
                <div>
                  <a-space>
                    <strong>{{ item.receiverName }}</strong>
                    <span>{{ item.receiverPhone }}</span>
                    <a-tag v-if="isDefault(item)" color="red">默认</a-tag>
                  </a-space>
                  <p>{{ item.region }} {{ item.detail }}</p>
                </div>
                <a-space wrap>
                  <a-button size="small" @click="openAddress(item)">编辑</a-button>
                  <a-button v-if="!isDefault(item)" size="small" @click="setDefault(item.id)">设为默认</a-button>
                  <a-popconfirm title="确认删除这个地址？" ok-text="删除" cancel-text="取消" @confirm="removeAddress(item.id)">
                    <a-button size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </article>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </section>

    <a-modal v-model:open="addressModalOpen" :title="editingAddressId ? '编辑地址' : '新增地址'" ok-text="保存" cancel-text="取消" @ok="saveAddress">
      <a-form layout="vertical">
        <a-form-item label="收货人">
          <a-input v-model:value="addressForm.receiverName" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="addressForm.receiverPhone" />
        </a-form-item>
        <a-form-item label="省市区">
          <a-input v-model:value="addressForm.region" placeholder="例如：上海市 浦东新区" />
        </a-form-item>
        <a-form-item label="详细地址">
          <a-textarea v-model:value="addressForm.detail" :rows="3" />
        </a-form-item>
        <a-checkbox v-model:checked="addressForm.isDefault">设为默认地址</a-checkbox>
      </a-form>
    </a-modal>
  </ShopLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import ShopLayout from '../components/ShopLayout.vue'
import { Address, UserProfile, mallApi } from '../api/mall'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const avatarInput = ref<HTMLInputElement>()
const avatarUploading = ref(false)
const savingProfile = ref(false)
const addressModalOpen = ref(false)
const editingAddressId = ref<number>()
const addresses = ref<Address[]>([])
const profile = reactive<UserProfile>({
  id: 0,
  phone: '',
  nickname: '',
  avatarUrl: '',
  gender: 'UNKNOWN',
  birthday: '',
  role: 'CUSTOMER'
})
const form = reactive({
  nickname: '',
  avatarUrl: '',
  gender: 'UNKNOWN' as UserProfile['gender'],
  birthday: ''
})
const addressForm = reactive<Partial<Address>>({
  receiverName: '',
  receiverPhone: '',
  region: '',
  detail: '',
  isDefault: false
})

async function load() {
  const [profileData, addressData] = await Promise.all([mallApi.profile(), mallApi.addresses()])
  Object.assign(profile, profileData)
  form.nickname = profileData.nickname
  form.avatarUrl = profileData.avatarUrl || ''
  form.gender = profileData.gender || 'UNKNOWN'
  form.birthday = profileData.birthday || ''
  addresses.value = addressData
  auth.updateUserSummary(profileData)
}

async function uploadAvatar(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    const result = await mallApi.uploadAvatar(file)
    form.avatarUrl = result.url
    message.success('头像已上传')
  } finally {
    avatarUploading.value = false
    if (avatarInput.value) avatarInput.value.value = ''
  }
}

async function saveProfile() {
  savingProfile.value = true
  try {
    const data = await mallApi.updateProfile(form)
    Object.assign(profile, data)
    auth.updateUserSummary(data)
    message.success('资料已保存')
  } finally {
    savingProfile.value = false
  }
}

function openAddress(item?: Address) {
  editingAddressId.value = item?.id
  addressForm.receiverName = item?.receiverName || ''
  addressForm.receiverPhone = item?.receiverPhone || ''
  addressForm.region = item?.region || ''
  addressForm.detail = item?.detail || ''
  addressForm.isDefault = item ? isDefault(item) : addresses.value.length === 0
  addressModalOpen.value = true
}

async function saveAddress() {
  if (editingAddressId.value) await mallApi.updateAddress(editingAddressId.value, addressForm)
  else await mallApi.createAddress(addressForm)
  addressModalOpen.value = false
  message.success('地址已保存')
  await load()
}

async function removeAddress(id: number) {
  await mallApi.deleteAddress(id)
  message.success('地址已删除')
  await load()
}

async function setDefault(id: number) {
  await mallApi.setDefaultAddress(id)
  message.success('默认地址已更新')
  await load()
}

function isDefault(item: Address) {
  return item.isDefault === true || item.isDefault === 1
}

onMounted(load)
</script>
