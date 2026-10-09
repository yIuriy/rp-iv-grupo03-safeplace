// Aviso exibido ao abrir a tela (UC02, regra 1 e cenário alternativo I): o sistema alerta, sem que a
// pessoa precise procurar, quantas certificações estão vencidas, quantas vencem dentro da antecedência
// e quantos colaboradores não têm certificação, além dos treinamentos com validade vencidos ou
// próximos do vencimento. Uma falha aqui não impede a listagem.

import { Alert, Button } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { consultarAlertas } from './api'
import { plural } from './formatos'

export function ResumoAlertas({ aoVerAlertas }: { aoVerAlertas: () => void }) {
  const { dados } = useConsulta('capacitacoes-resumo-alertas', consultarAlertas)
  if (!dados) return null
  const { vencidas, proximasDoVencimento, pendencias, diasAntecedenciaAlerta, treinamentosVencidos, treinamentosProximosDoVencimento } = dados
  if (vencidas.length + proximasDoVencimento.length + pendencias.length
    + treinamentosVencidos.length + treinamentosProximosDoVencimento.length === 0) return null

  const itens = [
    vencidas.length > 0 && `${plural(vencidas.length, 'certificação vencida', 'certificações vencidas')}`,
    proximasDoVencimento.length > 0 && `${plural(proximasDoVencimento.length, 'certificação vence', 'certificações vencem')} em até ${diasAntecedenciaAlerta} dias`,
    pendencias.length > 0 && `${plural(pendencias.length, 'colaborador sem certificações', 'colaboradores sem certificações')}`,
    treinamentosVencidos.length > 0 && `${plural(treinamentosVencidos.length, 'treinamento vencido', 'treinamentos vencidos')}`,
    treinamentosProximosDoVencimento.length > 0 && `${plural(treinamentosProximosDoVencimento.length, 'treinamento vence', 'treinamentos vencem')} em até ${diasAntecedenciaAlerta} dias`,
  ].filter((item): item is string => Boolean(item))

  return <Alert tone={vencidas.length + treinamentosVencidos.length > 0 ? 'danger' : 'warning'} live="polite" title="Alertas de capacitação">
    <ul className="sp-pendencias">{itens.map(item => <li key={item}>{item}</li>)}</ul>
    <Button variant="secondary" onClick={aoVerAlertas}>Ver alertas</Button>
  </Alert>
}
