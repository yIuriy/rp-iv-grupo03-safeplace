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

        int digito1 = calcularDigito(limpo.substring(0, 9), 10);
        int digito2 = calcularDigito(limpo.substring(0, 10), 11);

        if ((limpo.charAt(9) - '0' != digito1) || (limpo.charAt(10) - '0' != digito2)) {
            throw new CpfInvalidoException("CPF inválido: dígitos verificadores incorretos.");
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

    private static int calcularDigito(String str, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (int i = 0; i < str.length(); i++) {
            soma += (str.charAt(i) - '0') * peso--;
        }
        int resto = 11 - (soma % 11);
        return (resto >= 10) ? 0 : resto;
    }
}
