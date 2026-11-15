package br.edu.safeplace.backend.application.relatorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;

@DisplayName("RelatorioAreasRisco - ranking das áreas de maior risco (RF18, #299)")
class RelatorioAreasRiscoTest {

    private static final PeriodoRelatorio NOVEMBRO = new PeriodoRelatorio(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    private final AreaRiscoRepository areas = mock(AreaRiscoRepository.class);
    private final OcorrenciaRepository ocorrencias = mock(OcorrenciaRepository.class);
    private final InspecaoRepository inspecoes = mock(InspecaoRepository.class);
    private final RelatorioAreasRisco relatorio = new RelatorioAreasRisco(areas, ocorrencias, inspecoes);

    private static AreaRisco area(int id, String codigo, NivelPerigo nivel) {
        return new AreaRisco(id, codigo, "Área " + id, null, nivel, null, List.of("Ruído"), "Galpão", null);
    }

    private static Inspecao inspecao(AreaRisco area, LocalDate data, boolean... conformidades) {
        List<ItemVerificado> itens = new java.util.ArrayList<>();
        for (boolean conforme : conformidades) {
            itens.add(new ItemVerificado(null, "Item", conforme, null));
        }
        return new Inspecao(null, area, data, null, itens);
    }

    @Test
    @DisplayName("Ordena por ocorrências, depois por nível de perigo, e conta inspeções só do período")
    void ranking() {
        AreaRisco baixa = area(1, "SET-01", NivelPerigo.BAIXO);
        AreaRisco critica = area(2, "SET-02", NivelPerigo.CRITICO);
        AreaRisco media = area(3, "SET-03", NivelPerigo.MEDIO);
        when(areas.listar()).thenReturn(List.of(baixa, critica, media));
        when(ocorrencias.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of(
                Acidente.builder().idOcorrencia(1).dataOcorrencia(LocalDateTime.of(2026, 11, 2, 8, 0))
                        .gravidade(NivelPerigo.ALTO).area(baixa).build(),
                Incidente.builder().idOcorrencia(2).dataOcorrencia(LocalDateTime.of(2026, 11, 3, 8, 0)).area(baixa).build(),
                Incidente.builder().idOcorrencia(3).dataOcorrencia(LocalDateTime.of(2026, 11, 4, 8, 0)).area(media).build()));
        when(inspecoes.listarPorArea(1)).thenReturn(List.of(
                inspecao(baixa, LocalDate.of(2026, 11, 10), true, false, false),
                inspecao(baixa, LocalDate.of(2026, 10, 10), false)));
        when(inspecoes.listarPorArea(2)).thenReturn(List.of());
        when(inspecoes.listarPorArea(3)).thenReturn(List.of());

        RelatorioOutputDTO documento = relatorio.gerar(NOVEMBRO, LocalDateTime.of(2026, 12, 1, 9, 0));

        assertThat(documento.secoes().get(1).tabela().linhas()).containsExactly(
                List.of("1", "SET-01 · Área 1", "Baixo", "1", "1", "1", "2"),
                List.of("2", "SET-03 · Área 3", "Médio", "0", "1", "0", "0"),
                List.of("3", "SET-02 · Área 2", "Crítico", "0", "0", "0", "0"));
        assertThat(documento.secoes().getFirst().indicadores()).extracting(Indicador::valor)
                .containsExactly("3", "2", "1", "1", "2");
        assertThat(documento.semRegistros()).isFalse();
    }
}
