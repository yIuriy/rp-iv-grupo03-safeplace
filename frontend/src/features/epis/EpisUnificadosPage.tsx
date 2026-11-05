// Entrada única para catálogo, estoque e manutenção de EPIs (RF03, RF04, UC01 e UC06).
// O catálogo é consultado uma vez e entregue aos dois painéis; histórico e gravações mantêm
// seus contratos próprios da API.

import { useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useConsulta } from '../../shared/api/useConsulta'
import { Tabs } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { EstoquePainel } from '../estoque/EstoquePage'
import { listarEpis } from './api'
import { EpiManutencaoPainel } from './EpiPage'

type AbaEpi = 'catalogo' | 'manutencao'

function abaDaUrl(valor: string | null): AbaEpi {
  return valor === 'manutencao' ? 'manutencao' : 'catalogo'
}

export function EpisUnificadosPage() {
  const [parametros, definirParametros] = useSearchParams()
  const aba = abaDaUrl(parametros.get('aba'))
  const carregarEpis = useCallback((signal: AbortSignal) => listarEpis(signal), [])
  const consulta = useConsulta('epis-unificados', carregarEpis)

  function mudarAba(proxima: string) {
    const novos = new URLSearchParams(parametros)
    if (proxima === 'manutencao') novos.set('aba', 'manutencao')
    else novos.delete('aba')
    definirParametros(novos, { replace: true })
  }

  return <FeaturePage title="EPIs" description="Consulte catálogo, saldo, movimentações e manutenção dos equipamentos de proteção individual.">
    <Tabs label="Operações de EPIs" value={aba} onValueChange={mudarAba} items={[
      { id: 'catalogo', label: 'Catálogo e estoque', content: <EstoquePainel consulta={consulta} /> },
      { id: 'manutencao', label: 'Manutenção', content: <EpiManutencaoPainel epis={consulta} /> },
    ]} />
  </FeaturePage>
}
