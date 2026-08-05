const CACHE_NAME = 'apu-asc-static-v1'
const STATIC_ASSETS = [
  '/offline.html',
  '/manifest.webmanifest',
  '/pwa-192.png',
  '/pwa-512.png',
]

self.addEventListener('install', (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS)))
})

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((names) =>
        Promise.all(
          names
            .filter((name) => name.startsWith('apu-asc-static-') && name !== CACHE_NAME)
            .map((name) => caches.delete(name)),
        ),
      )
      .then(() => self.clients.claim()),
  )
})

self.addEventListener('fetch', (event) => {
  const { request } = event
  if (request.method !== 'GET') return

  const url = new URL(request.url)
  if (
    url.origin !== self.location.origin ||
    url.pathname.startsWith('/api/') ||
    url.pathname.startsWith('/auth/') ||
    url.pathname.startsWith('/oauth2/') ||
    url.pathname.startsWith('/login/') ||
    url.pathname === '/logout'
  ) {
    return
  }

  if (request.mode === 'navigate') {
    event.respondWith(fetch(request).catch(() => caches.match('/offline.html')))
    return
  }

  if (!['font', 'image', 'script', 'style'].includes(request.destination)) return

  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) return cached

      return fetch(request).then((response) => {
        if (!response.ok || response.type !== 'basic') return response
        const responseCopy = response.clone()
        void caches.open(CACHE_NAME).then((cache) => cache.put(request, responseCopy))
        return response
      })
    }),
  )
})
