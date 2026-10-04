import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const dist = path.join(root, 'dist');

function fail(message) {
  console.error(message);
  process.exit(1);
}

const manifestPath = path.join(dist, 'manifest.webmanifest');
if (!fs.existsSync(manifestPath)) fail('dist/manifest.webmanifest is missing');

const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
if (manifest.display !== 'standalone') fail(`manifest display is ${manifest.display}`);
if (!manifest.start_url) fail('manifest start_url is missing');

const icons = Array.isArray(manifest.icons) ? manifest.icons : [];
const sizes = new Set(icons.map((icon) => icon.sizes));
if (!sizes.has('192x192') || !sizes.has('512x512')) {
  fail(`manifest icons missing 192 or 512: ${[...sizes].join(', ')}`);
}

for (const icon of icons) {
  const file = path.join(dist, String(icon.src).replace(/^\//, ''));
  if (!fs.existsSync(file)) fail(`icon file missing: ${icon.src}`);
}

const entries = fs.readdirSync(dist);
const serviceWorker = entries.find((name) => name === 'sw.js' || name === 'service-worker.js');
if (!serviceWorker) fail(`no service worker in dist (${entries.join(', ')})`);

const worker = fs.readFileSync(path.join(dist, serviceWorker), 'utf8');
if (!/precache|workbox/i.test(worker)) fail('service worker does not precache assets');

const html = fs.readFileSync(path.join(dist, 'index.html'), 'utf8');
if (!html.includes('manifest.webmanifest')) fail('index.html does not link the manifest');
if (!/registerSW|serviceWorker/i.test(html)) fail('index.html does not register a service worker');

const license = path.join(dist, 'licenses', 'PlayfairDisplay-OFL.txt');
if (!fs.existsSync(license)) fail('Playfair OFL license was not copied into dist');

console.log(`PWA ok: ${serviceWorker}, ${icons.length} icons, standalone manifest`);
