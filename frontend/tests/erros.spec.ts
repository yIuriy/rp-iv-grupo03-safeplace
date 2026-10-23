// Testes do padrão Strategy em shared/api/erros.ts (#232): o Context criarDescritorDeFalha escolhe
// a estratégia pelo status HTTP e cai na estratégia padrão quando não há regra. Não usa navegador.

import { expect, test } from '@playwright/test'
import { AxiosError, AxiosHeaders, type AxiosResponse } from 'axios'
import { criarDescritorDeFalha, usarMensagemDaApi } from '../src/shared/api/erros'

function falhaHttp(status: number, corpo?: unknown): AxiosError {
  const config = { headers: new AxiosHeaders() }
  const resposta = { status, statusText: '', headers: {}, config, data: corpo } as AxiosResponse
  return new AxiosError(`HTTP ${status}`, AxiosError.ERR_BAD_RESPONSE, config, undefined, resposta)
}

const descrever = criarDescritorDeFalha({
  403: 'Sem permissão.',
  409: usarMensagemDaApi('Conflito sem detalhe.'),
}, 'Falha genérica.')

test.describe('Context criarDescritorDeFalha', () => {
  test('usa a estratégia de texto fixo do status', () => {
    expect(descrever(falhaHttp(403, { message: 'ignorada' }))).toBe('Sem permissão.')
  })

  test('estratégia usarMensagemDaApi devolve a mensagem enviada pela API', () => {
    expect(descrever(falhaHttp(409, { message: 'CPF já cadastrado.' }))).toBe('CPF já cadastrado.')
  })

  test('estratégia usarMensagemDaApi usa o texto alternativo sem mensagem ou com mensagem em branco', () => {
    expect(descrever(falhaHttp(409))).toBe('Conflito sem detalhe.')
    expect(descrever(falhaHttp(409, { message: '   ' }))).toBe('Conflito sem detalhe.')
    expect(descrever(falhaHttp(409, '<html>erro</html>'))).toBe('Conflito sem detalhe.')
  })

  test('status sem estratégia própria usa a estratégia padrão', () => {
    expect(descrever(falhaHttp(500, { message: 'detalhe interno' }))).toBe('Falha genérica.')
  })

  test('falha sem resposta do servidor ou que não veio do axios usa a estratégia padrão', () => {
    expect(descrever(new AxiosError('Network Error', AxiosError.ERR_NETWORK))).toBe('Falha genérica.')
    expect(descrever(new Error('inesperado'))).toBe('Falha genérica.')
    expect(descrever(undefined)).toBe('Falha genérica.')
  })

  test('a estratégia padrão também pode consultar a mensagem da API', () => {
    const comPadraoDaApi = criarDescritorDeFalha({ 403: 'Sem permissão.' }, usarMensagemDaApi('Tente novamente.'))

    expect(comPadraoDaApi(falhaHttp(422, { message: 'Data futura não permitida.' }))).toBe('Data futura não permitida.')
    expect(comPadraoDaApi(falhaHttp(500))).toBe('Tente novamente.')
    expect(comPadraoDaApi(falhaHttp(403, { message: 'ignorada' }))).toBe('Sem permissão.')
  })
})
