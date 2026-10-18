package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

import java.time.LocalDateTime;

/**
 * Dados de uma ocorrência devolvidos pelos casos de uso. O relator é quem registrou o relato, Gestor
 * de Segurança ou Supervisor, com o perfil correspondente (issue #195).
 */
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
        Integer relatorId,
        String relatorNome,
        String relatorPerfil
) {
    public static OcorrenciaOutputDTO deDominio(Ocorrencia ocorrencia) {
        Integer areaId = ocorrencia.getArea() != null ? ocorrencia.getArea().getId() : null;
        String areaNome = ocorrencia.getArea() != null ? ocorrencia.getArea().getNome() : null;
        Integer colaboradorId = ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getId() : null;
        String colaboradorNome = ocorrencia.getColaborador() != null ? ocorrencia.getColaborador().getNome() : null;
        Colaborador relator = ocorrencia.getRelator();
        Integer relatorId = relator != null ? relator.getId() : null;
        String relatorNome = relator != null ? relator.getNome() : null;
        String relatorPerfil = relator != null && relator.getPerfil() != null ? relator.getPerfil().name() : null;

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
                    relatorId,
                    relatorNome,
                    relatorPerfil
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
                    relatorId,
                    relatorNome,
                    relatorPerfil
            );
        }
        throw new IllegalArgumentException("Tipo de ocorrência não suportado");
    }
}
