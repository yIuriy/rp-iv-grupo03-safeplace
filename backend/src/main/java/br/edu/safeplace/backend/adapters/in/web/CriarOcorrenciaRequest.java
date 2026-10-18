package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Corpo de {@code POST /api/ocorrencias} (UC05, UC09). Protocolo CAT e relator são definidos pelo
 * servidor, e o plano de ação é cadastrado à parte pelo Gestor (UC07); por isso não fazem parte do
 * relato (issue #195). Gravidade e causa raiz chegam como texto e são validadas no caso de uso.
 */
public record CriarOcorrenciaRequest(
        @NotBlank(message = "Tipo de ocorrência é obrigatório.") String tipoOcorrencia,
        @NotNull(message = "Data do fato é obrigatória.") LocalDateTime dataOcorrencia,
        @NotBlank(message = "Descrição é obrigatória.") String descricao,

        @NotNull(message = "Área de risco é obrigatória.") Integer areaId,
        @NotNull(message = "Colaborador envolvido é obrigatório.") Integer colaboradorId,

        String causaRaiz,
        String gravidade,
        String tipo,
        String dano,
        String destino,

        String situacaoRisco,
        String potencialDano
) {
    public RegistrarAcidenteInputDTO toAcidenteInputDTO() {
        return new RegistrarAcidenteInputDTO(
                dataOcorrencia,
                descricao,
                causaRaiz,
                gravidade,
                tipo,
                dano,
                destino,
                areaId,
                colaboradorId
        );
    }

    public RegistrarIncidenteInputDTO toIncidenteInputDTO() {
        return new RegistrarIncidenteInputDTO(
                dataOcorrencia,
                descricao,
                situacaoRisco,
                potencialDano,
                areaId,
                colaboradorId
        );
    }
}
