package br.edu.safeplace.backend.validacao.projecao;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** Sem EPI não há projeção: interrompe a cadeia. */
class EpiInformadoElo extends EloValidacao<ContextoProjecao> {

    private static final String MENSAGEM = "EPI é obrigatório para a projeção.";

    @Override
    protected void verificar(ContextoProjecao contexto, ResultadoValidacao resultado) {
        if (contexto.epi() == null) {
            resultado.interromper(Problema.comExcecao(MENSAGEM, () -> new IllegalArgumentException(MENSAGEM)));
        }
    }
}
