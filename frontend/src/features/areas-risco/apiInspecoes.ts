// Contratos e chamadas das inspeções de áreas de risco (RF09, US09, issues #134 e #136): consulta do
// histórico de uma área e registro de inspeção, exclusivos do Gestor de Segurança. Os leitores validam
// a resposta da API antes de entregá-la à tela.

import { http } from '../../shared/api/https'

export type ItemVerificado = {
  id: number
  descricao: string
  conformidade: boolean
  observacoes: string | null
}

/** Formato de `InspecaoResponse`. `dataInspecao` vem como `AAAA-MM-DD`. */
export type Inspecao = {
  id: number
  areaRiscoId: number
  dataInspecao: string
  responsavelNome: string
  naoConformidades: number
  itens: ItemVerificado[]
}

/** Corpo de `POST /api/areas-risco/{id}/inspecoes`, igual a `RegistrarInspecaoRequest`. */
export type DadosRegistroInspecao = {
  dataInspecao: string
  itens: { descricao: string; conformidade: boolean; observacoes?: string }[]
}

function lerItem(dado: unknown): ItemVerificado {
  if (dado === null || typeof dado !== 'object') throw new Error('Item verificado inválido na resposta')
  const b = dado as Record<string, unknown>
  if (typeof b.id !== 'number' || typeof b.descricao !== 'string' || typeof b.conformidade !== 'boolean') {
    throw new Error('Item verificado incompatível com contrato da API')
  }
  return {
    id: b.id,
    descricao: b.descricao,
    conformidade: b.conformidade,
    observacoes: typeof b.observacoes === 'string' ? b.observacoes : null,
  }
}

function lerInspecao(dado: unknown): Inspecao {
  if (dado === null || typeof dado !== 'object') throw new Error('Inspeção inválida na resposta')
  const b = dado as Record<string, unknown>
  if (
    typeof b.id !== 'number' ||
    typeof b.areaRiscoId !== 'number' ||
    typeof b.dataInspecao !== 'string' ||
    typeof b.responsavelNome !== 'string' ||
    typeof b.naoConformidades !== 'number' ||
    !Array.isArray(b.itens)
  ) {
    throw new Error('Inspeção incompatível com contrato da API')
  }
  return {
    id: b.id,
    areaRiscoId: b.areaRiscoId,
    dataInspecao: b.dataInspecao,
    responsavelNome: b.responsavelNome,
    naoConformidades: b.naoConformidades,
    itens: b.itens.map(lerItem),
  }
}

export async function listarInspecoes(areaId: number, signal?: AbortSignal): Promise<Inspecao[]> {
  const { data } = await http.get<unknown>(`/areas-risco/${areaId}/inspecoes`, { signal })
  if (!Array.isArray(data)) throw new Error('Esperava uma lista de inspeções')
  return data.map(lerInspecao)
}

export async function registrarInspecao(areaId: number, dados: DadosRegistroInspecao): Promise<Inspecao> {
  const { data } = await http.post<unknown>(`/areas-risco/${areaId}/inspecoes`, dados)
  return lerInspecao(data)
}

/** `AAAA-MM-DD` para `DD/MM/AAAA`, sem passar por `Date`, que mudaria o dia pelo fuso horário. */
export function formatarDataInspecao(iso: string): string {
  const [ano, mes, dia] = iso.split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso
}
