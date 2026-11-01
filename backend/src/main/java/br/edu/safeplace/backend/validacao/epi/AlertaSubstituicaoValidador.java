package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.AlertaSubstituicaoEpi;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o alerta preventivo antes de entregá-lo (RF21, DS10 2.4). O alerta não é gravado; é
 * validado antes de virar resposta. Antes da issue #264, no construtor.
 */
@Component
public class AlertaSubstituicaoValidador extends ValidadorEmCadeia<AlertaSubstituicaoEpi> {

    public AlertaSubstituicaoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<AlertaSubstituicaoEpi> montarCadeia() {
        EloValidacao<AlertaSubstituicaoEpi> primeiro = exigir(a -> a.getEpiId() == null,
                "Identificador do EPI é obrigatório no alerta.");
        primeiro.encadear(exigir(a -> vazio(a.getNomeEpi()), "Nome do EPI é obrigatório no alerta."))
                .encadear(exigir(a -> a.getTipoAlerta() == null, "Tipo de alerta é obrigatório."))
                .encadear(exigir(a -> vazio(a.getMensagem()), "Mensagem do alerta é obrigatória."));
        return primeiro;
    }
}
