import { test, expect, type Page } from '@playwright/test'
import { gestora, iniciarSessao, supervisor } from './helpers/sessao'
import { simularApi } from './helpers/capacitacoes'

const tabela = (page: Page) => page.getByRole('table', { name: 'Situação das capacitações por colaborador' })
const dialogo = (page: Page) => page.getByRole('dialog', { name: 'Capacitações do colaborador' })
const linha = (page: Page, nome: string) => tabela(page).getByRole('row').filter({ hasText: nome })

test.describe('Capacitações: consulta, alertas, cadastro e alocação (RF10, UC02, US10, RNF11)', () => {
  test('Gestor vê os colaboradores com a situação das certificações e a ausência de histórico', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/capacitacoes')

    await expect(page.getByRole('heading', { name: 'Capacitações', level: 1 })).toBeVisible()
    await expect(page.getByRole('link', { name: 'Capacitações' })).toHaveAttribute('aria-current', 'page')
    await expect(linha(page, 'Ana Souza').getByText('Vencida')).toBeVisible()
    await expect(linha(page, 'Bruno Lima').getByText('Próxima do vencimento')).toBeVisible()
    await expect(linha(page, 'Carla Dias').getByText('Sem certificações cadastradas')).toBeVisible()
    await expect(linha(page, 'Carla Dias').getByText('Pendência de integração e treinamentos admissionais')).toBeVisible()
    await expect(linha(page, 'Carla Dias').getByText('1 treinamento')).toBeVisible()
    await expect(linha(page, 'Ana Souza').getByText('2 certificações')).toBeVisible()
    // O cadastro fica na aba própria; a lista não tem ação de escrita.
    await expect(page.getByRole('tab', { name: 'Cadastro' })).toBeVisible()
    await expect(page.getByRole('button', { name: /Nova|Cadastrar|Registrar/ })).toHaveCount(0)
  })

  test('filtro por situação consulta a API, distingue as três situações e sobrevive à recarga', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const requisicoes = await simularApi(page)
    await page.goto('/capacitacoes')

    const filtro = page.getByLabel('Situação das certificações')
    await expect(filtro.locator('option')).toHaveText([
      'Todas, incluindo sem certificações', 'Em dia', 'Próximas do vencimento (até 30 dias)', 'Vencidas'])

    await filtro.selectOption('VENCIDA')
    await expect(tabela(page).getByRole('row')).toHaveCount(2)
    await expect(linha(page, 'Ana Souza')).toBeVisible()
    expect(requisicoes.at(-1)?.busca.get('situacao')).toBe('VENCIDA')
    await expect(page).toHaveURL(/situacao=VENCIDA/)

    await filtro.selectOption('PROXIMA_DO_VENCIMENTO')
    await expect(linha(page, 'Bruno Lima')).toBeVisible()
    await expect(linha(page, 'Ana Souza')).toHaveCount(0)

    await filtro.selectOption('EM_DIA')
    await expect(page.getByRole('heading', { name: 'Nenhum colaborador com certificações em dia' })).toBeVisible()

    await filtro.selectOption('VENCIDA')
    await page.reload()
    await expect(page.getByLabel('Situação das certificações')).toHaveValue('VENCIDA')
    await expect(linha(page, 'Ana Souza')).toBeVisible()

    await page.getByRole('button', { name: 'Limpar' }).click()
    await expect(linha(page, 'Davi Rocha')).toBeVisible()
    await expect(page).not.toHaveURL(/situacao=/)
  })

  test('ao abrir a tela, um aviso resume os alertas e leva à aba de alertas com o foco nela', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page)
    await page.goto('/capacitacoes')

    const aviso = page.getByRole('status').filter({ hasText: 'Alertas de capacitação' })
    await expect(aviso).toBeVisible()
    await expect(aviso.getByRole('listitem')).toHaveText([
      '1 certificação vencida', '1 certificação vence em até 30 dias', '2 colaboradores sem certificações'])

    await aviso.getByRole('button', { name: 'Ver alertas' }).click()
    const abaAlertas = page.getByRole('tab', { name: 'Alertas de vencimento' })
    await expect(abaAlertas).toHaveAttribute('aria-selected', 'true')
    await expect(abaAlertas).toBeFocused()
    await expect(page.getByRole('table', { name: 'Certificações vencidas' })).toBeVisible()
  })

  test('sem alertas, o aviso não aparece e a aba de alertas informa que está tudo em ordem', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { semAlertas: true })
    await page.goto('/capacitacoes')

    await expect(linha(page, 'Ana Souza')).toBeVisible()
    await expect(page.getByText('Alertas de capacitação')).toHaveCount(0)
    await page.getByRole('tab', { name: 'Alertas de vencimento' }).click()
    await expect(page.getByText('Nenhum alerta no momento.')).toBeVisible()
    await expect(page.getByText('Nenhuma certificação vencida.')).toBeVisible()
    await expect(page.getByText('Todos os colaboradores têm certificações cadastradas.')).toBeVisible()
  })

  test('busca por nome filtra a lista e informa quando nada corresponde', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/capacitacoes')

    await page.getByLabel('Nome do colaborador').fill('carla')
    await page.getByRole('button', { name: 'Buscar' }).click()
    await expect(tabela(page).getByRole('row')).toHaveCount(2)
    await expect(linha(page, 'Carla Dias')).toBeVisible()

    await page.getByLabel('Nome do colaborador').fill('Zuleica')
    await page.getByRole('button', { name: 'Buscar' }).click()
    await expect(page.getByRole('status').filter({ hasText: 'Nenhum colaborador corresponde ao nome informado' })).toBeVisible()
  })

  test('selecionar colaborador mostra certificações e treinamentos com os atributos aprovados', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/capacitacoes')

    await page.getByRole('button', { name: 'Consultar capacitações de Ana Souza' }).click()
    const dialog = dialogo(page)
    await expect(dialog.getByText('Certificações necessitam de renovação')).toBeVisible()
    await expect(dialog.getByText('necessitando de renovação imediata')).toBeVisible()

    const certificacoes = dialog.getByRole('table', { name: 'Certificações do colaborador' })
    await expect(certificacoes.getByRole('columnheader')).toHaveText(['Certificação', 'Emissão', 'Vencimento', 'Situação'])
    const vencida = certificacoes.getByRole('row').filter({ hasText: 'NR-35 Trabalho em Altura' })
    await expect(vencida).toContainText('04/10/2024')
    await expect(vencida).toContainText('04/10/2026')
    await expect(vencida).toContainText('Necessita de renovação')
    await expect(certificacoes.getByRole('row').filter({ hasText: 'NR-33' })).toContainText('Em dia')

    const treinamentos = dialog.getByRole('table', { name: 'Treinamentos do colaborador' })
    await expect(treinamentos.getByRole('columnheader')).toHaveText(['Treinamento', 'Instrutor', 'Realização', 'Carga horária', 'Vencimento', 'Situação'])
    const treino = treinamentos.getByRole('row').filter({ hasText: 'Integração de Segurança' })
    await expect(treino).toContainText('Normas internas e rotas de fuga')
    await expect(treino).toContainText('Eng. Paula')
    await expect(treino).toContainText('20/09/2026')
    await expect(treino).toContainText('8 h')
    await expect(treino).toContainText('Não vence')
    await expect(treino).toContainText('Realizado')
    // Só vence o treinamento cadastrado com validade (#192).
    const comValidade = treinamentos.getByRole('row').filter({ hasText: 'Primeiros Socorros' })
    await expect(comValidade).toContainText('01/09/2026')
    await expect(comValidade).toContainText('Vencido')
    await expect(comValidade).toContainText('Não atende a exigência da tarefa')
    await expect(page).toHaveURL(/colaborador=4/)
  })

  test('colaborador sem certificações tem a ausência e a pendência de integração informadas', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page)
    await page.goto('/capacitacoes?colaborador=6')

    const dialog = dialogo(page)
    await expect(dialog.getByText('Sem certificações cadastradas. Não há histórico de certificações')).toBeVisible()
    await expect(dialog.getByText('Pendência da trilha inicial de integração')).toBeVisible()
    await expect(dialog.getByText('Pendência de treinamentos admissionais')).toBeVisible()
    await expect(dialog.getByRole('table', { name: 'Treinamentos do colaborador' })).toContainText('Integração de Segurança')
  })

  test('aptidão mostra o bloqueio com os motivos do servidor, e a tentativa de alocação segue para o servidor', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const requisicoes = await simularApi(page)
    await page.goto('/capacitacoes?colaborador=4')

    const dialog = dialogo(page)
    const tarefa = dialog.getByLabel('Tarefa pretendida')
    await expect(tarefa.locator('option')).toHaveText(['Selecione', 'Montagem de andaime (Alto)', 'Inventário do almoxarifado (Baixo)',
      'Limpeza de silo (sem classificação)'])

    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await expect(dialog.getByText('Selecione a tarefa pretendida.')).toBeVisible()
    await expect(tarefa).toBeFocused()

    await tarefa.selectOption('30')
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    const bloqueio = dialog.getByText('Alocação impedida em "Montagem de andaime"')
    await expect(bloqueio).toBeVisible()
    await expect(dialog.getByText("certificação obrigatória 'NR-35 Trabalho em Altura' ausente ou vencida")).toBeVisible()
    await expect(dialog.getByText("treinamento obrigatório 'Resgate em Altura' pendente ou não realizado")).toBeVisible()
    const consulta = requisicoes.find(r => r.caminho === '/api/capacitacoes/colaboradores/4/aptidao')
    expect(consulta?.busca.get('tarefaId')).toBe('30')

    // Trocar a tarefa descarta o resultado anterior.
    await tarefa.selectOption('31')
    await expect(bloqueio).toHaveCount(0)
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await expect(dialog.getByText('Sem impedimentos para "Inventário do almoxarifado"')).toBeVisible()
    await expect(dialog.getByText('A alocação só é confirmada pelo servidor')).toBeVisible()

    // Tarefa sem classificação também impede a alocação (UC11, exceção I), com o motivo do servidor.
    await tarefa.selectOption('32')
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await expect(dialog.getByText('Alocação impedida em "Limpeza de silo"')).toBeVisible()
    await expect(dialog.getByText("tarefa 'Limpeza de silo' sem grau de periculosidade classificado")).toBeVisible()

    // A tentativa de alocação só existe depois da consulta e nada é gravado sem o clique.
    expect(requisicoes.filter(r => r.metodo !== 'GET')).toHaveLength(0)
  })

  test('tentar a alocação com capacitação vencida mostra o bloqueio do servidor com os motivos', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    const requisicoes = await simularApi(page, { alocacao: 'bloqueada' })
    await page.goto('/capacitacoes?colaborador=4')

    const dialog = dialogo(page)
    await expect(dialog.getByRole('button', { name: /^Alocar/ })).toHaveCount(0)
    await dialog.getByLabel('Tarefa pretendida').selectOption('30')
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await dialog.getByRole('button', { name: 'Alocar Ana Souza em Montagem de andaime' }).click()

    await expect(dialog.getByText('Alocação impedida por capacitação')).toBeVisible()
    await expect(dialog.getByText('Alocação bloqueada por pendências normativas de capacitação.')).toBeVisible()
    await expect(dialog.getByText("certificação obrigatória 'NR-35 Trabalho em Altura' ausente ou vencida").last()).toBeVisible()
    const tentativa = requisicoes.find(r => r.metodo === 'POST')
    expect(tentativa?.caminho).toBe('/api/tarefas/30/alocacoes')
    expect(tentativa?.corpo).toEqual({ colaboradorId: 4 })
  })

  test('alocação de colaborador apto é confirmada', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/capacitacoes?colaborador=5')

    const dialog = dialogo(page)
    await dialog.getByLabel('Tarefa pretendida').selectOption('31')
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await dialog.getByRole('button', { name: 'Alocar Bruno Lima em Inventário do almoxarifado' }).click()
    await expect(dialog.getByText('Alocação confirmada')).toBeVisible()
  })

  test('alocação de gestor ou supervisor é recusada com motivo próprio', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { alocacao: 'nao-alocavel' })
    await page.goto('/capacitacoes?colaborador=5')

    const dialog = dialogo(page)
    await dialog.getByLabel('Tarefa pretendida').selectOption('31')
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await dialog.getByRole('button', { name: /^Alocar Bruno Lima/ }).click()
    await expect(dialog.getByText('Esta pessoa não pode ser alocada')).toBeVisible()
    await expect(dialog.getByText('Somente colaboradores podem ser alocados em tarefas.')).toBeVisible()
  })

  test('falha da aptidão é explicada sem apagar a escolha da tarefa', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { falhaAptidao: 404 })
    await page.goto('/capacitacoes?colaborador=4')

    const dialog = dialogo(page)
    await dialog.getByLabel('Tarefa pretendida').selectOption('30')
    await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
    await expect(dialog.getByText('Não foi possível verificar a aptidão.')).toBeVisible()
    await expect(dialog.getByText('Tarefa não encontrada: 30')).toBeVisible()
    await expect(dialog.getByLabel('Tarefa pretendida')).toHaveValue('30')
  })

  test('aba de alertas separa vencidas, vencimentos em 30 dias e pendências e abre o colaborador', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/capacitacoes')

    await page.getByRole('tab', { name: 'Alertas de vencimento' }).click()
    await expect(page).toHaveURL(/aba=alertas/)
    await expect(page.getByText('Situação calculada em 04/10/2026. Alertas de vencimento com 30 dias')).toBeVisible()

    const vencidas = page.getByRole('table', { name: 'Certificações vencidas' })
    await expect(vencidas.getByRole('row').filter({ hasText: 'Ana Souza' })).toContainText('NR-35 Trabalho em Altura')
    const proximas = page.getByRole('table', { name: 'Vencem em até 30 dias' })
    await expect(proximas.getByRole('row').filter({ hasText: 'Bruno Lima' })).toContainText('03/11/2026')
    const pendencias = page.getByRole('region', { name: 'Sem certificações cadastradas' })
    await expect(pendencias.getByText('Carla Dias')).toBeVisible()
    await expect(pendencias.getByText('Davi Rocha')).toBeVisible()

    await page.getByRole('button', { name: 'Consultar capacitações de Bruno Lima (NR-10 Eletricidade)' }).click()
    await expect(dialogo(page).getByText('Pendências de capacitação')).toBeVisible()
    await expect(dialogo(page).getByRole('table', { name: 'Certificações do colaborador' })).toContainText('Próxima do vencimento')

    await page.reload()
    await expect(page.getByRole('tab', { name: 'Alertas de vencimento' })).toHaveAttribute('aria-selected', 'true')
    await expect(dialogo(page)).toBeVisible()
  })

  test('falhas de consulta permitem nova tentativa e colaborador inexistente é informado', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { falhaLista: 500 })
    await page.goto('/capacitacoes?colaborador=99')

    await expect(page.getByText('Não foi possível carregar as capacitações dos colaboradores.')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Tentar novamente' }).first()).toBeVisible()
    const dialog = dialogo(page)
    await expect(dialog.getByText('Colaborador não encontrado: 99')).toBeVisible()
    await dialog.getByRole('button', { name: 'Fechar' }).click()
    await expect(page).not.toHaveURL(/colaborador=/)
  })

  test('perfil sem permissão recebe explicação no detalhe', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { falhaDetalhe: 403 })
    await page.goto('/capacitacoes?colaborador=4')
    await expect(dialogo(page).getByText('Seu perfil não tem permissão para consultar capacitações.')).toBeVisible()
  })

  test('fluxo completo pelo teclado: filtro, abertura do detalhe, aptidão e Escape devolve o foco', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page)
    await page.goto('/capacitacoes')
    await expect(linha(page, 'Ana Souza')).toBeVisible()

    await page.getByLabel('Situação das certificações').focus()
    await page.keyboard.press('ArrowDown')
    await expect(page.getByLabel('Situação das certificações')).not.toHaveValue('')

    await page.getByRole('button', { name: 'Limpar' }).focus()
    await page.keyboard.press('Enter')
    const consultar = page.getByRole('button', { name: 'Consultar capacitações de Ana Souza' })
    await consultar.focus()
    await page.keyboard.press('Enter')
    const dialog = dialogo(page)
    await expect(dialog).toBeVisible()

    await dialog.getByLabel('Tarefa pretendida').focus()
    await page.keyboard.press('ArrowDown')
    await page.keyboard.press('Tab')
    await expect(dialog.getByRole('button', { name: 'Verificar aptidão' })).toBeFocused()
    await page.keyboard.press('Enter')
    await expect(dialog.getByText('Alocação impedida em "Montagem de andaime"')).toBeVisible()

    await page.keyboard.press('Escape')
    await expect(dialog).toBeHidden()
    await expect(consultar).toBeFocused()

    await page.getByRole('tab', { name: 'Colaboradores' }).focus()
    await page.keyboard.press('ArrowRight')
    await expect(page.getByRole('tab', { name: 'Alertas de vencimento' })).toBeFocused()
    await expect(page.getByRole('table', { name: 'Certificações vencidas' })).toBeVisible()
  })

  test('alertas incluem treinamentos vencidos e próximos do vencimento, e o aviso os resume', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { comAlertasTreinamento: true })
    await page.goto('/capacitacoes')

    const aviso = page.getByRole('status').filter({ hasText: 'Alertas de capacitação' })
    await expect(aviso.getByRole('listitem')).toHaveText([
      '1 certificação vencida', '1 certificação vence em até 30 dias', '2 colaboradores sem certificações',
      '1 treinamento vencido', '1 treinamento vence em até 30 dias'])

    await aviso.getByRole('button', { name: 'Ver alertas' }).click()
    const vencidos = page.getByRole('table', { name: 'Treinamentos vencidos' })
    await expect(vencidos.getByRole('row').filter({ hasText: 'Ana Souza' })).toContainText('Primeiros Socorros')
    await expect(vencidos.getByRole('row').filter({ hasText: 'Ana Souza' })).toContainText('01/09/2026')
    const proximos = page.getByRole('table', { name: 'Treinamentos que vencem em até 30 dias' })
    await expect(proximos.getByRole('row').filter({ hasText: 'Bruno Lima' })).toContainText('Brigada de Incêndio')
  })

  test('Supervisor não tem a aba de cadastro, nem pela URL', async ({ page }) => {
    await iniciarSessao(page, supervisor)
    await simularApi(page)
    await page.goto('/capacitacoes?aba=cadastro')

    await expect(page.getByRole('tab', { name: 'Cadastro' })).toHaveCount(0)
    await expect(page.getByRole('tab', { name: 'Colaboradores' })).toHaveAttribute('aria-selected', 'true')
    await expect(page.getByRole('heading', { name: 'Cadastrar certificação' })).toHaveCount(0)
  })

  test('Gestor cadastra certificação, treinamento com validade e exigência', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const requisicoes = await simularApi(page)
    await page.goto('/capacitacoes')
    await page.getByRole('tab', { name: 'Cadastro' }).click()
    await expect(page).toHaveURL(/aba=cadastro/)

    const certificacao = page.getByRole('region', { name: 'Cadastrar certificação' })
    await certificacao.getByLabel('Colaborador').selectOption('5')
    await certificacao.getByLabel('Certificação', { exact: true }).fill('NR-18 Obras')
    await certificacao.getByLabel('Data de emissão').fill('2026-01-10')
    await certificacao.getByLabel('Data de vencimento').fill('2028-01-10')
    await certificacao.getByRole('button', { name: 'Cadastrar certificação' }).click()
    await expect(certificacao.getByText('Certificação "NR-18 Obras" cadastrada.')).toBeVisible()
    await expect(certificacao.getByLabel('Certificação', { exact: true })).toHaveValue('')

    const treinamento = page.getByRole('region', { name: 'Cadastrar treinamento' })
    await treinamento.getByLabel('Treinamento', { exact: true }).fill('Primeiros Socorros')
    await treinamento.getByLabel('Data de realização').fill('2026-09-01')
    await treinamento.getByLabel('Validade em meses (opcional)').fill('12')
    await treinamento.getByRole('checkbox', { name: 'Ana Souza' }).check()
    await treinamento.getByRole('checkbox', { name: 'Bruno Lima' }).check()
    await treinamento.getByRole('button', { name: 'Cadastrar treinamento' }).click()
    await expect(treinamento.getByText('Treinamento "Primeiros Socorros" cadastrado.')).toBeVisible()

    const exigencia = page.getByRole('region', { name: 'Definir exigência' })
    await exigencia.getByLabel('Certificação ou treinamento exigido').fill('NR-35 Trabalho em Altura')
    await exigencia.getByLabel('Tipo').selectOption('CERTIFICACAO')
    await exigencia.getByLabel('Exigida para').selectOption('TAREFA')
    await exigencia.getByLabel('Tarefa', { exact: true }).selectOption('30')
    await exigencia.getByRole('button', { name: 'Definir exigência' }).click()
    await expect(exigencia.getByText('Exigência "NR-35 Trabalho em Altura" definida.')).toBeVisible()

    const posts = requisicoes.filter(r => r.metodo === 'POST')
    expect(posts.map(r => r.caminho)).toEqual(['/api/capacitacoes/certificacoes', '/api/capacitacoes/treinamentos', '/api/capacitacoes/exigencias'])
    expect(posts[0].corpo).toEqual({ nome: 'NR-18 Obras', dataEmissao: '2026-01-10', dataVencimento: '2028-01-10', colaboradorId: 5 })
    expect(posts[1].corpo).toEqual({ nome: 'Primeiros Socorros', dataRealizacao: '2026-09-01', validadeMeses: 12, colaboradoresIds: [4, 5] })
    expect(posts[2].corpo).toEqual({ nomeCapacitacao: 'NR-35 Trabalho em Altura', tipoCapacitacao: 'CERTIFICACAO', alvo: 'TAREFA', alvoId: 30 })
  })

  test('exigência para EPI lista os EPIs cadastrados', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const requisicoes = await simularApi(page)
    await page.goto('/capacitacoes?aba=cadastro')

    const exigencia = page.getByRole('region', { name: 'Definir exigência' })
    await exigencia.getByLabel('Certificação ou treinamento exigido').fill('Treinamento de Cinto')
    await exigencia.getByLabel('Tipo').selectOption('TREINAMENTO')
    await exigencia.getByLabel('Exigida para').selectOption('EPI')
    await exigencia.getByLabel('EPI', { exact: true }).selectOption('40')
    await exigencia.getByRole('button', { name: 'Definir exigência' }).click()
    await expect(exigencia.getByText('Exigência "Treinamento de Cinto" definida.')).toBeVisible()
    expect(requisicoes.find(r => r.metodo === 'POST')?.corpo)
      .toEqual({ nomeCapacitacao: 'Treinamento de Cinto', tipoCapacitacao: 'TREINAMENTO', alvo: 'EPI', alvoId: 40 })
  })

  test('cadastro com campos inválidos é barrado no navegador, com foco no primeiro erro', async ({ page }) => {
    await iniciarSessao(page, gestora)
    const requisicoes = await simularApi(page)
    await page.goto('/capacitacoes?aba=cadastro')

    const certificacao = page.getByRole('region', { name: 'Cadastrar certificação' })
    await certificacao.getByLabel('Data de vencimento').fill('2020-01-01')
    await certificacao.getByRole('button', { name: 'Cadastrar certificação' }).click()
    await expect(certificacao.getByText('Selecione o colaborador titular.')).toBeVisible()
    await expect(certificacao.getByText('Informe o nome da certificação.')).toBeVisible()
    await expect(certificacao.getByText('A data de vencimento deve ser posterior a hoje.')).toBeVisible()
    await expect(certificacao.getByLabel('Colaborador')).toBeFocused()

    const treinamento = page.getByRole('region', { name: 'Cadastrar treinamento' })
    await treinamento.getByLabel('Validade em meses (opcional)').fill('0')
    await treinamento.getByRole('button', { name: 'Cadastrar treinamento' }).click()
    await expect(treinamento.getByText('A validade deve ser de ao menos 1 mês.')).toBeVisible()
    await expect(treinamento.getByText('Selecione ao menos um colaborador.')).toBeVisible()

    const exigencia = page.getByRole('region', { name: 'Definir exigência' })
    await exigencia.getByRole('button', { name: 'Definir exigência' }).click()
    await expect(exigencia.getByText('Informe a certificação ou o treinamento exigido.')).toBeVisible()

    expect(requisicoes.filter(r => r.metodo === 'POST')).toHaveLength(0)
  })

  test('recusa do servidor no cadastro mantém os dados e mostra a mensagem da API', async ({ page }) => {
    await iniciarSessao(page, gestora)
    await simularApi(page, { falhaCadastro: 400 })
    await page.goto('/capacitacoes?aba=cadastro')

    const certificacao = page.getByRole('region', { name: 'Cadastrar certificação' })
    await certificacao.getByLabel('Colaborador').selectOption('5')
    await certificacao.getByLabel('Certificação', { exact: true }).fill('NR-18 Obras')
    await certificacao.getByLabel('Data de emissão').fill('2026-01-10')
    await certificacao.getByLabel('Data de vencimento').fill('2028-01-10')
    await certificacao.getByRole('button', { name: 'Cadastrar certificação' }).click()
    await expect(certificacao.getByText('Não foi possível cadastrar a certificação')).toBeVisible()
    await expect(certificacao.getByText('Data de vencimento não pode ser no passado ou presente no momento do cadastro.')).toBeVisible()
    await expect(certificacao.getByLabel('Certificação', { exact: true })).toHaveValue('NR-18 Obras')
  })

  for (const width of [320, 360, 768, 1344]) {
    test(`lista, detalhe e alertas cabem em ${width} px sem rolagem horizontal`, async ({ page }) => {
      await page.setViewportSize({ width, height: 900 })
      await iniciarSessao(page, gestora)
      await simularApi(page)
      const semRolagem = () => page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)

      await page.goto('/capacitacoes')
      await expect(linha(page, 'Ana Souza')).toBeVisible()
      expect(await semRolagem()).toBe(true)

      await page.goto('/capacitacoes?colaborador=4')
      const dialog = dialogo(page)
      await dialog.getByLabel('Tarefa pretendida').selectOption('30')
      await dialog.getByRole('button', { name: 'Verificar aptidão' }).click()
      await expect(dialog.getByText('Alocação impedida em "Montagem de andaime"')).toBeVisible()
      expect(await semRolagem()).toBe(true)
      const dialogoCabe = await dialog.evaluate(el => el.getBoundingClientRect().right <= window.innerWidth)
      expect(dialogoCabe).toBe(true)

      await page.goto('/capacitacoes?aba=alertas')
      await expect(page.getByRole('table', { name: 'Certificações vencidas' })).toBeVisible()
      expect(await semRolagem()).toBe(true)
    })
  }
})
