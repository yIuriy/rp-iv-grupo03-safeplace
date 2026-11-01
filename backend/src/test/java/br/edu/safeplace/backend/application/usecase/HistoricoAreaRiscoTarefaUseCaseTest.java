/*
 * Testes do histórico de alterações nos casos de uso de áreas de risco e tarefas (issue #114): o que a
 * atualização grava em LogAuditoria e o que a consulta do histórico devolve, com portas simuladas.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.output.RegistroHistoricoOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AlteracaoAreaRisco;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistoricoAreaRiscoTarefaUseCaseTest {

    private static final Integer ID_AREA = 7;
    private static final Integer ID_TAREFA = 3;

    private AreaRiscoRepository areas;
    private LogAuditoriaRepository auditoria;
    private TarefaRepository tarefas;
    private AreaRiscoService areaUseCase;
    private TarefaService tarefaService;

    @BeforeEach
    void setUp() {
        areas = mock(AreaRiscoRepository.class);
        auditoria = mock(LogAuditoriaRepository.class);
        tarefas = mock(TarefaRepository.class);
        EpiRepository epis = mock(EpiRepository.class);
        SetorRepository setores = mock(SetorRepository.class);

        when(epis.buscarPorId(1)).thenReturn(Optional.of(epi(1, "Capacete")));
        when(epis.buscarPorId(2)).thenReturn(Optional.of(epi(2, "Luva térmica")));
        when(setores.buscarPorId(1)).thenReturn(Optional.of(
                Setor.reconstituir(1, "Usinagem", null, "Galpão 3", NivelPerigo.ALTO)));
        when(areas.buscarPorId(ID_AREA)).thenReturn(Optional.of(new AreaRisco(ID_AREA, "AR-01", "Solda", null,
                NivelPerigo.MEDIO, null, List.of("Ruído"), "Entre as linhas A e B", List.of(epi(1, "Capacete")))));
        when(areas.atualizar(any(AreaRisco.class))).thenAnswer(chamada -> chamada.getArgument(0));

        areaUseCase = new AreaRiscoService(areas, epis, setores, auditoria);
        tarefaService = new TarefaService(tarefas, auditoria, mock(UsuarioRepository.class));
    }

    private static Epi epi(Integer id, String nome) {
        return new Epi(id, nome, "CA-" + id, 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 365, null, null);
    }

    private LogAuditoria logGravado() {
        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(auditoria).salvar(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Atualizar área grava no histórico o valor anterior e o novo de cada campo, e a justificativa")
    void atualizacaoGravaAnteriorNovoEJustificativa() {
        areaUseCase.atualizarAreaRisco(ID_AREA, new AtualizarAreaRiscoInputDTO("Solda", null, NivelPerigo.ALTO, 1,
                List.of("Ruído", "Calor"), "Entre as linhas A e C", List.of(1, 2), "Revisão anual"),
                "gestor@safeplace.com");

        LogAuditoria log = logGravado();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getEntidade()).isEqualTo("AreaRisco");
        assertThat(log.getEntidadeId()).isEqualTo(ID_AREA);
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getJustificativa()).isEqualTo("Alteração da área de risco: "
                + "nível de perigo: de \"MEDIO\" para \"ALTO\"; "
                + "setor: de não informado para \"Usinagem\"; "
                + "agentes de risco: de \"Ruído\" para \"Ruído, Calor\"; "
                + "limites: de \"Entre as linhas A e B\" para \"Entre as linhas A e C\"; "
                + "EPIs obrigatórios: de \"Capacete\" para \"Capacete, Luva térmica\". "
                + "Justificativa: Revisão anual");
    }

    @Test
    @DisplayName("Sem justificativa, o histórico guarda só as mudanças")
    void semJustificativaGuardaSoMudancas() {
        areaUseCase.atualizarAreaRisco(ID_AREA, new AtualizarAreaRiscoInputDTO("Solda", null, NivelPerigo.MEDIO, null,
                List.of("Ruído"), "Linha amarela", List.of(1), "  "), "gestor@safeplace.com");

        assertThat(logGravado().getJustificativa()).isEqualTo(
                "Alteração da área de risco: limites: de \"Entre as linhas A e B\" para \"Linha amarela\".");
    }

    @Test
    @DisplayName("Atualização sem mudança de dados também fica registrada")
    void atualizacaoSemMudancaFicaRegistrada() {
        assertThat(AreaRiscoService.descreverHistorico(List.of(), "Conferência periódica"))
                .isEqualTo("Atualização da área de risco sem mudança de dados. Justificativa: Conferência periódica");
        assertThat(AreaRiscoService.descreverHistorico(
                List.of(new AlteracaoAreaRisco("limites", "A", null)), null))
                .isEqualTo("Alteração da área de risco: limites: de \"A\" para não informado.");
    }

    @Test
    @DisplayName("Histórico da área devolve as entradas na ordem da trilha de auditoria")
    void historicoDaAreaDevolveEntradas() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 2, 14, 30);
        when(auditoria.listarPorEntidade("AreaRisco", ID_AREA)).thenReturn(List.of(
                new LogAuditoria(2, "ALTERACAO", agora, "gestor@safeplace.com", "segunda", "AreaRisco", ID_AREA),
                new LogAuditoria(1, "ALTERACAO", agora.minusDays(1), "gestor@safeplace.com", "primeira", "AreaRisco", ID_AREA)));

        List<RegistroHistoricoOutputDTO> historico = areaUseCase.listarHistorico(ID_AREA);

        assertThat(historico).extracting(RegistroHistoricoOutputDTO::descricao).containsExactly("segunda", "primeira");
        assertThat(historico.get(0)).isEqualTo(new RegistroHistoricoOutputDTO(
                2, "ALTERACAO", agora, "gestor@safeplace.com", "segunda"));
    }

    @Test
    @DisplayName("Histórico de área inexistente responde não encontrada sem consultar a auditoria")
    void historicoDeAreaInexistente() {
        when(areas.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> areaUseCase.listarHistorico(99))
                .isInstanceOf(AreaRiscoNaoEncontradaException.class);
        verify(auditoria, never()).listarPorEntidade(anyString(), anyInt());
    }

    @Test
    @DisplayName("Histórico da tarefa devolve cadastro e classificações gravados na auditoria")
    void historicoDaTarefaDevolveEntradas() {
        when(tarefas.buscarPorId(ID_TAREFA)).thenReturn(Optional.of(new Tarefa(ID_TAREFA, "Solda em altura", null, null)));
        when(auditoria.listarPorEntidade("Tarefa", ID_TAREFA)).thenReturn(List.of(
                new LogAuditoria(5, "ALTERACAO", null, "gestor@safeplace.com",
                        "Classificação de periculosidade da tarefa: de MEDIO para ALTO", "Tarefa", ID_TAREFA),
                new LogAuditoria(4, "CADASTRO", null, "gestor@safeplace.com",
                        "Cadastro de Tarefa: Solda em altura", "Tarefa", ID_TAREFA)));

        assertThat(tarefaService.listarHistorico(ID_TAREFA))
                .extracting(RegistroHistoricoOutputDTO::tipoOperacao)
                .containsExactly("ALTERACAO", "CADASTRO");
    }

    @Test
    @DisplayName("Histórico de tarefa inexistente responde não encontrada")
    void historicoDeTarefaInexistente() {
        when(tarefas.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.listarHistorico(99))
                .isInstanceOf(TarefaNaoEncontradaException.class);
        verify(auditoria, never()).listarPorEntidade(anyString(), anyInt());
    }
}
