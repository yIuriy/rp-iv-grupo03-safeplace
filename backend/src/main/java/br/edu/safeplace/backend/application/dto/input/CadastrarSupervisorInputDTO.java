package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

/**
 * Entrada do cadastro de Supervisor (RF23).
 *
 * <p>Não existe campo de senha: a credencial inicial do Supervisor é gerada pelo sistema
 * (issue #91) e devolvida uma única vez na resposta do cadastro.</p>
 */
public record CadastrarSupervisorInputDTO(
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email) {
}
