package br.edu.safeplace.backend.application.port.out;

public interface CodificadorSenha {
    String codificar(String senha);
    boolean validar(String senhaPura, String senhaCodificada);
}
