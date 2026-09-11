package br.edu.safeplace.backend.application.dto.output;

public record TokenSaidaDTO(
        String token,
        String tipo,
        String email,
        String nome,
        String perfil
) {
    public TokenSaidaDTO(String token, String email, String nome, String perfil) {
        this(token, "Bearer", email, nome, perfil);
    }
}
