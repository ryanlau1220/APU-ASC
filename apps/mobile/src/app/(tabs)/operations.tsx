import {
  getGetAllAppointmentsQueryKey,
  getGetAllWorkOrdersQueryKey,
  useCreateWorkOrder,
  useGetAllAppointments,
  useGetAllUsers,
  useGetAllWorkOrders,
  useUpdateAppointmentStatus,
  useUpdateWorkOrder,
} from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, StatusPill, colors } from '../../components/ui'
import { useAuth } from '../../lib/auth'
import { errorMessage, formatDate } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import { Redirect } from 'expo-router'
import * as React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'
import type { WorkOrderDto } from '../../api/generated/models'

export default function OperationsScreen() {
  const { roles } = useAuth()
  const queryClient = useQueryClient()
  const appointments = useGetAllAppointments()
  const users = useGetAllUsers()
  const workOrders = useGetAllWorkOrders()
  const confirmAppointment = useUpdateAppointmentStatus()
  const createWorkOrder = useCreateWorkOrder()
  const updateWorkOrder = useUpdateWorkOrder()
  const [assignment, setAssignment] = React.useState<Record<string, string>>({})
  const [message, setMessage] = React.useState<string | null>(null)

  if (!roles.includes('STAFF') && !roles.includes('MANAGER')) return <Redirect href="/(tabs)" />

  const technicians = users.data?.filter((user) => user.role === 'TECHNICIAN' && user.status === 'ACTIVE') || []
  const workOrderAppointmentIds = new Set(workOrders.data?.map((workOrder) => workOrder.appointmentId).filter(Boolean))
  const refreshAppointments = () => void queryClient.invalidateQueries({ queryKey: getGetAllAppointmentsQueryKey() })
  const refreshWorkOrders = () => void queryClient.invalidateQueries({ queryKey: getGetAllWorkOrdersQueryKey() })

  const confirm = (appointmentId?: string) => {
    if (!appointmentId) return
    confirmAppointment.mutate(
      { id: appointmentId, params: { status: 'CONFIRMED' } },
      { onSuccess: refreshAppointments, onError: (error) => setMessage(errorMessage(error)) },
    )
  }

  const openWorkOrder = (appointment: { id?: string }) => {
    if (!appointment.id) return
    createWorkOrder.mutate(
      { data: { appointmentId: appointment.id, technicianId: assignment[appointment.id] || undefined } },
      {
        onSuccess: () => {
          setMessage('Work order opened.')
          refreshAppointments()
          refreshWorkOrders()
        },
        onError: (error) => setMessage(errorMessage(error)),
      },
    )
  }

  const assign = (workOrder: WorkOrderDto, technicianId: string) => {
    if (!workOrder.id) return
    updateWorkOrder.mutate(
      { id: workOrder.id, data: { ...workOrder, technicianId } },
      { onSuccess: refreshWorkOrders, onError: (error) => setMessage(errorMessage(error)) },
    )
  }

  return (
    <Screen>
      <Text style={styles.title}>Operations</Text>
      <Text style={styles.subtitle}>Confirm bookings, open work orders, and assign technicians.</Text>
      {message ? <Text style={message === 'Work order opened.' ? styles.success : styles.error}>{message}</Text> : null}

      <Text style={styles.sectionTitle}>Appointment intake</Text>
      <LoadState loading={appointments.isLoading || workOrders.isLoading || users.isLoading} error={appointments.error || workOrders.error || users.error} empty={appointments.data?.length === 0}>
        {appointments.data?.map((appointment) => {
          const alreadyOpened = Boolean(appointment.id && workOrderAppointmentIds.has(appointment.id))
          const selectedTechnician = assignment[appointment.id || '']
          return (
            <Card key={appointment.id}>
              <View style={styles.row}>
                <Text style={styles.date}>{formatDate(appointment.appointmentDate)}</Text>
                <StatusPill value={appointment.status} />
              </View>
              <Text style={styles.detail}>Vehicle: {appointment.vehicleId || 'Not recorded'}</Text>
              <Text style={styles.detail}>Service: {appointment.serviceId || 'Not recorded'}</Text>
              <Text style={styles.detail}>Slot: {appointment.timeSlot || 'Not scheduled'}</Text>
              {appointment.status === 'PENDING' ? <PrimaryButton label="Confirm appointment" loading={confirmAppointment.isPending} onPress={() => confirm(appointment.id)} /> : null}
              {appointment.status === 'CONFIRMED' && !alreadyOpened ? (
                <>
                  <Text style={styles.label}>Assign technician (optional)</Text>
                  <TechnicianChoices
                    selectedId={selectedTechnician}
                    technicians={technicians}
                    onSelect={(technicianId) => {
                      if (appointment.id) setAssignment((current) => ({ ...current, [appointment.id!]: technicianId }))
                    }}
                  />
                  <PrimaryButton label="Open work order" loading={createWorkOrder.isPending} onPress={() => openWorkOrder(appointment)} />
                </>
              ) : null}
              {alreadyOpened ? <Text style={styles.complete}>Work order already opened.</Text> : null}
            </Card>
          )
        })}
      </LoadState>

      <Text style={styles.sectionTitle}>Work-order assignment</Text>
      <LoadState loading={workOrders.isLoading || users.isLoading} error={workOrders.error || users.error} empty={workOrders.data?.length === 0}>
        {workOrders.data?.map((workOrder) => (
          <Card key={workOrder.id}>
            <View style={styles.row}>
              <Text numberOfLines={1} style={styles.workOrderId}>{workOrder.id || 'Work order'}</Text>
              <StatusPill value={workOrder.status} />
            </View>
            <Text style={styles.detail}>Vehicle: {workOrder.vehicleId || 'Not recorded'}</Text>
            <Text style={styles.detail}>Current technician: {technicianName(workOrder.technicianId, technicians) || 'Unassigned'}</Text>
            <Text style={styles.label}>Reassign technician</Text>
            <TechnicianChoices
              selectedId={assignment[workOrder.id || ''] ?? workOrder.technicianId}
              technicians={technicians}
              onSelect={(technicianId) => {
                if (workOrder.id) setAssignment((current) => ({ ...current, [workOrder.id!]: technicianId }))
              }}
            />
            {assignment[workOrder.id || ''] && assignment[workOrder.id || ''] !== workOrder.technicianId ? (
              <PrimaryButton label="Save technician assignment" loading={updateWorkOrder.isPending} onPress={() => assign(workOrder, assignment[workOrder.id || ''])} tone="quiet" />
            ) : null}
          </Card>
        ))}
      </LoadState>
    </Screen>
  )
}

function TechnicianChoices({
  selectedId,
  technicians,
  onSelect,
}: {
  selectedId?: string
  technicians: Array<{ id?: string; fullName?: string; username?: string }>
  onSelect(technicianId: string): void
}) {
  if (technicians.length === 0) return <Text style={styles.muted}>No active technicians are available.</Text>
  return (
    <View style={styles.choices}>
      {technicians.map((technician) => (
        <Pressable
          key={technician.id}
          accessibilityRole="radio"
          accessibilityState={{ selected: technician.id === selectedId }}
          disabled={!technician.id}
          onPress={() => {
            if (technician.id) onSelect(technician.id)
          }}
          style={({ pressed }) => [styles.choice, technician.id === selectedId && styles.choiceSelected, pressed && styles.pressed]}
        >
          <Text style={[styles.choiceText, technician.id === selectedId && styles.choiceTextSelected]}>{technician.fullName || technician.username || 'Technician'}</Text>
        </Pressable>
      ))}
    </View>
  )
}

function technicianName(technicianId: string | undefined, technicians: Array<{ id?: string; fullName?: string; username?: string }>) {
  const technician = technicians.find((entry) => entry.id === technicianId)
  return technician?.fullName || technician?.username
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 15 },
  sectionTitle: { color: colors.ink, fontSize: 17, fontWeight: '800', marginTop: 8 },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  date: { color: colors.ink, fontSize: 16, fontWeight: '800' },
  workOrderId: { color: colors.primary, flex: 1, fontFamily: 'monospace', fontSize: 13, fontWeight: '800', marginRight: 12 },
  detail: { color: colors.muted, fontSize: 13 },
  label: { color: colors.ink, fontSize: 13, fontWeight: '800', marginTop: 4 },
  choices: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  choice: { borderColor: colors.line, borderRadius: 9, borderWidth: 1, paddingHorizontal: 10, paddingVertical: 8 },
  choiceSelected: { backgroundColor: '#EAF1FF', borderColor: colors.primary },
  choiceText: { color: colors.ink, fontSize: 12, fontWeight: '700' },
  choiceTextSelected: { color: colors.primary },
  complete: { color: colors.success, fontSize: 13, fontWeight: '700' },
  muted: { color: colors.muted, fontSize: 12 },
  success: { color: colors.success, fontSize: 13, fontWeight: '700' },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
  pressed: { opacity: 0.75 },
})
