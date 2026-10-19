// Tela /capacitacoes (RF10, UC02, US10; issues #120 e #192): Gestor de Segurança e Supervisor
// consultam os colaboradores com a situação das certificações, os alertas de vencimento e, ao
// selecionar um colaborador, as certificações, os treinamentos, as pendências e a aptidão para uma
// tarefa, onde também tentam a alocação. Ao abrir, um aviso resume os alertas de vencimento (UC02,
// regra 1). A aba Cadastro é exclusiva do Gestor de Segurança. Sem cópia offline, que a #105 não
// aprova para capacitações. Aba, filtro e colaborador aberto ficam na URL para sobreviver à recarga.

import { useCallback, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Tabs } from '../../shared/components'
import { FeaturePage } from '../../shared/layout/FeaturePage'
import { useSessaoAtiva } from '../autenticacao/contexto'
import '../estoque/estoque.css'
import '../emprestimos/emprestimos.css'
import './capacitacoes.css'
import { situacoesCertificacao, type SituacaoCertificacao } from './api'
import { AlertasPainel } from './AlertasPainel'
import { CadastroPainel } from './CadastroPainel'
import { CapacitacoesColaboradorDialog } from './CapacitacoesColaboradorDialog'
import { ColaboradoresPainel } from './ColaboradoresPainel'
import { ResumoAlertas } from './ResumoAlertas'

type Aba = 'colaboradores' | 'alertas' | 'cadastro'

function lerAba(params: URLSearchParams, podeCadastrar: boolean): Aba {
  const aba = params.get('aba')
  if (aba === 'alertas') return 'alertas'
  return aba === 'cadastro' && podeCadastrar ? 'cadastro' : 'colaboradores'
}

function lerUrl(params: URLSearchParams, podeCadastrar: boolean) {
  const situacao = params.get('situacao')
  const id = Number(params.get('colaborador'))
  return {
    aba: lerAba(params, podeCadastrar),
    situacao: situacoesCertificacao.includes(situacao as SituacaoCertificacao) ? situacao as SituacaoCertificacao : null,
    colaboradorId: Number.isInteger(id) && id > 0 ? id : null,
  }
}

export function CapacitacoesPage() {
  const [params, setParams] = useSearchParams()
  const podeCadastrar = useSessaoAtiva().perfil === 'GESTOR_SEGURANCA'
  const { aba, situacao, colaboradorId } = lerUrl(params, podeCadastrar)
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
    if (valor === 'alertas' || valor === 'cadastro') proximos.set('aba', valor)
    else proximos.delete('aba')
  }), [atualizar])

  return <FeaturePage title="Capacitações"
    description="Consulte as certificações e os treinamentos dos colaboradores, os vencimentos e os impedimentos para tarefas.">
    {aba === 'colaboradores' && <ResumoAlertas aoVerAlertas={() => {
      focarAbaAberta.current = true
      mudarAba('alertas')
    }} />}
    <Tabs label="Capacitações" value={aba} onValueChange={mudarAba}
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
        ...(podeCadastrar ? [{
          id: 'cadastro', label: 'Cadastro',
          content: aba === 'cadastro' ? <CadastroPainel /> : null,
        }] : []),
      ]} />
    <CapacitacoesColaboradorDialog colaboradorId={colaboradorId} aoFechar={() => abrirColaborador(null)} />
  </FeaturePage>
}
