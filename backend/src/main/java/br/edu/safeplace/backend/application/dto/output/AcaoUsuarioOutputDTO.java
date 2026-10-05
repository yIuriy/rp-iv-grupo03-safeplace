/*
 * Uma ação da pessoa autenticada, lida da trilha de auditoria (RNF05): o que foi feito, quando e em
 * qual registro. Serve ao histórico do próprio perfil, em GET /api/usuarios/me/historico.
 */
package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;

/**
 * {@code entidade} é o tipo do registro afetado, como gravado no log (por exemplo, Incidente ou Epi);
 * {@code descricao} é a justificativa gravada, quando houver.
 */
public record AcaoUsuarioOutputDTO(
        Integer id,
        String tipoOperacao,
        LocalDateTime dataHora,
        String entidade,
        Integer entidadeId,
        String descricao) {

    public static AcaoUsuarioOutputDTO deDominio(LogAuditoria log) {
        return new AcaoUsuarioOutputDTO(
                log.getId(),
                log.getTipoOperacao(),
                log.getDataHora(),
                log.getEntidade(),
                log.getEntidadeId(),
                log.getJustificativa());
    }
}
