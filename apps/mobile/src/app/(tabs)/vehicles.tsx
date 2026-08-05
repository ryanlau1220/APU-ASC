import { getGetMyVehiclesQueryKey, useCreateVehicle, useGetMyVehicles } from '../../api/generated/endpoints'
import { Card, LoadState, PrimaryButton, Screen, colors } from '../../components/ui'
import { errorMessage } from '../../lib/format'
import { useQueryClient } from '@tanstack/react-query'
import * as React from 'react'
import { StyleSheet, Text, TextInput, View } from 'react-native'

export default function VehiclesScreen() {
  const queryClient = useQueryClient()
  const vehicles = useGetMyVehicles()
  const createVehicle = useCreateVehicle()
  const [licensePlate, setLicensePlate] = React.useState('')
  const [make, setMake] = React.useState('')
  const [model, setModel] = React.useState('')
  const [year, setYear] = React.useState('')
  const [message, setMessage] = React.useState<string | null>(null)

  const submit = () => {
    const yearOfManufacture = Number(year)
    if (!licensePlate.trim() || !make.trim() || !model.trim() || !Number.isInteger(yearOfManufacture)) {
      setMessage('Enter the vehicle plate, make, model, and manufacture year.')
      return
    }
    createVehicle.mutate(
      {
        data: {
          licensePlate: licensePlate.trim().toUpperCase(),
          make: make.trim(),
          model: model.trim(),
          yearOfManufacture,
        },
      },
      {
        onSuccess: () => {
          setLicensePlate('')
          setMake('')
          setModel('')
          setYear('')
          setMessage('Vehicle added.')
          void queryClient.invalidateQueries({ queryKey: getGetMyVehiclesQueryKey() })
        },
        onError: (error) => setMessage(errorMessage(error)),
      },
    )
  }

  return (
    <Screen>
      <Text style={styles.title}>My vehicles</Text>
      <Text style={styles.subtitle}>Keep your service vehicles ready for booking.</Text>
      <LoadState loading={vehicles.isLoading} error={vehicles.error} empty={vehicles.data?.length === 0}>
        {vehicles.data?.map((vehicle) => (
          <Card key={vehicle.id}>
            <Text style={styles.plate}>{vehicle.licensePlate}</Text>
            <Text style={styles.vehicle}>{vehicle.make} {vehicle.model}</Text>
            <Text style={styles.year}>{vehicle.yearOfManufacture}</Text>
          </Card>
        ))}
      </LoadState>

      <Text style={styles.sectionTitle}>Add a vehicle</Text>
      <Card>
        <TextInput autoCapitalize="characters" onChangeText={setLicensePlate} placeholder="Licence plate" placeholderTextColor={colors.muted} style={styles.input} value={licensePlate} />
        <TextInput onChangeText={setMake} placeholder="Make" placeholderTextColor={colors.muted} style={styles.input} value={make} />
        <TextInput onChangeText={setModel} placeholder="Model" placeholderTextColor={colors.muted} style={styles.input} value={model} />
        <TextInput keyboardType="number-pad" maxLength={4} onChangeText={setYear} placeholder="Year of manufacture" placeholderTextColor={colors.muted} style={styles.input} value={year} />
        {message ? <Text style={message === 'Vehicle added.' ? styles.success : styles.error}>{message}</Text> : null}
        <PrimaryButton label="Add vehicle" onPress={submit} loading={createVehicle.isPending} />
      </Card>
    </Screen>
  )
}

const styles = StyleSheet.create({
  title: { color: colors.ink, fontSize: 28, fontWeight: '800' },
  subtitle: { color: colors.muted, fontSize: 15 },
  sectionTitle: { color: colors.ink, fontSize: 17, fontWeight: '800', marginTop: 8 },
  plate: { color: colors.primary, fontSize: 17, fontWeight: '900', letterSpacing: 0.8 },
  vehicle: { color: colors.ink, fontSize: 16, fontWeight: '700' },
  year: { color: colors.muted, fontSize: 13 },
  input: { borderBottomColor: colors.line, borderBottomWidth: 1, color: colors.ink, fontSize: 15, minHeight: 46, paddingHorizontal: 2 },
  success: { color: colors.success, fontSize: 13, fontWeight: '700' },
  error: { color: colors.danger, fontSize: 13, fontWeight: '700' },
})
