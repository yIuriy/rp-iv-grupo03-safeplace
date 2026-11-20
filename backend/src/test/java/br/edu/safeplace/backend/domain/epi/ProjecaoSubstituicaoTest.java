package br.edu.safeplace.backend.domain.epi;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.epi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProjecaoSubstituicaoTest {

    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @Test
    @DisplayName("Deve instanciar projeção com atributos válidos e associação por objeto")
    void deveCriarProjecaoComDadosValidos() {
        Epi epiAssociado = epi(1);
        LocalDate descarte = hoje.plusDays(45);
        ProjecaoSubstituicao projecao = new ProjecaoSubstituicao(epiAssociado, hoje, descarte, "Justificativa teste");

        assertSame(epiAssociado, projecao.getEpi());
        assertEquals(1, projecao.getEpiId());
        assertEquals(hoje, projecao.getDataProjecao());
        assertEquals(descarte, projecao.getDataEstimadaDescarte());
        assertEquals("Justificativa teste", projecao.getJustificativa());
        assertNotNull(projecao.getCriadoEm());
        assertEquals(45, projecao.calcularDiasRestantes(hoje));
        assertFalse(projecao.isVencido(hoje));
    }

    @Test
    @DisplayName("Deve identificar descarte próximo dentro da janela de antecedência de 30 dias")
    void deveIdentificarDescarteProximo() {
        LocalDate descarteEm20Dias = hoje.plusDays(20);
        ProjecaoSubstituicao projecao = new ProjecaoSubstituicao(epi(1), hoje, descarteEm20Dias, "Desgaste");

        assertTrue(projecao.isProximoDoDescarte(hoje, 30L));
        assertFalse(projecao.isProximoDoDescarte(hoje, 15L));
    }

    @Test
    @DisplayName("Deve identificar descarte vencido")
    void deveIdentificarDescarteVencido() {
        LocalDate descartePassado = hoje.minusDays(1);
        ProjecaoSubstituicao projecao = new ProjecaoSubstituicao(epi(1), hoje.minusDays(30), descartePassado, "Vencido");

        assertTrue(projecao.isVencido(hoje));
        assertFalse(projecao.isProximoDoDescarte(hoje, 30L));
    }

    @Test
    @DisplayName("Igualdade segue identidade por id gerado (decisão #254)")
    void igualdadeSegueIdGerado() {
        ProjecaoSubstituicao p1 = new ProjecaoSubstituicao(epi(1), hoje, hoje.plusDays(10), "A");
        ProjecaoSubstituicao p2 = new ProjecaoSubstituicao(epi(1), hoje, hoje.plusDays(10), "A");

        assertNotEquals(p1, p2);

        ReflectionTestUtils.setField(p1, "id", 12);
        ReflectionTestUtils.setField(p2, "id", 12);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
