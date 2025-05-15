import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcssVite from '@tailwindcss/vite'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue(), tailwindcssVite()],
  resolve: {
    alias: {
      '@': resolve('./src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
