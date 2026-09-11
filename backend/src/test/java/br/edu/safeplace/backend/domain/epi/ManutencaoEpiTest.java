package br.edu.safeplace.backend.domain.epi;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ManutencaoEpiTest {

    @Test
    void deveCriarManutencaoEpiComDadosValidos() {
        LocalDateTime data = LocalDateTime.of(2026, 9, 11, 14, 30);
        ManutencaoEpi manutencao = new ManutencaoEpi(
                1, 10, data, TipoManutencao.PREVENTIVA,
                "Inspeção periódica de rotina", ResultadoManutencao.APROVADO, "Carlos Silva"
        );

        assertEquals(1, manutencao.getId());
        assertEquals(10, manutencao.getEpiId());
        assertEquals(data, manutencao.getDataManutencao());
        assertEquals(TipoManutencao.PREVENTIVA, manutencao.getTipoManutencao());
        assertEquals("Inspeção periódica de rotina", manutencao.getDescricao());
        assertEquals(ResultadoManutencao.APROVADO, manutencao.getResultado());
        assertEquals("Carlos Silva", manutencao.getResponsavelManutencao());
    }

    @Test
    void deveCriarNovaManutencaoUsandoMetodoFabrica() {
        LocalDateTime data = LocalDateTime.of(2026, 9, 11, 15, 0);
        ManutencaoEpi manutencao = ManutencaoEpi.novo(
                20, data, TipoManutencao.CORRETIVA,
                "Substituição de correia danificada", ResultadoManutencao.REPROVADO, "Ana Souza"
        );

        assertNull(manutencao.getId());
        assertEquals(20, manutencao.getEpiId());
        assertEquals(data, manutencao.getDataManutencao());
        assertEquals(TipoManutencao.CORRETIVA, manutencao.getTipoManutencao());
        assertEquals(ResultadoManutencao.REPROVADO, manutencao.getResultado());
    }

    @Test
    void deveValidarCamposObrigatoriosNaConstrucao() {
        LocalDateTime agora = LocalDateTime.now();

        assertThrows(IllegalArgumentException.class, () ->
                new ManutencaoEpi(1, null, agora, TipoManutencao.PREVENTIVA, "Desc", ResultadoManutencao.APROVADO, "Resp"));

        assertThrows(IllegalArgumentException.class, () ->
                new ManutencaoEpi(1, 10, null, TipoManutencao.PREVENTIVA, "Desc", ResultadoManutencao.APROVADO, "Resp"));

        assertThrows(IllegalArgumentException.class, () ->
                new ManutencaoEpi(1, 10, agora, null, "Desc", ResultadoManutencao.APROVADO, "Resp"));

        assertThrows(IllegalArgumentException.class, () ->
                new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA, "   ", ResultadoManutencao.APROVADO, "Resp"));

        assertThrows(IllegalArgumentException.class, () ->
                new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA, "Desc", null, "Resp"));

        assertThrows(IllegalArgumentException.class, () ->
                new ManutencaoEpi(1, 10, agora, TipoManutencao.PREVENTIVA, "Desc", ResultadoManutencao.APROVADO, "  "));
    }
}
