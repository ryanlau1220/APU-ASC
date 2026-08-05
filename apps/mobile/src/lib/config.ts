import { z } from 'zod'

const configuration = z.object({
  EXPO_PUBLIC_API_BASE_URL: z.url(),
  EXPO_PUBLIC_KEYCLOAK_ISSUER: z.url(),
  EXPO_PUBLIC_KEYCLOAK_CLIENT_ID: z.string().min(1).default('apu-asc-mobile'),
})

const parsed = configuration.safeParse({
  EXPO_PUBLIC_API_BASE_URL: process.env.EXPO_PUBLIC_API_BASE_URL,
  EXPO_PUBLIC_KEYCLOAK_ISSUER: process.env.EXPO_PUBLIC_KEYCLOAK_ISSUER,
  EXPO_PUBLIC_KEYCLOAK_CLIENT_ID: process.env.EXPO_PUBLIC_KEYCLOAK_CLIENT_ID,
})

if (!parsed.success) {
  throw new Error('Mobile app configuration is incomplete.')
}

function requireHttps(value: string, name: string) {
  const url = new URL(value)
  if (!__DEV__ && url.protocol !== 'https:') {
    throw new Error(`${name} must use HTTPS outside development.`)
  }
  return url.toString().replace(/\/$/, '')
}

export const API_BASE_URL = requireHttps(
  parsed.data.EXPO_PUBLIC_API_BASE_URL,
  'EXPO_PUBLIC_API_BASE_URL',
)
export const KEYCLOAK_ISSUER = requireHttps(
  parsed.data.EXPO_PUBLIC_KEYCLOAK_ISSUER,
  'EXPO_PUBLIC_KEYCLOAK_ISSUER',
)
export const KEYCLOAK_CLIENT_ID = parsed.data.EXPO_PUBLIC_KEYCLOAK_CLIENT_ID
