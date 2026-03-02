import { useEffect, useState } from 'react'
import {
  Box, Button, Typography, Alert, Snackbar, LinearProgress,
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Select, MenuItem, FormControl, InputLabel, FormHelperText,
  IconButton, Container, Stack,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useForm, Controller } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import type { GasReading } from '../types'
import { readingsApi } from '../api/readingsApi'
import { ConfirmDeleteDialog } from '../../../shared/ui/ConfirmDeleteDialog'
import { mapError } from '../../../shared/api/httpClient'

const schema = z.object({
  cups: z.string().min(1, 'CUPS es requerido'),
  fecha: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Formato YYYY-MM-DD'),
  lecturaM3: z.string().refine(v => !isNaN(Number(v)) && Number(v) >= 0, 'Debe ser >= 0'),
  tipo: z.enum(['REAL', 'ESTIMADA']),
})

type FormData = z.infer<typeof schema>

export function ReadingsPage() {
  const [rows, setRows] = useState<GasReading[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editRow, setEditRow] = useState<GasReading | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<GasReading | null>(null)
  const [saving, setSaving] = useState(false)
  const [filterCups, setFilterCups] = useState('')
  const [filterFechaDesde, setFilterFechaDesde] = useState('')
  const [filterFechaHasta, setFilterFechaHasta] = useState('')

  const { control, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { cups: '', fecha: '', lecturaM3: '', tipo: 'REAL' },
  })

  const load = () => {
    setLoading(true)
    setError(null)
    readingsApi.findAll()
      .then(setRows)
      .catch(e => setError(mapError(e).message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    let cancelled = false
    void readingsApi.findAll()
      .then(data => { if (!cancelled) setRows(data) })
      .catch(e => { if (!cancelled) setError(mapError(e).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  const filteredRows = rows.filter(r => {
    if (filterCups && !r.cups.toLowerCase().includes(filterCups.toLowerCase())) return false
    if (filterFechaDesde && r.fecha < filterFechaDesde) return false
    if (filterFechaHasta && r.fecha > filterFechaHasta) return false
    return true
  })

  const openCreate = () => {
    setEditRow(null)
    reset({ cups: '', fecha: '', lecturaM3: '', tipo: 'REAL' })
    setDialogOpen(true)
  }

  const openEdit = (row: GasReading) => {
    setEditRow(row)
    reset({ cups: row.cups, fecha: row.fecha, lecturaM3: String(row.lecturaM3), tipo: row.tipo })
    setDialogOpen(true)
  }

  const onSubmit = (data: FormData) => {
    setSaving(true)
    const payload = { ...data, lecturaM3: Number(data.lecturaM3) }
    const promise = editRow?.id
      ? readingsApi.update(editRow.id, { ...payload, id: editRow.id })
      : readingsApi.create(payload)

    promise
      .then(() => { setSuccess(editRow ? 'Lectura actualizada' : 'Lectura creada'); setDialogOpen(false); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setSaving(false))
  }

  const confirmDelete = () => {
    if (!deleteTarget?.id) return
    readingsApi.remove(deleteTarget.id)
      .then(() => { setSuccess('Lectura eliminada'); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setDeleteTarget(null))
  }

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'cups', headerName: 'CUPS', flex: 2 },
    { field: 'fecha', headerName: 'Fecha', flex: 1 },
    { field: 'lecturaM3', headerName: 'Lectura m³', flex: 1 },
    { field: 'tipo', headerName: 'Tipo', flex: 1 },
    {
      field: 'actions', headerName: 'Acciones', width: 120, sortable: false,
      renderCell: ({ row }) => (
        <>
          <IconButton size="small" aria-label="editar" onClick={() => openEdit(row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" aria-label="eliminar" onClick={() => setDeleteTarget(row)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </>
      ),
    },
  ]

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h4">Lecturas de Gas</Typography>
        <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={openCreate}>
          Nueva
        </Button>
      </Box>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField
          label="Filtrar por CUPS"
          value={filterCups}
          onChange={e => setFilterCups(e.target.value)}
          size="small"
        />
        <TextField
          label="Fecha desde"
          type="date"
          value={filterFechaDesde}
          onChange={e => setFilterFechaDesde(e.target.value)}
          size="small"
          InputLabelProps={{ shrink: true }}
        />
        <TextField
          label="Fecha hasta"
          type="date"
          value={filterFechaHasta}
          onChange={e => setFilterFechaHasta(e.target.value)}
          size="small"
          InputLabelProps={{ shrink: true }}
        />
        <Button variant="outlined" onClick={() => { setFilterCups(''); setFilterFechaDesde(''); setFilterFechaHasta('') }}>
          Limpiar
        </Button>
      </Stack>

      {loading && <LinearProgress sx={{ mb: 1 }} />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <DataGrid
        rows={filteredRows}
        columns={columns}
        autoHeight
        pageSizeOptions={[10, 25, 50]}
        initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        localeText={{ noRowsLabel: 'No hay lecturas' }}
      />

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editRow ? 'Editar Lectura' : 'Nueva Lectura'}</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Controller
              name="cups"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="CUPS" error={!!errors.cups} helperText={errors.cups?.message} />
              )}
            />
            <Controller
              name="fecha"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Fecha (YYYY-MM-DD)"
                  type="date"
                  error={!!errors.fecha}
                  helperText={errors.fecha?.message}
                  InputLabelProps={{ shrink: true }}
                />
              )}
            />
            <Controller
              name="lecturaM3"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Lectura m³"
                  type="number"
                  inputProps={{ min: 0, step: '0.001' }}
                  error={!!errors.lecturaM3}
                  helperText={errors.lecturaM3?.message}
                />
              )}
            />
            <Controller
              name="tipo"
              control={control}
              render={({ field }) => (
                <FormControl error={!!errors.tipo}>
                  <InputLabel>Tipo</InputLabel>
                  <Select {...field} label="Tipo">
                    <MenuItem value="REAL">REAL</MenuItem>
                    <MenuItem value="ESTIMADA">ESTIMADA</MenuItem>
                  </Select>
                  {errors.tipo && <FormHelperText>{errors.tipo.message}</FormHelperText>}
                </FormControl>
              )}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
          <Button variant="contained" onClick={handleSubmit(onSubmit)} disabled={saving}>
            {saving ? 'Guardando...' : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDeleteDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        label={`lectura ID ${deleteTarget?.id}`}
      />

      <Snackbar open={!!success} autoHideDuration={4000} onClose={() => setSuccess(null)}>
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Container>
  )
}
