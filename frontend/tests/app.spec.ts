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
