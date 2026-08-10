import {
  getGetMyNotificationsQueryKey,
  getGetMyPreferencesQueryKey,
  useCreateCheckoutSession,
  getGetMyPaymentsQueryKey,
  useGetMyNotifications,
  useGetMyPayments,
  useGetMyPreferences,
  useMarkAllNotificationsRead,
  useMarkNotificationRead,
  useUpdateMyPreferences,
} from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, StatusPill, colors } from '../../components/ui'
import { useAuth } from '../../lib/auth'
import { errorMessage, formatCurrency, formatDate } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import * as React from 'react'
import { Pressable, StyleSheet, Switch, Text, View } from 'react-native'
import * as WebBrowser from 'expo-web-browser'

export default function MoreScreen() {
  const queryClient = useQueryClient()
  const { signOut } = useAuth()
  const notifications = useGetMyNotifications({ limit: 50 })
  const payments = useGetMyPayments()
  const checkout = useCreateCheckoutSession()
  const preferences = useGetMyPreferences()
  const markRead = useMarkNotificationRead()
  const markAllRead = useMarkAllNotificationsRead()
  const updatePreferences = useUpdateMyPreferences()
  const [message, setMessage] = React.useState<string | null>(null)
  const notificationEnabled = preferences.data?.inAppNotificationsEnabled ?? true
  const timezone = preferences.data?.timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'

  const invalidateNotifications = () => {
    void queryClient.invalidateQueries({ queryKey: getGetMyNotificationsQueryKey({ limit: 50 }) })
  }

  const updateNotifications = (inAppNotificationsEnabled: boolean) => {
    updatePreferences.mutate(
      { data: { timeZone: timezone, inAppNotificationsEnabled } },
      {
        onSuccess: () =>
          void queryClient.invalidateQueries({
            queryKey: getGetMyPreferencesQueryKey(),
          }),
        onError: (error) => setMessage(errorMessage(error)),
      },
    )
  }

  const startCheckout = (paymentId: string) => {
    checkout.mutate(
      { id: paymentId },
      {
        onSuccess: async (session) => {
          if (!session.checkoutUrl) {
            setMessage('Unable to start secure checkout. Please try again.')
            return
          }
          await WebBrowser.openBrowserAsync(session.checkoutUrl)
          void queryClient.invalidateQueries({ queryKey: getGetMyPaymentsQueryKey() })
        },
        onError: (error) => setMessage(errorMessage(error)),
      },
    )
  }

  return (
    <Screen>
      <Text style={styles.title}>More</Text>

      <Text style={styles.sectionTitle}>Notification centre</Text>
      <Card>
        <View style={styles.settingRow}>
          <View style={styles.settingCopy}><Text style={styles.settingTitle}>In-app notifications</Text><Text style={styles.settingDetail}>Appointment and quotation updates.</Text></View>
          <Switch onValueChange={updateNotifications} value={notificationEnabled} disabled={updatePreferences.isPending} />
        </View>
        {message ? <Text style={styles.error}>{message}</Text> : null}
        <PrimaryButton
          label="Mark all as read"
          tone="quiet"
          loading={markAllRead.isPending}
          onPress={() => markAllRead.mutate(undefined, { onSuccess: invalidateNotifications, onError: (error) => setMessage(errorMessage(error)) })}
        />
        <LoadState loading={notifications.isLoading} error={notifications.error} empty={notifications.data?.length === 0}>
          {notifications.data?.map((notification) => (
            <PressableNotification
              key={notification.id}
              title={notification.title || 'APU-ASC update'}
              body={notification.body || ''}
              createdAt={notification.createdAt}
              read={Boolean(notification.readAt)}
              onPress={() => {
                if (!notification.id || notification.readAt) return
                markRead.mutate({ id: notification.id }, { onSuccess: invalidateNotifications, onError: (error) => setMessage(errorMessage(error)) })
              }}
            />
          ))}
        </LoadState>
      </Card>

      <Text style={styles.sectionTitle}>Invoices</Text>
      <LoadState loading={payments.isLoading} error={payments.error} empty={payments.data?.length === 0}>
        {payments.data?.map((payment) => (
          <Card key={payment.id}>
            <View style={styles.row}><Text style={styles.invoice}>{payment.invoiceNumber || 'Invoice'}</Text><StatusPill value={payment.paymentStatus} /></View>
            <Text style={styles.amount}>{formatCurrency(payment.amount)}</Text>
            <Text style={styles.meta}>Created {formatDate(payment.createdAt)}</Text>
            {(payment.paymentStatus === 'UNPAID' || payment.paymentStatus === 'FAILED') && payment.id ? (
              <PrimaryButton
                label={checkout.isPending ? 'Opening secure checkout…' : 'Pay securely'}
                loading={checkout.isPending}
                onPress={() => {
                  if (payment.id) startCheckout(payment.id)
                }}
              />
            ) : null}
          </Card>
        ))}
      </LoadState>

      <PrimaryButton label="Sign out" tone="quiet" onPress={() => void signOut()} />
    </Screen>
  )
}

function PressableNotification({ title, body, createdAt, read, onPress }: {
  title: string
  body: string
  createdAt?: string
  read: boolean
  onPress(): void
}) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [
        styles.notification,
        !read && styles.unread,
        pressed && styles.pressed,
      ]}
    >
      <Text style={styles.notificationTitle}>{title}</Text>
      <Text style={styles.notificationBody}>{body}</Text>
      <Text style={styles.meta}>{formatDate(createdAt)}</Text>
    </Pressable>
  )
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  sectionTitle: { color: colors.ink, fontSize: 17, fontWeight: '800', marginTop: 8 },
  settingRow: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  settingCopy: { flex: 1, gap: 3, paddingRight: 12 },
  settingTitle: { color: colors.ink, fontSize: 15, fontWeight: '700' },
  settingDetail: { color: colors.muted, fontSize: 12 },
  notification: { borderTopColor: colors.line, borderTopWidth: 1, gap: 3, marginHorizontal: -16, paddingHorizontal: 16, paddingTop: 12 },
  unread: { borderLeftColor: colors.primary, borderLeftWidth: 3 },
  pressed: { opacity: 0.7 },
  notificationTitle: { color: colors.ink, fontSize: 14, fontWeight: '700' },
  notificationBody: { color: colors.muted, fontSize: 13, lineHeight: 18 },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  invoice: { color: colors.ink, fontSize: 16, fontWeight: '800' },
  amount: { color: colors.primary, fontSize: 21, fontWeight: '900' },
  meta: { color: colors.muted, fontSize: 12 },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
})
