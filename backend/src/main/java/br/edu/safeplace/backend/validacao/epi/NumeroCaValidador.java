package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

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
        primeiro.encadear(exigir(numero -> !ModeloEPI.numeroSemPrefixo(numero).matches("[0-9]+"),
                        "Número do CA deve conter apenas dígitos, com prefixo CA- opcional."))
                .encadear(exigir(numero -> ModeloEPI.numeroSemPrefixo(numero).matches("[0-9]+")
                                && ModeloEPI.converterNumero(numero) == 0
                                && !ModeloEPI.numeroSemPrefixo(numero).matches("0+"),
                        "Número do CA excede o limite inteiro do modelo."));
        return primeiro;
    }

    /** Sem número não há formato a conferir: interrompe a cadeia. */
    private static final class ObrigatorioElo extends EloValidacao<String> {
        @Override
        protected void verificar(String numero, ResultadoValidacao resultado) {
            if (vazio(numero)) {
                String mensagem = "Número do CA é obrigatório.";
                resultado.interromper(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
            }
        }
    }
}
