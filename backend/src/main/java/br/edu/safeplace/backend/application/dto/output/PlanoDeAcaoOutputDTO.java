package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

import java.time.LocalDate;

public record PlanoDeAcaoOutputDTO(
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
        Integer gestorId,
        String gestorNome
) {
    public static PlanoDeAcaoOutputDTO deDominio(PlanoDeAcao plano) {
        if (plano == null) {
            return null;
        }

        String respTipo = null;
        Integer respId = null;
        String respNome = null;

        if (plano.getResponsavelColaborador() != null) {
            respTipo = "COLABORADOR";
            respId = plano.getResponsavelColaborador().getId();
            respNome = plano.getResponsavelColaborador().getNome();
        } else if (plano.getResponsavelSetor() != null) {
            respTipo = "SETOR";
            respId = plano.getResponsavelSetor().getId();
            respNome = plano.getResponsavelSetor().getNome();
        }

        Integer gId = plano.getGestor() != null ? plano.getGestor().getId() : null;
        String gNome = plano.getGestor() != null ? plano.getGestor().getNome() : null;

        return new PlanoDeAcaoOutputDTO(
                plano.getId(),
                plano.getIdOcorrencia(),
                plano.getMedidasCorretivas(),
                plano.getMedidasPreventivas(),
                plano.getPrazo(),
                plano.getStatus(),
                plano.getSituacao().map(Enum::name).orElse(plano.getStatus()),
                respTipo,
                respId,
                respNome,
                plano.isAlertaAtivo(),
                plano.getAntecedenciaAlertaDias(),
                plano.getDataAlerta().orElse(null),
                gId,
                gNome
        );
    }
}
