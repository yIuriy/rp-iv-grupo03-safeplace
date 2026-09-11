package br.edu.safeplace.backend.adapters.out.seguranca;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecureRandomGeradorSenhaAdaptadorTest {

    private final SecureRandomGeradorSenhaAdaptador gerador = new SecureRandomGeradorSenhaAdaptador();

    @RepeatedTest(20)
    void deveGerarSenhaComPeloMenosDozeCaracteres() {
        String senha = gerador.gerar();

        assertTrue(senha.length() >= SecureRandomGeradorSenhaAdaptador.TAMANHO,
                "Senha curta demais: " + senha.length());
    }

    @RepeatedTest(20)
    void deveConterMinusculaMaiusculaDigitoEEspecial() {
        String senha = gerador.gerar();

        assertTrue(senha.chars().anyMatch(Character::isLowerCase), "sem minúscula: " + senha);
        assertTrue(senha.chars().anyMatch(Character::isUpperCase), "sem maiúscula: " + senha);
        assertTrue(senha.chars().anyMatch(Character::isDigit), "sem dígito: " + senha);
        assertTrue(senha.chars().anyMatch(c -> SecureRandomGeradorSenhaAdaptador.ESPECIAIS.indexOf(c) >= 0),
                "sem caractere especial: " + senha);
    }

    @RepeatedTest(20)
    void deveUsarApenasCaracteresDoAlfabetoPermitido() {
        String senha = gerador.gerar();

        assertTrue(senha.chars().allMatch(c -> SecureRandomGeradorSenhaAdaptador.ALFABETO.indexOf(c) >= 0),
                "caractere fora do alfabeto: " + senha);
    }

    @Test
    void senhasConsecutivasDevemSerDiferentes() {
        Set<String> geradas = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            geradas.add(gerador.gerar());
        }

        assertEquals(50, geradas.size(), "houve senha repetida em 50 gerações");
    }
}
