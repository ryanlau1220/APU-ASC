import { Redirect, Tabs } from 'expo-router'
import { useAuth } from '../../lib/auth'

export default function CustomerTabs() {
  const { roles, status } = useAuth()
  if (status === 'loading') return null
  if (status === 'signed-out') return <Redirect href="/" />
  const technician = roles.includes('TECHNICIAN')
  const operational = roles.includes('STAFF') || roles.includes('MANAGER')
  const customer = !technician && !operational

  return (
    <Tabs screenOptions={{ headerTitleStyle: { fontWeight: '700' }, tabBarLabelStyle: { fontSize: 11 } }}>
      <Tabs.Screen name="index" options={{ href: customer ? undefined : null, title: 'Home' }} />
      <Tabs.Screen name="appointments" options={{ href: customer ? undefined : null, title: 'Appointments' }} />
      <Tabs.Screen name="vehicles" options={{ href: customer ? undefined : null, title: 'Vehicles' }} />
      <Tabs.Screen name="quotations" options={{ href: customer ? undefined : null, title: 'Quotes' }} />
      <Tabs.Screen name="feedback" options={{ href: null, title: 'Feedback' }} />
      <Tabs.Screen name="jobs" options={{ href: technician ? undefined : null, title: 'My jobs' }} />
      <Tabs.Screen name="operations" options={{ href: operational ? undefined : null, title: 'Operations' }} />
      <Tabs.Screen name="more" options={{ title: 'More' }} />
    </Tabs>
  )
}
