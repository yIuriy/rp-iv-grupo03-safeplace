/*
 * Testes do histórico do próprio perfil (RNF05) com a porta de auditoria simulada: consulta pelo
 * e-mail sem espaços, limite ajustado entre 1 e 100, e-mail vazio sem consulta e campos da ação.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.output.AcaoUsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistoricoProprioServiceTest {

    private static final String EMAIL = "gestor.demo@safeplace.com";

    private LogAuditoriaRepository logs;
    private HistoricoProprioService useCase;

    @BeforeEach
    void setUp() {
        logs = mock(LogAuditoriaRepository.class);
        useCase = new HistoricoProprioService(logs);
    }

    @Test
    @DisplayName("Lista as ações do e-mail informado com tipo, data, registro afetado e descrição")
    void listaAcoesDoEmail() {
        LocalDateTime quando = LocalDateTime.of(2026, 10, 5, 20, 53);
        when(logs.listarPorResponsavel(EMAIL, 20)).thenReturn(List.of(
                new LogAuditoria(31, "CADASTRO", quando, EMAIL, "Registro de incidente", "Incidente", 4)));

        List<AcaoUsuarioOutputDTO> acoes = useCase.listar("  " + EMAIL + " ", 20);

        assertThat(acoes).containsExactly(
                new AcaoUsuarioOutputDTO(31, "CADASTRO", quando, "Incidente", 4, "Registro de incidente"));
    }

    @Test
    @DisplayName("Ajusta o limite para a faixa de 1 a 100")
    void ajustaLimite() {
        useCase.listar(EMAIL, 0);
        useCase.listar(EMAIL, 500);

        verify(logs).listarPorResponsavel(EMAIL, 1);
        verify(logs).listarPorResponsavel(EMAIL, 100);
    }

    @Test
    @DisplayName("Sem e-mail, devolve lista vazia sem consultar a auditoria")
    void semEmail() {
        assertThat(useCase.listar(null, 20)).isEmpty();
        assertThat(useCase.listar("  ", 20)).isEmpty();

        verify(logs, never()).listarPorResponsavel(anyString(), anyInt());
    }
}
