package br.edu.safeplace.backend.validacao.tarefa;

import java.util.Objects;

import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** Cada setor associado à tarefa precisa existir (relação N:N da V26). */
class SetoresExistentesElo extends EloValidacao<Tarefa> {

    private final SetorRepository setores;

    SetoresExistentesElo(SetorRepository setores) {
        this.setores = setores;
    }

    @Override
    protected void verificar(Tarefa tarefa, ResultadoValidacao resultado) {
        if (setores == null) {
            return;
        }
        tarefa.getSetoresIds().stream()
                .filter(Objects::nonNull)
                .filter(setorId -> setores.buscarPorId(setorId).isEmpty())
                .forEach(setorId -> {
                    String mensagem = "Setor não encontrado com id: " + setorId;
                    resultado.adicionar(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
                });
    }
}
