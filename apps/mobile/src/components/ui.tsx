import * as React from 'react'
import {
  ActivityIndicator,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
  type StyleProp,
  type ViewStyle,
} from 'react-native'

const colors = {
  canvas: '#F4F7FB',
  surface: '#FFFFFF',
  ink: '#10233D',
  muted: '#62748C',
  line: '#DCE5F0',
  primary: '#2563EB',
  primaryInk: '#FFFFFF',
  success: '#15803D',
  warning: '#B45309',
  danger: '#B91C1C',
}

export { colors }

export function Screen({
  children,
  scroll = true,
}: {
  children: React.ReactNode
  scroll?: boolean
}) {
  const content = <View style={styles.content}>{children}</View>

  return (
    <SafeAreaView style={styles.safeArea}>
      {scroll ? (
        <ScrollView contentContainerStyle={styles.scrollContent}>{content}</ScrollView>
      ) : (
        content
      )}
    </SafeAreaView>
  )
}

export function Card({ children, style }: { children: React.ReactNode; style?: StyleProp<ViewStyle> }) {
  return <View style={[styles.card, style]}>{children}</View>
}

export function PrimaryButton({
  label,
  onPress,
  disabled = false,
  loading = false,
  tone = 'primary',
}: {
  label: string
  onPress(): void
  disabled?: boolean
  loading?: boolean
  tone?: 'primary' | 'danger' | 'quiet'
}) {
  const isDisabled = disabled || loading
  const buttonStyle =
    tone === 'danger'
      ? styles.dangerButton
      : tone === 'quiet'
        ? styles.quietButton
        : styles.primaryButton
  const labelStyle = tone === 'quiet' ? styles.quietButtonText : styles.primaryButtonText

  return (
    <Pressable
      accessibilityRole="button"
      disabled={isDisabled}
      onPress={onPress}
      style={({ pressed }) => [buttonStyle, isDisabled && styles.disabled, pressed && styles.pressed]}
    >
      {loading ? <ActivityIndicator color={colors.primaryInk} /> : <Text style={labelStyle}>{label}</Text>}
    </Pressable>
  )
}

export function StatusPill({ value }: { value?: string }) {
  const normalized = value?.replaceAll('_', ' ') || 'UNKNOWN'
  const color = normalized.includes('APPROVED') || normalized.includes('COMPLETED') || normalized.includes('PAID')
    ? colors.success
    : normalized.includes('REJECTED') || normalized.includes('CANCELLED') || normalized.includes('OVERDUE')
      ? colors.danger
      : colors.warning
  return <Text style={[styles.pill, { color }]}>{normalized}</Text>
}

export function LoadState({ loading, error, empty, children }: {
  loading: boolean
  error?: unknown
  empty: boolean
  children: React.ReactNode
}) {
  if (loading) return <ActivityIndicator style={styles.loader} color={colors.primary} />
  if (error) return <Text style={styles.error}>Unable to load this information.</Text>
  if (empty) return <Text style={styles.empty}>Nothing to show yet.</Text>
  return <>{children}</>
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: colors.canvas },
  scrollContent: { flexGrow: 1 },
  content: { gap: 16, padding: 20 },
  card: {
    backgroundColor: colors.surface,
    borderColor: colors.line,
    borderRadius: 18,
    borderWidth: 1,
    gap: 8,
    padding: 16,
  },
  primaryButton: {
    alignItems: 'center',
    backgroundColor: colors.primary,
    borderRadius: 12,
    justifyContent: 'center',
    minHeight: 48,
    paddingHorizontal: 16,
  },
  dangerButton: {
    alignItems: 'center',
    backgroundColor: colors.danger,
    borderRadius: 12,
    justifyContent: 'center',
    minHeight: 44,
    paddingHorizontal: 14,
  },
  quietButton: {
    alignItems: 'center',
    backgroundColor: '#EAF1FF',
    borderRadius: 12,
    justifyContent: 'center',
    minHeight: 44,
    paddingHorizontal: 14,
  },
  primaryButtonText: { color: colors.primaryInk, fontSize: 15, fontWeight: '700' },
  quietButtonText: { color: colors.primary, fontSize: 15, fontWeight: '700' },
  disabled: { opacity: 0.5 },
  pressed: { opacity: 0.8 },
  pill: { fontSize: 11, fontWeight: '800', letterSpacing: 0.4, textTransform: 'uppercase' },
  loader: { marginVertical: 32 },
  error: { color: colors.danger, fontSize: 14, paddingVertical: 16 },
  empty: { color: colors.muted, fontSize: 14, paddingVertical: 16 },
})
