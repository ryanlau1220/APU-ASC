import { QueryClient, QueryClientProvider, useQueryClient } from '@tanstack/react-query'
import { Stack } from 'expo-router'
import { StatusBar } from 'expo-status-bar'
import * as React from 'react'
import { AuthProvider } from '../lib/auth'
import { useAuth } from '../lib/auth'

function AppNavigator() {
  const queryClient = useQueryClient()
  const { status } = useAuth()

  React.useEffect(() => {
    if (status === 'signed-out') queryClient.clear()
  }, [queryClient, status])

  return <Stack screenOptions={{ headerShown: false }} />
}

export default function RootLayout() {
  const [queryClient] = React.useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { retry: 1, staleTime: 30_000 },
        },
      }),
  )

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <StatusBar style="dark" />
        <AppNavigator />
      </AuthProvider>
    </QueryClientProvider>
  )
}
