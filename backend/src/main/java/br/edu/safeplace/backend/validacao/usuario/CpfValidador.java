package br.edu.safeplace.backend.validacao.usuario;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.matchRegexp;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.usuario.Cpf;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o formato do CPF, com ou sem máscara (RF23). Antes da issue #263, era a classe estática
 * {@code domain.usuario.CpfValidador}, chamada no construtor de {@code Colaborador}; agora é um
 * componente injetado por interface. Desde a issue #308, as duas regras são expressões regulares
 * conferidas pelo Commons Validator. Os dígitos verificadores são conferidos pelo {@code @CPF} dos
 * Requests, na entrada HTTP.
 */
@Component
public class CpfValidador extends ValidadorEmCadeia<String> {

    private static final String ONZE_DIGITOS = "^[0-9]{11}$";
    /** O primeiro dígito seguido de dez cópias dele, como 111.111.111-11. */
    private static final String MESMO_DIGITO_ONZE_VEZES = "^([0-9])\\1{10}$";

    public CpfValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<String> montarCadeia() {
        EloValidacao<String> primeiro = exigir(cpf -> !confere(cpf, ONZE_DIGITOS),
                "CPF deve conter exatamente 11 dígitos numéricos.", CpfInvalidoException::new);
        primeiro.encadear(exigir(cpf -> confere(cpf, MESMO_DIGITO_ONZE_VEZES),
                "CPF inválido: sequência de dígitos repetidos.", CpfInvalidoException::new));
        return primeiro;
    }

    /** Tira a máscara e confere o regex. O Commons Validator não aceita texto nulo. */
    private static boolean confere(String cpf, String regex) {
        String limpo = Cpf.sanitizar(cpf);
        return limpo != null && matchRegexp(limpo, regex);
    }
}
