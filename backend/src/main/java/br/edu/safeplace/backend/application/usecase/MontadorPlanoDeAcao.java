package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CriarAcaoPlanoInputDTO;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcaoBuilder;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Monta {@link PlanoDeAcao} na camada de aplicação a partir do {@link CriarAcaoPlanoInputDTO} inteiro
 * usando {@link PlanoDeAcaoBuilder}, mantendo o domínio isolado dos DTOs e evitando que o serviço
 * desempacote campos soltos (#262, decisão #254).
 */
final class MontadorPlanoDeAcao {

    PlanoDeAcao montar(
            CriarAcaoPlanoInputDTO entrada,
            Ocorrencia ocorrencia,
            Colaborador responsavelColaborador,
            Setor responsavelSetor,
            GestorDeSeguranca gestor) {
        return new PlanoDeAcaoBuilder()
                .ocorrencia(ocorrencia)
                .medidasCorretivas(entrada.medidasCorretivas())
                .medidasPreventivas(entrada.medidasPreventivas())
                .prazo(entrada.prazo())
                .responsavelColaborador(responsavelColaborador)
                .responsavelSetor(responsavelSetor)
                .alertaAtivo(entrada.alertaAtivo())
                .antecedenciaAlertaDias(entrada.antecedenciaAlertaDias())
                .gestor(gestor)
                .build();
    }
}
