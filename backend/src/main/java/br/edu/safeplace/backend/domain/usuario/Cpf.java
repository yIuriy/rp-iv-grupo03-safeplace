package br.edu.safeplace.backend.domain.usuario;

/**
 * Normalização do CPF: mantém só os dígitos, para que a busca e a gravação funcionem com ou sem
 * máscara. A validação (11 dígitos, sem repetição) fica em {@code validacao.usuario.CpfValidador}
 * desde a issue #263.
 */
public final class Cpf {

    private Cpf() {
    }

    public static String sanitizar(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }
}
