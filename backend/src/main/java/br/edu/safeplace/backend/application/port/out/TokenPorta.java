package br.edu.safeplace.backend.application.port.out;

public interface TokenPorta {
    String gerarToken(String email, String perfil);
    String extrairEmail(String token);
    String extrairPerfil(String token);
    boolean validarToken(String token);
}
