import { useQuery } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import { Activity, History, User } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { bffFetch } from '../lib/apiClient'

export const Route = createFileRoute('/audit-logs')({
  component: AuditLogsPage,
})

interface AuditLogDto {
  id: string
  userId: string
  actionType: string
  entityName: string
  details: string
  createdAt: string
}

function AuditLogsPage() {
  const { data: auditLogs = [], isLoading } = useQuery<AuditLogDto[]>({
    queryKey: ['audit-logs'],
    queryFn: () => bffFetch<AuditLogDto[]>('/api/v1/audit-logs'),
  })

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <History className="w-4 h-4" />
              Operational Audit Trail
            </div>
            <h1 className="font-heading text-2xl font-bold">
              System Audit Logs
            </h1>
            <p className="text-xs text-muted-foreground">
              Immutable audit history logging domain actions, actor IDs, entity
              modifications, and timestamps.
            </p>
          </div>
        </section>

        {/* Audit Logs Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          {isLoading ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              Loading audit log records...
            </div>
          ) : auditLogs.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No audit logs recorded yet in database.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">Log ID</th>
                    <th className="py-3 px-3">Timestamp</th>
                    <th className="py-3 px-3">Actor ID</th>
                    <th className="py-3 px-3">Action Type</th>
                    <th className="py-3 px-3">Target Entity</th>
                    <th className="py-3 px-3">Details</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {auditLogs.map((log) => (
                    <tr
                      key={log.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {log.id}
                      </td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {log.createdAt}
                      </td>
                      <td className="py-3.5 px-3 font-medium flex items-center gap-1.5">
                        <User className="w-3.5 h-3.5 text-primary" />
                        {log.userId || 'SYSTEM'}
                      </td>
                      <td className="py-3.5 px-3">
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-[10px] font-mono font-bold bg-primary/10 text-primary border border-primary/30">
                          <Activity className="w-3 h-3" />
                          {log.actionType}
                        </span>
                      </td>
                      <td className="py-3.5 px-3 font-medium">
                        {log.entityName}
                      </td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {log.details}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      <Footer />
    </div>
  )
}
