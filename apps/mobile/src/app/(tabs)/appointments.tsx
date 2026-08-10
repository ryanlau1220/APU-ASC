import {
  getGetMyAppointmentsQueryKey,
  getGetSlotAvailabilityQueryKey,
  useCancelAppointment,
  useCreateAppointment,
  useGetMyAppointments,
  useGetMyVehicles,
  useGetMyWorkOrders,
  useGetServices,
  useGetSlotAvailability,
} from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, StatusPill, colors } from '../../components/ui'
import { WorkOrderDocuments } from '../../components/work-order-documents'
import { errorMessage, formatCurrency, formatDate } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import * as React from 'react'
import { Alert, Pressable, StyleSheet, Text, TextInput, View } from 'react-native'
import type { WorkOrderDto } from '../../api/generated/models'

const datePattern = /^\d{4}-\d{2}-\d{2}$/

export default function AppointmentsScreen() {
  const queryClient = useQueryClient()
  const appointments = useGetMyAppointments()
  const workOrders = useGetMyWorkOrders()
  const vehicles = useGetMyVehicles()
  const services = useGetServices()
  const [vehicleId, setVehicleId] = React.useState('')
  const [serviceId, setServiceId] = React.useState('')
  const [appointmentDate, setAppointmentDate] = React.useState('')
  const [timeSlot, setTimeSlot] = React.useState('')
  const [notes, setNotes] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)
  const hasValidDate = datePattern.test(appointmentDate)
  const availability = useGetSlotAvailability(
    { date: appointmentDate },
    { query: { enabled: hasValidDate } },
  )
  const cancel = useCancelAppointment()
  const create = useCreateAppointment()
  const selectedSlot = availability.data?.find((slot) => slot.timeSlot === timeSlot)

  React.useEffect(() => {
    if (selectedSlot?.bookable) return
    setTimeSlot('')
  }, [selectedSlot?.bookable])

  const refreshAppointments = () => {
    void queryClient.invalidateQueries({ queryKey: getGetMyAppointmentsQueryKey() })
    if (hasValidDate) {
      void queryClient.invalidateQueries({
        queryKey: getGetSlotAvailabilityQueryKey({ date: appointmentDate }),
      })
    }
  }

  const bookAppointment = () => {
    const today = new Date().toISOString().slice(0, 10)
    if (!vehicleId || !serviceId || !hasValidDate || appointmentDate < today || !timeSlot) {
      setMessage('Choose a vehicle, service, future date, and available time slot.')
      return
    }
    if (!selectedSlot?.bookable) {
      setMessage('Choose a time slot with remaining workshop capacity.')
      return
    }
    create.mutate(
      {
        data: {
          appointmentDate,
          notes: notes.trim() || undefined,
          serviceId,
          timeSlot,
          vehicleId,
        },
      },
      {
        onSuccess: () => {
          setMessage('Appointment booked.')
          setAppointmentDate('')
          setNotes('')
          setServiceId('')
          setTimeSlot('')
          setVehicleId('')
          refreshAppointments()
        },
        onError: (error) => setMessage(errorMessage(error)),
      },
    )
  }

  const cancelAppointment = (id?: string) => {
    if (!id) return
    Alert.alert('Cancel appointment?', 'This cannot be undone once confirmed.', [
      { text: 'Keep appointment', style: 'cancel' },
      {
        text: 'Cancel appointment',
        style: 'destructive',
        onPress: () => {
          cancel.mutate(
            { id },
            {
              onSuccess: () => {
                setMessage('Appointment cancelled.')
                refreshAppointments()
              },
              onError: (error) => setMessage(errorMessage(error)),
            },
          )
        },
      },
    ])
  }

  const isSuccess = message === 'Appointment booked.' || message === 'Appointment cancelled.'

  return (
    <Screen>
      <Text style={styles.title}>Appointments</Text>
      <Text style={styles.subtitle}>Book service when there is workshop capacity.</Text>

      <Text style={styles.sectionTitle}>Book an appointment</Text>
      <Card>
        <Text style={styles.label}>Vehicle</Text>
        <LoadState loading={vehicles.isLoading} error={vehicles.error} empty={vehicles.data?.length === 0}>
          <View style={styles.options}>
            {vehicles.data?.map((vehicle) => (
              <Choice
                key={vehicle.id}
                label={`${vehicle.licensePlate} · ${vehicle.make} ${vehicle.model}`}
                selected={vehicle.id === vehicleId}
                onPress={() => setVehicleId(vehicle.id || '')}
              />
            ))}
          </View>
        </LoadState>

        <Text style={styles.label}>Service package</Text>
        <LoadState loading={services.isLoading} error={services.error} empty={services.data?.length === 0}>
          <View style={styles.options}>
            {services.data?.map((service) => (
              <Choice
                key={service.id}
                label={`${service.name} · ${formatCurrency(service.basePrice)}`}
                selected={service.id === serviceId}
                onPress={() => setServiceId(service.id || '')}
              />
            ))}
          </View>
        </LoadState>

        <Text style={styles.label}>Appointment date</Text>
        <TextInput
          autoCapitalize="none"
          keyboardType="numbers-and-punctuation"
          maxLength={10}
          onChangeText={(value) => {
            setAppointmentDate(value)
            setTimeSlot('')
          }}
          placeholder="YYYY-MM-DD"
          placeholderTextColor={colors.muted}
          style={styles.input}
          value={appointmentDate}
        />
        <Text style={styles.hint}>Enter a date from today onwards.</Text>

        <Text style={styles.label}>Available time slot</Text>
        {!hasValidDate ? <Text style={styles.hint}>Enter a valid date to check workshop capacity.</Text> : null}
        {hasValidDate ? (
          <LoadState loading={availability.isLoading} error={availability.error} empty={availability.data?.length === 0}>
            <View style={styles.options}>
              {availability.data?.map((slot) => (
                <Choice
                  key={slot.timeSlot}
                  disabled={!slot.bookable}
                  label={`${slot.timeSlot} · ${slot.available ?? 0}/${slot.capacity ?? 0} available${slot.bookable ? '' : ' · Full'}`}
                  selected={slot.timeSlot === timeSlot}
                  onPress={() => setTimeSlot(slot.timeSlot || '')}
                />
              ))}
            </View>
          </LoadState>
        ) : null}

        <Text style={styles.label}>Notes (optional)</Text>
        <TextInput
          multiline
          onChangeText={setNotes}
          placeholder="e.g. engine noise check"
          placeholderTextColor={colors.muted}
          style={[styles.input, styles.notesInput]}
          value={notes}
        />
        {message ? <Text style={isSuccess ? styles.success : styles.error}>{message}</Text> : null}
        <PrimaryButton
          disabled={!selectedSlot?.bookable || vehicles.isLoading || services.isLoading}
          label="Confirm appointment"
          loading={create.isPending}
          onPress={bookAppointment}
        />
      </Card>

      <Text style={styles.sectionTitle}>Your schedule</Text>
      <LoadState loading={appointments.isLoading} error={appointments.error} empty={appointments.data?.length === 0}>
        {appointments.data?.map((appointment) => (
          <Card key={appointment.id}>
            <View style={styles.row}>
              <Text style={styles.date}>{formatDate(appointment.appointmentDate)}</Text>
              <StatusPill value={appointment.status} />
            </View>
            <Text style={styles.time}>{appointment.timeSlot || 'Time to be confirmed'}</Text>
            {appointment.notes ? <Text style={styles.note}>{appointment.notes}</Text> : null}
            {appointment.status === 'PENDING' ? (
              <PrimaryButton
                label="Cancel appointment"
                loading={cancel.isPending}
                onPress={() => cancelAppointment(appointment.id)}
                tone="quiet"
              />
            ) : null}
          </Card>
        ))}
      </LoadState>

      <Text style={styles.sectionTitle}>Service progress & documents</Text>
      <Text style={styles.hint}>Track work after vehicle check-in and see evidence shared by the workshop.</Text>
      <LoadState loading={workOrders.isLoading} error={workOrders.error} empty={workOrders.data?.length === 0}>
        {workOrders.data?.map((workOrder) => (
          <WorkOrderProgressCard key={workOrder.id} workOrder={workOrder} />
        ))}
      </LoadState>
    </Screen>
  )
}

function WorkOrderProgressCard({ workOrder }: { workOrder: WorkOrderDto }) {
  return (
    <Card>
      <View style={styles.row}>
        <Text numberOfLines={1} style={styles.workOrderId}>{workOrder.id || 'Work order'}</Text>
        <StatusPill value={workOrder.status} />
      </View>
      <Text style={styles.progress}>{workOrderProgressCopy(workOrder.status)}</Text>
      <View style={styles.detailGrid}>
        <View style={styles.detailCell}>
          <Text style={styles.detailLabel}>Vehicle</Text>
          <Text numberOfLines={1} style={styles.detailValue}>{workOrder.vehicleId || 'Not recorded'}</Text>
        </View>
        <View style={styles.detailCell}>
          <Text style={styles.detailLabel}>Service</Text>
          <Text numberOfLines={1} style={styles.detailValue}>{workOrder.serviceId || 'Not recorded'}</Text>
        </View>
      </View>
      {workOrder.id ? <WorkOrderDocuments workOrderId={workOrder.id} /> : null}
    </Card>
  )
}

function workOrderProgressCopy(status?: string) {
  switch (status) {
    case 'DIAGNOSING':
      return 'The workshop is assessing your vehicle.'
    case 'IN_PROGRESS':
      return 'Service work is currently underway.'
    case 'COMPLETED':
      return 'Your service work has been completed.'
    case 'CANCELLED':
      return 'This work order was cancelled.'
    default:
      return 'Your vehicle has been checked in and is awaiting assessment.'
  }
}

function Choice({
  label,
  selected,
  disabled = false,
  onPress,
}: {
  label: string
  selected: boolean
  disabled?: boolean
  onPress(): void
}) {
  return (
    <Pressable
      accessibilityRole="radio"
      accessibilityState={{ disabled, selected }}
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.choice,
        selected && styles.choiceSelected,
        disabled && styles.choiceDisabled,
        pressed && styles.pressed,
      ]}
    >
      <Text style={[styles.choiceLabel, selected && styles.choiceLabelSelected]}>{label}</Text>
    </Pressable>
  )
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 15, marginBottom: 4 },
  sectionTitle: { color: colors.ink, fontSize: 17, fontWeight: '800', marginTop: 8 },
  label: { color: colors.ink, fontSize: 14, fontWeight: '800', marginTop: 4 },
  hint: { color: colors.muted, fontSize: 12, lineHeight: 17 },
  options: { gap: 8 },
  choice: { borderColor: colors.line, borderRadius: 10, borderWidth: 1, padding: 11 },
  choiceSelected: { backgroundColor: '#EAF1FF', borderColor: colors.primary },
  choiceDisabled: { opacity: 0.45 },
  choiceLabel: { color: colors.ink, fontSize: 13, fontWeight: '600' },
  choiceLabelSelected: { color: colors.primary, fontWeight: '800' },
  input: { borderBottomColor: colors.line, borderBottomWidth: 1, color: colors.ink, fontSize: 15, minHeight: 46, paddingHorizontal: 2 },
  notesInput: { minHeight: 74, paddingTop: 10, textAlignVertical: 'top' },
  row: { alignItems: 'center', flexDirection: 'row', justifyContent: 'space-between' },
  date: { color: colors.ink, fontSize: 18, fontWeight: '800' },
  time: { color: colors.primary, fontSize: 15, fontWeight: '700' },
  note: { color: colors.muted, fontSize: 13, lineHeight: 19 },
  workOrderId: { color: colors.primary, flex: 1, fontFamily: 'monospace', fontSize: 13, fontWeight: '800', marginRight: 12 },
  progress: { color: colors.muted, fontSize: 13, lineHeight: 19 },
  detailGrid: { flexDirection: 'row', gap: 16 },
  detailCell: { flex: 1, gap: 2 },
  detailLabel: { color: colors.muted, fontSize: 11, fontWeight: '700', textTransform: 'uppercase' },
  detailValue: { color: colors.ink, fontSize: 13, fontWeight: '700' },
  success: { color: colors.success, fontSize: 13, fontWeight: '700' },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
  pressed: { opacity: 0.75 },
})
