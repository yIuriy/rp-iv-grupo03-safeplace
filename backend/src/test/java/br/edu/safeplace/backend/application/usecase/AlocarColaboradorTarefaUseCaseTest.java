package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.safeplace.backend.application.dto.input.AlocarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.output.AlocacaoTarefaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ConsultaAptidaoOutputDTO;
import br.edu.safeplace.backend.application.port.out.AlocacaoTarefaRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.ValidadorAptidaoColaboradorService;
import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.AlocacaoTarefa;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlocarColaboradorTarefaUseCase - Orquestração de Alocação e Consulta de Aptidão")
class AlocarColaboradorTarefaUseCaseTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CapacitacaoRepository capacitacaoRepository;

    @Mock
    private AlocacaoTarefaRepository alocacaoTarefaRepository;

    @Mock
    private LogAuditoriaRepository auditoriaRepository;

    private AlocarColaboradorTarefaService useCase;

    private GestorDeSeguranca gestor;
    private Colaborador colaborador;
    private Tarefa tarefa;

    @BeforeEach
    void setUp() {
        useCase = new AlocarColaboradorTarefaService(
                tarefaRepository,
                usuarioRepository,
                capacitacaoRepository,
                alocacaoTarefaRepository,
                auditoriaRepository,
                new ValidadorAptidaoColaboradorService()
        );

        gestor = new GestorDeSeguranca(1, "11122233344", "Gestor Silva", LocalDate.of(1980, 5, 10),
                "gestor@safeplace.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
        colaborador = new Colaborador(2, "55566677788", "Colaborador Santos", LocalDate.of(1995, 3, 20),
                null, true, LocalDateTime.now(), LocalDateTime.now());
        tarefa = new Tarefa(10, "Manutenção Elétrica", NivelPerigo.ALTO, LocalDateTime.now(), gestor);
    }

    @Test
    @DisplayName("Aloca colaborador com sucesso quando apto, gravando alocação e log de auditoria")
    void deveAlocarComSucessoQuandoApto() {
        when(usuarioRepository.buscarPorEmail("gestor@safeplace.com")).thenReturn(Optional.of(gestor));
        when(tarefaRepository.buscarPorId(10)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscarPorId(2)).thenReturn(Optional.of(colaborador));
        when(capacitacaoRepository.buscarCertificacoesPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarTreinamentosPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.TAREFA, 10)).thenReturn(List.of());

        AlocacaoTarefa salva = new AlocacaoTarefa(1, 10, colaborador.getId(), LocalDateTime.now(), gestor.getId(), "Turno 1");
        when(alocacaoTarefaRepository.salvar(any())).thenReturn(salva);

        AlocacaoTarefaOutputDTO resultado = useCase.alocar(10,
                new AlocarColaboradorInputDTO(2, "Turno 1"), "gestor@safeplace.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.tarefaId()).isEqualTo(10);
        assertThat(resultado.colaboradorId()).isEqualTo(colaborador.getId());
        verify(alocacaoTarefaRepository).salvar(any(AlocacaoTarefa.class));
        verify(auditoriaRepository).salvar(any(LogAuditoria.class));
    }

    @Test
    @DisplayName("Bloqueia alocação e não grava nada quando tarefa não é classificada")
    void deveBloquearAlocacaoQuandoTarefaSemClassificacao() {
        Tarefa tarefaSemClassificacao = Tarefa.cadastrar("Tarefa Sem Grau");
        when(usuarioRepository.buscarPorEmail("gestor@safeplace.com")).thenReturn(Optional.of(gestor));
        when(tarefaRepository.buscarPorId(11)).thenReturn(Optional.of(tarefaSemClassificacao));
        when(usuarioRepository.buscarPorId(2)).thenReturn(Optional.of(colaborador));

        assertThatThrownBy(() -> useCase.alocar(11, new AlocarColaboradorInputDTO(2, null), "gestor@safeplace.com"))
                .isInstanceOf(TarefaSemClassificacaoException.class);

        verify(alocacaoTarefaRepository, never()).salvar(any());
        verify(auditoriaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("Bloqueia alocação e não grava nada quando colaborador possui certificação vencida")
    void deveBloquearAlocacaoQuandoCertificacaoVencida() {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-10",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);
        Certificacao certVencida = new Certificacao(1, "NR-10", LocalDate.now().minusDays(5),
                LocalDate.now().minusYears(2), colaborador.getId());

        when(usuarioRepository.buscarPorEmail("gestor@safeplace.com")).thenReturn(Optional.of(gestor));
        when(tarefaRepository.buscarPorId(10)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscarPorId(2)).thenReturn(Optional.of(colaborador));
        when(capacitacaoRepository.buscarCertificacoesPorColaborador(colaborador.getId())).thenReturn(List.of(certVencida));
        when(capacitacaoRepository.buscarTreinamentosPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.TAREFA, 10)).thenReturn(List.of(exigencia));

        assertThatThrownBy(() -> useCase.alocar(10, new AlocarColaboradorInputDTO(2, null), "gestor@safeplace.com"))
                .isInstanceOf(AlocacaoBloqueadaException.class)
                .hasMessageContaining("NR-10");

        verify(alocacaoTarefaRepository, never()).salvar(any());
        verify(auditoriaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("Lança TarefaNaoEncontradaException quando tarefa não existe")
    void deveLancarExcecaoQuandoTarefaNaoExiste() {
        when(usuarioRepository.buscarPorEmail("gestor@safeplace.com")).thenReturn(Optional.of(gestor));
        when(tarefaRepository.buscarPorId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.alocar(999, new AlocarColaboradorInputDTO(2, null), "gestor@safeplace.com"))
                .isInstanceOf(TarefaNaoEncontradaException.class);

        verify(alocacaoTarefaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("Lança UsuarioNaoEncontradoException quando colaborador não existe")
    void deveLancarExcecaoQuandoColaboradorNaoExiste() {
        when(usuarioRepository.buscarPorEmail("gestor@safeplace.com")).thenReturn(Optional.of(gestor));
        when(tarefaRepository.buscarPorId(10)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscarPorId(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.alocar(10, new AlocarColaboradorInputDTO(999, null), "gestor@safeplace.com"))
                .isInstanceOf(UsuarioNaoEncontradoException.class);

        verify(alocacaoTarefaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("Consulta de aptidão retorna apto=true quando requisitos são atendidos")
    void deveRetornarAptoTrueNaConsulta() {
        when(tarefaRepository.buscarPorId(10)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscarPorId(colaborador.getId())).thenReturn(Optional.of(colaborador));
        when(capacitacaoRepository.buscarCertificacoesPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarTreinamentosPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.TAREFA, 10)).thenReturn(List.of());

        ConsultaAptidaoOutputDTO consulta = useCase.consultarAptidao(10, colaborador.getId());

        assertThat(consulta.apto()).isTrue();
        assertThat(consulta.impedimentos()).isEmpty();
    }

    @Test
    @DisplayName("Consulta de aptidão retorna apto=false e lista descritiva quando houver pendência")
    void deveRetornarAptoFalseEImpedimentosNaConsulta() {
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-35",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);

        when(tarefaRepository.buscarPorId(10)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscarPorId(colaborador.getId())).thenReturn(Optional.of(colaborador));
        when(capacitacaoRepository.buscarCertificacoesPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarTreinamentosPorColaborador(colaborador.getId())).thenReturn(List.of());
        when(capacitacaoRepository.buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo.TAREFA, 10)).thenReturn(List.of(exigencia));

        ConsultaAptidaoOutputDTO consulta = useCase.consultarAptidao(10, colaborador.getId());

        assertThat(consulta.apto()).isFalse();
        assertThat(consulta.impedimentos()).hasSize(1);
        assertThat(consulta.impedimentos().get(0)).contains("NR-35");
    }
}
