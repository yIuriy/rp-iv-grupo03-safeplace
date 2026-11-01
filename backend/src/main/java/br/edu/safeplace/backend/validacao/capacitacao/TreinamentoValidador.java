package br.edu.safeplace.backend.validacao.capacitacao;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/** Valida o treinamento antes de gravá-lo (RF10, UC02). Antes da issue #265, no construtor. */
@Component
public class TreinamentoValidador extends ValidadorEmCadeia<Treinamento> {

    @Autowired
    public TreinamentoValidador() {
        this(LocalDate::now);
    }

    /** {@code hoje} permite testar a regra da data futura sem depender do relógio. */
    public TreinamentoValidador(Supplier<LocalDate> hoje) {
        super(montarCadeia(hoje));
    }

    private static EloValidacao<Treinamento> montarCadeia(Supplier<LocalDate> hoje) {
        EloValidacao<Treinamento> primeiro = exigir(t -> vazio(t.getNome()), "Nome do treinamento é obrigatório.");
        primeiro.encadear(exigir(t -> t.getDataRealizacao() == null, "Data de realização é obrigatória."))
                .encadear(exigir(t -> t.getDataRealizacao() != null && t.getDataRealizacao().isAfter(hoje.get()),
                        "Data de realização não pode ser no futuro."))
                .encadear(exigir(t -> t.getValidadeMeses() != null && t.getValidadeMeses() < 1,
                        "Validade do treinamento deve ser de ao menos 1 mês."))
                .encadear(exigir(t -> t.getColaboradoresIds().isEmpty(),
                        "Treinamento deve possuir ao menos um colaborador vinculado."));
        return primeiro;
    }
}
