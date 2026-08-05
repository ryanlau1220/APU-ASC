import { createFileRoute, Link } from '@tanstack/react-router'
import { Bell, CheckCheck, Circle, Inbox } from 'lucide-react'
import {
  useGetMyNotifications,
  useGetMyPreferences,
  useMarkAllNotificationsRead,
  useMarkNotificationRead,
} from '../api/generated/endpoints'
import type { NotificationDto } from '../api/generated/models'
import Footer from '../components/Footer'
import Header from '../components/Header'
import { RequireAuth } from '../components/RequireAuth'
import { queryClient } from '../lib/queryClient'

export const Route = createFileRoute('/notifications')({
  head: () => ({
    meta: [{ title: 'Notifications | APU Automotive Service Centre' }],
  }),
  component: NotificationsRoute,
})

function NotificationsRoute() {
  return (
    <RequireAuth allowedRoles={['CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER']}>
      <NotificationsPage />
    </RequireAuth>
  )
}

function NotificationsPage() {
  const { data = [], isLoading } = useGetMyNotifications({ limit: 100 })
  const { data: preferences } = useGetMyPreferences()
  const markRead = useMarkNotificationRead<Error>()
  const markAllRead = useMarkAllNotificationsRead<Error>()
  const unreadCount = data.filter((notification) => !notification.readAt).length

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['/api/v1/notifications'] })
    queryClient.invalidateQueries({
      queryKey: ['/api/v1/notifications/unread-count'],
    })
  }

  const read = (notification: NotificationDto) => {
    if (!notification.id || notification.readAt) return
    markRead.mutate({ id: notification.id }, { onSuccess: refresh })
  }

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground transition-colors">
      <Header />
      <main className="flex-1 w-full max-w-3xl mx-auto px-4 sm:px-6 py-8 space-y-6">
        <section className="rounded-xl border border-primary/30 bg-card p-6 sm:p-8 flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <div className="inline-flex items-center gap-2 text-xs font-semibold text-primary">
              <Bell className="h-4 w-4" /> Private inbox
            </div>
            <h1 className="mt-2 font-heading text-2xl font-bold">
              Notifications
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">
              {unreadCount > 0
                ? `${unreadCount} notification${unreadCount === 1 ? '' : 's'} unread`
                : 'You are all caught up.'}
            </p>
          </div>
          {unreadCount > 0 && (
            <button
              type="button"
              onClick={() =>
                markAllRead.mutate(undefined, { onSuccess: refresh })
              }
              disabled={markAllRead.isPending}
              className="inline-flex items-center justify-center gap-2 rounded-lg border border-border px-4 py-2 text-sm font-semibold hover:bg-muted disabled:opacity-50"
            >
              <CheckCheck className="h-4 w-4 text-primary" /> Mark all read
            </button>
          )}
        </section>

        {isLoading ? (
          <section className="rounded-xl border border-border bg-card p-10 text-center text-sm text-muted-foreground">
            Loading notifications…
          </section>
        ) : data.length === 0 ? (
          <section className="rounded-xl border border-dashed border-border bg-card p-10 text-center">
            <Inbox className="mx-auto h-8 w-8 text-muted-foreground" />
            <h2 className="mt-3 font-heading font-bold">
              No notifications yet
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Appointment, quotation, and job updates will appear here.
            </p>
          </section>
        ) : (
          <section className="overflow-hidden rounded-xl border border-border bg-card divide-y divide-border">
            {data.map((notification) => {
              const destination = notification.link || '/notifications'
              const unread = !notification.readAt

              return (
                <Link
                  key={notification.id}
                  to={destination as never}
                  onClick={() => read(notification)}
                  className={`block p-5 transition-colors hover:bg-muted/70 ${
                    unread ? 'bg-primary/[0.035]' : ''
                  }`}
                >
                  <div className="flex gap-3">
                    <Circle
                      className={`mt-1 h-3 w-3 shrink-0 ${
                        unread
                          ? 'fill-primary text-primary'
                          : 'text-transparent'
                      }`}
                      aria-label={unread ? 'Unread' : 'Read'}
                    />
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                        <h2 className="font-semibold text-sm">
                          {notification.title}
                        </h2>
                        <time className="text-xs text-muted-foreground">
                          {formatTimestamp(
                            notification.createdAt,
                            preferences?.timeZone,
                          )}
                        </time>
                      </div>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {notification.body}
                      </p>
                    </div>
                  </div>
                </Link>
              )
            })}
          </section>
        )}
      </main>
      <Footer />
    </div>
  )
}

function formatTimestamp(value?: string, timeZone?: string) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return new Intl.DateTimeFormat('en-MY', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone,
  }).format(date)
}
