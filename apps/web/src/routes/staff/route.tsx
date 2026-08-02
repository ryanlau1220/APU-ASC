import { createFileRoute, Outlet } from '@tanstack/react-router'
import { RequireAuth } from '../../components/RequireAuth'

export const Route = createFileRoute('/staff')({
  component: StaffLayout,
})

function StaffLayout() {
  return (
    <RequireAuth allowedRoles={['STAFF', 'MANAGER', 'SYSTEM_ADMIN']}>
      <Outlet />
    </RequireAuth>
  )
}
