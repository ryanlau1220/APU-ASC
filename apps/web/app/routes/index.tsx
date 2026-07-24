import { createFileRoute } from '@tanstack/react-router'
import { Car, Wrench, CreditCard, ShieldAlert, CheckCircle2, ArrowRight } from 'lucide-react'

export const Route = createFileRoute('/')({
  component: IndexComponent,
})

function IndexComponent() {
  return (
    <div className="space-y-8">
      <div className="bg-gradient-to-r from-sky-950 via-slate-900 to-slate-950 p-8 rounded-2xl border border-slate-800 relative overflow-hidden">
        <div className="relative z-10 max-w-2xl space-y-4">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-sky-500/10 border border-sky-500/20 text-sky-400 text-xs font-semibold">
            Java 21 Spring Modulith & Keycloak 24
          </span>
          <h1 className="text-4xl font-extrabold tracking-tight text-white">
            APU Automotive Service Centre
          </h1>
          <p className="text-slate-400 text-sm leading-relaxed">
            Enterprise Cloud-Native Platform with role-based workflows for Customers, Counter Staff, Technicians, and Managers.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card title="Customer Portal" role="CUSTOMER" icon={Car} desc="Book appointments, track service history, view invoices" color="text-emerald-400" />
        <Card title="Counter Staff Desk" role="STAFF" icon={CreditCard} desc="Customer registration, appointment assignment, payment processing" color="text-sky-400" />
        <Card title="Technician Bay" role="TECHNICIAN" icon={Wrench} desc="Job ticket queue, diagnostic inspection reports, diagnostic notes" color="text-amber-400" />
        <Card title="Manager Control" role="MANAGER" icon={ShieldAlert} desc="User CRUD, service catalog editor, financial reports, audit logs" color="text-purple-400" />
      </div>
    </div>
  )
}

function Card({ title, role, icon: Icon, desc, color }: { title: string; role: string; icon: any; desc: string; color: string }) {
  return (
    <div className="bg-slate-900/50 border border-slate-800 rounded-xl p-5 hover:border-slate-700 transition-all flex flex-col justify-between group">
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className={`p-2.5 rounded-lg bg-slate-800 ${color}`}>
            <Icon className="w-5 h-5" />
          </div>
          <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-slate-800 text-slate-400">
            {role}
          </span>
        </div>
        <h3 className="font-semibold text-slate-100 mb-1 group-hover:text-sky-400 transition-colors">{title}</h3>
        <p className="text-xs text-slate-400 leading-relaxed">{desc}</p>
      </div>
    </div>
  )
}
