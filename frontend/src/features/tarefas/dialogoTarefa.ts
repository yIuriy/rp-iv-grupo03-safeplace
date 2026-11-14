// Padrão State (#246) da TasksPage: a tela está sempre em um único estado de diálogo. Antes, cinco
// useState<Tarefa | null> e uma flag de formulário aberto permitiam dois diálogos abertos ao mesmo
// tempo; com a união discriminada esse estado não cabe no tipo. Sem React e sem chamadas à API, para
// ser testado sem navegador (tests/dialogo-tarefa.spec.ts).

import type { NivelPerigo, Tarefa } from './api'

/** Diálogos que agem sobre uma tarefa já cadastrada. */
export type DialogoDaTarefa = 'editar' | 'exigencias' | 'classificar' | 'epis' | 'historico'

/** O diálogo aberto na TasksPage, ou nenhum. */
export type DialogoTarefa =
  | { tipo: 'nenhum' }
  | { tipo: 'novo' }
  | { tipo: DialogoDaTarefa; tarefa: Tarefa }

export const nenhumDialogo: DialogoTarefa = { tipo: 'nenhum' }

/** A tarefa do diálogo `tipo` quando ele é o aberto; nulo quando o aberto é outro (ou nenhum). */
export function tarefaDoDialogo(dialogo: DialogoTarefa, tipo: DialogoDaTarefa): Tarefa | null {
  return 'tarefa' in dialogo && dialogo.tipo === tipo ? dialogo.tarefa : null
}

/**
 * Mensagem de sucesso do diálogo que acabou de salvar. Exigências e histórico só consultam e não
 * salvam, então não têm mensagem.
 */
export function mensagemAoSalvar(
  dialogo: DialogoTarefa,
  salva: Tarefa,
  rotuloNivelPerigo: Record<NivelPerigo, string>,
): string {
  switch (dialogo.tipo) {
    case 'novo':
      return `Tarefa cadastrada com sucesso: "${salva.descricao}".`
    case 'editar':
      return `Tarefa atualizada com sucesso: "${salva.descricao}".`
    case 'classificar': {
      const grau = salva.nivelPerigo ? rotuloNivelPerigo[salva.nivelPerigo] : ''
      return `Grau de periculosidade atualizado para ${grau}.`
    }
    case 'epis': {
      const qtd = salva.episIds?.length ?? 0
      return `EPIs da tarefa atualizados com sucesso (${qtd} vinculado${qtd === 1 ? '' : 's'}).`
    }
    default:
      return ''
  }
}
