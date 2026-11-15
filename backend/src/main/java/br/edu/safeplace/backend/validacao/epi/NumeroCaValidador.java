package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;
import static org.apache.commons.validator.GenericValidator.isInt;
import static org.apache.commons.validator.GenericValidator.matchRegexp;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o número do CA informado no cadastro de EPI ("12345" ou "CA-12345"). Antes da issue #264,
 * as regras ficavam em {@code ModeloEPI.deCadastroLegado}, na conversão do texto para o CA inteiro.
 */
@Component
public class NumeroCaValidador extends ValidadorEmCadeia<String> {

    public NumeroCaValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<String> montarCadeia() {
        EloValidacao<String> primeiro = new ObrigatorioElo();
        primeiro.encadear(exigir(numero -> !soDigitos(numero),
                        "Número do CA deve conter apenas dígitos, com prefixo CA- opcional."))
                .encadear(exigir(numero -> soDigitos(numero) && !isInt(ModeloEPI.numeroSemPrefixo(numero)),
                        "Número do CA excede o limite inteiro do modelo."));
        return primeiro;
    }

    /** Issue #308: o regex e a conversão para {@code int} são do Commons Validator. */
    private static boolean soDigitos(String numero) {
        String semPrefixo = ModeloEPI.numeroSemPrefixo(numero);
        return semPrefixo != null && matchRegexp(semPrefixo, "^[0-9]+$");
    }

    /** Sem número não há formato a conferir: interrompe a cadeia. */
    private static final class ObrigatorioElo extends EloValidacao<String> {
        @Override
        protected void verificar(String numero, ResultadoValidacao resultado) {
            if (isBlankOrNull(numero)) {
                String mensagem = "Número do CA é obrigatório.";
                resultado.interromper(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
            }
        }
    }
}
