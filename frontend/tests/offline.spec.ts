// Testes do recorte offline de RNF08 (issue #127): consulta somente leitura de setores e áreas de
// risco a partir de cópia local por conta. A rede é simulada nas respostas HTTP (não em
// navigator.onLine) e o retorno dela é sinalizado pelo evento `online`.

import { test, expect, type Locator, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'

const galpao = { id: 1, nome: 'Galpão Principal', descricao: null, localizacao: 'Bloco 1', status: 'ALTO' }
const caldeiraria = { id: 2, nome: 'Caldeiraria', descricao: null, localizacao: 'Bloco 2', status: 'MEDIO' }

type Rede = { disponivel: boolean; setores: unknown[]; falhaDoServidor: boolean; liberarResposta?: Promise<void> }

async function simularRede(page: Page): Promise<Rede> {
  const rede: Rede = { disponivel: true, setores: [galpao], falhaDoServidor: false }
  await page.route('**/api/areas-risco', route => rede.disponivel ? route.fulfill({ json: [] }) : route.abort('internetdisconnected'))
  await page.route('**/api/epis', route => route.fulfill({ json: [] }))
  await page.route('**/api/setores', async route => {
    if (!rede.disponivel) return route.abort('internetdisconnected')
    await rede.liberarResposta
    if (rede.falhaDoServidor) return route.fulfill({ status: 503, json: {} })
    return route.fulfill({ json: rede.setores })
  })
  return rede
}

async function abrirSetores(page: Page) {
  await page.goto('/areas-risco')
  await page.getByRole('tab', { name: 'Setores físicos' }).click()
  return page.getByRole('tabpanel', { name: 'Setores físicos' })
}

async function recarregarSetores(page: Page) {
  await page.reload()
  await page.getByRole('tab', { name: 'Setores físicos' }).click()
  return page.getByRole('tabpanel', { name: 'Setores físicos' })
}

// Escopo no painel visível: a aba de áreas, oculta, também monta o próprio aviso.
const avisoOffline = (painel: Locator) => painel.locator('.sp-offline-banner')

test.describe('Consulta offline (RNF08, issue #127)', () => {
  test('sem cópia local a falha aparece como erro, sem aviso de dados locais', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const rede = await simularRede(page)
    rede.disponivel = false
    const painel = await abrirSetores(page)

    await expect(painel.getByRole('alert')).toContainText('Não foi possível carregar os setores.')
    await expect(avisoOffline(painel)).toHaveCount(0)
  })

  test('perda da rede mostra a última cópia com a data da sincronização e bloqueia o cadastro', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const rede = await simularRede(page)
    let painel = await abrirSetores(page)
    await expect(painel.getByRole('row', { name: /Galpão Principal/ })).toBeVisible()
    await expect(avisoOffline(painel)).toHaveCount(0)

    rede.disponivel = false
    painel = await recarregarSetores(page)

    await expect(painel.getByRole('row', { name: /Galpão Principal/ })).toBeVisible()
    await expect(avisoOffline(painel)).toContainText('Última sincronização realizada em')
    await expect(painel.getByRole('button', { name: 'Cadastrar setor' })).toBeDisabled()
  })

  test('retorno da rede só remove o aviso após o servidor confirmar a atualização', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const rede = await simularRede(page)
    await abrirSetores(page)
    rede.disponivel = false
    const painel = await recarregarSetores(page)
    await expect(avisoOffline(painel)).toBeVisible()

    let confirmar!: () => void
    rede.liberarResposta = new Promise<void>(resolver => { confirmar = resolver })
    rede.setores = [galpao, caldeiraria]
    rede.disponivel = true
    await page.evaluate(() => window.dispatchEvent(new Event('online')))

    await expect(avisoOffline(painel)).toBeVisible()
    await expect(painel.getByRole('row', { name: /Caldeiraria/ })).toHaveCount(0)

    confirmar()
    await expect(painel.getByRole('row', { name: /Caldeiraria/ })).toBeVisible()
    await expect(avisoOffline(painel)).toHaveCount(0)
  })

  test('falha ao atualizar após o retorno da rede mantém a cópia sinalizada como local', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const rede = await simularRede(page)
    await abrirSetores(page)
    rede.disponivel = false
    const painel = await recarregarSetores(page)

    rede.disponivel = true
    rede.falhaDoServidor = true
    await page.evaluate(() => window.dispatchEvent(new Event('online')))

    await expect(painel.getByRole('row', { name: /Galpão Principal/ })).toBeVisible()
    await expect(avisoOffline(painel)).toContainText('Última sincronização realizada em')
  })

  test('cópia local de uma conta não é exibida para outra conta', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const rede = await simularRede(page)
    await abrirSetores(page)

    rede.disponivel = false
    await iniciarSessao(page, supervisor)
    const painel = await recarregarSetores(page)

    await expect(painel.getByRole('alert')).toContainText('Não foi possível carregar os setores.')
    await expect(painel.getByRole('row', { name: /Galpão Principal/ })).toHaveCount(0)
  })
})
