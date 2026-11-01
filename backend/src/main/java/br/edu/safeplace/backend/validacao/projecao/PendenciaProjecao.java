package br.edu.safeplace.backend.validacao.projecao;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.domain.epi.exception.DadosInsuficientesParaProjecaoException;
import br.edu.safeplace.backend.validacao.Problema;

/** Pendência da projeção (UC10, exceção I): o caso de uso a registra e segue para o próximo EPI. */
final class PendenciaProjecao {

    private PendenciaProjecao() {
    }

    /** Monta a mensagem no formato "EPI 'nome' (ID n) ..." e a exceção que o caso de uso já trata. */
    static Problema de(ContextoProjecao contexto, String descricao) {
        Integer epiId = contexto.epiId();
        String mensagem = String.format("EPI '%s' (ID %d) %s", contexto.epi().getNome(), epiId, descricao);
        return Problema.comExcecao(mensagem, () -> new DadosInsuficientesParaProjecaoException(epiId, mensagem));
    }
}
