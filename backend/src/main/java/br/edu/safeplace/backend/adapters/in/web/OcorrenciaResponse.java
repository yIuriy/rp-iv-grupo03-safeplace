package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

import java.time.LocalDateTime;

public record OcorrenciaResponse(
        Integer idOcorrencia,
        String tipoOcorrencia,
        LocalDateTime dataOcorrencia,
        String local,
        String descricao,
        PlanoDeAcaoResponse planoDeAcao,

        String causaRaiz,
        String tipo,
        String dano,
        String numeroProtocolo,
        String destino,

        String situacaoRisco,
        String potencialDano) {
    public static OcorrenciaResponse fromDomain(Ocorrencia ocorrencia) {
        if (ocorrencia instanceof Acidente acidente) {
            return new OcorrenciaResponse(
                    acidente.getIdOcorrencia(),
                    "ACIDENTE",
                    acidente.getDataOcorrencia(),
                    acidente.getLocal(),
                    acidente.getDescricao(),
                    PlanoDeAcaoResponse.fromDomain(acidente.getPlanoDeAcao()),
                    acidente.getCausaRaiz(),
                    acidente.getTipo(),
                    acidente.getDano(),
                    acidente.getNumeroProtocolo(),
                    acidente.getDestino(),
                    null,
                    null);
        }

        if (ocorrencia instanceof Incidente incidente) {
            return new OcorrenciaResponse(
                    incidente.getIdOcorrencia(),
                    "INCIDENTE",
                    incidente.getDataOcorrencia(),
                    incidente.getLocal(),
                    incidente.getDescricao(),
                    PlanoDeAcaoResponse.fromDomain(incidente.getPlanoDeAcao()),
                    null,
                    null,
                    null,
                    null,
                    null,
                    incidente.getSituacaoRisco(),
                    incidente.getPotencialDano());
        }

        throw new IllegalStateException("Tipo de ocorrência não suportado.");
    }
}