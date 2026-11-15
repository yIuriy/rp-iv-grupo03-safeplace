package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;
import static org.apache.commons.validator.GenericValidator.minValue;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/** Valida a movimentação de estoque antes de gravá-la (UC06). Antes da issue #264, no construtor. */
@Component
public class MovimentacaoEstoqueValidador extends ValidadorEmCadeia<MovimentacaoEstoque> {

    public MovimentacaoEstoqueValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<MovimentacaoEstoque> montarCadeia() {
        EloValidacao<MovimentacaoEstoque> primeiro = exigir(m -> m.getTipo() == null, "Tipo de movimentação é obrigatório.");
        primeiro.encadear(exigir(m -> !minValue(m.getQuantidade(), 1), "Quantidade movimentada deve ser maior que zero."))
                .encadear(exigir(m -> m.getDataHora() == null, "Data e hora da movimentação são obrigatórias."))
                .encadear(exigir(m -> isBlankOrNull(m.getMotivo()), "Motivo da movimentação é obrigatório."));
        return primeiro;
    }
}
