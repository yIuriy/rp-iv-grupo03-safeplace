import { Alert, Badge } from '../components'

function formatarDataHora(data: Date): string {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(data)
}

export function OfflineBanner({
  isOffline,
  sincronizadoEm,
}: {
  isOffline: boolean
  sincronizadoEm?: Date | null
}) {
  if (!isOffline) return null

  return (
    <div className="sp-offline-banner" style={{ marginBottom: '1.25rem' }}>
      <Alert tone="warning" title="Modo Offline — Exibindo dados locais">
        <span>
          Você está navegando sem conexão com a rede.{' '}
          {sincronizadoEm ? (
            <>Última sincronização realizada em: <strong>{formatarDataHora(sincronizadoEm)}</strong>.</>
          ) : (
            'Não foi possível atualizar os dados a partir do servidor.'
          )}
        </span>
      </Alert>
    </div>
  )
}

export function OfflineBadge({
  isOffline,
  sincronizadoEm,
}: {
  isOffline: boolean
  sincronizadoEm?: Date | null
}) {
  if (!isOffline) return null

  return (
    <Badge tone="warning">
      Offline {sincronizadoEm ? `(${formatarDataHora(sincronizadoEm)})` : ''}
    </Badge>
  )
}
