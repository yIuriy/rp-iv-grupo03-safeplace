package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TreinamentoTest {

    @Test
    @DisplayName("Deve instanciar treinamento coletivo com sucesso")
    void deveInstanciarTreinamentoComSucesso() {
        LocalDate hoje = LocalDate.now();
        Treinamento treinamento = Treinamento.cadastrar(
                "Integração de Segurança",
                "Normas internas e procedimentos de emergência",
                "Carlos Instrutor",
                hoje.minusDays(2),
                8,
                List.of(1, 2, 3)
        );

        assertThat(treinamento.getId()).isNull();
        assertThat(treinamento.getNome()).isEqualTo("Integração de Segurança");
        assertThat(treinamento.getInstrutor()).isEqualTo("Carlos Instrutor");
        assertThat(treinamento.getCargaHoraria()).isEqualTo(8);
        assertThat(treinamento.getColaboradoresIds()).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Diagrama de Classes: Deve verificar status de treinamento unitário e coletivo")
    void deveVerificarStatusTreinamentos() {
        LocalDate hoje = LocalDate.now();
        Treinamento t1 = Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", hoje.minusDays(5), 8, List.of(1));
        Treinamento t2 = Treinamento.cadastrar("NR-35", "Conteúdo", "Instrutor", hoje, 8, List.of(1));

        assertThat(t1.calcularStatus(hoje)).isEqualTo(StatusTreinamento.REALIZADO);
        assertThat(t2.calcularStatus(hoje)).isEqualTo(StatusTreinamento.REALIZADO);
        assertThat(t1.verificarStatusTreinamentos(List.of(t1, t2))).isEqualTo("REALIZADO");
        assertThat(t1.verificarStatusTreinamentos(List.of())).isEqualTo("PENDENTE");
        assertThat(t1.verificarStatusTreinamentos(null)).isEqualTo("PENDENTE");
    }

    @Test
    @DisplayName("Treinamento sem validade nunca vence")
    void treinamentoSemValidadeNaoVence() {
        LocalDate hoje = LocalDate.of(2026, 10, 9);
        Treinamento treinamento = Treinamento.cadastrar("NR-10", "Conteúdo", "Instrutor", hoje.minusYears(20), 8, List.of(1));

        assertThat(treinamento.getDataVencimento()).isEmpty();
        assertThat(treinamento.calcularStatus(hoje)).isEqualTo(StatusTreinamento.REALIZADO);
    }

    @Test
    @DisplayName("Vencimento é a data de realização somada à validade em meses")
    void deveCalcularDataVencimento() {
        Treinamento treinamento = Treinamento.cadastrar("NR-35", "Conteúdo", "Instrutor", LocalDate.of(2025, 10, 9), 8, 12, List.of(1));

        assertThat(treinamento.getDataVencimento()).contains(LocalDate.of(2026, 10, 9));
    }

    @ParameterizedTest(name = "{0} dias antes do vencimento -> {1}")
    @CsvSource({"31,REALIZADO", "30,PROXIMO_DO_VENCIMENTO", "1,PROXIMO_DO_VENCIMENTO", "0,VENCIDO", "-1,VENCIDO"})
    @DisplayName("Fronteiras de vencimento seguem a regra das certificações")
    void deveAplicarFronteirasDeVencimento(long diasParaVencer, StatusTreinamento esperado) {
        LocalDate vencimento = LocalDate.of(2026, 10, 9);
        Treinamento treinamento = Treinamento.cadastrar("NR-35", "Conteúdo", "Instrutor", vencimento.minusMonths(12), 8, 12, List.of(1));

        assertThat(treinamento.calcularStatus(vencimento.minusDays(diasParaVencer))).isEqualTo(esperado);
    }
}
