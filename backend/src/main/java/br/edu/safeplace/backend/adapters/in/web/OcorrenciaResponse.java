package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;

import java.time.LocalDateTime;

/**
 * Ocorrência devolvida pela API. {@code local} é o nome da área de risco (ou o texto legado de
 * registros antigos); {@code relator*} identifica quem registrou o relato (issue #195).
 */
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
        Integer relatorId,
        String relatorNome,
        String relatorPerfil) {

    public static OcorrenciaResponse fromOutputDTO(OcorrenciaOutputDTO dto) {
        return new OcorrenciaResponse(
                dto.idOcorrencia(),
                dto.tipoOcorrencia(),
                dto.dataOcorrencia(),
                dto.dataRegistro(),
                dto.statusOcorrencia(),
                dto.local(),
                dto.descricao(),
                PlanoDeAcaoResponse.fromOutputDTO(dto.planoDeAcao()),
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
                dto.relatorId(),
                dto.relatorNome(),
                dto.relatorPerfil()
        );
    }
}
