// Lista de colaboradores para os seletores dos formulários de cadastro.

import { useConsulta } from '../../shared/api/useConsulta'
import { listarColaboradores } from './api'

const carregar = (signal: AbortSignal) => listarColaboradores(null, signal)

export function useColaboradoresOpcoes() {
  return useConsulta('capacitacoes-cadastro-colaboradores', carregar)
}
