package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

import java.time.LocalDateTime;

public record RegistrarAcidenteInputDTO(
        LocalDateTime dataOcorrencia,
        String local,
        String descricao,
        PlanoDeAcao planoDeAcao,
        String causaRaiz,
        String tipo,
        String dano,
        String numeroProtocolo,
        String destino,
        NivelPerigo gravidade
) {
    public RegistrarAcidenteInputDTO(
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao,
            String causaRaiz,
            String tipo,
            String dano,
            String numeroProtocolo,
            String destino) {
        this(dataOcorrencia, local, descricao, planoDeAcao, causaRaiz, tipo, dano, numeroProtocolo, destino, NivelPerigo.BAIXO);
    }
}
