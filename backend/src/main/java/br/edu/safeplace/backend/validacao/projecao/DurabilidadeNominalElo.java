package br.edu.safeplace.backend.validacao.projecao;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** UC10, exceção I: sem vida útil em dias não há como projetar o desgaste. */
class DurabilidadeNominalElo extends EloValidacao<ContextoProjecao> {

    @Override
    protected void verificar(ContextoProjecao contexto, ResultadoValidacao resultado) {
        Integer vidaUtil = contexto.epi().getVidaUtilDias();
        if (vidaUtil == null || vidaUtil <= 0) {
            resultado.adicionar(PendenciaProjecao.de(contexto,
                    "não possui durabilidade nominal (vida útil em dias) cadastrada."));
        }
    }
}
