// Contratos e chamadas dos relatórios estatísticos (RF18, issue #299): pré-visualização em JSON e
// download do arquivo em CSV ou PDF. Os leitores conferem a resposta com as primitivas do Adapter
// (#244) antes de entregá-la à tela.

import { isAxiosError } from 'axios'
import { http } from '../../shared/api/https'
import { lista, listaOuVazia, objeto, textoOuNulo } from '../../shared/api/leitura'
import type { Periodo } from './periodo'

export type TipoRelatorio = 'acidentes' | 'treinamentos' | 'areas-risco'
export type FormatoRelatorio = 'CSV' | 'PDF'

export const tiposRelatorio: { valor: TipoRelatorio; rotulo: string; descricao: string }[] = [
  { valor: 'acidentes', rotulo: 'Acidentes e incidentes',
    descricao: 'Totais do período, ocorrências por mês, acidentes por gravidade e por área de risco.' },
  { valor: 'treinamentos', rotulo: 'Treinamentos e certificações',
    descricao: 'Treinamentos realizados no período e certificações vencidas ou a vencer no último dia.' },
  { valor: 'areas-risco', rotulo: 'Áreas de maior risco',
    descricao: 'Ranking das áreas pelas ocorrências do período, com nível de perigo e inspeções.' },
]

export type Indicador = { rotulo: string; valor: string }
export type Tabela = { colunas: string[]; linhas: string[][]; semDados: string }
export type Secao = { titulo: string; indicadores: Indicador[]; tabela: Tabela | null }

/** Formato de `RelatorioResponse`. Datas em `AAAA-MM-DD`; `geradoEm` em `AAAA-MM-DDTHH:mm:ss`. */
export type Relatorio = {
  tipo: string
  titulo: string
  inicio: string
  fim: string
  geradoEm: string
  /** Nenhum registro do assunto no período: a tela avisa em vez de mostrar só zeros. */
  semRegistros: boolean
  secoes: Secao[]
}

const INVALIDO = 'Relatório incompatível com o contrato da API'

function textoObrigatorio(valor: unknown): string {
  const texto = textoOuNulo(valor)
  if (texto === null) throw new Error(INVALIDO)
  return texto
}

function lerTabela(dado: unknown): Tabela | null {
  if (dado === null || dado === undefined) return null
  const b = objeto(dado, INVALIDO)
  return {
    colunas: lista(b.colunas, textoObrigatorio, INVALIDO),
    linhas: lista(b.linhas, linha => lista(linha, celula => (typeof celula === 'string' ? celula : ''), INVALIDO), INVALIDO),
    semDados: textoOuNulo(b.semDados) ?? 'Sem dados no período.',
  }
}

function lerSecao(dado: unknown): Secao {
  const b = objeto(dado, INVALIDO)
  return {
    titulo: textoObrigatorio(b.titulo),
    indicadores: listaOuVazia(b.indicadores, item => {
      const i = objeto(item, INVALIDO)
      return { rotulo: textoObrigatorio(i.rotulo), valor: textoOuNulo(i.valor) ?? '-' }
    }),
    tabela: lerTabela(b.tabela),
  }
}

export function lerRelatorio(dado: unknown): Relatorio {
  const b = objeto(dado, INVALIDO)
  return {
    tipo: textoObrigatorio(b.tipo),
    titulo: textoObrigatorio(b.titulo),
    inicio: textoObrigatorio(b.inicio),
    fim: textoObrigatorio(b.fim),
    geradoEm: textoObrigatorio(b.geradoEm),
    semRegistros: b.semRegistros === true,
    secoes: lista(b.secoes, lerSecao, INVALIDO),
  }
}

export async function gerarRelatorio(tipo: TipoRelatorio, periodo: Periodo, signal?: AbortSignal): Promise<Relatorio> {
  const resposta = await http.get(`/relatorios/${tipo}`, { params: periodo, signal })
  return lerRelatorio(resposta.data)
}

/**
 * Baixa o arquivo e dispara o download no navegador. Em erro, a API responde JSON mesmo com
 * `responseType: 'blob'`; o corpo é convertido de volta para que `mensagemDaApi` leia o `message`.
 */
export async function baixarRelatorio(tipo: TipoRelatorio, periodo: Periodo, formato: FormatoRelatorio): Promise<string> {
  try {
    const resposta = await http.get<Blob>(`/relatorios/${tipo}/arquivo`, {
      params: { ...periodo, formato }, responseType: 'blob', timeout: 30000,
    })
    const nome = nomeDoArquivo(resposta.headers['content-disposition'])
      ?? `relatorio-${tipo}-${periodo.inicio}-a-${periodo.fim}.${formato.toLowerCase()}`
    salvar(resposta.data, nome)
    return nome
  } catch (erro) {
    if (isAxiosError(erro) && erro.response?.data instanceof Blob) {
      try {
        erro.response.data = JSON.parse(await erro.response.data.text())
      } catch {
        // Corpo que não é JSON: fica a mensagem alternativa da tela.
      }
    }
    throw erro
  }
}

function nomeDoArquivo(cabecalho: unknown): string | null {
  if (typeof cabecalho !== 'string') return null
  return /filename="?([^";]+)"?/.exec(cabecalho)?.[1] ?? null
}

function salvar(conteudo: Blob, nome: string) {
  const url = URL.createObjectURL(conteudo)
  const link = document.createElement('a')
  link.href = url
  link.download = nome
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
