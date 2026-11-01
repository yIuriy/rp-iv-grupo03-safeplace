package br.edu.safeplace.backend.application.dto.output;

public record TokenOutputDTO(
        String token,
        String tipo,
        String email,
        String nome,
        String perfil
) {
    public TokenOutputDTO(String token, String email, String nome, String perfil) {
        this(token, "Bearer", email, nome, perfil);
    }
}
