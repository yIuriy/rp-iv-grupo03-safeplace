package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida a projeção calculada antes de gravá-la (RF21, UC10). Antes da issue #264, no construtor.
 * Não confundir com {@code validacao.projecao.ProjecaoValidador}, que verifica os dados de entrada
 * do cálculo (#267).
 */
@Component
public class ProjecaoSubstituicaoValidador extends ValidadorEmCadeia<ProjecaoSubstituicao> {

    public ProjecaoSubstituicaoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<ProjecaoSubstituicao> montarCadeia() {
        EloValidacao<ProjecaoSubstituicao> primeiro = exigir(p -> p.getEpi() == null || p.getEpiId() == null,
                "Identificador do EPI é obrigatório para a projeção.");
        primeiro.encadear(exigir(p -> p.getDataProjecao() == null, "Data da projeção é obrigatória."))
                .encadear(exigir(p -> p.getDataEstimadaDescarte() == null, "Data estimada de descarte é obrigatória."))
                .encadear(exigir(p -> isBlankOrNull(p.getJustificativa()), "Justificativa da projeção é obrigatória."));
        return primeiro;
    }
}
