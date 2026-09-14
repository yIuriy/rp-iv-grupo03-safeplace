package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

/**
 * Entrada do cadastro de Colaborador (RF23).
 *
 * <p>O Colaborador não acessa o sistema: existe apenas para vinculação a ocorrências,
 * capacitações e empréstimos de EPIs. Por isso o tipo não declara senha nem perfil — não há
 * como informar credenciais por este caminho.</p>
 */
public record CadastrarColaboradorInputDTO(
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email) {
}
