package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;

public record SetorOutputDTO(
        Integer id,
        String nome,
        String descricao,
        String localizacao,
        NivelPerigo status
) {
    public static SetorOutputDTO deDominio(Setor setor) {
        if (setor == null) {
            return null;
        }
        return new SetorOutputDTO(
                setor.getId(),
                setor.getNome(),
                setor.getDescricao(),
                setor.getLocalizacao(),
                setor.getStatus()
        );
    }
}
