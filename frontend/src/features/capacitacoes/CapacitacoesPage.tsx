// Tela /capacitacoes (RF10, UC02, US10; issue #120): Gestor de Segurança e Supervisor consultam os
// colaboradores com a situação das certificações, os alertas de vencimento e, ao selecionar um
// colaborador, as certificações, os treinamentos, as pendências e a aptidão para uma tarefa. Ao abrir,
// um aviso resume os alertas de vencimento (UC02, regra 1). Só leitura: o cadastro aguarda o fluxo
// de #116 e #126. Sem cópia offline, que a #105 não aprova para capacitações. Aba, filtro e
// colaborador aberto ficam na URL para sobreviver à recarga.

import { useCallback, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Tabs } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import '../estoque/estoque.css'
import '../emprestimos/emprestimos.css'
import './capacitacoes.css'
import { situacoesCertificacao, type SituacaoCertificacao } from './api'
import { AlertasPainel } from './AlertasPainel'
import { CapacitacoesColaboradorDialog } from './CapacitacoesColaboradorDialog'
import { ColaboradoresPainel } from './ColaboradoresPainel'
import { ResumoAlertas } from './ResumoAlertas'

type Aba = 'colaboradores' | 'alertas'

function lerUrl(params: URLSearchParams) {
  const situacao = params.get('situacao')
  const id = Number(params.get('colaborador'))
  return {
    aba: (params.get('aba') === 'alertas' ? 'alertas' : 'colaboradores') as Aba,
    situacao: situacoesCertificacao.includes(situacao as SituacaoCertificacao) ? situacao as SituacaoCertificacao : null,
    colaboradorId: Number.isInteger(id) && id > 0 ? id : null,
  }
}

export function CapacitacoesPage() {
  const [params, setParams] = useSearchParams()
  const { aba, situacao, colaboradorId } = lerUrl(params)
  const focarAbaAberta = useRef(false)

  // O aviso de alertas sai da tela com a troca de aba; o foco vai para a aba aberta.
  useEffect(() => {
    if (!focarAbaAberta.current) return
    focarAbaAberta.current = false
    document.querySelector<HTMLElement>('[role="tab"][aria-selected="true"]')?.focus()
  }, [aba])

  const atualizar = useCallback((mudar: (proximos: URLSearchParams) => void) => {
    setParams(atuais => {
      const proximos = new URLSearchParams(atuais)
      mudar(proximos)
      return proximos
    })
  }, [setParams])

  const abrirColaborador = useCallback((id: number | null) => atualizar(proximos => {
    if (id === null) proximos.delete('colaborador')
    else proximos.set('colaborador', String(id))
  }), [atualizar])

  const filtrar = useCallback((nova: SituacaoCertificacao | null) => atualizar(proximos => {
    if (nova) proximos.set('situacao', nova)
    else proximos.delete('situacao')
  }), [atualizar])

  const mudarAba = useCallback((valor: string) => atualizar(proximos => {
    if (valor === 'alertas') proximos.set('aba', 'alertas')
    else proximos.delete('aba')
  }), [atualizar])

  return <FeaturePage title="Capacitações"
    description="Consulte as certificações e os treinamentos dos colaboradores, os vencimentos e os impedimentos para tarefas (UC02).">
    {aba === 'colaboradores' && <ResumoAlertas aoVerAlertas={() => {
      focarAbaAberta.current = true
      mudarAba('alertas')
    }} />}
    <Tabs label="Consulta de capacitações" value={aba} onValueChange={mudarAba}
      items={[
        {
          id: 'colaboradores', label: 'Colaboradores',
          content: aba === 'colaboradores'
            ? <ColaboradoresPainel situacao={situacao} aoFiltrar={filtrar} aoConsultar={abrirColaborador} />
            : null,
        },
        {
          id: 'alertas', label: 'Alertas de vencimento',
          content: aba === 'alertas' ? <AlertasPainel aoConsultar={abrirColaborador} /> : null,
        },
      ]} />
    <CapacitacoesColaboradorDialog colaboradorId={colaboradorId} aoFechar={() => abrirColaborador(null)} />
  </FeaturePage>
}
