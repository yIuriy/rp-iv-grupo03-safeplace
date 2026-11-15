package br.edu.safeplace.backend.application.port.out;

import java.util.Optional;

/**
 * Porta de saída para recuperar informações sobre o operador atualmente autenticado
 * sem acoplar a camada de aplicação/domínio ao framework de segurança.
 */
public interface OperadorAutenticado {
    Optional<String> obterIdentificador();
    boolean possuiPapel(String papel);
}
