package br.edu.safeplace.backend.validacao.capacitacao;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida a certificação antes de gravá-la (RF10, UC02). Antes da issue #265, as regras ficavam no
 * construtor e em {@code Certificacao.cadastrar}, que exigia vencimento futuro. A ordem repete a
 * antiga: primeiro o vencimento no cadastro, depois os campos.
 */
@Component
public class CertificacaoValidador extends ValidadorEmCadeia<Certificacao> {

    @Autowired
    public CertificacaoValidador(Clock clock) {
        this(() -> LocalDate.now(clock));
    }

    public CertificacaoValidador() {
        this(Clock.systemDefaultZone());
    }

    /** {@code hoje} permite testar as regras de data sem depender do relógio. */
    public CertificacaoValidador(Supplier<LocalDate> hoje) {
        super(montarCadeia(hoje));
    }

    private static EloValidacao<Certificacao> montarCadeia(Supplier<LocalDate> hoje) {
        EloValidacao<Certificacao> primeiro = exigir(c -> c.getId() == null && c.getDataVencimento() != null
                        && !c.getDataVencimento().isAfter(hoje.get()),
                "Data de vencimento não pode ser no passado ou presente no momento do cadastro.");
        primeiro.encadear(exigir(c -> vazio(c.getNome()), "Nome da certificação é obrigatório."))
                .encadear(exigir(c -> c.getDataEmissao() == null, "Data de emissão é obrigatória."))
                .encadear(exigir(c -> c.getDataVencimento() == null, "Data de vencimento é obrigatória."))
                .encadear(exigir(c -> c.getDataEmissao() != null && c.getDataEmissao().isAfter(hoje.get()),
                        "Data de emissão não pode ser no futuro."))
                .encadear(exigir(c -> Certificacao.validarInconsistenciaCronologica(c.getDataEmissao(), c.getDataVencimento()),
                        "Data de emissão não pode ser posterior à data de vencimento."))
                .encadear(exigir(c -> c.getColaborador() == null || c.getColaboradorId() == null, "Colaborador é obrigatório."));
        return primeiro;
    }
}
