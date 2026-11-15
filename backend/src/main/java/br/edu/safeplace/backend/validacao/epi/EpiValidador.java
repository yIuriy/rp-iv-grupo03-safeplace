package br.edu.safeplace.backend.validacao.epi;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;
import static org.apache.commons.validator.GenericValidator.minValue;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o EPI antes do cadastro (RF03, UC06). Antes da issue #264, as regras ficavam no construtor
 * privado de {@code Epi}, em {@code LoteEPI}, em {@code EspecificacaoEPI} e nas fábricas
 * {@code Epi.novo}, que recusavam CA vencido.
 *
 * <p>A ordem repete a antiga: lote e especificação eram montados antes do EPI, depois vinham os
 * campos do EPI e, por último, o CA vigente na data do cadastro.</p>
 */
@Component
public class EpiValidador extends ValidadorEmCadeia<Epi> {

    @Autowired
    public EpiValidador(Clock clock) {
        this(() -> LocalDate.now(clock));
    }

    public EpiValidador() {
        this(Clock.systemDefaultZone());
    }

    /** {@code hoje} permite testar a regra do CA vencido sem depender do relógio. */
    public EpiValidador(Supplier<LocalDate> hoje) {
        super(montarCadeia(hoje));
    }

    private static EloValidacao<Epi> montarCadeia(Supplier<LocalDate> hoje) {
        EloValidacao<Epi> primeiro = exigir(e -> e.getLote() != null && e.getLote().getModelo() == null,
                "Modelo do EPI é obrigatório para o lote.");
        primeiro.encadear(exigir(e -> modelo(e) != null && !minValue(modelo(e).getCa(), 1), "CA inválido."))
                .encadear(exigir(e -> modelo(e) != null && modelo(e).getValidadeCA() == null,
                        "Data de validade do CA é obrigatória."))
                .encadear(exigir(e -> e.getEspecificacao() != null && !minValue(e.getEspecificacao().getQuantidadeMinima(), 0),
                        "Quantidade mínima não pode ser negativa."))
                .encadear(exigir(e -> isBlankOrNull(e.getNome()), "Nome do EPI é obrigatório."))
                .encadear(exigir(e -> !minValue(e.getQuantidade(), 0), "Quantidade em estoque não pode ser negativa."))
                .encadear(exigir(e -> e.getEspecificacao() == null, "Especificação do EPI é obrigatória."))
                .encadear(exigir(e -> e.getStatus() == null, "Status do EPI é obrigatório."))
                .encadear(exigir(e -> e.getLote() == null, "Lote do EPI é obrigatório."))
                .encadear(exigir(e -> caVencidoNoCadastro(e, hoje.get()), "Não é permitido cadastrar EPI com CA vencido."));
        return primeiro;
    }

    private static ModeloEPI modelo(Epi epi) {
        return epi.getLote() != null ? epi.getLote().getModelo() : null;
    }

    /** Só o cadastro (sem id) exige CA vigente; um EPI gravado pode ter o CA vencido depois. */
    private static boolean caVencidoNoCadastro(Epi epi, LocalDate hoje) {
        if (epi.getId() != null || epi.getLote() == null) {
            return false;
        }
        ModeloEPI modelo = epi.getLote().getModelo();
        return modelo != null && modelo.getValidadeCA() != null && modelo.getValidadeCA().isBefore(hoje);
    }
}
