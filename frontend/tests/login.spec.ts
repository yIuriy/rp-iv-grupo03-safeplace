import { test, expect } from '@playwright/test'
import { gestora, iniciarSessao, sessaoGuardada, simularListagens } from './helpers/sessao'

const respostaLogin = { token: gestora.token, tipo: 'Bearer', email: gestora.email, nome: gestora.nome, perfil: gestora.perfil }

test('redirects anonymous visitors to the login page and returns them to the requested screen', async ({ page }) => {
  const chamadas = await simularListagens(page)
  let corpoEnviado: unknown
  await page.route('**/api/auth/login', route => {
    corpoEnviado = route.request().postDataJSON()
    return route.fulfill({ json: respostaLogin })
  })
  await page.goto('/ocorrencias')
  await expect(page).toHaveURL(/\/login$/)
  await expect(page).toHaveTitle('Entrar | SafePlace')
  await expect(page.getByRole('heading', { name: 'Entrar no SafePlace' })).toBeVisible()

  await page.getByLabel('E-mail').fill(gestora.email)
  await page.getByLabel('Senha').fill('Senha@123')
  await page.keyboard.press('Enter')
  await expect(page).toHaveURL('/ocorrencias')
  await expect(page.getByRole('heading', { name: 'Ocorrências', exact: true })).toBeVisible()
  expect(corpoEnviado).toEqual({ email: gestora.email, senha: 'Senha@123' })
  await expect(page.getByText('Gestora Ana · Gestor de Segurança')).toBeVisible()
  expect(await sessaoGuardada(page)).toMatchObject({ token: gestora.token, perfil: 'GESTOR_SEGURANCA' })

  await page.getByRole('navigation', { name: 'Navegação principal' }).getByRole('link', { name: 'Usuários', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Usuários', exact: true })).toBeVisible()
  await expect.poll(() => chamadas.length).toBe(1)
  expect(chamadas.map(chamada => chamada.authorization)).toEqual([`Bearer ${gestora.token}`])
})

test('keeps the typed e-mail and announces invalid credentials without leaving the page', async ({ page }) => {
  const errors: string[] = []
  page.on('pageerror', error => errors.push(error.message))
  await page.route('**/api/auth/login', route => route.fulfill({
    status: 401, json: { status: 401, error: 'Não Autorizado', message: 'Credenciais inválidas.' },
  }))
  await page.goto('/login')
  await page.getByLabel('E-mail').fill('alguem@safeplace.test')
  await page.getByLabel('Senha').fill('senha-errada')
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('alert')).toContainText('E-mail ou senha incorretos')
  await expect(page.getByLabel('E-mail')).toHaveValue('alguem@safeplace.test')
  await expect(page.getByLabel('Senha')).toBeFocused()
  await expect(page).toHaveURL(/\/login$/)
  expect(await sessaoGuardada(page)).toBeNull()
  expect(errors).toEqual([])
})

test('announces a connection failure and lets the user try again', async ({ page }) => {
  let disponivel = false
  await page.route('**/api/auth/login', route => disponivel
    ? route.fulfill({ json: respostaLogin })
    : route.abort('internetdisconnected'))
  await simularListagens(page)
  await page.goto('/login')
  await page.getByLabel('E-mail').fill(gestora.email)
  await page.getByLabel('Senha').fill('Senha@123')
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('alert')).toContainText('Não foi possível entrar agora')
  disponivel = true
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page).toHaveURL('/usuarios')
})

test('requires e-mail and password before calling the API', async ({ page }) => {
  let chamadas = 0
  await page.route('**/api/auth/login', route => { chamadas++; return route.fulfill({ json: respostaLogin }) })
  await page.goto('/login')
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByText('Informe o e-mail de acesso.')).toBeVisible()
  await expect(page.getByText('Informe a senha.')).toBeVisible()
  await expect(page.getByLabel('E-mail')).toBeFocused()
  await expect(page.getByLabel('E-mail')).toHaveAttribute('aria-invalid', 'true')
  expect(chamadas).toBe(0)
})

test('ends the session and explains it when the API rejects the stored token', async ({ page }) => {
  await iniciarSessao(page)
  await page.route('**/api/usuarios', route => route.fulfill({
    status: 401, json: { status: 401, error: 'Não Autorizado', message: 'Acesso não autenticado. Forneça um token JWT válido.' },
  }))
  await page.goto('/usuarios')
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('status')).toContainText('Sua sessão expirou')
  expect(await sessaoGuardada(page)).toBeNull()
})

test('signs out from the header and keeps protected screens closed afterwards', async ({ page }) => {
  await page.route('**/api/auth/login', route => route.fulfill({ json: respostaLogin }))
  await simularListagens(page)
  await page.goto('/usuarios')
  await page.getByLabel('E-mail').fill(gestora.email)
  await page.getByLabel('Senha').fill('Senha@123')
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByText('Gestora Ana · Gestor de Segurança')).toBeVisible()

  await page.getByRole('button', { name: 'Sair' }).click()
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('status')).toContainText('Você saiu do SafePlace')
  expect(await sessaoGuardada(page)).toBeNull()

  await page.goto('/usuarios')
  await expect(page).toHaveURL(/\/login$/)
  await expect(page.getByRole('heading', { name: 'Usuários', exact: true })).toHaveCount(0)
})

test('login form is labelled, keyboard operable and fits a 360px screen', async ({ page }) => {
  await page.setViewportSize({ width: 360, height: 780 })
  await page.route('**/api/auth/login', route => route.fulfill({ json: respostaLogin }))
  await simularListagens(page)
  await page.goto('/login')
  await page.keyboard.press('Tab')
  await expect(page.getByLabel('E-mail')).toBeFocused()
  await page.keyboard.type(gestora.email)
  await page.keyboard.press('Tab')
  await expect(page.getByLabel('Senha')).toBeFocused()
  await page.keyboard.type('Senha@123')
  await page.keyboard.press('Tab')
  await expect(page.getByRole('button', { name: 'Entrar' })).toBeFocused()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  await page.keyboard.press('Enter')
  await expect(page).toHaveURL('/usuarios')
  await expect(page.getByRole('button', { name: 'Sair' })).toBeVisible()
})
