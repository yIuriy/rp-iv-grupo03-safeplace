package br.edu.safeplace.backend.validacao.projecao;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** DS10, 2.1: o multiplicador de desgaste vem do nível de perigo do setor. */
class PericulosidadeSetorElo extends EloValidacao<ContextoProjecao> {

    @Override
    protected void verificar(ContextoProjecao contexto, ResultadoValidacao resultado) {
        if (contexto.periculosidadeSetor() == null) {
            resultado.adicionar(PendenciaProjecao.de(contexto,
                    "não possui setor com nível de periculosidade definido."));
        }
    }
}
