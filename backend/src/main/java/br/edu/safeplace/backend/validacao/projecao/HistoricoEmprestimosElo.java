package br.edu.safeplace.backend.validacao.projecao;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** DS10, 2.1: a taxa de uso vem dos empréstimos até a data de referência. */
class HistoricoEmprestimosElo extends EloValidacao<ContextoProjecao> {

    @Override
    protected void verificar(ContextoProjecao contexto, ResultadoValidacao resultado) {
        if (contexto.historico().isEmpty()) {
            resultado.adicionar(PendenciaProjecao.de(contexto,
                    "não possui registros de empréstimo para apuração da frequência de uso."));
        }
    }
}
