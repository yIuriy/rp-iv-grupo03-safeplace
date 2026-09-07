package br.edu.safeplace.backend.application.dto.input;

import br.edu.safeplace.backend.domain.usuario.Perfil;

import java.time.LocalDate;

public record CadastrarUsuarioEntradaDTO(
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        String senha,
        Perfil perfil
) {
}
