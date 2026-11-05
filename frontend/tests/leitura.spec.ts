// Testes do padrão Adapter na leitura da API (#244): as primitivas de shared/api/leitura.ts têm uma
// semântica única, usada por todos os adaptadores lerX das features. Não usa navegador. Os adaptadores
// importam o cliente http, que depende do import.meta.env do Vite; por isso são exercitados pelas specs
// de cada tela (por exemplo, estoque.spec.ts), que simulam as respostas da API. A exceção é o `lerEpi`
// único (#231), que fica em features/epis/epi.ts, sem o cliente http, e é testado em leitura-epi.spec.ts.

import { expect, test } from '@playwright/test'
import {
  lista,
  listaOuVazia,
  numeroOuNulo,
  objeto,
  texto,
  textoOuNulo,
} from '../src/shared/api/leitura'

test.describe('Primitiva textoOuNulo', () => {
  test('devolve o texto com conteúdo exatamente como veio, sem trim', () => {
    expect(textoOuNulo('Luva')).toBe('Luva')
    expect(textoOuNulo('  Luva nitrílica ')).toBe('  Luva nitrílica ')
  })

  test('texto vazio ou só com espaços vira null', () => {
    for (const vazio of ['', ' ', '   ', '\t', '\n ']) expect(textoOuNulo(vazio)).toBeNull()
  })

  test('ausência e outros tipos viram null', () => {
    for (const valor of [null, undefined, 0, 12, true, {}, [], ['Luva']]) expect(textoOuNulo(valor)).toBeNull()
  })
})

test.describe('Primitiva texto', () => {
  test('devolve o texto com conteúdo e usa a reserva da tela no lugar de vazio ou ausente', () => {
    expect(texto('Bota', 'EPI 3')).toBe('Bota')
    expect(texto('   ', 'EPI 3')).toBe('EPI 3')
    expect(texto(undefined, 'EPI 3')).toBe('EPI 3')
    expect(texto(42, 'EPI 3')).toBe('EPI 3')
  })
})

test.describe('Primitiva numeroOuNulo', () => {
  test('devolve números como vieram, inclusive zero e negativos', () => {
    expect(numeroOuNulo(0)).toBe(0)
    expect(numeroOuNulo(-3)).toBe(-3)
    expect(numeroOuNulo(2.5)).toBe(2.5)
  })

  test('texto numérico, ausência e outros tipos viram null', () => {
    for (const valor of ['12', '', null, undefined, true, {}, []]) expect(numeroOuNulo(valor)).toBeNull()
  })
})

test.describe('Primitiva objeto', () => {
  test('devolve o objeto JSON para o adaptador ler os campos', () => {
    const dado = { id: 1, nome: 'Luva' }
    expect(objeto(dado, 'Registro inválido')).toBe(dado)
  })

  test('recusa null, lista e valor simples com a mensagem do adaptador', () => {
    for (const valor of [null, undefined, [], [{ id: 1 }], 'texto', 3, true]) {
      expect(() => objeto(valor, 'Registro de EPI inválido')).toThrow('Registro de EPI inválido')
    }
  })
})

test.describe('Primitivas lista e listaOuVazia', () => {
  const dobro = (item: unknown) => Number(item) * 2

  test('lista aplica o leitor a cada item e recusa o que não é lista', () => {
    expect(lista([1, 2, 3], dobro, 'Esperava uma lista')).toEqual([2, 4, 6])
    expect(lista([], dobro, 'Esperava uma lista')).toEqual([])
    for (const valor of [null, undefined, {}, 'a,b']) {
      expect(() => lista(valor, dobro, 'Esperava uma lista de EPIs')).toThrow('Esperava uma lista de EPIs')
    }
  })

  test('lista propaga a recusa do leitor de um item inválido', () => {
    const ler = (item: unknown) => objeto(item, 'Item inválido')
    expect(() => lista([{ id: 1 }, null], ler, 'Esperava uma lista')).toThrow('Item inválido')
  })

  test('listaOuVazia aplica o leitor e troca ausência ou outro tipo por lista vazia', () => {
    expect(listaOuVazia([1, 2], dobro)).toEqual([2, 4])
    for (const valor of [null, undefined, {}, 'x', 7]) expect(listaOuVazia(valor, dobro)).toEqual([])
  })
})
