import { test, expect } from '@playwright/test'

test('opens the application and navigates between its five features without reloading', async ({ page }) => {
  await page.route('**/api/usuarios', route => route.fulfill({ json: [] }))
  await page.goto('/')
  await expect(page).toHaveURL(/\/usuarios$/)
  await expect(page.getByRole('heading', { name: 'Usuários', exact: true })).toBeVisible()

  const documentRequests: string[] = []
  page.on('request', request => {
    if (request.isNavigationRequest()) documentRequests.push(request.url())
  })

  for (const [label, path] of [
    ['Ocorrências', '/ocorrencias'],
    ['EPIs', '/epis'],
    ['Áreas de risco', '/areas-risco'],
    ['Tarefas', '/tarefas'],
    ['Usuários', '/usuarios'],
  ]) {
    const link = page.getByRole('navigation', { name: 'Navegação principal' }).getByRole('link', { name: label, exact: true })
    await link.click()
    await expect(page).toHaveURL(path)
    await expect(page.getByRole('heading', { name: label, exact: true })).toBeVisible()
    await expect(link).toHaveAttribute('aria-current', 'page')
  }
  expect(documentRequests).toEqual([])
})

test('supports direct URLs, browser history and recovery from unknown routes', async ({ page }) => {
  await page.goto('/tarefas/')
  await expect(page).toHaveTitle('Tarefas | SafePlace')
  await page.reload()
  await expect(page.getByRole('heading', { name: 'Tarefas', exact: true })).toBeVisible()
  await page.getByRole('link', { name: 'EPIs', exact: true }).click()
  await expect(page.getByRole('main')).toBeFocused()
  await expect(page).toHaveTitle('EPIs | SafePlace')
  await page.goBack()
  await expect(page.getByRole('heading', { name: 'Tarefas', exact: true })).toBeVisible()
  await page.goForward()
  await expect(page.getByRole('heading', { name: 'EPIs', exact: true })).toBeVisible()
  await page.goto('/pagina-inexistente')
  await expect(page.getByRole('heading', { name: 'Página não encontrada' })).toBeVisible()
  await expect(page).toHaveTitle('Página não encontrada | SafePlace')
  await page.route('**/api/usuarios', route => route.fulfill({ json: [] }))
  await page.getByRole('link', { name: 'Voltar ao início' }).click()
  await expect(page).toHaveURL('/usuarios')
})

for (const width of [360, 768, 1344]) {
  test(`application navigation works with the keyboard at ${width}px`, async ({ page }, testInfo) => {
    const errors: string[] = []
    page.on('pageerror', error => errors.push(error.message))
    await page.setViewportSize({ width, height: 960 })
    await page.route('**/api/usuarios', route => route.fulfill({ json: [{ id: 1, nome: 'Ana Silva' }] }))
    await page.goto('/usuarios')
    await page.keyboard.press('Tab')
    await expect(page.getByRole('link', { name: 'Pular para o conteúdo' })).toBeFocused()
    await page.keyboard.press('Enter')
    await expect(page.getByRole('main')).toBeFocused()
    await expect(page.getByRole('cell', { name: 'Ana Silva' })).toBeVisible()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    await page.screenshot({ path: testInfo.outputPath(`users-${width}.png`), fullPage: true })

    for (const label of ['Ocorrências', 'EPIs', 'Áreas de risco', 'Tarefas']) {
      if (width < 1200) {
        await page.getByRole('button', { name: 'Menu', exact: true }).click()
        await expect(page.getByRole('dialog', { name: 'Navegação' })).toBeVisible()
      }
      const link = page.getByRole('navigation', { name: 'Navegação principal' }).getByRole('link', { name: label, exact: true })
      await link.focus()
      await page.keyboard.press('Enter')
      await expect(page.getByRole('heading', { name: label, exact: true })).toBeVisible()
      await expect(page.getByRole('dialog', { name: 'Navegação' })).not.toBeVisible()
      expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
    }
    expect(errors).toEqual([])
  })
}
