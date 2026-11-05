/*
 * Testes do caso de uso de inspeções (issue #134) com portas simuladas: Gestor autenticado como
 * responsável, registro do cadastro na trilha de auditoria (RNF05), recusa sem Gestor identificável sem
 * gravar nada, área inexistente e consulta do histórico.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InspecaoServiceTest {

    private static final String EMAIL_GESTOR = "gestora@safeplace.test";
    private static final String EMAIL_SUPERVISOR = "supervisor@safeplace.test";
    private static final AreaRisco AREA = new AreaRisco(4, "AR-04", "Solda", null, NivelPerigo.ALTO, List.of());
    private static final GestorDeSeguranca GESTOR = new GestorDeSeguranca(1, "52998224725", "Gestora Ana",
            LocalDate.of(1985, 1, 15), EMAIL_GESTOR, "hash", true, LocalDateTime.now(), LocalDateTime.now());

    private InspecaoRepository inspecoes;
    private AreaRiscoRepository areas;
    private LogAuditoriaRepository logs;
    private InspecaoService useCase;

    @BeforeEach
    void setUp() {
        inspecoes = mock(InspecaoRepository.class);
        areas = mock(AreaRiscoRepository.class);
        logs = mock(LogAuditoriaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.buscarPorEmail(EMAIL_GESTOR)).thenReturn(Optional.of(GESTOR));
        when(usuarios.buscarPorEmail(EMAIL_SUPERVISOR)).thenReturn(Optional.of(new Supervisor(2, "11144477735",
                "Supervisor Bruno", LocalDate.of(1990, 1, 10), EMAIL_SUPERVISOR, "hash", true,
                LocalDateTime.now(), LocalDateTime.now())));
        when(areas.buscarPorId(4)).thenReturn(Optional.of(AREA));
        when(areas.buscarPorId(99)).thenReturn(Optional.empty());
        when(inspecoes.salvar(any(Inspecao.class))).thenAnswer(chamada -> {
            Inspecao nova = chamada.getArgument(0);
            return new Inspecao(10, nova.getAreaRisco(), nova.getDataInspecao(), nova.getResponsavel(), nova.getItens());
        });
        useCase = new InspecaoService(inspecoes, areas, usuarios, logs);
        autenticarComo(EMAIL_GESTOR);
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    private static void autenticarComo(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null));
    }

    private static RegistrarInspecaoInputDTO registro(LocalDate data) {
        return new RegistrarInspecaoInputDTO(data, List.of(
                new RegistrarInspecaoInputDTO.Item("NR-12: proteções fixas", true, null),
                new RegistrarInspecaoInputDTO.Item("NR-26: sinalização", false, "Faixa apagada")));
    }

    @Test
    @DisplayName("Registra a inspeção com o Gestor autenticado como responsável")
    void registraComGestorAutenticado() {
        InspecaoOutputDTO salva = useCase.registrar(4, registro(LocalDate.now()));

        assertThat(salva.id()).isEqualTo(10);
        assertThat(salva.areaRiscoId()).isEqualTo(4);
        assertThat(salva.responsavelId()).isEqualTo(1);
        assertThat(salva.responsavelNome()).isEqualTo("Gestora Ana");
        assertThat(salva.naoConformidades()).isEqualTo(1);
        assertThat(salva.itens()).extracting(InspecaoOutputDTO.Item::observacoes).containsExactly(null, "Faixa apagada");
    }

    @Test
    @DisplayName("Grava o cadastro da inspeção na trilha de auditoria com o e-mail do Gestor")
    void gravaCadastroNaAuditoria() {
        useCase.registrar(4, registro(LocalDate.of(2026, 9, 10)));

        ArgumentCaptor<LogAuditoria> log = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logs).salvar(log.capture());
        assertThat(log.getValue().getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getValue().getResponsavel()).isEqualTo(EMAIL_GESTOR);
        assertThat(log.getValue().getEntidade()).isEqualTo("Inspecao");
        assertThat(log.getValue().getEntidadeId()).isEqualTo(10);
        assertThat(log.getValue().getJustificativa())
                .isEqualTo("Inspeção de 10/09/2026 na área AR-04 - Solda: 2 itens verificados, 1 não conformidade.");
    }

    @Test
    @DisplayName("Sem Gestor identificável, o registro é recusado e nada é gravado")
    void recusaSemGestor() {
        autenticarComo(EMAIL_SUPERVISOR);
        assertThatThrownBy(() -> useCase.registrar(4, registro(LocalDate.now())))
                .isInstanceOf(AccessDeniedException.class);

        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> useCase.registrar(4, registro(LocalDate.now())))
                .isInstanceOf(AccessDeniedException.class);
        verify(inspecoes, never()).salvar(any());
        verify(logs, never()).salvar(any());
    }

    @Test
    @DisplayName("Área inexistente responde não encontrada no registro e na consulta")
    void areaInexistente() {
        assertThatThrownBy(() -> useCase.registrar(99, registro(LocalDate.now())))
                .isInstanceOf(AreaRiscoNaoEncontradaException.class);
        assertThatThrownBy(() -> useCase.listarPorArea(99))
                .isInstanceOf(AreaRiscoNaoEncontradaException.class);
        verify(inspecoes, never()).salvar(any());
        verify(inspecoes, never()).listarPorArea(any());
    }

    @Test
    @DisplayName("Data futura e item vazio são recusados sem gravar")
    void recusaRegistroInvalido() {
        assertThatThrownBy(() -> useCase.registrar(4, registro(LocalDate.now().plusDays(1))))
                .isInstanceOf(InspecaoInvalidaException.class);
        List<RegistrarInspecaoInputDTO.Item> comVazio = new java.util.ArrayList<>();
        comVazio.add(null);
        assertThatThrownBy(() -> useCase.registrar(4, new RegistrarInspecaoInputDTO(LocalDate.now(), comVazio)))
                .isInstanceOf(InspecaoInvalidaException.class);
        verify(inspecoes, never()).salvar(any());
    }

    @Test
    @DisplayName("Consulta devolve o histórico da área na ordem da porta de saída")
    void consultaHistorico() {
        when(inspecoes.listarPorArea(4)).thenReturn(List.of(
                new Inspecao(12, AREA, LocalDate.of(2026, 9, 30), GESTOR, List.of(new ItemVerificado(3, "NR-26", false, "Faixa"))),
                new Inspecao(11, AREA, LocalDate.of(2026, 9, 1), GESTOR, List.of(new ItemVerificado(2, "NR-26", true, null)))));

        assertThat(useCase.listarPorArea(4)).extracting(InspecaoOutputDTO::id).containsExactly(12, 11);
    }
}
