/*
 * Relatório de acidentes e incidentes (RF18, issue #299): totais do período, distribuição por mês,
 * acidentes por gravidade e ocorrências por área de risco. O período usa a data do fato, como a
 * consulta de ocorrências (#128).
 */
package br.edu.safeplace.backend.application.relatorio;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Secao;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Tabela;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;

@Component
public class RelatorioAcidentes extends Relatorio<List<Ocorrencia>> {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MM/yyyy");
    private static final String SEM_AREA = "Sem área de risco cadastrada";

    private final OcorrenciaRepository ocorrencias;

    public RelatorioAcidentes(OcorrenciaRepository ocorrencias) {
        this.ocorrencias = ocorrencias;
    }

    @Override
    public TipoRelatorio tipo() {
        return TipoRelatorio.ACIDENTES;
    }

    @Override
    protected List<Ocorrencia> buscar(PeriodoRelatorio periodo) {
        return ocorrencias.listar(periodo.comoFiltroDeOcorrencias());
    }

    @Override
    protected List<Indicador> resumir(List<Ocorrencia> dados, PeriodoRelatorio periodo) {
        return List.of(
                indicador("Total de ocorrências", dados.size()),
                indicador("Acidentes", contar(dados, TipoOcorrencia.ACIDENTE)),
                indicador("Incidentes", contar(dados, TipoOcorrencia.INCIDENTE)),
                indicador("Acidentes graves (gravidade alta ou crítica)", dados.stream().filter(RelatorioAcidentes::grave).count()),
                indicador("Ocorrências arquivadas", dados.stream()
                        .filter(o -> o.getStatusOcorrencia() == StatusOcorrencia.ARQUIVADA).count()));
    }

    @Override
    protected List<Secao> detalhar(List<Ocorrencia> dados, PeriodoRelatorio periodo) {
        return List.of(porMes(dados, periodo), porGravidade(dados), porArea(dados));
    }

    @Override
    protected boolean semRegistros(List<Ocorrencia> dados) {
        return dados.isEmpty();
    }

    /** Um mês por linha, inclusive os meses sem ocorrência, para a série não ter buracos. */
    private Secao porMes(List<Ocorrencia> dados, PeriodoRelatorio periodo) {
        List<List<String>> linhas = new ArrayList<>();
        for (YearMonth mes = YearMonth.from(periodo.inicio()); !mes.isAfter(YearMonth.from(periodo.fim())); mes = mes.plusMonths(1)) {
            YearMonth atual = mes;
            List<Ocorrencia> doMes = dados.stream()
                    .filter(o -> YearMonth.from(o.getDataOcorrencia()).equals(atual))
                    .toList();
            linhas.add(List.of(MES.format(mes), numero(contar(doMes, TipoOcorrencia.ACIDENTE)),
                    numero(contar(doMes, TipoOcorrencia.INCIDENTE)), numero(doMes.size())));
        }
        return new Secao("Ocorrências por mês",
                new Tabela(List.of("Mês", "Acidentes", "Incidentes", "Total"), linhas, "Nenhuma ocorrência no período."));
    }

    /** Só acidentes têm gravidade (decisão 2.7 da DS09); todos os níveis aparecem, mesmo zerados. */
    private Secao porGravidade(List<Ocorrencia> dados) {
        List<Ocorrencia> acidentes = dados.stream()
                .filter(o -> o.getTipoOcorrencia() == TipoOcorrencia.ACIDENTE)
                .toList();
        List<List<String>> linhas = new ArrayList<>();
        for (NivelPerigo nivel : NivelPerigo.values()) {
            linhas.add(List.of(rotulo(nivel), numero(acidentes.stream().filter(a -> a.getGravidade() == nivel).count())));
        }
        long semGravidade = acidentes.stream().filter(a -> a.getGravidade() == null).count();
        if (semGravidade > 0) {
            linhas.add(List.of(rotulo(null), numero(semGravidade)));
        }
        return new Secao("Acidentes por gravidade", new Tabela(List.of("Gravidade", "Quantidade"),
                acidentes.isEmpty() ? List.of() : linhas, "Nenhum acidente no período."));
    }

    /** Áreas com mais ocorrências primeiro; empate pelo nome da área. */
    private Secao porArea(List<Ocorrencia> dados) {
        Map<String, List<Ocorrencia>> porArea = dados.stream()
                .collect(Collectors.groupingBy(this::descreverArea, LinkedHashMap::new, Collectors.toList()));
        List<List<String>> linhas = porArea.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, List<Ocorrencia>> e) -> e.getValue().size()).reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(e -> List.of(e.getKey(), numero(contar(e.getValue(), TipoOcorrencia.ACIDENTE)),
                        numero(contar(e.getValue(), TipoOcorrencia.INCIDENTE)), numero(e.getValue().size())))
                .toList();
        return new Secao("Ocorrências por área de risco", new Tabela(
                List.of("Área de risco", "Acidentes", "Incidentes", "Total"), linhas, "Nenhuma ocorrência no período."));
    }

    private String descreverArea(Ocorrencia ocorrencia) {
        AreaRisco area = ocorrencia.getArea();
        return area == null ? SEM_AREA : area.getCodigo() + " · " + area.getNome();
    }

    /** Mesma regra de Acidente.isGrave (UC07), sem depender de a gravidade estar preenchida. */
    private static boolean grave(Ocorrencia ocorrencia) {
        return ocorrencia.getGravidade() != null && ocorrencia.getGravidade().isPeloMenos(NivelPerigo.ALTO);
    }

    private static long contar(List<Ocorrencia> dados, TipoOcorrencia tipo) {
        return dados.stream().filter(o -> o.getTipoOcorrencia() == tipo).count();
    }
}
