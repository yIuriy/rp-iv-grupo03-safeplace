// Testes do registro e acompanhamento de ocorrências e planos de ação (issue #100; UC05/UC07/UC09):
// relato de acidente e incidente com confirmação do protocolo, campos obrigatórios, recusas da API,
// notificação de novos relatos ao Gestor, triagem, edição, arquivamento, plano de ação (cadastro,
// conclusão, prorrogação, reatribuição e alerta), acompanhamento pelo Supervisor, recarga da página,
// teclado e larguras. Só as respostas HTTP são simuladas, por uma API em memória com estado.

import { test, expect, type Page, type Request } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

function diaIso(deslocamento = 0): string {
  const data = new Date()
  data.setDate(data.getDate() + deslocamento)
  return `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`
}
const ddmmaaaa = (iso: string) => iso.split('-').reverse().join('/')

const setores = [
  { id: 1, nome: 'Galpão Principal', descricao: null, localizacao: 'Bloco 1', status: 'ALTO' },
  { id: 2, nome: 'Doca de Carga', descricao: null, localizacao: null, status: 'MEDIO' },
]
const areas = [
  { id: 10, codigo: 'AR-10', nome: 'Linha de Prensas', descricao: null, nivelPerigo: 'ALTO', setorId: 1, agentesRisco: [], limites: null, episObrigatorios: [] },
  { id: 11, codigo: 'AR-11', nome: 'Rampa da Doca', descricao: null, nivelPerigo: 'MEDIO', setorId: 2, agentesRisco: [], limites: null, episObrigatorios: [] },
]
const colaboradores = [
  { id: 7, nome: 'João da Linha', cpf: '12345678909', dataNascimento: '1992-06-18', email: null, perfil: 'COLABORADOR', ativo: true },
  { id: 8, nome: 'Maria Souza', cpf: '52998224725', dataNascimento: '1990-01-01', email: null, perfil: 'COLABORADOR', ativo: true },
]

const supervisora = { id: 9, nome: 'Joana Ribeiro', cpf: '11144477735', dataNascimento: '1988-04-20', email: 'joana@safeplace.test', perfil: 'SUPERVISOR', ativo: true }

type Registro = Record<string, unknown> & { idOcorrencia: number; statusOcorrencia: string; tipoOcorrencia: string }
type Acao = Record<string, unknown> & { id: number; idOcorrencia: number; prazo: string; situacao: string; status: string }

function ocorrenciaBase(campos: Partial<Registro> & { idOcorrencia: number }): Registro {
  return {
    tipoOcorrencia: 'INCIDENTE', dataOcorrencia: `${diaIso(-3)}T09:00:00`, dataRegistro: `${diaIso(-2)}T08:00:00`,
    statusOcorrencia: 'ABERTA', descricao: 'Óleo no corredor da doca.', local: null, numeroProtocoloCAT: null, gravidade: null,
    causaRaiz: null, tipo: null, dano: null, destino: null, situacaoRisco: 'Piso escorregadio', potencialDano: 'Queda',
    areaId: 11, areaNome: 'Rampa da Doca', colaboradorId: 8, colaboradorNome: 'Maria Souza', gestorId: null, gestorNome: null,
    ...campos,
  }
}

type Estado = {
  ocorrencias: Registro[]
  acoes: Acao[]
  prorrogacoes: { idAcao: number; prazoAnterior: string; novoPrazo: string; justificativa: string }[]
  requisicoes: { metodo: string; caminho: string; corpo: unknown }[]
  /** Resposta forçada para a próxima requisição que casar com método e caminho. */
  falhas: { metodo: string; caminho: RegExp; status: number; mensagem: string }[]
}

function corpo(request: Request): unknown {
  try { return request.postDataJSON() } catch { return null }
}

function comAlerta(acao: Acao): Acao {
  const dias = acao.alertaAtivo ? Number(acao.antecedenciaAlertaDias ?? 0) : null
  if (dias === null) return { ...acao, dataAlerta: null }
  const data = new Date(`${acao.prazo}T12:00:00`)
  data.setDate(data.getDate() - dias)
  return { ...acao, dataAlerta: data.toISOString().slice(0, 10) }
}

/** API em memória que aplica as regras essenciais do backend e guarda o estado entre recargas. */
async function simularApi(page: Page, inicial: Partial<Pick<Estado, 'ocorrencias' | 'acoes'>> = {}): Promise<Estado> {
  const estado: Estado = {
    ocorrencias: inicial.ocorrencias ?? [], acoes: inicial.acoes ?? [], prorrogacoes: [], requisicoes: [], falhas: [],
  }
  let proximaOcorrencia = 100
  let proximaAcao = 500

  await page.route(url => url.pathname.startsWith('/api/'), async route => {
    const request = route.request()
    const url = new URL(request.url())
    const caminho = url.pathname.replace(/^\/api/, '')
    const metodo = request.method()
    const dados = corpo(request) as Record<string, unknown> | null
    if (metodo !== 'GET') estado.requisicoes.push({ metodo, caminho, corpo: dados })
    const falha = estado.falhas.findIndex(f => f.metodo === metodo && f.caminho.test(caminho))
    if (falha >= 0) {
      const [f] = estado.falhas.splice(falha, 1)
      return route.fulfill({ status: f.status, json: { status: f.status, message: f.mensagem } })
    }
    const json = (valor: unknown, status = 200) => route.fulfill({ status, json: valor })
    const naoEncontrado = () => json({ message: 'Não encontrado' }, 404)

    if (caminho === '/setores') return json(setores)
    if (caminho === '/areas-risco') return json(areas)
    if (caminho.startsWith('/usuarios/colaboradores')) return json(colaboradores)
    if (caminho === '/usuarios') return json([...colaboradores, supervisora])

    if (caminho === '/ocorrencias' && metodo === 'GET') return json(estado.ocorrencias)
    if (caminho === '/ocorrencias' && metodo === 'POST') {
      const id = proximaOcorrencia++
      const acidente = dados?.tipoOcorrencia === 'ACIDENTE'
      const area = areas.find(a => a.id === dados?.areaId)
      const pessoa = colaboradores.find(c => c.id === dados?.colaboradorId)
      const novo = ocorrenciaBase({
        idOcorrencia: id, tipoOcorrencia: String(dados?.tipoOcorrencia), dataOcorrencia: String(dados?.dataOcorrencia),
        dataRegistro: `${diaIso()}T10:15:00`, descricao: String(dados?.descricao), areaId: area?.id ?? null,
        areaNome: area?.nome ?? null, colaboradorId: pessoa?.id ?? null, colaboradorNome: pessoa?.nome ?? null,
        numeroProtocoloCAT: acidente ? `CAT-${diaIso().slice(0, 7)}-0042` : null,
        causaRaiz: acidente ? dados?.causaRaiz ?? null : null, gravidade: acidente ? dados?.gravidade ?? null : null,
        tipo: acidente ? dados?.tipo ?? null : null, dano: acidente ? dados?.dano ?? null : null, destino: acidente ? dados?.destino ?? null : null,
        situacaoRisco: acidente ? null : dados?.situacaoRisco ?? null, potencialDano: acidente ? null : dados?.potencialDano ?? null,
      })
      estado.ocorrencias.push(novo)
      return json(novo, 201)
    }

    const porId = caminho.match(/^\/ocorrencias\/(\d+)(\/[a-z-]+)?$/)
    if (porId) {
      const registro = estado.ocorrencias.find(o => o.idOcorrencia === Number(porId[1]))
      if (!registro) return naoEncontrado()
      const sufixo = porId[2]
      if (!sufixo && metodo === 'GET') return json(registro)
      if (!sufixo && metodo === 'PUT') {
        const area = areas.find(a => a.id === dados?.areaId)
        const pessoa = colaboradores.find(c => c.id === dados?.colaboradorId)
        Object.assign(registro, dados, { areaNome: area?.nome ?? null, colaboradorNome: pessoa?.nome ?? null })
        return json(registro)
      }
      if (sufixo === '/triagem') {
        if (registro.statusOcorrencia !== 'ABERTA') return json({ message: 'Transição inválida.' }, 400)
        registro.statusOcorrencia = 'EM_TRIAGEM'
        return json(registro)
      }
      if (sufixo === '/arquivar') {
        if (registro.statusOcorrencia !== 'EM_TRIAGEM') return json({ message: 'Transição inválida.' }, 400)
        registro.statusOcorrencia = 'ARQUIVADA'
        return json(registro)
      }
      if (sufixo === '/planos-acao' && metodo === 'GET') return json(estado.acoes.filter(a => a.idOcorrencia === registro.idOcorrencia).map(comAlerta))
      if (sufixo === '/planos-acao' && metodo === 'POST') {
        const responsavel = dados?.responsavelColaboradorId
          ? { responsavelTipo: 'COLABORADOR', responsavelId: dados.responsavelColaboradorId, responsavelNome: colaboradores.find(c => c.id === dados.responsavelColaboradorId)?.nome }
          : { responsavelTipo: 'SETOR', responsavelId: dados?.responsavelSetorId, responsavelNome: setores.find(s => s.id === dados?.responsavelSetorId)?.nome }
        const acao: Acao = {
          id: proximaAcao++, idOcorrencia: registro.idOcorrencia, medidasCorretivas: dados?.medidasCorretivas ?? null,
          medidasPreventivas: dados?.medidasPreventivas ?? null, prazo: String(dados?.prazo), status: 'ABERTA', situacao: 'ABERTA',
          ...responsavel, alertaAtivo: dados?.alertaAtivo === true, antecedenciaAlertaDias: dados?.antecedenciaAlertaDias ?? null,
          gestorId: 1, gestorNome: 'Gestora Ana',
        }
        estado.acoes.push(acao)
        return json(comAlerta(acao), 201)
      }
    }

    const acaoMatch = caminho.match(/^\/planos-acao\/(\d+)\/([a-z]+)$/)
    if (acaoMatch) {
      const acao = estado.acoes.find(a => a.id === Number(acaoMatch[1]))
      if (!acao) return json({ message: 'Ação não encontrada' }, 400)
      const operacao = acaoMatch[2]
      if (operacao === 'status') { acao.status = 'CONCLUIDA'; acao.situacao = 'CONCLUIDA'; return json(comAlerta(acao)) }
      if (operacao === 'responsavel') {
        Object.assign(acao, dados?.colaboradorId
          ? { responsavelTipo: 'COLABORADOR', responsavelId: dados.colaboradorId, responsavelNome: colaboradores.find(c => c.id === dados.colaboradorId)?.nome }
          : { responsavelTipo: 'SETOR', responsavelId: dados?.setorId, responsavelNome: setores.find(s => s.id === dados?.setorId)?.nome })
        return json(comAlerta(acao))
      }
      if (operacao === 'alerta') {
        acao.alertaAtivo = dados?.ativo === true
        acao.antecedenciaAlertaDias = dados?.ativo ? dados?.antecedenciaDias ?? null : null
        return json(comAlerta(acao))
      }
      if (operacao === 'prorrogacoes' && metodo === 'POST') {
        const registro = { idAcao: acao.id, prazoAnterior: acao.prazo, novoPrazo: String(dados?.novoPrazo), justificativa: String(dados?.justificativa) }
        estado.prorrogacoes.push(registro)
        acao.prazo = registro.novoPrazo
        return json(registro, 201)
      }
      if (operacao === 'prorrogacoes') return json(estado.prorrogacoes.filter(p => p.idAcao === acao.id))
    }
    return naoEncontrado()
  })
  return estado
}

const ultimo = (estado: Estado, metodo: string, caminho: string | RegExp) =>
  estado.requisicoes.filter(r => r.metodo === metodo && (typeof caminho === 'string' ? r.caminho === caminho : caminho.test(r.caminho))).at(-1)

const formulario = (page: Page) => page.getByRole('form', { name: 'Relatar ocorrência' })

async function preencherBase(page: Page, opcoes: { setor?: string; area?: string } = {}) {
  const form = formulario(page)
  await form.getByLabel('Data e hora do fato *').fill(`${diaIso(-1)}T14:37`)
  await form.getByLabel('Colaborador envolvido *').selectOption({ label: 'João da Linha' })
  await form.getByLabel('Setor *').selectOption({ label: opcoes.setor ?? 'Galpão Principal (Bloco 1)' })
  await form.getByLabel('Área de risco *').selectOption({ label: opcoes.area ?? 'Linha de Prensas' })
  await form.getByLabel('Descrição dos fatos *').fill('Prensa atingiu a mão do operador.')
}

test.describe('Relato de ocorrências (UC09/UC05, issue #100)', () => {
  test('relato vazio é barrado com os campos pendentes e foco no tipo', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const estado = await simularApi(page)
    await page.goto('/ocorrencias')
    await page.getByRole('button', { name: 'Relatar ocorrência' }).click()
    const form = formulario(page)
    await form.getByRole('button', { name: 'Enviar relato' }).click()

    await expect(form.getByText('Selecione se foi um acidente ou um incidente.')).toBeVisible()
    await expect(form.getByText('Informe a data e a hora do fato.')).toBeVisible()
    await expect(form.getByText('Selecione o setor onde o fato ocorreu.')).toBeVisible()
    await expect(form.getByText('Selecione o colaborador envolvido.')).toBeVisible()
    await expect(form.getByText('Descreva o que aconteceu.')).toBeVisible()
    await expect(form.getByRole('radio', { name: /^Acidente/ })).toBeFocused()
    await expect(form.getByLabel('Descrição dos fatos *')).toHaveAttribute('aria-invalid', 'true')
    expect(ultimo(estado, 'POST', '/ocorrencias')).toBeUndefined()
  })

  test('acidente exige causa e gravidade, só lista áreas do setor e mostra o protocolo CAT na confirmação', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const estado = await simularApi(page)
    await page.goto('/ocorrencias')
    await page.getByRole('button', { name: 'Relatar ocorrência' }).click()
    const form = formulario(page)
    await form.getByRole('radio', { name: /^Acidente/ }).check()
    await expect(form.getByLabel('Situação de risco *')).toHaveCount(0)

    await expect(form.getByLabel('Área de risco *')).toBeDisabled()
    await form.getByLabel('Setor *').selectOption({ label: 'Doca de Carga' })
    await expect(form.getByLabel('Área de risco *').locator('option')).toHaveText(['Selecione a área', 'Rampa da Doca'])

    await preencherBase(page)
    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await expect(form.getByText('Indique a causa: fator humano, falha de EPI ou outra.')).toBeVisible()
    await expect(form.getByLabel('Causa *')).toBeFocused()

    await form.getByLabel('Causa *').selectOption({ label: 'Falha de EPI' })
    await form.getByLabel('Gravidade *').selectOption({ label: 'Crítico' })
    await form.getByLabel('Tipo de lesão').fill('Esmagamento')
    await form.getByRole('button', { name: 'Enviar relato' }).click()

    const confirmacao = page.getByRole('dialog', { name: 'Relato registrado' })
    await expect(confirmacao.getByText('Protocolo de acompanhamento (CAT)')).toBeVisible()
    await expect(confirmacao.locator('.sp-protocolo-numero')).toHaveText(`CAT-${diaIso().slice(0, 7)}-0042`)
    await expect(confirmacao.getByText(`${ddmmaaaa(diaIso(-1))} 14:37`)).toBeVisible()
    await expect(confirmacao.getByText(`${ddmmaaaa(diaIso())} 10:15`)).toBeVisible()
    await expect(confirmacao.getByText('Gestor de Segurança notificado')).toBeVisible()

    expect(ultimo(estado, 'POST', '/ocorrencias')!.corpo).toEqual({
      tipoOcorrencia: 'ACIDENTE', dataOcorrencia: `${diaIso(-1)}T14:37:00`, descricao: 'Prensa atingiu a mão do operador.',
      areaId: 10, colaboradorId: 7, causaRaiz: 'FALHA_EPI', gravidade: 'CRITICO', tipo: 'Esmagamento',
    })

    await confirmacao.getByRole('button', { name: 'Acompanhar relato' }).click()
    const detalhe = page.getByRole('dialog', { name: 'Ocorrência #100' })
    await expect(detalhe).toBeVisible()
    await expect(page).toHaveURL(/ocorrencia=100/)
    await expect(detalhe.getByRole('group', { name: 'Ações do Gestor de Segurança' })).toHaveCount(0)
  })

  test('incidente pede situação de risco e potencial de dano e é acompanhado pelo número do registro', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const estado = await simularApi(page)
    await page.goto('/ocorrencias')
    await page.getByRole('button', { name: 'Relatar ocorrência' }).click()
    const form = formulario(page)
    await form.getByRole('radio', { name: /^Incidente/ }).check()
    await expect(form.getByLabel('Causa *')).toHaveCount(0)
    await preencherBase(page)
    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await expect(form.getByText('Descreva a situação de risco observada.')).toBeVisible()
    await expect(form.getByText('Informe o potencial de dano.')).toBeVisible()

    await form.getByLabel('Situação de risco *').fill('Piso escorregadio')
    await form.getByLabel('Potencial de dano *').fill('Queda com fratura')
    await form.getByRole('button', { name: 'Enviar relato' }).click()

    const confirmacao = page.getByRole('dialog', { name: 'Relato registrado' })
    await expect(confirmacao.locator('.sp-protocolo-numero')).toHaveText('#100')
    await expect(confirmacao.getByText('Incidentes não recebem protocolo CAT')).toBeVisible()
    expect(ultimo(estado, 'POST', '/ocorrencias')!.corpo).toMatchObject({ tipoOcorrencia: 'INCIDENTE', situacaoRisco: 'Piso escorregadio', potencialDano: 'Queda com fratura' })
    expect(ultimo(estado, 'POST', '/ocorrencias')!.corpo).not.toHaveProperty('causaRaiz')
  })

  test('data do fato no futuro é barrada e recusa da API mantém os dados preenchidos', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const estado = await simularApi(page)
    await page.goto('/ocorrencias')
    await page.getByRole('button', { name: 'Relatar ocorrência' }).click()
    const form = formulario(page)
    await form.getByRole('radio', { name: /^Incidente/ }).check()
    await preencherBase(page)
    await form.getByLabel('Situação de risco *').fill('Piso escorregadio')
    await form.getByLabel('Potencial de dano *').fill('Queda')
    await form.getByLabel('Data e hora do fato *').fill(`${diaIso(2)}T08:00`)
    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await expect(form.getByText('A data do fato não pode estar no futuro.')).toBeVisible()
    await expect(form.getByLabel('Data e hora do fato *')).toBeFocused()

    await form.getByLabel('Data e hora do fato *').fill(`${diaIso(-1)}T08:00`)
    estado.falhas.push({ metodo: 'POST', caminho: /^\/ocorrencias$/, status: 400, mensagem: 'Colaborador não encontrado para o ID: 7' })
    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await expect(form.getByRole('alert').filter({ hasText: 'Relato não registrado' })).toContainText('Colaborador não encontrado para o ID: 7')
    await expect(form.getByLabel('Descrição dos fatos *')).toHaveValue('Prensa atingiu a mão do operador.')

    estado.falhas.push({ metodo: 'POST', caminho: /^\/ocorrencias$/, status: 503, mensagem: 'indisponível' })
    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await expect(form.getByRole('alert').filter({ hasText: 'Relato não registrado' })).toContainText('Os dados preenchidos foram mantidos')

    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await expect(page.getByRole('dialog', { name: 'Relato registrado' })).toBeVisible()
  })

  test('formulário funciona pelo teclado e Escape devolve o foco ao botão de relato', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page)
    await page.goto('/ocorrencias')
    const botao = page.getByRole('button', { name: 'Relatar ocorrência' })
    await botao.focus()
    await page.keyboard.press('Enter')
    const form = formulario(page)
    await expect(form.getByLabel('Data e hora do fato *')).toBeVisible()
    await form.getByRole('radio', { name: /^Acidente/ }).focus()
    await page.keyboard.press('Space')
    await expect(form.getByRole('radio', { name: /^Acidente/ })).toBeChecked()
    await page.keyboard.press('ArrowRight')
    await expect(form.getByRole('radio', { name: /^Incidente/ })).toBeChecked()
    await expect(form.getByLabel('Situação de risco *')).toBeVisible()
    await page.keyboard.press('Escape')
    await expect(page.getByRole('dialog', { name: 'Relatar ocorrência' })).not.toBeVisible()
    await expect(botao).toBeFocused()
  })
})

test.describe('Acompanhamento pelo Gestor (UC05/UC09, issue #100)', () => {
  test('novo relato aparece como notificação, sai da fila após a triagem e é arquivado com confirmação', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const estado = await simularApi(page, { ocorrencias: [ocorrenciaBase({ idOcorrencia: 1 }), ocorrenciaBase({ idOcorrencia: 2, statusOcorrencia: 'EM_TRIAGEM' })] })
    await page.goto('/ocorrencias')

    const painel = page.getByRole('region', { name: '1 novo relato aguardando triagem' })
    await expect(painel).toBeVisible()
    const link = page.getByRole('navigation', { name: 'Navegação principal' }).first().getByRole('link', { name: 'Ocorrências', exact: true })
    await expect(link).toHaveAccessibleDescription('1 novo relato aguardando triagem')

    await painel.getByRole('button', { name: 'Abrir novo relato 1 para triagem' }).click()
    const detalhe = page.getByRole('dialog', { name: 'Ocorrência #1' })
    await detalhe.getByRole('button', { name: 'Enviar para triagem' }).click()
    await expect(detalhe.getByText('Ocorrência enviada para triagem.')).toBeVisible()
    await expect(detalhe.locator('dd').getByText('Em triagem')).toBeVisible()
    await expect(painel).toHaveCount(0)
    await expect(link).not.toHaveAttribute('aria-describedby')

    await detalhe.getByRole('button', { name: 'Arquivar' }).click()
    await detalhe.getByRole('group', { name: 'Confirmar arquivamento' }).getByRole('button', { name: 'Cancelar' }).click()
    expect(ultimo(estado, 'PATCH', '/ocorrencias/1/arquivar')).toBeUndefined()
    await detalhe.getByRole('button', { name: 'Arquivar' }).click()
    await detalhe.getByRole('button', { name: 'Confirmar arquivamento' }).click()
    await expect(detalhe.getByText('Ocorrência arquivada', { exact: true })).toBeVisible()
    await expect(detalhe.getByRole('button', { name: 'Editar dados' })).toHaveCount(0)
    await page.keyboard.press('Escape')
    await expect(page.getByRole('row', { name: /#1 / })).toContainText('Arquivada')
  })

  test('notificação chega sem recarregar quando um relato é registrado na mesma sessão', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/ocorrencias')
    await expect(page.getByRole('heading', { name: 'Nenhuma ocorrência registrada' })).toBeVisible()
    await page.getByRole('button', { name: 'Relatar ocorrência' }).click()
    const form = formulario(page)
    await form.getByRole('radio', { name: /^Incidente/ }).check()
    await preencherBase(page)
    await form.getByLabel('Situação de risco *').fill('Piso escorregadio')
    await form.getByLabel('Potencial de dano *').fill('Queda')
    await form.getByRole('button', { name: 'Enviar relato' }).click()
    await page.getByRole('dialog', { name: 'Relato registrado' }).getByRole('button', { name: 'Fechar' }).click()
    await expect(page.getByRole('region', { name: '1 novo relato aguardando triagem' })).toBeVisible()
    await expect(page.getByRole('row', { name: /#100 / })).toBeVisible()
  })

  test('Gestor edita o relato com os dados atuais preenchidos e vê a alteração gravada', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const estado = await simularApi(page, { ocorrencias: [ocorrenciaBase({
      idOcorrencia: 3, tipoOcorrencia: 'ACIDENTE', statusOcorrencia: 'EM_TRIAGEM', numeroProtocoloCAT: 'CAT-2026-09-0003',
      causaRaiz: 'FATOR_HUMANO', gravidade: 'BAIXO', situacaoRisco: null, potencialDano: null, areaId: 10, areaNome: 'Linha de Prensas', colaboradorId: 7, colaboradorNome: 'João da Linha',
    })] })
    await page.goto('/ocorrencias?ocorrencia=3')
    await page.getByRole('dialog', { name: 'Ocorrência #3' }).getByRole('button', { name: 'Editar dados' }).click()

    const form = page.getByRole('form', { name: 'Editar ocorrência' })
    await expect(form.getByText('Tipo: Acidente')).toBeVisible()
    await expect(form.getByLabel('Setor *')).toHaveValue('1')
    await expect(form.getByLabel('Área de risco *')).toHaveValue('10')
    await expect(form.getByLabel('Causa *')).toHaveValue('FATOR_HUMANO')
    await expect(form.getByLabel('Data e hora do fato *')).toHaveValue(`${diaIso(-3)}T09:00`)
    await form.getByLabel('Descrição dos fatos *').fill('Óleo vazou da prensa e o operador escorregou.')
    await form.getByLabel('Gravidade *').selectOption({ label: 'Alto' })
    await form.getByRole('button', { name: 'Salvar alterações' }).click()

    const detalhe = page.getByRole('dialog', { name: 'Ocorrência #3' })
    await expect(detalhe.getByText('Óleo vazou da prensa e o operador escorregou.')).toBeVisible()
    await expect(detalhe.locator('dd').getByText('Alto', { exact: true })).toBeVisible()
    await expect(page.getByRole('status').filter({ hasText: 'Ocorrência #3 atualizada.' })).toBeVisible()
    expect(ultimo(estado, 'PUT', '/ocorrencias/3')!.corpo).toMatchObject({
      dataOcorrencia: `${diaIso(-3)}T09:00:00`, areaId: 10, colaboradorId: 7, causaRaiz: 'FATOR_HUMANO', gravidade: 'ALTO',
    })
  })
})

test.describe('Plano de ação (UC07, issue #100)', () => {
  const acidenteGrave = () => ocorrenciaBase({
    idOcorrencia: 5, tipoOcorrencia: 'ACIDENTE', statusOcorrencia: 'EM_TRIAGEM', numeroProtocoloCAT: 'CAT-2026-09-0005',
    causaRaiz: 'FALHA_EPI', gravidade: 'CRITICO', situacaoRisco: null, potencialDano: null,
  })

  test('Gestor cadastra ação validando medidas, prazo, responsável e alerta; recusa da API é exibida', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const estado = await simularApi(page, { ocorrencias: [acidenteGrave()] })
    await page.goto('/ocorrencias?ocorrencia=5&aba=plano')
    const detalhe = page.getByRole('dialog', { name: 'Ocorrência #5' })
    await expect(detalhe.getByRole('tab', { name: 'Plano de ação' })).toHaveAttribute('aria-selected', 'true')
    await expect(detalhe.getByText('o plano precisa de ao menos uma ação com medida preventiva')).toBeVisible()
    await expect(detalhe.getByText('Nenhuma ação definida para esta ocorrência.')).toBeVisible()

    await detalhe.getByRole('button', { name: 'Nova ação' }).click()
    const form = detalhe.getByRole('form', { name: 'Nova ação do plano' })
    await form.getByLabel('Prazo *').fill(diaIso(-1))
    await form.getByRole('checkbox', { name: 'Ativar alerta de prazo' }).check()
    await form.getByRole('button', { name: 'Cadastrar ação' }).click()
    await expect(form.getByText('Informe a medida corretiva, a preventiva ou ambas.')).toBeVisible()
    await expect(form.getByText('O prazo não pode ser anterior a hoje nem à data do fato.')).toBeVisible()
    await expect(form.getByText('Indique se o responsável é um colaborador ou um setor.')).toBeVisible()
    await expect(form.getByText('Com o alerta ativo, informe a antecedência em dias')).toBeVisible()
    await expect(form.getByLabel('Medida corretiva')).toBeFocused()

    await form.getByLabel('Medida corretiva').fill('Trocar a luva de proteção da prensa.')
    await form.getByLabel('Prazo *').fill(diaIso(2))
    await form.getByRole('radio', { name: 'Setor' }).check()
    await form.getByLabel('Setor responsável *').selectOption({ label: 'Galpão Principal (Bloco 1)' })
    await form.getByLabel('Antecedência do alerta em dias *').fill('3')
    estado.falhas.push({ metodo: 'POST', caminho: /planos-acao$/, status: 400, mensagem: 'Ocorrência grave exige ao menos uma ação preventiva.' })
    await form.getByRole('button', { name: 'Cadastrar ação' }).click()
    await expect(form.getByRole('alert').filter({ hasText: 'Ação não cadastrada' })).toContainText('Ocorrência grave exige ao menos uma ação preventiva.')

    await form.getByLabel('Medida preventiva').fill('Treinar a equipe no uso de luvas.')
    await form.getByRole('button', { name: 'Cadastrar ação' }).click()
    await expect(form).toHaveCount(0)
    const acao = detalhe.getByRole('article', { name: 'Ação #500' })
    await expect(acao).toContainText('Setor: Galpão Principal')
    await expect(acao).toContainText(ddmmaaaa(diaIso(2)))
    await expect(acao.getByText('Alerta de prazo')).toBeVisible()
    await expect(acao).toContainText(`3 dia(s) antes, a partir de ${ddmmaaaa(diaIso(-1))}`)
    expect(ultimo(estado, 'POST', '/ocorrencias/5/planos-acao')!.corpo).toEqual({
      medidasCorretivas: 'Trocar a luva de proteção da prensa.', medidasPreventivas: 'Treinar a equipe no uso de luvas.',
      prazo: diaIso(2), responsavelSetorId: 1, alertaAtivo: true, antecedenciaAlertaDias: 3,
    })
  })

  test('Gestor prorroga com justificativa, reatribui, configura alerta e conclui; histórico fica visível', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const estado = await simularApi(page, {
      ocorrencias: [acidenteGrave()],
      acoes: [{ id: 501, idOcorrencia: 5, medidasCorretivas: null, medidasPreventivas: 'Instalar proteção na prensa.', prazo: diaIso(-1),
        status: 'ABERTA', situacao: 'ABERTA', responsavelTipo: 'COLABORADOR', responsavelId: 7, responsavelNome: 'João da Linha',
        alertaAtivo: false, antecedenciaAlertaDias: null, gestorNome: 'Gestora Ana' }],
    })
    await page.goto('/ocorrencias?ocorrencia=5&aba=plano')
    const acao = page.getByRole('article', { name: 'Ação #501' })
    await expect(acao.getByText('Em atraso')).toBeVisible()
    await expect(page.getByRole('status').filter({ hasText: '1 ação' })).toContainText('1 em atraso')

    await acao.getByRole('button', { name: 'Prorrogar prazo' }).click()
    const prorrogar = acao.getByRole('form', { name: 'Prorrogar prazo da ação #501' })
    await prorrogar.getByLabel('Novo prazo *').fill(diaIso(10))
    await prorrogar.getByRole('button', { name: 'Prorrogar' }).click()
    await expect(prorrogar.getByText('A justificativa técnica é obrigatória para prorrogar.')).toBeVisible()
    await prorrogar.getByLabel('Justificativa técnica *').fill('Peça importada com entrega atrasada.')
    await prorrogar.getByRole('button', { name: 'Prorrogar' }).click()
    await expect(acao.getByText('Em atraso')).toHaveCount(0)
    await expect(acao).toContainText(ddmmaaaa(diaIso(10)))
    await acao.getByRole('button', { name: 'Histórico de prorrogações' }).click()
    await expect(acao.getByRole('list', { name: 'Prorrogações da ação #501' }))
      .toContainText(`De ${ddmmaaaa(diaIso(-1))} para ${ddmmaaaa(diaIso(10))}: Peça importada com entrega atrasada.`)

    await acao.getByRole('button', { name: 'Reatribuir responsável' }).click()
    const reatribuir = acao.getByRole('form', { name: 'Reatribuir responsável da ação #501' })
    await reatribuir.getByLabel('Novo colaborador responsável *').selectOption({ label: 'João da Linha' })
    await reatribuir.getByRole('button', { name: 'Reatribuir' }).click()
    await expect(reatribuir.getByText('Escolha um responsável diferente do atual.')).toBeVisible()
    await expect(reatribuir.getByLabel('Novo colaborador responsável *').locator('option')).toContainText(['Joana Ribeiro (Supervisor)'])
    await reatribuir.getByLabel('Novo colaborador responsável *').selectOption({ label: 'Maria Souza' })
    await reatribuir.getByRole('button', { name: 'Reatribuir' }).click()
    await expect(acao).toContainText('Colaborador: Maria Souza')
    await expect(page.getByRole('status').filter({ hasText: 'reatribuída a Maria Souza' })).toBeVisible()

    await acao.getByRole('button', { name: 'Configurar alerta' }).click()
    const alerta = acao.getByRole('form', { name: 'Configurar alerta da ação #501' })
    await alerta.getByRole('checkbox', { name: 'Alerta de prazo ativo' }).check()
    await alerta.getByLabel('Antecedência em dias *').fill('-2')
    await alerta.getByRole('button', { name: 'Salvar alerta' }).click()
    await expect(alerta.getByText('Informe a antecedência em dias (número inteiro, zero ou mais).')).toBeVisible()
    await alerta.getByLabel('Antecedência em dias *').fill('15')
    await alerta.getByRole('button', { name: 'Salvar alerta' }).click()
    await expect(acao.getByText('Alerta de prazo', { exact: true })).toBeVisible()
    await expect(acao.getByText('Prazo próximo')).toBeVisible()

    await acao.getByRole('button', { name: 'Concluir' }).click()
    await acao.getByRole('button', { name: 'Confirmar conclusão' }).click()
    await expect(acao.getByText('Concluída')).toBeVisible()
    await expect(acao.getByRole('button', { name: 'Prorrogar prazo' })).toHaveCount(0)
    await expect(acao.getByText('Alerta de prazo', { exact: true })).toHaveCount(0)
    expect(ultimo(estado, 'PATCH', '/planos-acao/501/status')!.corpo).toEqual({ status: 'CONCLUIDA' })
    expect(ultimo(estado, 'PATCH', '/planos-acao/501/responsavel')!.corpo).toEqual({ colaboradorId: 8 })
    expect(ultimo(estado, 'PATCH', '/planos-acao/501/alerta')!.corpo).toEqual({ ativo: true, antecedenciaDias: 15 })
  })

  test('Supervisor acompanha o plano e os prazos sem operações de alteração', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page, {
      ocorrencias: [ocorrenciaBase({ idOcorrencia: 6 })],
      acoes: [{ id: 601, idOcorrencia: 6, medidasCorretivas: 'Limpar o piso.', medidasPreventivas: null, prazo: diaIso(-2),
        status: 'ABERTA', situacao: 'ABERTA', responsavelTipo: 'SETOR', responsavelId: 2, responsavelNome: 'Doca de Carga',
        alertaAtivo: true, antecedenciaAlertaDias: 1, gestorNome: 'Gestora Ana' }],
    })
    await page.goto('/ocorrencias')
    await page.getByRole('button', { name: 'Ver plano de ação da ocorrência 6' }).click()
    const detalhe = page.getByRole('dialog', { name: 'Ocorrência #6' })
    const acao = detalhe.getByRole('article', { name: 'Ação #601' })
    await expect(acao).toContainText('Setor: Doca de Carga')
    await expect(acao.getByText('Em atraso')).toBeVisible()
    await expect(acao.getByText('Prazo vencido')).toBeVisible()
    await expect(detalhe.getByRole('button', { name: 'Nova ação' })).toHaveCount(0)
    await expect(acao.getByRole('button', { name: 'Concluir' })).toHaveCount(0)
    await expect(detalhe.getByRole('button', { name: 'Enviar para triagem' })).toHaveCount(0)
    await expect(page.getByRole('region', { name: /novo relato/ })).toHaveCount(0)
  })
})

test.describe('Persistência, teclado e larguras (issue #100)', () => {
  test('recarregar mantém a ocorrência aberta, a aba e os dados gravados', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { ocorrencias: [ocorrenciaBase({ idOcorrencia: 1 })] })
    await page.goto('/ocorrencias')
    await page.getByRole('button', { name: 'Ver detalhes da ocorrência 1', exact: true }).click()
    const detalhe = page.getByRole('dialog', { name: 'Ocorrência #1' })
    await detalhe.getByRole('button', { name: 'Enviar para triagem' }).click()
    await expect(detalhe.locator('dd').getByText('Em triagem')).toBeVisible()
    await detalhe.getByRole('tab', { name: 'Plano de ação' }).click()
    await detalhe.getByRole('button', { name: 'Nova ação' }).click()
    const form = detalhe.getByRole('form', { name: 'Nova ação do plano' })
    await form.getByLabel('Medida corretiva').fill('Sinalizar o corredor.')
    await form.getByLabel('Prazo *').fill(diaIso(5))
    await form.getByRole('radio', { name: 'Colaborador' }).check()
    await form.getByLabel('Colaborador responsável *').selectOption({ label: 'Maria Souza' })
    await form.getByRole('button', { name: 'Cadastrar ação' }).click()
    await expect(detalhe.getByRole('article', { name: 'Ação #500' })).toBeVisible()
    await expect(page).toHaveURL(/ocorrencia=1&aba=plano/)

    await page.reload()
    const recarregado = page.getByRole('dialog', { name: 'Ocorrência #1' })
    await expect(recarregado.getByRole('tab', { name: 'Plano de ação' })).toHaveAttribute('aria-selected', 'true')
    await expect(recarregado.getByRole('article', { name: 'Ação #500' })).toContainText('Colaborador: Maria Souza')
    await recarregado.getByRole('tab', { name: 'Plano de ação' }).focus()
    await page.keyboard.press('ArrowLeft')
    await expect(recarregado.getByRole('tab', { name: 'Dados do relato' })).toBeFocused()
    await expect(recarregado.locator('dd').getByText('Em triagem')).toBeVisible()
    await expect(page).not.toHaveURL(/aba=plano/)

    await page.keyboard.press('Escape')
    await expect(page).not.toHaveURL(/ocorrencia=/)
    await page.reload()
    await expect(page.getByRole('dialog', { name: 'Ocorrência #1' })).not.toBeVisible()
    await expect(page.getByRole('row', { name: /#1 / })).toContainText('Em triagem')
  })

  test('ocorrência inexistente na URL mostra aviso em vez de quebrar a tela', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/ocorrencias?ocorrencia=999')
    await expect(page.getByRole('dialog', { name: 'Ocorrência #999' }).getByRole('status'))
      .toContainText('não está entre os relatos que seu perfil pode consultar')
  })

  for (const width of [360, 768, 1344]) {
    test(`relato, confirmação e plano cabem em ${width} px sem rolagem horizontal`, async ({ page }) => {
      await page.setViewportSize({ width, height: 900 })
      await iniciarSessao(page, gestora)
      await simularApi(page, {
        ocorrencias: [ocorrenciaBase({ idOcorrencia: 1 })],
        acoes: [{ id: 700, idOcorrencia: 1, medidasCorretivas: 'Sinalizar o corredor com fita antiderrapante.', medidasPreventivas: null,
          prazo: diaIso(1), status: 'ABERTA', situacao: 'ABERTA', responsavelTipo: 'COLABORADOR', responsavelId: 8,
          responsavelNome: 'Maria Souza', alertaAtivo: true, antecedenciaAlertaDias: 2, gestorNome: 'Gestora Ana' }],
      })
      const semRolagem = () => page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)
      await page.goto('/ocorrencias')
      await expect(page.getByRole('row', { name: /#1 / })).toBeVisible()
      expect(await semRolagem()).toBe(true)

      await page.getByRole('button', { name: 'Relatar ocorrência' }).click()
      const form = formulario(page)
      await form.getByRole('radio', { name: /^Acidente/ }).check()
      await preencherBase(page)
      await form.getByLabel('Causa *').selectOption({ label: 'Fator humano' })
      await form.getByLabel('Gravidade *').selectOption({ label: 'Baixo' })
      expect(await semRolagem()).toBe(true)
      await form.getByRole('button', { name: 'Enviar relato' }).click()
      await expect(page.getByRole('dialog', { name: 'Relato registrado' })).toBeVisible()
      expect(await semRolagem()).toBe(true)
      await page.getByRole('dialog', { name: 'Relato registrado' }).getByRole('button', { name: 'Fechar' }).click()

      await page.getByRole('button', { name: 'Ver plano de ação da ocorrência 1', exact: true }).click()
      const acao = page.getByRole('article', { name: 'Ação #700' })
      await acao.getByRole('button', { name: 'Prorrogar prazo' }).click()
      await expect(acao.getByLabel('Justificativa técnica *')).toBeVisible()
      expect(await semRolagem()).toBe(true)
      const caixa = await page.getByRole('dialog', { name: 'Ocorrência #1' }).boundingBox()
      expect(caixa!.x).toBeGreaterThanOrEqual(0)
      expect(caixa!.x + caixa!.width).toBeLessThanOrEqual(width)
    })
  }
})
