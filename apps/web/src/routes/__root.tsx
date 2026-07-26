import { QueryClientProvider } from '@tanstack/react-query'
import {
  createRootRouteWithContext,
  HeadContent,
  redirect,
  Scripts,
} from '@tanstack/react-router'
import { getWebRequest } from '@tanstack/react-start/server'
import { bffFetch } from '../lib/apiClient'
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

export const Route = createRootRouteWithContext<RouterContext>()({
  beforeLoad: async ({ location }) => {
    const isPublicRoute =
      location.pathname.startsWith('/login') ||
      location.pathname.startsWith('/register')

    const headers: Record<string, string> = {}
    if (typeof window === 'undefined') {
      try {
        const req = getWebRequest()
        const cookie = req?.headers?.get('cookie')
        if (cookie) {
          headers.cookie = cookie
        }
      } catch {
        // Ignore if getWebRequest unavailable
      }
    }

    let userSession: UserSession = { authenticated: false, roles: [] }
    try {
      userSession = await bffFetch<UserSession>('/api/v1/auth/me', { headers })
    } catch {
      userSession = { authenticated: false, roles: [] }
    }

    if (!userSession.authenticated && !isPublicRoute) {
      if (typeof window !== 'undefined') {
        window.location.href = '/oauth2/authorization/keycloak'
      } else {
        throw redirect({ href: '/oauth2/authorization/keycloak' })
      }
    }

    return { userSession }
  },
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
})

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
          <AppContent>{children}</AppContent>
        </QueryClientProvider>
        <Scripts />
      </body>
    </html>
  )
}
