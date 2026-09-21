package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Auditoria - OcorrenciaUseCase")
class OcorrenciaUseCaseAuditoriaTest {

    @Mock
    private OcorrenciaRepositoryPort repository;

    @Mock
    private AreaRiscoRepositoryPort areaRiscoRepository;

    @Mock
    private UsuarioRepositorioPorta usuarioRepository;

    @Mock
    private LogAuditoriaRepositoryPort logAuditoriaRepositoryPort;

    private OcorrenciaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new OcorrenciaUseCase(repository, areaRiscoRepository, usuarioRepository, logAuditoriaRepositoryPort);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("gestor@safeplace.com", "senha")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao registrar Acidente")
    void deveRegistrarAuditoriaAoRegistrarAcidente() {
        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                "Galpão 1",
                "Queda de nível",
                null,
                "FALHA_EPI",
                "Fratura",
                "Fratura no braço",
                null,
                "Hospital"
        );

        Acidente salvo = new Acidente(
                1,
                "Queda de nível",
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of(),
                null,
                List.of(),
                null,
                null,
                "Galpão 1",
                null,
                CausaRaizAcidente.FALHA_EPI,
                "Fratura",
                "Fratura no braço",
                null,
                "Hospital",
                NivelPerigo.ALTO
        );
        when(repository.salvarAcidente(any(Acidente.class))).thenReturn(salvo);

        OcorrenciaOutputDTO dto = useCase.registrarAcidente(input);

        assertThat(dto.idOcorrencia()).isEqualTo(1);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepositoryPort).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao registrar Incidente")
    void deveRegistrarAuditoriaAoRegistrarIncidente() {
        RegistrarIncidenteInputDTO input = new RegistrarIncidenteInputDTO(
                LocalDateTime.of(2026, 9, 20, 11, 0),
                "Setor Solda",
                "Quase acidente por fagulha",
                null,
                "Vazamento de faíscas próximo a inflamáveis",
                "Incêndio"
        );

        Incidente salvo = new Incidente(
                2,
                "Quase acidente por fagulha",
                LocalDateTime.of(2026, 9, 20, 11, 0),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of(),
                null,
                List.of(),
                null,
                null,
                "Setor Solda",
                null,
                "Vazamento de faíscas",
                "Incêndio"
        );
        when(repository.salvarIncidente(any(Incidente.class))).thenReturn(salvo);

        OcorrenciaOutputDTO dto = useCase.registrarIncidente(input);

        assertThat(dto.idOcorrencia()).isEqualTo(2);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepositoryPort).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Incidente");
        assertThat(log.getEntidadeId()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao enviar ocorrencia para triagem")
    void deveRegistrarAuditoriaAoEnviarParaTriagem() {
        Acidente acidente = new Acidente(
                3, "Descricao", LocalDateTime.now(), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, List.of(), null, List.of(), null, null,
                "Local", null, CausaRaizAcidente.OUTRO, null, null, null, null, null
        );
        when(repository.buscarPorId(3)).thenReturn(Optional.of(acidente));
        when(repository.salvarAcidente(any(Acidente.class))).thenReturn(acidente);

        useCase.enviarParaTriagem(3);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepositoryPort).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(3);
        assertThat(log.getJustificativa()).isEqualTo("Envio de ocorrência para triagem");
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao arquivar ocorrencia")
    void deveRegistrarAuditoriaAoArquivar() {
        Acidente acidente = new Acidente(
                4, "Descricao", LocalDateTime.now(), LocalDateTime.now(),
                StatusOcorrencia.EM_TRIAGEM, List.of(), null, List.of(), null, null,
                "Local", null, CausaRaizAcidente.OUTRO, null, null, null, null, null
        );
        when(repository.buscarPorId(4)).thenReturn(Optional.of(acidente));
        when(repository.salvarAcidente(any(Acidente.class))).thenReturn(acidente);

        useCase.arquivar(4);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepositoryPort).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(4);
        assertThat(log.getJustificativa()).isEqualTo("Arquivamento de ocorrência");
    }

    @Test
    @DisplayName("Deve registrar log de auditoria ao consolidar CAT")
    void deveRegistrarAuditoriaAoConsolidarCAT() {
        Acidente acidente = new Acidente(
                5, "Descricao", LocalDateTime.now(), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, List.of(), null, List.of(), null, null,
                "Local", null, CausaRaizAcidente.OUTRO, null, null, null, null, null
        );
        when(repository.buscarAcidentePorId(5)).thenReturn(Optional.of(acidente));
        when(repository.proximoSequencialCAT(anyInt(), anyInt())).thenReturn(1L);
        when(repository.salvarAcidente(any(Acidente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.consolidarCAT(5);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepositoryPort).salvar(captor.capture());

        LogAuditoria log = captor.getValue();
        assertThat(log.getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(log.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(log.getEntidade()).isEqualTo("Acidente");
        assertThat(log.getEntidadeId()).isEqualTo(5);
        assertThat(log.getJustificativa()).contains("Consolidação de CAT com protocolo");
    }

    @Test
    @DisplayName("Falha ao buscar ocorrencia nao deve gerar log de auditoria")
    void falhaNaoGeraLogAuditoria() {
        when(repository.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.arquivar(99))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);

        verifyNoInteractions(logAuditoriaRepositoryPort);
    }
}
