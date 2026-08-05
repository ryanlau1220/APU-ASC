import { useGetMyAppointments, useGetMyQuotations, useGetUnreadNotificationCount } from '../../api/generated/endpoints'
import { Card, LoadState, Screen, StatusPill, colors } from '../../components/ui'
import { formatCurrency, formatDate } from '../../lib/format'
import { StyleSheet, Text, View } from 'react-native'

export default function CustomerHomeScreen() {
  const appointments = useGetMyAppointments()
  const quotations = useGetMyQuotations()
  const unread = useGetUnreadNotificationCount()
  const nextAppointment = appointments.data?.find((appointment) => appointment.status !== 'CANCELLED')
  const pendingQuote = quotations.data?.find((quotation) => quotation.status === 'PENDING_APPROVAL')

  return (
    <Screen>
      <View style={styles.hero}>
        <Text style={styles.kicker}>APU-ASC CUSTOMER</Text>
        <Text style={styles.title}>Good to see you.</Text>
        <Text style={styles.subtitle}>Everything important is ready when you are.</Text>
      </View>

      <Card style={styles.notice}>
        <Text style={styles.noticeNumber}>{unread.data?.count ?? 0}</Text>
        <Text style={styles.noticeText}>unread notification{unread.data?.count === 1 ? '' : 's'}</Text>
      </Card>

      <Text style={styles.sectionTitle}>Next appointment</Text>
      <LoadState loading={appointments.isLoading} error={appointments.error} empty={!nextAppointment}>
        {nextAppointment ? (
          <Card>
            <View style={styles.row}><Text style={styles.date}>{formatDate(nextAppointment.appointmentDate)}</Text><StatusPill value={nextAppointment.status} /></View>
            <Text style={styles.detail}>{nextAppointment.timeSlot || 'Time to be confirmed'}</Text>
            <Text style={styles.meta}>Booking {nextAppointment.id}</Text>
          </Card>
        ) : null}
      </LoadState>

      <Text style={styles.sectionTitle}>Quotation requiring attention</Text>
      <LoadState loading={quotations.isLoading} error={quotations.error} empty={!pendingQuote}>
        {pendingQuote ? (
          <Card>
            <View style={styles.row}><Text style={styles.detail}>{pendingQuote.quoteNumber || 'Quotation'}</Text><StatusPill value={pendingQuote.status} /></View>
            <Text style={styles.amount}>{formatCurrency(pendingQuote.totalAmount)}</Text>
            <Text style={styles.meta}>Valid until {formatDate(pendingQuote.validUntil)}</Text>
          </Card>
        ) : null}
      </LoadState>
    </Screen>
  )
}

const styles = StyleSheet.create({
  hero: { gap: 5, paddingBottom: 6, paddingTop: 8 },
  kicker: { color: colors.primary, fontSize: 11, fontWeight: '800', letterSpacing: 1.2 },
  title: { color: colors.ink, fontSize: 30, fontWeight: '800', letterSpacing: -0.7 },
  subtitle: { color: colors.muted, fontSize: 15 },
  notice: { alignItems: 'center', backgroundColor: '#10233D', flexDirection: 'row', gap: 10 },
  noticeNumber: { color: '#FFFFFF', fontSize: 24, fontWeight: '800' },
  noticeText: { color: '#D8E5F8', fontSize: 14, fontWeight: '600' },
  sectionTitle: { color: colors.ink, fontSize: 17, fontWeight: '800', marginTop: 8 },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  date: { color: colors.ink, fontSize: 18, fontWeight: '700' },
  detail: { color: colors.ink, fontSize: 15, fontWeight: '700' },
  amount: { color: colors.primary, fontSize: 22, fontWeight: '800', marginTop: 4 },
  meta: { color: colors.muted, fontSize: 12 },
})
