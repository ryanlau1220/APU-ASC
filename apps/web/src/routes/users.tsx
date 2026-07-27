import { useQuery } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import {
  CheckCircle2,
  Filter,
  Shield,
  User as UserIcon,
  Users,
} from 'lucide-react'
import * as React from 'react'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { bffFetch } from '../lib/apiClient'

export const Route = createFileRoute('/users')({ component: UsersPage })

interface UserDto {
  id: string
  keycloakId: string
  username: string
  email: string
  fullName: string
  role: string
  status: string
  createdAt: string
}

function UsersPage() {
  const [activeRole, setActiveRole] = React.useState<string>('ALL')

  const { data: users = [], isLoading } = useQuery<UserDto[]>({
    queryKey: ['users'],
    queryFn: () => bffFetch<UserDto[]>('/api/v1/users'),
  })

  const filteredUsers =
    activeRole === 'ALL' ? users : users.filter((u) => u.role === activeRole)

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        <section className="bg-card border border-border rounded-xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-primary">
              <Users className="w-4 h-4" />
              User Account Directory
            </div>
            <h1 className="font-heading text-2xl font-bold">
              System Users & Roles
            </h1>
            <p className="text-xs text-muted-foreground">
              Manage accounts for customers, workshop technicians, counter
              staff, and service managers.
            </p>
          </div>
        </section>

        {/* Role Filters */}
        <section className="flex items-center gap-2 overflow-x-auto pb-2">
          <Filter className="w-4 h-4 text-muted-foreground mr-1 shrink-0" />
          {['ALL', 'CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER'].map((role) => (
            <button
              key={role}
              type="button"
              onClick={() => setActiveRole(role)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-colors border ${
                activeRole === role
                  ? 'bg-primary text-primary-foreground border-primary'
                  : 'bg-card text-muted-foreground border-border hover:text-foreground'
              }`}
            >
              {role}
            </button>
          ))}
        </section>

        {/* Users Table */}
        <section className="bg-card border border-border rounded-xl p-6 space-y-4">
          {isLoading ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              Loading user directory...
            </div>
          ) : filteredUsers.length === 0 ? (
            <div className="text-center py-8 text-xs text-muted-foreground">
              No registered user accounts found for role: {activeRole}.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground font-semibold">
                    <th className="py-3 px-3">ID</th>
                    <th className="py-3 px-3">Name</th>
                    <th className="py-3 px-3">Email Address</th>
                    <th className="py-3 px-3">Assigned Role</th>
                    <th className="py-3 px-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {filteredUsers.map((u) => (
                    <tr
                      key={u.id}
                      className="hover:bg-muted/50 transition-colors"
                    >
                      <td className="py-3.5 px-3 font-mono font-semibold">
                        {u.id}
                      </td>
                      <td className="py-3.5 px-3 font-medium flex items-center gap-2">
                        <UserIcon className="w-3.5 h-3.5 text-primary" />
                        {u.fullName || u.username}
                      </td>
                      <td className="py-3.5 px-3 text-muted-foreground">
                        {u.email}
                      </td>
                      <td className="py-3.5 px-3">
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-[11px] font-semibold bg-muted border border-border">
                          <Shield className="w-3 h-3 text-primary" />
                          {u.role}
                        </span>
                      </td>
                      <td className="py-3.5 px-3">
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold text-status-completed bg-status-completed/10 border border-status-completed/30">
                          <CheckCircle2 className="w-3 h-3" />
                          {u.status}
                        </span>
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
