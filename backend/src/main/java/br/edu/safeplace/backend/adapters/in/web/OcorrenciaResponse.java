package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;

import java.time.LocalDateTime;

public record OcorrenciaResponse(
        Integer idOcorrencia,
        String tipoOcorrencia,
        LocalDateTime dataOcorrencia,
        LocalDateTime dataRegistro,
        String statusOcorrencia,
        String local,
        String descricao,
        PlanoDeAcaoResponse planoDeAcao,

        String causaRaiz,
        String tipo,
        String dano,
        String numeroProtocolo,
        String numeroProtocoloCAT,
        String destino,
        String gravidade,

        String situacaoRisco,
        String potencialDano,

        Integer areaId,
        String areaNome,
        Integer colaboradorId,
        String colaboradorNome,
        Integer gestorId,
        String gestorNome) {

    public OcorrenciaResponse(
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
            String gravidade,
            String situacaoRisco,
            String potencialDano) {
        this(idOcorrencia, tipoOcorrencia, dataOcorrencia, null, "ABERTA", local, descricao, planoDeAcao,
                causaRaiz, tipo, dano, numeroProtocolo, numeroProtocolo, destino, gravidade,
                situacaoRisco, potencialDano, null, null, null, null, null, null);
    }

    public OcorrenciaResponse(
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
        this(idOcorrencia, tipoOcorrencia, dataOcorrencia, null, "ABERTA", local, descricao, planoDeAcao,
                causaRaiz, tipo, dano, numeroProtocolo, numeroProtocolo, destino, null,
                situacaoRisco, potencialDano, null, null, null, null, null, null);
    }

    public static OcorrenciaResponse fromDomain(Ocorrencia ocorrencia) {
        Integer areaId = ocorrencia.getArea() != null ? ocorrencia.getArea().getId() : null;
        String areaNome = ocorrencia.getArea() != null ? ocorrencia.getArea().getNome() : null;
        Integer colaboradorId = ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getId() : null;
        String colaboradorNome = ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getNome() : null;
        Integer gestorId = ocorrencia.getGestor() != null ? ocorrencia.getGestor().getId() : null;
        String gestorNome = ocorrencia.getGestor() != null ? ocorrencia.getGestor().getNome() : null;
        String status = ocorrencia.getStatusOcorrencia() != null ? ocorrencia.getStatusOcorrencia().name() : null;

        if (ocorrencia instanceof Acidente acidente) {
            return new OcorrenciaResponse(
                    acidente.getIdOcorrencia(),
                    "ACIDENTE",
                    acidente.getDataOcorrencia(),
                    acidente.getDataRegistro(),
                    status,
                    acidente.getLocal(),
                    acidente.getDescricao(),
                    PlanoDeAcaoResponse.fromDomain(acidente.getPlanoDeAcao()),
                    acidente.getCausaRaiz() != null ? acidente.getCausaRaiz().name() : null,
                    acidente.getTipo(),
                    acidente.getDano(),
                    acidente.getNumeroProtocoloCAT(),
                    acidente.getNumeroProtocoloCAT(),
                    acidente.getDestino(),
                    acidente.getGravidade() != null ? acidente.getGravidade().name() : null,
                    null,
                    null,
                    areaId,
                    areaNome,
                    colaboradorId,
                    colaboradorNome,
                    gestorId,
                    gestorNome);
        }

        if (ocorrencia instanceof Incidente incidente) {
            return new OcorrenciaResponse(
                    incidente.getIdOcorrencia(),
                    "INCIDENTE",
                    incidente.getDataOcorrencia(),
                    incidente.getDataRegistro(),
                    status,
                    incidente.getLocal(),
                    incidente.getDescricao(),
                    PlanoDeAcaoResponse.fromDomain(incidente.getPlanoDeAcao()),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    incidente.getSituacaoRisco(),
                    incidente.getPotencialDano(),
                    areaId,
                    areaNome,
                    colaboradorId,
                    colaboradorNome,
                    gestorId,
                    gestorNome);
        }

        throw new IllegalStateException("Tipo de ocorrência não suportado.");
    }

    public static OcorrenciaResponse fromOutputDTO(br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO dto) {
        return new OcorrenciaResponse(
                dto.idOcorrencia(),
                dto.tipoOcorrencia(),
                dto.dataOcorrencia(),
                dto.dataRegistro(),
                dto.statusOcorrencia(),
                dto.local(),
                dto.descricao(),
                dto.planoDeAcao() != null ? PlanoDeAcaoResponse.fromDomain(dto.planoDeAcao()) : null,
                dto.causaRaiz(),
                dto.tipo(),
                dto.dano(),
                dto.numeroProtocolo(),
                dto.numeroProtocoloCAT(),
                dto.destino(),
                dto.gravidade(),
                dto.situacaoRisco(),
                dto.potencialDano(),
                dto.areaId(),
                dto.areaNome(),
                dto.colaboradorId(),
                dto.colaboradorNome(),
                dto.gestorId(),
                dto.gestorNome()
        );
    }
}