import {fileURLToPath, URL} from 'node:url'
import {defineConfig, loadEnv} from 'vite'
import vue from '@vitejs/plugin-vue'
import fs from 'fs'
import path from 'path'

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      vue(),
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
        pfx: fs.readFileSync(path.resolve(__dirname, env.CERTIFICATE_NAME)),
        passphrase: env.CERTIFICATE_PASS
      },
      proxy: {
        '/api': {
          target: 'http://backend:8080',
          changeOrigin: true,
          secure: false,
          ws: true
        },
        '/ws': {
          target: 'http://backend:8080',
          changeOrigin: true,
          secure: false,
          ws: true,
        },
      },
    }
  }
})