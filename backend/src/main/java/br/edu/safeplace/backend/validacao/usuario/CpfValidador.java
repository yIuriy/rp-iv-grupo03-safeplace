package br.edu.safeplace.backend.validacao.usuario;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.usuario.Cpf;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o formato do CPF, com ou sem máscara (RF23). Antes da issue #263, era a classe estática
 * {@code domain.usuario.CpfValidador}, chamada no construtor de {@code Colaborador}; agora é um
 * componente injetado por interface.
 */
@Component
public class CpfValidador extends ValidadorEmCadeia<String> {

    public CpfValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<String> montarCadeia() {
        EloValidacao<String> primeiro = exigir(cpf -> {
            String limpo = Cpf.sanitizar(cpf);
            return limpo == null || limpo.length() != 11;
        }, "CPF deve conter exatamente 11 dígitos numéricos.", CpfInvalidoException::new);
        primeiro.encadear(exigir(cpf -> todosDigitosIguais(Cpf.sanitizar(cpf)),
                "CPF inválido: sequência de dígitos repetidos.", CpfInvalidoException::new));
        return primeiro;
    }

    private static boolean todosDigitosIguais(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        return cpf.chars().allMatch(digito -> digito == cpf.charAt(0));
    }
}
