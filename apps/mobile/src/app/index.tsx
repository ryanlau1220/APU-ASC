import { Redirect } from 'expo-router'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'
import { PrimaryButton, colors } from '../components/ui'
import { useAuth } from '../lib/auth'

export default function WelcomeScreen() {
  const { status, signIn } = useAuth()

  if (status === 'loading') {
    return (
      <View style={styles.loading}>
        <ActivityIndicator color={colors.primary} />
      </View>
    )
  }
  if (status === 'signed-in') return <Redirect href="/(tabs)" />

  return (
    <View style={styles.page}>
      <View style={styles.brandMark}><Text style={styles.brandGlyph}>⌁</Text></View>
      <Text style={styles.eyebrow}>AUTOMOTIVE SERVICE CENTRE</Text>
      <Text style={styles.title}>Your service journey, in one place.</Text>
      <Text style={styles.copy}>
        Review appointments, approve quotations, and keep track of your vehicles securely.
      </Text>
      <PrimaryButton label="Sign in securely" onPress={() => void signIn()} />
      <Text style={styles.note}>Sign-in opens the secure APU-ASC account page.</Text>
    </View>
  )
}

const styles = StyleSheet.create({
  page: { backgroundColor: colors.canvas, flex: 1, justifyContent: 'center', padding: 28 },
  loading: { alignItems: 'center', backgroundColor: colors.canvas, flex: 1, justifyContent: 'center' },
  brandMark: { alignItems: 'center', backgroundColor: '#DBEAFE', borderRadius: 22, height: 72, justifyContent: 'center', marginBottom: 28, width: 72 },
  brandGlyph: { color: colors.primary, fontSize: 44, fontWeight: '700', lineHeight: 48 },
  eyebrow: { color: colors.primary, fontSize: 11, fontWeight: '800', letterSpacing: 1.3, marginBottom: 12 },
  title: { color: colors.ink, fontSize: 36, fontWeight: '800', letterSpacing: -1.1, lineHeight: 42, marginBottom: 14 },
  copy: { color: colors.muted, fontSize: 16, lineHeight: 24, marginBottom: 32 },
  note: { color: colors.muted, fontSize: 12, lineHeight: 18, marginTop: 16, textAlign: 'center' },
})
