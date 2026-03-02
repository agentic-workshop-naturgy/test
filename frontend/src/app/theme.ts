import { createTheme } from '@mui/material/styles'

const theme = createTheme({
  palette: {
    primary: {
      main: '#F47920',   // Naturgy orange
      dark: '#C85A00',
      light: '#FFA04D',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#E85E00',   // Naturgy burnt orange (CTA)
      contrastText: '#ffffff',
    },
    background: {
      default: '#FBF6F1',   // warm light neutral
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
