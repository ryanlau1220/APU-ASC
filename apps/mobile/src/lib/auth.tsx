import * as AuthSession from 'expo-auth-session'
import * as SecureStore from 'expo-secure-store'
import * as WebBrowser from 'expo-web-browser'
import * as React from 'react'
import { KEYCLOAK_CLIENT_ID, KEYCLOAK_ISSUER } from './config'
import { setAccessToken } from './apiClient'
import { getCurrentUser } from '../api/generated/endpoints'

WebBrowser.maybeCompleteAuthSession()

const REFRESH_TOKEN_KEY = 'apu-asc.refresh-token'
const REDIRECT_URI = AuthSession.makeRedirectUri({
  scheme: 'com.apuasc.app',
  path: 'oauthredirect',
})

type AuthState =
  | { status: 'loading'; accessToken: null; roles: string[] }
  | { status: 'signed-out'; accessToken: null; roles: string[] }
  | { status: 'signed-in'; accessToken: string; roles: string[] }

type AuthContextValue = AuthState & {
  signIn(): Promise<void>
  signOut(): Promise<void>
}

const AuthContext = React.createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const discovery = AuthSession.useAutoDiscovery(KEYCLOAK_ISSUER)
  const [state, setState] = React.useState<AuthState>({
    status: 'loading',
    accessToken: null,
    roles: [],
  })
  const [request, response, promptAsync] = AuthSession.useAuthRequest(
    {
      clientId: KEYCLOAK_CLIENT_ID,
      redirectUri: REDIRECT_URI,
      responseType: AuthSession.ResponseType.Code,
      scopes: ['openid', 'profile', 'email'],
      usePKCE: true,
    },
    discovery,
  )

  const acceptToken = React.useCallback(async (token: AuthSession.TokenResponse) => {
    setAccessToken(token.accessToken)
    if (token.refreshToken) {
      await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, token.refreshToken)
    }
    let roles: string[] = []
    try {
      roles = readRoles(await getCurrentUser())
    } catch {
      // Authentication remains valid even if the optional profile hydration is temporarily unavailable.
    }
    setState({ status: 'signed-in', accessToken: token.accessToken, roles })
  }, [])

  React.useEffect(() => {
    if (!discovery) return
    let active = true

    const restoreSession = async () => {
      const refreshToken = await SecureStore.getItemAsync(REFRESH_TOKEN_KEY)
      if (!refreshToken) {
        if (active) setState({ status: 'signed-out', accessToken: null, roles: [] })
        return
      }
      try {
        const token = await AuthSession.refreshAsync(
          { clientId: KEYCLOAK_CLIENT_ID, refreshToken },
          discovery,
        )
        if (active) await acceptToken(token)
      } catch {
        await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY)
        if (active) setState({ status: 'signed-out', accessToken: null, roles: [] })
      }
    }

    void restoreSession()
    return () => {
      active = false
    }
  }, [acceptToken, discovery])

  React.useEffect(() => {
    if (!response || response.type !== 'success' || !discovery || !request?.codeVerifier) return

    void AuthSession.exchangeCodeAsync(
      {
        clientId: KEYCLOAK_CLIENT_ID,
        code: response.params.code,
        redirectUri: REDIRECT_URI,
        extraParams: { code_verifier: request.codeVerifier },
      },
      discovery,
    )
      .then(acceptToken)
      .catch(() => setState({ status: 'signed-out', accessToken: null, roles: [] }))
  }, [acceptToken, discovery, request?.codeVerifier, response])

  const signIn = React.useCallback(async () => {
    if (!request) return
    await promptAsync({ showInRecents: true })
  }, [promptAsync, request])

  const signOut = React.useCallback(async () => {
    setAccessToken(null)
    await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY)
    setState({ status: 'signed-out', accessToken: null, roles: [] })
  }, [])

  return (
    <AuthContext.Provider value={{ ...state, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

function readRoles(profile: unknown) {
  if (!profile || typeof profile !== 'object') return []
  const roles = (profile as { roles?: unknown }).roles
  if (!Array.isArray(roles)) return []
  return roles
    .filter((role): role is string => typeof role === 'string')
    .map((role) => role.trim().toUpperCase().replace(/^ROLE_/, ''))
    .filter(Boolean)
}

export function useAuth() {
  const value = React.useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used inside AuthProvider.')
  return value
}
