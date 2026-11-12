package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.edu.safeplace.backend.application.dto.capacitacao.CertificacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ExigenciaCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.TreinamentoOutputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;

class CadastrarCapacitacaoServiceTest {

    private FakeCapacitacaoRepository repositorio;
    private FakeLogAuditoriaRepository auditoria;
    private CadastrarCapacitacaoService service;
    /** Dublê da porta do operador (#236): começa sem sessão. */
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();

    @BeforeEach
    void setUp() {
        repositorio = new FakeCapacitacaoRepository();
        auditoria = new FakeLogAuditoriaRepository();
        service = new CadastrarCapacitacaoService(repositorio, auditoria,
                operador.operadorAtual(mock(UsuarioRepository.class)));
    }

    @Test
    @DisplayName("Adapter (#236): a auditoria registra o operador lido pela porta")
    void auditoriaRegistraOperadorDaPorta() {
        operador.autenticar("gestora@safeplace.com", "GESTOR_SEGURANCA");
        LocalDate hoje = LocalDate.now();

        service.cadastrarCertificacao(new CadastrarCertificacaoInputDTO("NR-10", hoje.plusYears(1), hoje.minusDays(1), 10));

        assertThat(auditoria.logs.get(0).getResponsavel()).isEqualTo("gestora@safeplace.com");
    }

    @Test
    @DisplayName("Adapter (#236): sem operador autenticado, a auditoria registra SISTEMA")
    void semOperadorAuditoriaRegistraSistema() {
        LocalDate hoje = LocalDate.now();

        service.cadastrarCertificacao(new CadastrarCertificacaoInputDTO("NR-10", hoje.plusYears(1), hoje.minusDays(1), 10));

        assertThat(auditoria.logs.get(0).getResponsavel()).isEqualTo(OperadorAtual.SISTEMA);
    }

    @Test
    @DisplayName("Deve cadastrar certificação válida e registrar log de auditoria (#121)")
    void deveCadastrarCertificacaoEAuditar() {
        LocalDate hoje = LocalDate.now();
        CertificacaoOutputDTO cert = service.cadastrarCertificacao(new CadastrarCertificacaoInputDTO("NR-35 Trabalho em Altura", hoje.plusYears(1), hoje.minusDays(5), 10));

        assertThat(cert).isNotNull();
        assertThat(cert.id()).isEqualTo(1);
        assertThat(cert.nome()).isEqualTo("NR-35 Trabalho em Altura");
        assertThat(repositorio.buscarCertificacoesPorColaborador(10)).hasSize(1);

        assertThat(auditoria.logs).hasSize(1);
        LogAuditoria log = auditoria.logs.get(0);
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getEntidade()).isEqualTo("Certificacao");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve recusar certificação com data de vencimento inválida (no passado) sem gerar auditoria")
    void deveRecusarCertificacaoVencidaNoCadastro() {
        LocalDate hoje = LocalDate.now();
        assertThatThrownBy(() -> service.cadastrarCertificacao(new CadastrarCertificacaoInputDTO("NR-35", hoje.minusDays(1), hoje.minusMonths(1), 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser no passado ou presente");

        assertThat(repositorio.certificacoes).isEmpty();
        assertThat(auditoria.logs).isEmpty();
    }

    @Test
    @DisplayName("Deve cadastrar treinamento com colaboradores e registrar log de auditoria (#121)")
    void deveCadastrarTreinamentoEAuditar() {
        LocalDate hoje = LocalDate.now();
        TreinamentoOutputDTO treino = service.cadastrarTreinamento(new CadastrarTreinamentoInputDTO("Prevenção e Combate a Incêndio", "Módulo prático", "Instrutor Bombeiro", hoje.minusDays(2), 16, 12, List.of(10, 20)));

        assertThat(treino).isNotNull();
        assertThat(treino.id()).isEqualTo(1);
        assertThat(treino.validadeMeses()).isEqualTo(12);
        assertThat(repositorio.treinamentos.get(0).getColaboradoresIds()).containsExactlyInAnyOrder(10, 20);

        assertThat(auditoria.logs).hasSize(1);
        LogAuditoria log = auditoria.logs.get(0);
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getEntidade()).isEqualTo("Treinamento");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({"TAREFA,5", "EPI,8"})
    @DisplayName("Deve definir exigência para tarefa ou EPI e registrar auditoria (#121)")
    void deveDefinirExigenciaEAuditar(ExigenciaCapacitacao.TipoAlvo alvo, int alvoId) {
        ExigenciaCapacitacaoOutputDTO ex = service.definirExigencia(
                new DefinirExigenciaInputDTO("NR-10", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, alvo, alvoId));

        assertThat(ex.id()).isEqualTo(1);
        assertThat(ex.alvo()).isEqualTo(alvo);
        assertThat(ex.alvoId()).isEqualTo(alvoId);

        assertThat(auditoria.logs).hasSize(1);
        LogAuditoria log = auditoria.logs.get(0);
        assertThat(log.getEntidade()).isEqualTo("ExigenciaCapacitacao");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    private static class FakeCapacitacaoRepository implements CapacitacaoRepository {
        private final List<Certificacao> certificacoes = new ArrayList<>();
        private final List<Treinamento> treinamentos = new ArrayList<>();
        private final List<ExigenciaCapacitacao> exigencias = new ArrayList<>();
        private int idSequence = 1;

        @Override
        public Certificacao salvarCertificacao(Certificacao certificacao) {
            Certificacao c = new Certificacao(idSequence++, certificacao.getNome(), certificacao.getDataVencimento(), certificacao.getDataEmissao(), certificacao.getColaborador());
            certificacoes.add(c);
            return c;
        }

        @Override
        public Treinamento salvarTreinamento(Treinamento treinamento) {
            Treinamento t = new Treinamento(idSequence++, treinamento.getNome(), treinamento.getConteudo(), treinamento.getInstrutor(), treinamento.getDataRealizacao(), treinamento.getCargaHoraria(), treinamento.getValidadeMeses(), treinamento.getColaboradores());
            treinamentos.add(t);
            return t;
        }

        @Override
        public ExigenciaCapacitacao salvarExigencia(ExigenciaCapacitacao exigencia) {
            ExigenciaCapacitacao e = new ExigenciaCapacitacao(idSequence++, exigencia.getNomeCapacitacao(), exigencia.getTipoCapacitacao(), exigencia.getAlvoId(), exigencia.getAlvo());
            exigencias.add(e);
            return e;
        }

        @Override
        public List<Certificacao> buscarCertificacoesPorColaborador(Integer colaboradorId) {
            return certificacoes.stream().filter(c -> c.getColaboradorId().equals(colaboradorId)).toList();
        }

        @Override
        public List<Treinamento> buscarTreinamentosPorColaborador(Integer colaboradorId) {
            return treinamentos.stream().filter(t -> t.getColaboradoresIds().contains(colaboradorId)).toList();
        }

        @Override
        public List<ExigenciaCapacitacao> buscarExigenciasPorAlvo(ExigenciaCapacitacao.TipoAlvo tipoAlvo, Integer alvoId) {
            return exigencias.stream().filter(e -> e.getAlvo() == tipoAlvo && e.getAlvoId().equals(alvoId)).toList();
        }

        @Override
        public Optional<Certificacao> buscarCertificacaoPorId(Integer id) {
            return certificacoes.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Treinamento> buscarTreinamentoPorId(Integer id) {
            return treinamentos.stream().filter(t -> t.getId().equals(id)).findFirst();
        }

        @Override
        public List<Certificacao> listarTodasCertificacoes() {
            return new ArrayList<>(certificacoes);
        }

        @Override
        public List<Treinamento> listarTodosTreinamentos() {
            return new ArrayList<>(treinamentos);
        }
    }

    private static class FakeLogAuditoriaRepository implements LogAuditoriaRepository {
        private final List<LogAuditoria> logs = new ArrayList<>();
        private int seq = 1;

        @Override
        public LogAuditoria salvar(LogAuditoria log) {
            LogAuditoria salvo = new LogAuditoria(seq++, log.getTipoOperacao(), log.getDataHora(), log.getResponsavel(), log.getJustificativa(), log.getEntidade(), log.getEntidadeId());
            logs.add(salvo);
            return salvo;
        }

        @Override
        public List<LogAuditoria> listarPorEntidade(String entidade, Integer entidadeId) {
            return logs.stream().filter(l -> l.getEntidade().equals(entidade) && l.getEntidadeId().equals(entidadeId)).toList();
        }
    }
}
