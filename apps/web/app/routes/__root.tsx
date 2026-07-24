import { Link, Outlet, createRootRoute } from '@tanstack/react-router'
import { Wrench, Shield, LogIn, FileText, UserCheck } from 'lucide-react'

export const Route = createRootRoute({
  component: () => (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800 bg-slate-900/50 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 font-bold text-xl text-sky-400">
            <Wrench className="w-6 h-6" />
            <span>APU-ASC</span>
          </Link>
          <nav className="flex items-center gap-6 text-sm font-medium">
            <Link to="/" className="hover:text-sky-400 transition-colors">
              Dashboard
            </Link>
            <a href="http://localhost/docs" target="_blank" rel="noreferrer" className="flex items-center gap-1 hover:text-sky-400 transition-colors">
              <FileText className="w-4 h-4" />
              <span>Scalar API Docs</span>
            </a>
            <a href="http://localhost/auth" target="_blank" rel="noreferrer" className="flex items-center gap-1 hover:text-sky-400 transition-colors">
              <Shield className="w-4 h-4" />
              <span>Keycloak Auth</span>
            </a>
          </nav>
        </div>
      </header>
      <main className="flex-1 max-w-7xl w-full mx-auto p-6">
        <Outlet />
      </main>
      <footer className="border-t border-slate-800 py-6 text-center text-xs text-slate-500">
        APU Automotive Service Centre Enterprise Platform • Java 21 Spring Modulith & TanStack Start
      </footer>
    </div>
  ),
})
