/*
 * Testes de evidência de auditoria (RNF05): alteração de área de risco e de colaborador, movimentação
 * de estoque de EPI e classificação de tarefa, cada uma seguida da consulta do LogAuditoria gravado.
 */
package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.AtualizarAreaRiscoInputDTO;
import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTarefaInputDTO;
import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.TarefaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Alteração com Consulta de Evidência de Auditoria (RNF05 / Critério de Aceite)")
class AuditoriaEvidenciaIntegracaoTest {

    @Mock
    private LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;

    @Mock
    private AreaRiscoRepositoryPort areaRiscoRepositoryPort;

    @Mock
    private UsuarioRepositorioPorta usuarioRepositorioPorta;

    @Mock
    private TarefaRepositoryPort tarefaRepositoryPort;

    @Mock
    private EpiRepositoryPort epiRepositoryPort;

    @Mock
    private ManutencaoEpiRepositoryPort manutencaoEpiRepositoryPort;

    private List<LogAuditoria> auditoriaGravada;

    @BeforeEach
    void setUp() {
        auditoriaGravada = new ArrayList<>();

        // Simula armazenamento e consulta de evidência no repositório de auditoria
        lenient().when(logAuditoriaRepositoryPort.salvar(any(LogAuditoria.class))).thenAnswer(invocation -> {
            LogAuditoria log = invocation.getArgument(0);
            LogAuditoria persistido = new LogAuditoria(
                    auditoriaGravada.size() + 1,
                    log.getTipoOperacao(),
                    log.getDataHora(),
                    log.getResponsavel(),
                    log.getJustificativa(),
                    log.getEntidade(),
                    log.getEntidadeId()
            );
            auditoriaGravada.add(persistido);
            return persistido;
        });

        lenient().when(logAuditoriaRepositoryPort.listarPorEntidade(anyString(), anyInt())).thenAnswer(invocation -> {
            String entidade = invocation.getArgument(0);
            Integer entidadeId = invocation.getArgument(1);
            return auditoriaGravada.stream()
                    .filter(l -> entidade.equalsIgnoreCase(l.getEntidade()) && entidadeId.equals(l.getEntidadeId()))
                    .toList();
        });

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor.auditor@safeplace.com", "senha")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve realizar alteração de Área de Risco seguida de consulta e verificação de evidência")
    void alteracaoAreaRiscoSeguidaDeConsultaDeEvidencia() {
        Epi epi = new Epi(50, "Capacete", 10, StatusEpi.DISPONIVEL);
        when(epiRepositoryPort.buscarPorId(50)).thenReturn(Optional.of(epi));

        AreaRiscoUseCase areaRiscoUseCase = new AreaRiscoUseCase(
                areaRiscoRepositoryPort, epiRepositoryPort, null, logAuditoriaRepositoryPort
        );

        AreaRisco existente = new AreaRisco(
                10, "AR-01", "Corte", "Desc", NivelPerigo.BAIXO, null, List.of("Ruído"), "Galpão", List.of(epi)
        );
        when(areaRiscoRepositoryPort.buscarPorId(10)).thenReturn(Optional.of(existente));
        when(areaRiscoRepositoryPort.atualizar(any(AreaRisco.class))).thenAnswer(i -> i.getArgument(0));

        AtualizarAreaRiscoInputDTO inputDTO = new AtualizarAreaRiscoInputDTO(
                "Corte Atualizado", "Nova desc", NivelPerigo.ALTO, null, List.of("Ruído"), "Limites", List.of(50), "Reclassificação de risco"
        );

        areaRiscoUseCase.atualizarAreaRisco(10, inputDTO, "gestor.auditor@safeplace.com");

        // Consulta de evidência
        List<LogAuditoria> evidencias = logAuditoriaRepositoryPort.listarPorEntidade("AreaRisco", 10);
        assertThat(evidencias).hasSize(1);

        LogAuditoria evidencia = evidencias.get(0);
        assertThat(evidencia.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(evidencia.getResponsavel()).isEqualTo("gestor.auditor@safeplace.com");
        assertThat(evidencia.getEntidade()).isEqualTo("AreaRisco");
        assertThat(evidencia.getEntidadeId()).isEqualTo(10);
        // Issue #114: a evidência guarda o valor anterior e o novo de cada campo, e depois a justificativa
        assertThat(evidencia.getJustificativa())
                .startsWith("Alteração da área de risco: ")
                .contains("nível de perigo: de \"BAIXO\" para \"ALTO\"")
                .endsWith("Justificativa: Reclassificação de risco");
        assertThat(evidencia.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve realizar alteração cadastral de Colaborador seguida de consulta e verificação de evidência")
    void alteracaoColaboradorSeguidaDeConsultaDeEvidencia() {
        UsuarioCasoDeUso usuarioCasoDeUso = new UsuarioCasoDeUso(
                usuarioRepositorioPorta, null, null, logAuditoriaRepositoryPort
        );

        Colaborador existente = new Colaborador(
                25, "52998224725", "Marcos Silva", LocalDate.of(1989, 3, 15),
                "marcos@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(usuarioRepositorioPorta.buscarPorId(25)).thenReturn(Optional.of(existente));
        when(usuarioRepositorioPorta.salvar(any(Colaborador.class))).thenAnswer(i -> i.getArgument(0));

        AtualizarPessoaInputDTO inputDTO = new AtualizarPessoaInputDTO(
                "Marcos Silva Jr", LocalDate.of(1989, 3, 15), "marcos.jr@safeplace.com"
        );

        usuarioCasoDeUso.atualizarColaborador(25, inputDTO, "gestor.auditor@safeplace.com");

        // Consulta de evidência
        List<LogAuditoria> evidencias = logAuditoriaRepositoryPort.listarPorEntidade("Colaborador", 25);
        assertThat(evidencias).hasSize(1);

        LogAuditoria evidencia = evidencias.get(0);
        assertThat(evidencia.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(evidencia.getResponsavel()).isEqualTo("gestor.auditor@safeplace.com");
        assertThat(evidencia.getEntidade()).isEqualTo("Colaborador");
        assertThat(evidencia.getEntidadeId()).isEqualTo(25);
        assertThat(evidencia.getJustificativa()).isEqualTo("Atualização cadastral de Colaborador");
    }

    @Test
    @DisplayName("Deve realizar movimentação de estoque de EPI seguida de consulta de evidência")
    void movimentacaoEstoqueSeguidaDeConsultaDeEvidencia() {
        Colaborador operador = new Colaborador(
                1, "52998224725", "Gestor", LocalDate.now(), "gestor.auditor@safeplace.com", true, LocalDateTime.now(), LocalDateTime.now()
        );
        when(usuarioRepositorioPorta.buscarPorEmail("gestor.auditor@safeplace.com")).thenReturn(Optional.of(operador));

        EpiUseCase epiUseCase = new EpiUseCase(
                epiRepositoryPort, usuarioRepositorioPorta, logAuditoriaRepositoryPort
        );

        Epi epi = new Epi(50, "Capacete de Segurança", 100, StatusEpi.DISPONIVEL);
        when(epiRepositoryPort.buscarPorIdComLock(50)).thenReturn(Optional.of(epi));
        when(epiRepositoryPort.salvarMovimentacao(any(MovimentacaoEstoque.class))).thenAnswer(i -> i.getArgument(0));

        epiUseCase.registrarMovimentacao(50, TipoMovimentacao.ENTRADA, 20, "Recebimento novo lote", "gestor.auditor@safeplace.com");

        // Consulta de evidência
        List<LogAuditoria> evidencias = logAuditoriaRepositoryPort.listarPorEntidade("Epi", 50);
        assertThat(evidencias).hasSize(1);

        LogAuditoria evidencia = evidencias.get(0);
        assertThat(evidencia.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(evidencia.getResponsavel()).isEqualTo("gestor.auditor@safeplace.com");
        assertThat(evidencia.getEntidade()).isEqualTo("Epi");
        assertThat(evidencia.getEntidadeId()).isEqualTo(50);
        assertThat(evidencia.getJustificativa()).contains("Movimentação de estoque: ENTRADA (20 un)");
    }

    @Test
    @DisplayName("Deve realizar classificação de Tarefa seguida de consulta de evidência")
    void classificacaoTarefaSeguidaDeConsultaDeEvidencia() {
        TarefaUseCase tarefaUseCase = new TarefaUseCase(
                tarefaRepositoryPort, logAuditoriaRepositoryPort, usuarioRepositorioPorta
        );

        GestorDeSeguranca auditor = new GestorDeSeguranca(3, "49216091040", "Gestor Auditor",
                LocalDate.of(1988, 7, 20), "gestor.auditor@safeplace.com", "hash", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(usuarioRepositorioPorta.buscarPorEmail("gestor.auditor@safeplace.com")).thenReturn(Optional.of(auditor));

        Tarefa tarefa = new Tarefa(15, "Manuseio de Prensa", NivelPerigo.BAIXO, LocalDateTime.now());
        when(tarefaRepositoryPort.buscarPorId(15)).thenReturn(Optional.of(tarefa));
        when(tarefaRepositoryPort.salvar(any(Tarefa.class))).thenAnswer(i -> i.getArgument(0));

        tarefaUseCase.classificar(15, NivelPerigo.CRITICO);

        // Consulta de evidência
        List<LogAuditoria> evidencias = logAuditoriaRepositoryPort.listarPorEntidade("Tarefa", 15);
        assertThat(evidencias).hasSize(1);

        LogAuditoria evidencia = evidencias.get(0);
        assertThat(evidencia.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(evidencia.getResponsavel()).isEqualTo("gestor.auditor@safeplace.com");
        assertThat(evidencia.getEntidade()).isEqualTo("Tarefa");
        assertThat(evidencia.getEntidadeId()).isEqualTo(15);
        assertThat(evidencia.getJustificativa())
                .contains("de BAIXO para CRITICO")
                .contains("responsável técnico: Gestor Auditor (id 3)");
    }
}
