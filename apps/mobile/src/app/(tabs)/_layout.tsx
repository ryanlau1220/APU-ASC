import { Redirect, Tabs } from 'expo-router'
import { useAuth } from '../../lib/auth'

export default function CustomerTabs() {
  const { status } = useAuth()
  if (status === 'loading') return null
  if (status === 'signed-out') return <Redirect href="/" />

  return (
    <Tabs screenOptions={{ headerTitleStyle: { fontWeight: '700' }, tabBarLabelStyle: { fontSize: 11 } }}>
      <Tabs.Screen name="index" options={{ title: 'Home' }} />
      <Tabs.Screen name="appointments" options={{ title: 'Appointments' }} />
      <Tabs.Screen name="vehicles" options={{ title: 'Vehicles' }} />
      <Tabs.Screen name="quotations" options={{ title: 'Quotes' }} />
      <Tabs.Screen name="more" options={{ title: 'More' }} />
    </Tabs>
  )
}
