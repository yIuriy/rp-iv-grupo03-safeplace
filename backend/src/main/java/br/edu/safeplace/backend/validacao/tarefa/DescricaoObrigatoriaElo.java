package br.edu.safeplace.backend.validacao.tarefa;

import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** UC11: a tarefa precisa de descrição. Antes da issue #228, esta regra ficava no construtor. */
class DescricaoObrigatoriaElo extends EloValidacao<Tarefa> {

    private static final String MENSAGEM = "Descrição da tarefa é obrigatória.";

    @Override
    protected void verificar(Tarefa tarefa, ResultadoValidacao resultado) {
        if (tarefa.getDescricao() == null || tarefa.getDescricao().isBlank()) {
            resultado.adicionar(Problema.comExcecao(MENSAGEM, () -> new IllegalArgumentException(MENSAGEM)));
        }
    }
}
