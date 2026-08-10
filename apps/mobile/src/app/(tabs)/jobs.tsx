import {
  getGetMyWorkOrdersQueryKey,
  useGetMyWorkOrders,
  useUpdateWorkOrderDiagnosticNotes,
  useUpdateWorkOrderStatus,
} from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, StatusPill, colors } from '../../components/ui'
import { WorkOrderDocuments } from '../../components/work-order-documents'
import { useAuth } from '../../lib/auth'
import { errorMessage } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import { Redirect } from 'expo-router'
import * as React from 'react'
import { StyleSheet, Text, TextInput, View } from 'react-native'
import type { WorkOrderDto } from '../../api/generated/models'

export default function TechnicianJobsScreen() {
  const { roles } = useAuth()
  const queryClient = useQueryClient()
  const workOrders = useGetMyWorkOrders()
  const updateStatus = useUpdateWorkOrderStatus()
  const updateNotes = useUpdateWorkOrderDiagnosticNotes()
  const [notes, setNotes] = React.useState<Record<string, string>>({})
  const [message, setMessage] = React.useState<string | null>(null)

  if (!roles.includes('TECHNICIAN')) return <Redirect href="/(tabs)" />

  const refresh = () => void queryClient.invalidateQueries({ queryKey: getGetMyWorkOrdersQueryKey() })
  const saveNotes = (workOrder: WorkOrderDto) => {
    if (!workOrder.id) return
    updateNotes.mutate(
      { id: workOrder.id, data: notes[workOrder.id] ?? workOrder.diagnosticNotes ?? '' },
      { onSuccess: refresh, onError: (error) => setMessage(errorMessage(error)) },
    )
  }
  const advance = (workOrder: WorkOrderDto) => {
    const target = nextStatus(workOrder.status)
    if (!workOrder.id || !target) return
    updateStatus.mutate(
      { id: workOrder.id, params: { status: target } },
      { onSuccess: refresh, onError: (error) => setMessage(errorMessage(error)) },
    )
  }

  return (
    <Screen>
      <Text style={styles.title}>My jobs</Text>
      <Text style={styles.subtitle}>Update assigned work, diagnostics, and shared evidence.</Text>
      {message ? <Text style={styles.error}>{message}</Text> : null}
      <LoadState loading={workOrders.isLoading} error={workOrders.error} empty={workOrders.data?.length === 0}>
        {workOrders.data?.map((workOrder) => {
          const target = nextStatus(workOrder.status)
          const currentNotes = notes[workOrder.id || ''] ?? workOrder.diagnosticNotes ?? ''
          return (
            <Card key={workOrder.id}>
              <View style={styles.row}>
                <Text numberOfLines={1} style={styles.id}>{workOrder.id || 'Work order'}</Text>
                <StatusPill value={workOrder.status} />
              </View>
              <Text style={styles.detail}>Vehicle: {workOrder.vehicleId || 'Not recorded'}</Text>
              <Text style={styles.detail}>Service: {workOrder.serviceId || 'Not recorded'}</Text>
              <Text style={styles.label}>Diagnostic notes</Text>
              <TextInput
                multiline
                onChangeText={(value) => {
                  if (workOrder.id) setNotes((current) => ({ ...current, [workOrder.id!]: value }))
                }}
                placeholder="Record findings for the service team and customer."
                placeholderTextColor={colors.muted}
                style={styles.input}
                value={currentNotes}
              />
              <PrimaryButton label="Save diagnostics" loading={updateNotes.isPending} onPress={() => saveNotes(workOrder)} tone="quiet" />
              {target ? <PrimaryButton label={`Mark ${target.replaceAll('_', ' ')}`} loading={updateStatus.isPending} onPress={() => advance(workOrder)} /> : null}
              {workOrder.id ? <WorkOrderDocuments workOrderId={workOrder.id} /> : null}
            </Card>
          )
        })}
      </LoadState>
    </Screen>
  )
}

function nextStatus(status?: string): 'DIAGNOSING' | 'IN_PROGRESS' | 'COMPLETED' | null {
  if (status === 'OPEN') return 'DIAGNOSING'
  if (status === 'DIAGNOSING') return 'IN_PROGRESS'
  if (status === 'IN_PROGRESS') return 'COMPLETED'
  return null
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 15 },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  id: { color: colors.primary, flex: 1, fontFamily: 'monospace', fontSize: 13, fontWeight: '800', marginRight: 12 },
  detail: { color: colors.muted, fontSize: 13 },
  label: { color: colors.ink, fontSize: 14, fontWeight: '800', marginTop: 4 },
  input: { borderColor: colors.line, borderRadius: 10, borderWidth: 1, color: colors.ink, minHeight: 88, padding: 12, textAlignVertical: 'top' },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
})
