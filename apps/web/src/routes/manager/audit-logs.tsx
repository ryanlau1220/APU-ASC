import { createFileRoute } from '@tanstack/react-router'
import {
  Activity,
  Calendar,
  Filter,
  RefreshCw,
  Search,
  ShieldCheck,
  User,
} from 'lucide-react'
import * as React from 'react'
import { useGetAuditLogs } from '../../api/generated/endpoints'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { RequireAuth } from '../../components/RequireAuth'

export const Route = createFileRoute('/manager/audit-logs')({
  head: () => ({
    meta: [
      {
        title: 'Audit Compliance Logs | APU Automotive Service Centre Manager',
      },
    ],
  }),
  component: ManagerAuditLogsPage,
})

function ManagerAuditLogsPage() {
  return (
    <RequireAuth allowedRoles={['MANAGER', 'SYSTEM_ADMIN']}>
      <AuditLogsContent />
    </RequireAuth>
  )
}

function AuditLogsContent() {
  const { data: auditLogs, isLoading, refetch } = useGetAuditLogs()
  const [searchTerm, setSearchTerm] = React.useState('')
  const [actionFilter, setActionFilter] = React.useState('ALL')

  const logsList = auditLogs || []

  const filteredLogs = logsList.filter((log) => {
    const matchesSearch =
      !searchTerm ||
      log.userId?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.entityName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.details?.toLowerCase().includes(searchTerm.toLowerCase())

    const matchesAction =
      actionFilter === 'ALL' || log.actionType === actionFilter

    return matchesSearch && matchesAction
  })

  const uniqueActions = Array.from(
    new Set(logsList.map((l) => l.actionType).filter(Boolean)),
  )

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        {/* Header Section */}
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <ShieldCheck className="w-4 h-4" />
              Executive Compliance & Security
            </div>
            <h1 className="font-heading text-2xl font-bold">
              System Audit Event Trail
            </h1>
            <p className="text-xs text-muted-foreground">
              Immutable partition-logged security and business action logs.
            </p>
          </div>

          <button
            type="button"
            onClick={() => refetch()}
            className="inline-flex items-center justify-center gap-2 px-4 py-2 rounded-lg bg-primary text-primary-foreground text-xs font-semibold hover:bg-primary/90 transition-colors shadow-sm self-start md:self-auto"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            Refresh Trail
          </button>
        </section>

        {/* Filter Toolbar */}
        <section className="bg-card border border-border rounded-xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              placeholder="Filter by user ID, entity name, or details..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-lg bg-muted border border-border text-xs focus:border-primary outline-none transition-colors"
            />
          </div>

          <div className="flex items-center gap-2 w-full sm:w-auto">
            <Filter className="w-4 h-4 text-muted-foreground shrink-0" />
            <select
              value={actionFilter}
              onChange={(e) => setActionFilter(e.target.value)}
              aria-label="Filter audit logs by action type"
              className="w-full sm:w-48 py-2 px-3 rounded-lg bg-muted border border-border text-xs font-medium focus:border-primary outline-none"
            >
              <option value="ALL">All Action Types</option>
              {uniqueActions.map((action) => (
                <option key={action} value={action}>
                  {action}
                </option>
              ))}
            </select>
          </div>
        </section>

        {/* Audit Log Table */}
        <section className="bg-card border border-border rounded-xl overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-muted/50 border-b border-border text-muted-foreground font-semibold uppercase tracking-wider">
                  <th className="py-3 px-4">Timestamp</th>
                  <th className="py-3 px-4">User ID</th>
                  <th className="py-3 px-4">Action</th>
                  <th className="py-3 px-4">Entity</th>
                  <th className="py-3 px-4">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {isLoading ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="py-8 text-center text-muted-foreground"
                    >
                      Loading audit compliance trail...
                    </td>
                  </tr>
                ) : filteredLogs.length === 0 ? (
                  <tr>
                    <td
                      colSpan={5}
                      className="py-8 text-center text-muted-foreground"
                    >
                      No audit log entries found matching criteria.
                    </td>
                  </tr>
                ) : (
                  filteredLogs.map((log) => (
                    <tr
                      key={log.id}
                      className="hover:bg-muted/30 transition-colors font-mono"
                    >
                      <td className="py-3.5 px-4 text-muted-foreground whitespace-nowrap">
                        <div className="flex items-center gap-1.5">
                          <Calendar className="w-3.5 h-3.5 text-primary shrink-0" />
                          {log.createdAt
                            ? new Date(log.createdAt).toLocaleString()
                            : 'N/A'}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 font-semibold text-foreground whitespace-nowrap">
                        <div className="flex items-center gap-1.5">
                          <User className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
                          {log.userId || 'SYSTEM'}
                        </div>
                      </td>
                      <td className="py-3.5 px-4">
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-primary/10 text-primary border border-primary/20 text-[11px] font-bold">
                          <Activity className="w-3 h-3" />
                          {log.actionType}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 font-bold text-foreground">
                        {log.entityName}
                      </td>
                      <td className="py-3.5 px-4 text-muted-foreground font-sans max-w-md truncate">
                        {log.details}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
