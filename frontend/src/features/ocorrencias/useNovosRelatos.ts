// Notificação interna de novos relatos ao Gestor de Segurança (UC09, passo 12; DS09,
// VisaoNotificacao): relatos ainda na situação Aberta aguardam triagem. A lista vem da API e é
// atualizada periodicamente, ao voltar o foco para a aba e quando a própria tela altera ocorrências.
// O canal é interno (decisão do plano de ação, seção 2.6): não há e-mail nem SMS.

import { useEffect, useState } from 'react'
import { EVENTO_OCORRENCIAS_ALTERADAS, listarOcorrencias, type OcorrenciaResumo } from './api'

export const INTERVALO_NOTIFICACAO_MS = 30_000

export type NovosRelatos = { relatos: OcorrenciaResumo[]; carregado: boolean }

function maisRecentesPrimeiro(a: OcorrenciaResumo, b: OcorrenciaResumo): number {
  return (b.dataRegistro ?? '').localeCompare(a.dataRegistro ?? '') || b.idOcorrencia - a.idOcorrencia
}

/** Com `ativo` falso (perfil sem triagem), nada é consultado. */
export function useNovosRelatos(ativo: boolean, intervaloMs = INTERVALO_NOTIFICACAO_MS): NovosRelatos {
  const [estado, setEstado] = useState<NovosRelatos>({ relatos: [], carregado: false })

  useEffect(() => {
    if (!ativo) return
    let controle = new AbortController()

    function consultar() {
      controle.abort()
      controle = new AbortController()
      const sinal = controle.signal
      listarOcorrencias({}, sinal)
        .then(lista => {
          if (sinal.aborted) return
          setEstado({ relatos: lista.filter(o => o.statusOcorrencia === 'ABERTA').sort(maisRecentesPrimeiro), carregado: true })
        })
        .catch(() => { /* Sem conexão: mantém a última contagem conhecida. */ })
    }

    consultar()
    const temporizador = window.setInterval(consultar, intervaloMs)
    const aoFocar = () => { if (document.visibilityState === 'visible') consultar() }
    window.addEventListener(EVENTO_OCORRENCIAS_ALTERADAS, consultar)
    document.addEventListener('visibilitychange', aoFocar)
    return () => {
      controle.abort()
      window.clearInterval(temporizador)
      window.removeEventListener(EVENTO_OCORRENCIAS_ALTERADAS, consultar)
      document.removeEventListener('visibilitychange', aoFocar)
    }
  }, [ativo, intervaloMs])

  return ativo ? estado : { relatos: [], carregado: false }
}
