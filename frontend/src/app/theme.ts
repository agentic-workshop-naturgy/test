import { createTheme } from '@mui/material/styles'

const theme = createTheme({
  palette: {
    primary: {
      main: '#1a2b4a',   // Naturgy navy
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#28a745',   // Naturgy green CTA
      contrastText: '#ffffff',
    },
    background: {
      default: '#f4f6f9',
      paper: '#ffffff',
    },
  },
  typography: {
    fontFamily: '"Roboto","Helvetica","Arial",sans-serif',
    h4: { fontWeight: 700 },
    h6: { fontWeight: 600 },
  },
  shape: { borderRadius: 8 },
})

export default theme
