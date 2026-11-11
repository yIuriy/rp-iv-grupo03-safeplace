package br.edu.safeplace.backend.application.relatorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.relatorio.FormatoCsv;
import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Secao;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;

@DisplayName("RelatorioAcidentes - Template Method do relatório de acidentes e incidentes (RF18, #299)")
class RelatorioAcidentesTest {

    private static final PeriodoRelatorio OUT_A_DEZ = new PeriodoRelatorio(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 31));
    private static final LocalDateTime AGORA = LocalDateTime.of(2027, 1, 2, 8, 0);

    private static final AreaRisco CALDEIRAS = new AreaRisco(1, "SET-01", "Caldeiras", null, NivelPerigo.ALTO,
            null, List.of("Calor"), "Galpão 1", null);
    private static final AreaRisco PATIO = new AreaRisco(2, "SET-02", "Pátio", null, NivelPerigo.BAIXO,
            null, List.of("Tráfego"), "Externo", null);

    private final OcorrenciaRepository repositorio = mock(OcorrenciaRepository.class);
    private final RelatorioAcidentes relatorio = new RelatorioAcidentes(repositorio);

    private static Acidente acidente(int dia, int mes, NivelPerigo gravidade, AreaRisco area) {
        return Acidente.builder().idOcorrencia(dia * 100 + mes).descricao("Acidente")
                .dataOcorrencia(LocalDateTime.of(2026, mes, dia, 10, 0)).gravidade(gravidade).area(area).build();
    }

    private static Incidente incidente(int dia, int mes, AreaRisco area) {
        return Incidente.builder().idOcorrencia(dia * 1000 + mes).descricao("Incidente")
                .dataOcorrencia(LocalDateTime.of(2026, mes, dia, 10, 0)).area(area)
                .statusOcorrencia(StatusOcorrencia.ARQUIVADA).build();
    }

    private RelatorioOutputDTO gerarCom(List<Ocorrencia> ocorrencias) {
        when(repositorio.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(ocorrencias);
        return relatorio.gerar(OUT_A_DEZ, AGORA);
    }

    @Test
    @DisplayName("Template Method: todo relatório começa pelo Resumo e mantém título, período e instante")
    void esqueletoFixo() {
        RelatorioOutputDTO documento = gerarCom(List.of());

        assertThat(documento.tipo()).isEqualTo("ACIDENTES");
        assertThat(documento.titulo()).isEqualTo("Relatório de acidentes e incidentes");
        assertThat(documento.inicio()).isEqualTo(OUT_A_DEZ.inicio());
        assertThat(documento.fim()).isEqualTo(OUT_A_DEZ.fim());
        assertThat(documento.geradoEm()).isEqualTo(AGORA);
        assertThat(documento.semRegistros()).isTrue();
        assertThat(documento.secoes()).extracting(Secao::titulo).containsExactly(
                "Resumo", "Ocorrências por mês", "Acidentes por gravidade", "Ocorrências por área de risco");
    }

    @Test
    @DisplayName("Resumo conta acidentes, incidentes, graves e arquivadas")
    void resumo() {
        RelatorioOutputDTO documento = gerarCom(List.of(
                acidente(3, 10, NivelPerigo.CRITICO, CALDEIRAS),
                acidente(9, 11, NivelPerigo.BAIXO, PATIO),
                incidente(15, 11, CALDEIRAS)));

        assertThat(documento.secoes().getFirst().indicadores()).extracting(Indicador::valor)
                .containsExactly("3", "2", "1", "1", "1");
        assertThat(documento.semRegistros()).isFalse();
    }

    @Test
    @DisplayName("Hook semRegistros: período vazio escreve o aviso logo depois do cabeçalho do arquivo")
    void avisoSemRegistrosNoArquivo() {
        when(repositorio.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of());

        String csv = new String(relatorio.exportar(OUT_A_DEZ, AGORA, new FormatoCsv()), StandardCharsets.UTF_8);

        assertThat(csv.lines().toList().get(2)).isEqualTo(Relatorio.AVISO_SEM_REGISTROS);
    }

    @Test
    @DisplayName("Uma linha por mês do período, inclusive meses sem ocorrência")
    void porMes() {
        RelatorioOutputDTO documento = gerarCom(List.of(acidente(3, 10, NivelPerigo.ALTO, CALDEIRAS), incidente(15, 12, PATIO)));

        assertThat(documento.secoes().get(1).tabela().linhas()).containsExactly(
                List.of("10/2026", "1", "0", "1"),
                List.of("11/2026", "0", "0", "0"),
                List.of("12/2026", "0", "1", "1"));
    }

    @Test
    @DisplayName("Gravidade lista todos os níveis; área ordena pela quantidade de ocorrências")
    void gravidadeEArea() {
        RelatorioOutputDTO documento = gerarCom(List.of(
                acidente(3, 10, NivelPerigo.ALTO, PATIO),
                acidente(4, 10, NivelPerigo.ALTO, CALDEIRAS),
                incidente(5, 10, CALDEIRAS),
                incidente(6, 10, null)));

        assertThat(documento.secoes().get(2).tabela().linhas()).containsExactly(
                List.of("Baixo", "0"), List.of("Médio", "0"), List.of("Alto", "2"), List.of("Crítico", "0"));
        assertThat(documento.secoes().get(3).tabela().linhas()).containsExactly(
                List.of("SET-01 · Caldeiras", "1", "1", "2"),
                List.of("SET-02 · Pátio", "1", "0", "1"),
                List.of("Sem área de risco cadastrada", "0", "1", "1"));
    }

    @Test
    @DisplayName("Sem acidentes, a tabela de gravidade fica vazia com aviso")
    void semAcidentes() {
        RelatorioOutputDTO documento = gerarCom(List.of(incidente(5, 10, PATIO)));

        assertThat(documento.secoes().get(2).tabela().vazia()).isTrue();
        assertThat(documento.secoes().get(2).tabela().semDados()).isEqualTo("Nenhum acidente no período.");
    }
}
