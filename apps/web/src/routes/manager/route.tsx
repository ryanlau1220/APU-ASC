import { createFileRoute, Outlet } from '@tanstack/react-router'
import { RequireAuth } from '../../components/RequireAuth'

export const Route = createFileRoute('/manager')({
  component: ManagerLayout,
})

function ManagerLayout() {
  return (
    <RequireAuth
      allowedRoles={[
        'MANAGER',
        'WORKSHOP_MANAGER',
        'SYSTEM_ADMIN',
        'ROLE_MANAGER',
        'ROLE_WORKSHOP_MANAGER',
        'ROLE_SYSTEM_ADMIN',
      ]}
    >
      <Outlet />
    </RequireAuth>
  )
}
