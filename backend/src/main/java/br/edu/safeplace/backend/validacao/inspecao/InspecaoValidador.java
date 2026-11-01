package br.edu.safeplace.backend.validacao.inspecao;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;

import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o registro de inspeção (RF09, US09). Antes da issue #263, as regras ficavam em
 * {@code Inspecao.registrar} e no construtor de {@code ItemVerificado}.
 *
 * <p>A ordem repete a antiga: os itens eram convertidos, e validados, antes de registrar a
 * inspeção; depois vinham área, responsável, data e a lista de itens.</p>
 */
@Component
public class InspecaoValidador extends ValidadorEmCadeia<Inspecao> {

    @Autowired
    public InspecaoValidador() {
        this(LocalDate::now);
    }

    /** {@code hoje} permite testar a regra da data futura sem depender do relógio. */
    public InspecaoValidador(Supplier<LocalDate> hoje) {
        super(montarCadeia(hoje));
    }

    private static EloValidacao<Inspecao> montarCadeia(Supplier<LocalDate> hoje) {
        EloValidacao<Inspecao> primeiro = new ItensValidosElo();
        primeiro.encadear(exigir(i -> i.getAreaRiscoId() == null,
                        "Informe a área de risco inspecionada.", InspecaoInvalidaException::new))
                .encadear(exigir(i -> i.getResponsavel() == null,
                        "O registro da inspeção exige o Gestor de Segurança responsável."))
                .encadear(exigir(i -> i.getDataInspecao() == null,
                        "Informe a data em que a inspeção foi realizada.", InspecaoInvalidaException::new))
                .encadear(exigir(i -> i.getDataInspecao() != null && i.getDataInspecao().isAfter(hoje.get()),
                        "A data da inspeção não pode ser posterior a hoje.", InspecaoInvalidaException::new))
                .encadear(exigir(i -> i.getItens().isEmpty(),
                        "Informe ao menos um item verificado com seu resultado.", InspecaoInvalidaException::new));
        return primeiro;
    }
}
