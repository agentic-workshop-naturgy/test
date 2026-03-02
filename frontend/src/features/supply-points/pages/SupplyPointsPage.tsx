import { useEffect, useState } from 'react'
import {
  Box, Button, Typography, Alert, Snackbar, LinearProgress,
  Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, Select, MenuItem, FormControl, InputLabel, FormHelperText,
  IconButton, Container,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import { DataGrid, type GridColDef } from '@mui/x-data-grid'
import { useForm, Controller } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import type { SupplyPoint } from '../types'
import { supplyPointsApi } from '../api/supplyPointsApi'
import { ConfirmDeleteDialog } from '../../../shared/ui/ConfirmDeleteDialog'
import { mapError } from '../../../shared/api/httpClient'

const schema = z.object({
  cups: z.string().min(1, 'CUPS es requerido'),
  zona: z.string().min(1, 'Zona es requerida'),
  tarifa: z.string().min(1, 'Tarifa es requerida'),
  estado: z.enum(['ACTIVO', 'INACTIVO']),
})

type FormData = z.infer<typeof schema>

export function SupplyPointsPage() {
  const [rows, setRows] = useState<SupplyPoint[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editRow, setEditRow] = useState<SupplyPoint | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<SupplyPoint | null>(null)
  const [saving, setSaving] = useState(false)

  const { control, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { cups: '', zona: '', tarifa: '', estado: 'ACTIVO' },
  })

  const load = () => {
    setLoading(true)
    setError(null)
    supplyPointsApi.findAll()
      .then(setRows)
      .catch(e => setError(mapError(e).message))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    let cancelled = false
    void supplyPointsApi.findAll()
      .then(data => { if (!cancelled) setRows(data) })
      .catch(e => { if (!cancelled) setError(mapError(e).message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  const openCreate = () => {
    setEditRow(null)
    reset({ cups: '', zona: '', tarifa: '', estado: 'ACTIVO' })
    setDialogOpen(true)
  }

  const openEdit = (row: SupplyPoint) => {
    setEditRow(row)
    reset({ cups: row.cups, zona: row.zona, tarifa: row.tarifa, estado: row.estado })
    setDialogOpen(true)
  }

  const onSubmit = (data: FormData) => {
    setSaving(true)
    const promise = editRow
      ? supplyPointsApi.update(editRow.cups, data)
      : supplyPointsApi.create(data)

    promise
      .then(() => {
        setSuccess(editRow ? 'Punto actualizado' : 'Punto creado')
        setDialogOpen(false)
        load()
      })
      .catch(e => setError(mapError(e).message))
      .finally(() => setSaving(false))
  }

  const confirmDelete = () => {
    if (!deleteTarget) return
    supplyPointsApi.remove(deleteTarget.cups)
      .then(() => { setSuccess('Punto eliminado'); load() })
      .catch(e => setError(mapError(e).message))
      .finally(() => setDeleteTarget(null))
  }

  const columns: GridColDef[] = [
    { field: 'cups', headerName: 'CUPS', flex: 2 },
    { field: 'zona', headerName: 'Zona', flex: 1 },
    { field: 'tarifa', headerName: 'Tarifa', flex: 1 },
    { field: 'estado', headerName: 'Estado', flex: 1 },
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
        <Typography variant="h4">Puntos de Suministro</Typography>
        <Button variant="contained" color="secondary" startIcon={<AddIcon />} onClick={openCreate}>
          Nuevo
        </Button>
      </Box>

      {loading && <LinearProgress sx={{ mb: 1 }} />}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <DataGrid
        rows={rows}
        columns={columns}
        getRowId={r => r.cups}
        autoHeight
        pageSizeOptions={[10, 25, 50]}
        initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
        localeText={{ noRowsLabel: 'No hay puntos de suministro' }}
      />

      {/* Create / Edit Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{editRow ? 'Editar Punto de Suministro' : 'Nuevo Punto de Suministro'}</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Controller
              name="cups"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="CUPS"
                  error={!!errors.cups}
                  helperText={errors.cups?.message}
                  disabled={!!editRow}
                />
              )}
            />
            <Controller
              name="zona"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Zona"
                  error={!!errors.zona}
                  helperText={errors.zona?.message}
                />
              )}
            />
            <Controller
              name="tarifa"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Tarifa"
                  error={!!errors.tarifa}
                  helperText={errors.tarifa?.message}
                />
              )}
            />
            <Controller
              name="estado"
              control={control}
              render={({ field }) => (
                <FormControl error={!!errors.estado}>
                  <InputLabel>Estado</InputLabel>
                  <Select {...field} label="Estado">
                    <MenuItem value="ACTIVO">ACTIVO</MenuItem>
                    <MenuItem value="INACTIVO">INACTIVO</MenuItem>
                  </Select>
                  {errors.estado && <FormHelperText>{errors.estado.message}</FormHelperText>}
                </FormControl>
              )}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancelar</Button>
          <Button
            variant="contained"
            onClick={handleSubmit(onSubmit)}
            disabled={saving}
          >
            {saving ? 'Guardando...' : 'Guardar'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmDeleteDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={confirmDelete}
        label={deleteTarget?.cups}
      />

      <Snackbar open={!!success} autoHideDuration={4000} onClose={() => setSuccess(null)}>
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Container>
  )
}
