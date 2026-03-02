import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from '@mui/material'

interface Props {
  open: boolean
  onClose: () => void
  onConfirm: () => void
  label?: string
}

export function ConfirmDeleteDialog({ open, onClose, onConfirm, label = 'este elemento' }: Props) {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Confirmar eliminación</DialogTitle>
      <DialogContent>
        <DialogContentText>
          ¿Estás seguro de que quieres eliminar {label}? Esta acción no se puede deshacer.
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button onClick={onConfirm} color="error" variant="contained">
          Eliminar
        </Button>
      </DialogActions>
    </Dialog>
  )
}
