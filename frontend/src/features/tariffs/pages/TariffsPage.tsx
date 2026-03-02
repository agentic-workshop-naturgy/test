import { useEffect, useState } from 'react'
import {
  Box, Button, Typography, Alert, Snackbar, LinearProgress,
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, IconButton, Container,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useForm, Controller } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import type { GasTariff } from '../types'
import { tariffsApi } from '../api/tariffsApi'
import { ConfirmDeleteDialog } from '../../../shared/ui/ConfirmDeleteDialog'
import { mapError } from '../../../shared/api/httpClient'

const schema = z.object({
  tarifa: z.string().min(1, 'Tarifa es requerida'),
  fijoMesEur: z.string().refine(v => !isNaN(Number(v)) && Number(v) >= 0, 'Debe ser >= 0'),
  variableEurKwh: z.string().refine(v => !isNaN(Number(v)) && Number(v) >= 0, 'Debe ser >= 0'),
  vigenciaDesde: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Formato YYYY-MM-DD'),
})

type FormData = z.infer<typeof schema>

export function TariffsPage() {
  const [rows, setRows] = useState<GasTariff[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editRow, setEditRow] = useState<GasTariff | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<GasTariff | null>(null)
  const [saving, setSaving] = useState(false)

  const { control, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { tarifa: '', fijoMesEur: '', variableEurKwh: '', vigenciaDesde: '' },
  })

  const load = () => {
    setLoading(true)
    setError(null)
    tariffsApi.findAll()
      .then(setRows)
      .catch(e => setError(mapError(e).message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    let cancelled = false
    void tariffsApi.findAll()
      .then(data => { if (!cancelled) setRows(data) })
      .catch(e => { if (!cancelled) setError(mapError(e).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  const openCreate = () => {
    setEditRow(null)
    reset({ tarifa: '', fijoMesEur: '', variableEurKwh: '', vigenciaDesde: '' })
    setDialogOpen(true)
  }

  const openEdit = (row: GasTariff) => {
    setEditRow(row)
    reset({
      tarifa: row.tarifa,
      fijoMesEur: String(row.fijoMesEur),
      variableEurKwh: String(row.variableEurKwh),
      vigenciaDesde: row.vigenciaDesde,
    })
    setDialogOpen(true)
  }

  const onSubmit = (data: FormData) => {
    setSaving(true)
    const payload = {
      tarifa: data.tarifa,
      fijoMesEur: Number(data.fijoMesEur),
      variableEurKwh: Number(data.variableEurKwh),
      vigenciaDesde: data.vigenciaDesde,
    }
    const promise = editRow
      ? tariffsApi.update(editRow.tarifa, payload)
      : tariffsApi.create(payload)

    promise
      .then(() => { setSuccess(editRow ? 'Tarifa actualizada' : 'Tarifa creada'); setDialogOpen(false); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setSaving(false))
  }

  const confirmDelete = () => {
    if (!deleteTarget) return
    tariffsApi.remove(deleteTarget.tarifa)
      .then(() => { setSuccess('Tarifa eliminada'); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setDeleteTarget(null))
  }

  const columns: GridColDef[] = [
    { field: 'tarifa', headerName: 'Tarifa', flex: 1 },
    { field: 'fijoMesEur', headerName: 'Fijo/mes (€)', flex: 1 },
    { field: 'variableEurKwh', headerName: 'Variable €/kWh', flex: 1 },
    { field: 'vigenciaDesde', headerName: 'Vigencia desde', flex: 1 },
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
        <Typography variant="h4">Tarifas</Typography>
        <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={openCreate}>
          Nueva
        </Button>
      </Box>

      {loading && <LinearProgress sx={{ mb: 1 }} />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <DataGrid
        rows={rows}
        columns={columns}
        getRowId={r => r.tarifa}
        autoHeight
        pageSizeOptions={[10, 25, 50]}
        initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        localeText={{ noRowsLabel: 'No hay tarifas' }}
      />

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editRow ? 'Editar Tarifa' : 'Nueva Tarifa'}</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Controller
              name="tarifa"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Código tarifa" error={!!errors.tarifa} helperText={errors.tarifa?.message} disabled={!!editRow} />
              )}
            />
            <Controller
              name="fijoMesEur"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Fijo/mes (€)" type="number" inputProps={{ min: 0, step: '0.0001' }} error={!!errors.fijoMesEur} helperText={errors.fijoMesEur?.message} />
              )}
            />
            <Controller
              name="variableEurKwh"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Variable €/kWh" type="number" inputProps={{ min: 0, step: '0.000001' }} error={!!errors.variableEurKwh} helperText={errors.variableEurKwh?.message} />
              )}
            />
            <Controller
              name="vigenciaDesde"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Vigencia desde" type="date" error={!!errors.vigenciaDesde} helperText={errors.vigenciaDesde?.message} InputLabelProps={{ shrink: true }} />
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
        label={deleteTarget?.tarifa}
      />

      <Snackbar open={!!success} autoHideDuration={4000} onClose={() => setSuccess(null)}>
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Container>
  )
}
