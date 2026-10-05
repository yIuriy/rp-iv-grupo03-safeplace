/*
 * Testes do adaptador de auditoria na consulta das ações de um responsável (RNF05): pede ao
 * repositório só a primeira página com o limite e não consulta sem responsável ou com limite inválido.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogAuditoriaHistoricoProprioTest {

    private final LogAuditoriaJpaRepository repositorio = mock(LogAuditoriaJpaRepository.class);
    private final LogAuditoriaJpaAdapter adapter = new LogAuditoriaJpaAdapter(repositorio);

    @Test
    @DisplayName("Consulta a primeira página do responsável com o limite e converte para o domínio")
    void consultaPrimeiraPagina() {
        LocalDateTime quando = LocalDateTime.of(2026, 10, 5, 20, 53);
        LogAuditoriaEntity entidade = LogAuditoriaEntity.deDominio(
                new LogAuditoria(7, "CADASTRO", quando, "gestor@safeplace.com", "Registro", "Incidente", 4));
        when(repositorio.findByResponsavelOrderByDataHoraDescIdDesc("gestor@safeplace.com", PageRequest.of(0, 20)))
                .thenReturn(List.of(entidade));

        List<LogAuditoria> acoes = adapter.listarPorResponsavel("gestor@safeplace.com", 20);

        assertThat(acoes).singleElement().satisfies(log -> {
            assertThat(log.getEntidade()).isEqualTo("Incidente");
            assertThat(log.getEntidadeId()).isEqualTo(4);
            assertThat(log.getDataHora()).isEqualTo(quando);
        });
    }

    @Test
    @DisplayName("Sem responsável ou com limite menor que 1, não consulta o banco")
    void naoConsultaSemResponsavel() {
        assertThat(adapter.listarPorResponsavel(null, 20)).isEmpty();
        assertThat(adapter.listarPorResponsavel(" ", 20)).isEmpty();
        assertThat(adapter.listarPorResponsavel("gestor@safeplace.com", 0)).isEmpty();

        verify(repositorio, never()).findByResponsavelOrderByDataHoraDescIdDesc(anyString(), any(Pageable.class));
    }
}
