import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import tailwindcss from 'tailwindcss';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    preserveSymlinks: true,
  },
  css: {
    postcss: {
      plugins: [tailwindcss()],
    }
  },
  server: {
    allowedHosts: ["minecraft.koornbeurs.net"]
  }
})
