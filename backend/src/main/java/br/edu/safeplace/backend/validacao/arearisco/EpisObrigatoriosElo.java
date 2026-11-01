package br.edu.safeplace.backend.validacao.arearisco;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** UC03, RN1: toda área de risco, de qualquer grau de perigo, exige EPIs obrigatórios de acesso. */
class EpisObrigatoriosElo extends EloValidacao<AreaRisco> {

    @Override
    protected void verificar(AreaRisco area, ResultadoValidacao resultado) {
        if (area.getEpisObrigatorios().isEmpty()) {
            AreaRiscoSemEpiObrigatorioException excecao = new AreaRiscoSemEpiObrigatorioException(area.getCodigo());
            resultado.adicionar(Problema.comExcecao(excecao.getMessage(), () -> excecao));
        }
    }
}
