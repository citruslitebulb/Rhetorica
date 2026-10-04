# Rhetorica web

Installable, offline vocabulary site for Rhetorica. It reads the Android seed JSON at build time from `app/src/main/assets/data/seed/` and does not replace the Android app. There is no account, backend, or API key. Saved words and the selected orator live in `localStorage` on the device.

## Develop

From `web/`:

```bash
npm install
npm run dev
```

Open the URL Vite prints (usually http://localhost:5173).

```bash
npm test
npm run build
npm run preview
```

`npm run build` typechecks, writes a static site to `web/dist`, and checks that the output is a PWA (manifest, 192/512 icons, service worker).

## What you can do

- Browse orator dictionaries and open one to read the bio and sample line.
- Scroll that orator’s word feed, or the full library, including a stable Word of the Day.
- Open a word for its definition, example, and source. Save it. The saved list is still there after refresh.
- Read quotes for the selected orator, and open a full speech when the seed includes one.

After the first load, the service worker serves the app offline.

## Deploy on Vercel (static)

This folder is a static Vite build. No environment variables.

1. Import the Git repository in Vercel.
2. Set **Root Directory** to `web`.
3. Framework preset: **Vite**.
4. Build command: `npm run build`.
5. Output directory: `dist`.

`vercel.json` rewrites deep links such as `/words/40001` to `index.html`. The build still needs the repo’s `app/src/main/assets` seed and Playfair font files, which stay available when the root directory is `web`.

Add to Home Screen on a phone, or install from the browser’s app menu on desktop, after the site is served over HTTPS.
