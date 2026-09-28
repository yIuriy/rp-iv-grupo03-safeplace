// Painel de projeções de substituição (RF21; UC10; US21; DS10 e DA10), exclusivo do Gestor: lista de
// reposição com os alertas preventivos, projeção mais recente de cada EPI com a data estimada e a
// justificativa do servidor, e solicitação da análise de projeção de desgaste, com as pendências e a
// nova tentativa da exceção I. Uma falha não gera alertas nem datas na interface.

import { useId, useState } from 'react'
import { Alert, Button, Table, TableRow } from '../../shared/components'
import { useConsulta } from '../../shared/api/useConsulta'
import { formatarData } from '../estoque/formatos'
import { ResultadoConsulta } from '../usuarios/ResultadoConsulta'
import {
  descreverFalhaDeAnalise, listarAlertas, listarProjecoes, maisRecentesPorEpi, solicitarAnalise,
  type AlertaReposicao, type Projecao, type ResultadoAnalise,
} from './apiProjecoes'
import { rotuloDoAlerta, textoDosDiasRestantes } from './formatos'

type Analise = { resultado: ResultadoAnalise } | { falha: string } | null

export function ProjecoesPainel() {
  const idReposicao = useId()
  const idProjecoes = useId()
  const [versao, setVersao] = useState(0)
  const [analise, setAnalise] = useState<Analise>(null)
  const [analisando, setAnalisando] = useState(false)
  const alertas = useConsulta(`alertas-${versao}`, listarAlertas)
  const projecoes = useConsulta(`projecoes-${versao}`, listarProjecoes)

  async function solicitar() {
    if (analisando) return
    setAnalisando(true)
    try {
      setAnalise({ resultado: await solicitarAnalise() })
      setVersao(atual => atual + 1)
    } catch (erro) {
      setAnalise({ falha: descreverFalhaDeAnalise(erro) })
    }
    setAnalisando(false)
  }

  return <div className="sp-painel">
    <div className="sp-painel-acoes">
      <p>O servidor calcula a previsão com o uso registrado nos empréstimos, a vida útil do EPI, o risco do setor e a validade do CA.</p>
      <Button icon="calendar" loading={analisando} onClick={() => void solicitar()}>Solicitar análise de projeção</Button>
    </div>
    <ResultadoDaAnalise analise={analise} analisando={analisando} aoTentarNovamente={() => void solicitar()} />
    <section className="sp-secao" aria-labelledby={idReposicao}>
      <h2 id={idReposicao} className="sp-type-heading-sm">Lista de reposição</h2>
      <p className="sp-texto-secundario">Descarte estimado ou CA vencendo em até 30 dias e EPIs com estoque crítico, na ordem enviada pelo servidor.</p>
      <ResultadoConsulta consulta={alertas} carregandoLabel="Carregando lista de reposição..."
        erroTitulo="Não foi possível carregar a lista de reposição."
        vazio={{ title: 'Nenhum alerta de reposição', description: 'Nenhum EPI atende agora às condições de alerta preventivo.' }}>
        {itens => <AlertasTabela alertas={itens} />}
      </ResultadoConsulta>
    </section>
    <section className="sp-secao" aria-labelledby={idProjecoes}>
      <h2 id={idProjecoes} className="sp-type-heading-sm">Projeções registradas</h2>
      <p className="sp-texto-secundario">Projeção mais recente de cada EPI, da data estimada de descarte mais próxima para a mais distante.</p>
      <ResultadoConsulta consulta={projecoes} carregandoLabel="Carregando projeções..."
        erroTitulo="Não foi possível carregar as projeções."
        vazio={{ title: 'Nenhuma projeção registrada', description: 'Solicite a análise de projeção para calcular a previsão de substituição dos EPIs.' }}>
        {itens => <ProjecoesTabela projecoes={maisRecentesPorEpi(itens)} />}
      </ResultadoConsulta>
    </section>
  </div>
}

/** UC10: exibe o resultado da análise ou o erro, sempre com a opção de nova tentativa. */
function ResultadoDaAnalise({ analise, analisando, aoTentarNovamente }: {
  analise: Analise
  analisando: boolean
  aoTentarNovamente: () => void
}) {
  if (analise === null) return null
  if ('falha' in analise) {
    return <div className="sp-secao">
      <Alert tone="danger" live="assertive" title="Análise não concluída">{analise.falha}</Alert>
      <div><Button variant="secondary" loading={analisando} onClick={aoTentarNovamente}>Tentar novamente</Button></div>
    </div>
  }
  const { resultado } = analise
  if (resultado.totalPendencias === 0) {
    return <Alert tone="success" live="polite" title="Análise concluída">
      {resultado.totalSucesso === 1 ? '1 EPI com projeção calculada.' : `${resultado.totalSucesso} EPIs com projeção calculada.`}
    </Alert>
  }
  return <div className="sp-secao">
    <Alert tone="warning" live="polite" title="Análise concluída com pendências">
      {resultado.totalSucesso} de {resultado.totalProcessados} EPIs com projeção calculada. Os EPIs abaixo não têm dados
      suficientes; corrija os dados e solicite nova tentativa.
    </Alert>
    <ul className="sp-pendencias" aria-label="EPIs sem projeção calculada">
      {resultado.pendencias.map((pendencia, indice) => <li key={`${pendencia.epiId ?? 'sem-id'}-${indice}`}>
        <strong>{pendencia.nomeEpi}</strong>: {pendencia.motivo}
      </li>)}
    </ul>
    <div><Button variant="secondary" loading={analisando} onClick={aoTentarNovamente}>Solicitar nova tentativa</Button></div>
  </div>
}

function AlertasTabela({ alertas }: { alertas: AlertaReposicao[] }) {
  return <Table caption="Lista de reposição de EPIs">
    <thead><tr>
      <th scope="col">EPI</th><th scope="col">Alerta</th><th scope="col">Mensagem do servidor</th>
      <th scope="col">Data limite</th><th scope="col">Prazo</th>
    </tr></thead>
    <tbody>{alertas.map((alerta, indice) => <TableRow key={`${alerta.epiId}-${alerta.tipo}-${indice}`}>
      <td><strong>{alerta.nomeEpi}</strong></td>
      <td>{rotuloDoAlerta(alerta.tipo)}</td>
      <td>{alerta.mensagem || 'Sem mensagem.'}</td>
      <td>{alerta.dataLimiteEstimada ? formatarData(alerta.dataLimiteEstimada) : 'Não se aplica'}</td>
      <td>{textoDosDiasRestantes(alerta.diasRestantes)}</td>
    </TableRow>)}</tbody>
  </Table>
}

function ProjecoesTabela({ projecoes }: { projecoes: Projecao[] }) {
  return <Table caption="Projeções de substituição de EPIs">
    <thead><tr>
      <th scope="col">EPI</th><th scope="col">Descarte estimado</th><th scope="col">Justificativa</th>
      <th scope="col">Validade do CA</th><th scope="col">Saldo</th><th scope="col">Calculada em</th>
    </tr></thead>
    <tbody>{projecoes.map(projecao => <TableRow key={projecao.id}>
      <td><strong>{projecao.nomeEpi}</strong></td>
      <td>{formatarData(projecao.dataEstimadaDescarte)}</td>
      <td>{projecao.justificativa}</td>
      <td>{projecao.validadeCa ? formatarData(projecao.validadeCa) : 'Não informada'}</td>
      <td>{projecao.quantidadeDisponivel ?? 'Não informado'}
        {projecao.estoqueMinimo !== null && <span className="sp-texto-secundario"> (mínimo {projecao.estoqueMinimo})</span>}</td>
      <td>{formatarData(projecao.dataProjecao)}</td>
    </TableRow>)}</tbody>
  </Table>
}
