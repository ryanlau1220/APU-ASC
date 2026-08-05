import { getGetMyQuotationsQueryKey, useDecideQuotation, useGetMyQuotations } from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, StatusPill, colors } from '../../components/ui'
import { errorMessage, formatCurrency, formatDate } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import * as React from 'react'
import { Alert, StyleSheet, Text, View } from 'react-native'

export default function QuotationsScreen() {
  const queryClient = useQueryClient()
  const quotations = useGetMyQuotations()
  const decide = useDecideQuotation()
  const [message, setMessage] = React.useState<string | null>(null)

  const submitDecision = (id: string | undefined, decision: 'APPROVE' | 'REJECT') => {
    if (!id) return
    const verb = decision === 'APPROVE' ? 'approve' : 'reject'
    Alert.alert(`${decision === 'APPROVE' ? 'Approve' : 'Reject'} quotation?`, `You are about to ${verb} this quotation.`, [
      { text: 'Go back', style: 'cancel' },
      {
        text: decision === 'APPROVE' ? 'Approve' : 'Reject',
        style: decision === 'REJECT' ? 'destructive' : 'default',
        onPress: () => {
          decide.mutate(
            { id, data: { decision } },
            {
              onSuccess: () => {
                setMessage(`Quotation ${verb}d.`)
                void queryClient.invalidateQueries({ queryKey: getGetMyQuotationsQueryKey() })
              },
              onError: (error) => setMessage(errorMessage(error)),
            },
          )
        },
      },
    ])
  }

  return (
    <Screen>
      <Text style={styles.title}>Quotations</Text>
      <Text style={styles.subtitle}>Review repair estimates before work begins.</Text>
      {message ? <Text style={message.startsWith('Quotation ') ? styles.success : styles.error}>{message}</Text> : null}
      <LoadState loading={quotations.isLoading} error={quotations.error} empty={quotations.data?.length === 0}>
        {quotations.data?.map((quotation) => (
          <Card key={quotation.id}>
            <View style={styles.row}><Text style={styles.number}>{quotation.quoteNumber || 'Quotation'}</Text><StatusPill value={quotation.status} /></View>
            <Text style={styles.amount}>{formatCurrency(quotation.totalAmount)}</Text>
            <Text style={styles.meta}>Valid until {formatDate(quotation.validUntil)}</Text>
            {quotation.notes ? <Text style={styles.notes}>{quotation.notes}</Text> : null}
            {quotation.status === 'PENDING_APPROVAL' ? (
              <View style={styles.actions}>
                <View style={styles.action}><PrimaryButton label="Reject" tone="quiet" onPress={() => submitDecision(quotation.id, 'REJECT')} disabled={decide.isPending} /></View>
                <View style={styles.action}><PrimaryButton label="Approve" onPress={() => submitDecision(quotation.id, 'APPROVE')} loading={decide.isPending} /></View>
              </View>
            ) : null}
          </Card>
        ))}
      </LoadState>
    </Screen>
  )
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 15 },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  number: { color: colors.ink, fontSize: 16, fontWeight: '800' },
  amount: { color: colors.primary, fontSize: 25, fontWeight: '900' },
  meta: { color: colors.muted, fontSize: 12 },
  notes: { color: colors.ink, fontSize: 13, lineHeight: 19 },
  actions: { flexDirection: 'row', gap: 8, marginTop: 4 },
  action: { flex: 1 },
  success: { color: colors.success, fontSize: 13, fontWeight: '700' },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
})
