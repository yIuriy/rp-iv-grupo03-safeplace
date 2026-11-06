/*
 * Testes do caso de uso de empréstimos (issue #106) com portas em memória: entrega debita uma
 * unidade e grava movimentação; impedimentos do UC12 (situação, saldo, CA, capacitação,
 * colaborador) não alteram o estoque; duplicidade exige confirmação e justificativa; devolução
 * APTO credita e MANUTENCAO/DESCARTE não; consulta de pendentes e atrasados.
 */
package br.edu.safeplace.backend.application.usecase;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import br.edu.safeplace.backend.suporte.EpiDeTeste;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase.Situacao;
import br.edu.safeplace.backend.application.port.out.EmprestimoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.VerificadorImpedimentoCapacitacao;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoNaoEncontradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Supervisor;

class EmprestimoServiceTest {

    private static final String EMAIL_SUPERVISOR = "supervisor@safeplace.test";
    private static final LocalDate PREVISTA = LocalDate.now().plusDays(7);

    private EmprestimosEmMemoria emprestimos;
    private EpisEmMemoria epis;
    private UsuariosEmMemoria usuarios;
    private CapacitacoesEmMemoria capacitacoes;
    private ManutencoesEmMemoria manutencoes;
    private EmprestimoService useCase;

    private Integer supervisorId;
    private Integer colaboradorId;
    private Integer capaceteId;

    @BeforeEach
    void prepararCenario() {
        emprestimos = new EmprestimosEmMemoria();
        epis = new EpisEmMemoria();
        usuarios = new UsuariosEmMemoria();
        capacitacoes = new CapacitacoesEmMemoria();
        manutencoes = new ManutencoesEmMemoria();
        useCase = new EmprestimoService(emprestimos, epis, usuarios, capacitacoes, new TrilhaAuditoriaEmMemoria(), manutencoes);

        supervisorId = usuarios.salvar(Supervisor.novo("Bruno Souza", "52998224725",
                LocalDate.of(1985, 3, 10), EMAIL_SUPERVISOR, "hash")).getId();
        colaboradorId = usuarios.salvar(Colaborador.novo("João da Silva", "12345678909",
                LocalDate.of(1992, 6, 18), null)).getId();
        capaceteId = epis.salvar(epi("Capacete", 5, StatusEpi.DISPONIVEL, LocalDate.now().plusYears(1),
                ClassificacaoEPI.PROTECAO_DE_CABECA)).getId();
    }

    /** Id 0 marca "não salvo": com id, o domínio reconstrói o EPI mesmo com CA vencido, como faz o adaptador JPA. */
    private static Epi epi(String nome, int quantidade, StatusEpi status, LocalDate validadeCa,
                           ClassificacaoEPI classificacao) {
        return EpiDeTeste.umEpi()
                .comId(0)
                .comNome(nome)
                .comCa("1234")
                .comQuantidade(quantidade)
                .comEstoqueMinimo(1)
                .comStatus(status)
                .comValidadeCa(validadeCa)
                .comVidaUtilDias(365)
                .comDescricao(nome)
                .comClassificacao(classificacao)
                .build();
    }

    private RegistrarEntregaInputDTO entregaDe(Integer epiId) {
        return new RegistrarEntregaInputDTO(epiId, colaboradorId, PREVISTA, false, null);
    }

    @Test
    @DisplayName("Entrega registra posse INDISPONIVEL, termo de cautela, operador e debita uma unidade com movimentação SAIDA")
    void entregaDebitaUmaUnidadeERegistraTermo() {
        EmprestimoOutputDTO saida = useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);

        assertThat(saida.status()).isEqualTo(ClassificacaoEmprestimo.INDISPONIVEL);
        assertThat(saida.atrasado()).isFalse();
        assertThat(saida.colaboradorId()).isEqualTo(colaboradorId);
        assertThat(saida.colaboradorNome()).isEqualTo("João da Silva");
        assertThat(saida.epiNome()).isEqualTo("Capacete");
        assertThat(saida.responsavelEntregaId()).isEqualTo(supervisorId);
        assertThat(saida.dataRetirada()).isNotNull();
        assertThat(saida.termoCautela()).contains("Termo de cautela", "João da Silva", "Capacete", "Bruno Souza");

        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(4);
        assertThat(epis.listarMovimentacoesPorEpi(capaceteId)).singleElement().satisfies(mov -> {
            assertThat(mov.getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
            assertThat(mov.getQuantidade()).isEqualTo(1);
            assertThat(mov.getResponsavelId()).isEqualTo(supervisorId);
        });
    }

    @Test
    @DisplayName("Última unidade emprestada deixa o EPI ESGOTADO e a próxima entrega é impedida por falta de saldo")
    void semSaldoBloqueiaEntrega() {
        Integer luvaId = epis.salvar(epi("Luva", 1, StatusEpi.DISPONIVEL, LocalDate.now().plusYears(1),
                ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)).getId();
        useCase.registrarEntrega(entregaDe(luvaId), EMAIL_SUPERVISOR);
        assertThat(epis.buscarPorId(luvaId).orElseThrow().getStatus()).isEqualTo(StatusEpi.ESGOTADO);

        assertThatThrownBy(() -> useCase.registrarEntrega(entregaDe(luvaId), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("ESGOTADO");
        assertThat(emprestimos.listarTodos()).hasSize(1);
    }

    @Test
    @DisplayName("CA vencido impede a entrega e não altera saldo nem histórico")
    void caVencidoImpedeEntrega() {
        Integer oculosId = epis.salvar(epi("Óculos", 3, StatusEpi.DISPONIVEL, LocalDate.now().minusDays(1),
                ClassificacaoEPI.PROTECAO_DE_OLHOS)).getId();

        assertThatThrownBy(() -> useCase.registrarEntrega(entregaDe(oculosId), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("Certificado de Aprovação vencido");

        assertThat(epis.buscarPorId(oculosId).orElseThrow().getQuantidade()).isEqualTo(3);
        assertThat(epis.listarMovimentacoesPorEpi(oculosId)).isEmpty();
        assertThat(emprestimos.listarTodos()).isEmpty();
    }

    @Test
    @DisplayName("EPI em manutenção não pode ser emprestado")
    void epiEmManutencaoImpedeEntrega() {
        Integer cintoId = epis.salvar(epi("Cinto", 3, StatusEpi.EM_MANUTENCAO, LocalDate.now().plusYears(1),
                ClassificacaoEPI.PROTECAO_CONTRA_QUEDAS)).getId();

        assertThatThrownBy(() -> useCase.registrarEntrega(entregaDe(cintoId), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("EM_MANUTENCAO");
    }

    @Test
    @DisplayName("Capacitação obrigatória vencida impede a entrega com mensagem de impedimento normativo")
    void capacitacaoVencidaImpedeEntrega() {
        capacitacoes.impedir(colaboradorId, capaceteId, "treinamento NR-35 não registrado");

        assertThatThrownBy(() -> useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("Impedimento normativo")
                .hasMessageContaining("NR-35");
        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(5);
    }

    @Test
    @DisplayName("Colaborador inexistente ou inativo impede a entrega")
    void colaboradorInvalidoImpedeEntrega() {
        assertThatThrownBy(() -> useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(capaceteId, 999, PREVISTA, false, null), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("não está cadastrado");

        Integer inativoId = usuarios.salvar(new Colaborador(null, "11144477735", "Inativa", LocalDate.of(1990, 1, 1),
                null, false, LocalDateTime.now(), LocalDateTime.now())).getId();
        assertThatThrownBy(() -> useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(capaceteId, inativoId, PREVISTA, false, null), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("Empréstimo bloqueado para destinatário com conta de acesso (Supervisor ou Gestor)")
    void destinatarioComContaDeAcessoImpedeEmprestimo() {
        assertThatThrownBy(() -> useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(capaceteId, supervisorId, PREVISTA, false, null), EMAIL_SUPERVISOR))
                .isInstanceOf(EntregaImpedidaException.class)
                .hasMessageContaining("Apenas colaboradores sem conta de acesso podem receber empréstimo de EPIs.");
    }

    @Test
    @DisplayName("Segundo EPI do mesmo tipo em posse ativa gera alerta; com confirmação e justificativa é entregue")
    void duplicidadeExigeConfirmacaoEJustificativa() {
        useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);
        Integer outroCapaceteId = epis.salvar(epi("Capacete com jugular", 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), ClassificacaoEPI.PROTECAO_DE_CABECA)).getId();

        assertThatThrownBy(() -> useCase.registrarEntrega(entregaDe(outroCapaceteId), EMAIL_SUPERVISOR))
                .isInstanceOf(DuplicidadeDePosseException.class)
                .hasMessageContaining("PROTECAO_DE_CABECA");
        assertThatThrownBy(() -> useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(outroCapaceteId, colaboradorId, PREVISTA, true, " "), EMAIL_SUPERVISOR))
                .isInstanceOf(DuplicidadeDePosseException.class);
        assertThat(epis.buscarPorId(outroCapaceteId).orElseThrow().getQuantidade()).isEqualTo(2);

        EmprestimoOutputDTO substituto = useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(outroCapaceteId, colaboradorId, PREVISTA, true, "Capacete anterior danificado"),
                EMAIL_SUPERVISOR);

        assertThat(substituto.justificativaSubstituicao()).isEqualTo("Capacete anterior danificado");
        assertThat(epis.buscarPorId(outroCapaceteId).orElseThrow().getQuantidade()).isEqualTo(1);
        assertThat(useCase.listar(Situacao.PENDENTES, colaboradorId)).hasSize(2);
    }

    @Test
    @DisplayName("Tipos diferentes em posse não geram alerta de duplicidade")
    void tiposDiferentesNaoGeramDuplicidade() {
        useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);
        Integer luvaId = epis.salvar(epi("Luva", 2, StatusEpi.DISPONIVEL, LocalDate.now().plusYears(1),
                ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES)).getId();

        useCase.registrarEntrega(entregaDe(luvaId), EMAIL_SUPERVISOR);

        assertThat(useCase.listar(Situacao.PENDENTES, colaboradorId)).hasSize(2);
    }

    @Test
    @DisplayName("Devolução APTO encerra o empréstimo e credita a unidade com movimentação ENTRADA")
    void devolucaoAptaCreditaEstoque() {
        EmprestimoOutputDTO entrega = useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);

        EmprestimoOutputDTO devolvido = useCase.registrarDevolucao(entrega.id(),
                new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR);

        assertThat(devolvido.status()).isEqualTo(ClassificacaoEmprestimo.DISPONIVEL);
        assertThat(devolvido.estadoDevolucao()).isEqualTo(EstadoDevolucao.APTO);
        assertThat(devolvido.dataDevolucaoReal()).isNotNull();
        assertThat(devolvido.responsavelDevolucaoId()).isEqualTo(supervisorId);
        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(5);
        assertThat(epis.listarMovimentacoesPorEpi(capaceteId)).extracting(MovimentacaoEstoque::getTipo)
                .containsExactly(TipoMovimentacao.SAIDA, TipoMovimentacao.ENTRADA);
        assertThat(useCase.listar(Situacao.PENDENTES, colaboradorId)).isEmpty();
    }

    @Test
    @DisplayName("Duplicidade de posse é verificada por fallback de ID quando EPI não possui classificação")
    void duplicidadeMesmoSemClassificacaoVerificaPorId() {
        Integer epiSemClassificacaoId = epis.salvar(EpiDeTeste.umEpi()
                .comId(0)
                .comNome("Óculos Simples")
                .comCa("12345")
                .comQuantidade(5)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(365)
                .comDescricao("Sem spec")
                .build()).getId();

        useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(epiSemClassificacaoId, colaboradorId, PREVISTA, false, null), EMAIL_SUPERVISOR);

        assertThatThrownBy(() -> useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(epiSemClassificacaoId, colaboradorId, PREVISTA, false, null), EMAIL_SUPERVISOR))
                .isInstanceOf(DuplicidadeDePosseException.class)
                .hasMessageContaining("Óculos Simples");
    }

    @Test
    @DisplayName("Devolução para MANUTENCAO ou DESCARTE encerra o empréstimo sem creditar saldo, registrando manutenção e movimentação")
    void devolucaoParaManutencaoNaoCredita() {
        EmprestimoOutputDTO primeira = useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);
        EmprestimoOutputDTO segunda = useCase.registrarEntrega(
                new RegistrarEntregaInputDTO(capaceteId, colaboradorId, PREVISTA, true, "Reposição"), EMAIL_SUPERVISOR);
        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(3);

        useCase.registrarDevolucao(primeira.id(), new RegistrarDevolucaoInputDTO(EstadoDevolucao.MANUTENCAO), EMAIL_SUPERVISOR);
        useCase.registrarDevolucao(segunda.id(), new RegistrarDevolucaoInputDTO(EstadoDevolucao.DESCARTE), EMAIL_SUPERVISOR);

        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(3);
        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getStatus()).isEqualTo(StatusEpi.DISPONIVEL);
        assertThat(epis.listarMovimentacoesPorEpi(capaceteId)).extracting(MovimentacaoEstoque::getTipo)
                .containsExactly(TipoMovimentacao.SAIDA, TipoMovimentacao.SAIDA, TipoMovimentacao.SAIDA);
        assertThat(manutencoes.listarPorEpiId(capaceteId)).hasSize(1);
        assertThat(useCase.buscarPorId(primeira.id()).estadoDevolucao()).isEqualTo(EstadoDevolucao.MANUTENCAO);
        assertThat(useCase.buscarPorId(segunda.id()).estadoDevolucao()).isEqualTo(EstadoDevolucao.DESCARTE);
    }

    @Test
    @DisplayName("Devolver duas vezes ou devolver empréstimo inexistente falha sem alterar o estoque")
    void devolucaoRepetidaOuInexistenteFalha() {
        EmprestimoOutputDTO entrega = useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);
        useCase.registrarDevolucao(entrega.id(), new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR);

        assertThatThrownBy(() -> useCase.registrarDevolucao(entrega.id(),
                new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR))
                .isInstanceOf(EmprestimoJaEncerradoException.class);
        assertThatThrownBy(() -> useCase.registrarDevolucao(999,
                new RegistrarDevolucaoInputDTO(EstadoDevolucao.APTO), EMAIL_SUPERVISOR))
                .isInstanceOf(EmprestimoNaoEncontradoException.class);
        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(5);
    }

    @Test
    @DisplayName("Consulta identifica pendências e atrasos: empréstimo vencido aparece como DEVOLUCAO_ATRASADA")
    void consultaDePendentesEAtrasados() {
        useCase.registrarEntrega(entregaDe(capaceteId), EMAIL_SUPERVISOR);
        emprestimos.salvar(Emprestimo.registrarEntrega(epis.buscarPorId(capaceteId).orElseThrow(), pessoa(colaboradorId), pessoa(supervisorId),
                LocalDateTime.now().minusDays(10), LocalDate.now().minusDays(3), "Termo antigo", null));

        List<EmprestimoOutputDTO> pendentes = useCase.listar(Situacao.PENDENTES, null);
        List<EmprestimoOutputDTO> atrasados = useCase.listar(Situacao.ATRASADOS, null);

        assertThat(pendentes).hasSize(2);
        assertThat(atrasados).singleElement().satisfies(atrasado -> {
            assertThat(atrasado.atrasado()).isTrue();
            assertThat(atrasado.status()).isEqualTo(ClassificacaoEmprestimo.DEVOLUCAO_ATRASADA);
            assertThat(atrasado.colaboradorNome()).isEqualTo("João da Silva");
        });
        assertThat(useCase.listar(null, colaboradorId)).hasSize(2);
        assertThat(useCase.listar(Situacao.TODOS, 999)).isEmpty();
    }

    @Test
    @DisplayName("EPIs disponíveis para empréstimo excluem esgotados, em manutenção e com CA vencido")
    void listaEpisDisponiveisParaEmprestimo() {
        epis.salvar(epi("Esgotado", 0, StatusEpi.ESGOTADO, LocalDate.now().plusYears(1), ClassificacaoEPI.PROTECAO_DE_FACE));
        epis.salvar(epi("Manutenção", 2, StatusEpi.EM_MANUTENCAO, LocalDate.now().plusYears(1), ClassificacaoEPI.PROTECAO_DE_FACE));
        epis.salvar(epi("CA vencido", 2, StatusEpi.DISPONIVEL, LocalDate.now().minusDays(1), ClassificacaoEPI.PROTECAO_DE_FACE));

        assertThat(useCase.listarEpisDisponiveisParaEmprestimo()).singleElement()
                .satisfies(epi -> assertThat(epi.nome()).isEqualTo("Capacete"));
    }

    @Test
    @DisplayName("Operador autenticado desconhecido é rejeitado antes de qualquer alteração")
    void operadorDesconhecidoEhRejeitado() {
        assertThatThrownBy(() -> useCase.registrarEntrega(entregaDe(capaceteId), "ninguem@safeplace.test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Operador autenticado");
        assertThat(epis.buscarPorId(capaceteId).orElseThrow().getQuantidade()).isEqualTo(5);
    }

    // ----- portas em memória -----

    private static class EmprestimosEmMemoria implements EmprestimoRepository {
        private final Map<Integer, Emprestimo> registros = new HashMap<>();
        private int proximoId = 1;

        @Override
        public Emprestimo salvar(Emprestimo emprestimo) {
            Integer id = emprestimo.getId() != null ? emprestimo.getId() : proximoId++;
            Emprestimo salvo = new Emprestimo(id, emprestimo.getEpi(), emprestimo.getColaborador(),
                    emprestimo.getResponsavelEntrega(), emprestimo.getDataRetirada(),
                    emprestimo.getDataDevolucaoPrevista(), emprestimo.getTermoCautela(),
                    emprestimo.getJustificativaSubstituicao(), emprestimo.getDataDevolucaoReal(),
                    emprestimo.getResponsavelDevolucao(), emprestimo.getEstadoDevolucao(), emprestimo.getStatus());
            registros.put(id, salvo);
            return salvo;
        }

        @Override
        public Optional<Emprestimo> buscarPorId(Integer id) {
            return Optional.ofNullable(registros.get(id));
        }

        @Override
        public List<Emprestimo> listarTodos() {
            return new ArrayList<>(registros.values());
        }

        @Override
        public List<Emprestimo> listarPorColaborador(Integer colaboradorId) {
            return registros.values().stream().filter(e -> e.getColaboradorId().equals(colaboradorId)).toList();
        }

        @Override
        public List<Emprestimo> listarAtivosPorColaborador(Integer colaboradorId) {
            return listarPorColaborador(colaboradorId).stream().filter(Emprestimo::estaAtivo).toList();
        }

        @Override
        public List<Emprestimo> listarPorEpi(Integer epiId) {
            return registros.values().stream().filter(e -> e.getEpiId().equals(epiId)).toList();
        }
    }

    private static class EpisEmMemoria implements EpiRepository {
        private final Map<Integer, Epi> registros = new HashMap<>();
        private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
        private int proximoId = 1;
        private int proximaMovimentacao = 1;

        @Override
        public Epi salvar(Epi epi) {
            Integer id = epi.getId() != null && epi.getId() > 0 ? epi.getId() : proximoId++;
            Epi salvo = EpiDeTeste.umEpi()
                    .comId(id)
                    .comNome(epi.getNome())
                    .comCa(epi.getNumeroCa())
                    .comQuantidade(epi.getQuantidade())
                    .comEstoqueMinimo(epi.getEstoqueMinimo())
                    .comStatus(epi.getStatus())
                    .comValidadeCa(epi.getDataValidadeCa())
                    .comVidaUtilDias(epi.getVidaUtilDias())
                    .comDescricao(epi.getEspecificacao().getDescricao())
                    .comClassificacao(epi.getEspecificacao().getClassificacao())
                    .build();
            registros.put(id, salvo);
            return salvo;
        }

        @Override
        public Optional<Epi> buscarPorId(Integer id) {
            return Optional.ofNullable(registros.get(id));
        }

        @Override
        public Optional<Epi> buscarPorCA(String numeroCa) {
            return registros.values().stream().filter(e -> e.getNumeroCa().equals(numeroCa)).findFirst();
        }

        @Override
        public List<Epi> listarTodos() {
            return new ArrayList<>(registros.values());
        }

        @Override
        public Epi atualizarSaldo(Epi epi) {
            return salvar(epi);
        }

        @Override
        public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
            MovimentacaoEstoque salva = new MovimentacaoEstoque(proximaMovimentacao++, movimentacao.getEpi(),
                    movimentacao.getLote(), movimentacao.getResponsavel(), movimentacao.getTipo(),
                    movimentacao.getQuantidade(), movimentacao.getDataHora(), movimentacao.getMotivo());
            movimentacoes.add(salva);
            return salva;
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return movimentacoes.stream().filter(m -> m.getEpiId().equals(epiId)).toList();
        }
    }

    private static class UsuariosEmMemoria implements UsuarioRepository {
        private final Map<Integer, Colaborador> registros = new HashMap<>();
        private int proximoId = 1;

        @Override
        public Colaborador salvar(Colaborador colaborador) {
            Integer id = colaborador.getId() != null ? colaborador.getId() : proximoId++;
            Colaborador salvo = colaborador instanceof Supervisor supervisor
                    ? new Supervisor(id, supervisor.getCpf(), supervisor.getNome(), supervisor.getDataNascimento(),
                            supervisor.getEmail(), supervisor.getSenha(), supervisor.isAtivo(),
                            supervisor.getCriadoEm(), supervisor.getAtualizadoEm())
                    : new Colaborador(id, colaborador.getCpf(), colaborador.getNome(), colaborador.getDataNascimento(),
                            colaborador.getEmail(), colaborador.isAtivo(), colaborador.getCriadoEm(),
                            colaborador.getAtualizadoEm());
            registros.put(id, salvo);
            return salvo;
        }

        @Override
        public Optional<Colaborador> buscarPorId(Integer id) {
            return Optional.ofNullable(registros.get(id));
        }

        @Override
        public Optional<Colaborador> buscarPorCpf(String cpf) {
            return registros.values().stream().filter(c -> c.getCpf().equals(cpf)).findFirst();
        }

        @Override
        public Optional<Colaborador> buscarPorEmail(String email) {
            return registros.values().stream().filter(c -> email.equalsIgnoreCase(c.getEmail())).findFirst();
        }

        @Override
        public List<Colaborador> listarTodos() {
            return new ArrayList<>(registros.values());
        }

        @Override
        public List<Colaborador> listarColaboradores(String nome, String cpf) {
            return listarTodos();
        }
    }

    private static class CapacitacoesEmMemoria implements VerificadorImpedimentoCapacitacao {
        private final Map<String, List<String>> impedimentos = new HashMap<>();

        void impedir(Integer colaboradorId, Integer epiId, String motivo) {
            impedimentos.computeIfAbsent(colaboradorId + ":" + epiId, chave -> new ArrayList<>()).add(motivo);
        }

        @Override
        public List<String> impedimentosParaEntrega(Integer colaboradorId, Integer epiId) {
            return impedimentos.getOrDefault(colaboradorId + ":" + epiId, List.of());
        }
    }

    private static class ManutencoesEmMemoria implements ManutencaoEpiRepository {
        private final List<ManutencaoEpi> registros = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public ManutencaoEpi salvar(ManutencaoEpi manutencao) {
            ManutencaoEpi salva = new ManutencaoEpi(
                    proximoId++,
                    manutencao.getEpiId(),
                    manutencao.getDataManutencao(),
                    manutencao.getTipoManutencao(),
                    manutencao.getDescricao(),
                    manutencao.getResultado(),
                    manutencao.getResponsavelManutencao(),
                    manutencao.getResponsavelId()
            );
            registros.add(salva);
            return salva;
        }

        @Override
        public Optional<ManutencaoEpi> buscarPorId(Integer id) {
            return registros.stream().filter(m -> m.getId().equals(id)).findFirst();
        }

        @Override
        public List<ManutencaoEpi> listarPorEpiId(Integer epiId) {
            return registros.stream().filter(m -> m.getEpiId().equals(epiId)).toList();
        }
    }
}
