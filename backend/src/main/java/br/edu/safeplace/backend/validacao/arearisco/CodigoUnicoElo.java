package br.edu.safeplace.backend.validacao.arearisco;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/**
 * UC03, exceção I: o código não pode pertencer a outra área. A própria área, na atualização, não
 * conta como duplicada.
 */
class CodigoUnicoElo extends EloValidacao<AreaRisco> {

    private final AreaRiscoRepository areas;

    CodigoUnicoElo(AreaRiscoRepository areas) {
        this.areas = areas;
    }

    @Override
    protected void verificar(AreaRisco area, ResultadoValidacao resultado) {
        String codigo = area.getCodigo();
        if (codigo == null || areas == null) {
            return;
        }
        areas.buscarPorCodigo(codigo)
                .filter(outra -> area.getId() == null || !area.getId().equals(outra.getId()))
                .ifPresent(outra -> {
                    CodigoAreaRiscoDuplicadoException excecao = new CodigoAreaRiscoDuplicadoException(codigo);
                    resultado.adicionar(Problema.comExcecao(excecao.getMessage(), () -> excecao));
                });
    }
}
