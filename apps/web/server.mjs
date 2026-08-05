import { createReadStream } from 'node:fs'
import { stat } from 'node:fs/promises'
import { createServer } from 'node:http'
import { Readable } from 'node:stream'
import { fileURLToPath } from 'node:url'
import { dirname, extname, resolve } from 'node:path'
import app from './dist/server/server.js'

const directory = dirname(fileURLToPath(import.meta.url))
const clientDirectory = resolve(directory, 'dist/client')
const port = Number(process.env.PORT || 3000)

const contentTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.ico': 'image/x-icon',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.map': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.webp': 'image/webp',
}

function requestOrigin(request) {
  const protocol = request.headers['x-forwarded-proto'] || 'http'
  const host = request.headers.host || 'localhost'
  return `${protocol}://${host}`
}

async function serveAsset(request, response) {
  const pathname = new URL(request.url || '/', requestOrigin(request)).pathname
  const assetPath = resolve(clientDirectory, `.${decodeURIComponent(pathname)}`)
  if (!assetPath.startsWith(`${clientDirectory}/`)) return false

  try {
    if (!(await stat(assetPath)).isFile()) return false
  } catch {
    return false
  }

  response.writeHead(200, {
    'Cache-Control': pathname.startsWith('/assets/')
      ? 'public, max-age=31536000, immutable'
      : 'public, max-age=3600',
    'Content-Type': contentTypes[extname(assetPath)] || 'application/octet-stream',
  })
  createReadStream(assetPath).pipe(response)
  return true
}

createServer(async (request, response) => {
  try {
    if (await serveAsset(request, response)) return

    const body = ['GET', 'HEAD'].includes(request.method || '') ? undefined : request
    const appResponse = await app.fetch(
      new Request(new URL(request.url || '/', requestOrigin(request)), {
        body,
        duplex: body ? 'half' : undefined,
        headers: request.headers,
        method: request.method,
      }),
    )

    for (const [name, value] of appResponse.headers) response.setHeader(name, value)
    if (typeof appResponse.headers.getSetCookie === 'function') {
      response.setHeader('set-cookie', appResponse.headers.getSetCookie())
    }
    response.statusCode = appResponse.status
    if (appResponse.body) Readable.fromWeb(appResponse.body).pipe(response)
    else response.end()
  } catch (error) {
    console.error('Web request failed', error)
    response.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' })
    response.end('Internal Server Error')
  }
}).listen(port, '0.0.0.0', () => {
  console.log(`APU-ASC web server listening on ${port}`)
})
