import { Camera, Download, FileText, Loader2, Upload } from 'lucide-react'
import * as React from 'react'
import {
  useGetWorkOrderDocuments,
  useUploadWorkOrderDocument,
} from '../api/generated/endpoints'
import type { UploadWorkOrderDocumentType as DocumentType } from '../api/generated/models'

const DOCUMENT_TYPES: ReadonlyArray<{ value: DocumentType; label: string }> = [
  { value: 'VEHICLE_CONDITION', label: 'Vehicle condition' },
  { value: 'DIAGNOSTIC_EVIDENCE', label: 'Diagnostic evidence' },
  { value: 'REPAIR_EVIDENCE', label: 'Repair evidence' },
  { value: 'REPORT', label: 'Report' },
  { value: 'INVOICE', label: 'Invoice' },
  { value: 'OTHER', label: 'Other' },
]

const MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024

export function WorkOrderDocuments({ workOrderId }: { workOrderId: string }) {
  const {
    data: documents = [],
    isLoading,
    refetch,
  } = useGetWorkOrderDocuments(workOrderId)
  const upload = useUploadWorkOrderDocument<Error>()
  const [type, setType] = React.useState<DocumentType>('REPAIR_EVIDENCE')
  const [message, setMessage] = React.useState<string | null>(null)

  const uploadFile = (file?: File) => {
    if (!file) return
    if (file.size > MAX_FILE_SIZE_BYTES) {
      setMessage('Files must be 10 MB or smaller.')
      return
    }
    setMessage(null)
    upload.mutate(
      { workOrderId, data: { file }, params: { type } },
      {
        onSuccess: () => {
          setMessage('Evidence uploaded.')
          void refetch()
        },
        onError: (error) => setMessage(error.message || 'Upload failed.'),
      },
    )
  }

  return (
    <section className="space-y-3 border-t border-border pt-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="flex items-center gap-1.5 text-xs font-semibold text-foreground">
            <FileText className="h-4 w-4 text-primary" /> Evidence & documents
          </h3>
          <p className="mt-1 text-[11px] text-muted-foreground">
            JPEG, PNG, WebP, or PDF up to 10 MB.
          </p>
        </div>
        {upload.isPending && (
          <Loader2 className="h-4 w-4 animate-spin text-primary" />
        )}
      </div>

      <div className="flex flex-col gap-2 sm:flex-row">
        <select
          aria-label="Document type"
          value={type}
          onChange={(event) => setType(event.target.value as DocumentType)}
          className="min-w-0 flex-1 rounded-lg border border-border bg-input px-2.5 py-2 text-xs outline-none focus:border-primary"
        >
          {DOCUMENT_TYPES.map((documentType) => (
            <option key={documentType.value} value={documentType.value}>
              {documentType.label}
            </option>
          ))}
        </select>
        <div className="flex gap-2">
          <label className="inline-flex flex-1 cursor-pointer items-center justify-center gap-1.5 rounded-lg border border-primary/40 px-3 py-2 text-xs font-semibold text-primary hover:bg-primary/10">
            <Camera className="h-3.5 w-3.5" /> Take photo
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              capture="environment"
              className="sr-only"
              onChange={(event) => uploadFile(event.target.files?.[0])}
            />
          </label>
          <label className="inline-flex flex-1 cursor-pointer items-center justify-center gap-1.5 rounded-lg border border-border px-3 py-2 text-xs font-semibold hover:bg-muted">
            <Upload className="h-3.5 w-3.5" /> File
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp,application/pdf"
              className="sr-only"
              onChange={(event) => uploadFile(event.target.files?.[0])}
            />
          </label>
        </div>
      </div>

      {message && (
        <output
          className={`text-[11px] font-medium ${
            message === 'Evidence uploaded.'
              ? 'text-status-completed'
              : 'text-destructive'
          }`}
        >
          {message}
        </output>
      )}

      {isLoading ? (
        <p className="text-[11px] text-muted-foreground">Loading documents…</p>
      ) : documents.length === 0 ? (
        <p className="text-[11px] text-muted-foreground">
          No evidence uploaded yet.
        </p>
      ) : (
        <ul className="space-y-1.5">
          {documents.map((document) => (
            <li
              key={document.id}
              className="flex items-center justify-between gap-2 rounded-lg bg-muted/50 px-2.5 py-2 text-[11px]"
            >
              <span className="min-w-0 truncate text-foreground">
                {document.fileName}
              </span>
              {document.id && (
                <a
                  href={`/api/v1/documents/${document.id}/download`}
                  className="inline-flex shrink-0 items-center gap-1 font-semibold text-primary hover:underline"
                >
                  <Download className="h-3.5 w-3.5" /> Download
                </a>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
