package br.edu.safeplace.backend.validacao.ocorrencia;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.exigirComMensagem;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Operação {@code validarPlano} do diagrama de classes, que confere o conjunto de ações de uma
 * ocorrência. Toda ocorrência grave exige ao menos uma ação preventiva (UC07, regra 1). Antes da
 * issue #266, era o método estático {@code PlanoDeAcao.validarPlano}.
 */
@Component
public class PlanoValidador extends ValidadorEmCadeia<PlanoDaOcorrencia> {

    public PlanoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<PlanoDaOcorrencia> montarCadeia() {
        EloValidacao<PlanoDaOcorrencia> primeiro = new OcorrenciaRegistradaElo();
        primeiro.encadear(exigir(p -> p.acoes().isEmpty(),
                        "O plano de ação deve conter ao menos uma ação.", PlanoDeAcaoInvalidoException::new))
                .encadear(exigirComMensagem(p -> p.acoes().stream()
                                .anyMatch(acao -> !p.ocorrencia().getId().equals(acao.getIdOcorrencia())),
                        p -> "Todas as ações do plano devem pertencer à ocorrência " + p.ocorrencia().getId() + ".",
                        PlanoDeAcaoInvalidoException::new))
                .encadear(new PreventivaEmOcorrenciaGraveElo());
        return primeiro;
    }

    /** Sem ocorrência registrada não há plano a conferir: interrompe a cadeia. */
    private static final class OcorrenciaRegistradaElo extends EloValidacao<PlanoDaOcorrencia> {
        @Override
        protected void verificar(PlanoDaOcorrencia plano, ResultadoValidacao resultado) {
            if (plano.ocorrencia() == null || plano.ocorrencia().getId() == null) {
                String mensagem = "O plano deve estar vinculado a uma ocorrência registrada.";
                resultado.interromper(Problema.comExcecao(mensagem, () -> new PlanoDeAcaoInvalidoException(mensagem)));
            }
        }
    }

    /** UC07, regra 1: ocorrência grave exige ao menos uma ação preventiva. */
    private static final class PreventivaEmOcorrenciaGraveElo extends EloValidacao<PlanoDaOcorrencia> {
        @Override
        protected void verificar(PlanoDaOcorrencia plano, ResultadoValidacao resultado) {
            if (plano.ocorrencia().isGrave() && plano.acoes().stream().noneMatch(PlanoDeAcao::possuiMedidaPreventiva)) {
                AcaoPreventivaObrigatoriaException excecao = new AcaoPreventivaObrigatoriaException(plano.ocorrencia().getId());
                resultado.adicionar(Problema.comExcecao(excecao.getMessage(), () -> excecao));
            }
        }
    }
}
