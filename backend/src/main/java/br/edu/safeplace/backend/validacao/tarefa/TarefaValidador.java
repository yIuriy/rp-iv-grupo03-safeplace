package br.edu.safeplace.backend.validacao.tarefa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida a tarefa antes do cadastro e da atualização (UC11, V26). A ordem dos elos repete a ordem
 * em que as regras eram verificadas antes da issue #228: relações primeiro, descrição por último.
 * Cada problema lança a {@link IllegalArgumentException} de antes, respondida com 400.
 */
@Component
public class TarefaValidador extends ValidadorEmCadeia<Tarefa> {

    /** As portas podem ser nulas nos testes que não exercitam as relações da tarefa. */
    @Autowired
    public TarefaValidador(AreaRiscoRepository areas, SetorRepository setores) {
        super(montarCadeia(areas, setores));
    }

    private static EloValidacao<Tarefa> montarCadeia(AreaRiscoRepository areas, SetorRepository setores) {
        EloValidacao<Tarefa> primeiro = new AreaRiscoExistenteElo(areas);
        primeiro.encadear(new SetoresInformadosElo())
                .encadear(new SetoresExistentesElo(setores))
                .encadear(new DescricaoObrigatoriaElo());
        return primeiro;
    }
}
