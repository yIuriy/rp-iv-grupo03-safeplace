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
        String potencialDano,
        String gravidade,
        Integer areaId,
        String areaNome,
        Integer colaboradorId,
        String colaboradorNome,
        Integer gestorId,
        String gestorNome
) {
    public OcorrenciaOutputDTO(
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
            String potencialDano,
            String gravidade) {
        this(
                idOcorrencia,
                tipoOcorrencia,
                dataOcorrencia,
                dataRegistro,
                statusOcorrencia,
                local,
                descricao,
                planoDeAcao,
                causaRaiz,
                tipo,
                dano,
                numeroProtocolo,
                numeroProtocoloCAT,
                destino,
                situacaoRisco,
                potencialDano,
                gravidade,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public OcorrenciaOutputDTO(
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
            String potencialDano) {
        this(
                idOcorrencia,
                tipoOcorrencia,
                dataOcorrencia,
                dataRegistro,
                statusOcorrencia,
                local,
                descricao,
                planoDeAcao,
                causaRaiz,
                tipo,
                dano,
                numeroProtocolo,
                numeroProtocoloCAT,
                destino,
                situacaoRisco,
                potencialDano,
                null
        );
    }

    public static OcorrenciaOutputDTO deDominio(Ocorrencia ocorrencia) {
        Integer areaId = ocorrencia.getArea() != null ? ocorrencia.getArea().getId() : null;
        String areaNome = ocorrencia.getArea() != null ? ocorrencia.getArea().getNome() : null;
        Integer colaboradorId = ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getId() : null;
        String colaboradorNome = ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getNome() : null;
        Integer gestorId = ocorrencia.getGestor() != null ? ocorrencia.getGestor().getId() : null;
        String gestorNome = ocorrencia.getGestor() != null ? ocorrencia.getGestor().getNome() : null;

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
                    null,
                    a.getGravidade() != null ? a.getGravidade().name() : null,
                    areaId,
                    areaNome,
                    colaboradorId,
                    colaboradorNome,
                    gestorId,
                    gestorNome
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
                    i.getPotencialDano(),
                    null,
                    areaId,
                    areaNome,
                    colaboradorId,
                    colaboradorNome,
                    gestorId,
                    gestorNome
            );
        }
        throw new IllegalArgumentException("Tipo de ocorrência não suportado");
    }
}
