import * as React from 'react'

export type Theme = 'light' | 'dark'

interface ThemeContextType {
  theme: Theme
  toggleTheme: () => void
}

const ThemeContext = React.createContext<ThemeContextType | undefined>(
  undefined,
)

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setTheme] = React.useState<Theme>('dark')

  React.useEffect(() => {
    if (typeof window === 'undefined') return
    const root = document.documentElement
    const saved = localStorage.getItem('theme') as Theme
    if (saved) {
      setTheme(saved)
      root.classList.remove('light', 'dark')
      root.classList.add(saved)
      root.setAttribute('data-theme', saved)
    } else {
      const isDark =
        root.classList.contains('dark') ||
        root.getAttribute('data-theme') === 'dark'
      setTheme(isDark ? 'dark' : 'light')
    }
  }, [])

  const toggleTheme = React.useCallback(() => {
    setTheme((prev) => {
      const nextTheme = prev === 'light' ? 'dark' : 'light'
      if (typeof window !== 'undefined') {
        const root = document.documentElement
        root.classList.remove('light', 'dark')
        root.classList.add(nextTheme)
        root.setAttribute('data-theme', nextTheme)
        localStorage.setItem('theme', nextTheme)
      }
      return nextTheme
    })
  }, [])

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  const context = React.useContext(ThemeContext)
  if (!context) {
    // Fallback if rendered outside provider
    return {
      theme: 'dark' as Theme,
      toggleTheme: () => {},
    }
  }
  return context
}
