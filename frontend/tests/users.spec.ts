import { test, expect } from '@playwright/test'

for (const failure of ['missing endpoint', 'network unavailable']) {
  test(`keeps navigation available when ${failure}`, async ({ page }) => {
    await page.route('**/api/usuarios', route => failure === 'missing endpoint'
      ? route.fulfill({ status: 404, json: { message: 'Not found' } })
      : route.abort('internetdisconnected'))
    await page.goto('/usuarios')
    await expect(page.getByRole('alert')).toContainText('Não foi possível carregar os usuários.')
    await page.getByRole('link', { name: 'Ocorrências', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Ocorrências', exact: true })).toBeVisible()
  })
}

for (const [label, body] of [
  ['HTML instead of JSON', '<html>Not an API response</html>'],
  ['an incompatible user record', JSON.stringify([{ id: 1, nome: null }])],
]) {
  test(`keeps the page usable when the API returns ${label}`, async ({ page }) => {
    const errors: string[] = []
    page.on('pageerror', error => errors.push(error.message))
    await page.route('**/api/usuarios', route => route.fulfill({ contentType: 'application/json', body }))
    await page.goto('/usuarios')
    await expect(page.getByRole('alert')).toContainText('Não foi possível carregar os usuários.')
    await expect(page.getByRole('button', { name: 'Tentar novamente' })).toBeVisible()
    expect(errors).toEqual([])
  })
}

test('shows the user names returned by GET /api/usuarios without exposing extra fields', async ({ page }) => {
  await page.route('**/api/usuarios', route => route.fulfill({ json: [
    { id: 1, nome: 'Ana Silva', senha: 'never-render-this-password' },
    { id: 2, nome: 'Bruno Costa' },
  ] }))
  const request = page.waitForRequest('**/api/usuarios')
  await page.goto('/usuarios')
  const apiRequest = await request
  expect(apiRequest.method()).toBe('GET')
  expect(new URL(apiRequest.url()).origin).toBe(new URL(page.url()).origin)
  const table = page.getByRole('table', { name: 'Usuários cadastrados' })
  await expect(table.getByRole('cell', { name: 'Ana Silva', exact: true })).toBeVisible()
  await expect(table.getByRole('cell', { name: 'Bruno Costa', exact: true })).toBeVisible()
  await expect(table.getByRole('row')).toHaveCount(3)
  await expect(page.getByText('never-render-this-password')).toHaveCount(0)
})

test('announces a failed query and allows retrying without reloading the page', async ({ page }) => {
  const errors: string[] = []
  page.on('pageerror', error => errors.push(error.message))
  let available = false
  await page.route('**/api/usuarios', route => available
    ? route.fulfill({ json: [{ id: 3, nome: 'Carla Souza' }] })
    : route.fulfill({ status: 503, json: { message: 'Unavailable' } }))
  await page.goto('/usuarios')
  await expect(page.getByRole('alert')).toContainText('Não foi possível carregar os usuários.')
  await expect(page.getByRole('heading', { name: 'Nenhum usuário encontrado' })).toHaveCount(0)
  available = true
  await page.getByRole('button', { name: 'Tentar novamente' }).click()
  await expect(page.getByRole('cell', { name: 'Carla Souza' })).toBeVisible()
  await expect(page.getByRole('alert')).toHaveCount(0)
  expect(errors).toEqual([])
})

test('announces loading and distinguishes an empty response from an unfinished request', async ({ page }) => {
  let releaseResponse = () => {}
  const responseReady = new Promise<void>(resolve => { releaseResponse = resolve })
  await page.route('**/api/usuarios', async route => {
    await responseReady
    await route.fulfill({ json: [] })
  })
  await page.goto('/usuarios')
  try {
    await expect(page.getByRole('status')).toHaveText('Carregando usuários...')
    await expect(page.getByRole('heading', { name: 'Nenhum usuário encontrado' })).toHaveCount(0)
  } finally {
    releaseResponse()
  }
  await expect(page.getByRole('heading', { name: 'Nenhum usuário encontrado' })).toBeVisible()
  await expect(page.getByRole('table')).toHaveCount(0)
  await expect(page.getByText('Carregando usuários...')).toHaveCount(0)
})
