import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Frontend roda em http://localhost:5173 e conversa com o backend em :8080.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173 }
})
