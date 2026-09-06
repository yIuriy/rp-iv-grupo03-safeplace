package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EpiTest {

    @Test
    void deveCriarEpiComDadosValidos() {
        Epi epi = new Epi(1, "Óculos de Proteção", "CA-9988", 10, 2,
                StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

        assertEquals(1, epi.getId());
        assertEquals("Óculos de Proteção", epi.getNome());
        assertEquals("CA-9988", epi.getNumeroCa());
        assertEquals(10, epi.getQuantidade());
        assertEquals(2, epi.getEstoqueMinimo());
        assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
        assertFalse(epi.isEstoqueCritico());
    }

    @Test
    void deveAdicionarEstoqueETransitarDeEsgotadoParaDisponivel() {
        Epi epi = new Epi(1, "Luva de Vaqueta", "CA-1234", 0, 5,
                StatusEpi.ESGOTADO, null, null);

        MovimentacaoEstoque mov = epi.adicionarEstoque(15, "Chegada de lote");

        assertEquals(15, epi.getQuantidade());
        assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
        assertEquals(TipoMovimentacao.ENTRADA, mov.getTipo());
        assertEquals(15, mov.getQuantidade());
        assertEquals("Chegada de lote", mov.getMotivo());
    }

    @Test
    void deveRemoverEstoqueETransitarParaEsgotadoAoZerar() {
        Epi epi = new Epi(1, "Protetor Auricular", "CA-5678", 5, 2,
                StatusEpi.DISPONIVEL, null, null);

        MovimentacaoEstoque mov = epi.removerEstoque(5, "Distribuição para equipe");

        assertEquals(0, epi.getQuantidade());
        assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
        assertEquals(TipoMovimentacao.SAIDA, mov.getTipo());
        assertEquals(5, mov.getQuantidade());
        assertTrue(epi.isEstoqueCritico());
    }

    @Test
    void deveLancarExcecaoAoTentarRemoverMaisQueOSaldoDisponivel() {
        Epi epi = new Epi(1, "Capacete", "CA-1111", 10, 3,
                StatusEpi.DISPONIVEL, null, null);

        assertThrows(SaldoInsuficienteException.class, () -> epi.removerEstoque(11, "Retirada excessiva"));
        assertEquals(10, epi.getQuantidade());
    }

    @Test
    void deveDetectarEstoqueCritico() {
        Epi epi = new Epi(1, "Máscara PFF2", "CA-2222", 3, 3,
                StatusEpi.DISPONIVEL, null, null);

        assertTrue(epi.isEstoqueCritico());
    }

    @Test
    void deveValidarCamposObrigatoriosNaConstrucao() {
        assertThrows(IllegalArgumentException.class, () ->
                new Epi(1, "", "CA-111", 5, 1, StatusEpi.DISPONIVEL, null, null));

        assertThrows(IllegalArgumentException.class, () ->
                new Epi(1, "Luva", null, 5, 1, StatusEpi.DISPONIVEL, null, null));

        assertThrows(IllegalArgumentException.class, () ->
                new Epi(1, "Luva", "CA-111", -1, 1, StatusEpi.DISPONIVEL, null, null));
    }
}
