package br.edu.safeplace.backend.application.relatorio;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoas;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.PeriodoRelatorio;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO;
import br.edu.safeplace.backend.application.dto.output.RelatorioOutputDTO.Indicador;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@DisplayName("RelatorioTreinamentos - treinamentos realizados e certificações pendentes (RF18, #299)")
class RelatorioTreinamentosTest {

    private static final PeriodoRelatorio NOVEMBRO = new PeriodoRelatorio(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30));

    private final CapacitacaoRepository capacitacoes = mock(CapacitacaoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final RelatorioTreinamentos relatorio = new RelatorioTreinamentos(capacitacoes, usuarios);

    private static Treinamento treinamento(String nome, LocalDate data, int carga, List<Integer> participantes) {
        return new Treinamento(null, nome, "Conteúdo", "Instrutora", data, carga, pessoas(participantes));
    }

    private RelatorioOutputDTO gerar() {
        when(usuarios.listar()).thenReturn(List.of(
                new Colaborador(1, "52998224725", "Ana", LocalDate.of(1990, 1, 1), null, true, null, null)));
        return relatorio.gerar(NOVEMBRO, LocalDateTime.of(2026, 12, 1, 9, 0));
    }

    @Test
    @DisplayName("Só os treinamentos do período entram, em ordem de data")
    void treinamentosDoPeriodo() {
        when(capacitacoes.listarTodosTreinamentos()).thenReturn(List.of(
                treinamento("NR-35", LocalDate.of(2026, 11, 20), 8, List.of(1, 2)),
                treinamento("NR-10", LocalDate.of(2026, 11, 3), 40, List.of(1)),
                treinamento("Antigo", LocalDate.of(2026, 10, 31), 4, List.of(1))));
        when(capacitacoes.listarTodasCertificacoes()).thenReturn(List.of());

        RelatorioOutputDTO documento = gerar();

        assertThat(documento.secoes().getFirst().indicadores()).extracting(Indicador::valor)
                .startsWith("2", "3", "48");
        assertThat(documento.semRegistros()).isFalse();
        assertThat(documento.secoes().get(1).tabela().linhas()).containsExactly(
                List.of("03/11/2026", "NR-10", "Instrutora", "40", "1"),
                List.of("20/11/2026", "NR-35", "Instrutora", "8", "2"));
    }

    @Test
    @DisplayName("Hook semRegistros: sem treinamento no período e certificações em dia")
    void semRegistros() {
        when(capacitacoes.listarTodosTreinamentos()).thenReturn(List.of(treinamento("Antigo", LocalDate.of(2026, 10, 1), 4, List.of(1))));
        when(capacitacoes.listarTodasCertificacoes()).thenReturn(List.of(
                new Certificacao(1, "NR-12", LocalDate.of(2028, 1, 1), LocalDate.of(2026, 1, 1), pessoa(1))));

        assertThat(gerar().semRegistros()).isTrue();
    }

    @Test
    @DisplayName("Certificações avaliadas no fim do período: vencidas primeiro, com o nome do colaborador")
    void certificacoesPendentes() {
        when(capacitacoes.listarTodosTreinamentos()).thenReturn(List.of());
        when(capacitacoes.listarTodasCertificacoes()).thenReturn(List.of(
                new Certificacao(1, "Brigada", LocalDate.of(2026, 12, 10), LocalDate.of(2025, 12, 10), pessoa(1)),
                new Certificacao(2, "NR-33", LocalDate.of(2026, 11, 15), LocalDate.of(2025, 11, 15), pessoa(9)),
                new Certificacao(3, "NR-12", LocalDate.of(2028, 1, 1), LocalDate.of(2026, 1, 1), pessoa(1))));

        RelatorioOutputDTO documento = gerar();

        assertThat(documento.secoes().get(2).titulo()).isEqualTo("Certificações vencidas ou a vencer em 30/11/2026");
        assertThat(documento.secoes().get(2).tabela().linhas()).containsExactly(
                List.of("Colaborador 9", "NR-33", "15/11/2026", "Vencida"),
                List.of("Ana", "Brigada", "10/12/2026", "Próxima do vencimento"));
        assertThat(documento.secoes().getFirst().indicadores()).extracting(Indicador::valor).endsWith("1", "1");
    }
}
