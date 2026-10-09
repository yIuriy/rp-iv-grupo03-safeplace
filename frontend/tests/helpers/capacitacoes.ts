// Apoio aos testes da tela de capacitações (issue #120): dados no contrato de /api/capacitacoes
// (#119, #192) e simulação da API, incluindo /api/tarefas e /api/epis para aptidão, alocação e exigências.

import type { Page } from '@playwright/test'

// Contrato de /api/capacitacoes (#119). Ana tem certificação vencida, Bruno uma próxima do vencimento,
// Carla só treinamento (sem certificação) e Davi nenhum histórico.
export const resumos = [
  { colaboradorId: 4, colaboradorNome: 'Ana Souza', situacaoCertificacoes: 'VENCIDA', semCertificacoes: false, totalCertificacoes: 2, totalTreinamentos: 1, pendenteIntegracaoAdmissional: false },
  { colaboradorId: 5, colaboradorNome: 'Bruno Lima', situacaoCertificacoes: 'PROXIMA_DO_VENCIMENTO', semCertificacoes: false, totalCertificacoes: 1, totalTreinamentos: 0, pendenteIntegracaoAdmissional: false },
  { colaboradorId: 6, colaboradorNome: 'Carla Dias', situacaoCertificacoes: null, semCertificacoes: true, totalCertificacoes: 0, totalTreinamentos: 1, pendenteIntegracaoAdmissional: true },
  { colaboradorId: 7, colaboradorNome: 'Davi Rocha', situacaoCertificacoes: null, semCertificacoes: true, totalCertificacoes: 0, totalTreinamentos: 0, pendenteIntegracaoAdmissional: true },
]

const nr35Vencida = { id: 10, nome: 'NR-35 Trabalho em Altura', status: 'VENCIDA', dataVencimento: '2026-10-04', dataEmissao: '2024-10-04' }
const nr33EmDia = { id: 11, nome: 'NR-33 Espaço Confinado', status: 'EM_DIA', dataVencimento: '2027-06-30', dataEmissao: '2025-06-30' }
const nr10Proxima = { id: 12, nome: 'NR-10 Eletricidade', status: 'PROXIMA_DO_VENCIMENTO', dataVencimento: '2026-11-03', dataEmissao: '2025-11-03' }
const integracao = { id: 20, nome: 'Integração de Segurança', conteudo: 'Normas internas e rotas de fuga', instrutor: 'Eng. Paula', dataRealizacao: '2026-09-20', cargaHoraria: 8, validadeMeses: null, dataVencimento: null, status: 'REALIZADO' }
const primeirosSocorrosVencido = { id: 22, nome: 'Primeiros Socorros', conteudo: null, instrutor: null, dataRealizacao: '2025-09-01', cargaHoraria: 4, validadeMeses: 12, dataVencimento: '2026-09-01', status: 'VENCIDO' }

const pendenciaIntegracao = ['Pendência da trilha inicial de integração', 'Pendência de treinamentos admissionais']

export const detalhes: Record<number, unknown> = {
  4: {
    colaboradorId: 4, colaboradorNome: 'Ana Souza', situacaoCertificacoes: 'VENCIDA',
    certificacoes: [nr35Vencida, nr33EmDia], treinamentos: [integracao, primeirosSocorrosVencido],
    certificacoesVencidas: [nr35Vencida], certificacoesProximasDoVencimento: [], pendenteIntegracaoAdmissional: false,
    pendencias: ['Colaborador possui 1 certificação(ões) vencida(s) necessitando de renovação imediata'],
  },
  5: {
    colaboradorId: 5, colaboradorNome: 'Bruno Lima', situacaoCertificacoes: 'PROXIMA_DO_VENCIMENTO',
    certificacoes: [nr10Proxima], treinamentos: [], certificacoesVencidas: [], certificacoesProximasDoVencimento: [nr10Proxima],
    pendenteIntegracaoAdmissional: false, pendencias: ['Colaborador possui 1 certificação(ões) próxima(s) do vencimento (30 dias)'],
  },
  6: {
    colaboradorId: 6, colaboradorNome: 'Carla Dias', situacaoCertificacoes: null, certificacoes: [],
    treinamentos: [{ ...integracao, id: 21 }], certificacoesVencidas: [], certificacoesProximasDoVencimento: [],
    pendenteIntegracaoAdmissional: true, pendencias: pendenciaIntegracao,
  },
  7: {
    colaboradorId: 7, colaboradorNome: 'Davi Rocha', situacaoCertificacoes: null, certificacoes: [], treinamentos: [],
    certificacoesVencidas: [], certificacoesProximasDoVencimento: [], pendenteIntegracaoAdmissional: true, pendencias: pendenciaIntegracao,
  },
}

export const alertas = {
  dataReferencia: '2026-10-04', diasAntecedenciaAlerta: 30,
  proximasDoVencimento: [{ certificacaoId: 12, nome: 'NR-10 Eletricidade', status: 'PROXIMA_DO_VENCIMENTO', dataVencimento: '2026-11-03', dataEmissao: '2025-11-03', colaboradorId: 5, colaboradorNome: 'Bruno Lima' }],
  vencidas: [{ certificacaoId: 10, nome: 'NR-35 Trabalho em Altura', status: 'VENCIDA', dataVencimento: '2026-10-04', dataEmissao: '2024-10-04', colaboradorId: 4, colaboradorNome: 'Ana Souza' }],
  pendencias: [
    { colaboradorId: 6, colaboradorNome: 'Carla Dias', mensagens: pendenciaIntegracao },
    { colaboradorId: 7, colaboradorNome: 'Davi Rocha', mensagens: pendenciaIntegracao },
  ],
  treinamentosProximosDoVencimento: [],
  treinamentosVencidos: [],
}

export const epis = [{ id: 40, nome: 'Cinto de segurança', numeroCa: '12345', quantidade: 10, estoqueMinimo: 2, status: 'DISPONIVEL', dataValidadeCa: '2027-01-01' }]

export const alertasTreinamento = {
  treinamentosVencidos: [{ treinamentoId: 22, nome: 'Primeiros Socorros', status: 'VENCIDO', dataRealizacao: '2025-09-01', dataVencimento: '2026-09-01', colaboradorId: 4, colaboradorNome: 'Ana Souza' }],
  treinamentosProximosDoVencimento: [{ treinamentoId: 23, nome: 'Brigada de Incêndio', status: 'PROXIMO_DO_VENCIMENTO', dataRealizacao: '2025-10-20', dataVencimento: '2026-10-20', colaboradorId: 5, colaboradorNome: 'Bruno Lima' }],
}

export const tarefas = [
  { id: 30, descricao: 'Montagem de andaime', nivelPerigo: 'ALTO', classificada: true, dataClassificacao: '2026-09-20T10:00:00', responsavelClassificacaoNome: 'Gestora Ana' },
  { id: 31, descricao: 'Inventário do almoxarifado', nivelPerigo: 'BAIXO', classificada: true, dataClassificacao: '2026-09-20T10:00:00', responsavelClassificacaoNome: 'Gestora Ana' },
  { id: 32, descricao: 'Limpeza de silo', nivelPerigo: null, classificada: false, dataClassificacao: null, responsavelClassificacaoNome: null },
]

export type Requisicao = { metodo: string; caminho: string; busca: URLSearchParams; corpo: unknown }
/** `alocacao` define a resposta de POST /api/tarefas/{id}/alocacoes; por padrão, 201. */
export type Opcoes = {
  falhaLista?: number; falhaDetalhe?: number; falhaAptidao?: number; semAlertas?: boolean; comAlertasTreinamento?: boolean
  alocacao?: 'bloqueada' | 'nao-alocavel'; falhaCadastro?: number
}

export async function simularApi(page: Page, opcoes: Opcoes = {}) {
  const requisicoes: Requisicao[] = []
  await page.route(url => url.pathname.startsWith('/api/capacitacoes') || url.pathname.startsWith('/api/tarefas') || url.pathname === '/api/epis', route => {
    const request = route.request()
    const url = new URL(request.url())
    requisicoes.push({ metodo: request.method(), caminho: url.pathname, busca: url.searchParams, corpo: request.postDataJSON() })
    if (request.method() === 'POST') {
      if (url.pathname.endsWith('/alocacoes')) {
        if (opcoes.alocacao === 'bloqueada') {
          return route.fulfill({ status: 422, json: { status: 422, error: 'Alocação Bloqueada', message: 'Alocação bloqueada por pendências normativas de capacitação.',
            impedimentos: ["certificação obrigatória 'NR-35 Trabalho em Altura' ausente ou vencida"] } })
        }
        if (opcoes.alocacao === 'nao-alocavel') {
          return route.fulfill({ status: 422, json: { status: 422, error: 'Colaborador Não Alocável', message: 'Somente colaboradores podem ser alocados em tarefas.' } })
        }
        return route.fulfill({ status: 201, json: { id: 1 } })
      }
      if (opcoes.falhaCadastro) return route.fulfill({ status: opcoes.falhaCadastro, json: { message: 'Data de vencimento não pode ser no passado ou presente no momento do cadastro.' } })
      const corpo = request.postDataJSON() as Record<string, unknown>
      if (url.pathname === '/api/capacitacoes/certificacoes') {
        return route.fulfill({ status: 201, json: { id: 50, status: 'EM_DIA', ...corpo } })
      }
      if (url.pathname === '/api/capacitacoes/treinamentos') {
        return route.fulfill({ status: 201, json: { id: 51, conteudo: null, instrutor: null, cargaHoraria: null, validadeMeses: null, dataVencimento: null, status: 'REALIZADO', ...corpo } })
      }
      if (url.pathname === '/api/capacitacoes/exigencias') return route.fulfill({ status: 201, json: { id: 52, ...corpo } })
      return route.fulfill({ status: 403, json: { message: 'Acesso proibido' } })
    }

    if (url.pathname === '/api/epis') return route.fulfill({ json: epis })
    if (url.pathname === '/api/tarefas') return route.fulfill({ json: tarefas })
    if (url.pathname === '/api/capacitacoes/alertas') {
      if (opcoes.semAlertas) return route.fulfill({ json: { ...alertas, vencidas: [], proximasDoVencimento: [], pendencias: [] } })
      return route.fulfill({ json: opcoes.comAlertasTreinamento ? { ...alertas, ...alertasTreinamento } : alertas })
    }
    if (url.pathname === '/api/capacitacoes/colaboradores') {
      if (opcoes.falhaLista) return route.fulfill({ status: opcoes.falhaLista, json: { message: 'Falha' } })
      const situacao = url.searchParams.get('situacao')
      return route.fulfill({ json: situacao ? resumos.filter(r => r.situacaoCertificacoes === situacao) : resumos })
    }
    const aptidao = url.pathname.match(/^\/api\/capacitacoes\/colaboradores\/(\d+)\/aptidao$/)
    if (aptidao) {
      if (opcoes.falhaAptidao) return route.fulfill({ status: opcoes.falhaAptidao, json: { message: 'Tarefa não encontrada: 30' } })
      const colaboradorId = Number(aptidao[1])
      const tarefa = tarefas.find(t => t.id === Number(url.searchParams.get('tarefaId')))!
      const nome = resumos.find(r => r.colaboradorId === colaboradorId)!.colaboradorNome
      const impedimentos = tarefa.id === 32
        ? ["tarefa 'Limpeza de silo' sem grau de periculosidade classificado"]
        : colaboradorId === 4 && tarefa.id === 30
          ? ["certificação obrigatória 'NR-35 Trabalho em Altura' ausente ou vencida", "treinamento obrigatório 'Resgate em Altura' pendente ou não realizado"]
          : []
      return route.fulfill({ json: { apto: impedimentos.length === 0, tarefaId: tarefa.id, tarefaDescricao: tarefa.descricao, colaboradorId, colaboradorNome: nome, impedimentos } })
    }
    const detalhe = url.pathname.match(/^\/api\/capacitacoes\/colaboradores\/(\d+)$/)
    if (detalhe) {
      if (opcoes.falhaDetalhe) return route.fulfill({ status: opcoes.falhaDetalhe, json: { message: 'Colaborador não encontrado: 99' } })
      const dados = detalhes[Number(detalhe[1])]
      return dados ? route.fulfill({ json: dados }) : route.fulfill({ status: 404, json: { message: `Colaborador não encontrado: ${detalhe[1]}` } })
    }
    return route.fulfill({ status: 404, json: {} })
  })
  return requisicoes
}
