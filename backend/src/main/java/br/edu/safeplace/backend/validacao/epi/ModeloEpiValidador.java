package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o modelo (CA) informado no cadastro, antes de procurar um modelo existente com o mesmo CA
 * (decisão #124). Antes da issue #264, ficava no construtor de {@code ModeloEPI}.
 */
@Component
public class ModeloEpiValidador extends ValidadorEmCadeia<ModeloEPI> {

    public ModeloEpiValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<ModeloEPI> montarCadeia() {
        EloValidacao<ModeloEPI> primeiro = exigir(m -> m.getCa() <= 0, "CA inválido.");
        primeiro.encadear(exigir(m -> m.getValidadeCA() == null, "Data de validade do CA é obrigatória."));
        return primeiro;
    }
}
