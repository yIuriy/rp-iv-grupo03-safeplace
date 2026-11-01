// Diálogo "Novo empréstimo" (UC12, passos 2 a 6; US11, critérios 1, 5 e 6): escolhe colaborador ativo
// e EPI disponível, pede a data prevista de devolução, mostra o responsável conectado, trata a
// duplicidade de tipo com confirmação e justificativa, exibe o impedimento informado pelo servidor e,
// ao concluir, o termo de cautela. O conteúdo só existe com o diálogo aberto: cada abertura recomeça
// com listas atualizadas e campos vazios.

import { useState, type FormEvent } from 'react'
import { Alert, Button, Dialog, Select, TextField } from '../../shared/components'
import { focarCampo } from '../../shared/formularios/envioFormulario'
import { useConsulta, type Consulta } from '../../shared/api/useConsulta'
import { useSessaoAtiva } from '../autenticacao/contexto'
import type { Epi } from '../estoque/api'
import type { Usuario } from '../usuarios/api'
import { hojeIso } from '../usuarios/formatos'
import {
  descreverFalhaDeEntrega, duplicidadeDaFalha, listarColaboradoresAtivos, listarEpisDisponiveis, registrarEntrega,
  type DadosEntrega, type Emprestimo,
} from './api'
import { DuplicidadeConfirmacao, EntregaConcluida } from './EntregaEtapas'
import { rotuloDoColaborador, rotuloDoEpi } from './formatos'
import {
  lerRascunho, ordemDosCamposDeEntrega, paraDadosDeEntrega, rascunhoVazio, validarEntrega,
  type ErrosEntrega, type RascunhoEntrega,
} from './validacaoEntrega'

type Etapa =
  | { tipo: 'formulario'; voltouDaDuplicidade: boolean }
  | { tipo: 'duplicidade'; mensagem: string; dados: DadosEntrega }
  | { tipo: 'concluida'; emprestimo: Emprestimo }

type AoRegistrar = (emprestimo: Emprestimo) => void

export function EntregaDialog({ aberto, aoFechar, aoRegistrar }: {
  aberto: boolean
  aoFechar: () => void
  aoRegistrar: AoRegistrar
}) {
  return <Dialog open={aberto} title="Novo empréstimo" onClose={aoFechar}
    description="Identifique o colaborador e o equipamento que será entregue.">
    {aberto && <EntregaConteudo aoFechar={aoFechar} aoRegistrar={aoRegistrar} />}
  </Dialog>
}

function EntregaConteudo({ aoFechar, aoRegistrar }: { aoFechar: () => void; aoRegistrar: AoRegistrar }) {
  const colaboradores = useConsulta('colaboradores-ativos', listarColaboradoresAtivos)
  const epis = useConsulta('epis-disponiveis', listarEpisDisponiveis)
  const [etapa, setEtapa] = useState<Etapa>({ tipo: 'formulario', voltouDaDuplicidade: false })
  const [rascunho, setRascunho] = useState<RascunhoEntrega>(rascunhoVazio)
  const [falha, setFalha] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(dados: DadosEntrega) {
    setFalha('')
    setEnviando(true)
    try {
      const emprestimo = await registrarEntrega(dados)
      aoRegistrar(emprestimo)
      setEtapa({ tipo: 'concluida', emprestimo })
    } catch (erro) {
      const duplicidade = dados.confirmarSubstituicao ? null : duplicidadeDaFalha(erro)
      if (duplicidade) setEtapa({ tipo: 'duplicidade', mensagem: duplicidade, dados })
      else setFalha(descreverFalhaDeEntrega(erro))
    }
    setEnviando(false)
  }

  if (etapa.tipo === 'concluida') return <EntregaConcluida emprestimo={etapa.emprestimo} aoFechar={aoFechar} />
  if (etapa.tipo === 'duplicidade') {
    const { dados, mensagem } = etapa
    return <DuplicidadeConfirmacao mensagem={mensagem} falha={falha} enviando={enviando}
      aoVoltar={() => {
        setFalha('')
        setEtapa({ tipo: 'formulario', voltouDaDuplicidade: true })
      }}
      aoConfirmar={justificativa => {
        void enviar({ ...dados, confirmarSubstituicao: true, justificativaSubstituicao: justificativa })
      }} />
  }
  return <EntregaFormulario colaboradores={colaboradores} epis={epis} rascunho={rascunho} falha={falha}
    enviando={enviando} focarPrimeiro={etapa.voltouDaDuplicidade} aoCancelar={aoFechar}
    aoEnviar={novo => {
      setRascunho(novo)
      void enviar(paraDadosDeEntrega(novo))
    }} />
}

function EntregaFormulario({ colaboradores, epis, rascunho, falha, enviando, focarPrimeiro, aoCancelar, aoEnviar }: {
  colaboradores: Consulta<Usuario[]>
  epis: Consulta<Epi[]>
  rascunho: RascunhoEntrega
  falha: string
  enviando: boolean
  focarPrimeiro: boolean
  aoCancelar: () => void
  aoEnviar: (rascunho: RascunhoEntrega) => void
}) {
  const sessao = useSessaoAtiva()
  const [erros, setErros] = useState<ErrosEntrega>({})
  const hoje = hojeIso()
  const pronto = Boolean(colaboradores.dados?.length && epis.dados?.length)

  function submeter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (enviando || !pronto) return
    const form = event.currentTarget
    const novo = lerRascunho(new FormData(form))
    const pendencias = validarEntrega(novo, hoje)
    setErros(pendencias)
    const primeiro = ordemDosCamposDeEntrega.find(campo => pendencias[campo])
    if (!primeiro) {
      aoEnviar(novo)
      return
    }
    focarCampo(form, primeiro)
  }

  return <form className="sp-form" onSubmit={submeter} noValidate>
    <CampoDeLista consulta={colaboradores} name="colaboradorId" valorInicial={rascunho.colaboradorId}
      erro={erros.colaboradorId} focar={focarPrimeiro}
      opcao={colaborador => ({ valor: String(colaborador.id), texto: rotuloDoColaborador(colaborador) })}
      textos={{
        rotulo: 'Colaborador', placeholder: 'Selecione o colaborador',
        ajuda: 'Somente colaboradores ativos. O colaborador não tem conta de acesso.',
        carregando: 'Carregando colaboradores...', falha: 'Não foi possível carregar os colaboradores.',
        vazioTitulo: 'Nenhum colaborador ativo cadastrado',
        vazioTexto: 'Cadastre o colaborador na tela Usuários antes de registrar a entrega.',
      }} />
    <CampoDeLista consulta={epis} name="epiId" valorInicial={rascunho.epiId} erro={erros.epiId} focar={false}
      opcao={epi => ({ valor: String(epi.id), texto: rotuloDoEpi(epi) })}
      textos={{
        rotulo: 'EPI disponível', placeholder: 'Selecione o EPI',
        ajuda: 'Situação Disponível, com saldo e CA válido. Cada empréstimo retém uma unidade.',
        carregando: 'Carregando EPIs disponíveis...', falha: 'Não foi possível carregar os EPIs disponíveis.',
        vazioTitulo: 'Nenhum EPI disponível para empréstimo',
        vazioTexto: 'Os EPIs precisam estar na situação Disponível, com saldo e CA válido.',
      }} />
    <TextField label="Data prevista de devolução" name="dataDevolucaoPrevista" type="date" min={hoje} required
      defaultValue={rascunho.dataDevolucaoPrevista} error={erros.dataDevolucaoPrevista}
      helper="Prazo combinado com o colaborador. Não pode ser anterior a hoje." />
    <TextField label="Responsável pela entrega" value={sessao.nome} readOnly
      helper="Usuário conectado. A data e a hora da entrega são registradas na confirmação." />
    {falha && <Alert tone="danger" title="Entrega não registrada" live="assertive">{falha}</Alert>}
    <p className="sp-dialog-note">Ao confirmar, o sistema registra o termo de cautela e a saída de uma unidade do estoque.</p>
    <div className="sp-dialog-actions">
      <Button variant="quiet" onClick={aoCancelar} disabled={enviando}>Cancelar</Button>
      <Button type="submit" icon="check" loading={enviando} disabled={!pronto}>Confirmar entrega</Button>
    </div>
  </form>
}

type TextosDaLista = {
  rotulo: string
  placeholder: string
  ajuda: string
  carregando: string
  falha: string
  vazioTitulo: string
  vazioTexto: string
}

/** Select alimentado pela API, com os estados carregando, erro com nova tentativa e lista vazia. */
function CampoDeLista<T>({ consulta, name, valorInicial, erro, focar, opcao, textos }: {
  consulta: Consulta<T[]>
  name: string
  valorInicial: string
  erro?: string
  focar: boolean
  opcao: (item: T) => { valor: string; texto: string }
  textos: TextosDaLista
}) {
  if (consulta.erro) {
    return <div className="sp-campo-lista">
      <Alert tone="danger" live="assertive" title={textos.falha}>Verifique sua conexão ou tente novamente.</Alert>
      <div><Button variant="secondary" onClick={consulta.recarregar}>Tentar novamente</Button></div>
    </div>
  }
  if (consulta.dados === null) return <p role="status">{textos.carregando}</p>
  if (consulta.dados.length === 0) return <Alert tone="warning" live="polite" title={textos.vazioTitulo}>{textos.vazioTexto}</Alert>
  return <Select label={textos.rotulo} name={name} defaultValue={valorInicial} required autoFocus={focar}
    error={erro} helper={textos.ajuda}>
    <option value="">{textos.placeholder}</option>
    {consulta.dados.map(item => {
      const { valor, texto } = opcao(item)
      return <option key={valor} value={valor}>{texto}</option>
    })}
  </Select>
}
