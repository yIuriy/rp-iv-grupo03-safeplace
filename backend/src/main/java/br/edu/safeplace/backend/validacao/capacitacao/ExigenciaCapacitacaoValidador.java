package br.edu.safeplace.backend.validacao.capacitacao;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/** Valida a exigência de capacitação antes de gravá-la (UC02). Antes da issue #265, no construtor. */
@Component
public class ExigenciaCapacitacaoValidador extends ValidadorEmCadeia<ExigenciaCapacitacao> {

    public ExigenciaCapacitacaoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<ExigenciaCapacitacao> montarCadeia() {
        EloValidacao<ExigenciaCapacitacao> primeiro = exigir(e -> isBlankOrNull(e.getNomeCapacitacao()),
                "Nome da capacitação exigida é obrigatório.");
        primeiro.encadear(exigir(e -> e.getTipoCapacitacao() == null || e.getAlvoId() == null || e.getAlvo() == null,
                "Todos os campos de vínculo são obrigatórios."));
        return primeiro;
    }
}
