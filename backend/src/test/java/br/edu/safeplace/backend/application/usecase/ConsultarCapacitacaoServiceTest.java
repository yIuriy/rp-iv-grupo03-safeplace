package br.edu.safeplace.backend.application.usecase;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoas;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.capacitacao.PendenciasColaboradorDTO;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

class ConsultarCapacitacaoServiceTest {

    private FakeCapacitacaoRepository repositorio;
    private ConsultarCapacitacaoService service;

    @BeforeEach
    void setUp() {
        repositorio = new FakeCapacitacaoRepository();
        service = new ConsultarCapacitacaoService(repositorio);
    }

    @Test
    @DisplayName("UC02 Cenário Alternativo III: Identificar pendência admissional quando colaborador não possui capacitações")
    void deveIdentificarPendenciaIntegracaoAdmissional() {
        PendenciasColaboradorDTO pendencias = service.consultarPendenciasColaborador(1);

        assertThat(pendencias.totalCertificacoes()).isZero();
        assertThat(pendencias.totalTreinamentosRealizados()).isZero();
        assertThat(pendencias.pendenteIntegracaoAdmissional()).isTrue();
        assertThat(pendencias.possuiPendenciasImpeditivas()).isTrue();
    }

    @Test
    @DisplayName("UC02 Regra 1: Alertar certificação próxima do vencimento em 30 dias")
    void deveAlertarCertificacaoProximaDoVencimento() {
        LocalDate hoje = LocalDate.now();
        Certificacao cert = new Certificacao(1, "NR-35", hoje.plusDays(20), hoje.minusMonths(11), pessoa(1));
        repositorio.salvar(cert);

        PendenciasColaboradorDTO pendencias = service.consultarPendenciasColaborador(1);

        assertThat(pendencias.certificacoesProximasVencimento()).isEqualTo(1);
        assertThat(pendencias.alertasVencimento()).hasSize(1);
        assertThat(pendencias.alertasVencimento().get(0).getNome()).isEqualTo("NR-35");
        assertThat(pendencias.pendenteIntegracaoAdmissional()).isFalse();
        assertThat(pendencias.possuiPendenciasImpeditivas()).isFalse();
    }

    @Test
    @DisplayName("UC02 Cenário Alternativo I: Identificar e listar certificações vencidas")
    void deveIdentificarCertificacoesVencidas() {
        LocalDate hoje = LocalDate.now();
        Certificacao certVencida = new Certificacao(1, "NR-10", hoje.minusDays(1), hoje.minusYears(1), pessoa(1));
        repositorio.salvar(certVencida);

        PendenciasColaboradorDTO pendencias = service.consultarPendenciasColaborador(1);

        assertThat(pendencias.certificacoesVencidas()).isEqualTo(1);
        assertThat(pendencias.certificacoesVencidasLista()).hasSize(1);
        assertThat(pendencias.possuiPendenciasImpeditivas()).isTrue();
    }

    @Test
    @DisplayName("UC12 Exceção I: Impedir entrega de EPI quando certificação exigida estiver vencida ou faltar treinamento")
    void deveVerificarImpedimentosParaEpi() {
        LocalDate hoje = LocalDate.now();
        Integer epiId = 100;
        Integer colaboradorId = 1;

        repositorio.salvar(new ExigenciaCapacitacao(1, "NR-10", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, epiId, ExigenciaCapacitacao.TipoAlvo.EPI));
        repositorio.salvar(new ExigenciaCapacitacao(2, "Treinamento Trabalho Seguro", ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, epiId, ExigenciaCapacitacao.TipoAlvo.EPI));

        // Sem nada cadastrado para o colaborador
        List<String> impedimentos = service.verificarImpedimentosParaEpi(colaboradorId, epiId);
        assertThat(impedimentos).hasSize(2);
        assertThat(impedimentos.get(0)).contains("certificação NR-10 ausente ou vencida");
        assertThat(impedimentos.get(1)).contains("treinamento Treinamento Trabalho Seguro não registrado");

        // Colaborador realiza o treinamento e obtém certificação válida
        repositorio.salvar(Treinamento.cadastrar("Treinamento Trabalho Seguro", "Conteúdo", "Instrutor", hoje.minusDays(2), 4, pessoas(List.of(colaboradorId))));
        repositorio.salvar(new Certificacao(1, "NR-10", hoje.plusMonths(6), hoje.minusMonths(6), pessoa(colaboradorId)));

        List<String> impedimentosAposConclusao = service.verificarImpedimentosParaEpi(colaboradorId, epiId);
        assertThat(impedimentosAposConclusao).isEmpty();
    }

    @Test
    @DisplayName("UC12 Exceção I: Impedir entrega de EPI na data exata de vencimento da certificação exigida")
    void deveImpedirEntregaNaDataDeVencimento() {
        LocalDate hoje = LocalDate.now();
        Integer epiId = 101;
        Integer colaboradorId = 3;
        repositorio.salvar(new ExigenciaCapacitacao(1, "NR-10", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, epiId, ExigenciaCapacitacao.TipoAlvo.EPI));

        repositorio.salvar(new Certificacao(1, "NR-10", hoje, hoje.minusYears(1), pessoa(colaboradorId)));
        assertThat(service.verificarImpedimentosParaEpi(colaboradorId, epiId))
                .containsExactly("certificação NR-10 ausente ou vencida");

        repositorio.salvar(new Certificacao(1, "NR-10", hoje.plusDays(1), hoje.minusYears(1), pessoa(colaboradorId)));
        assertThat(service.verificarImpedimentosParaEpi(colaboradorId, epiId)).isEmpty();
    }

    @Test
    @DisplayName("UC02 Cenário Alternativo III: ausência de certificações é pendência mesmo com treinamentos registrados")
    void deveApontarPendenciaSemCertificacoesMesmoComTreinamento() {
        LocalDate hoje = LocalDate.now();
        repositorio.salvar(new Treinamento(30, "Integração", "Normas internas", "Instrutor", hoje.minusDays(3), 8, pessoas(List.of(7))));

        PendenciasColaboradorDTO pendencias = service.consultarPendenciasColaborador(7);

        assertThat(pendencias.totalCertificacoes()).isZero();
        assertThat(pendencias.totalTreinamentosRealizados()).isEqualTo(1);
        assertThat(pendencias.pendenteIntegracaoAdmissional()).isTrue();
        assertThat(pendencias.obterMensagensAvisos())
                .contains("Pendência da trilha inicial de integração", "Pendência de treinamentos admissionais");
    }

    @Test
    @DisplayName("UC02 Exceção I: Impedir alocação em tarefa quando certificação estiver vencida")
    void deveVerificarImpedimentosParaTarefa() {
        LocalDate hoje = LocalDate.now();
        Integer tarefaId = 50;
        Integer colaboradorId = 2;

        repositorio.salvar(new ExigenciaCapacitacao(1, "NR-35", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, tarefaId, ExigenciaCapacitacao.TipoAlvo.TAREFA));

        // Certificação vencida ontem
        repositorio.salvar(new Certificacao(1, "NR-35", hoje.minusDays(1), hoje.minusYears(1), pessoa(colaboradorId)));

        assertThat(service.colaboradorPossuiPendencias(colaboradorId, tarefaId)).isTrue();
        List<String> impedimentos = service.verificarImpedimentosParaTarefa(colaboradorId, tarefaId);
        assertThat(impedimentos).containsExactly("certificação NR-35 ausente ou vencida");
    }

    private static class FakeCapacitacaoRepository implements CapacitacaoRepository {
        private final List<Certificacao> certificacoes = new ArrayList<>();
        private final List<Treinamento> treinamentos = new ArrayList<>();
        private final List<ExigenciaCapacitacao> exigencias = new ArrayList<>();

        @Override
        public Certificacao salvar(Certificacao certificacao) {
            certificacoes.removeIf(c -> c.getId() != null && c.getId().equals(certificacao.getId()));
            certificacoes.add(certificacao);
            return certificacao;
        }

        @Override
        public Treinamento salvar(Treinamento treinamento) {
            treinamentos.removeIf(t -> t.getId() != null && t.getId().equals(treinamento.getId()));
            treinamentos.add(treinamento);
            return treinamento;
        }

        @Override
        public ExigenciaCapacitacao salvar(ExigenciaCapacitacao exigencia) {
            exigencias.removeIf(e -> e.getId() != null && e.getId().equals(exigencia.getId()));
            exigencias.add(exigencia);
            return exigencia;
        }

        @Override
        public List<Certificacao> listarCertificacoes(Integer colaboradorId) {
            return new ArrayList<>(certificacoes.stream()
                    .filter(c -> c.getColaboradorId().equals(colaboradorId))
                    .toList());
        }

        @Override
        public List<Treinamento> listarTreinamentos(Integer colaboradorId) {
            return new ArrayList<>(treinamentos.stream()
                    .filter(t -> t.getColaboradoresIds().contains(colaboradorId))
                    .toList());
        }

        @Override
        public List<ExigenciaCapacitacao> listarExigencias(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId) {
            return new ArrayList<>(exigencias.stream()
                    .filter(e -> e.getAlvo() == tipoAlvo && e.getAlvoId().equals(alvoId))
                    .toList());
        }

        @Override
        public Optional<Certificacao> buscarCertificacao(Integer id) {
            return certificacoes.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Treinamento> buscarTreinamento(Integer id) {
            return treinamentos.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Certificacao> listarCertificacoes() {
            return new ArrayList<>(certificacoes);
        }

        @Override
        public List<Treinamento> listarTreinamentos() {
            return new ArrayList<>(treinamentos);
        }
    }

    @Test
    @DisplayName("UC02 Cenário IV: Filtrar colaboradores por status de certificação (em dia, próxima do vencimento, vencida)")
    void deveFiltrarColaboradoresPorStatusCertificacao() {
        LocalDate hoje = LocalDate.now();

        // Colaborador 1: Vencida
        repositorio.salvar(new Certificacao(1, "NR-10", hoje.minusDays(5), hoje.minusYears(1), pessoa(1)));
        // Colaborador 2: Próxima do vencimento (20 dias)
        repositorio.salvar(new Certificacao(2, "NR-35", hoje.plusDays(20), hoje.minusMonths(11), pessoa(2)));
        // Colaborador 3: Em dia (60 dias)
        repositorio.salvar(new Certificacao(3, "NR-33", hoje.plusDays(60), hoje.minusMonths(6), pessoa(3)));

        List<Integer> vencidos = service.listarColaboradoresPorStatusCertificacao(br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao.VENCIDA);
        List<Integer> proximos = service.listarColaboradoresPorStatusCertificacao(br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao.PROXIMA_DO_VENCIMENTO);
        List<Integer> emDia = service.listarColaboradoresPorStatusCertificacao(br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao.EM_DIA);

        assertThat(vencidos).containsExactly(1);
        assertThat(proximos).containsExactly(2);
        assertThat(emDia).containsExactly(3);
    }

    @Test
    @DisplayName("UC02: Listar todas as certificações e treinamentos com status reavaliado")
    void deveListarTodasCertificacoesETreinamentos() {
        LocalDate hoje = LocalDate.now();
        repositorio.salvar(new Certificacao(10, "NR-35", hoje.plusDays(10), hoje.minusMonths(11), pessoa(1)));
        repositorio.salvar(new Treinamento(20, "Treinamento Trabalho em Altura", "Conteúdo", "Instrutor", hoje.minusMonths(2), 8, pessoas(List.of(1, 2))));

        List<Certificacao> certs = service.listarTodasCertificacoes();
        List<Treinamento> treinos = service.listarTodosTreinamentos();

        assertThat(certs).hasSize(1);
        assertThat(certs.get(0).getStatus()).isEqualTo(br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao.PROXIMA_DO_VENCIMENTO);
        assertThat(treinos).hasSize(1);
        assertThat(treinos.get(0).getColaboradoresIds()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("UC02 Cenários I, II e III: Gerar mensagens textuais de avisos e pendências")
    void deveGerarMensagensAvisosCorretamente() {
        // Sem capacitações: pendência admissional e trilha de integração
        PendenciasColaboradorDTO pendenciaAdmissional = service.consultarPendenciasColaborador(99);
        assertThat(pendenciaAdmissional.obterMensagensAvisos())
                .contains("Pendência da trilha inicial de integração", "Pendência de treinamentos admissionais");

        // Com certificação vencida
        LocalDate hoje = LocalDate.now();
        repositorio.salvar(new Certificacao(1, "NR-10", hoje.minusDays(2), hoje.minusYears(1), pessoa(50)));
        PendenciasColaboradorDTO pendenciaVencida = service.consultarPendenciasColaborador(50);
        assertThat(pendenciaVencida.obterMensagensAvisos())
                .anyMatch(msg -> msg.contains("vencida(s) necessitando de renovação imediata"));
    }
}
