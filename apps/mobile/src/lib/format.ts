export function formatCurrency(value?: number) {
  return new Intl.NumberFormat('en-MY', {
    style: 'currency',
    currency: 'MYR',
    minimumFractionDigits: 2,
  }).format(value ?? 0)
}

export function formatDate(value?: string) {
  if (!value) return 'Not scheduled'
  const date = new Date(value.includes('T') ? value : `${value}T00:00:00`)
  return Number.isNaN(date.valueOf())
    ? value
    : new Intl.DateTimeFormat('en-MY', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      }).format(date)
}

export function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : 'Something went wrong. Please try again.'
}
