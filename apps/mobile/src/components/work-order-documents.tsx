import { useGetWorkOrderDocuments } from '../api/generated/endpoints'
import { openProtectedDocument } from '../lib/apiClient'
import { errorMessage } from '../lib/format'
import { LoadState, colors } from './ui'
import * as React from 'react'
import { Pressable, StyleSheet, Text, View } from 'react-native'

export function WorkOrderDocuments({ workOrderId }: { workOrderId: string }) {
  const documents = useGetWorkOrderDocuments(workOrderId)
  const [openingId, setOpeningId] = React.useState<string | null>(null)
  const [message, setMessage] = React.useState<string | null>(null)

  const openDocument = async (id: string, fileName: string) => {
    setMessage(null)
    setOpeningId(id)
    try {
      await openProtectedDocument(id, fileName)
    } catch (error) {
      setMessage(errorMessage(error))
    } finally {
      setOpeningId(null)
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Workshop documents</Text>
      <LoadState loading={documents.isLoading} error={documents.error} empty={documents.data?.length === 0}>
        {documents.data?.map((document) => {
          const id = document.id || ''
          const fileName = document.fileName || 'Document'
          const opening = openingId === id
          return (
            <Pressable
              key={id}
              accessibilityHint="Downloads the protected document and opens the device share sheet."
              accessibilityLabel={`Open ${fileName}`}
              accessibilityRole="button"
              disabled={!id || opening}
              onPress={() => {
                if (id) void openDocument(id, fileName)
              }}
              style={({ pressed }) => [styles.document, opening && styles.disabled, pressed && styles.pressed]}
            >
              <View style={styles.copy}>
                <Text numberOfLines={1} style={styles.fileName}>{fileName}</Text>
                <Text style={styles.type}>{document.type?.replaceAll('_', ' ') || 'DOCUMENT'}</Text>
              </View>
              <Text style={styles.action}>{opening ? 'Opening…' : 'Open'}</Text>
            </Pressable>
          )
        })}
      </LoadState>
      {message ? <Text style={styles.error}>{message}</Text> : null}
    </View>
  )
}

const styles = StyleSheet.create({
  container: { borderTopColor: colors.line, borderTopWidth: 1, gap: 8, marginTop: 2, paddingTop: 12 },
  title: { color: colors.ink, fontSize: 13, fontWeight: '800' },
  document: { alignItems: 'center', backgroundColor: '#F4F7FB', borderRadius: 8, flexDirection: 'row', gap: 10, padding: 10 },
  copy: { flex: 1, gap: 2 },
  fileName: { color: colors.ink, fontSize: 13, fontWeight: '700' },
  type: { color: colors.muted, fontSize: 11, fontWeight: '700' },
  action: { color: colors.primary, fontSize: 12, fontWeight: '800' },
  error: { color: colors.danger, fontSize: 12, lineHeight: 17 },
  disabled: { opacity: 0.55 },
  pressed: { opacity: 0.75 },
})
