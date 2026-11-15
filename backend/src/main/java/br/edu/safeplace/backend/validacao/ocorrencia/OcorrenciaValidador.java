package br.edu.safeplace.backend.validacao.ocorrencia;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o relato e a atualização de ocorrências (RF01, RF13, UC09). Antes da issue #266, as regras
 * ficavam nos construtores de {@code Ocorrencia}, {@code Acidente} e {@code Incidente} e no Value
 * Object {@code NumeroProtocoloCAT}.
 *
 * <p>A ordem repete a antiga: o construtor da classe base verificava primeiro, depois o da
 * subclasse. As regras de cada subtipo só se aplicam a ele.</p>
 */
@Component
public class OcorrenciaValidador extends ValidadorEmCadeia<Ocorrencia> {

    /** Issue #308: usa o mesmo {@link Clock} do projeto (Strategy Clock, #247), com o fuso configurado. */
    @Autowired
    public OcorrenciaValidador(Clock clock) {
        this(() -> LocalDateTime.now(clock));
    }

    public OcorrenciaValidador() {
        this(Clock.systemDefaultZone());
    }

    /** {@code agora} permite testar a regra da data futura sem depender do relógio. */
    public OcorrenciaValidador(Supplier<LocalDateTime> agora) {
        super(montarCadeia(agora));
    }

    private static EloValidacao<Ocorrencia> montarCadeia(Supplier<LocalDateTime> agora) {
        EloValidacao<Ocorrencia> primeiro = exigir(o -> o.getDataOcorrencia() == null, "Data da ocorrência é obrigatória.");
        primeiro.encadear(exigir(o -> o.getDataOcorrencia() != null && o.getDataOcorrencia().isAfter(agora.get()),
                        "Data da ocorrência não pode ser futura.", DataOcorrenciaInvalidaException::new))
                .encadear(exigir(o -> isBlankOrNull(o.getDescricao()), "Descrição é obrigatória."))
                .encadear(exigir(o -> o instanceof Acidente a && a.getCausaRaiz() == null,
                        "Causa raiz é obrigatória para acidentes."))
                .encadear(exigir(o -> o instanceof Acidente a && a.getGravidade() == null,
                        "Gravidade é obrigatória para acidentes."))
                // Mesmo elo usado sozinho pelo ProtocoloCATValidador na consolidação da CAT (#230).
                .encadear(ProtocoloCATValidador.<Ocorrencia>eloFormato())
                .encadear(exigir(o -> o instanceof Incidente i && isBlankOrNull(i.getSituacaoRisco()),
                        "Situação de risco é obrigatória para incidentes."))
                .encadear(exigir(o -> o instanceof Incidente i && isBlankOrNull(i.getPotencialDano()),
                        "Potencial de dano é obrigatório para incidentes."));
        return primeiro;
    }
}
