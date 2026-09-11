package br.edu.safeplace.backend.domain.epi;

import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    void deveCriarNovoEpiComStatusDisponivelQuandoQuantidadeMaiorQueZero() {
        Epi epi = Epi.novo("Óculos de Proteção", "CA-9988", 10, 2, LocalDate.of(2028, 1, 1), 365);

        assertNull(epi.getId());
        assertEquals("Óculos de Proteção", epi.getNome());
        assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
    }

    @Test
    void deveCriarNovoEpiComStatusEsgotadoQuandoQuantidadeForZero() {
        Epi epi = Epi.novo("Luva", "CA-1234", 0, 5, null, null);

        assertNull(epi.getId());
        assertEquals(StatusEpi.ESGOTADO, epi.getStatus());
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

    @Test
    void deveEnviarEpiParaManutencaoComSucesso() {
        Epi epi = new Epi(1, "Capacete", "CA-111", 5, 1, StatusEpi.DISPONIVEL, LocalDate.of(2030, 1, 1), 365);

        epi.enviarParaManutencao(LocalDate.of(2026, 9, 11));

        assertEquals(StatusEpi.EM_MANUTENCAO, epi.getStatus());
    }

    @Test
    void deveImpedirEnvioParaManutencaoQuandoStatusNaoForDisponivel() {
        Epi epi = new Epi(1, "Capacete", "CA-111", 5, 1, StatusEpi.EM_USO, LocalDate.of(2030, 1, 1), 365);

        assertThrows(EpiIndisponivelParaManutencaoException.class, () ->
                epi.enviarParaManutencao(LocalDate.of(2026, 9, 11)));
    }

    @Test
    void deveBloquearEnvioParaManutencaoQuandoCaEstiverVencido() {
        LocalDate caVencido = LocalDate.of(2025, 1, 1);
        Epi epi = new Epi(1, "Máscara", "CA-222", 5, 1, StatusEpi.DISPONIVEL, caVencido, 180);

        CertificadoAprovacaoVencidoException ex = assertThrows(
                CertificadoAprovacaoVencidoException.class,
                () -> epi.enviarParaManutencao(LocalDate.of(2026, 9, 11))
        );

        assertEquals(1, ex.getEpiId());
        assertEquals("CA-222", ex.getNumeroCa());
        assertEquals(caVencido, ex.getDataValidadeCa());
        assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
    }

    @Test
    void deveConcluirManutencaoAprovadaEVoltarParaDisponivel() {
        Epi epi = new Epi(1, "Óculos", "CA-333", 4, 1, StatusEpi.EM_MANUTENCAO, LocalDate.of(2030, 1, 1), null);
        ManutencaoEpi manutencao = new ManutencaoEpi(
                10, 1, LocalDateTime.now(), TipoManutencao.PREVENTIVA,
                "Ajuste e higienização", ResultadoManutencao.APROVADO, "Técnico Silva"
        );

        epi.concluirManutencao(manutencao);

        assertEquals(StatusEpi.DISPONIVEL, epi.getStatus());
        assertEquals(4, epi.getQuantidade());
    }

    @Test
    void deveConcluirManutencaoReprovadaETransitarParaDescartadoComDecremento() {
        Epi epi = new Epi(1, "Luva", "CA-444", 2, 1, StatusEpi.EM_MANUTENCAO, LocalDate.of(2030, 1, 1), null);
        ManutencaoEpi manutencao = new ManutencaoEpi(
                11, 1, LocalDateTime.now(), TipoManutencao.CORRETIVA,
                "Rasgo estrutural irreparável", ResultadoManutencao.REPROVADO, "Técnico Silva"
        );

        epi.concluirManutencao(manutencao);

        assertEquals(StatusEpi.DESCARTADO, epi.getStatus());
        assertEquals(1, epi.getQuantidade());
    }

    @Test
    void deveImpedirConclusaoDeManutencaoSeEpiNaoEstiverEmManutencao() {
        Epi epi = new Epi(1, "Luva", "CA-444", 2, 1, StatusEpi.DISPONIVEL, LocalDate.of(2030, 1, 1), null);
        ManutencaoEpi manutencao = new ManutencaoEpi(
                12, 1, LocalDateTime.now(), TipoManutencao.PREVENTIVA,
                "Troca", ResultadoManutencao.APROVADO, "Técnico Silva"
        );

        assertThrows(EpiIndisponivelParaManutencaoException.class, () -> epi.concluirManutencao(manutencao));
    }

    @Test
    void deveValidarCaComSucessoQuandoDataFutura() {
        Epi epi = new Epi(1, "Cinto", "CA-555", 1, 1, StatusEpi.DISPONIVEL, LocalDate.of(2028, 1, 1), 365);

        assertDoesNotThrow(() -> epi.validarCaValido(LocalDate.of(2026, 9, 11)));
    }
}
