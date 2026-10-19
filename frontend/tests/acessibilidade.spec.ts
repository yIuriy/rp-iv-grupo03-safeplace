// Verificação automática de acessibilidade (RNF11, issue #202): o axe-core avalia as regras WCAG 2.1
// níveis A e AA nas telas e formulários do MVP, em larguras de celular, tablet e desktop. Também confere
// o que o axe não cobre: reflow sem rolagem horizontal (1.4.10) e foco visível ao usar Tab (2.4.7). A
// API é simulada com dados no formato do backend, para que tabelas e listas sejam avaliadas preenchidas.

import AxeBuilder from '@axe-core/playwright'
import { test, expect, type Page } from '@playwright/test'
import { alertas as alertasCapacitacao, resumos, tarefas } from './helpers/capacitacoes'
import { gestora, iniciarSessao, pessoas, supervisor } from './helpers/sessao'

const regrasWcag21AA = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa']

/** 320 px é a largura de referência do critério 1.4.10 (reflow). */
const larguras = [
  { nome: 'celular', width: 320, height: 800 },
  { nome: 'tablet', width: 768, height: 1024 },
  { nome: 'desktop', width: 1344, height: 960 },
]

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
const maria = { id: 4, nome: 'Maria Souza', cpf: '98765432100', dataNascimento: '1990-03-02', email: null, perfil: null, ativo: true }

const emprestimoAtrasado = {
  id: 10, epiId: 2, epiNome: 'Luva nitrílica', colaboradorId: 3, colaboradorNome: 'João da Silva',
  responsavelEntregaId: 1, dataRetirada: '2026-09-20T09:15:00', dataDevolucaoPrevista: '2026-09-25',
  dataDevolucaoReal: null, responsavelDevolucaoId: null, estadoDevolucao: null, status: 'DEVOLUCAO_ATRASADA',
  atrasado: true, termoCautela: 'Termo de cautela da luva nitrílica.', justificativaSubstituicao: null,
}

const acidente = {
  idOcorrencia: 1, tipoOcorrencia: 'ACIDENTE', dataOcorrencia: '2026-09-10T14:37:00', dataRegistro: '2026-09-10T15:02:00',
  statusOcorrencia: 'ABERTA', descricao: 'Prensa atingiu a mão do operador.', local: null, numeroProtocoloCAT: 'CAT-2026-09-0001',
  gravidade: 'CRITICO', causaRaiz: 'FALHA_EPI', tipo: 'Esmagamento', dano: 'Lesão na mão direita', destino: 'Hospital Municipal',
  situacaoRisco: null, potencialDano: null, areaId: 1, areaNome: 'Linha de Solda', colaboradorId: 3,
  colaboradorNome: 'João da Silva', relatorId: 1, relatorNome: 'Gestora Ana', relatorPerfil: 'GESTOR_SEGURANCA',
}
const incidente = {
  idOcorrencia: 2, tipoOcorrencia: 'INCIDENTE', dataOcorrencia: '2026-08-20T09:00:00', dataRegistro: '2026-08-21T08:00:00',
  statusOcorrencia: 'EM_TRIAGEM', descricao: 'Óleo no corredor da doca.', local: null, numeroProtocoloCAT: null, gravidade: null,
  causaRaiz: null, tipo: null, dano: null, destino: null, situacaoRisco: 'Piso escorregadio', potencialDano: 'Queda',
  areaId: 2, areaNome: 'Doca Externa', colaboradorId: 4, colaboradorNome: 'Maria Souza', relatorId: 2, relatorNome: 'Joana Ribeiro',
  relatorPerfil: 'SUPERVISOR',
}

const setores = [
  { id: 1, nome: 'Galpão Principal', descricao: 'Área de manufatura e usinagem', localizacao: 'Bloco 1', status: 'ALTO' },
  { id: 2, nome: 'Doca de Carga', descricao: 'Movimentação e logística', localizacao: null, status: 'MEDIO' },
]
const areas = [
  { id: 1, codigo: 'SET-01', nome: 'Linha de Solda', descricao: 'Soldagem pesada e montagem', nivelPerigo: 'ALTO', setorId: 1,
    agentesRisco: ['Fumos metálicos', 'Ruído elevado'], limites: 'Piso 1, demarcação amarela',
    episObrigatorios: [{ id: 1, nome: 'Capacete classe B', numeroCa: '12345', status: 'DISPONIVEL' }] },
  { id: 2, codigo: 'SET-02', nome: 'Doca Externa', descricao: 'Manobra de caminhões', nivelPerigo: 'MEDIO', setorId: 2,
    agentesRisco: ['Tráfego de veículos'], limites: 'Pátio externo sul',
    episObrigatorios: [{ id: 2, nome: 'Luva nitrílica', numeroCa: '777', status: 'DISPONIVEL' }] },
]

/** Respostas de GET por caminho; caminhos ausentes respondem lista vazia. */
const respostas: Record<string, unknown> = {
  '/api/ocorrencias': [acidente, incidente],
  '/api/setores': setores,
  '/api/areas-risco': areas,
  '/api/epis': [capacete, luva],
  '/api/emprestimos': [emprestimoAtrasado],
  '/api/emprestimos/epis-disponiveis': [capacete, luva],
  '/api/tarefas': tarefas,
  '/api/capacitacoes/colaboradores': resumos,
  '/api/capacitacoes/alertas': alertasCapacitacao,
  '/api/usuarios': Object.values(pessoas),
  '/api/usuarios/colaboradores': [pessoas.colaborador, maria],
  '/api/usuarios/me': pessoas.gestora,
}

async function simularApi(page: Page) {
  await page.route(url => url.pathname.startsWith('/api/'), route => {
    const request = route.request()
    if (request.method() !== 'GET') return route.fulfill({ json: {} })
    return route.fulfill({ json: respostas[new URL(request.url()).pathname] ?? [] })
  })
}

/**
 * Roda o axe com as regras WCAG 2.1 A e AA e devolve as violações em formato legível na falha. Exige
 * regras aprovadas, para que uma página vazia ou ainda carregando não passe sem ter sido avaliada.
 */
async function violacoesWcag(page: Page) {
  const { passes, violations } = await new AxeBuilder({ page }).withTags(regrasWcag21AA).analyze()
  expect(passes.length, 'o axe deve ter avaliado regras na página').toBeGreaterThan(0)
  return violations.map(({ id, impact, help, nodes }) => ({ id, impact, help, alvos: nodes.map(node => node.target.join(' ')) }))
}

/** Indica se a página é mais larga que a janela, o que obrigaria a rolar na horizontal. */
async function rolagemHorizontal(page: Page) {
  return page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth)
}

/** Os dois perfis com acesso veem as mesmas telas, com conteúdo e ações diferentes. */
const perfis = [{ nome: 'Gestor', sessao: gestora }, { nome: 'Supervisor', sessao: supervisor }]

/**
 * Telas do MVP; `pronto` é um texto que só aparece após a carga. Em usuários, o Gestor abre na aba de
 * supervisores e o Supervisor vê só os colaboradores (`prontoSupervisor`).
 */
const telas = [
  { nome: 'ocorrências', rota: '/ocorrencias', pronto: 'CAT-2026-09-0001' },
  { nome: 'EPIs', rota: '/epis', pronto: 'Capacete classe B' },
  { nome: 'empréstimos', rota: '/emprestimos', pronto: 'João da Silva' },
  { nome: 'áreas de risco', rota: '/areas-risco', pronto: 'Linha de Solda' },
  { nome: 'tarefas', rota: '/tarefas', pronto: 'Montagem de andaime' },
  { nome: 'capacitações', rota: '/capacitacoes', pronto: 'Ana Souza' },
  { nome: 'usuários', rota: '/usuarios', pronto: 'Joana Ribeiro', prontoSupervisor: 'João da Silva' },
]

/**
 * Formulário principal de cada tela, enviado vazio para que as mensagens de erro também sejam
 * avaliadas. `abrir` é o botão que mostra o formulário; `formulario` localiza o diálogo ou a região.
 * O erro aparece no campo (`aria-invalid`) ou num aviso no topo do formulário (`role="alert"`). No
 * cadastro de EPI, `validacaoNativa` indica que o navegador bloqueia o envio e foca o primeiro campo
 * inválido, com mensagem própria que não faz parte da página.
 */
const formularios = [
  { nome: 'relato de ocorrência', rota: '/ocorrencias', abrir: 'Relatar ocorrência',
    formulario: (page: Page) => page.getByRole('form', { name: 'Relatar ocorrência' }), enviar: 'Enviar relato' },
  { nome: 'cadastro de EPI', rota: '/epis?aba=manutencao', abrir: 'Cadastrar EPI',
    formulario: (page: Page) => page.getByRole('dialog', { name: 'Cadastrar EPI' }), enviar: 'Cadastrar EPI', validacaoNativa: true },
  { nome: 'entrega de EPI', rota: '/emprestimos', abrir: 'Novo empréstimo',
    formulario: (page: Page) => page.getByRole('dialog', { name: 'Novo empréstimo' }), enviar: 'Confirmar entrega' },
  { nome: 'cadastro de área de risco', rota: '/areas-risco', abrir: 'Cadastrar área',
    formulario: (page: Page) => page.getByRole('dialog', { name: 'Cadastrar área de risco' }), enviar: 'Cadastrar área' },
  { nome: 'cadastro de tarefa', rota: '/tarefas', abrir: 'Nova tarefa',
    formulario: (page: Page) => page.getByRole('dialog', { name: 'Cadastrar nova tarefa' }), enviar: 'Cadastrar tarefa' },
  { nome: 'cadastro de certificação', rota: '/capacitacoes?aba=cadastro', abrir: null,
    formulario: (page: Page) => page.getByRole('region', { name: 'Cadastrar certificação' }), enviar: 'Cadastrar certificação' },
  { nome: 'cadastro de supervisor', rota: '/usuarios', abrir: 'Novo supervisor',
    formulario: (page: Page) => page.getByRole('dialog', { name: 'Novo supervisor' }), enviar: 'Cadastrar supervisor' },
]

for (const largura of larguras) {
  test.describe(`WCAG 2.1 AA em ${largura.nome} (${largura.width} px)`, () => {
    test.use({ viewport: { width: largura.width, height: largura.height } })

    test('login', async ({ page }) => {
      await page.goto('/login')
      await expect(page.getByRole('heading', { name: 'Entrar no SafePlace' })).toBeVisible()
      expect(await violacoesWcag(page)).toEqual([])
      expect(await rolagemHorizontal(page)).toBe(false)
    })

    test('login com credenciais recusadas', async ({ page }) => {
      await page.route('**/api/auth/login', route =>
        route.fulfill({ status: 401, json: { message: 'E-mail ou senha incorretos.' } }))
      await page.goto('/login')
      await page.getByLabel('E-mail').fill('alguem@safeplace.test')
      await page.getByLabel('Senha').fill('senha-errada')
      await page.getByRole('button', { name: 'Entrar' }).click()
      await expect(page.getByRole('alert')).toContainText('E-mail ou senha incorretos')
      expect(await violacoesWcag(page)).toEqual([])
    })

    for (const perfil of perfis) {
      for (const tela of telas) {
        test(`${tela.nome} (${perfil.nome})`, async ({ page }) => {
          await abrirTela(page, perfil, tela)
          expect(await violacoesWcag(page)).toEqual([])
          expect(await rolagemHorizontal(page)).toBe(false)
        })
      }
    }

    for (const item of formularios) {
      test(`${item.nome} com erros de validação`, async ({ page }) => {
        await iniciarSessao(page, gestora)
        await simularApi(page)
        await page.goto(item.rota)
        if (item.abrir) await page.getByRole('button', { name: item.abrir }).click()
        const formulario = item.formulario(page)
        await formulario.getByRole('button', { name: item.enviar }).click()
        if ('validacaoNativa' in item) {
          await expect(formulario.locator('input:invalid, select:invalid, textarea:invalid').first()).toBeFocused()
        } else {
          await expect(formulario.locator('[aria-invalid="true"], [role="alert"]').first()).toBeVisible()
        }
        expect(await violacoesWcag(page)).toEqual([])
      })
    }

    for (const perfil of perfis) {
      for (const tela of telas) {
        test(`foco visível ao percorrer ${tela.nome} com Tab (${perfil.nome})`, async ({ page }) => {
          await abrirTela(page, perfil, tela)
          const { visitados, semIndicador, primeiro, fim } = await percorrerComTab(page)
          expect(primeiro).toBe('a "Pular para o conteúdo"')
          expect(['saiu', 'inicio']).toContain(fim)
          expect(visitados).toBeGreaterThan(3)
          expect(semIndicador).toEqual([])
        })
      }
    }
  })
}

/** Abre a tela com a sessão do perfil e a API simulada, e espera o conteúdo carregado. */
async function abrirTela(page: Page, perfil: (typeof perfis)[number], tela: (typeof telas)[number]) {
  await iniciarSessao(page, perfil.sessao)
  await simularApi(page)
  await page.goto(tela.rota)
  const pronto = perfil.sessao === supervisor && 'prontoSupervisor' in tela ? tela.prontoSupervisor : tela.pronto
  await expect(page.getByText(pronto).first()).toBeVisible()
}

/**
 * Percorre a página com Tab e lista os elementos focados sem indicador visível (WCAG 2.4.7). O indicador
 * do design system é o contorno de `:focus-visible`, a sombra interna dos campos ou o contorno no visual
 * vizinho de caixas de seleção. `fim` diz como o percurso acabou: o foco saiu da página ou voltou ao
 * primeiro elemento (percurso completo), repetiu outro elemento (foco preso) ou atingiu o limite. Nos
 * campos de data, o Tab passa por dia, mês e ano sem trocar de elemento; essa repetição seguida é ignorada.
 */
async function percorrerComTab(page: Page, limite = 150) {
  const semIndicador: string[] = []
  let visitados = 0
  let primeiro = ''
  let fim: 'saiu' | 'inicio' | 'preso' | 'limite' = 'limite'
  for (let passo = 0; passo < limite; passo++) {
    await page.keyboard.press('Tab')
    const foco = await page.evaluate(ehPrimeiro => {
      const elemento = document.activeElement as HTMLElement | null
      if (!elemento || elemento === document.body) return 'saiu'
      if (elemento.dataset.focoAtual) return 'mesmo'
      document.querySelector('[data-foco-atual]')?.removeAttribute('data-foco-atual')
      elemento.dataset.focoAtual = 'sim'
      if (elemento.dataset.focoVisitado) return elemento.dataset.focoVisitado === 'primeiro' ? 'inicio' : 'preso'
      elemento.dataset.focoVisitado = ehPrimeiro ? 'primeiro' : 'sim'
      const temIndicador = (alvo: Element | null) => {
        if (!alvo) return false
        const estilo = getComputedStyle(alvo)
        return (estilo.outlineStyle !== 'none' && parseFloat(estilo.outlineWidth) > 0) || estilo.boxShadow !== 'none'
      }
      const nome = elemento.getAttribute('aria-label') ?? elemento.textContent ?? elemento.getAttribute('name') ?? ''
      return {
        descricao: `${elemento.tagName.toLowerCase()} "${nome.trim().slice(0, 50)}"`,
        visivel: temIndicador(elemento) || (elemento.matches('.sp-choice-input') && temIndicador(elemento.nextElementSibling)),
      }
    }, visitados === 0)
    if (foco === 'mesmo') continue
    if (typeof foco === 'string') {
      fim = foco
      break
    }
    if (visitados === 0) primeiro = foco.descricao
    visitados++
    if (!foco.visivel) semIndicador.push(foco.descricao)
  }
  return { visitados, semIndicador, primeiro, fim }
}
