// Testes Playwright da tela de empréstimos (/emprestimos, issue #110): posse com prazo e atraso em
// texto, filtro por situação na API, entrega com termo de cautela, duplicidade com justificativa,
// impedimento informado pelo servidor, validação local, devolução com destino confirmado, detalhes
// com responsáveis, projeções e lista de reposição do Gestor, análise concluída com as listas
// recarregadas, pendências e falha da análise com nova tentativa, recarga da página, teclado e
// larguras de 360, 768 e 1344 px. Somente as respostas HTTP são simuladas.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, pessoas, supervisor } from './helpers/sessao'

type EmprestimoTeste = {
  id: number
  epiId: number
  epiNome: string
  colaboradorId: number
  colaboradorNome: string
  responsavelEntregaId: number
  dataRetirada: string
  dataDevolucaoPrevista: string
  dataDevolucaoReal: string | null
  responsavelDevolucaoId: number | null
  estadoDevolucao: string | null
  status: string
  atrasado: boolean
  termoCautela: string
  justificativaSubstituicao: string | null
}

type Resposta = { status: number; json: unknown }
type Chamada = { metodo: string; caminho: string; situacao: string | null; corpo: Record<string, unknown> | null }

const capacete = {
  id: 1, nome: 'Capacete classe B', numeroCa: '12345', quantidade: 10, estoqueMinimo: 5, estoqueCritico: false,
  status: 'DISPONIVEL', dataValidadeCa: '2027-12-31', vidaUtilDias: 365, descricao: null,
  classificacao: 'PROTECAO_DE_CABECA', localizacao: 'Almoxarifado A',
}
const luva = {
  id: 2, nome: 'Luva nitrílica', numeroCa: '777', quantidade: 5, estoqueMinimo: 5, estoqueCritico: true,
  status: 'DISPONIVEL', dataValidadeCa: '2027-06-30', vidaUtilDias: 180, descricao: null,
  classificacao: 'PROTECAO_DE_MEMBROS_SUPERIORES', localizacao: null,
}
const joao = pessoas.colaborador
const maria = { id: 4, nome: 'Maria Souza', cpf: '98765432100', dataNascimento: '1990-03-02', email: null, perfil: 'COLABORADOR', ativo: true }
const pedro = { id: 5, nome: 'Pedro Inativo', cpf: '11122233396', dataNascimento: '1985-07-07', email: null, perfil: 'COLABORADOR', ativo: false }

function termo(pessoa: { nome: string; cpf: string }, epi: { id: number; nome: string; numeroCa: string }, prazo: string) {
  return `Termo de cautela: o colaborador ${pessoa.nome} (CPF ${pessoa.cpf}) recebeu 1 unidade do EPI ${epi.nome} `
    + `(código ${epi.id}, CA ${epi.numeroCa}) em 2026-09-20T09:15, entregue por Gestora Ana, com devolução prevista para ${prazo}.`
}

function emprestimo(parcial: Partial<EmprestimoTeste> = {}): EmprestimoTeste {
  return {
    id: 10, epiId: 2, epiNome: 'Luva nitrílica', colaboradorId: 3, colaboradorNome: 'João da Silva',
    responsavelEntregaId: 1, dataRetirada: '2026-09-20T09:15:00', dataDevolucaoPrevista: '2026-09-25',
    dataDevolucaoReal: null, responsavelDevolucaoId: null, estadoDevolucao: null, status: 'DEVOLUCAO_ATRASADA',
    atrasado: true, termoCautela: termo(joao, luva, '2026-09-25'), justificativaSubstituicao: null, ...parcial,
  }
}

const luvaAtrasada = emprestimo()
const capaceteEmPosse = emprestimo({
  id: 11, epiId: 1, epiNome: 'Capacete classe B', colaboradorId: 4, colaboradorNome: 'Maria Souza',
  dataRetirada: '2026-09-27T08:00:00', dataDevolucaoPrevista: '2026-10-27', status: 'INDISPONIVEL', atrasado: false,
  termoCautela: termo(maria, capacete, '2026-10-27'), justificativaSubstituicao: 'Capacete anterior com a aba trincada.',
})
const capaceteDevolvido = emprestimo({
  id: 9, epiId: 1, epiNome: 'Capacete classe B', dataRetirada: '2026-09-01T10:00:00', dataDevolucaoPrevista: '2026-09-10',
  dataDevolucaoReal: '2026-09-09T16:30:00', responsavelDevolucaoId: 2, estadoDevolucao: 'MANUTENCAO', status: 'DISPONIVEL',
  atrasado: false, termoCautela: termo(joao, capacete, '2026-09-10'),
})

const projecoes = [
  { id: 1, epiId: 1, nomeEpi: 'Capacete classe B', dataProjecao: '2026-09-20', dataEstimadaDescarte: '2026-12-01',
    justificativa: 'Substituição por desgaste acelerado em ambiente MEDIO (fator 1.25), taxa de uso diário de 30%.',
    validadeCa: '2027-12-31', quantidadeDisponivel: 10, estoqueMinimo: 5 },
  { id: 2, epiId: 1, nomeEpi: 'Capacete classe B', dataProjecao: '2026-09-27', dataEstimadaDescarte: '2026-11-15',
    justificativa: 'Substituição por desgaste acelerado em ambiente ALTO (fator 1.5), taxa de uso diário de 40%.',
    validadeCa: '2027-12-31', quantidadeDisponivel: 10, estoqueMinimo: 5 },
  { id: 3, epiId: 2, nomeEpi: 'Luva nitrílica', dataProjecao: '2026-09-27', dataEstimadaDescarte: '2027-06-30',
    justificativa: 'Substituição antecipada pelo vencimento legal do CA em 2027-06-30, antes do término da vida útil física projetada (2027-08-10).',
    validadeCa: '2027-06-30', quantidadeDisponivel: 5, estoqueMinimo: 5 },
]
/** Como no backend, a análise devolve as projeções montadas antes de salvar, ainda sem `id`. */
const projecaoDaAnalise = { ...projecoes[1], id: null }
const alertas = [
  { epiId: 1, nomeEpi: 'Capacete classe B', tipoAlerta: 'DESCARTE_PROXIMO', dataLimiteEstimada: '2026-10-20', diasRestantes: 22,
    mensagem: 'Substituição preventiva recomendada por aproximação da data estimada de descarte', prioridade: 'ALTA' },
  { epiId: 2, nomeEpi: 'Luva nitrílica', tipoAlerta: 'ESTOQUE_CRITICO', dataLimiteEstimada: null, diasRestantes: null,
    mensagem: 'Saldo atual igual ou abaixo do estoque mínimo de segurança', prioridade: 'MEDIA' },
]

const mensagemDuplicidade = 'O colaborador já possui um EPI do tipo PROTECAO_DE_CABECA em posse ativa (empréstimo 11). '
  + 'Confirme a substituição e informe a justificativa.'

function combina(situacao: string | null, item: EmprestimoTeste): boolean {
  if (situacao === 'TODOS') return true
  if (situacao === 'ATRASADOS') return item.atrasado && item.status !== 'DISPONIVEL'
  return item.status !== 'DISPONIVEL'
}

/**
 * Servidor simulado em memória: empréstimos, EPIs disponíveis, colaboradores, nomes de usuários e
 * projeções. `entrega` e `devolucao` podem trocar a resposta de escrita; `analises` é consumida em ordem.
 */
async function simularApi(page: Page, opcoes: {
  entrega?: (corpo: Record<string, unknown>) => Resposta | undefined
  devolucao?: Resposta
  analises?: Resposta[]
} = {}) {
  const chamadas: Chamada[] = []
  const analises = [...(opcoes.analises ?? [])]
  let emprestimos = [luvaAtrasada, capaceteEmPosse, capaceteDevolvido]
  let proximoId = 20

  function entregar(corpo: Record<string, unknown>): Resposta {
    const substituta = opcoes.entrega?.(corpo)
    if (substituta) return substituta
    const epi = [capacete, luva].find(item => item.id === corpo.epiId) ?? capacete
    const pessoa = [joao, maria].find(item => item.id === corpo.colaboradorId) ?? joao
    const prazo = String(corpo.dataDevolucaoPrevista)
    const novo = emprestimo({
      id: proximoId++, epiId: epi.id, epiNome: epi.nome, colaboradorId: pessoa.id, colaboradorNome: pessoa.nome,
      dataRetirada: '2026-09-28T10:00:00', dataDevolucaoPrevista: prazo, status: 'INDISPONIVEL', atrasado: false,
      termoCautela: termo(pessoa, epi, prazo),
      justificativaSubstituicao: typeof corpo.justificativaSubstituicao === 'string' ? corpo.justificativaSubstituicao : null,
    })
    emprestimos = [novo, ...emprestimos]
    return { status: 201, json: novo }
  }

  function devolver(id: number, corpo: Record<string, unknown>): Resposta {
    if (opcoes.devolucao) return opcoes.devolucao
    const atual = emprestimos.find(item => item.id === id) ?? luvaAtrasada
    const devolvido = { ...atual, status: 'DISPONIVEL', atrasado: false, dataDevolucaoReal: '2026-09-28T11:00:00',
      responsavelDevolucaoId: 1, estadoDevolucao: String(corpo.estado) }
    emprestimos = emprestimos.map(item => item.id === id ? devolvido : item)
    return { status: 200, json: devolvido }
  }

  function responder(metodo: string, url: URL, corpo: Record<string, unknown> | null): Resposta {
    const rota = `${metodo} ${url.pathname}`
    const devolucao = url.pathname.match(/^\/api\/emprestimos\/(\d+)\/devolucao$/)
    if (rota === 'GET /api/emprestimos') {
      return { status: 200, json: emprestimos.filter(item => combina(url.searchParams.get('situacao'), item)) }
    }
    if (rota === 'GET /api/emprestimos/epis-disponiveis') return { status: 200, json: [capacete, luva] }
    if (rota === 'GET /api/usuarios/colaboradores') return { status: 200, json: [joao, maria, pedro] }
    if (rota === 'GET /api/usuarios/1') return { status: 200, json: pessoas.gestora }
    if (rota === 'GET /api/usuarios/2') return { status: 200, json: pessoas.supervisora }
    if (rota === 'POST /api/emprestimos') return entregar(corpo ?? {})
    if (metodo === 'POST' && devolucao) return devolver(Number(devolucao[1]), corpo ?? {})
    if (rota === 'GET /api/projecoes') return { status: 200, json: projecoes }
    if (rota === 'GET /api/projecoes/alertas') return { status: 200, json: alertas }
    if (rota === 'POST /api/projecoes/analise') return analises.shift() ?? { status: 500, json: { status: 500 } }
    return { status: 404, json: { message: `Rota não simulada: ${rota}` } }
  }

  await page.route(url => url.pathname.startsWith('/api/'), route => {
    const request = route.request()
    const url = new URL(request.url())
    const corpo = request.postData() ? request.postDataJSON() as Record<string, unknown> : null
    chamadas.push({
      metodo: request.method(), caminho: url.pathname, corpo,
      situacao: url.pathname === '/api/emprestimos' ? url.searchParams.get('situacao') : null,
    })
    return route.fulfill(responder(request.method(), url, corpo))
  })
  return chamadas
}

const escritas = (chamadas: Chamada[]) => chamadas.filter(chamada => chamada.metodo === 'POST')

async function abrirNovoEmprestimo(page: Page) {
  await page.getByRole('button', { name: 'Novo empréstimo' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Novo empréstimo' })
  await expect(dialogo.getByLabel('Colaborador', { exact: true })).toBeVisible()
  return dialogo
}

test('lists loans in custody with collaborator, deadline and a textual overdue status', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularApi(page)
  await page.goto('/emprestimos')

  await expect(page).toHaveTitle('Empréstimos de EPIs | SafePlace')
  const tabela = page.getByRole('table', { name: 'Empréstimos de EPIs' })
  await expect(tabela.getByRole('row')).toHaveCount(3)
  const atrasada = tabela.getByRole('row', { name: /Luva nitrílica/ })
  await expect(atrasada.getByText('Atrasado', { exact: true })).toBeVisible()
  await expect(atrasada.getByRole('cell', { name: 'João da Silva', exact: true })).toBeVisible()
  await expect(atrasada.getByRole('cell', { name: '20/09/2026 09:15', exact: true })).toBeVisible()
  await expect(atrasada.getByRole('cell', { name: '25/09/2026', exact: true })).toBeVisible()
  const emPosse = tabela.getByRole('row', { name: /Capacete classe B/ })
  await expect(emPosse.getByText('Em posse', { exact: true })).toBeVisible()
  await expect(page.getByRole('status').filter({ hasText: '1 empréstimo com devolução atrasada.' })).toBeVisible()
  expect(chamadas.some(chamada => chamada.situacao === 'PENDENTES')).toBe(true)
  await expect(page.getByRole('tab')).toHaveCount(0)
  await expect(page.getByText('Projeções de substituição')).toHaveCount(0)
})

test('situation filter asks the API for overdue or all loans and shows the return condition', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularApi(page)
  await page.goto('/emprestimos')
  const tabela = page.getByRole('table', { name: 'Empréstimos de EPIs' })
  await expect(tabela.getByRole('row')).toHaveCount(3)

  await page.getByLabel('Situação', { exact: true }).selectOption('ATRASADOS')
  await expect(tabela.getByRole('row')).toHaveCount(2)
  await expect(tabela.getByRole('row', { name: /Luva nitrílica/ })).toBeVisible()
  expect(chamadas.some(chamada => chamada.situacao === 'ATRASADOS')).toBe(true)

  await page.getByLabel('Situação', { exact: true }).selectOption('TODOS')
  await expect(tabela.getByRole('row')).toHaveCount(4)
  const devolvida = tabela.getByRole('row', { name: /09\/09\/2026 16:30/ })
  await expect(devolvida.getByText('Devolvido', { exact: true })).toBeVisible()
  await expect(devolvida.getByText('09/09/2026 16:30, indicado para manutenção')).toBeVisible()
  await expect(devolvida.getByRole('button', { name: /Registrar devolução/ })).toHaveCount(0)
  expect(chamadas.some(chamada => chamada.situacao === 'TODOS')).toBe(true)

  await page.getByRole('searchbox', { name: 'Colaborador ou EPI' }).fill('maria')
  await page.getByRole('button', { name: 'Buscar' }).click()
  await expect(tabela.getByRole('row')).toHaveCount(2)
  await page.getByRole('button', { name: 'Limpar' }).click()
  await expect(page.getByLabel('Situação', { exact: true })).toHaveValue('PENDENTES')
  await expect(tabela.getByRole('row')).toHaveCount(3)
})

test('Gestor registers a delivery: request body, custody term, toast and refreshed list', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularApi(page)
  await page.goto('/emprestimos')

  const dialogo = await abrirNovoEmprestimo(page)
  await expect(dialogo.getByLabel('Responsável pela entrega')).toHaveValue('Gestora Ana')
  const colaborador = dialogo.getByLabel('Colaborador', { exact: true })
  await expect(colaborador.getByRole('option')).toHaveCount(3)
  await expect(colaborador.getByRole('option', { name: /Pedro Inativo/ })).toHaveCount(0)
  await expect(colaborador.getByRole('option', { name: 'Maria Souza · CPF •••.654.321-••' })).toHaveCount(1)
  await expect(dialogo.getByLabel('EPI disponível').getByRole('option', { name: 'Capacete classe B · código 1 · CA 12345 · saldo 10' })).toHaveCount(1)
  await colaborador.selectOption('4')
  await dialogo.getByLabel('EPI disponível').selectOption('1')
  await dialogo.getByLabel('Data prevista de devolução').fill('2030-10-15')
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()

  await expect(dialogo.getByText('Entrega registrada', { exact: true })).toBeVisible()
  await expect(dialogo.getByText('Capacete classe B está em posse de Maria Souza, com devolução prevista para 15/10/2030. Uma unidade saiu do estoque.')).toBeVisible()
  await expect(dialogo.getByText(/^Termo de cautela: o colaborador Maria Souza \(CPF 98765432100\) recebeu 1 unidade do EPI Capacete classe B/)).toBeVisible()
  expect(escritas(chamadas)).toHaveLength(1)
  expect(escritas(chamadas)[0].corpo).toEqual({ epiId: 1, colaboradorId: 4, dataDevolucaoPrevista: '2030-10-15' })
  await expect(dialogo.getByRole('button', { name: 'Fechar' })).toBeFocused()
  await dialogo.getByRole('button', { name: 'Fechar' }).click()

  await expect(dialogo).not.toBeVisible()
  await expect(page.getByRole('status').filter({ hasText: 'Entrega registrada: Capacete classe B para Maria Souza.' })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Empréstimos de EPIs' }).getByRole('row')).toHaveCount(4)
})

test('duplicate type in custody requires confirmation and justification before registering', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularApi(page, {
    entrega: corpo => corpo.confirmarSubstituicao === true ? undefined : {
      status: 409, json: { status: 409, error: 'Duplicidade de Posse', message: mensagemDuplicidade, emprestimoAtivoId: 11 },
    },
  })
  await page.goto('/emprestimos')

  const dialogo = await abrirNovoEmprestimo(page)
  await dialogo.getByLabel('Colaborador', { exact: true }).selectOption('4')
  await dialogo.getByLabel('EPI disponível').selectOption('1')
  await dialogo.getByLabel('Data prevista de devolução').fill('2030-10-15')
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()
  await expect(dialogo.getByRole('alert')).toContainText(mensagemDuplicidade)
  await expect(dialogo.getByLabel('Justificativa da substituição')).toBeFocused()

  await dialogo.getByRole('button', { name: 'Voltar' }).click()
  await expect(dialogo.getByLabel('Colaborador', { exact: true })).toHaveValue('4')
  await expect(dialogo.getByLabel('EPI disponível')).toHaveValue('1')
  await expect(dialogo.getByLabel('Data prevista de devolução')).toHaveValue('2030-10-15')
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()

  await dialogo.getByRole('button', { name: 'Confirmar substituição' }).click()
  await expect(dialogo.getByText('Informe a justificativa da substituição.')).toBeVisible()
  expect(escritas(chamadas)).toHaveLength(2)
  await dialogo.getByLabel('Justificativa da substituição').fill('Capacete anterior com a aba trincada.')
  await dialogo.getByRole('button', { name: 'Confirmar substituição' }).click()

  await expect(dialogo.getByText('Entrega registrada', { exact: true })).toBeVisible()
  expect(escritas(chamadas)).toHaveLength(3)
  expect(escritas(chamadas)[2].corpo).toEqual({
    epiId: 1, colaboradorId: 4, dataDevolucaoPrevista: '2030-10-15',
    confirmarSubstituicao: true, justificativaSubstituicao: 'Capacete anterior com a aba trincada.',
  })

  // Com a substituição, o mesmo EPI fica duas vezes com a mesma pessoa: a data da entrega distingue os botões.
  await dialogo.getByRole('button', { name: 'Fechar' }).click()
  await expect(page.getByRole('button', { name: /^Registrar devolução de Capacete classe B por Maria Souza/ })).toHaveCount(2)
  await expect(page.getByRole('button', { name: 'Registrar devolução de Capacete classe B por Maria Souza, entregue em 27/09/2026 08:00', exact: true })).toHaveCount(1)
  await expect(page.getByRole('button', { name: 'Registrar devolução de Capacete classe B por Maria Souza, entregue em 28/09/2026 10:00', exact: true })).toHaveCount(1)
})

test('server impediment is shown with its reason and the form keeps the data', async ({ page }) => {
  const motivo = 'Impedimento normativo: o colaborador João da Silva não atende às capacitações exigidas pelo EPI: Treinamento NR-6 vencido.'
  await iniciarSessao(page, gestora)
  const chamadas = await simularApi(page, {
    entrega: () => ({ status: 422, json: { status: 422, error: 'Entrega Impedida', message: motivo } }),
  })
  await page.goto('/emprestimos')

  const dialogo = await abrirNovoEmprestimo(page)
  await dialogo.getByLabel('Colaborador', { exact: true }).selectOption('3')
  await dialogo.getByLabel('EPI disponível').selectOption('2')
  await dialogo.getByLabel('Data prevista de devolução').fill('2030-10-15')
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()

  await expect(dialogo.getByRole('alert')).toContainText(motivo)
  await expect(dialogo.getByLabel('Colaborador', { exact: true })).toHaveValue('3')
  await expect(dialogo.getByLabel('EPI disponível')).toHaveValue('2')
  await expect(dialogo.getByLabel('Data prevista de devolução')).toHaveValue('2030-10-15')
  expect(escritas(chamadas)).toHaveLength(1)
})

test('required fields and a past return date are blocked locally without calling the API', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularApi(page)
  await page.goto('/emprestimos')

  const dialogo = await abrirNovoEmprestimo(page)
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()
  await expect(dialogo.getByText('Selecione o colaborador que recebe o EPI.')).toBeVisible()
  await expect(dialogo.getByText('Selecione o EPI que será entregue.')).toBeVisible()
  await expect(dialogo.getByText('Informe a data prevista de devolução.')).toBeVisible()
  await expect(dialogo.getByLabel('Colaborador', { exact: true })).toBeFocused()

  await dialogo.getByLabel('Colaborador', { exact: true }).selectOption('3')
  await dialogo.getByLabel('EPI disponível').selectOption('2')
  await dialogo.getByLabel('Data prevista de devolução').fill('2020-01-01')
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()
  await expect(dialogo.getByText('A devolução prevista não pode ser anterior a hoje, dia da entrega.')).toBeVisible()
  await expect(dialogo.getByLabel('Data prevista de devolução')).toBeFocused()
  expect(escritas(chamadas)).toHaveLength(0)
})

test('return asks for the condition, confirms the destination and sends only the state', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularApi(page)
  await page.goto('/emprestimos')

  await page.getByRole('button', { name: 'Registrar devolução de Luva nitrílica por João da Silva' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Registrar devolução' })
  await expect(dialogo.getByText('João da Silva', { exact: true })).toBeVisible()
  await expect(dialogo.getByText('Luva nitrílica (código 2)')).toBeVisible()
  await expect(dialogo.getByText('25/09/2026 (prazo vencido)')).toBeVisible()
  await dialogo.getByRole('button', { name: 'Confirmar devolução' }).click()
  await expect(dialogo.getByText('Informe a condição do item devolvido.')).toBeVisible()
  await expect(dialogo.getByRole('radio', { name: 'Apto para uso' })).toBeFocused()
  expect(escritas(chamadas)).toHaveLength(0)

  await dialogo.getByRole('radio', { name: 'Apto para uso' }).check()
  await expect(dialogo.getByRole('status')).toHaveText('O empréstimo será encerrado e o item voltará ao saldo disponível do estoque.')
  await dialogo.getByRole('radio', { name: 'Encaminhar para manutenção' }).check()
  await expect(dialogo.getByRole('status')).toHaveText('O empréstimo será encerrado com a indicação de manutenção. O item não voltará ao saldo disponível.')
  await dialogo.getByRole('button', { name: 'Confirmar devolução' }).click()

  await expect(dialogo).not.toBeVisible()
  expect(escritas(chamadas)).toHaveLength(1)
  expect(escritas(chamadas)[0].caminho).toBe('/api/emprestimos/10/devolucao')
  expect(escritas(chamadas)[0].corpo).toEqual({ estado: 'MANUTENCAO' })
  await expect(page.getByRole('status').filter({ hasText: 'Devolução registrada: Luva nitrílica indicado para manutenção, fora do saldo disponível.' })).toBeVisible()
  await expect(page.getByRole('row', { name: /Luva nitrílica/ })).toHaveCount(0)
})

test('a return already closed by someone else shows the server message and keeps the choice', async ({ page }) => {
  await iniciarSessao(page, gestora)
  await simularApi(page, {
    devolucao: { status: 409, json: { status: 409, error: 'Empréstimo Encerrado', message: 'O empréstimo 10 já foi encerrado.' } },
  })
  await page.goto('/emprestimos')

  await page.getByRole('button', { name: 'Registrar devolução de Luva nitrílica por João da Silva' }).click()
  const dialogo = page.getByRole('dialog', { name: 'Registrar devolução' })
  await dialogo.getByRole('radio', { name: 'Indicar descarte' }).check()
  await expect(dialogo.getByRole('status')).toContainText('a baixa definitiva não é registrada nesta tela')
  await dialogo.getByRole('button', { name: 'Confirmar devolução' }).click()

  await expect(dialogo.getByRole('alert')).toContainText('O empréstimo 10 já foi encerrado.')
  await expect(dialogo.getByRole('radio', { name: 'Indicar descarte' })).toBeChecked()
})

test('details show the custody term, the responsible people and the replacement justification', async ({ page }) => {
  await iniciarSessao(page, gestora)
  await simularApi(page)
  await page.goto('/emprestimos')

  const botao = page.getByRole('button', { name: 'Detalhes do empréstimo de Capacete classe B para Maria Souza' })
  await botao.click()
  const dialogo = page.getByRole('dialog', { name: 'Detalhes do empréstimo' })
  await expect(dialogo.getByText('27/09/2026 08:00, por Gestora Ana')).toBeVisible()
  await expect(dialogo.getByText('Capacete anterior com a aba trincada.')).toBeVisible()
  await expect(dialogo.getByText(/^Termo de cautela: o colaborador Maria Souza/)).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(dialogo).not.toBeVisible()
  await expect(botao).toBeFocused()

  await page.getByLabel('Situação', { exact: true }).selectOption('TODOS')
  await page.getByRole('button', { name: 'Detalhes do empréstimo de Capacete classe B para João da Silva' }).click()
  await expect(dialogo.getByText('09/09/2026 16:30, por Joana Ribeiro')).toBeVisible()
  await expect(dialogo.getByText('Indicado para manutenção')).toBeVisible()
  await expect(dialogo.getByText('Devolvido', { exact: true })).toBeVisible()
})

test('Gestor sees the latest projection of each EPI and the replenishment list from the server', async ({ page }) => {
  await iniciarSessao(page, gestora)
  await simularApi(page)
  await page.goto('/emprestimos')
  await page.getByRole('tab', { name: 'Projeções de substituição' }).click()

  const tabelaProjecoes = page.getByRole('table', { name: 'Projeções de substituição de EPIs' })
  await expect(tabelaProjecoes.getByRole('row')).toHaveCount(3)
  const linhaCapacete = tabelaProjecoes.getByRole('row', { name: /Capacete classe B/ })
  await expect(linhaCapacete.getByRole('cell', { name: '15/11/2026', exact: true })).toBeVisible()
  await expect(linhaCapacete.getByText('Substituição por desgaste acelerado em ambiente ALTO (fator 1.5), taxa de uso diário de 40%.')).toBeVisible()
  await expect(tabelaProjecoes.getByText('01/12/2026')).toHaveCount(0)
  await expect(tabelaProjecoes.getByRole('row').nth(1)).toContainText('Capacete classe B')

  const reposicao = page.getByRole('table', { name: 'Lista de reposição de EPIs' })
  await expect(reposicao.getByRole('row')).toHaveCount(3)
  await expect(reposicao.getByRole('row', { name: /Capacete classe B/ }).getByText('Descarte próximo', { exact: true })).toBeVisible()
  await expect(reposicao.getByRole('row', { name: /Capacete classe B/ }).getByText('22 dias', { exact: true })).toBeVisible()
  const linhaLuva = reposicao.getByRole('row', { name: /Luva nitrílica/ })
  await expect(linhaLuva.getByText('Estoque crítico', { exact: true })).toBeVisible()
  await expect(linhaLuva.getByText('Não se aplica')).toHaveCount(2)
  await expect(page.getByText('ALTA', { exact: true })).toHaveCount(0)
})

test('a successful analysis reports the calculated EPIs and reloads the saved lists', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularApi(page, {
    analises: [{ status: 200, json: {
      projecoes: [projecaoDaAnalise], pendenciasFalhas: [], alertas: [], totalProcessados: 1, totalSucesso: 1, totalPendencias: 0,
    } }],
  })
  const leituras = (caminho: string) => chamadas.filter(chamada => chamada.metodo === 'GET' && chamada.caminho === caminho).length
  await page.goto('/emprestimos')
  await page.getByRole('tab', { name: 'Projeções de substituição' }).click()
  await expect(page.getByRole('table', { name: 'Projeções de substituição de EPIs' }).getByRole('row')).toHaveCount(3)
  const antes = { projecoes: leituras('/api/projecoes'), alertas: leituras('/api/projecoes/alertas') }

  await page.getByRole('button', { name: 'Solicitar análise de projeção' }).click()
  await expect(page.getByText('Análise concluída', { exact: true })).toBeVisible()
  await expect(page.getByText('1 EPI com projeção calculada.')).toBeVisible()
  await expect.poll(() => leituras('/api/projecoes')).toBeGreaterThan(antes.projecoes)
  await expect.poll(() => leituras('/api/projecoes/alertas')).toBeGreaterThan(antes.alertas)
})

test('analysis lists pending EPIs with a new attempt, and a failure shows an error without new alerts', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularApi(page, {
    analises: [
      { status: 200, json: {
        projecoes: [projecaoDaAnalise], alertas: [], totalProcessados: 2, totalSucesso: 1, totalPendencias: 1,
        pendenciasFalhas: [{ epiId: 3, nomeEpi: 'Protetor auricular', motivoFalha: 'EPI sem histórico de empréstimos no período de amostragem.' }],
      } },
      { status: 500, json: { status: 500, error: 'Erro Interno' } },
    ],
  })
  await page.goto('/emprestimos')
  await page.getByRole('tab', { name: 'Projeções de substituição' }).click()
  const reposicao = page.getByRole('table', { name: 'Lista de reposição de EPIs' })
  await expect(reposicao.getByRole('row')).toHaveCount(3)

  await page.getByRole('button', { name: 'Solicitar análise de projeção' }).click()
  await expect(page.getByText('Análise concluída com pendências')).toBeVisible()
  await expect(page.getByText('1 de 2 EPIs com projeção calculada.', { exact: false })).toBeVisible()
  await expect(page.getByRole('list', { name: 'EPIs sem projeção calculada' }).getByRole('listitem'))
    .toHaveText(['Protetor auricular: EPI sem histórico de empréstimos no período de amostragem.'])

  await page.getByRole('button', { name: 'Solicitar nova tentativa' }).click()
  await expect(page.getByRole('alert').filter({ hasText: 'Análise não concluída' })).toContainText('Não foi possível concluir a análise de projeção agora.')
  await expect(page.getByRole('button', { name: 'Tentar novamente' })).toBeVisible()
  await expect(reposicao.getByRole('row')).toHaveCount(3)
  expect(escritas(chamadas).filter(chamada => chamada.caminho === '/api/projecoes/analise')).toHaveLength(2)
})

test('reloading the page shows the loans persisted by the API', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  await simularApi(page)
  await page.goto('/emprestimos')

  const dialogo = await abrirNovoEmprestimo(page)
  await dialogo.getByLabel('Colaborador', { exact: true }).selectOption('4')
  await dialogo.getByLabel('EPI disponível').selectOption('2')
  await dialogo.getByLabel('Data prevista de devolução').fill('2030-11-30')
  await dialogo.getByRole('button', { name: 'Confirmar entrega' }).click()
  await dialogo.getByRole('button', { name: 'Fechar' }).click()
  await expect(page.getByRole('table', { name: 'Empréstimos de EPIs' }).getByRole('row')).toHaveCount(4)

  await page.reload()
  const tabela = page.getByRole('table', { name: 'Empréstimos de EPIs' })
  await expect(tabela.getByRole('row')).toHaveCount(4)
  await expect(tabela.getByRole('row', { name: /Maria Souza/ }).filter({ hasText: '30/11/2030' })).toHaveCount(1)
})

test('keyboard opens the new loan dialog, Escape closes it and focus returns to the button', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  await simularApi(page)
  await page.goto('/emprestimos')

  const botao = page.getByRole('button', { name: 'Novo empréstimo' })
  await botao.focus()
  await page.keyboard.press('Enter')
  const dialogo = page.getByRole('dialog', { name: 'Novo empréstimo' })
  await expect(dialogo.getByLabel('Colaborador', { exact: true })).toBeVisible()
  await page.keyboard.press('Escape')
  await expect(dialogo).not.toBeVisible()
  await expect(botao).toBeFocused()

  const devolver = page.getByRole('button', { name: 'Registrar devolução de Luva nitrílica por João da Silva' })
  await devolver.focus()
  await page.keyboard.press('Enter')
  const devolucao = page.getByRole('dialog', { name: 'Registrar devolução' })
  await devolucao.getByRole('radio', { name: 'Apto para uso' }).focus()
  await page.keyboard.press('ArrowDown')
  await expect(devolucao.getByRole('radio', { name: 'Encaminhar para manutenção' })).toBeChecked()
  await page.keyboard.press('Escape')
  await expect(devolucao).not.toBeVisible()
  await expect(devolver).toBeFocused()
})

for (const width of [360, 768, 1344]) {
  test(`loans page has no horizontal overflow at ${width}px`, async ({ page }, testInfo) => {
    const errors: string[] = []
    page.on('pageerror', error => errors.push(error.message))
    const semRolagemHorizontal = () => page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)
    await page.setViewportSize({ width, height: 960 })
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/emprestimos')

    await expect(page.getByRole('table', { name: 'Empréstimos de EPIs' })).toBeVisible()
    expect(await semRolagemHorizontal()).toBe(true)
    await abrirNovoEmprestimo(page)
    expect(await semRolagemHorizontal()).toBe(true)
    await page.screenshot({ path: testInfo.outputPath(`emprestimos-${width}.png`), fullPage: true })
    await page.keyboard.press('Escape')

    await page.getByRole('tab', { name: 'Projeções de substituição' }).click()
    await expect(page.getByRole('table', { name: 'Lista de reposição de EPIs' })).toBeVisible()
    expect(await semRolagemHorizontal()).toBe(true)
    await page.screenshot({ path: testInfo.outputPath(`projecoes-${width}.png`), fullPage: true })
    expect(errors).toEqual([])
  })
}
