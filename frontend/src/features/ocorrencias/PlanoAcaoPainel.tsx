// Plano de ação de uma ocorrência (UC07; UC05, alternativo I, passo 9): resumo das ações abertas, em
// atraso e com alerta, a lista de ações e, para o Gestor, o cadastro de nova ação com medidas,
// prazo, responsável (colaborador ou setor) e alerta. O Supervisor acompanha sem alterar. O envio da nova
// ação vem do hook useEnvioFormulario (Template Method, #243).

import { useCallback, useState } from 'react'
import { Alert, Button, Checkbox, Radio, Select, TextArea, TextField, Toast } from '../../shared/components'
import { useEnvioFormulario } from '../../shared/formularios/useEnvioFormulario'
import { useConsulta } from '../../shared/api/useConsulta'
import { descreverSetor, listarSetores, type Setor } from '../areas-risco/api'
import { hojeIso } from '../usuarios/formatos'
import { listarColaboradores, listarSupervisores, type Usuario } from '../usuarios/api'
import { avisarOcorrenciasAlteradas, descreverFalhaDeOcorrencia, type OcorrenciaResumo } from './api'
import { cadastrarAcao, listarAcoes } from './apiPlanos'
import { AcaoPlanoItem } from './AcaoPlanoItem'
import { lerNovaAcao, ordemDosCamposDaAcao, validarNovaAcao, type ErrosAcao } from './validacaoPlano'

/**
 * Responsáveis possíveis (contrato da issue #97, seção 2.2): colaboradores e também supervisores,
 * que são subclasses de Colaborador. Ser responsável não dá acesso ao sistema.
 */
async function carregarResponsaveis(signal: AbortSignal): Promise<Usuario[]> {
  const [colaboradores, supervisores] = await Promise.all([listarColaboradores({}, signal), listarSupervisores(signal)])
  const vistos = new Set(colaboradores.map(pessoa => pessoa.id))
  return [...colaboradores, ...supervisores.filter(pessoa => !vistos.has(pessoa.id))]
}
const semLista = (): Promise<never[]> => Promise.resolve([])

function nomeDoResponsavel(pessoa: Usuario): string {
  return pessoa.perfil === 'SUPERVISOR' ? `${pessoa.nome} (Supervisor)` : pessoa.nome
}

/** Acidente com gravidade alta ou crítica exige ação preventiva (decisão DS09, seção 2.7). */
function ehGrave(ocorrencia: OcorrenciaResumo): boolean {
  return ocorrencia.tipoOcorrencia === 'ACIDENTE' && (ocorrencia.gravidade === 'ALTO' || ocorrencia.gravidade === 'CRITICO')
}

export function PlanoAcaoPainel({ ocorrencia, gestor, offline, ativo }: {
  ocorrencia: OcorrenciaResumo
  gestor: boolean
  offline: boolean
  /** Aba visível: só então o Gestor carrega as listas de responsáveis e setores. */
  ativo: boolean
}) {
  const id = ocorrencia.idOcorrencia
  const carregar = useCallback((signal: AbortSignal) => listarAcoes(id, signal), [id])
  const acoes = useConsulta(`plano-${id}`, carregar)
  const listas = gestor && ativo
  const colaboradores = useConsulta(listas ? 'responsaveis-plano' : 'sem-responsaveis', listas ? carregarResponsaveis : semLista)
  const setores = useConsulta(listas ? 'setores-plano' : 'sem-setores', listas ? listarSetores : semLista)
  const [novaAberta, setNovaAberta] = useState(false)
  const [aviso, setAviso] = useState('')
  const hoje = hojeIso()

  function alterou(mensagem: string) {
    setAviso(mensagem)
    acoes.recarregar()
    avisarOcorrenciasAlteradas()
  }

  const lista = acoes.dados ?? []
  const abertas = lista.filter(acao => acao.situacao === 'ABERTA').length
  const atrasadas = lista.filter(acao => acao.emAtraso).length
  const comAlerta = lista.filter(acao => acao.alertaDevido).length
  const podeEditar = gestor && !offline

  return <section className="sp-plano" aria-labelledby={`plano-${id}-titulo`}>
    <div className="sp-plano-topo">
      <h3 id={`plano-${id}-titulo`} className="sp-type-heading-sm">Plano de ação</h3>
      {podeEditar && !novaAberta && <Button icon="plus" onClick={() => setNovaAberta(true)}>Nova ação</Button>}
    </div>
    {!gestor && <p className="sp-texto-secundario">Acompanhamento das ações definidas pelo Gestor de Segurança.</p>}
    {ehGrave(ocorrencia) && <Alert tone="warning" title="Acidente grave">
      Gravidade {ocorrencia.gravidade === 'CRITICO' ? 'crítica' : 'alta'}: o plano precisa de ao menos uma ação com medida preventiva.
    </Alert>}

    {novaAberta && <NovaAcaoForm ocorrencia={ocorrencia} hoje={hoje}
      colaboradores={colaboradores.dados ?? []} setores={setores.dados ?? []}
      listasCarregando={colaboradores.carregando || setores.carregando}
      aoCancelar={() => setNovaAberta(false)}
      aoSalvar={mensagem => { setNovaAberta(false); alterou(mensagem) }} />}

    <Toast message={aviso} onDismiss={() => setAviso('')} />

    {acoes.erro && <>
      <Alert tone="danger" title="Não foi possível carregar o plano de ação" live="assertive">Verifique sua conexão e tente novamente.</Alert>
      <div><Button variant="secondary" onClick={acoes.recarregar}>Tentar novamente</Button></div>
    </>}
    {acoes.carregando && <p role="status">Carregando plano de ação...</p>}
    {acoes.dados && (lista.length === 0
      ? <p className="sp-texto-secundario">Nenhuma ação definida para esta ocorrência.</p>
      : <>
        <p className="sp-plano-resumo" role="status">
          {lista.length} {lista.length === 1 ? 'ação' : 'ações'} · {abertas} {abertas === 1 ? 'aberta' : 'abertas'}
          {atrasadas > 0 && <> · <strong>{atrasadas} em atraso</strong></>}
          {comAlerta > 0 && <> · {comAlerta} com alerta de prazo</>}
        </p>
        <div className="sp-acoes-lista">
          {lista.map(acao => <AcaoPlanoItem key={acao.id} acao={acao} hoje={hoje} gestor={podeEditar}
            colaboradores={colaboradores.dados ?? []} setores={setores.dados ?? []} aoAlterar={alterou} />)}
        </div>
      </>)}
  </section>
}

function NovaAcaoForm({ ocorrencia, hoje, colaboradores, setores, listasCarregando, aoCancelar, aoSalvar }: {
  ocorrencia: OcorrenciaResumo
  hoje: string
  colaboradores: Usuario[]
  setores: Setor[]
  listasCarregando: boolean
  aoCancelar: () => void
  aoSalvar: (mensagem: string) => void
}) {
  const [tipoResponsavel, setTipoResponsavel] = useState<'' | 'COLABORADOR' | 'SETOR'>('')
  const [alertaAtivo, setAlertaAtivo] = useState(false)
  const diaDoFato = ocorrencia.dataOcorrencia.slice(0, 10)
  const prazoMinimo = diaDoFato > hoje ? diaDoFato : hoje

  const { enviando, erros, falha, onSubmit } = useEnvioFormulario({
    ler: form => lerNovaAcao(new FormData(form)),
    validar: leitura => {
      const pendencias = validarNovaAcao(leitura, prazoMinimo)
      return { dados: ordemDosCamposDaAcao.some(campo => pendencias[campo]) ? null : leitura.dados, erros: pendencias }
    },
    ordem: ordemDosCamposDaAcao,
    enviar: async dados => {
      const acao = await cadastrarAcao(ocorrencia.idOcorrencia, dados)
      aoSalvar(`Ação #${acao.id} cadastrada no plano.`)
    },
    descreverFalha: descreverFalhaDeOcorrencia,
  }, {} as ErrosAcao)

  return <form className="sp-form sp-acao-operacao" noValidate onSubmit={onSubmit} aria-label="Nova ação do plano">
    <TextArea label="Medida corretiva" name="medidasCorretivas" rows={2} error={erros.medidasCorretivas}
      helper={erros.medidasCorretivas ? undefined : 'Corrige a causa do fato. Informe esta, a preventiva ou ambas.'} />
    <TextArea label="Medida preventiva" name="medidasPreventivas" rows={2}
      helper="Evita que o fato se repita." />
    <TextField label="Prazo *" name="prazo" type="date" min={prazoMinimo} required error={erros.prazo}
      helper={erros.prazo ? undefined : 'Não pode ser anterior a hoje nem à data do fato.'} />
    <fieldset className="sp-fieldset">
      <legend className="sp-field-label">Responsável *</legend>
      <div className="sp-opcoes">
        <Radio label="Colaborador" name="responsavelTipo" value="COLABORADOR" checked={tipoResponsavel === 'COLABORADOR'}
          onChange={() => setTipoResponsavel('COLABORADOR')} aria-invalid={erros.responsavelTipo ? true : undefined} />
        <Radio label="Setor" name="responsavelTipo" value="SETOR" checked={tipoResponsavel === 'SETOR'}
          onChange={() => setTipoResponsavel('SETOR')} aria-invalid={erros.responsavelTipo ? true : undefined} />
      </div>
      {erros.responsavelTipo && <p role="alert" className="sp-field-message sp-field-message--error">{erros.responsavelTipo}</p>}
    </fieldset>
    {tipoResponsavel && <Select key={tipoResponsavel} label={tipoResponsavel === 'SETOR' ? 'Setor responsável *' : 'Colaborador responsável *'}
      name="responsavelId" defaultValue="" required error={erros.responsavelId}
      helper={listasCarregando ? 'Carregando opções...' : 'Ser responsável não cria conta de acesso; o aviso chega ao Gestor.'}>
      <option value="">Selecione</option>
      {tipoResponsavel === 'SETOR'
        ? setores.map(setor => <option key={setor.id} value={setor.id}>{descreverSetor(setor)}</option>)
        : colaboradores.map(pessoa => <option key={pessoa.id} value={pessoa.id}>{nomeDoResponsavel(pessoa)}</option>)}
    </Select>}
    <Checkbox label="Ativar alerta de prazo" name="alertaAtivo" checked={alertaAtivo} onChange={event => setAlertaAtivo(event.target.checked)} />
    {alertaAtivo && <TextField label="Antecedência do alerta em dias *" name="antecedenciaAlertaDias" type="number" inputMode="numeric"
      min={0} step={1} required error={erros.antecedenciaAlertaDias}
      helper={erros.antecedenciaAlertaDias ? undefined : 'Quantos dias antes do prazo o alerta passa a aparecer.'} />}
    {falha && <Alert tone="danger" title="Ação não cadastrada" live="assertive">{falha}</Alert>}
    <div className="sp-dialog-actions">
      <Button variant="secondary" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" loading={enviando} icon="check">Cadastrar ação</Button>
    </div>
  </form>
}
