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
import type { TaxConfig } from '../types'
import { taxesApi } from '../api/taxesApi'
import { ConfirmDeleteDialog } from '../../../shared/ui/ConfirmDeleteDialog'
import { mapError } from '../../../shared/api/httpClient'

const schema = z.object({
  taxCode: z.string().min(1, 'Código es requerido'),
  taxRate: z.string().refine(v => {
    const n = Number(v)
    return !isNaN(n) && n >= 0 && n <= 1
  }, 'Debe estar en [0, 1], e.g. 0.21 para 21%'),
  vigenciaDesde: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Formato YYYY-MM-DD'),
})

type FormData = z.infer<typeof schema>

export function TaxesPage() {
  const [rows, setRows] = useState<TaxConfig[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editRow, setEditRow] = useState<TaxConfig | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<TaxConfig | null>(null)
  const [saving, setSaving] = useState(false)

  const { control, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { taxCode: '', taxRate: '', vigenciaDesde: '' },
  })

  const load = () => {
    setLoading(true)
    setError(null)
    taxesApi.findAll()
      .then(setRows)
      .catch(e => setError(mapError(e).message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    let cancelled = false
    void taxesApi.findAll()
      .then(data => { if (!cancelled) setRows(data) })
      .catch(e => { if (!cancelled) setError(mapError(e).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  const openCreate = () => {
    setEditRow(null)
    reset({ taxCode: '', taxRate: '', vigenciaDesde: '' })
    setDialogOpen(true)
  }

  const openEdit = (row: TaxConfig) => {
    setEditRow(row)
    reset({ taxCode: row.taxCode, taxRate: String(row.taxRate), vigenciaDesde: row.vigenciaDesde })
    setDialogOpen(true)
  }

  const onSubmit = (data: FormData) => {
    setSaving(true)
    const payload = { taxCode: data.taxCode, taxRate: Number(data.taxRate), vigenciaDesde: data.vigenciaDesde }
    const promise = editRow
      ? taxesApi.update(editRow.taxCode, payload)
      : taxesApi.create(payload)

    promise
      .then(() => { setSuccess(editRow ? 'Impuesto actualizado' : 'Impuesto creado'); setDialogOpen(false); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setSaving(false))
  }

  const confirmDelete = () => {
    if (!deleteTarget) return
    taxesApi.remove(deleteTarget.taxCode)
      .then(() => { setSuccess('Impuesto eliminado'); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setDeleteTarget(null))
  }

  const columns: GridColDef[] = [
    { field: 'taxCode', headerName: 'Código', flex: 1 },
    {
      field: 'taxRate', headerName: 'Tasa (%)', flex: 1,
      valueFormatter: (value) => `${(Number(value) * 100).toFixed(2)}%`,
    },
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
        <Typography variant="h4">IVA / Impuestos</Typography>
        <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={openCreate}>
          Nuevo
        </Button>
      </Box>

      {loading && <LinearProgress sx={{ mb: 1 }} />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <DataGrid
        rows={rows}
        columns={columns}
        getRowId={r => r.taxCode}
        autoHeight
        pageSizeOptions={[10, 25]}
        initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        localeText={{ noRowsLabel: 'No hay impuestos configurados' }}
      />

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editRow ? 'Editar Impuesto' : 'Nuevo Impuesto'}</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Controller
              name="taxCode"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Código (e.g. IVA)" error={!!errors.taxCode} helperText={errors.taxCode?.message} disabled={!!editRow} />
              )}
            />
            <Controller
              name="taxRate"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Tasa (decimal, e.g. 0.21 = 21%)"
                  type="number"
                  inputProps={{ min: 0, max: 1, step: '0.01' }}
                  error={!!errors.taxRate}
                  helperText={errors.taxRate?.message}
                />
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
        label={deleteTarget?.taxCode}
      />

      <Snackbar open={!!success} autoHideDuration={4000} onClose={() => setSuccess(null)}>
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Container>
  )
}
