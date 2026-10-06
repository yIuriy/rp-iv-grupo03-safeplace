import { http } from '../../shared/api/https'

export type StatusEpi = 'DISPONIVEL' | 'EM_USO' | 'EM_MANUTENCAO' | 'ESGOTADO' | 'DESCARTADO'
export type ResultadoManutencao = 'APROVADO' | 'REPROVADO'
export type ClassificacaoEpi =
  | 'PROTECAO_CONTRA_QUEDAS' | 'PROTECAO_DE_MEMBROS_INFERIORES' | 'PROTECAO_DE_MEMBROS_SUPERIORES'
  | 'PROTECAO_DE_TRONCO' | 'PROTECAO_RESPIRATORIA' | 'PROTECAO_AUDITIVA' | 'PROTECAO_DE_OLHOS'
  | 'PROTECAO_DE_FACE' | 'PROTECAO_DE_CABECA'

export const classificacoesEpi: readonly ClassificacaoEpi[] = [
  'PROTECAO_CONTRA_QUEDAS',
  'PROTECAO_DE_MEMBROS_INFERIORES',
  'PROTECAO_DE_MEMBROS_SUPERIORES',
  'PROTECAO_DE_TRONCO',
  'PROTECAO_RESPIRATORIA',
  'PROTECAO_AUDITIVA',
  'PROTECAO_DE_OLHOS',
  'PROTECAO_DE_FACE',
  'PROTECAO_DE_CABECA',
]

export const rotuloClassificacaoEpi: Record<ClassificacaoEpi, string> = {
  PROTECAO_CONTRA_QUEDAS: 'Proteção contra quedas',
  PROTECAO_DE_MEMBROS_INFERIORES: 'Proteção de membros inferiores',
  PROTECAO_DE_MEMBROS_SUPERIORES: 'Proteção de membros superiores',
  PROTECAO_DE_TRONCO: 'Proteção de tronco',
  PROTECAO_RESPIRATORIA: 'Proteção respiratória',
  PROTECAO_AUDITIVA: 'Proteção auditiva',
  PROTECAO_DE_OLHOS: 'Proteção de olhos',
  PROTECAO_DE_FACE: 'Proteção de face',
  PROTECAO_DE_CABECA: 'Proteção de cabeça',
}

export type Epi = {
  id: number
  nome: string
  numeroCa: string
  quantidade: number
  estoqueMinimo: number
  status: StatusEpi
  dataValidadeCa: string | null
  classificacao?: ClassificacaoEpi | null
}

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

const status: readonly StatusEpi[] = ['DISPONIVEL', 'EM_USO', 'EM_MANUTENCAO', 'ESGOTADO', 'DESCARTADO']
const resultados: readonly ResultadoManutencao[] = ['APROVADO', 'REPROVADO']

function ehStatus(valor: unknown): valor is StatusEpi { return status.includes(valor as StatusEpi) }
function ehResultado(valor: unknown): valor is ResultadoManutencao { return resultados.includes(valor as ResultadoManutencao) }
function ehClassificacao(valor: unknown): valor is ClassificacaoEpi {
  return typeof valor === 'string' && classificacoesEpi.includes(valor as ClassificacaoEpi)
}

function lerEpi(dado: unknown): Epi {
  if (dado === null || typeof dado !== 'object') throw new Error('EPI inválido na resposta da API')
  const bruto = dado as Record<string, unknown>
  if (typeof bruto.id !== 'number' || typeof bruto.nome !== 'string' || typeof bruto.numeroCa !== 'string'
    || typeof bruto.quantidade !== 'number' || typeof bruto.estoqueMinimo !== 'number' || !ehStatus(bruto.status)) {
    throw new Error('EPI incompatível com contrato da API')
  }
  return {
    id: bruto.id, nome: bruto.nome, numeroCa: bruto.numeroCa, quantidade: bruto.quantidade,
    estoqueMinimo: bruto.estoqueMinimo, status: bruto.status,
    dataValidadeCa: typeof bruto.dataValidadeCa === 'string' ? bruto.dataValidadeCa : null,
    classificacao: ehClassificacao(bruto.classificacao) ? bruto.classificacao : null,
  }
}

function lerManutencao(dado: unknown): ManutencaoEpi {
  if (dado === null || typeof dado !== 'object') throw new Error('Manutenção inválida na resposta da API')
  const bruto = dado as Record<string, unknown>
  if (typeof bruto.id !== 'number' || typeof bruto.epiId !== 'number' || typeof bruto.dataManutencao !== 'string'
    || typeof bruto.descricao !== 'string' || !ehResultado(bruto.resultado) || !ehStatus(bruto.statusAtualEpi)) {
    throw new Error('Manutenção incompatível com contrato da API')
  }
  return {
    id: bruto.id, epiId: bruto.epiId, dataManutencao: bruto.dataManutencao, descricao: bruto.descricao,
    resultado: bruto.resultado, statusAtualEpi: bruto.statusAtualEpi,
    responsavelManutencao: typeof bruto.responsavelManutencao === 'string' ? bruto.responsavelManutencao : null,
  }
}

export async function listarEpis(signal?: AbortSignal): Promise<Epi[]> {
  const { data } = await http.get<unknown>('/epis', { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de EPIs')
  return data.map(lerEpi)
}

export async function listarHistorico(epiId: number, signal?: AbortSignal): Promise<ManutencaoEpi[]> {
  const { data } = await http.get<unknown>(`/epis/${epiId}/manutencoes`, { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de manutenções')
  return data.map(lerManutencao)
}

export async function registrarManutencao(epiId: number, dados: DadosManutencao): Promise<ManutencaoEpi> {
  const { data } = await http.post<unknown>(`/epis/${epiId}/manutencoes`, dados)
  return lerManutencao(data)
}

export async function cadastrarEpi(dados: DadosCadastroEpi): Promise<Epi> {
  const { data } = await http.post<unknown>('/epis', dados)
  return lerEpi(data)
}
