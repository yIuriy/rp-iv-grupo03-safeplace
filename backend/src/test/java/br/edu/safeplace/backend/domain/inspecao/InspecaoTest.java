/*
 * Testes do domínio de inspeções (RF09, US09, issue #134): registro válido, data futura, ausência de
 * área, responsável, data ou itens, e item sem referência avaliada ou sem resultado.
 */
package br.edu.safeplace.backend.domain.inspecao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspecaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 2);
    private static final GestorDeSeguranca GESTOR = new GestorDeSeguranca(1, "52998224725", "Gestora Ana",
            LocalDate.of(1985, 1, 15), "gestora@safeplace.test", "hash", true, LocalDateTime.now(), LocalDateTime.now());

    private static List<ItemVerificado> itens() {
        return List.of(
                ItemVerificado.novo("NR-12: proteções fixas", true, null),
                ItemVerificado.novo("NR-26: sinalização", false, "Faixa apagada"));
    }

    @Test
    @DisplayName("Registra inspeção com área, data, responsável e itens, contando as não conformidades")
    void registraInspecaoValida() {
        Inspecao inspecao = Inspecao.registrar(4, HOJE, GESTOR, itens(), HOJE);

        assertThat(inspecao.getId()).isNull();
        assertThat(inspecao.getAreaRiscoId()).isEqualTo(4);
        assertThat(inspecao.getDataInspecao()).isEqualTo(HOJE);
        assertThat(inspecao.getResponsavel()).isSameAs(GESTOR);
        assertThat(inspecao.getItens()).extracting(ItemVerificado::getDescricao)
                .containsExactly("NR-12: proteções fixas", "NR-26: sinalização");
        assertThat(inspecao.contarNaoConformidades()).isEqualTo(1);
    }

    @Test
    @DisplayName("A data da inspeção não pode ser posterior ao dia do registro")
    void recusaDataFutura() {
        assertThatThrownBy(() -> Inspecao.registrar(4, HOJE.plusDays(1), GESTOR, itens(), HOJE))
                .isInstanceOf(InspecaoInvalidaException.class)
                .hasMessageContaining("posterior a hoje");
    }

    @Test
    @DisplayName("Inspeção sem item verificado não tem resultado e é recusada")
    void recusaSemItens() {
        assertThatThrownBy(() -> Inspecao.registrar(4, HOJE, GESTOR, List.of(), HOJE))
                .isInstanceOf(InspecaoInvalidaException.class)
                .hasMessageContaining("ao menos um item");
        assertThatThrownBy(() -> Inspecao.registrar(4, HOJE, GESTOR, null, HOJE))
                .isInstanceOf(InspecaoInvalidaException.class);
    }

    @Test
    @DisplayName("Área, data e responsável são obrigatórios")
    void exigeAreaDataEResponsavel() {
        assertThatThrownBy(() -> Inspecao.registrar(null, HOJE, GESTOR, itens(), HOJE))
                .isInstanceOf(InspecaoInvalidaException.class);
        assertThatThrownBy(() -> Inspecao.registrar(4, null, GESTOR, itens(), HOJE))
                .isInstanceOf(InspecaoInvalidaException.class);
        assertThatThrownBy(() -> Inspecao.registrar(4, HOJE, null, itens(), HOJE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Item exige referência avaliada e resultado; observações vazias viram ausentes")
    void itemExigeDescricaoEResultado() {
        assertThatThrownBy(() -> ItemVerificado.novo("  ", true, null))
                .isInstanceOf(InspecaoInvalidaException.class);
        assertThatThrownBy(() -> ItemVerificado.novo("NR-26", null, null))
                .isInstanceOf(InspecaoInvalidaException.class)
                .hasMessageContaining("NR-26");

        ItemVerificado item = ItemVerificado.novo("  NR-26  ", false, "   ");
        assertThat(item.getDescricao()).isEqualTo("NR-26");
        assertThat(item.isConformidade()).isFalse();
        assertThat(item.getObservacoes()).isNull();
    }
}
