/*
 * Null Object da porta OperadorAutenticado (issue #241): representa "ninguém autenticado" sem usar
 * null. Substitui a classe anônima que UsuarioService criava quando recebia o operador nulo. Não é um
 * bean: em produção o Spring injeta SpringSecurityOperadorAutenticadoAdapter; esta classe serve a
 * montagens sem sessão, como os testes de cadastro.
 */
package br.edu.safeplace.backend.adapters.out.seguranca;

import java.util.Optional;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;

public final class OperadorAutenticadoNulo implements OperadorAutenticado {

    /** Sem sessão não há identificador; a auditoria cai no responsável "SISTEMA". */
    @Override
    public Optional<String> obterIdentificador() {
        return Optional.empty();
    }

    /** Quem não está autenticado não tem papel nenhum. */
    @Override
    public boolean possuiPapel(String papel) {
        return false;
    }
}
