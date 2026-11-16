// Aba "Inspeções" de /areas-risco para o Gestor de Segurança (RF09, US09, issue #136): escolhe a área,
// consulta o histórico de inspeções com os itens verificados e registra uma nova inspeção. Não mostra
// situação da área, interdição nem cores além do resultado de cada item.

import { useCallback, useId, useState } from 'react'
import { Badge, Button, EmptyState, Select, Toast } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarData } from '../../shared/formatos'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import type { AreaRisco } from './api'
import { listarInspecoes, type Inspecao } from './apiInspecoes'
import { InspecaoFormDialog } from './InspecaoFormDialog'
import './inspecoes.css'

function resumo(inspecao: Inspecao): string {
  const itens = inspecao.itens.length === 1 ? '1 item verificado' : `${inspecao.itens.length} itens verificados`
  const falhas = inspecao.naoConformidades === 0 ? 'nenhuma não conformidade'
    : inspecao.naoConformidades === 1 ? '1 não conformidade' : `${inspecao.naoConformidades} não conformidades`
  return `${itens}, ${falhas}`
}

function CartaoInspecao({ inspecao }: { inspecao: Inspecao }) {
  const idTitulo = useId()
  return <li>
    <article className="sp-inspecao" aria-labelledby={idTitulo}>
      <h3 id={idTitulo} className="sp-type-heading-sm">
        Inspeção de <time dateTime={inspecao.dataInspecao}>{formatarData(inspecao.dataInspecao)}</time>
      </h3>
      <p className="sp-texto-secundario">Registrada por {inspecao.responsavelNome}; {resumo(inspecao)}.</p>
      <ul className="sp-inspecao-itens">
        {inspecao.itens.map(item => <li key={item.id}>
          <div className="sp-inspecao-item-topo">
            <strong>{item.descricao}</strong>
            <Badge tone={item.conformidade ? 'success' : 'danger'}>
              {item.conformidade ? 'Conformidade' : 'Não conformidade'}
            </Badge>
          </div>
          {item.observacoes && <p>{item.observacoes}</p>}
        </li>)}
      </ul>
    </article>
  </li>
}

export function InspecoesPainel({ areas, offline }: { areas: AreaRisco[] | null; offline: boolean }) {
  const [areaId, setAreaId] = useState<number | null>(null)
  const [registrando, setRegistrando] = useState(false)
  const [mensagem, setMensagem] = useState('')
  const [erroArea, setErroArea] = useState<string>()
  const idArea = useId()
  const carregar = useCallback(
    (signal: AbortSignal) => (areaId === null ? Promise.resolve([]) : listarInspecoes(areaId, signal)),
    [areaId]
  )
  const consulta = useConsulta(`inspecoes-${areaId ?? 'nenhuma'}`, carregar)
  const area = areas?.find(atual => atual.id === areaId) ?? null

  function abrirRegistro() {
    if (area === null) {
      setErroArea('Selecione a área de risco para registrar a inspeção.')
      document.getElementById(idArea)?.focus()
      return
    }
    setRegistrando(true)
  }

  return <div className="sp-painel">
    <div className="sp-painel-topo sp-inspecao-topo">
      <Select id={idArea} label="Área de risco inspecionada" value={areaId ?? ''} className="sp-inspecao-area"
        error={erroArea}
        onChange={e => {
          setAreaId(e.target.value ? Number(e.target.value) : null)
          setErroArea(undefined)
        }}
        helper="O histórico mostra as inspeções da mais recente para a mais antiga.">
        <option value="">Selecione a área</option>
        {(areas ?? []).map(atual => <option key={atual.id} value={atual.id}>{`${atual.codigo} - ${atual.nome}`}</option>)}
      </Select>
      <Button variant="primary" icon="plus" disabled={offline}
        title={offline ? 'Operação indisponível em modo offline' : undefined}
        onClick={abrirRegistro}>
        Registrar inspeção
      </Button>
    </div>

    {area === null
      ? <EmptyState title="Escolha uma área de risco"
          description="Selecione a área para consultar as inspeções registradas e registrar uma nova." />
      : <ResultadoConsulta consulta={consulta} carregandoLabel="Carregando inspeções..."
          erroTitulo="Não foi possível carregar as inspeções da área."
          vazio={{ title: 'Nenhuma inspeção registrada', description: 'As inspeções desta área aparecerão aqui.' }}>
          {inspecoes => <section aria-label={`Histórico de inspeções: ${area.codigo} - ${area.nome}`}>
            <p className="sp-texto-secundario" role="status">
              {inspecoes.length === 1 ? '1 inspeção registrada' : `${inspecoes.length} inspeções registradas`}
            </p>
            <ol className="sp-inspecoes-lista">
              {inspecoes.map(inspecao => <CartaoInspecao key={inspecao.id} inspecao={inspecao} />)}
            </ol>
          </section>}
        </ResultadoConsulta>}

    <InspecaoFormDialog area={registrando ? area : null} aoFechar={() => setRegistrando(false)}
      aoSalvar={inspecao => {
        setMensagem(`Inspeção de ${formatarData(inspecao.dataInspecao)} registrada no histórico da área.`)
        consulta.recarregar()
      }} />

    <Toast message={mensagem} onDismiss={() => setMensagem('')} />
  </div>
}
