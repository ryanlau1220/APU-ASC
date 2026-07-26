import { QueryClientProvider } from '@tanstack/react-query'
import {
  createRootRouteWithContext,
  HeadContent,
  Outlet,
  Scripts,
} from '@tanstack/react-router'
import * as React from 'react'
import { queryClient } from '../lib/queryClient'
import { useEventStream } from '../lib/useEventStream'

import appCss from '../styles.css?url'

export interface UserSession {
  authenticated: boolean
  id?: string
  keycloakId?: string
  username?: string
  email?: string
  fullName?: string
  roles?: string[]
}

export interface RouterContext {
  userSession: UserSession
}

const THEME_INIT_SCRIPT = `(function(){try{var stored=window.localStorage.getItem('theme');var mode=(stored==='light'||stored==='dark'||stored==='auto')?stored:'auto';var prefersDark=window.matchMedia('(prefers-color-scheme: dark)').matches;var resolved=mode==='auto'?(prefersDark?'dark':'light'):mode;var root=document.documentElement;root.classList.remove('light','dark');root.classList.add(resolved);if(mode==='auto'){root.removeAttribute('data-theme')}else{root.setAttribute('data-theme',mode)}root.style.colorScheme=resolved;}catch(e){}})();`

/**
 * React context to share the user session state across the app.
 * This is populated by the AuthProvider component which fetches
 * the session on the client side (where browser cookies are available).
 */
export const UserSessionContext = React.createContext<{
  userSession: UserSession
  loading: boolean
}>({
  userSession: { authenticated: false, roles: [] },
  loading: true,
})

export function useUserSession() {
  return React.useContext(UserSessionContext)
}

export const Route = createRootRouteWithContext<RouterContext>()({
  head: () => ({
    meta: [
      {
        charSet: 'utf-8',
      },
      {
        name: 'viewport',
        content: 'width=device-width, initial-scale=1',
      },
      {
        title: 'APU Automotive Service Centre (APU-ASC)',
      },
    ],
    links: [
      {
        rel: 'stylesheet',
        href: appCss,
      },
      {
        rel: 'icon',
        type: 'image/svg+xml',
        href: '/favicon.svg',
      },
      {
        rel: 'alternate icon',
        href: '/favicon.ico',
      },
    ],
  }),
  shellComponent: RootDocument,
  component: RootComponent,
})

/**
 * Client-side auth provider. Fetches the user session after hydration
 * using the browser's JSESSIONID cookie (sent via `credentials: 'include'`).
 *
 * During SSR the auth check is impossible because the browser's cookies
 * are not available in the server-side fetch context. This component
 * bridges that gap by performing the auth check on the client and
 * providing the result via React context.
 */
function AuthProvider({ children }: { children: React.ReactNode }) {
  const [userSession, setUserSession] = React.useState<UserSession>({
    authenticated: false,
    roles: [],
  })
  const [loading, setLoading] = React.useState(true)

  React.useEffect(() => {
    let cancelled = false

    // Use raw fetch instead of bffFetch to avoid triggering the
    // automatic 401 → Keycloak redirect. The auth provider should
    // silently check the session status without side effects.
    fetch('/api/v1/auth/me', { credentials: 'include' })
      .then((res) => {
        if (!res.ok) throw new Error('Not authenticated')
        return res.json()
      })
      .then((session: UserSession) => {
        if (!cancelled) {
          setUserSession(session)
          setLoading(false)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setUserSession({ authenticated: false, roles: [] })
          setLoading(false)
        }
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <UserSessionContext.Provider value={{ userSession, loading }}>
      {children}
    </UserSessionContext.Provider>
  )
}

function RootComponent() {
  return <Outlet />
}

function AppContent({ children }: { children: React.ReactNode }) {
  useEventStream()
  return <>{children}</>
}

function RootDocument({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        {/* biome-ignore lint/security/noDangerouslySetInnerHtml: theme init script */}
        <script dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
        <HeadContent />
      </head>
      <body className="bg-background text-foreground font-sans antialiased selection:bg-primary/20 min-h-screen flex flex-col">
        <QueryClientProvider client={queryClient}>
          <AuthProvider>
            <AppContent>{children}</AppContent>
          </AuthProvider>
        </QueryClientProvider>
        <Scripts />
      </body>
    </html>
  )
}
