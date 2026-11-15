package br.edu.safeplace.backend.validacao.ocorrencia;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.minValue;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida uma ação nova do plano (UC07, cenário principal, passo 5). Antes da issue #266, as regras
 * ficavam em {@code PlanoDeAcao.definir}, nos auxiliares {@code validarResponsavel} e
 * {@code validarPrazo} e no construtor (responsável único e alerta). A ordem é a mesma.
 */
@Component
public class AcaoPlanoValidador extends ValidadorEmCadeia<AcaoDoPlano> {

    public AcaoPlanoValidador() {
        super(montarCadeia());
    }

    private static EloValidacao<AcaoDoPlano> montarCadeia() {
        EloValidacao<AcaoDoPlano> primeiro = exigir(c -> c.ocorrencia() == null || c.ocorrencia().getId() == null,
                "A ação deve estar vinculada a uma ocorrência registrada.", PlanoDeAcaoInvalidoException::new);
        primeiro.encadear(exigir(c -> !c.acao().possuiMedidaCorretiva() && !c.acao().possuiMedidaPreventiva(),
                        "Informe a medida corretiva ou a medida preventiva da ação.", PlanoDeAcaoInvalidoException::new))
                .encadear(exigir(c -> (c.acao().getResponsavelColaborador() == null) == (c.acao().getResponsavelSetor() == null),
                        "Informe um único responsável pela ação: colaborador ou setor.", PlanoDeAcaoInvalidoException::new))
                .encadear(exigir(AcaoPlanoValidador::responsavelSemCadastro,
                        "O responsável pela ação deve estar cadastrado.", PlanoDeAcaoInvalidoException::new))
                .encadear(exigir(c -> c.acao().getPrazo() == null,
                        "O prazo da ação é obrigatório.", PrazoAcaoInvalidoException::new))
                .encadear(exigir(c -> c.acao().getPrazo() != null && c.acao().getPrazo().isBefore(c.hoje()),
                        "O prazo da ação não pode ser anterior à data atual.", PrazoAcaoInvalidoException::new))
                .encadear(exigir(AcaoPlanoValidador::prazoAntesDaOcorrencia,
                        "O prazo da ação não pode ser anterior à data da ocorrência.", PrazoAcaoInvalidoException::new))
                .encadear(exigir(c -> c.acao().isAlertaAtivo() && c.acao().getAntecedenciaAlertaDias() == null,
                        "Informe a antecedência do alerta em dias.", PlanoDeAcaoInvalidoException::new))
                .encadear(exigir(c -> c.acao().isAlertaAtivo() && c.acao().getAntecedenciaAlertaDias() != null
                                && !minValue(c.acao().getAntecedenciaAlertaDias(), 0),
                        "A antecedência do alerta não pode ser negativa.", PlanoDeAcaoInvalidoException::new));
        return primeiro;
    }

    /** Só avalia quando há exatamente um responsável; o elo anterior cobre os demais casos. */
    private static boolean responsavelSemCadastro(AcaoDoPlano contexto) {
        PlanoDeAcao acao = contexto.acao();
        if (acao.getResponsavelColaborador() != null && acao.getResponsavelSetor() == null) {
            return acao.getResponsavelColaborador().getId() == null;
        }
        if (acao.getResponsavelSetor() != null && acao.getResponsavelColaborador() == null) {
            return acao.getResponsavelSetor().getId() == null;
        }
        return false;
    }

    private static boolean prazoAntesDaOcorrencia(AcaoDoPlano contexto) {
        return contexto.acao().getPrazo() != null
                && contexto.ocorrencia() != null
                && contexto.ocorrencia().getDataOcorrencia() != null
                && contexto.acao().getPrazo().isBefore(contexto.ocorrencia().getDataOcorrencia().toLocalDate());
    }
}
