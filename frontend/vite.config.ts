import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY || 'http://172.23.86.146:8080',
        changeOrigin: true
      }
    }
  }
})
