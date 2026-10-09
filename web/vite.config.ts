import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
  },
  server: {
    host: '127.0.0.1',
    port: 3001,
    proxy: {
      '/actuator': 'http://127.0.0.1:8080',
      '/api': 'http://127.0.0.1:8080',
      '/docs': 'http://127.0.0.1:8080',
      '/v3': 'http://127.0.0.1:8080',
    },
  },
});
