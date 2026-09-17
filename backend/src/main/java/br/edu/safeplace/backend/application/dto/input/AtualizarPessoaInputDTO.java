package br.edu.safeplace.backend.application.dto.input;

import java.time.LocalDate;

/**
 * Entrada da atualização cadastral de RF23 (issue #122): só os dados cadastrais do modelo.
 * Identidade (id e CPF), perfil, credenciais e situação não passam por aqui.
 */
public record AtualizarPessoaInputDTO(
        String nome,
        LocalDate dataNascimento,
        String email) {
}
