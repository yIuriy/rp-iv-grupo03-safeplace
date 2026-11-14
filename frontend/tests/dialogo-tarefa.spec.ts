// Testes do padrão State em features/tarefas/dialogoTarefa.ts (#246): a TasksPage guarda um único
// DialogoTarefa; a tarefa só é entregue ao diálogo que está aberto, e a mensagem de sucesso depende do
// diálogo que salvou. Não usa navegador.

import { expect, test } from '@playwright/test'
import type { Tarefa } from '../src/features/tarefas/api'
import {
  mensagemAoSalvar,
  nenhumDialogo,
  tarefaDoDialogo,
  type DialogoDaTarefa,
  type DialogoTarefa,
} from '../src/features/tarefas/dialogoTarefa'

const tarefa: Tarefa = {
  id: 7,
  descricao: 'Solda em espaço confinado',
  nivelPerigo: 'CRITICO',
  classificada: true,
  dataClassificacao: '2026-10-01T09:00:00',
  responsavelClassificacaoNome: 'Gestora Demonstração',
}

const rotulos = { BAIXO: 'Baixo', MEDIO: 'Médio', ALTO: 'Alto', CRITICO: 'Crítico' } as const

const dialogosDaTarefa: DialogoDaTarefa[] = ['editar', 'exigencias', 'classificar', 'epis', 'historico']

test.describe('State: diálogo único da TasksPage (#246)', () => {
  test('sem diálogo aberto, nenhum diálogo recebe tarefa', () => {
    for (const tipo of dialogosDaTarefa) {
      expect(tarefaDoDialogo(nenhumDialogo, tipo)).toBeNull()
    }
  })

  test('cadastro de nova tarefa não entrega tarefa a nenhum diálogo, nem ao de edição', () => {
    const novo: DialogoTarefa = { tipo: 'novo' }
    for (const tipo of dialogosDaTarefa) {
      expect(tarefaDoDialogo(novo, tipo)).toBeNull()
    }
  })

  test('só o diálogo aberto recebe a tarefa; os outros ficam fechados', () => {
    for (const aberto of dialogosDaTarefa) {
      const dialogo: DialogoTarefa = { tipo: aberto, tarefa }
      const comTarefa = dialogosDaTarefa.filter(tipo => tarefaDoDialogo(dialogo, tipo) !== null)
      expect(comTarefa).toEqual([aberto])
      expect(tarefaDoDialogo(dialogo, aberto)).toBe(tarefa)
    }
  })

  test('cada diálogo que salva tem a sua mensagem de sucesso', () => {
    expect(mensagemAoSalvar({ tipo: 'novo' }, tarefa, rotulos))
      .toBe('Tarefa cadastrada com sucesso: "Solda em espaço confinado".')
    expect(mensagemAoSalvar({ tipo: 'editar', tarefa }, tarefa, rotulos))
      .toBe('Tarefa atualizada com sucesso: "Solda em espaço confinado".')
    expect(mensagemAoSalvar({ tipo: 'classificar', tarefa }, tarefa, rotulos))
      .toBe('Grau de periculosidade atualizado para Crítico.')
  })

  test('mensagem dos EPIs concorda com a quantidade vinculada', () => {
    const epis: DialogoTarefa = { tipo: 'epis', tarefa }
    expect(mensagemAoSalvar(epis, { ...tarefa, episIds: [] }, rotulos))
      .toBe('EPIs da tarefa atualizados com sucesso (0 vinculados).')
    expect(mensagemAoSalvar(epis, { ...tarefa, episIds: [3] }, rotulos))
      .toBe('EPIs da tarefa atualizados com sucesso (1 vinculado).')
    expect(mensagemAoSalvar(epis, tarefa, rotulos))
      .toBe('EPIs da tarefa atualizados com sucesso (0 vinculados).')
  })

  test('diálogos só de consulta não têm mensagem de sucesso', () => {
    expect(mensagemAoSalvar(nenhumDialogo, tarefa, rotulos)).toBe('')
    expect(mensagemAoSalvar({ tipo: 'exigencias', tarefa }, tarefa, rotulos)).toBe('')
    expect(mensagemAoSalvar({ tipo: 'historico', tarefa }, tarefa, rotulos)).toBe('')
  })
})
