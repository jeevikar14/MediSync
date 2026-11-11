import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// All API calls go to the Spring Cloud Gateway on :8080 (no CORS needed in dev).
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, proxy: { '/api': 'http://localhost:8080' } },
});
