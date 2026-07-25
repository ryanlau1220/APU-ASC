import {
  createRouter as createTanStackRouter,
  Link,
} from '@tanstack/react-router'
import { FileQuestion } from 'lucide-react'
import { routeTree } from './routeTree.gen'

export function getRouter() {
  const router = createTanStackRouter({
    routeTree,
    scrollRestoration: true,
    defaultPreload: 'intent',
    defaultPreloadStaleTime: 0,
    defaultNotFoundComponent: () => (
      <div className="min-h-screen flex flex-col items-center justify-center bg-background text-foreground p-6 text-center space-y-4">
        <div className="p-4 rounded-full bg-primary/10 border border-primary/30 text-primary">
          <FileQuestion className="w-8 h-8" />
        </div>
        <h1 className="font-heading text-2xl font-bold">
          404 - Page Not Found
        </h1>
        <p className="text-xs text-muted-foreground max-w-sm">
          The requested page or route could not be found. Please verify the URL
          or return to the main dashboard.
        </p>
        <Link
          to="/"
          className="px-4 py-2 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:opacity-90 transition-opacity"
        >
          Return to Dashboard
        </Link>
      </div>
    ),
  })

  return router
}

declare module '@tanstack/react-router' {
  interface Register {
    router: ReturnType<typeof getRouter>
  }
}
