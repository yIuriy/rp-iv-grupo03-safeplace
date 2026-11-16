// Fluxos integrados da entrada única de EPIs (#175): uma consulta de catálogo, cadastro e manutenção.

import { expect, test } from '@playwright/test'
import { gestora, iniciarSessao } from './helpers/sessao'

const epi = {
  id: 1, nome: 'Capacete classe B', numeroCa: '12345', quantidade: 10, estoqueMinimo: 5,
  estoqueCritico: false, status: 'DISPONIVEL', dataValidadeCa: '2027-12-31',
  descricao: null, classificacao: null, localizacao: 'Almoxarifado A',
}

test('unified route shares one catalog request between stock and maintenance tabs and preserves the old route', async ({ page }) => {
  await iniciarSessao(page, gestora)
  let consultasCatalogo = 0
  await page.route('**/api/epis', route => {
    consultasCatalogo += 1
    return route.fulfill({ json: [epi] })
  })
  await page.route('**/api/epis/1/manutencoes', route => route.fulfill({ json: [] }))

  await page.goto('/estoque')
  await expect(page).toHaveURL('/epis')
  await expect(page.getByRole('tab', { name: 'Catálogo e estoque' })).toHaveAttribute('aria-selected', 'true')
  await page.getByRole('tab', { name: 'Manutenção' }).click()
  await expect(page).toHaveTitle('EPIs — Manutenção | SafePlace')
  await expect(page.getByRole('tab', { name: 'Manutenção' })).toHaveAttribute('aria-selected', 'true')
  expect(consultasCatalogo).toBe(1)
})

test('Gestor cadastra EPI e registra manutenção pela tela unificada', async ({ page }) => {
  await iniciarSessao(page, gestora)
  let catalogo = [epi]
  await page.route('**/api/epis', route => {
    if (route.request().method() === 'GET') return route.fulfill({ json: catalogo })
    const novo = { ...epi, id: 2, nome: 'Luva nitrílica' }
    catalogo = [...catalogo, novo]
    return route.fulfill({ status: 201, json: novo })
  })
  let historico: object[] = []
  await page.route('**/api/epis/1/manutencoes', route => {
    if (route.request().method() === 'GET') return route.fulfill({ json: historico })
    const registrada = {
      id: 10, epiId: 1, dataManutencao: '2026-10-03T10:00:00', descricao: 'Inspeção',
      resultado: 'APROVADO', responsavelManutencao: 'Gestora Ana', statusAtualEpi: 'DISPONIVEL',
    }
    historico = [registrada]
    return route.fulfill({ status: 201, json: registrada })
  })

  await page.goto('/epis?aba=manutencao')
  await page.getByRole('button', { name: 'Cadastrar EPI' }).click()
  const cadastro = page.getByRole('dialog', { name: 'Cadastrar EPI' })
  await cadastro.getByLabel('Nome do EPI').fill('Luva nitrílica')
  await cadastro.getByLabel('Número do CA').fill('67890')
  await cadastro.getByLabel('Validade do CA').fill('2028-06-30')
  await cadastro.getByLabel('Quantidade inicial').fill('8')
  await cadastro.getByLabel('Estoque mínimo').fill('3')
  await cadastro.getByRole('button', { name: 'Cadastrar EPI' }).click()
  await expect(page.getByRole('status').filter({ hasText: 'EPI cadastrado' })).toBeVisible()

  await page.getByRole('button', { name: 'Consultar histórico de Capacete classe B' }).click()
  await page.getByRole('button', { name: 'Registrar manutenção' }).click()
  const manutencao = page.getByRole('dialog', { name: 'Registrar manutenção: Capacete classe B' })
  await manutencao.getByLabel('Data e hora da manutenção').fill('2026-10-03T10:00')
  await manutencao.getByLabel('Descrição da manutenção').fill('Inspeção')
  await manutencao.getByLabel('Resultado').selectOption('APROVADO')
  await manutencao.getByRole('button', { name: 'Salvar manutenção' }).click()
  await expect(page.getByRole('status').filter({ hasText: 'Manutenção registrada' })).toBeVisible()
  // A data vem cortada do texto ISO, no mesmo formato das outras telas, sem depender do fuso (#250).
  await expect(page.getByText('03/10/2026 10:00', { exact: true })).toBeVisible()
})

test('cadastro exige validade do CA antes de enviar à API (#197)', async ({ page }) => {
  await iniciarSessao(page, gestora)
  let cadastros = 0
  await page.route('**/api/epis', route => {
    if (route.request().method() === 'GET') return route.fulfill({ json: [epi] })
    cadastros += 1
    return route.fulfill({ status: 201, json: { ...epi, id: 2 } })
  })

  await page.goto('/epis?aba=manutencao')
  await page.getByRole('button', { name: 'Cadastrar EPI' }).click()
  const cadastro = page.getByRole('dialog', { name: 'Cadastrar EPI' })
  await cadastro.getByLabel('Nome do EPI').fill('Luva nitrílica')
  await cadastro.getByLabel('Número do CA').fill('67890')
  await cadastro.getByLabel('Quantidade inicial').fill('8')
  await cadastro.getByLabel('Estoque mínimo').fill('3')
  await cadastro.getByRole('button', { name: 'Cadastrar EPI' }).click()

  await expect(cadastro.getByLabel('Validade do CA')).toHaveJSProperty('validity.valueMissing', true)
  await expect(cadastro).toBeVisible()
  expect(cadastros).toBe(0)
})

test('filtro de manutenção lista apenas EPIs com CA vencido e não oferece situações não gravadas (#197)', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const vencido = { ...epi, id: 3, nome: 'Óculos com CA vencido', numeroCa: '55555', dataValidadeCa: '2020-01-31' }
  await page.route('**/api/epis', route => route.fulfill({ json: [epi, vencido] }))

  await page.goto('/epis?aba=manutencao')
  const filtro = page.getByLabel('Filtrar por situação')
  await expect(filtro.locator('option')).toHaveText(
    ['Todas as situações ativas', 'Disponível', 'Em manutenção', 'Esgotado', 'CA vencido'])

  await filtro.selectOption('CA_VENCIDO')
  await expect(page.getByRole('status').filter({ hasText: '1 EPI ativo encontrado' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Consultar histórico de Óculos com CA vencido' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Consultar histórico de Capacete classe B' })).toHaveCount(0)
})
