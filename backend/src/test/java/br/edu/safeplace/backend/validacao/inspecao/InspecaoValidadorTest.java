/*
 * Recusas do registro de inspeção (RF09, US09). Vieram de InspecaoTest com as mesmas asserções
 * (issue #263): antes, quem lançava era Inspecao.registrar e o construtor de ItemVerificado; agora é
 * a cadeia InspecaoValidador, executada antes do repository.
 */
package br.edu.safeplace.backend.validacao.inspecao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InspecaoValidador - cadeia de validação da inspeção (Chain of Responsibility)")
class InspecaoValidadorTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 2);
    private static final GestorDeSeguranca GESTOR = new GestorDeSeguranca(1, "52998224725", "Gestora Ana",
            LocalDate.of(1985, 1, 15), "gestora@safeplace.test", "hash", true, LocalDateTime.now(), LocalDateTime.now());

    private final InspecaoValidador validador = new InspecaoValidador(() -> HOJE);

    private static List<ItemVerificado> itens() {
        return List.of(
                ItemVerificado.novo("NR-12: proteções fixas", true, null),
                ItemVerificado.novo("NR-26: sinalização", false, "Faixa apagada"));
    }

    @Test
    @DisplayName("Aceita inspeção com área, data de hoje, responsável e itens completos")
    void aceitaInspecaoValida() {
        assertThatCode(() -> validador.validar(Inspecao.registrar(4, HOJE, GESTOR, itens())))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A data da inspeção não pode ser posterior ao dia do registro")
    void recusaDataFutura() {
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, HOJE.plusDays(1), GESTOR, itens())))
                .isInstanceOf(InspecaoInvalidaException.class)
                .hasMessageContaining("posterior a hoje");
    }

    @Test
    @DisplayName("Inspeção sem item verificado não tem resultado e é recusada")
    void recusaSemItens() {
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, HOJE, GESTOR, List.of())))
                .isInstanceOf(InspecaoInvalidaException.class)
                .hasMessageContaining("ao menos um item");
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, HOJE, GESTOR, null)))
                .isInstanceOf(InspecaoInvalidaException.class);
    }

    @Test
    @DisplayName("Área, data e responsável são obrigatórios")
    void exigeAreaDataEResponsavel() {
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(null, HOJE, GESTOR, itens())))
                .isInstanceOf(InspecaoInvalidaException.class);
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, null, GESTOR, itens())))
                .isInstanceOf(InspecaoInvalidaException.class);
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, HOJE, null, itens())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Item exige referência avaliada e resultado")
    void itemExigeDescricaoEResultado() {
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, HOJE, GESTOR,
                List.of(ItemVerificado.novo("  ", true, null)))))
                .isInstanceOf(InspecaoInvalidaException.class);
        assertThatThrownBy(() -> validador.validar(Inspecao.registrar(4, HOJE, GESTOR,
                List.of(ItemVerificado.novo("NR-26", null, null)))))
                .isInstanceOf(InspecaoInvalidaException.class)
                .hasMessageContaining("NR-26");
    }

    @Test
    @DisplayName("Os itens são verificados primeiro, como quando eram validados na conversão")
    void itensVemAntesDaData() {
        Inspecao inspecao = Inspecao.registrar(4, HOJE.plusDays(1), GESTOR, List.of(ItemVerificado.novo("NR-26", null, null)));

        assertThat(validador.verificar(inspecao).mensagens()).containsExactly(
                "Informe se o item \"NR-26\" está em conformidade ou não conformidade.",
                "A data da inspeção não pode ser posterior a hoje.");
    }
}
