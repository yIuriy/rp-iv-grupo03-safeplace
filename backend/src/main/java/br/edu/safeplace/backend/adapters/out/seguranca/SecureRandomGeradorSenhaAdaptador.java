package br.edu.safeplace.backend.adapters.out.seguranca;

import br.edu.safeplace.backend.application.port.out.GeradorSenhaPorta;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gera senhas iniciais aleatórias com {@link SecureRandom}.
 * Garante ao menos um caractere de cada classe (minúscula, maiúscula, dígito, especial)
 * e completa o restante a partir do alfabeto inteiro, embaralhando no final para que
 * a posição dos caracteres obrigatórios não seja previsível.
 */
@Component
public class SecureRandomGeradorSenhaAdaptador implements GeradorSenhaPorta {

    static final int TAMANHO = 12;
    static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    static final String MAIUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    static final String DIGITOS = "23456789";
    static final String ESPECIAIS = "!@#$%&*?";
    static final String ALFABETO = MINUSCULAS + MAIUSCULAS + DIGITOS + ESPECIAIS;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String gerar() {
        List<Character> caracteres = new ArrayList<>(TAMANHO);
        caracteres.add(sortear(MINUSCULAS));
        caracteres.add(sortear(MAIUSCULAS));
        caracteres.add(sortear(DIGITOS));
        caracteres.add(sortear(ESPECIAIS));
        while (caracteres.size() < TAMANHO) {
            caracteres.add(sortear(ALFABETO));
        }
        Collections.shuffle(caracteres, random);

        StringBuilder senha = new StringBuilder(TAMANHO);
        caracteres.forEach(senha::append);
        return senha.toString();
    }

    private char sortear(String conjunto) {
        return conjunto.charAt(random.nextInt(conjunto.length()));
    }
}
