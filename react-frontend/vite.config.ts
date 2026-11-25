import { fileURLToPath, URL } from 'node:url'

import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    // "@/something" means "src/something". It avoids long "../../.." imports.
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    port: 5173,
    // In development the browser talks to Vite (same origin), and Vite forwards
    // every /api request to the API Gateway. So the browser never makes a
    // cross-origin call and we do not need CORS settings on the gateway.
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    env: {
      VITE_KEYCLOAK_URL: 'http://localhost:8180',
      VITE_KEYCLOAK_REALM: 'Learn_Microservices',
      VITE_KEYCLOAK_CLIENT_ID: 'learn-microservices-web',
    },
  },
})
