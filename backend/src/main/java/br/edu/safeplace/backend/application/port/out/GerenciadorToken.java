package br.edu.safeplace.backend.application.port.out;

public interface GerenciadorToken {
    String gerar(String email, String perfil);
    String extrairEmail(String token);
    String extrairPerfil(String token);
    boolean validar(String token);
}
