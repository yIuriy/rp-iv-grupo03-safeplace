package br.edu.safeplace.backend.validacao.aptidao;

import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/**
 * UC11, exceção I, e US06, regra 3: tarefa sem grau de periculosidade não aceita alocação. A
 * cadeia continua, para a consulta também listar as pendências de capacitação.
 */
class TarefaClassificadaElo extends EloValidacao<AptidaoParaTarefa> {

    @Override
    protected void verificar(AptidaoParaTarefa aptidao, ResultadoValidacao resultado) {
        String descricao = aptidao.tarefa().getDescricao();
        if (!aptidao.tarefa().estaClassificada()) {
            resultado.adicionar(Problema.comExcecao(
                    "tarefa '" + descricao + "' sem grau de periculosidade classificado",
                    () -> new TarefaSemClassificacaoException(descricao)));
        }
    }
}
