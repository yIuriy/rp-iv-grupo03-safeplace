package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.relatorio.FormatoCsv;
import br.edu.safeplace.backend.adapters.out.relatorio.FormatoPdf;
import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.ArquivoRelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Secao;
import br.edu.safeplace.backend.application.port.out.FormatoRelatorio;
import br.edu.safeplace.backend.application.relatorio.Relatorio;
import br.edu.safeplace.backend.application.relatorio.TipoFormato;
import br.edu.safeplace.backend.application.relatorio.TipoRelatorio;
import br.edu.safeplace.backend.validacao.relatorio.PeriodoRelatorioValidador;

@DisplayName("RelatorioService - escolha de relatório e formato, período e auditoria (RF18, #299)")
class RelatorioServiceTest {

    /** 11/11/2026 às 12:00 em São Paulo. */
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-11T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    /** Relatório mínimo: devolve o período recebido como indicador, para o teste conferir. */
    private static final class RelatorioFixo extends Relatorio<PeriodoRelatorio> {
        private final TipoRelatorio tipo;

        RelatorioFixo(TipoRelatorio tipo) {
            this.tipo = tipo;
        }

        @Override
        public TipoRelatorio tipo() {
            return tipo;
        }

        @Override
        protected PeriodoRelatorio buscar(PeriodoRelatorio periodo) {
            return periodo;
        }

        @Override
        protected List<Indicador> resumir(PeriodoRelatorio dados, PeriodoRelatorio periodo) {
            return List.of(new Indicador("Período", dados.descrever()));
        }

        @Override
        protected List<Secao> detalhar(PeriodoRelatorio dados, PeriodoRelatorio periodo) {
            return List.of();
        }
    }

    private final RegistradorAuditoria auditoria = mock(RegistradorAuditoria.class);

    private RelatorioService servico(List<Relatorio<?>> relatorios, List<FormatoRelatorio> formatos) {
        return new RelatorioService(relatorios, formatos, new PeriodoRelatorioValidador(), auditoria, RELOGIO);
    }

    private RelatorioService servicoCompleto() {
        List<Relatorio<?>> relatorios = Arrays.stream(TipoRelatorio.values()).<Relatorio<?>>map(RelatorioFixo::new).toList();
        return servico(relatorios, List.of(new FormatoCsv(), new FormatoPdf()));
    }

    @Test
    @DisplayName("Sem datas, o período é o mês corrente do relógio")
    void mesCorrentePorPadrao() {
        RelatorioOutputDTO relatorio = servicoCompleto().gerar("acidentes", null, null);

        assertThat(relatorio.inicio()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(relatorio.fim()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(relatorio.geradoEm()).isEqualTo("2026-11-11T12:00");
        assertThat(relatorio.secoes().getFirst().indicadores().getFirst().valor()).isEqualTo("01/11/2026 a 30/11/2026");
    }

    @Test
    @DisplayName("Período inválido é recusado pelo validador antes de buscar dados")
    void periodoInvalido() {
        assertThatThrownBy(() -> servicoCompleto().gerar("acidentes", LocalDate.of(2026, 11, 1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Informe as datas inicial e final do período do relatório.");
    }

    @Test
    @DisplayName("Exporta no formato pedido, com nome de arquivo e emissão auditada")
    void exportaEAudita() {
        ArquivoRelatorioOutputDTO arquivo = servicoCompleto()
                .exportar("areas-risco", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "csv");

        assertThat(arquivo.nome()).isEqualTo("relatorio-areas-risco-2026-01-01-a-2026-12-31.csv");
        assertThat(arquivo.tipoConteudo()).isEqualTo(TipoFormato.CSV.tipoConteudo());
        assertThat(new String(arquivo.conteudo(), StandardCharsets.UTF_8)).contains("Relatório de áreas de maior risco");
        verify(auditoria).operacao(RelatorioService.OPERACAO_AUDITORIA, RelatorioService.ENTIDADE_AUDITORIA, null,
                "Relatório de áreas de maior risco em CSV, período 01/01/2026 a 31/12/2026.");
    }

    @Test
    @DisplayName("Tipo ou formato desconhecido é recusado sem registrar auditoria")
    void tipoOuFormatoDesconhecido() {
        RelatorioService servico = servicoCompleto();

        assertThatThrownBy(() -> servico.gerar("vendas", null, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Tipo de relatório inválido");
        assertThatThrownBy(() -> servico.exportar("acidentes", null, null, "xlsx"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Formato de relatório inválido");
        verify(auditoria, never()).operacao(anyString(), anyString(), org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    @DisplayName("Falta de relatório ou formato para algum tipo impede a criação do serviço")
    void mapasCompletos() {
        assertThatThrownBy(() -> servico(List.of(new RelatorioFixo(TipoRelatorio.ACIDENTES)), List.of(new FormatoCsv(), new FormatoPdf())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nenhuma implementação registrada para TREINAMENTOS.");
        List<Relatorio<?>> todos = Arrays.stream(TipoRelatorio.values()).<Relatorio<?>>map(RelatorioFixo::new).toList();
        assertThatThrownBy(() -> servico(todos, List.of(new FormatoCsv())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nenhuma implementação registrada para PDF.");
    }
}
