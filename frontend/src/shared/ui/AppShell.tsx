import { useState } from 'react'
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  Typography,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  IconButton,
  Divider,
} from '@mui/material'
import MenuIcon from '@mui/icons-material/Menu'
import LocationOnIcon from '@mui/icons-material/LocationOn'
import SpeedIcon from '@mui/icons-material/Speed'
import LocalOfferIcon from '@mui/icons-material/LocalOffer'
import ScienceIcon from '@mui/icons-material/Science'
import PercentIcon from '@mui/icons-material/Percent'
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong'
import { useNavigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'

const DRAWER_WIDTH = 240

const NAV_ITEMS = [
  { label: 'Puntos de Suministro', path: '/supply-points', icon: <LocationOnIcon /> },
  { label: 'Lecturas', path: '/readings', icon: <SpeedIcon /> },
  { label: 'Tarifas', path: '/tariffs', icon: <LocalOfferIcon /> },
  { label: 'Factores Conversión', path: '/factors', icon: <ScienceIcon /> },
  { label: 'IVA / Impuestos', path: '/taxes', icon: <PercentIcon /> },
  { label: 'Facturación', path: '/billing', icon: <ReceiptLongIcon /> },
]

export function AppShell({ children }: { children: ReactNode }) {
  const [mobileOpen, setMobileOpen] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  const drawerContent = (
    <Box>
      <Toolbar sx={{ bgcolor: 'primary.main' }}>
        <Typography variant="h6" color="primary.contrastText" noWrap>
          GAS Workshop
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {NAV_ITEMS.map(({ label, path, icon }) => (
          <ListItemButton
            key={path}
            selected={location.pathname === path}
            onClick={() => { navigate(path); setMobileOpen(false) }}
          >
            <ListItemIcon sx={{ color: location.pathname === path ? 'secondary.main' : 'inherit' }}>
              {icon}
            </ListItemIcon>
            <ListItemText primary={label} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{ width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` }, ml: { sm: `${DRAWER_WIDTH}px` } }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setMobileOpen(!mobileOpen)}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap>
            Facturación Gas — Naturgy
          </Typography>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { sm: DRAWER_WIDTH }, flexShrink: { sm: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}
        >
          {drawerContent}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' } }}
          open
        >
          {drawerContent}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` }, mt: 8 }}
      >
        {children}
      </Box>
    </Box>
  )
}
