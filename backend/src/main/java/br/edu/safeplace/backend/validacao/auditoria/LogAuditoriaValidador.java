package br.edu.safeplace.backend.validacao.auditoria;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o registro da trilha de auditoria (RNF05) antes de gravar. Antes, no construtor de
 * {@link LogAuditoria} (issue #297). Mesma ordem e mesmas mensagens; recusa com
 * {@link IllegalArgumentException}, como o construtor.
 */
@Component
public class LogAuditoriaValidador extends ValidadorEmCadeia<LogAuditoria> {

    public LogAuditoriaValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<LogAuditoria> montarCadeia() {
        EloValidacao<LogAuditoria> primeiro =
                exigir(log -> isBlankOrNull(log.getTipoOperacao()), "Tipo de operação é obrigatório.");
        primeiro.encadear(exigir(log -> isBlankOrNull(log.getResponsavel()), "Responsável pela operação é obrigatório."));
        return primeiro;
    }
}
