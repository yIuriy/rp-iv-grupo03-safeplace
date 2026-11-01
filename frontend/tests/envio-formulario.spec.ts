// Testes do Template Method em shared/formularios/envioFormulario.ts (#233): o método-modelo
// executarEnvio segue sempre a mesma sequência e chama as operações primitivas (ler, validar, ordem,
// enviar, descreverFalha) de cada formulário. Não usa navegador: as ações sobre a tela são registradas.

import { expect, test } from '@playwright/test'
import {
  camposInvalidos,
  executarEnvio,
  type AcoesDoEnvio,
  type EtapasDoEnvio,
} from '../src/shared/formularios/envioFormulario'

type Erros = { nome?: string; email?: string }
type Dados = { nome: string; email: string }

/** Ações que só anotam o que o esqueleto fez, na ordem em que fez. */
function registrarAcoes<E = Erros>() {
  const passos: string[] = []
  const acoes: AcoesDoEnvio<E> = {
    definirErros: erros => passos.push(`erros:${JSON.stringify(erros)}`),
    definirFalha: falha => passos.push(`falha:${falha}`),
    definirEnviando: enviando => passos.push(`enviando:${enviando}`),
    focar: campo => passos.push(`focar:${campo}`),
  }
  return { passos, acoes }
}

function etapas(lido: Dados, enviar: (dados: Dados) => Promise<void>): EtapasDoEnvio<Dados, Dados, Erros> {
  return {
    ler: () => lido,
    validar: dados => {
      const erros: Erros = {}
      if (!dados.nome) erros.nome = 'Informe o nome.'
      if (!dados.email) erros.email = 'Informe o e-mail.'
      return { dados: erros.nome || erros.email ? null : dados, erros }
    },
    ordem: ['nome', 'email'],
    enviar,
    descreverFalha: erro => `Falhou: ${(erro as Error).message}`,
  }
}

const formulario = {} as HTMLFormElement

test.describe('Método-modelo executarEnvio', () => {
  test('dados inválidos: mostra os erros, foca o primeiro campo da ordem e não envia', async () => {
    const { passos, acoes } = registrarAcoes()
    let enviados = 0
    const resultado = await executarEnvio(formulario, etapas({ nome: '', email: '' }, async () => { enviados++ }), acoes)

    expect(resultado).toBe('invalido')
    expect(enviados).toBe(0)
    expect(passos).toEqual([
      'erros:{"nome":"Informe o nome.","email":"Informe o e-mail."}',
      'focar:nome',
    ])
  })

  test('sucesso: limpa a falha anterior, envia e mantém "enviando" até o diálogo fechar', async () => {
    const { passos, acoes } = registrarAcoes()
    const recebidos: Dados[] = []
    const dados = { nome: 'Ana', email: 'ana@safeplace.com' }
    const resultado = await executarEnvio(formulario, etapas(dados, async d => { recebidos.push(d) }), acoes)

    expect(resultado).toBe('enviado')
    expect(recebidos).toEqual([dados])
    expect(passos).toEqual(['erros:{}', 'falha:', 'enviando:true'])
  })

  test('falha da API: descreve a falha e libera o botão para nova tentativa', async () => {
    const { passos, acoes } = registrarAcoes()
    const resultado = await executarEnvio(formulario, etapas({ nome: 'Ana', email: 'ana@safeplace.com' }, async () => {
      throw new Error('HTTP 500')
    }), acoes)

    expect(resultado).toBe('falhou')
    expect(passos).toEqual(['erros:{}', 'falha:', 'enviando:true', 'falha:Falhou: HTTP 500', 'enviando:false'])
  })

  test('dados ausentes (undefined) contam como inválidos, como em validarInspecao', async () => {
    const { passos, acoes } = registrarAcoes()
    const resultado = await executarEnvio(formulario, {
      ...etapas({ nome: 'Ana', email: '' }, async () => {}),
      validar: (): { erros: Erros } => ({ erros: { email: 'Informe o e-mail.' } }),
    }, acoes)

    expect(resultado).toBe('invalido')
    expect(passos).toEqual(['erros:{"email":"Informe o e-mail."}', 'focar:email'])
  })

  test('sem campo para focar (mensagem única), apenas registra os erros', async () => {
    const { passos, acoes } = registrarAcoes<{ geral?: string }>()
    const resultado = await executarEnvio(formulario, {
      ler: () => '',
      validar: () => ({ erros: { geral: 'A descrição da atividade é obrigatória.' } }),
      ordem: [],
      enviar: async () => {},
      descreverFalha: () => '',
    }, acoes)

    expect(resultado).toBe('invalido')
    expect(passos).toEqual(['erros:{"geral":"A descrição da atividade é obrigatória."}'])
  })
})

test.describe('Operação primitiva ordem', () => {
  test('lista de chaves: devolve só os campos com mensagem, na ordem da tela', () => {
    expect(camposInvalidos<Erros>(['nome', 'email'], { email: 'x' })).toEqual(['email'])
    expect(camposInvalidos<Erros>(['nome', 'email'], {})).toEqual([])
  })

  test('função: usada para erros aninhados, como os itens da inspeção', () => {
    type ErrosAninhados = { itens: Record<number, { descricao?: string }> }
    const ordem = (erros: ErrosAninhados) =>
      Object.entries(erros.itens).filter(([, e]) => e.descricao).map(([chave]) => `item-${chave}-descricao`)
    expect(camposInvalidos(ordem, { itens: { 2: { descricao: 'Informe.' }, 3: {} } })).toEqual(['item-2-descricao'])
  })
})
