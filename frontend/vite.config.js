import {fileURLToPath, URL} from 'node:url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import fs from 'fs'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    watch: {
      usePolling: true,
    },
    https: {
      pfx: fs.readFileSync(path.resolve(__dirname, 'localhost.pfx')),
      passphrase: 'changeit'
    },
    proxy: {
      '/api': {
        target: 'https://backend:8080',
        changeOrigin: true,
        secure: false,
        ws: true
      },
    },
  }
})
