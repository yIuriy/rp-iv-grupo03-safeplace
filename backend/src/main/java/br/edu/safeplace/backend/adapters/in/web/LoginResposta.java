package br.edu.safeplace.backend.adapters.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com token JWT")
public record LoginResposta(
        @Schema(description = "Token JWT de acesso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tipo,

        @Schema(description = "E-mail do usuário autenticado", example = "gestor@safeplace.com")
        String email,

        @Schema(description = "Nome do usuário autenticado", example = "Gestor da Silva")
        String nome,

        @Schema(description = "Perfil de acesso do usuário", example = "GESTOR_SEGURANCA")
        String perfil
) {
}
