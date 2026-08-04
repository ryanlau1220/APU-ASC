import { PostHogProvider } from '@posthog/react'
import { QueryClientProvider } from '@tanstack/react-query'
import {
  createRootRouteWithContext,
  HeadContent,
  Outlet,
  Scripts,
} from '@tanstack/react-router'
import posthog from 'posthog-js'
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
  avatarUrl?: string
  roles?: string[]
}

export interface RouterContext {
  userSession: UserSession
}

const THEME_INIT_SCRIPT = `(function(){try{var stored=window.localStorage.getItem('theme');var mode=(stored==='light'||stored==='dark'||stored==='auto')?stored:'auto';var prefersDark=window.matchMedia('(prefers-color-scheme: dark)').matches;var resolved=mode==='auto'?(prefersDark?'dark':'light'):mode;var root=document.documentElement;root.classList.remove('light','dark');root.classList.add(resolved);if(mode==='auto'){root.removeAttribute('data-theme')}else{root.setAttribute('data-theme',mode)}root.style.colorScheme=resolved;}catch(e){}})();`

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

function AuthProvider({ children }: { children: React.ReactNode }) {
  const [userSession, setUserSession] = React.useState<UserSession>({
    authenticated: false,
    roles: [],
  })
  const [loading, setLoading] = React.useState(true)

  React.useEffect(() => {
    let cancelled = false

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

  React.useEffect(() => {
    const userId = userSession.authenticated ? userSession.id : undefined
    if (userId) {
      posthog.identify(userId, { roles: userSession.roles ?? [] })
    } else {
      posthog.reset()
    }

    void import('@sentry/react').then((Sentry) => {
      if (userId) {
        Sentry.setUser({ id: userId })
        Sentry.setTag('user.roles', (userSession.roles ?? []).join(','))
      } else {
        Sentry.setUser(null)
      }
    })
  }, [userSession.authenticated, userSession.id, userSession.roles])

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
  const { userSession } = useUserSession()
  useEventStream(userSession?.authenticated ?? false)
  return <>{children}</>
}

function ClientPostHogProvider({ children }: { children: React.ReactNode }) {
  const [isMounted, setIsMounted] = React.useState(false)

  React.useEffect(() => {
    setIsMounted(true)

    const sentryDsn = import.meta.env.VITE_SENTRY_DSN
    if (sentryDsn) {
      void import('@sentry/react').then((Sentry) => {
        Sentry.init({
          dsn: sentryDsn,
          environment:
            import.meta.env.VITE_OBSERVABILITY_ENVIRONMENT || 'development',
          release: import.meta.env.VITE_OBSERVABILITY_RELEASE || undefined,
          integrations: [Sentry.browserTracingIntegration()],
          tracesSampleRate: Number(
            import.meta.env.VITE_SENTRY_TRACES_SAMPLE_RATE || 0.1,
          ),
          sendDefaultPii: false,
        })
      })
    }

    // Safely initialize PostHog on client browser only
    const posthogKey =
      import.meta.env.VITE_POSTHOG_PROJECT_TOKEN ||
      import.meta.env.VITE_POSTHOG_KEY
    const posthogHost =
      import.meta.env.VITE_POSTHOG_HOST || 'https://us.i.posthog.com'
    if (posthogKey) {
      posthog.init(posthogKey, {
        api_host: posthogHost,
        person_profiles: 'identified_only',
        capture_pageview: true,
      })
    }
  }, [])

  if (!isMounted) {
    return <>{children}</>
  }

  return <PostHogProvider client={posthog}>{children}</PostHogProvider>
}

import { ThemeProvider } from '../lib/useTheme'

function RootDocument({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        {/* biome-ignore lint/security/noDangerouslySetInnerHtml: theme init script */}
        <script dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
        <HeadContent />
      </head>
      <body className="bg-background text-foreground font-sans antialiased selection:bg-primary/20 min-h-screen flex flex-col">
        <ClientPostHogProvider>
          <QueryClientProvider client={queryClient}>
            <ThemeProvider>
              <AuthProvider>
                <AppContent>{children}</AppContent>
              </AuthProvider>
            </ThemeProvider>
          </QueryClientProvider>
        </ClientPostHogProvider>
        <Scripts />
      </body>
    </html>
  )
}
