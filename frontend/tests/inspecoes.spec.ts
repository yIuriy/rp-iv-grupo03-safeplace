// Testes da aba "Inspeções" de /areas-risco (RF09, US09, issue #136): histórico com itens verificados,
// registro com validação local e envio à API, erro do servidor, falha de rede, largura de 360 px e
// ausência da aba para o Supervisor. Somente as respostas HTTP são simuladas.

import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

const area = {
  id: 1,
  codigo: 'SET-01',
  nome: 'Linha de Solda',
  descricao: null,
  nivelPerigo: 'ALTO',
  setorId: null,
  agentesRisco: ['Ruído'],
  limites: null,
  episObrigatorios: [{ id: 1, nome: 'Capacete de segurança', numeroCa: '12345', status: 'DISPONIVEL' }],
}

// Formato de `InspecaoResponse`, da data mais recente para a mais antiga, como a API devolve.
const inspecaoSetembro = {
  id: 2,
  areaRiscoId: 1,
  dataInspecao: '2026-09-30',
  responsavelId: 1,
  responsavelNome: 'Gestora Ana',
  naoConformidades: 1,
  itens: [
    { id: 3, descricao: 'NR-12: proteções fixas das máquinas', conformidade: true, observacoes: null },
    { id: 4, descricao: 'NR-26: sinalização de segurança', conformidade: false, observacoes: 'Faixa de circulação apagada' },
  ],
}

const inspecaoAgosto = {
  id: 1,
  areaRiscoId: 1,
  dataInspecao: '2026-08-15',
  responsavelId: 1,
  responsavelNome: 'Gestora Ana',
  naoConformidades: 0,
  itens: [{ id: 1, descricao: 'NR-26: sinalização de segurança', conformidade: true, observacoes: null }],
}

function dataLocal(deslocamentoDias: number): string {
  const data = new Date()
  data.setDate(data.getDate() + deslocamentoDias)
  const doisDigitos = (valor: number) => String(valor).padStart(2, '0')
  return `${data.getFullYear()}-${doisDigitos(data.getMonth() + 1)}-${doisDigitos(data.getDate())}`
}

async function simularApi(page: Page, opcoes: { falhaConsulta?: boolean; erroRegistro?: string } = {}) {
  const registros: unknown[] = []
  let consultas = 0
  let inspecoes = [inspecaoSetembro, inspecaoAgosto]
  await page.route('**/api/setores', route => route.fulfill({ json: [] }))
  await page.route('**/api/epis', route => route.fulfill({ json: [] }))
  await page.route('**/api/areas-risco', route => route.fulfill({ json: [area] }))
  await page.route('**/api/areas-risco/*/inspecoes', route => {
    const req = route.request()
    if (req.method() === 'GET') {
      consultas += 1
      return opcoes.falhaConsulta
        ? route.fulfill({ status: 500, json: { message: 'falha' } })
        : route.fulfill({ json: inspecoes })
    }
    registros.push(req.postDataJSON())
    if (opcoes.erroRegistro) {
      return route.fulfill({ status: 400, json: { message: opcoes.erroRegistro } })
    }
    const corpo = req.postDataJSON() as { dataInspecao: string; itens: { descricao: string; conformidade: boolean; observacoes?: string }[] }
    const nova = {
      id: 9, areaRiscoId: 1, dataInspecao: corpo.dataInspecao, responsavelId: 1, responsavelNome: 'Gestora Ana',
      naoConformidades: corpo.itens.filter(item => !item.conformidade).length,
      itens: corpo.itens.map((item, indice) => ({ id: 20 + indice, observacoes: null, ...item })),
    }
    inspecoes = [nova, ...inspecoes]
    return route.fulfill({ status: 201, json: nova })
  })
  return { registros, consultas: () => consultas }
}

async function abrirAba(page: Page) {
  await page.goto('/areas-risco')
  await page.getByRole('tab', { name: 'Inspeções' }).click()
  await page.getByLabel('Área de risco inspecionada').selectOption({ label: 'SET-01 - Linha de Solda' })
}

test.describe('Inspeções de áreas de risco (RF09, US09, #136)', () => {
  test('Gestor consulta o histórico com itens, resultados e observações', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/areas-risco')
    await page.getByRole('tab', { name: 'Inspeções' }).click()

    await expect(page.getByRole('heading', { name: 'Escolha uma área de risco' })).toBeVisible()
    await expect(page.getByRole('button', { name: 'Registrar inspeção' })).toBeDisabled()
    await page.getByLabel('Área de risco inspecionada').selectOption({ label: 'SET-01 - Linha de Solda' })

    const historico = page.getByRole('region', { name: 'Histórico de inspeções: SET-01 - Linha de Solda' })
    await expect(historico.getByText('2 inspeções registradas')).toBeVisible()
    const inspecoes = historico.getByRole('article')
    await expect(inspecoes).toHaveCount(2)
    const recente = inspecoes.first()
    await expect(recente.getByRole('heading', { name: 'Inspeção de 30/09/2026' })).toBeVisible()
    await expect(recente.getByText('Registrada por Gestora Ana; 2 itens verificados, 1 não conformidade.')).toBeVisible()
    const item = recente.getByRole('listitem').filter({ hasText: 'NR-26: sinalização de segurança' })
    await expect(item.getByText('Não conformidade', { exact: true })).toBeVisible()
    await expect(item.getByText('Faixa de circulação apagada')).toBeVisible()
    await expect(inspecoes.last().getByText('1 item verificado, nenhuma não conformidade.', { exact: false })).toBeVisible()
  })

  test('Gestor registra inspeção com dois itens após corrigir os campos destacados', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const api = await simularApi(page)
    await abrirAba(page)
    await expect(page.getByRole('article')).toHaveCount(2)

    await page.getByRole('button', { name: 'Registrar inspeção' }).click()
    const dialog = page.getByRole('dialog', { name: 'Registrar inspeção: SET-01' })
    await expect(dialog.getByLabel('Data da inspeção')).toHaveValue(dataLocal(0))

    await dialog.getByLabel('Data da inspeção').fill(dataLocal(1))
    await dialog.getByRole('button', { name: 'Registrar inspeção' }).click()
    await expect(dialog.getByText('A data da inspeção não pode ser posterior a hoje.')).toBeVisible()
    await expect(dialog.getByText('Informe a norma ou referência avaliada.')).toBeVisible()
    await expect(dialog.getByText('Indique conformidade ou não conformidade.')).toBeVisible()
    await expect(dialog.getByLabel('Data da inspeção')).toBeFocused()
    expect(api.registros).toHaveLength(0)

    await dialog.getByLabel('Data da inspeção').fill('2026-10-01')
    const item1 = dialog.getByRole('group', { name: 'Item verificado 1' })
    await item1.getByLabel('Norma ou referência avaliada').fill('NR-12: proteções fixas')
    await item1.getByLabel('Conformidade', { exact: true }).check()
    await dialog.getByRole('button', { name: 'Adicionar item verificado' }).click()
    const item2 = dialog.getByRole('group', { name: 'Item verificado 2' })
    await item2.getByLabel('Norma ou referência avaliada').fill('NR-26: sinalização')
    await item2.getByLabel('Não conformidade').check()
    await item2.getByLabel('Observações (opcional)').fill('Faixa apagada')
    await dialog.getByRole('button', { name: 'Registrar inspeção' }).click()

    await expect(dialog).not.toBeVisible()
    expect(api.registros).toEqual([{
      dataInspecao: '2026-10-01',
      itens: [
        { descricao: 'NR-12: proteções fixas', conformidade: true },
        { descricao: 'NR-26: sinalização', conformidade: false, observacoes: 'Faixa apagada' },
      ],
    }])
    await expect(page.getByRole('status').filter({ hasText: 'Inspeção de 01/10/2026 registrada' })).toBeVisible()
    await expect(page.getByRole('article')).toHaveCount(3)
    await expect(page.getByRole('article').first().getByRole('heading', { name: 'Inspeção de 01/10/2026' })).toBeVisible()
  })

  test('remover item e mensagem de recusa do servidor', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { erroRegistro: 'A data da inspeção não pode ser posterior a hoje.' })
    await abrirAba(page)

    await page.getByRole('button', { name: 'Registrar inspeção' }).click()
    const dialog = page.getByRole('dialog', { name: 'Registrar inspeção: SET-01' })
    await expect(dialog.getByRole('button', { name: /Remover item/ })).toHaveCount(0)
    await dialog.getByRole('button', { name: 'Adicionar item verificado' }).click()
    await dialog.getByRole('button', { name: 'Remover item 2' }).click()
    await expect(dialog.getByRole('group', { name: /Item verificado/ })).toHaveCount(1)

    const item = dialog.getByRole('group', { name: 'Item verificado 1' })
    await item.getByLabel('Norma ou referência avaliada').fill('NR-26')
    await item.getByLabel('Conformidade', { exact: true }).check()
    await dialog.getByRole('button', { name: 'Registrar inspeção' }).click()

    await expect(dialog.getByRole('alert')).toContainText('A data da inspeção não pode ser posterior a hoje.')
    await expect(dialog.getByLabel('Norma ou referência avaliada')).toHaveValue('NR-26')
  })

  test('falha ao carregar o histórico mostra aviso e permite tentar novamente', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const api = await simularApi(page, { falhaConsulta: true })
    await abrirAba(page)

    await expect(page.getByRole('alert')).toContainText('Não foi possível carregar as inspeções da área.')
    const antes = api.consultas()
    await page.getByRole('button', { name: 'Tentar novamente' }).click()
    await expect.poll(() => api.consultas()).toBe(antes + 1)
  })

  test('aba e formulário cabem em 360 px sem rolagem horizontal', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 900 })
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await abrirAba(page)
    await expect(page.getByRole('article')).toHaveCount(2)
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

    await page.getByRole('button', { name: 'Registrar inspeção' }).click()
    const dialog = page.getByRole('dialog', { name: 'Registrar inspeção: SET-01' })
    await dialog.getByRole('button', { name: 'Adicionar item verificado' }).click()
    await expect(dialog.getByRole('group', { name: 'Item verificado 2' })).toBeVisible()
    expect(await dialog.evaluate(elemento => elemento.scrollWidth <= elemento.clientWidth)).toBe(true)
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  })

  test('Supervisor não vê a aba de inspeções nem chama a API', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const api = await simularApi(page)
    await page.goto('/areas-risco')

    await expect(page.getByRole('tab', { name: 'Setores físicos' })).toBeVisible()
    await expect(page.getByRole('tab', { name: 'Inspeções' })).toHaveCount(0)
    expect(api.consultas()).toBe(0)
  })
})
