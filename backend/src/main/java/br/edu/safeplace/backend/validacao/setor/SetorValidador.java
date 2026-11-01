package br.edu.safeplace.backend.validacao.setor;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/** Valida o setor físico no cadastro e na atualização (RF05, UC03). Antes, no construtor (issue #263). */
@Component
public class SetorValidador extends ValidadorEmCadeia<Setor> {

    public SetorValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<Setor> montarCadeia() {
        EloValidacao<Setor> primeiro = exigir(s -> vazio(s.getNome()), "Nome do setor é obrigatório.");
        primeiro.encadear(exigir(s -> s.getStatus() == null, "Status/Nível de perigo do setor é obrigatório."));
        return primeiro;
    }
}
