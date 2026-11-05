// Testes do padrão Adapter, parte 2 (#231): um único adaptador `lerEpi` (Adapter) converte o
// `EpiResponse` da API (Adaptee) num único tipo `Epi` (Target) para todas as telas. Não usa navegador:
// `features/epis/epi.ts` não importa o cliente `http`, então o adaptador é exercitado diretamente.
// As telas que consomem o tipo (Client) seguem cobertas pelas specs de EPI, estoque, empréstimos,
// áreas de risco, tarefas e capacitações.

import { expect, test } from '@playwright/test'
import { classificacoesEpi, lerEpi, rotuloClassificacaoEpi, type Epi } from '../src/features/epis/epi'
import { rotuloDaClassificacao } from '../src/features/estoque/formatos'

/** `EpiResponse` completo, como o backend devolve em `GET /api/epis` (inclui lote e modelo). */
const respostaCompleta = {
  id: 7,
  nome: 'Capacete classe B',
  numeroCa: '12345',
  quantidade: 10,
  estoqueMinimo: 5,
  estoqueCritico: false,
  status: 'DISPONIVEL',
  dataValidadeCa: '2027-12-31',
  vidaUtilDias: 365,
  descricao: 'Casco em polietileno',
  classificacao: 'PROTECAO_DE_CABECA',
  localizacao: 'Almoxarifado A',
  loteId: 3,
  numeroLote: 'L-01',
  notaFiscal: 'NF-9',
  dataFabricacao: '2026-01-10',
  validadeLote: '2031-01-10',
  modeloId: 2,
  marca: 'Marca X',
}

test.describe('Adaptador lerEpi: leitura do contrato EpiResponse', () => {
  test('converte a resposta completa no tipo Epi único, sem os campos de lote e modelo', () => {
    const esperado: Epi = {
      id: 7,
      nome: 'Capacete classe B',
      numeroCa: '12345',
      quantidade: 10,
      estoqueMinimo: 5,
      estoqueCritico: false,
      status: 'DISPONIVEL',
      dataValidadeCa: '2027-12-31',
      descricao: 'Casco em polietileno',
      classificacao: 'PROTECAO_DE_CABECA',
      localizacao: 'Almoxarifado A',
    }
    expect(lerEpi(respostaCompleta)).toEqual(esperado)
  })

  test('lê o estoque crítico calculado pelo backend sem recalcular', () => {
    expect(lerEpi({ ...respostaCompleta, quantidade: 50, estoqueCritico: true }).estoqueCritico).toBe(true)
    expect(lerEpi({ ...respostaCompleta, quantidade: 1, estoqueCritico: false }).estoqueCritico).toBe(false)
  })

  test('aceita a resposta reduzida usada por áreas de risco e capacitações, com as reservas de cada campo', () => {
    const reduzida = { id: 1, nome: 'Luva', numeroCa: '777', quantidade: 4, estoqueMinimo: 2, status: 'ESGOTADO' }
    expect(lerEpi(reduzida)).toEqual({
      id: 1, nome: 'Luva', numeroCa: '777', quantidade: 4, estoqueMinimo: 2, status: 'ESGOTADO',
      estoqueCritico: false, dataValidadeCa: null, descricao: null, classificacao: null, localizacao: null,
    })
  })

  test('CA ausente ou em branco vira texto vazio, para a tela mostrar "não informado"', () => {
    expect(lerEpi({ ...respostaCompleta, numeroCa: undefined }).numeroCa).toBe('')
    expect(lerEpi({ ...respostaCompleta, numeroCa: '   ' }).numeroCa).toBe('')
    expect(lerEpi({ ...respostaCompleta, numeroCa: null }).numeroCa).toBe('')
  })

  test('textos em branco viram null, com a mesma regra das primitivas de leitura (#244)', () => {
    const epi = lerEpi({ ...respostaCompleta, dataValidadeCa: '', descricao: '  ', localizacao: '\t' })
    expect(epi.dataValidadeCa).toBeNull()
    expect(epi.descricao).toBeNull()
    expect(epi.localizacao).toBeNull()
  })

  test('aceita as nove classificações do enum e troca qualquer outro valor por null', () => {
    for (const classificacao of classificacoesEpi) {
      expect(lerEpi({ ...respostaCompleta, classificacao }).classificacao).toBe(classificacao)
    }
    for (const invalida of ['PROTECAO_DESCONHECIDA', 'protecao_de_cabeca', '', 3, null, undefined]) {
      expect(lerEpi({ ...respostaCompleta, classificacao: invalida }).classificacao).toBeNull()
    }
  })

  test('aceita as cinco situações do EPI', () => {
    for (const status of ['DISPONIVEL', 'EM_USO', 'EM_MANUTENCAO', 'ESGOTADO', 'DESCARTADO'] as const) {
      expect(lerEpi({ ...respostaCompleta, status }).status).toBe(status)
    }
  })

  test('recusa o que não é objeto com a mensagem do adaptador', () => {
    for (const valor of [null, undefined, [], 'EPI', 7]) {
      expect(() => lerEpi(valor)).toThrow('Registro de EPI inválido')
    }
  })

  test('recusa registro sem os campos obrigatórios ou com situação desconhecida', () => {
    const incompativeis = [
      { ...respostaCompleta, id: '7' },
      { ...respostaCompleta, nome: undefined },
      { ...respostaCompleta, quantidade: '10' },
      { ...respostaCompleta, estoqueMinimo: null },
      { ...respostaCompleta, status: 'VENCIDO' },
    ]
    for (const registro of incompativeis) {
      expect(() => lerEpi(registro)).toThrow('Registro de EPI incompatível com o contrato da API')
    }
  })
})

test.describe('Rótulos de classificação do contrato único', () => {
  test('cada classificação tem rótulo, e a tela de estoque usa o mesmo mapa da vinculação de tarefas', () => {
    expect(Object.keys(rotuloClassificacaoEpi).sort()).toEqual([...classificacoesEpi].sort())
    for (const classificacao of classificacoesEpi) {
      expect(rotuloDaClassificacao(classificacao)).toBe(rotuloClassificacaoEpi[classificacao])
    }
    expect(rotuloDaClassificacao('PROTECAO_RESPIRATORIA')).toBe('Proteção respiratória')
  })

  test('classificação ausente aparece como "Não informada"', () => {
    expect(rotuloDaClassificacao(null)).toBe('Não informada')
  })
})
