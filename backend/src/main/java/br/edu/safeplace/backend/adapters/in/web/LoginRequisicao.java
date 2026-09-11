package br.edu.safeplace.backend.adapters.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição de login no sistema")
public record LoginRequisicao(
        @NotBlank(message = "E-mail é obrigatório")
        @Schema(description = "E-mail de acesso do usuário (o sistema aceita exclusivamente e-mail, não aceita CPF)", example = "gestor@safeplace.com")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Schema(description = "Senha de acesso do usuário", example = "Senha@123")
        String senha
) {
}
