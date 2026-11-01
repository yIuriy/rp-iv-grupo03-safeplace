package br.edu.safeplace.backend.application.port.out;

public interface GerenciadorToken {
    String gerarToken(String email, String perfil);
    String extrairEmail(String token);
    String extrairPerfil(String token);
    boolean validarToken(String token);
}
