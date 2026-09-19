import { test, expect } from '@playwright/test'
import { gestora, iniciarSessao, pessoas, simularListagens, supervisor } from './helpers/sessao'

test('Gestor sees supervisors and colaboradores in tabs without any credential data', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const chamadas = await simularListagens(page, {
    usuarios: [pessoas.gestora, { ...pessoas.supervisora, senha: 'never-render-this-hash', senhaInicial: 'never-render-this-password' }, pessoas.colaborador],
    colaboradores: [pessoas.colaborador],
  })
  await page.goto('/usuarios')
  const abas = page.getByRole('tablist', { name: 'Tipo de cadastro' })
  await expect(abas.getByRole('tab', { name: 'Supervisores' })).toHaveAttribute('aria-selected', 'true')

  const supervisores = page.getByRole('table', { name: 'Supervisores cadastrados' })
  await expect(supervisores.getByRole('cell', { name: 'Joana Ribeiro', exact: true })).toBeVisible()
  await expect(supervisores.getByRole('cell', { name: '111.444.777-35', exact: true })).toBeVisible()
  await expect(supervisores.getByRole('cell', { name: '20/04/1988', exact: true })).toBeVisible()
  await expect(supervisores.getByRole('cell', { name: 'Ativo', exact: true })).toBeVisible()
  await expect(supervisores.getByRole('row')).toHaveCount(2)
  await expect(supervisores.getByRole('cell', { name: 'Gestora Ana', exact: true })).toHaveCount(0)
  await expect(page.getByText('never-render-this')).toHaveCount(0)

  await abas.getByRole('tab', { name: 'Colaboradores' }).click()
  const colaboradores = page.getByRole('table', { name: 'Colaboradores cadastrados' })
  await expect(colaboradores.getByRole('cell', { name: 'João da Silva', exact: true })).toBeVisible()
  await expect(colaboradores.getByRole('cell', { name: 'Não informado', exact: true })).toBeVisible()
  expect(chamadas.map(chamada => chamada.url.pathname)).toEqual(['/api/usuarios', '/api/usuarios/colaboradores'])
  expect(new Set(chamadas.map(chamada => chamada.authorization))).toEqual(new Set([`Bearer ${gestora.token}`]))
})

test('Supervisor only manages colaboradores', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularListagens(page, { colaboradores: [pessoas.colaborador] })
  await page.goto('/usuarios')
  await expect(page.getByText('Supervisor Bruno · Supervisor')).toBeVisible()
  await expect(page.getByRole('tablist')).toHaveCount(0)
  await expect(page.getByRole('button', { name: 'Novo supervisor' })).toHaveCount(0)
  await expect(page.getByRole('button', { name: 'Novo colaborador' })).toBeVisible()
  await expect(page.getByRole('cell', { name: 'João da Silva', exact: true })).toBeVisible()
  expect(chamadas.map(chamada => chamada.url.pathname)).toEqual(['/api/usuarios/colaboradores'])
})

test('searches colaboradores by name and CPF digits and distinguishes an empty search', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const chamadas = await simularListagens(page, {
    colaboradores: url => url.searchParams.has('nome') ? [] : [pessoas.colaborador],
  })
  await page.goto('/usuarios')
  await expect(page.getByRole('cell', { name: 'João da Silva', exact: true })).toBeVisible()

  const busca = page.getByRole('search', { name: 'Buscar colaboradores' })
  await busca.getByLabel('Nome').fill('Ana')
  await busca.getByLabel('CPF').fill('111.444.777-35')
  await busca.getByRole('button', { name: 'Buscar' }).click()
  await expect(page.getByRole('heading', { name: 'Nenhum colaborador encontrado' })).toBeVisible()
  await expect(page.getByRole('table')).toHaveCount(0)

  await busca.getByRole('button', { name: 'Limpar' }).click()
  await expect(page.getByRole('cell', { name: 'João da Silva', exact: true })).toBeVisible()
  await expect(busca.getByLabel('Nome')).toHaveValue('')
  expect(chamadas.map(chamada => chamada.url.search)).toEqual(['', '?nome=Ana&cpf=11144477735', ''])
})

test('Gestor creates a Supervisor and sees the initial password only once', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const supervisores: unknown[] = []
  const chamadas = await simularListagens(page, { usuarios: supervisores })
  let corpo: Record<string, unknown> | undefined
  await page.route('**/api/usuarios/supervisores', route => {
    corpo = route.request().postDataJSON()
    const registro = { ...pessoas.supervisora, nome: corpo?.nome }
    supervisores.push(registro)
    return route.fulfill({ status: 201, json: { ...registro, senhaInicial: 'Xk7!pQ2#mA9z' } })
  })
  await page.goto('/usuarios')
  await expect(page.getByRole('heading', { name: 'Nenhum supervisor cadastrado' })).toBeVisible()

  await page.getByRole('button', { name: 'Novo supervisor' }).click()
  const dialog = page.getByRole('dialog', { name: 'Novo supervisor' })
  await expect(dialog).toBeVisible()
  await expect(dialog.locator('input[type="password"]')).toHaveCount(0)
  await dialog.getByRole('button', { name: 'Cadastrar supervisor' }).click()
  await expect(dialog.getByText('Informe o nome completo.')).toBeVisible()
  await expect(dialog.getByText('Informe o e-mail corporativo usado no login.')).toBeVisible()
  await expect(dialog.getByLabel('Nome completo')).toBeFocused()
  expect(corpo).toBeUndefined()

  await dialog.getByLabel('Nome completo').fill('Joana Ribeiro')
  await dialog.getByLabel('CPF').fill('111.444.777-35')
  await dialog.getByLabel('Data de nascimento').fill('1988-04-20')
  await dialog.getByLabel('E-mail corporativo').fill('joana.ribeiro@safeplace.test')
  await dialog.getByRole('button', { name: 'Cadastrar supervisor' }).click()

  const resultado = page.getByRole('dialog', { name: 'Supervisor cadastrado' })
  await expect(resultado).toBeVisible()
  await expect(resultado.getByText('Xk7!pQ2#mA9z')).toBeVisible()
  await expect(resultado.getByText('não é exibida novamente')).toBeVisible()
  expect(corpo).toEqual({ nome: 'Joana Ribeiro', cpf: '111.444.777-35', dataNascimento: '1988-04-20', email: 'joana.ribeiro@safeplace.test' })

  await resultado.getByRole('button', { name: 'Concluir' }).click()
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await expect(page.getByText('Xk7!pQ2#mA9z')).toHaveCount(0)
  await expect(page.getByRole('table', { name: 'Supervisores cadastrados' }).getByRole('cell', { name: 'Joana Ribeiro', exact: true })).toBeVisible()
  expect(chamadas.filter(chamada => chamada.url.pathname === '/api/usuarios')).toHaveLength(2)

  await page.getByRole('button', { name: 'Novo supervisor' }).click()
  await expect(page.getByRole('dialog', { name: 'Novo supervisor' }).getByLabel('Nome completo')).toHaveValue('')
})

test('Colaborador form has no credential fields, shows API conflicts and confirms success', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const colaboradores: unknown[] = [pessoas.colaborador]
  const chamadas = await simularListagens(page, { colaboradores })
  const corpos: Record<string, unknown>[] = []
  await page.route('**/api/usuarios/colaboradores**', route => {
    if (route.request().method() !== 'POST') return route.fallback()
    corpos.push(route.request().postDataJSON())
    if (corpos.length === 1) {
      return route.fulfill({ status: 409, json: { status: 409, error: 'Conflito de Dados', message: 'CPF já cadastrado.' } })
    }
    const registro = { ...pessoas.colaborador, id: 4, nome: 'Maria Souza', cpf: '52998224725' }
    colaboradores.push(registro)
    return route.fulfill({ status: 201, json: registro })
  })
  await page.goto('/usuarios')
  await page.getByRole('button', { name: 'Novo colaborador' }).click()
  const dialog = page.getByRole('dialog', { name: 'Novo colaborador' })
  await expect(dialog).toBeVisible()
  await expect(dialog.getByText('Não recebe conta, senha nem acesso ao sistema.')).toBeVisible()
  await expect(dialog.locator('input[type="password"]')).toHaveCount(0)
  await expect(dialog.getByLabel(/senha|perfil/i)).toHaveCount(0)
  await expect(dialog.getByLabel('E-mail de contato (opcional)')).toBeVisible()

  await dialog.getByLabel('Nome completo').fill('Maria Souza')
  await dialog.getByLabel('CPF').fill('52998224725')
  await dialog.getByLabel('Data de nascimento').fill('1990-02-10')
  await dialog.getByRole('button', { name: 'Cadastrar colaborador' }).click()
  await expect(dialog.getByRole('alert')).toContainText('CPF já cadastrado.')
  await expect(dialog.getByLabel('Nome completo')).toHaveValue('Maria Souza')

  await dialog.getByRole('button', { name: 'Cadastrar colaborador' }).click()
  await expect(page.getByRole('dialog', { name: 'Novo colaborador' })).toHaveCount(0)
  await expect(page.getByText('Maria Souza cadastrado sem conta de acesso.')).toBeVisible()
  await expect(page.getByRole('table', { name: 'Colaboradores cadastrados' }).getByRole('cell', { name: 'Maria Souza', exact: true })).toBeVisible()
  expect(corpos).toHaveLength(2)
  for (const corpo of corpos) {
    expect(corpo).toEqual({ nome: 'Maria Souza', cpf: '52998224725', dataNascimento: '1990-02-10' })
  }
  expect(chamadas.filter(chamada => chamada.url.pathname === '/api/usuarios/colaboradores')).toHaveLength(2)
})

test('announces a failed colaboradores query and allows retrying without reloading', async ({ page }) => {
  const errors: string[] = []
  page.on('pageerror', error => errors.push(error.message))
  await iniciarSessao(page, supervisor)
  let disponivel = false
  await page.route('**/api/usuarios/colaboradores**', route => disponivel
    ? route.fulfill({ json: [pessoas.colaborador] })
    : route.fulfill({ status: 503, json: { message: 'Unavailable' } }))
  await page.goto('/usuarios')
  await expect(page.getByRole('alert')).toContainText('Não foi possível carregar os colaboradores.')
  await expect(page.getByRole('heading', { name: 'Nenhum colaborador cadastrado' })).toHaveCount(0)
  disponivel = true
  await page.getByRole('button', { name: 'Tentar novamente' }).click()
  await expect(page.getByRole('cell', { name: 'João da Silva', exact: true })).toBeVisible()
  await expect(page.getByRole('alert')).toHaveCount(0)
  expect(errors).toEqual([])
})

for (const [label, body] of [
  ['HTML instead of JSON', '<html>Not an API response</html>'],
  ['an incompatible record', JSON.stringify([{ id: 1, nome: null }])],
]) {
  test(`keeps the page usable when the API returns ${label}`, async ({ page }) => {
    const errors: string[] = []
    page.on('pageerror', error => errors.push(error.message))
    await iniciarSessao(page, gestora)
    await page.route('**/api/usuarios', route => route.fulfill({ contentType: 'application/json', body }))
    await page.goto('/usuarios')
    await expect(page.getByRole('alert')).toContainText('Não foi possível carregar os supervisores.')
    await expect(page.getByRole('button', { name: 'Tentar novamente' })).toBeVisible()
    await page.getByRole('link', { name: 'Ocorrências', exact: true }).click()
    await expect(page.getByRole('heading', { name: 'Ocorrências', exact: true })).toBeVisible()
    expect(errors).toEqual([])
  })
}

test('announces loading and distinguishes an empty response from an unfinished request', async ({ page }) => {
  await iniciarSessao(page, gestora)
  let releaseResponse = () => {}
  const responseReady = new Promise<void>(resolve => { releaseResponse = resolve })
  await page.route('**/api/usuarios', async route => {
    await responseReady
    await route.fulfill({ json: [] })
  })
  await page.goto('/usuarios')
  try {
    await expect(page.getByText('Carregando supervisores...')).toBeVisible()
    await expect(page.getByRole('heading', { name: 'Nenhum supervisor cadastrado' })).toHaveCount(0)
  } finally {
    releaseResponse()
  }
  await expect(page.getByRole('heading', { name: 'Nenhum supervisor cadastrado' })).toBeVisible()
  await expect(page.getByRole('table')).toHaveCount(0)
  await expect(page.getByText('Carregando supervisores...')).toHaveCount(0)
})

test('Gestor edits a Supervisor: CPF is read-only, PUT carries only editable fields and the list shows the saved data', async ({ page }) => {
  await iniciarSessao(page, gestora)
  const supervisores: Record<string, unknown>[] = [{ ...pessoas.supervisora }]
  const chamadas = await simularListagens(page, { usuarios: supervisores })
  let corpo: Record<string, unknown> | undefined
  await page.route('**/api/usuarios/supervisores/2', route => {
    corpo = route.request().postDataJSON()
    Object.assign(supervisores[0], corpo)
    return route.fulfill({ json: supervisores[0] })
  })
  await page.goto('/usuarios')
  await page.getByRole('button', { name: 'Editar Joana Ribeiro' }).click()

  const dialog = page.getByRole('dialog', { name: 'Editar supervisor' })
  await expect(dialog).toBeVisible()
  await expect(dialog.getByLabel('Nome completo')).toHaveValue('Joana Ribeiro')
  await expect(dialog.getByLabel('CPF')).toHaveValue('11144477735')
  await expect(dialog.getByLabel('CPF')).toHaveAttribute('readonly', '')
  await expect(dialog.getByLabel('E-mail corporativo')).toHaveValue('joana.ribeiro@safeplace.test')
  await expect(dialog.locator('input[type="password"]')).toHaveCount(0)

  await dialog.getByLabel('Nome completo').fill('Joana Ribeiro Souza')
  await dialog.getByLabel('E-mail corporativo').fill('joana.souza@safeplace.test')
  await dialog.getByRole('button', { name: 'Salvar alterações' }).click()

  await expect(page.getByRole('dialog', { name: 'Editar supervisor' })).toHaveCount(0)
  await expect(page.getByText('Joana Ribeiro Souza atualizado.')).toBeVisible()
  const tabela = page.getByRole('table', { name: 'Supervisores cadastrados' })
  await expect(tabela.getByRole('cell', { name: 'Joana Ribeiro Souza', exact: true })).toBeVisible()
  await expect(tabela.getByRole('cell', { name: 'joana.souza@safeplace.test', exact: true })).toBeVisible()
  expect(corpo).toEqual({ nome: 'Joana Ribeiro Souza', dataNascimento: '1988-04-20', email: 'joana.souza@safeplace.test' })
  expect(chamadas.filter(chamada => chamada.url.pathname === '/api/usuarios')).toHaveLength(2)

  await page.reload()
  await expect(page.getByRole('table', { name: 'Supervisores cadastrados' }).getByRole('cell', { name: 'Joana Ribeiro Souza', exact: true })).toBeVisible()
})

test('Supervisor edits a Colaborador and keeps the typed data when the API reports a conflict', async ({ page }) => {
  await iniciarSessao(page, supervisor)
  const colaboradores: Record<string, unknown>[] = [{ ...pessoas.colaborador }]
  await simularListagens(page, { colaboradores })
  const corpos: Record<string, unknown>[] = []
  await page.route('**/api/usuarios/colaboradores/3', route => {
    corpos.push(route.request().postDataJSON())
    if (corpos.length === 1) {
      return route.fulfill({ status: 409, json: { status: 409, error: 'Conflito de Dados', message: 'Email já cadastrado no sistema: joana.ribeiro@safeplace.test' } })
    }
    Object.assign(colaboradores[0], corpos[corpos.length - 1])
    return route.fulfill({ json: colaboradores[0] })
  })
  await page.goto('/usuarios')
  await page.getByRole('button', { name: 'Editar João da Silva' }).click()

  const dialog = page.getByRole('dialog', { name: 'Editar colaborador' })
  await expect(dialog).toBeVisible()
  await expect(dialog.getByText('continua sem conta de acesso')).toBeVisible()
  await expect(dialog.getByLabel('CPF')).toHaveAttribute('readonly', '')
  await expect(dialog.getByLabel(/senha|perfil/i)).toHaveCount(0)

  await dialog.getByLabel('Nome completo').fill('João Pedro da Silva')
  await dialog.getByLabel('E-mail de contato (opcional)').fill('joana.ribeiro@safeplace.test')
  await dialog.getByRole('button', { name: 'Salvar alterações' }).click()
  await expect(dialog.getByRole('alert')).toContainText('Email já cadastrado no sistema')
  await expect(dialog.getByLabel('Nome completo')).toHaveValue('João Pedro da Silva')

  await dialog.getByLabel('E-mail de contato (opcional)').fill('joao.pedro@safeplace.test')
  await dialog.getByRole('button', { name: 'Salvar alterações' }).click()
  await expect(page.getByRole('dialog', { name: 'Editar colaborador' })).toHaveCount(0)
  await expect(page.getByText('João Pedro da Silva atualizado.')).toBeVisible()
  await expect(page.getByRole('cell', { name: 'João Pedro da Silva', exact: true })).toBeVisible()
  expect(corpos).toHaveLength(2)
  for (const corpo of corpos) {
    expect(Object.keys(corpo).sort()).toEqual(['dataNascimento', 'email', 'nome'])
  }
})
