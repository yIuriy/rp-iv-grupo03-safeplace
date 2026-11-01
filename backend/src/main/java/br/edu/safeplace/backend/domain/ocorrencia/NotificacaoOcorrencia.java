package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

/**
 * Notificação interna ao Gestor de Segurança sobre um novo relato (UC09, passo 12; DS09,
 * {@code VisaoNotificacao.exibirNotificacao(protocolo, tipo, setor)}). Fica gravada para consulta
 * (issue #195); o canal é interno, sem e-mail nem SMS.
 */
public record NotificacaoOcorrencia(
        Integer id,
        Integer ocorrenciaId,
        TipoOcorrencia tipoOcorrencia,
        String numeroProtocoloCAT,
        String areaNome,
        String mensagem,
        LocalDateTime criadaEm) {

    public static NotificacaoOcorrencia deNovoRelato(Ocorrencia ocorrencia) {
        // A ocorrência é validada por NotificacaoOcorrenciaValidador no adaptador (#266).
        TipoOcorrencia tipo = ocorrencia instanceof Acidente ? TipoOcorrencia.ACIDENTE : TipoOcorrencia.INCIDENTE;
        String protocolo = ocorrencia instanceof Acidente acidente ? acidente.getNumeroProtocoloCAT() : null;
        String area = ocorrencia.getArea() != null ? ocorrencia.getArea().getNome() : null;

        StringBuilder mensagem = new StringBuilder(tipo == TipoOcorrencia.ACIDENTE ? "Novo acidente" : "Novo incidente")
                .append(" registrado");
        if (protocolo != null) {
            mensagem.append(" (protocolo ").append(protocolo).append(")");
        }
        if (area != null) {
            mensagem.append(" na área ").append(area);
        }
        mensagem.append(". Aguarda triagem.");

        return new NotificacaoOcorrencia(null, ocorrencia.getId(), tipo, protocolo, area, mensagem.toString(),
                LocalDateTime.now());
    }
}
