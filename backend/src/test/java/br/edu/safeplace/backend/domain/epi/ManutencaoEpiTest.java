package br.edu.safeplace.backend.domain.epi;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.epi;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.domain.usuario.Colaborador;

class ManutencaoEpiTest {

    @Test
    void deveCriarManutencaoEpiComBuilderEAssociacoes() {
        LocalDateTime data = LocalDateTime.of(2026, 9, 11, 14, 30);
        Epi epiAssociado = epi(10);
        Colaborador responsavel = pessoa(7);

        ManutencaoEpi manutencao = new ManutencaoEpi.Builder()
                .epi(epiAssociado)
                .realizadaEm(data)
                .tipo(TipoManutencao.PREVENTIVA)
                .descricao("Inspeção periódica de rotina")
                .resultado(ResultadoManutencao.APROVADO)
                .responsavelManutencao("Carlos Silva")
                .responsavel(responsavel)
                .build();

        assertNull(manutencao.getId());
        assertSame(epiAssociado, manutencao.getEpi());
        assertEquals(10, manutencao.getEpiId());
        assertSame(responsavel, manutencao.getResponsavel());
        assertEquals(7, manutencao.getResponsavelId());
        assertEquals(data, manutencao.getDataManutencao());
        assertEquals(TipoManutencao.PREVENTIVA, manutencao.getTipoManutencao());
        assertEquals("Inspeção periódica de rotina", manutencao.getDescricao());
        assertEquals(ResultadoManutencao.APROVADO, manutencao.getResultado());
        assertEquals("Carlos Silva", manutencao.getResponsavelManutencao());
    }

    @Test
    void devePermitirTipoDeManutencaoAusenteNoMvp() {
        ManutencaoEpi manutencao = new ManutencaoEpi.Builder()
                .epi(epi(10))
                .realizadaEm(LocalDateTime.now())
                .tipo(null)
                .descricao("Inspeção concluída")
                .resultado(ResultadoManutencao.APROVADO)
                .responsavelManutencao("Gestor")
                .build();

        assertNull(manutencao.getTipoManutencao());
        assertNull(manutencao.getResponsavel());
        assertNull(manutencao.getResponsavelId());
    }

    @Test
    void igualdadeConsideraIdGeradoQuandoPresente() {
        ManutencaoEpi m1 = new ManutencaoEpi.Builder()
                .epi(epi(10))
                .realizadaEm(LocalDateTime.of(2026, 9, 11, 15, 0))
                .descricao("Revisão")
                .resultado(ResultadoManutencao.APROVADO)
                .responsavelManutencao("Ana")
                .build();
        ManutencaoEpi m2 = new ManutencaoEpi.Builder()
                .epi(epi(10))
                .realizadaEm(LocalDateTime.of(2026, 9, 11, 15, 0))
                .descricao("Revisão")
                .resultado(ResultadoManutencao.APROVADO)
                .responsavelManutencao("Ana")
                .build();

        assertNotEquals(m1, m2);

        ReflectionTestUtils.setField(m1, "id", 5);
        ReflectionTestUtils.setField(m2, "id", 5);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }
}
