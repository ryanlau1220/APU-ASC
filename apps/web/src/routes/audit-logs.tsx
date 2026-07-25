import { createFileRoute } from '@tanstack/react-router'
import { Activity, History, User } from 'lucide-react'
import Footer from '../components/Footer'
import Header from '../components/Header'

export const Route = createFileRoute('/audit-logs')({
  component: AuditLogsPage,
})

function AuditLogsPage() {
  const auditLogs = [
    {
      id: 'LOG-7001',
      actor: 'USR-301 (Sarah Manager)',
      action: 'APPOINTMENT_CONFIRMED',
      entity: 'Appointment APT-1001',
      details: 'Confirmed booking slot for Alex Tan (WXD 8821)',
      ipAddress: '192.168.1.45',
      timestamp: '2026-07-25 16:30:12',
    },
    {
      id: 'LOG-7002',
      actor: 'USR-201 (Master Tech Rahman)',
      action: 'FEEDBACK_RECORDED',
      entity: 'Feedback FBK-401',
      details: 'Added diagnostic notes for AC cooling repair',
      ipAddress: '192.168.1.88',
      timestamp: '2026-07-25 15:45:00',
    },
    {
      id: 'LOG-7003',
      actor: 'USR-103 (Devon Lee)',
      action: 'PAYMENT_COMPLETED',
      entity: 'Invoice INV-2026-001',
      details: 'Paid RM 150.00 via Credit Card',
      ipAddress: '175.143.22.10',
      timestamp: '2026-07-25 14:10:05',
    },
  ]

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
              modifications, and IP addresses.
            </p>
          </div>
        </section>

        {/* Audit Logs Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-border text-muted-foreground font-semibold">
                  <th className="py-3 px-3">Log ID</th>
                  <th className="py-3 px-3">Timestamp</th>
                  <th className="py-3 px-3">Actor</th>
                  <th className="py-3 px-3">Action</th>
                  <th className="py-3 px-3">Target Entity</th>
                  <th className="py-3 px-3">Details</th>
                  <th className="py-3 px-3">IP Address</th>
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
                      {log.timestamp}
                    </td>
                    <td className="py-3.5 px-3 font-medium flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5 text-primary" />
                      {log.actor}
                    </td>
                    <td className="py-3.5 px-3">
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-[10px] font-mono font-bold bg-primary/10 text-primary border border-primary/30">
                        <Activity className="w-3 h-3" />
                        {log.action}
                      </span>
                    </td>
                    <td className="py-3.5 px-3 font-medium">{log.entity}</td>
                    <td className="py-3.5 px-3 text-muted-foreground">
                      {log.details}
                    </td>
                    <td className="py-3.5 px-3 font-mono text-muted-foreground">
                      {log.ipAddress}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}
