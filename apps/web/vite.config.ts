import tailwindcss from '@tailwindcss/vite'
import { devtools } from '@tanstack/devtools-vite'
import { tanstackStart } from '@tanstack/react-start/plugin/vite'
import viteReact from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

const config = defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/oauth2': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/login': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/logout': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
  plugins: [devtools(), tailwindcss(), tanstackStart(), viteReact()],
})

export default config
