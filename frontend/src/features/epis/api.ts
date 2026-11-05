// Cliente tipado da API de EPIs (/api/epis): catálogo, cadastro e manutenção (RF03, RF04, UC01).
// O tipo `Epi` e o adaptador `lerEpi` ficam em `epi.ts`, únicos para todas as telas (issue #231).

import { http } from '../../shared/api/https'
import { lista, objeto } from '../../shared/api/leitura'
import { ehStatusEpi, lerEpi, type ClassificacaoEpi, type Epi, type StatusEpi } from './epi'

export type ResultadoManutencao = 'APROVADO' | 'REPROVADO'

export type ManutencaoEpi = {
  id: number
  epiId: number
  dataManutencao: string
  descricao: string
  resultado: ResultadoManutencao
  responsavelManutencao: string | null
  statusAtualEpi: StatusEpi
}

export type DadosManutencao = {
  dataManutencao: string
  descricao: string
  resultado: ResultadoManutencao
}

export type DadosCadastroEpi = {
  nome: string
  numeroCa: string
  quantidade: number
  estoqueMinimo: number
  /** Obrigatória: o CA e sua validade ficam no modelo do EPI (decisão #124). */
  dataValidadeCa: string
  vidaUtilDias?: number
  descricao?: string
  classificacao?: ClassificacaoEpi
  localizacao?: string
  numeroLote?: string
  notaFiscal?: string
  dataFabricacao?: string
  validadeLote?: string
  marca?: string
}

const resultados: readonly ResultadoManutencao[] = ['APROVADO', 'REPROVADO']

function ehResultado(valor: unknown): valor is ResultadoManutencao { return resultados.includes(valor as ResultadoManutencao) }

function lerManutencao(dado: unknown): ManutencaoEpi {
  const bruto = objeto(dado, 'Manutenção inválida na resposta da API')
  if (typeof bruto.id !== 'number' || typeof bruto.epiId !== 'number' || typeof bruto.dataManutencao !== 'string'
    || typeof bruto.descricao !== 'string' || !ehResultado(bruto.resultado) || !ehStatusEpi(bruto.statusAtualEpi)) {
    throw new Error('Manutenção incompatível com contrato da API')
  }
  return {
    id: bruto.id, epiId: bruto.epiId, dataManutencao: bruto.dataManutencao, descricao: bruto.descricao,
    resultado: bruto.resultado, statusAtualEpi: bruto.statusAtualEpi,
    responsavelManutencao: typeof bruto.responsavelManutencao === 'string' ? bruto.responsavelManutencao : null,
  }
}

/** `GET /api/epis`: catálogo com saldo e situação crítica, usado por todas as telas que listam EPIs. */
export async function listarEpis(signal?: AbortSignal): Promise<Epi[]> {
  const { data } = await http.get<unknown>('/epis', { signal })
  return lista(data, lerEpi, 'Esperava uma lista de EPIs')
}

export async function listarHistorico(epiId: number, signal?: AbortSignal): Promise<ManutencaoEpi[]> {
  const { data } = await http.get<unknown>(`/epis/${epiId}/manutencoes`, { signal })
  return lista(data, lerManutencao, 'Esperava uma lista de manutenções')
}

export async function registrarManutencao(epiId: number, dados: DadosManutencao): Promise<ManutencaoEpi> {
  const { data } = await http.post<unknown>(`/epis/${epiId}/manutencoes`, dados)
  return lerManutencao(data)
}

export async function cadastrarEpi(dados: DadosCadastroEpi): Promise<Epi> {
  const { data } = await http.post<unknown>('/epis', dados)
  return lerEpi(data)
}
