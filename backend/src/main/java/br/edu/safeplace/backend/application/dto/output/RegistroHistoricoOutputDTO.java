/*
 * Uma entrada do histórico de alterações de uma área de risco ou tarefa, lida de LogAuditoria
 * (UC03, cenário alternativo II; UC11, regra 1). Serve à consulta do Gestor de Segurança (#114).
 */
package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;

/**
 * {@code descricao} é a justificativa gravada no log: o que mudou, com valor anterior e novo, e o
 * motivo informado pelo Gestor, quando houver.
 */
public record RegistroHistoricoOutputDTO(
        Integer id,
        String tipoOperacao,
        LocalDateTime dataHora,
        String responsavel,
        String descricao) {

    public static RegistroHistoricoOutputDTO deDominio(LogAuditoria log) {
        return new RegistroHistoricoOutputDTO(
                log.getId(),
                log.getTipoOperacao(),
                log.getDataHora(),
                log.getResponsavel(),
                log.getJustificativa());
    }
}
