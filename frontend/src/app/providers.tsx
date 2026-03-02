import { ThemeProvider, CssBaseline } from '@mui/material'
import type { ReactNode } from 'react'
import theme from './theme'

export function Providers({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  )
}
