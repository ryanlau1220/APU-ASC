import {
  getGetMyFeedbackQueryKey,
  useGetMyFeedback,
  useGetMyWorkOrders,
  useSubmitFeedback,
} from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, StatusPill, colors } from '../../components/ui'
import { useAuth } from '../../lib/auth'
import { errorMessage, formatDate } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import { Redirect } from 'expo-router'
import * as React from 'react'
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native'

export default function FeedbackScreen() {
  const { roles } = useAuth()
  const queryClient = useQueryClient()
  const workOrders = useGetMyWorkOrders()
  const feedback = useGetMyFeedback()
  const submit = useSubmitFeedback()
  const [workOrderId, setWorkOrderId] = React.useState('')
  const [rating, setRating] = React.useState(0)
  const [comments, setComments] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  if (roles.includes('TECHNICIAN') || roles.includes('STAFF') || roles.includes('MANAGER')) {
    return <Redirect href="/(tabs)" />
  }

  const submittedWorkOrderIds = new Set(feedback.data?.map((entry) => entry.workOrderId).filter(Boolean))
  const eligibleWorkOrders = workOrders.data?.filter(
    (workOrder) => workOrder.status === 'COMPLETED' && workOrder.id && !submittedWorkOrderIds.has(workOrder.id),
  )

  const submitFeedback = () => {
    if (!workOrderId || rating < 1 || !comments.trim()) {
      setMessage('Choose a completed service, rating, and short review.')
      return
    }
    submit.mutate(
      { data: { workOrderId, rating, comments: comments.trim() } },
      {
        onSuccess: () => {
          setComments('')
          setMessage('Thank you. Your feedback has been submitted.')
          setRating(0)
          setWorkOrderId('')
          void queryClient.invalidateQueries({ queryKey: getGetMyFeedbackQueryKey() })
        },
        onError: (error) => setMessage(errorMessage(error)),
      },
    )
  }

  return (
    <Screen>
      <Text style={styles.title}>Feedback</Text>
      <Text style={styles.subtitle}>Share your experience after a completed service.</Text>

      <Text style={styles.sectionTitle}>Leave a review</Text>
      <Card>
        <Text style={styles.label}>Completed service</Text>
        <LoadState loading={workOrders.isLoading || feedback.isLoading} error={workOrders.error || feedback.error} empty={eligibleWorkOrders?.length === 0}>
          <View style={styles.choices}>
            {eligibleWorkOrders?.map((workOrder) => (
              <Pressable
                key={workOrder.id}
                accessibilityRole="radio"
                accessibilityState={{ selected: workOrderId === workOrder.id }}
                onPress={() => setWorkOrderId(workOrder.id || '')}
                style={({ pressed }) => [styles.choice, workOrderId === workOrder.id && styles.choiceSelected, pressed && styles.pressed]}
              >
                <Text numberOfLines={1} style={styles.choiceTitle}>{workOrder.id}</Text>
                <Text style={styles.choiceCopy}>{workOrder.serviceId || 'Service'} · {formatDate(workOrder.completedAt)}</Text>
              </Pressable>
            ))}
          </View>
        </LoadState>

        <Text style={styles.label}>Rating</Text>
        <View accessibilityRole="radiogroup" style={styles.ratings}>
          {[1, 2, 3, 4, 5].map((value) => (
            <Pressable
              key={value}
              accessibilityLabel={`${value} star${value === 1 ? '' : 's'}`}
              accessibilityRole="radio"
              accessibilityState={{ selected: rating === value }}
              onPress={() => setRating(value)}
              style={({ pressed }) => [styles.rating, rating === value && styles.ratingSelected, pressed && styles.pressed]}
            >
              <Text style={[styles.ratingText, rating === value && styles.ratingTextSelected]}>{value}</Text>
            </Pressable>
          ))}
        </View>

        <Text style={styles.label}>Your review</Text>
        <TextInput
          maxLength={1000}
          multiline
          onChangeText={setComments}
          placeholder="Tell us what went well or what we can improve."
          placeholderTextColor={colors.muted}
          style={styles.input}
          value={comments}
        />
        {message ? <Text style={message.startsWith('Thank') ? styles.success : styles.error}>{message}</Text> : null}
        <PrimaryButton label="Submit feedback" loading={submit.isPending} onPress={submitFeedback} />
      </Card>

      <Text style={styles.sectionTitle}>Your previous feedback</Text>
      <LoadState loading={feedback.isLoading} error={feedback.error} empty={feedback.data?.length === 0}>
        {feedback.data?.map((entry) => (
          <Card key={entry.id}>
            <View style={styles.row}>
              <Text style={styles.workOrder}>{entry.workOrderId || 'Service feedback'}</Text>
              <StatusPill value={`${entry.rating || 0} / 5`} />
            </View>
            <Text style={styles.comment}>{entry.comments || 'No written review.'}</Text>
            <Text style={styles.meta}>{formatDate(entry.createdAt)}</Text>
          </Card>
        ))}
      </LoadState>
    </Screen>
  )
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 15 },
  sectionTitle: { color: colors.ink, fontSize: 17, fontWeight: '800', marginTop: 8 },
  label: { color: colors.ink, fontSize: 14, fontWeight: '800', marginTop: 4 },
  choices: { gap: 8 },
  choice: { borderColor: colors.line, borderRadius: 10, borderWidth: 1, gap: 3, padding: 11 },
  choiceSelected: { backgroundColor: '#EAF1FF', borderColor: colors.primary },
  choiceTitle: { color: colors.ink, fontFamily: 'monospace', fontSize: 13, fontWeight: '800' },
  choiceCopy: { color: colors.muted, fontSize: 12 },
  ratings: { flexDirection: 'row', gap: 8 },
  rating: { alignItems: 'center', borderColor: colors.line, borderRadius: 10, borderWidth: 1, height: 42, justifyContent: 'center', width: 42 },
  ratingSelected: { backgroundColor: colors.primary, borderColor: colors.primary },
  ratingText: { color: colors.ink, fontWeight: '800' },
  ratingTextSelected: { color: colors.primaryInk },
  input: { borderColor: colors.line, borderRadius: 10, borderWidth: 1, color: colors.ink, minHeight: 100, padding: 12, textAlignVertical: 'top' },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  workOrder: { color: colors.primary, fontFamily: 'monospace', fontSize: 13, fontWeight: '800' },
  comment: { color: colors.ink, fontSize: 14, lineHeight: 20 },
  meta: { color: colors.muted, fontSize: 12 },
  success: { color: colors.success, fontSize: 13, fontWeight: '700' },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
  pressed: { opacity: 0.75 },
})
