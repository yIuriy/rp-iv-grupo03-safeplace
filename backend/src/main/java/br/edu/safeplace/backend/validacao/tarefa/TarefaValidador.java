package br.edu.safeplace.backend.validacao.tarefa;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida a tarefa antes do cadastro e da atualização (UC11). Com a área e os setores como associações por
 * objeto (#260), a existência deles é conferida pelo caso de uso ao resolvê-los pelas portas, antes de montar a
 * tarefa; a cadeia confere as regras da própria tarefa. Cada problema lança a {@link IllegalArgumentException}
 * de antes, respondida com 400.
 */
@Component
public class TarefaValidador extends ValidadorEmCadeia<Tarefa> {

    public TarefaValidador() {
        super(new DescricaoObrigatoriaElo());
    }
}
