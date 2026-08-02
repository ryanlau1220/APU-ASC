import { createFileRoute, Outlet } from '@tanstack/react-router'
import { RequireAuth } from '../../components/RequireAuth'

export const Route = createFileRoute('/customer')({
  component: CustomerLayout,
})

function CustomerLayout() {
  return (
    <RequireAuth allowedRoles={['CUSTOMER']}>
      <Outlet />
    </RequireAuth>
  )
}
