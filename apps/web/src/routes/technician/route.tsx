import { createFileRoute, Outlet } from '@tanstack/react-router'
import { RequireAuth } from '../../components/RequireAuth'

export const Route = createFileRoute('/technician')({
  component: TechnicianLayout,
})

function TechnicianLayout() {
  return (
    <RequireAuth
      allowedRoles={[
        'TECHNICIAN',
        'ROLE_TECHNICIAN',
        'MANAGER',
        'WORKSHOP_MANAGER',
        'SYSTEM_ADMIN',
      ]}
    >
      <Outlet />
    </RequireAuth>
  )
}
