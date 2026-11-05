// Contrato único do recurso EPI no frontend (padrão Adapter, parte 2, issue #231): o tipo `Epi` (Target)
// e o adaptador `lerEpi`, que converte o `EpiResponse` da API (Adaptee) nesse tipo. Toda tela que lista
// EPIs (catálogo, estoque, manutenção, empréstimos, áreas de risco, tarefas e capacitações) lê o mesmo
// registro do mesmo jeito. O módulo não importa o cliente `http`, então o adaptador é testável sem o Vite.

import { objeto, texto, textoOuNulo } from '../../shared/api/leitura'

export type StatusEpi = 'DISPONIVEL' | 'EM_USO' | 'EM_MANUTENCAO' | 'ESGOTADO' | 'DESCARTADO'

/** Os nove valores de `ClassificacaoEPI` do diagrama de classes. */
export type ClassificacaoEpi =
  | 'PROTECAO_CONTRA_QUEDAS' | 'PROTECAO_DE_MEMBROS_INFERIORES' | 'PROTECAO_DE_MEMBROS_SUPERIORES'
  | 'PROTECAO_DE_TRONCO' | 'PROTECAO_RESPIRATORIA' | 'PROTECAO_AUDITIVA' | 'PROTECAO_DE_OLHOS'
  | 'PROTECAO_DE_FACE' | 'PROTECAO_DE_CABECA'

const statusEpi: readonly StatusEpi[] = ['DISPONIVEL', 'EM_USO', 'EM_MANUTENCAO', 'ESGOTADO', 'DESCARTADO']

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

/** Rótulo de exibição de cada classificação, com acentuação. */
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

/**
 * Campos do contrato `EpiResponse` usados pelas telas (decisão #124: sem código de barras nem
 * fornecedor). Os campos de lote e modelo da resposta não são lidos porque nenhuma tela os exibe.
 */
export type Epi = {
  id: number
  nome: string
  /** Vazio quando a API não envia o CA; as telas mostram "não informado". */
  numeroCa: string
  quantidade: number
  estoqueMinimo: number
  /** Regra do backend (saldo <= mínimo, US03 regra 3). A tela exibe, não recalcula. */
  estoqueCritico: boolean
  status: StatusEpi
  /** `AAAA-MM-DD`, ou `null` quando ausente. */
  dataValidadeCa: string | null
  descricao: string | null
  /** Valor fora dos nove do enum vira `null`, para a tela nunca indexar um rótulo inexistente. */
  classificacao: ClassificacaoEpi | null
  localizacao: string | null
}

export function ehStatusEpi(valor: unknown): valor is StatusEpi {
  return statusEpi.includes(valor as StatusEpi)
}

function ehClassificacaoEpi(valor: unknown): valor is ClassificacaoEpi {
  return classificacoesEpi.includes(valor as ClassificacaoEpi)
}

/**
 * Adaptador único do `EpiResponse`. Lê `GET /api/epis`, a resposta de `POST /api/epis` e
 * `GET /api/emprestimos/epis-disponiveis`, que devolvem o mesmo contrato. Identificador, nome,
 * quantidades e situação são obrigatórios; os demais campos usam as primitivas de `leitura.ts`.
 */
export function lerEpi(dado: unknown): Epi {
  const bruto = objeto(dado, 'Registro de EPI inválido')
  if (typeof bruto.id !== 'number' || typeof bruto.nome !== 'string' || typeof bruto.quantidade !== 'number'
    || typeof bruto.estoqueMinimo !== 'number' || !ehStatusEpi(bruto.status)) {
    throw new Error('Registro de EPI incompatível com o contrato da API')
  }
  return {
    id: bruto.id,
    nome: bruto.nome,
    numeroCa: texto(bruto.numeroCa, ''),
    quantidade: bruto.quantidade,
    estoqueMinimo: bruto.estoqueMinimo,
    estoqueCritico: bruto.estoqueCritico === true,
    status: bruto.status,
    dataValidadeCa: textoOuNulo(bruto.dataValidadeCa),
    descricao: textoOuNulo(bruto.descricao),
    classificacao: ehClassificacaoEpi(bruto.classificacao) ? bruto.classificacao : null,
    localizacao: textoOuNulo(bruto.localizacao),
  }
}
