/*
 * Testes do domínio de inspeções (RF09, US09, issue #134): registro válido e normalização dos itens.
 * As recusas (data futura, campos ausentes, item incompleto) estão em InspecaoValidadorTest (#263).
 */
package br.edu.safeplace.backend.domain.inspecao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;

class InspecaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 2);
    private static final AreaRisco AREA = new AreaRisco(4, "AR-04", "Solda", null, NivelPerigo.ALTO, List.of());
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
        Inspecao inspecao = Inspecao.registrar(AREA, HOJE, GESTOR, itens());

        assertThat(inspecao.getId()).isNull();
        assertThat(inspecao.getAreaRisco()).isSameAs(AREA);
        assertThat(inspecao.getDataInspecao()).isEqualTo(HOJE);
        assertThat(inspecao.getResponsavel()).isSameAs(GESTOR);
        assertThat(inspecao.getItens()).extracting(ItemVerificado::getDescricao)
                .containsExactly("NR-12: proteções fixas", "NR-26: sinalização");
        assertThat(inspecao.contarNaoConformidades()).isEqualTo(1);
    }

    @Test
    @DisplayName("Observações vazias viram ausentes e a descrição é aparada")
    void itemNormalizaDescricaoEObservacoes() {
        ItemVerificado item = ItemVerificado.novo("  NR-26  ", false, "   ");
        assertThat(item.getDescricao()).isEqualTo("NR-26");
        assertThat(item.isConformidade()).isFalse();
        assertThat(item.getObservacoes()).isNull();
    }
}
