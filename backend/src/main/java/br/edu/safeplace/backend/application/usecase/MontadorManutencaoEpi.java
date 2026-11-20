package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Monta {@link ManutencaoEpi} na camada de aplicação a partir do {@link ConcluirManutencaoInputDTO}
 * inteiro usando {@link ManutencaoEpi.Builder}, mantendo o domínio isolado dos DTOs (#259, decisão #254).
 */
final class MontadorManutencaoEpi {

    ManutencaoEpi montar(ConcluirManutencaoInputDTO entrada, Epi epi,
                         Colaborador responsavel, String responsavelEmail) {
        return new ManutencaoEpi.Builder()
                .epi(epi)
                .realizadaEm(entrada.dataManutencao())
                .tipo(entrada.tipoManutencao())
                .descricao(entrada.descricao())
                .resultado(entrada.resultado())
                .responsavelManutencao(responsavelEmail != null
                        ? responsavelEmail
                        : entrada.responsavelManutencao())
                .responsavel(responsavel)
                .build();
    }
}
