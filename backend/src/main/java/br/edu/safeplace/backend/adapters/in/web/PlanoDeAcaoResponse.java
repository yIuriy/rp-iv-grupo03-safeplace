package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;

import java.time.LocalDate;

public record PlanoDeAcaoResponse(
        Integer id,
        Integer idOcorrencia,
        String medidasCorretivas,
        String medidasPreventivas,
        LocalDate prazo,
        String status,
        String situacao,
        String responsavelTipo,
        Integer responsavelId,
        String responsavelNome,
        boolean alertaAtivo,
        Integer antecedenciaAlertaDias,
        LocalDate dataAlerta,
        boolean alertaDevido,
        boolean emAtraso,
        Integer gestorId,
        String gestorNome
) {
    public static PlanoDeAcaoResponse fromOutputDTO(PlanoDeAcaoOutputDTO dto) {
        if (dto == null) {
            return null;
        }
        return new PlanoDeAcaoResponse(
                dto.id(),
                dto.idOcorrencia(),
                dto.medidasCorretivas(),
                dto.medidasPreventivas(),
                dto.prazo(),
                dto.status(),
                dto.situacao(),
                dto.responsavelTipo(),
                dto.responsavelId(),
                dto.responsavelNome(),
                dto.alertaAtivo(),
                dto.antecedenciaAlertaDias(),
                dto.dataAlerta(),
                dto.alertaDevido(),
                dto.emAtraso(),
                dto.gestorId(),
                dto.gestorNome()
        );
    }
}
