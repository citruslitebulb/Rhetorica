import path from 'node:path';
import { fileURLToPath } from 'node:url';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';
import { VitePWA } from 'vite-plugin-pwa';
import { playfairLicensePlugin, rhetoricaSeedPlugin } from './vite/seedPlugin.ts';

const rootDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(rootDir, '..');
const seedDir = path.join(repoRoot, 'app/src/main/assets/data/seed');
const licensePath = path.join(repoRoot, 'app/src/main/assets/licenses/PlayfairDisplay-OFL.txt');

export default defineConfig({
  plugins: [
    react(),
    rhetoricaSeedPlugin(seedDir),
    playfairLicensePlugin(licensePath, path.join(rootDir, 'dist')),
    VitePWA({
      registerType: 'autoUpdate',
      injectRegister: 'script',
      includeAssets: ['favicon.svg', 'icons/*.png'],
      manifest: {
        id: '/',
        name: 'Rhetorica',
        short_name: 'Rhetorica',
        description: 'Daily words that moved empires. An offline vocabulary of great orators.',
        theme_color: '#14110e',
        background_color: '#14110e',
        display: 'standalone',
        start_url: '/',
        scope: '/',
        lang: 'en',
        categories: ['education', 'books'],
        icons: [
          {
            src: 'icons/icon-192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: 'icons/icon-512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: 'icons/icon-maskable-512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
        ],
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,svg,png,ttf,ico,webmanifest,txt}'],
        navigateFallback: 'index.html',
        maximumFileSizeToCacheInBytes: 8 * 1024 * 1024,
      },
    }),
  ],
  build: {
    chunkSizeWarningLimit: 2000,
  },
  server: {
    fs: {
      allow: [repoRoot],
    },
  },
  test: {
    environment: 'node',
    include: ['src/**/*.test.ts'],
  },
});
