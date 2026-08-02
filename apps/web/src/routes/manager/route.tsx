import { createFileRoute, Outlet } from '@tanstack/react-router'
import { RequireAuth } from '../../components/RequireAuth'

export const Route = createFileRoute('/manager')({
  component: ManagerLayout,
})

function ManagerLayout() {
  return (
    <RequireAuth allowedRoles={['MANAGER', 'SYSTEM_ADMIN']}>
      <Outlet />
    </RequireAuth>
  )
}
