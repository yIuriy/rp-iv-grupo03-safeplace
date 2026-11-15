package br.edu.safeplace.backend.domain.epi;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.usecase.ControlarManutencaoEpiService;
import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.suporte.EpiDeTeste;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;
import br.edu.safeplace.backend.validacao.epi.EpiValidador;
import br.edu.safeplace.backend.validacao.epi.ManutencaoEpiValidador;

/**
 * Testes determinísticos para a Strategy de tempo (Clock) nas regras de vencimento de EPI (#247).
 * Verifica as fronteiras de expiração do CA (vence hoje x venceu ontem x vence amanhã).
 */
@DisplayName("EPI - Regras de vencimento com Clock Strategy (GoF)")
class EpiVencimentoClockTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DATA_FIXA = LocalDate.of(2026, 11, 7);
    private static final Instant INSTANTE_FIXO = DATA_FIXA.atStartOfDay(FUSO).toInstant();

    private Clock clock;
    private EpiValidador epiValidador;
    private EpiRepository epiRepository;
    private ControlarManutencaoEpiService manutencaoService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(INSTANTE_FIXO, FUSO);
        epiValidador = new EpiValidador(clock);
        epiRepository = mock(EpiRepository.class);
        ManutencaoEpiRepository manutencaoRepository = mock(ManutencaoEpiRepository.class);
        TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();
        OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();

        manutencaoService = new ControlarManutencaoEpiService(
                epiRepository,
                manutencaoRepository,
                null,
                auditoria,
                new ManutencaoEpiValidador(),
                operador.operadorAtual(mock(UsuarioRepository.class)),
                clock
        );
    }

    private Epi criarEpiComValidade(Integer id, LocalDate validadeCa) {
        return EpiDeTeste.umEpi()
                .comId(id)
                .comNome("Óculos de Proteção")
                .comCa("CA-1234")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(validadeCa)
                .comVidaUtilDias(365)
                .build();
    }

    @Test
    @DisplayName("validarCaValido: CA vencido ontem é recusado com CertificadoAprovacaoVencidoException")
    void deveRecusarCaVencidoOntem() {
        LocalDate hoje = LocalDate.now(clock);
        Epi epiVencidoOntem = criarEpiComValidade(1, hoje.minusDays(1));

        assertThatThrownBy(() -> epiVencidoOntem.validarCaValido(hoje))
                .isInstanceOf(CertificadoAprovacaoVencidoException.class);
    }

    @Test
    @DisplayName("validarCaValido: CA com vencimento na data de hoje ainda é válido")
    void deveAceitarCaQueVenceHoje() {
        LocalDate hoje = LocalDate.now(clock);
        Epi epiVenceHoje = criarEpiComValidade(2, hoje);

        assertThatCode(() -> epiVenceHoje.validarCaValido(hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarCaValido: CA com vencimento amanhã é aceito")
    void deveAceitarCaQueVenceAmanha() {
        LocalDate hoje = LocalDate.now(clock);
        Epi epiVenceAmanha = criarEpiComValidade(3, hoje.plusDays(1));

        assertThatCode(() -> epiVenceAmanha.validarCaValido(hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ControlarManutencaoEpiService: enviarParaManutencao recusa EPI com CA vencido ontem usando Clock fixed")
    void serviceDeveRecusarManutencaoDeEpiComCaVencidoOntem() {
        LocalDate hoje = LocalDate.now(clock);
        Epi epiVencido = criarEpiComValidade(10, hoje.minusDays(1));
        when(epiRepository.buscarPorId(10)).thenReturn(Optional.of(epiVencido));

        assertThatThrownBy(() -> manutencaoService.enviar(10))
                .isInstanceOf(CertificadoAprovacaoVencidoException.class);
    }

    @Test
    @DisplayName("ControlarManutencaoEpiService: enviarParaManutencao aceita EPI com CA que vence hoje usando Clock fixed")
    void serviceDeveAceitarManutencaoDeEpiComCaQueVenceHoje() {
        LocalDate hoje = LocalDate.now(clock);
        Epi epiVigente = criarEpiComValidade(11, hoje);
        when(epiRepository.buscarPorId(11)).thenReturn(Optional.of(epiVigente));
        when(epiRepository.salvar(any(Epi.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatCode(() -> manutencaoService.enviar(11))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("EpiValidador: cadastro de EPI recusa CA vencido ontem e aceita CA vigente com base no Clock injetado")
    void validadorDeveVerificarCaNoCadastroUsandoClock() {
        LocalDate hoje = LocalDate.now(clock);

        Epi novoVencido = EpiDeTeste.umEpi()
                .comId(null)
                .comNome("Luva Térmica")
                .comCa("CA-999")
                .comValidadeCa(hoje.minusDays(1))
                .comQuantidade(5)
                .comEstoqueMinimo(1)
                .build();

        assertThatThrownBy(() -> epiValidador.validar(novoVencido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Não é permitido cadastrar EPI com CA vencido.");

        Epi novoVigenteHoje = EpiDeTeste.umEpi()
                .comId(null)
                .comNome("Luva Térmica")
                .comCa("CA-999")
                .comValidadeCa(hoje)
                .comQuantidade(5)
                .comEstoqueMinimo(1)
                .build();

        assertThatCode(() -> epiValidador.validar(novoVigenteHoje))
                .doesNotThrowAnyException();
    }
}
