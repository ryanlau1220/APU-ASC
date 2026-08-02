import { Moon, Sun } from 'lucide-react'
import { useTheme } from '../lib/useTheme'

export default function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()

  return (
    <button
      type="button"
      onClick={toggleTheme}
      className="p-2 rounded-md border border-border bg-card hover:bg-muted text-foreground transition-colors"
      title="Toggle theme"
    >
      {theme === 'dark' ? (
        <Sun className="w-4 h-4 text-primary" />
      ) : (
        <Moon className="w-4 h-4 text-primary" />
      )}
    </button>
  )
}
