package br.edu.safeplace.backend.validacao.aptidao;

import br.edu.safeplace.backend.domain.capacitacao.exception.ColaboradorNaoAlocavelException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/**
 * Só quem tem o perfil de colaborador pode ser alocado. Supervisor e Gestor não passam, e as
 * pendências de capacitação deles não são listadas: a cadeia é interrompida.
 */
class ColaboradorAlocavelElo extends EloValidacao<AptidaoParaTarefa> {

    @Override
    protected void verificar(AptidaoParaTarefa aptidao, ResultadoValidacao resultado) {
        if (!aptidao.colaborador().podeSerAlocado()) {
            ColaboradorNaoAlocavelException excecao = new ColaboradorNaoAlocavelException(aptidao.colaborador().getNome());
            resultado.interromper(Problema.comExcecao(excecao.getMessage(), () -> excecao));
        }
    }
}
