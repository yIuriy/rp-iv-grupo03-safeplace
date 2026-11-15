package br.edu.safeplace.backend.domain.capacitacao;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.application.usecase.ConsultarCapacitacaoService;
import br.edu.safeplace.backend.validacao.capacitacao.CertificacaoValidador;

/**
 * Testes determinísticos para a Strategy de tempo (Clock) nas regras de vencimento de Certificação (#247).
 * Verifica as fronteiras de status (vence hoje x venceu ontem x próxima do vencimento x em dia).
 */
@DisplayName("Certificação - Regras de vencimento com Clock Strategy (GoF)")
class CertificacaoVencimentoClockTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DATA_FIXA = LocalDate.of(2026, 11, 7);
    private static final Instant INSTANTE_FIXO = DATA_FIXA.atStartOfDay(FUSO).toInstant();

    private Clock clock;
    private CertificacaoValidador certificacaoValidador;
    private FakeCapacitacaoRepository capacitacaoRepository;
    private ConsultarCapacitacaoService consultarService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(INSTANTE_FIXO, FUSO);
        certificacaoValidador = new CertificacaoValidador(clock);
        capacitacaoRepository = new FakeCapacitacaoRepository();
        consultarService = new ConsultarCapacitacaoService(capacitacaoRepository, clock);
    }

    @Test
    @DisplayName("calcularStatus: Certificação com vencimento ontem está VENCIDA")
    void deveEstarVencidaQuandoVenceuOntem() {
        LocalDate hoje = LocalDate.now(clock);
        Certificacao cert = new Certificacao(1, "NR-10 Segurança em Eletricidade", hoje.minusDays(1), hoje.minusYears(1), pessoa(10), hoje);

        assertThat(cert.getStatus()).isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(cert.estaValidaEm(hoje)).isFalse();
    }

    @Test
    @DisplayName("calcularStatus: Certificação com vencimento na data de hoje já é considerada VENCIDA (UC02)")
    void deveEstarVencidaQuandoVenceHoje() {
        LocalDate hoje = LocalDate.now(clock);
        Certificacao cert = new Certificacao(2, "NR-35 Trabalho em Altura", hoje, hoje.minusYears(1), pessoa(10), hoje);

        assertThat(cert.getStatus()).isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(cert.estaValidaEm(hoje)).isFalse();
    }

    @Test
    @DisplayName("calcularStatus: Certificação com vencimento amanhã está PROXIMA_DO_VENCIMENTO")
    void deveEstarProximaDoVencimentoQuandoVenceAmanha() {
        LocalDate hoje = LocalDate.now(clock);
        Certificacao cert = new Certificacao(3, "NR-35 Trabalho em Altura", hoje.plusDays(1), hoje.minusYears(1), pessoa(10), hoje);

        assertThat(cert.getStatus()).isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
        assertThat(cert.estaValidaEm(hoje)).isTrue();
    }

    @Test
    @DisplayName("calcularStatus: Certificação com vencimento no limite de 30 dias está PROXIMA_DO_VENCIMENTO e com 31 dias está EM_DIA")
    void deveRespeitarLimiteTrintaDiasParaAlerta() {
        LocalDate hoje = LocalDate.now(clock);
        Certificacao certLimite = new Certificacao(4, "NR-12", hoje.plusDays(30), hoje.minusYears(1), pessoa(10), hoje);
        Certificacao certFutura = new Certificacao(5, "NR-12", hoje.plusDays(31), hoje.minusYears(1), pessoa(10), hoje);

        assertThat(certLimite.getStatus()).isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
        assertThat(certFutura.getStatus()).isEqualTo(StatusCertificacao.EM_DIA);
    }

    @Test
    @DisplayName("ConsultarCapacitacaoService: reavalia status de certificações cadastradas usando Clock injetado")
    void serviceDeveReavaliarStatusUsandoClockInjetado() {
        LocalDate hoje = LocalDate.now(clock);

        capacitacaoRepository.salvar(
                new Certificacao(1, "NR-10", hoje.minusDays(1), hoje.minusYears(1), pessoa(10))
        );
        capacitacaoRepository.salvar(
                new Certificacao(2, "NR-35", hoje, hoje.minusYears(1), pessoa(10))
        );
        capacitacaoRepository.salvar(
                new Certificacao(3, "NR-33", hoje.plusDays(15), hoje.minusYears(1), pessoa(10))
        );
        capacitacaoRepository.salvar(
                new Certificacao(4, "NR-12", hoje.plusMonths(6), hoje.minusMonths(6), pessoa(10))
        );

        List<Certificacao> listadas = consultarService.listarCertificacoes(10);
        assertThat(listadas).hasSize(4);

        assertThat(listadas.stream().filter(c -> c.getId() == 1).findFirst().orElseThrow().getStatus())
                .isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(listadas.stream().filter(c -> c.getId() == 2).findFirst().orElseThrow().getStatus())
                .isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(listadas.stream().filter(c -> c.getId() == 3).findFirst().orElseThrow().getStatus())
                .isEqualTo(StatusCertificacao.PROXIMA_DO_VENCIMENTO);
        assertThat(listadas.stream().filter(c -> c.getId() == 4).findFirst().orElseThrow().getStatus())
                .isEqualTo(StatusCertificacao.EM_DIA);
    }

    @Test
    @DisplayName("CertificacaoValidador: cadastro de certificação recusa vencimento no presente/passado usando Clock")
    void validadorDeveRecusarVencimentoPresenteOuPassadoNoCadastroUsandoClock() {
        LocalDate hoje = LocalDate.now(clock);

        Certificacao venceOntem = new Certificacao(null, "NR-10", hoje.minusDays(1), hoje.minusMonths(1), pessoa(10));
        assertThatThrownBy(() -> certificacaoValidador.validar(venceOntem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser no passado ou presente no momento do cadastro.");

        Certificacao venceHoje = new Certificacao(null, "NR-10", hoje, hoje.minusMonths(1), pessoa(10));
        assertThatThrownBy(() -> certificacaoValidador.validar(venceHoje))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser no passado ou presente no momento do cadastro.");

        Certificacao venceAmanha = new Certificacao(null, "NR-10", hoje.plusDays(1), hoje.minusMonths(1), pessoa(10));
        assertThatCode(() -> certificacaoValidador.validar(venceAmanha))
                .doesNotThrowAnyException();
    }

    private static class FakeCapacitacaoRepository implements CapacitacaoRepository {
        private final List<Certificacao> certs = new ArrayList<>();

        @Override
        public Certificacao salvar(Certificacao certificacao) {
            certs.add(certificacao);
            return certificacao;
        }

        @Override
        public List<Certificacao> listarCertificacoes(Integer colaboradorId) {
            return new ArrayList<>(certs.stream().filter(c -> c.getColaboradorId().equals(colaboradorId)).toList());
        }

        @Override
        public Optional<Certificacao> buscarCertificacao(Integer id) {
            return certs.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public List<Certificacao> listarCertificacoes() {
            return new ArrayList<>(certs);
        }

        @Override public Treinamento salvar(Treinamento treinamento) { return treinamento; }
        @Override public List<Treinamento> listarTreinamentos(Integer colaboradorId) { return List.of(); }
        @Override public Optional<Treinamento> buscarTreinamento(Integer id) { return Optional.empty(); }
        @Override public List<Treinamento> listarTreinamentos() { return List.of(); }
        @Override public ExigenciaCapacitacao salvar(ExigenciaCapacitacao exigencia) { return exigencia; }
        @Override public List<ExigenciaCapacitacao> listarExigencias(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId) { return List.of(); }
    }
}
