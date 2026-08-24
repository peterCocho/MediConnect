const statusLabels: Record<string, string> = {
  SCHEDULED: 'Programada',
  CANCELED: 'Cancelada',
  PENDING_CONFIRMATION: 'Pendiente de Confirmación',
  COMPLETED: 'Completada',
  CONFIRMED: 'Confirmada',
  FINALIZED: 'Finalizada',
};

export const translateStatus = (status?: string | null) => {
  if (!status) return 'Pendiente';
  return statusLabels[status] ?? status;
};
