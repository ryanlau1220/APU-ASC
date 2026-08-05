import { existsSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const publicPath = fileURLToPath(new URL('../public/', import.meta.url))

describe('PWA assets', () => {
  it('declares installable icons and standalone display', () => {
    const manifest = JSON.parse(
      readFileSync(`${publicPath}manifest.webmanifest`, 'utf8'),
    )

    expect(manifest).toMatchObject({
      display: 'standalone',
      start_url: '/',
      icons: [
        { src: '/pwa-192.png', sizes: '192x192' },
        { src: '/pwa-512.png', sizes: '512x512', purpose: 'any maskable' },
      ],
    })
    expect(existsSync(`${publicPath}pwa-192.png`)).toBe(true)
    expect(existsSync(`${publicPath}pwa-512.png`)).toBe(true)
  })

  it('never caches authenticated routes', () => {
    const worker = readFileSync(`${publicPath}service-worker.js`, 'utf8')

    expect(worker).toContain("url.pathname.startsWith('/api/')")
    expect(worker).toContain("url.pathname.startsWith('/auth/')")
    expect(worker).toContain("url.pathname.startsWith('/oauth2/')")
  })
})
