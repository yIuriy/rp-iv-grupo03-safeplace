package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDateTime;

/**
 * Dados de uma ocorrência devolvidos pelos casos de uso. O relator é quem registrou o relato, Gestor
 * de Segurança ou Supervisor, com o perfil correspondente (issue #195).
 * Só carrega dados; quem o monta a partir do domínio é o {@code OcorrenciaOutputAssembler} (#245).
 */
public record OcorrenciaOutputDTO(
        Integer idOcorrencia,
        String tipoOcorrencia,
        LocalDateTime dataOcorrencia,
        LocalDateTime dataRegistro,
        String statusOcorrencia,
        String local,
        String descricao,
        PlanoDeAcaoOutputDTO planoDeAcao,
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
}
