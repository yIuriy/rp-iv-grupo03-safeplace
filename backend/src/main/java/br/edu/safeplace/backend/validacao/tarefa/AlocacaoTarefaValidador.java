package br.edu.safeplace.backend.validacao.tarefa;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o registro da alocação antes de gravá-lo (RF06, UC11). A aptidão do colaborador é
 * verificada antes, pela cadeia {@code AptidaoColaboradorValidador} (#228). Antes da issue #265,
 * estas regras ficavam no construtor de {@code AlocacaoTarefa}.
 */
@Component
public class AlocacaoTarefaValidador extends ValidadorEmCadeia<AlocacaoTarefa> {

    public AlocacaoTarefaValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<AlocacaoTarefa> montarCadeia() {
        EloValidacao<AlocacaoTarefa> primeiro = exigir(a -> a.getTarefaId() == null,
                "Identificador da tarefa é obrigatório para alocação.");
        primeiro.encadear(exigir(a -> a.getColaboradorId() == null,
                        "Identificador do colaborador é obrigatório para alocação."))
                .encadear(exigir(a -> a.getResponsavelAlocacaoId() == null,
                        "Responsável pelo registro da alocação é obrigatório."));
        return primeiro;
    }
}
