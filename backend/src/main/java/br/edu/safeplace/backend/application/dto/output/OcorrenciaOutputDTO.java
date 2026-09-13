package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

import java.time.LocalDateTime;

public record OcorrenciaOutputDTO(
        Integer idOcorrencia,
        String tipoOcorrencia,
        LocalDateTime dataOcorrencia,
        LocalDateTime dataRegistro,
        String statusOcorrencia,
        String local,
        String descricao,
        PlanoDeAcao planoDeAcao,
        String causaRaiz,
        String tipo,
        String dano,
        String numeroProtocolo,
        String numeroProtocoloCAT,
        String destino,
        String situacaoRisco,
        String potencialDano
) {
    public static OcorrenciaOutputDTO deDominio(Ocorrencia ocorrencia) {
        if (ocorrencia instanceof Acidente a) {
            return new OcorrenciaOutputDTO(
                    a.getIdOcorrencia(),
                    "ACIDENTE",
                    a.getDataOcorrencia(),
                    a.getDataRegistro(),
                    a.getStatusOcorrencia() != null ? a.getStatusOcorrencia().name() : null,
                    a.getLocal(),
                    a.getDescricao(),
                    a.getPlanoDeAcao(),
                    a.getCausaRaiz() != null ? a.getCausaRaiz().name() : null,
                    a.getTipo(),
                    a.getDano(),
                    a.getNumeroProtocoloCAT(),
                    a.getNumeroProtocoloCAT(),
                    a.getDestino(),
                    null,
                    null
            );
        } else if (ocorrencia instanceof Incidente i) {
            return new OcorrenciaOutputDTO(
                    i.getIdOcorrencia(),
                    "INCIDENTE",
                    i.getDataOcorrencia(),
                    i.getDataRegistro(),
                    i.getStatusOcorrencia() != null ? i.getStatusOcorrencia().name() : null,
                    i.getLocal(),
                    i.getDescricao(),
                    i.getPlanoDeAcao(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    i.getSituacaoRisco(),
                    i.getPotencialDano()
            );
        }
        throw new IllegalArgumentException("Tipo de ocorrência não suportado");
    }
}
