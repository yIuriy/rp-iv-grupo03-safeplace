// Cliente tipado da API de projeções de substituição (/api/projecoes; RF21, UC10, US21, issue #110):
// projeções registradas, lista de reposição com os alertas preventivos e solicitação da análise de
// projeção de desgaste, com as pendências que pedem nova tentativa (UC10, exceção I). A interface não
// calcula nem completa nenhum valor: datas, justificativas e alertas vêm do servidor.

import { http } from '../../shared/api/https'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../../shared/api/erros'

/** Campos de `ProjecaoSubstituicaoOutputDTO` (decisão de DS10 em docs/arquitetura). */
export type Projecao = {
  id: number
  epiId: number
  nomeEpi: string
  dataProjecao: string
  dataEstimadaDescarte: string
  justificativa: string
  validadeCa: string | null
  quantidadeDisponivel: number | null
  estoqueMinimo: number | null
}

/** Item da lista de reposição (`AlertaSubstituicaoOutputDTO`), sem a prioridade, ainda a confirmar na US21. */
export type AlertaReposicao = {
  epiId: number
  nomeEpi: string
  tipo: string
  mensagem: string
  dataLimiteEstimada: string | null
  diasRestantes: number | null
}

export type PendenciaProjecao = { epiId: number | null; nomeEpi: string; motivo: string }

/**
 * Resumo de `POST /api/projecoes/analise`. As projeções e os alertas da resposta não são lidos: o
 * servidor monta as projeções antes de salvá-las, com `id` nulo, e a tela recarrega as listas salvas.
 */
export type ResultadoAnalise = {
  pendencias: PendenciaProjecao[]
  totalProcessados: number
  totalSucesso: number
  totalPendencias: number
}

function texto(valor: unknown, reserva: string): string {
  return typeof valor === 'string' && valor.trim() ? valor : reserva
}

function textoOuNulo(valor: unknown): string | null {
  return typeof valor === 'string' && valor.trim() ? valor : null
}

function numeroOuNulo(valor: unknown): number | null {
  return typeof valor === 'number' ? valor : null
}

function registro(dado: unknown, descricao: string): Record<string, unknown> {
  if (dado === null || typeof dado !== 'object') throw new Error(`${descricao} inválido`)
  return dado as Record<string, unknown>
}

function lerProjecao(dado: unknown): Projecao {
  const bruto = registro(dado, 'Registro de projeção')
  if (typeof bruto.id !== 'number' || typeof bruto.epiId !== 'number'
    || typeof bruto.dataProjecao !== 'string' || typeof bruto.dataEstimadaDescarte !== 'string') {
    throw new Error('Projeção incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    epiId: bruto.epiId,
    nomeEpi: texto(bruto.nomeEpi, `EPI ${bruto.epiId}`),
    dataProjecao: bruto.dataProjecao,
    dataEstimadaDescarte: bruto.dataEstimadaDescarte,
    justificativa: texto(bruto.justificativa, 'Justificativa não informada pelo servidor.'),
    validadeCa: textoOuNulo(bruto.validadeCa),
    quantidadeDisponivel: numeroOuNulo(bruto.quantidadeDisponivel),
    estoqueMinimo: numeroOuNulo(bruto.estoqueMinimo),
  }
}

function lerAlerta(dado: unknown): AlertaReposicao {
  const bruto = registro(dado, 'Alerta de reposição')
  if (typeof bruto.epiId !== 'number' || typeof bruto.tipoAlerta !== 'string') {
    throw new Error('Alerta incompatível com o contrato da API')
  }
  return {
    epiId: bruto.epiId,
    nomeEpi: texto(bruto.nomeEpi, `EPI ${bruto.epiId}`),
    tipo: bruto.tipoAlerta,
    mensagem: texto(bruto.mensagem, ''),
    dataLimiteEstimada: textoOuNulo(bruto.dataLimiteEstimada),
    diasRestantes: numeroOuNulo(bruto.diasRestantes),
  }
}

function lerPendencia(dado: unknown): PendenciaProjecao {
  const bruto = registro(dado, 'Pendência da análise')
  const epiId = numeroOuNulo(bruto.epiId)
  return {
    epiId,
    nomeEpi: texto(bruto.nomeEpi, epiId === null ? 'EPI não identificado' : `EPI ${epiId}`),
    motivo: texto(bruto.motivoFalha, 'Motivo não informado pelo servidor.'),
  }
}

function lerLista<T>(dados: unknown, ler: (dado: unknown) => T): T[] {
  return Array.isArray(dados) ? dados.map(ler) : []
}

function lerResultado(dado: unknown): ResultadoAnalise {
  const bruto = registro(dado, 'Resultado da análise')
  const calculadas = Array.isArray(bruto.projecoes) ? bruto.projecoes.length : 0
  const pendencias = lerLista(bruto.pendenciasFalhas, lerPendencia)
  return {
    pendencias,
    totalProcessados: numeroOuNulo(bruto.totalProcessados) ?? calculadas + pendencias.length,
    totalSucesso: numeroOuNulo(bruto.totalSucesso) ?? calculadas,
    totalPendencias: numeroOuNulo(bruto.totalPendencias) ?? pendencias.length,
  }
}

function lerListaObrigatoria<T>(dados: unknown, ler: (dado: unknown) => T, descricao: string): T[] {
  if (!Array.isArray(dados)) throw new Error(`Esperava uma lista de ${descricao}`)
  return dados.map(ler)
}

/** `GET /api/projecoes`: histórico de projeções registradas pelas análises anteriores. */
export async function listarProjecoes(signal?: AbortSignal): Promise<Projecao[]> {
  const { data } = await http.get<unknown>('/projecoes', { signal })
  return lerListaObrigatoria(data, lerProjecao, 'projeções')
}

/** `GET /api/projecoes/alertas`: lista de reposição com a antecedência padrão do servidor (30 dias). */
export async function listarAlertas(signal?: AbortSignal): Promise<AlertaReposicao[]> {
  const { data } = await http.get<unknown>('/projecoes/alertas', { signal })
  return lerListaObrigatoria(data, lerAlerta, 'alertas de reposição')
}

/** Exclusivo do Gestor (`POST /api/projecoes/**`). Sem corpo, o servidor usa a data de hoje e 90 dias de amostragem. */
export async function solicitarAnalise(): Promise<ResultadoAnalise> {
  const { data } = await http.post<unknown>('/projecoes/analise')
  return lerResultado(data)
}

export const descreverFalhaDeAnalise = criarDescritorDeFalha({
  403: 'Somente o Gestor de Segurança pode solicitar a análise de projeção.',
}, usarMensagemDaApi('Não foi possível concluir a análise de projeção agora. Tente novamente em alguns instantes.'))

/** Cada análise grava novas projeções; a tabela mostra só a mais recente de cada EPI. */
export function maisRecentesPorEpi(projecoes: Projecao[]): Projecao[] {
  const porEpi = new Map<number, Projecao>()
  for (const projecao of projecoes) {
    const atual = porEpi.get(projecao.epiId)
    const maisNova = !atual || projecao.dataProjecao > atual.dataProjecao
      || (projecao.dataProjecao === atual.dataProjecao && projecao.id > atual.id)
    if (maisNova) porEpi.set(projecao.epiId, projecao)
  }
  return [...porEpi.values()].sort((a, b) => a.dataEstimadaDescarte.localeCompare(b.dataEstimadaDescarte))
}
