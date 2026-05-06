import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import { ConfigProvider } from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './styles.css'
import App from './App.vue'
import { router } from './router'

ConfigProvider.config({
  theme: {
    primaryColor: '#ff0036'
  }
})

createApp(App).use(createPinia()).use(router).use(Antd).mount('#app')
