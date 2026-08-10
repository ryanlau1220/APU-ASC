import { createFileRoute } from '@tanstack/react-router'
import Footer from '../../components/Footer'
import Header from '../../components/Header'
import { WorkOrderOperations } from '../../components/WorkOrderOperations'

export const Route = createFileRoute('/manager/work-orders')({
  component: ManagerWorkOrdersContent,
})

function ManagerWorkOrdersContent() {
  return (
    <div className="flex min-h-screen flex-col bg-background text-foreground transition-colors">
      <Header />
      <main className="mx-auto w-full max-w-7xl flex-1 space-y-8 px-4 py-8 sm:px-6 lg:px-8">
        <WorkOrderOperations />
      </main>
      <Footer />
    </div>
  )
}
