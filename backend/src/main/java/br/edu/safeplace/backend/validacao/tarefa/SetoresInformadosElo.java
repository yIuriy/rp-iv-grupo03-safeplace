package br.edu.safeplace.backend.validacao.tarefa;

import java.util.Objects;

import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** A lista de setores pode ser vazia, mas não pode conter identificador nulo. */
class SetoresInformadosElo extends EloValidacao<Tarefa> {

    private static final String MENSAGEM = "Identificador de setor não pode ser nulo.";

    @Override
    protected void verificar(Tarefa tarefa, ResultadoValidacao resultado) {
        if (tarefa.getSetoresIds().stream().anyMatch(Objects::isNull)) {
            resultado.adicionar(Problema.comExcecao(MENSAGEM, () -> new IllegalArgumentException(MENSAGEM)));
        }
    }
}
