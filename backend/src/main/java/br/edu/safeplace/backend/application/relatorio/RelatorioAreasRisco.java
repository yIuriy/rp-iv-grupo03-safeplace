/*
 * Relatório de áreas de maior risco (RF18, issue #299): ranking das áreas pelas ocorrências do período,
 * com o nível de perigo cadastrado (UC03) e o resultado das inspeções do período (RF09).
 */
package br.edu.safeplace.backend.application.relatorio;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Secao;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Tabela;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;

@Component
public class RelatorioAreasRisco extends Relatorio<List<RelatorioAreasRisco.SituacaoArea>> {

    /** Números de uma área no período. */
    public record SituacaoArea(AreaRisco area, long acidentes, long incidentes, long inspecoes, long naoConformidades) {

        long ocorrencias() {
            return acidentes + incidentes;
        }

        int gravidade() {
            return area.getNivelPerigo() == null ? 0 : area.getNivelPerigo().getGravidade();
        }
    }

    /**
     * Maior risco primeiro: mais ocorrências; no empate, maior nível de perigo; depois mais não
     * conformidades; por fim, o código da área, para a ordem ser estável.
     */
    static final Comparator<SituacaoArea> MAIOR_RISCO = Comparator.comparingLong(SituacaoArea::ocorrencias).reversed()
            .thenComparing(Comparator.comparingInt(SituacaoArea::gravidade).reversed())
            .thenComparing(Comparator.comparingLong(SituacaoArea::naoConformidades).reversed())
            .thenComparing(s -> s.area().getCodigo(), Comparator.nullsLast(Comparator.naturalOrder()));

    private final AreaRiscoRepository areas;
    private final OcorrenciaRepository ocorrencias;
    private final InspecaoRepository inspecoes;

    public RelatorioAreasRisco(AreaRiscoRepository areas, OcorrenciaRepository ocorrencias, InspecaoRepository inspecoes) {
        this.areas = areas;
        this.ocorrencias = ocorrencias;
        this.inspecoes = inspecoes;
    }

    @Override
    public TipoRelatorio tipo() {
        return TipoRelatorio.AREAS_RISCO;
    }

    @Override
    protected List<SituacaoArea> buscar(PeriodoRelatorio periodo) {
        List<Ocorrencia> doPeriodo = ocorrencias.listar(periodo.comoFiltroDeOcorrencias());
        return areas.listar().stream()
                .map(area -> situacao(area, doPeriodo, periodo))
                .sorted(MAIOR_RISCO)
                .toList();
    }

    private SituacaoArea situacao(AreaRisco area, List<Ocorrencia> doPeriodo, PeriodoRelatorio periodo) {
        List<Ocorrencia> daArea = doPeriodo.stream()
                .filter(o -> o.getArea() != null && Objects.equals(o.getArea().getId(), area.getId()))
                .toList();
        List<Inspecao> inspecoesDoPeriodo = inspecoes.listarPorArea(area.getId()).stream()
                .filter(i -> periodo.contem(i.getDataInspecao()))
                .toList();
        return new SituacaoArea(area,
                daArea.stream().filter(o -> o.getTipoOcorrencia() == TipoOcorrencia.ACIDENTE).count(),
                daArea.stream().filter(o -> o.getTipoOcorrencia() == TipoOcorrencia.INCIDENTE).count(),
                inspecoesDoPeriodo.size(),
                inspecoesDoPeriodo.stream().mapToLong(Inspecao::contarNaoConformidades).sum());
    }

    @Override
    protected List<Indicador> resumir(List<SituacaoArea> dados, PeriodoRelatorio periodo) {
        return List.of(
                indicador("Áreas de risco cadastradas", dados.size()),
                indicador("Áreas com ocorrência no período", dados.stream().filter(s -> s.ocorrencias() > 0).count()),
                indicador("Áreas de perigo alto ou crítico", dados.stream()
                        .filter(s -> s.area().getNivelPerigo() != null && s.area().getNivelPerigo().isPeloMenos(NivelPerigo.ALTO))
                        .count()),
                indicador("Inspeções realizadas", dados.stream().mapToLong(SituacaoArea::inspecoes).sum()),
                indicador("Itens não conformes nas inspeções", dados.stream().mapToLong(SituacaoArea::naoConformidades).sum()));
    }

    /** Nenhuma ocorrência e nenhuma inspeção no período, em nenhuma área. */
    @Override
    protected boolean semRegistros(List<SituacaoArea> dados) {
        return dados.stream().allMatch(s -> s.ocorrencias() == 0 && s.inspecoes() == 0);
    }

    @Override
    protected List<Secao> detalhar(List<SituacaoArea> dados, PeriodoRelatorio periodo) {
        AtomicInteger posicao = new AtomicInteger();
        List<List<String>> linhas = dados.stream()
                .map(s -> List.of(numero(posicao.incrementAndGet()), s.area().getCodigo() + " · " + s.area().getNome(),
                        rotulo(s.area().getNivelPerigo()), numero(s.acidentes()), numero(s.incidentes()),
                        numero(s.inspecoes()), numero(s.naoConformidades())))
                .toList();
        return List.of(new Secao("Ranking das áreas de maior risco", new Tabela(
                List.of("Posição", "Área de risco", "Nível de perigo", "Acidentes", "Incidentes", "Inspeções", "Não conformidades"),
                linhas, "Nenhuma área de risco cadastrada.")));
    }
}
