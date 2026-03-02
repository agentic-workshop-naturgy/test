import { useEffect, useState } from 'react'
import {
  Box, Button, Typography, Alert, Snackbar, LinearProgress,
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, IconButton, Container, Stack,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useForm, Controller } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import type { GasConversionFactor } from '../types'
import { factorsApi } from '../api/factorsApi'
import { ConfirmDeleteDialog } from '../../../shared/ui/ConfirmDeleteDialog'
import { mapError } from '../../../shared/api/httpClient'

const schema = z.object({
  zona: z.string().min(1, 'Zona es requerida'),
  mes: z.string().regex(/^\d{4}-\d{2}$/, 'Formato YYYY-MM'),
  coefConv: z.string().refine(v => !isNaN(Number(v)) && Number(v) > 0, 'Debe ser > 0'),
  pcsKwhM3: z.string().refine(v => !isNaN(Number(v)) && Number(v) > 0, 'Debe ser > 0'),
})

type FormData = z.infer<typeof schema>

export function FactorsPage() {
  const [rows, setRows] = useState<GasConversionFactor[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editRow, setEditRow] = useState<GasConversionFactor | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<GasConversionFactor | null>(null)
  const [saving, setSaving] = useState(false)
  const [filterZona, setFilterZona] = useState('')
  const [filterMes, setFilterMes] = useState('')

  const { control, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { zona: '', mes: '', coefConv: '', pcsKwhM3: '' },
  })

  const load = () => {
    setLoading(true)
    setError(null)
    factorsApi.findAll()
      .then(setRows)
      .catch(e => setError(mapError(e).message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    let cancelled = false
    void factorsApi.findAll()
      .then(data => { if (!cancelled) setRows(data) })
      .catch(e => { if (!cancelled) setError(mapError(e).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  const filteredRows = rows.filter(r => {
    if (filterZona && !r.zona.toLowerCase().includes(filterZona.toLowerCase())) return false
    if (filterMes && r.mes !== filterMes) return false
    return true
  })

  const openCreate = () => {
    setEditRow(null)
    reset({ zona: '', mes: '', coefConv: '', pcsKwhM3: '' })
    setDialogOpen(true)
  }

  const openEdit = (row: GasConversionFactor) => {
    setEditRow(row)
    reset({ zona: row.zona, mes: row.mes, coefConv: String(row.coefConv), pcsKwhM3: String(row.pcsKwhM3) })
    setDialogOpen(true)
  }

  const onSubmit = (data: FormData) => {
    setSaving(true)
    const payload = { ...data, coefConv: Number(data.coefConv), pcsKwhM3: Number(data.pcsKwhM3) }
    const promise = editRow?.id
      ? factorsApi.update(editRow.id, { ...payload, id: editRow.id })
      : factorsApi.create(payload)

    promise
      .then(() => { setSuccess(editRow ? 'Factor actualizado' : 'Factor creado'); setDialogOpen(false); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setSaving(false))
  }

  const confirmDelete = () => {
    if (!deleteTarget?.id) return
    factorsApi.remove(deleteTarget.id)
      .then(() => { setSuccess('Factor eliminado'); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setDeleteTarget(null))
  }

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 80 },
    { field: 'zona', headerName: 'Zona', flex: 1 },
    { field: 'mes', headerName: 'Mes (YYYY-MM)', flex: 1 },
    { field: 'coefConv', headerName: 'Coef. Conv.', flex: 1 },
    { field: 'pcsKwhM3', headerName: 'PCS kWh/m³', flex: 1 },
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
        <Typography variant="h4">Factores de Conversión</Typography>
        <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={openCreate}>
          Nuevo
        </Button>
      </Box>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField label="Filtrar zona" value={filterZona} onChange={e => setFilterZona(e.target.value)} size="small" />
        <TextField
          label="Filtrar mes (YYYY-MM)"
          value={filterMes}
          onChange={e => setFilterMes(e.target.value)}
          size="small"
          placeholder="2026-01"
        />
        <Button variant="outlined" onClick={() => { setFilterZona(''); setFilterMes('') }}>Limpiar</Button>
      </Stack>

      {loading && <LinearProgress sx={{ mb: 1 }} />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <DataGrid
        rows={filteredRows}
        columns={columns}
        autoHeight
        pageSizeOptions={[10, 25, 50]}
        initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        localeText={{ noRowsLabel: 'No hay factores de conversión' }}
      />

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editRow ? 'Editar Factor' : 'Nuevo Factor de Conversión'}</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Controller
              name="zona"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Zona" error={!!errors.zona} helperText={errors.zona?.message} />
              )}
            />
            <Controller
              name="mes"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Mes (YYYY-MM)" placeholder="2026-01" error={!!errors.mes} helperText={errors.mes?.message} />
              )}
            />
            <Controller
              name="coefConv"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="Coef. Conversión" type="number" inputProps={{ min: 0, step: '0.0001' }} error={!!errors.coefConv} helperText={errors.coefConv?.message} />
              )}
            />
            <Controller
              name="pcsKwhM3"
              control={control}
              render={({ field }) => (
                <TextField {...field} label="PCS kWh/m³" type="number" inputProps={{ min: 0, step: '0.0001' }} error={!!errors.pcsKwhM3} helperText={errors.pcsKwhM3?.message} />
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
        label={`factor ID ${deleteTarget?.id}`}
      />

      <Snackbar open={!!success} autoHideDuration={4000} onClose={() => setSuccess(null)}>
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Container>
  )
}
