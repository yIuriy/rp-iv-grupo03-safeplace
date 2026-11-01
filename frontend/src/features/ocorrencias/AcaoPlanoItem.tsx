// Uma ação do plano (UC07): medidas, prazo, responsável, situação, atraso e alerta calculados pelo
// servidor (issue #196), e o histórico de prorrogações. Ao Gestor, em ação aberta, oferece concluir, prorrogar com
// justificativa, reatribuir o responsável e configurar o alerta, em formulários dentro do próprio item
// (sem diálogo sobre diálogo). Supervisor só acompanha.

import { useCallback, useState, type FormEvent } from 'react'
import { Alert, Badge, Button, Checkbox, Radio, Select, TextArea, TextField } from '../../shared/components'
import { focarCampo } from '../../shared/formularios/envioFormulario'
import { useConsulta } from '../../shared/api/useConsulta'
import { descreverSetor, type Setor } from '../areas-risco/api'
import { formatarData } from '../estoque/formatos'
import type { Usuario } from '../usuarios/api'
import { descreverFalhaDeOcorrencia } from './api'
import {
  concluirAcao,
  configurarAlerta,
  listarProrrogacoes,
  prorrogarPrazo,
  reatribuirResponsavel,
  rotuloSituacaoAcao,
  type AcaoPlano,
} from './apiPlanos'
import { validarAntecedencia, validarProrrogacao, type ErrosProrrogacao } from './validacaoPlano'

type Operacao = 'concluir' | 'prorrogar' | 'reatribuir' | 'alerta' | null

export function AcaoPlanoItem({ acao, hoje, gestor, colaboradores, setores, aoAlterar }: {
  acao: AcaoPlano
  hoje: string
  gestor: boolean
  colaboradores: Usuario[]
  setores: Setor[]
  aoAlterar: (mensagem: string) => void
}) {
  const [operacao, setOperacao] = useState<Operacao>(null)
  const [historicoAberto, setHistoricoAberto] = useState(false)
  const atraso = acao.emAtraso
  const alerta = acao.alertaDevido
  const aberta = acao.situacao === 'ABERTA'
  const titulo = `Ação #${acao.id}`

  function concluiu(mensagem: string) {
    setOperacao(null)
    aoAlterar(mensagem)
  }

  return <article className="sp-acao" aria-labelledby={`acao-${acao.id}-titulo`}>
    <header className="sp-acao-cabecalho">
      <h4 id={`acao-${acao.id}-titulo`} className="sp-acao-titulo">{titulo}</h4>
      <div className="sp-acao-marcas">
        {acao.situacao
          ? <Badge tone={aberta ? 'info' : 'success'}>{rotuloSituacaoAcao[acao.situacao]}</Badge>
          : <Badge tone="warning">Situação não reconhecida: {acao.statusGravado || 'vazia'}</Badge>}
        {atraso && <Badge tone="danger">Em atraso</Badge>}
        {alerta && <Badge tone="warning">Alerta de prazo</Badge>}
      </div>
    </header>

    <dl className="sp-detalhes sp-acao-dados">
      {acao.medidasCorretivas && <div className="sp-detalhes-largo"><dt>Medida corretiva</dt><dd>{acao.medidasCorretivas}</dd></div>}
      {acao.medidasPreventivas && <div className="sp-detalhes-largo"><dt>Medida preventiva</dt><dd>{acao.medidasPreventivas}</dd></div>}
      <div><dt>Prazo</dt><dd>{formatarData(acao.prazo)}</dd></div>
      <div><dt>Responsável</dt><dd>{acao.responsavelNome
        ? `${acao.responsavelTipo === 'SETOR' ? 'Setor' : 'Colaborador'}: ${acao.responsavelNome}`
        : 'Não definido'}</dd></div>
      <div><dt>Alerta</dt><dd>{acao.alertaAtivo
        ? `${acao.antecedenciaAlertaDias ?? 0} dia(s) antes${acao.dataAlerta ? `, a partir de ${formatarData(acao.dataAlerta)}` : ''}`
        : 'Inativo'}</dd></div>
      {acao.gestorNome && <div><dt>Definida por</dt><dd>{acao.gestorNome}</dd></div>}
    </dl>

    {alerta && <Alert tone={atraso ? 'danger' : 'warning'} title={atraso ? 'Prazo vencido' : 'Prazo próximo'}>
      {atraso
        ? `A ação venceu em ${formatarData(acao.prazo)} e continua aberta. Avise o responsável ou prorrogue com justificativa.`
        : `O prazo vence em ${formatarData(acao.prazo)}. Avise o responsável${acao.responsavelNome ? ` (${acao.responsavelNome})` : ''}.`}
    </Alert>}

    <div className="sp-acao-botoes">
      {gestor && aberta && <>
        <Button variant="secondary" icon="check" aria-expanded={operacao === 'concluir'}
          onClick={() => setOperacao(operacao === 'concluir' ? null : 'concluir')}>Concluir</Button>
        <Button variant="secondary" icon="calendar" aria-expanded={operacao === 'prorrogar'}
          onClick={() => setOperacao(operacao === 'prorrogar' ? null : 'prorrogar')}>Prorrogar prazo</Button>
        <Button variant="secondary" icon="users" aria-expanded={operacao === 'reatribuir'}
          onClick={() => setOperacao(operacao === 'reatribuir' ? null : 'reatribuir')}>Reatribuir responsável</Button>
        <Button variant="secondary" icon="alert-triangle" aria-expanded={operacao === 'alerta'}
          onClick={() => setOperacao(operacao === 'alerta' ? null : 'alerta')}>Configurar alerta</Button>
      </>}
      <Button variant="quiet" aria-expanded={historicoAberto} onClick={() => setHistoricoAberto(aberto => !aberto)}>
        {historicoAberto ? 'Ocultar prorrogações' : 'Histórico de prorrogações'}
      </Button>
    </div>

    {operacao === 'concluir' && <ConfirmarConclusao acao={acao} aoCancelar={() => setOperacao(null)} aoConcluir={concluiu} />}
    {operacao === 'prorrogar' && <FormProrrogacao acao={acao} hoje={hoje} aoCancelar={() => setOperacao(null)} aoConcluir={concluiu} />}
    {operacao === 'reatribuir' && <FormReatribuicao acao={acao} colaboradores={colaboradores} setores={setores}
      aoCancelar={() => setOperacao(null)} aoConcluir={concluiu} />}
    {operacao === 'alerta' && <FormAlerta acao={acao} aoCancelar={() => setOperacao(null)} aoConcluir={concluiu} />}
    {historicoAberto && <HistoricoProrrogacoes idAcao={acao.id} />}
  </article>
}

type PropsOperacao = { acao: AcaoPlano; aoCancelar: () => void; aoConcluir: (mensagem: string) => void }

function useEnvio() {
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)
  async function executar(tarefa: () => Promise<unknown>, aoTerminar: () => void) {
    setFalha('')
    setEnviando(true)
    try {
      await tarefa()
      aoTerminar()
    } catch (erro) {
      setFalha(descreverFalhaDeOcorrencia(erro))
      setEnviando(false)
    }
  }
  return { falha, enviando, executar }
}

function Falha({ titulo, texto }: { titulo: string; texto: string }) {
  return texto ? <Alert tone="danger" title={titulo} live="assertive">{texto}</Alert> : null
}

function ConfirmarConclusao({ acao, aoCancelar, aoConcluir }: PropsOperacao) {
  const { falha, enviando, executar } = useEnvio()
  return <div className="sp-acao-operacao" role="group" aria-label={`Concluir ação #${acao.id}`}>
    <p>Confirmar que a ação foi executada? A conclusão é definitiva: a ação não poderá ser prorrogada nem reatribuída.</p>
    <Falha titulo="Ação não concluída" texto={falha} />
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button loading={enviando} icon="check"
        onClick={() => executar(() => concluirAcao(acao.id), () => aoConcluir(`Ação #${acao.id} concluída.`))}>Confirmar conclusão</Button>
    </div>
  </div>
}

function FormProrrogacao({ acao, hoje, aoCancelar, aoConcluir }: PropsOperacao & { hoje: string }) {
  const { falha, enviando, executar } = useEnvio()
  const [erros, setErros] = useState<ErrosProrrogacao>({})

  function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const dados = new FormData(form)
    const novoPrazo = String(dados.get('novoPrazo') ?? '')
    const justificativa = String(dados.get('justificativa') ?? '').trim()
    const pendencias = validarProrrogacao(novoPrazo, justificativa, acao.prazo, hoje)
    setErros(pendencias)
    const primeiro = (['novoPrazo', 'justificativa'] as const).find(campo => pendencias[campo])
    if (primeiro) {
      focarCampo(form, primeiro)
      return
    }
    void executar(() => prorrogarPrazo(acao.id, novoPrazo, justificativa),
      () => aoConcluir(`Prazo da ação #${acao.id} prorrogado para ${formatarData(novoPrazo)}.`))
  }

  return <form className="sp-form sp-acao-operacao" noValidate onSubmit={enviar} aria-label={`Prorrogar prazo da ação #${acao.id}`}>
    <TextField label="Novo prazo *" name="novoPrazo" type="date" min={acao.prazo > hoje ? acao.prazo : hoje} required
      error={erros.novoPrazo} helper={erros.novoPrazo ? undefined : `Prazo atual: ${formatarData(acao.prazo)}. O alerta é reajustado ao novo prazo.`} />
    <TextArea label="Justificativa técnica *" name="justificativa" rows={2} required error={erros.justificativa} />
    <Falha titulo="Prazo não prorrogado" texto={falha} />
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="calendar">Prorrogar</Button>
    </div>
  </form>
}

function FormReatribuicao({ acao, colaboradores, setores, aoCancelar, aoConcluir }: PropsOperacao & { colaboradores: Usuario[]; setores: Setor[] }) {
  const { falha, enviando, executar } = useEnvio()
  const [tipo, setTipo] = useState<'COLABORADOR' | 'SETOR'>(acao.responsavelTipo ?? 'COLABORADOR')
  const [erro, setErro] = useState<string>()

  function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const id = Number(new FormData(form).get('responsavelId')) || 0
    const mesmo = id === acao.responsavelId && tipo === acao.responsavelTipo
    const pendencia = !id ? 'Selecione o novo responsável.' : mesmo ? 'Escolha um responsável diferente do atual.' : undefined
    setErro(pendencia)
    if (pendencia) {
      focarCampo(form, 'responsavelId')
      return
    }
    const nome = tipo === 'SETOR' ? setores.find(s => s.id === id)?.nome : colaboradores.find(c => c.id === id)?.nome
    void executar(() => reatribuirResponsavel(acao.id, tipo === 'SETOR' ? { setorId: id } : { colaboradorId: id }),
      () => aoConcluir(`Ação #${acao.id} reatribuída a ${nome ?? 'novo responsável'}. Avise o novo encarregado.`))
  }

  return <form className="sp-form sp-acao-operacao" noValidate onSubmit={enviar} aria-label={`Reatribuir responsável da ação #${acao.id}`}>
    <fieldset className="sp-fieldset">
      <legend className="sp-field-label">Tipo de responsável</legend>
      <div className="sp-opcoes">
        <Radio label="Colaborador" name="tipoReatribuicao" value="COLABORADOR" checked={tipo === 'COLABORADOR'} onChange={() => setTipo('COLABORADOR')} />
        <Radio label="Setor" name="tipoReatribuicao" value="SETOR" checked={tipo === 'SETOR'} onChange={() => setTipo('SETOR')} />
      </div>
    </fieldset>
    <Select key={tipo} label={tipo === 'SETOR' ? 'Novo setor responsável *' : 'Novo colaborador responsável *'}
      name="responsavelId" defaultValue="" required error={erro}>
      <option value="">Selecione</option>
      {tipo === 'SETOR'
        ? setores.map(setor => <option key={setor.id} value={setor.id}>{descreverSetor(setor)}</option>)
        : colaboradores.map(pessoa => <option key={pessoa.id} value={pessoa.id}>{pessoa.perfil === 'SUPERVISOR' ? `${pessoa.nome} (Supervisor)` : pessoa.nome}</option>)}
    </Select>
    <Falha titulo="Responsável não reatribuído" texto={falha} />
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="users">Reatribuir</Button>
    </div>
  </form>
}

function FormAlerta({ acao, aoCancelar, aoConcluir }: PropsOperacao) {
  const { falha, enviando, executar } = useEnvio()
  const [ativo, setAtivo] = useState(acao.alertaAtivo)
  const [erro, setErro] = useState<string>()

  function enviar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = event.currentTarget
    const valor = String(new FormData(form).get('antecedenciaDias') ?? '')
    const pendencia = validarAntecedencia(ativo, valor)
    setErro(pendencia)
    if (pendencia) {
      focarCampo(form, 'antecedenciaDias')
      return
    }
    void executar(() => configurarAlerta(acao.id, ativo, ativo ? Number(valor) : undefined),
      () => aoConcluir(ativo ? `Alerta da ação #${acao.id} configurado para ${Number(valor)} dia(s) antes do prazo.` : `Alerta da ação #${acao.id} desativado.`))
  }

  return <form className="sp-form sp-acao-operacao" noValidate onSubmit={enviar} aria-label={`Configurar alerta da ação #${acao.id}`}>
    <Checkbox label="Alerta de prazo ativo" name="ativo" checked={ativo} onChange={event => setAtivo(event.target.checked)} />
    {ativo && <TextField label="Antecedência em dias *" name="antecedenciaDias" type="number" inputMode="numeric" min={0} step={1}
      defaultValue={acao.antecedenciaAlertaDias ?? ''} required error={erro}
      helper={erro ? undefined : 'Quantos dias antes do prazo o alerta passa a aparecer.'} />}
    <Falha titulo="Alerta não configurado" texto={falha} />
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">Salvar alerta</Button>
    </div>
  </form>
}

function HistoricoProrrogacoes({ idAcao }: { idAcao: number }) {
  const carregar = useCallback((signal: AbortSignal) => listarProrrogacoes(idAcao, signal), [idAcao])
  const consulta = useConsulta(`prorrogacoes-${idAcao}`, carregar)
  if (consulta.erro) {
    return <Alert tone="danger" title="Não foi possível carregar as prorrogações" live="assertive">
      <Button variant="quiet" onClick={consulta.recarregar}>Tentar novamente</Button>
    </Alert>
  }
  if (!consulta.dados) return <p role="status">Carregando prorrogações...</p>
  if (consulta.dados.length === 0) return <p className="sp-texto-secundario">Nenhuma prorrogação registrada.</p>
  return <ol className="sp-prorrogacoes" aria-label={`Prorrogações da ação #${idAcao}`}>
    {consulta.dados.map((p, indice) => <li key={`${p.novoPrazo}-${indice}`}>
      De <strong>{formatarData(p.prazoAnterior)}</strong> para <strong>{formatarData(p.novoPrazo)}</strong>: {p.justificativa}
    </li>)}
  </ol>
}
