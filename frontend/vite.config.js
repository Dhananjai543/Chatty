import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // Fix for sockjs-client "global is not defined" error
    global: 'globalThis',
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  },
  optimizeDeps: {
    include: ['sockjs-client', '@stomp/stompjs']
  },
  build: {
    commonjsOptions: {
      include: [/sockjs-client/, /node_modules/]
    }
  }
})
