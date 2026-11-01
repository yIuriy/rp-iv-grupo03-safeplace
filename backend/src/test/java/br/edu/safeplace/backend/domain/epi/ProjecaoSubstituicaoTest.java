package br.edu.safeplace.backend.domain.epi;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjecaoSubstituicaoTest {

    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @Test
    @DisplayName("Deve instanciar projeção com atributos válidos")
    void deveCriarProjecaoComDadosValidos() {
        LocalDate descarte = hoje.plusDays(45);
        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(1, hoje, descarte, "Justificativa teste");

        assertEquals(1, projecao.getEpiId());
        assertEquals(hoje, projecao.getDataProjecao());
        assertEquals(descarte, projecao.getDataEstimadaDescarte());
        assertEquals("Justificativa teste", projecao.getJustificativa());
        assertEquals(45, projecao.calcularDiasRestantes(hoje));
        assertFalse(projecao.isVencido(hoje));
    }

    @Test
    @DisplayName("Deve identificar descarte próximo dentro da janela de antecedência de 30 dias")
    void deveIdentificarDescarteProximo() {
        LocalDate descarteEm20Dias = hoje.plusDays(20);
        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(1, hoje, descarteEm20Dias, "Desgaste");

        assertTrue(projecao.isProximoDoDescarte(hoje, 30L));
        assertFalse(projecao.isProximoDoDescarte(hoje, 15L));
    }

    @Test
    @DisplayName("Deve identificar descarte vencido")
    void deveIdentificarDescarteVencido() {
        LocalDate descartePassado = hoje.minusDays(1);
        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(1, hoje.minusDays(30), descartePassado, "Vencido");

        assertTrue(projecao.isVencido(hoje));
        assertFalse(projecao.isProximoDoDescarte(hoje, 30L));
    }
}
