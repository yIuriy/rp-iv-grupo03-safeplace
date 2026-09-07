package br.edu.safeplace.backend.domain.usuario;

import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;

public final class CpfValidador {

    private CpfValidador() {
    }

    public static String sanitizar(String cpf) {
        if (cpf == null) {
            return null;
        }
        return cpf.replaceAll("\\D", "");
    }

    public static void validar(String cpf) {
        String limpo = sanitizar(cpf);
        if (limpo == null || limpo.length() != 11) {
            throw new CpfInvalidoException("CPF deve conter exatamente 11 dígitos numéricos.");
        }

        if (todosDigitosIguais(limpo)) {
            throw new CpfInvalidoException("CPF inválido: sequência de dígitos repetidos.");
        }
    }

    private static boolean todosDigitosIguais(String cpf) {
        char primeiro = cpf.charAt(0);
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }
}
