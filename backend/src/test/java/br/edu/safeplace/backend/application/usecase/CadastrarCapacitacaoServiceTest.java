package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepositoryPort;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

class CadastrarCapacitacaoServiceTest {

    private FakeCapacitacaoRepository repositorio;
    private FakeLogAuditoriaRepository auditoria;
    private CadastrarCapacitacaoService service;

    @BeforeEach
    void setUp() {
        repositorio = new FakeCapacitacaoRepository();
        auditoria = new FakeLogAuditoriaRepository();
        service = new CadastrarCapacitacaoService(repositorio, auditoria);
    }

    @Test
    @DisplayName("Deve cadastrar certificação válida e registrar log de auditoria (#121)")
    void deveCadastrarCertificacaoEAuditar() {
        LocalDate hoje = LocalDate.now();
        Certificacao cert = service.cadastrarCertificacao("NR-35 Trabalho em Altura", hoje.plusYears(1), hoje.minusDays(5), 10);

        assertThat(cert).isNotNull();
        assertThat(cert.getId()).isEqualTo(1);
        assertThat(cert.getNome()).isEqualTo("NR-35 Trabalho em Altura");
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
        assertThatThrownBy(() -> service.cadastrarCertificacao("NR-35", hoje.minusDays(1), hoje.minusMonths(1), 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de vencimento não pode ser no passado ou presente");

        assertThat(repositorio.certificacoes).isEmpty();
        assertThat(auditoria.logs).isEmpty();
    }

    @Test
    @DisplayName("Deve cadastrar treinamento com colaboradores e registrar log de auditoria (#121)")
    void deveCadastrarTreinamentoEAuditar() {
        LocalDate hoje = LocalDate.now();
        Treinamento treino = service.cadastrarTreinamento("Prevenção e Combate a Incêndio", "Módulo prático", "Instrutor Bombeiro", hoje.minusDays(2), 16, List.of(10, 20));

        assertThat(treino).isNotNull();
        assertThat(treino.getId()).isEqualTo(1);
        assertThat(treino.getColaboradoresIds()).containsExactlyInAnyOrder(10, 20);

        assertThat(auditoria.logs).hasSize(1);
        LogAuditoria log = auditoria.logs.get(0);
        assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(log.getEntidade()).isEqualTo("Treinamento");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve definir exigência para tarefa e registrar auditoria (#121)")
    void deveDefinirExigenciaTarefaEAuditar() {
        ExigenciaCapacitacao ex = service.definirExigenciaTarefa("NR-10", ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 5);

        assertThat(ex).isNotNull();
        assertThat(ex.getId()).isEqualTo(1);
        assertThat(ex.getAlvo()).isEqualTo(ExigenciaCapacitacao.TipoAlvo.TAREFA);

        assertThat(auditoria.logs).hasSize(1);
        LogAuditoria log = auditoria.logs.get(0);
        assertThat(log.getEntidade()).isEqualTo("ExigenciaCapacitacao");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve definir exigência para EPI e registrar auditoria (#121)")
    void deveDefinirExigenciaEpiEAuditar() {
        ExigenciaCapacitacao ex = service.definirExigenciaEpi("Treinamento de Respirador", ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, 8);

        assertThat(ex).isNotNull();
        assertThat(ex.getId()).isEqualTo(1);
        assertThat(ex.getAlvo()).isEqualTo(ExigenciaCapacitacao.TipoAlvo.EPI);

        assertThat(auditoria.logs).hasSize(1);
        LogAuditoria log = auditoria.logs.get(0);
        assertThat(log.getEntidade()).isEqualTo("ExigenciaCapacitacao");
        assertThat(log.getEntidadeId()).isEqualTo(1);
    }

    private static class FakeCapacitacaoRepository implements CapacitacaoRepositoryPort {
        private final List<Certificacao> certificacoes = new ArrayList<>();
        private final List<Treinamento> treinamentos = new ArrayList<>();
        private final List<ExigenciaCapacitacao> exigencias = new ArrayList<>();
        private int idSequence = 1;

        @Override
        public Certificacao salvarCertificacao(Certificacao certificacao) {
            Certificacao c = new Certificacao(idSequence++, certificacao.getNome(), certificacao.getDataVencimento(), certificacao.getDataEmissao(), certificacao.getColaboradorId());
            certificacoes.add(c);
            return c;
        }

        @Override
        public Treinamento salvarTreinamento(Treinamento treinamento) {
            Treinamento t = new Treinamento(idSequence++, treinamento.getNome(), treinamento.getConteudo(), treinamento.getInstrutor(), treinamento.getDataRealizacao(), treinamento.getCargaHoraria(), treinamento.getColaboradoresIds());
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

    private static class FakeLogAuditoriaRepository implements LogAuditoriaRepositoryPort {
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
