import { useEffect, useState } from 'react'
import {
  Box, Button, Typography, Alert, Snackbar, LinearProgress,
  TextField, Container, Stack, Paper, Divider,
  Table, TableHead, TableRow, TableCell, TableBody,
  Chip, IconButton, Collapse,
} from '@mui/material'
import ReceiptIcon from '@mui/icons-material/Receipt'
import DownloadIcon from '@mui/icons-material/Download'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import ExpandLessIcon from '@mui/icons-material/ExpandLess'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import { DataGrid, type GridColDef, type GridRenderCellParams } from '@mui/x-data-grid'
import { billingApi } from '../api/billingApi'
import type { Invoice, BillingResultDto } from '../types'
import { mapError } from '../../../shared/api/httpClient'

export function BillingPage() {
  const [period, setPeriod] = useState('')
  const [periodError, setPeriodError] = useState('')
  const [running, setRunning] = useState(false)
  const [billingResult, setBillingResult] = useState<BillingResultDto | null>(null)
  const [billingErr, setBillingErr] = useState<string | null>(null)

  const [invoices, setInvoices] = useState<Invoice[]>([])
  const [loadingInvoices, setLoadingInvoices] = useState(true)
  const [invoiceError, setInvoiceError] = useState<string | null>(null)
  const [filterCups, setFilterCups] = useState('')
  const [filterPeriod, setFilterPeriod] = useState('')
  const [expandedId, setExpandedId] = useState<number | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const loadInvoices = () => {
    setLoadingInvoices(true)
    setInvoiceError(null)
    const params: Record<string, string> = {}
    if (filterCups.trim()) params['cups'] = filterCups.trim()
    if (filterPeriod.trim()) params['period'] = filterPeriod.trim()
    billingApi.findInvoices(params)
      .then(setInvoices)
      .catch(e => setInvoiceError(mapError(e).message))
      .finally(() => setLoadingInvoices(false))
  }

  useEffect(() => {
    let cancelled = false
    const params: Record<string, string> = {}
    void billingApi.findInvoices(params)
      .then(data => { if (!cancelled) setInvoices(data) })
      .catch(e => { if (!cancelled) setInvoiceError(mapError(e).message) })
      .finally(() => { if (!cancelled) setLoadingInvoices(false) })
    return () => { cancelled = true }
  }, [])

  const handleRunBilling = () => {
    if (!period.match(/^\d{4}-\d{2}$/)) {
      setPeriodError('Formato requerido: YYYY-MM (e.g. 2026-01)')
      return
    }
    setPeriodError('')
    setRunning(true)
    setBillingResult(null)
    setBillingErr(null)
    billingApi.runBilling(period)
      .then(result => {
        setBillingResult(result)
        setSuccess(`Facturación completada: ${result.invoiced.length} factura(s) generada(s)`)
        loadInvoices()
      })
      .catch(e => setBillingErr(mapError(e).message))
      .finally(() => setRunning(false))
  }

  const invoiceColumns: GridColDef[] = [
    { field: 'numeroFactura', headerName: 'Nº Factura', flex: 2 },
    { field: 'cups', headerName: 'CUPS', flex: 2 },
    { field: 'periodoInicio', headerName: 'Periodo inicio', flex: 1 },
    { field: 'periodoFin', headerName: 'Periodo fin', flex: 1 },
    {
      field: 'base', headerName: 'Base (€)', flex: 1,
      valueFormatter: (value) => `${Number(value).toFixed(2)} €`,
    },
    {
      field: 'impuestos', headerName: 'IVA (€)', flex: 1,
      valueFormatter: (value) => `${Number(value).toFixed(2)} €`,
    },
    {
      field: 'total', headerName: 'Total (€)', flex: 1,
      valueFormatter: (value) => `${Number(value).toFixed(2)} €`,
    },
    { field: 'fechaEmision', headerName: 'Emisión', flex: 1 },
    {
      field: 'actions', headerName: 'Acciones', width: 140, sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <Stack direction="row" spacing={0.5}>
          <IconButton
            size="small"
            aria-label="ver detalle"
            onClick={() => setExpandedId(expandedId === params.row.id ? null : params.row.id)}
          >
            {expandedId === params.row.id ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
          </IconButton>
          <IconButton
            size="small"
            aria-label="descargar PDF"
            component="a"
            href={billingApi.getPdfUrl(params.row.id)}
            target="_blank"
            download
          >
            <DownloadIcon fontSize="small" />
          </IconButton>
        </Stack>
      ),
    },
  ]

  const expandedInvoice = invoices.find(i => i.id === expandedId)

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" sx={{ mb: 3 }}>Facturación</Typography>

      {/* Run billing section */}
      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>
          <PlayArrowIcon sx={{ verticalAlign: 'middle', mr: 1, color: 'secondary.main' }} />
          Ejecutar Facturación
        </Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="flex-start">
          <TextField
            label="Período (YYYY-MM)"
            value={period}
            onChange={e => { setPeriod(e.target.value); setPeriodError('') }}
            placeholder="2026-01"
            error={!!periodError}
            helperText={periodError || 'Ej: 2026-01'}
            size="small"
            sx={{ minWidth: 200 }}
          />
          <Button
            variant="contained"
            color="secondary"
            startIcon={running ? undefined : <ReceiptIcon />}
            onClick={handleRunBilling}
            disabled={running}
            sx={{ mt: periodError ? 0 : 0 }}
          >
            {running ? 'Procesando...' : 'Ejecutar facturación'}
          </Button>
        </Stack>

        {running && <LinearProgress sx={{ mt: 2 }} />}
        {billingErr && <Alert severity="error" sx={{ mt: 2 }}>{billingErr}</Alert>}

        {billingResult && (
          <Box sx={{ mt: 2 }}>
            <Alert severity="success" sx={{ mb: 1 }}>
              Período {billingResult.period}: {billingResult.invoiced.length} factura(s) generada(s), {billingResult.errors.length} error(es)
            </Alert>
            {billingResult.errors.length > 0 && (
              <Box sx={{ mt: 1 }}>
                <Typography variant="subtitle2" color="error">Errores de facturación:</Typography>
                {billingResult.errors.map((e, i) => (
                  <Alert key={i} severity="warning" sx={{ mt: 0.5 }}>
                    CUPS {e.cups}: {e.reason}
                  </Alert>
                ))}
              </Box>
            )}
          </Box>
        )}
      </Paper>

      {/* Invoice list */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" sx={{ mb: 2 }}>Facturas</Typography>

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
          <TextField
            label="Filtrar CUPS"
            value={filterCups}
            onChange={e => setFilterCups(e.target.value)}
            size="small"
          />
          <TextField
            label="Filtrar periodo (YYYY-MM)"
            value={filterPeriod}
            onChange={e => setFilterPeriod(e.target.value)}
            placeholder="2026-01"
            size="small"
          />
          <Button variant="outlined" onClick={loadInvoices}>Buscar</Button>
          <Button variant="text" onClick={() => { setFilterCups(''); setFilterPeriod('') }}>Limpiar</Button>
        </Stack>

        {loadingInvoices && <LinearProgress sx={{ mb: 1 }} />}
        {invoiceError && <Alert severity="error" sx={{ mb: 2 }}>{invoiceError}</Alert>}

        <DataGrid
          rows={invoices}
          columns={invoiceColumns}
          autoHeight
          pageSizeOptions={[10, 25, 50]}
          initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
          localeText={{ noRowsLabel: 'No hay facturas' }}
          sx={{ mb: 2 }}
        />

        {/* Invoice Detail Expand */}
        {expandedInvoice && (
          <Collapse in={!!expandedInvoice}>
            <Paper variant="outlined" sx={{ p: 2, mt: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                <Typography variant="h6">
                  Detalle: {expandedInvoice.numeroFactura}
                </Typography>
                <Stack direction="row" spacing={1}>
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<DownloadIcon />}
                    component="a"
                    href={billingApi.getPdfUrl(expandedInvoice.id)}
                    target="_blank"
                    download
                  >
                    Descargar PDF
                  </Button>
                  <IconButton size="small" aria-label="cerrar detalle" onClick={() => setExpandedId(null)}>
                    <ExpandLessIcon />
                  </IconButton>
                </Stack>
              </Box>

              <Stack direction="row" spacing={4} sx={{ mb: 2 }} flexWrap="wrap">
                <Box><Typography variant="caption" color="text.secondary">CUPS</Typography><Typography>{expandedInvoice.cups}</Typography></Box>
                <Box><Typography variant="caption" color="text.secondary">Periodo</Typography><Typography>{expandedInvoice.periodoInicio} → {expandedInvoice.periodoFin}</Typography></Box>
                <Box><Typography variant="caption" color="text.secondary">Emisión</Typography><Typography>{expandedInvoice.fechaEmision}</Typography></Box>
                <Box><Typography variant="caption" color="text.secondary">Base</Typography><Typography>{Number(expandedInvoice.base).toFixed(2)} €</Typography></Box>
                <Box><Typography variant="caption" color="text.secondary">IVA</Typography><Typography>{Number(expandedInvoice.impuestos).toFixed(2)} €</Typography></Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Total</Typography>
                  <Typography variant="h6" color="secondary.main">{Number(expandedInvoice.total).toFixed(2)} €</Typography>
                </Box>
              </Stack>

              <Divider sx={{ mb: 1 }} />
              <Typography variant="subtitle2" sx={{ mb: 1 }}>Líneas de factura</Typography>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Tipo</TableCell>
                    <TableCell>Descripción</TableCell>
                    <TableCell align="right">Cantidad</TableCell>
                    <TableCell align="right">Precio unit.</TableCell>
                    <TableCell align="right">Importe (€)</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {expandedInvoice.lines.map((line, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <Chip label={line.tipo} size="small" variant="outlined" />
                      </TableCell>
                      <TableCell>{line.descripcion}</TableCell>
                      <TableCell align="right">{Number(line.cantidad).toFixed(3)}</TableCell>
                      <TableCell align="right">{Number(line.precioUnitario).toFixed(6)}</TableCell>
                      <TableCell align="right"><strong>{Number(line.importe).toFixed(2)} €</strong></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          </Collapse>
        )}
      </Paper>

      <Snackbar open={!!success} autoHideDuration={5000} onClose={() => setSuccess(null)}>
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Container>
  )
}
