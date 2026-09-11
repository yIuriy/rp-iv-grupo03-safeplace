package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.MovimentacaoEstoqueOutputDTO;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EpiUseCaseTest {

    private EpiRepositoryFake repository;
    private EpiUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new EpiRepositoryFake();
        useCase = new EpiUseCase(repository);
    }

    @Test
    void deveCadastrarEListarEpis() {
        CadastrarEpiInputDTO input = new CadastrarEpiInputDTO("Capacete", "CA-100", 20, 5, LocalDate.now().plusYears(1),
                null, null, null);
        EpiOutputDTO salvo = useCase.cadastrarEpi(input);

        assertNotNull(salvo.id());
        assertEquals(1, salvo.id());
        assertEquals(StatusEpi.DISPONIVEL, salvo.status());

        List<EpiOutputDTO> todos = useCase.listar();
        assertEquals(1, todos.size());
        assertEquals("Capacete", todos.get(0).nome());
    }

    @Test
    void deveBuscarEpiPorIdComSucesso() {
        EpiOutputDTO epi = useCase
                .cadastrarEpi(new CadastrarEpiInputDTO("Bota de Segurança", "CA-200", 15, 3,
                        LocalDate.now().plusYears(1), null, null, null));
        EpiOutputDTO encontrado = useCase.buscarPorId(epi.id());

        assertEquals("Bota de Segurança", encontrado.nome());
    }

    @Test
    void deveLancarExcecaoAoBuscarEpiInexistente() {
        assertThrows(EpiNaoEncontradoException.class, () -> useCase.buscarPorId(999));
    }

    @Test
    void deveRegistrarEntradaDeEstoque() {
        EpiOutputDTO epi = useCase
                .cadastrarEpi(new CadastrarEpiInputDTO("Luva Nitrílica", "CA-300", 10, 2, LocalDate.now().plusYears(1),
                        null, null, null));

        MovimentacaoEstoqueOutputDTO mov = useCase.registrarMovimentacao(epi.id(), TipoMovimentacao.ENTRADA, 5,
                "Compra mensal");

        assertEquals(TipoMovimentacao.ENTRADA, mov.tipo());
        assertEquals(5, mov.quantidade());
        assertEquals(15, mov.saldoAposMovimentacao());

        EpiOutputDTO atualizado = useCase.buscarPorId(epi.id());
        assertEquals(15, atualizado.quantidade());
        assertEquals(1, repository.movimentacoes.size());
    }

    @Test
    void deveRegistrarSaidaDeEstoque() {
        EpiOutputDTO epi = useCase
                .cadastrarEpi(new CadastrarEpiInputDTO("Óculos", "CA-400", 10, 2, LocalDate.now().plusYears(1), null,
                        null, null));

        MovimentacaoEstoqueOutputDTO mov = useCase.registrarMovimentacao(epi.id(), TipoMovimentacao.SAIDA, 4,
                "Uso operacional");

        assertEquals(TipoMovimentacao.SAIDA, mov.tipo());
        assertEquals(4, mov.quantidade());
        assertEquals(6, mov.saldoAposMovimentacao());

        EpiOutputDTO atualizado = useCase.buscarPorId(epi.id());
        assertEquals(6, atualizado.quantidade());
    }

    @Test
    void deveImpedirSaidaMaiorQueSaldo() {
        EpiOutputDTO epi = useCase.cadastrarEpi(
                new CadastrarEpiInputDTO("Protetor", "CA-500", 10, 2, LocalDate.now().plusYears(1), null, null, null));

        assertThrows(SaldoInsuficienteException.class,
                () -> useCase.registrarMovimentacao(epi.id(), TipoMovimentacao.SAIDA, 11, "Retirada excessiva"));

        EpiOutputDTO atualizado = useCase.buscarPorId(epi.id());
        assertEquals(10, atualizado.quantidade());
    }

    private static class EpiRepositoryFake implements EpiRepositoryPort {
        private final Map<Integer, Epi> storage = new HashMap<>();
        private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
        private int nextEpiId = 1;
        private int nextMovId = 1;

        @Override
        public Epi salvar(Epi epi) {
            Integer id = epi.getId() != null ? epi.getId() : nextEpiId++;
            Epi salvo = new Epi(
                    id,
                    epi.getNome(),
                    epi.getNumeroCa(),
                    epi.getQuantidade(),
                    epi.getEstoqueMinimo(),
                    epi.getStatus(),
                    epi.getDataValidadeCa(),
                    epi.getVidaUtilDias(),
                    epi.getEspecificacao().getDescricao(),
                    epi.getEspecificacao().getClassificacao());
            storage.put(id, salvo);
            return salvo;
        }

        @Override
        public List<Epi> listar() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Optional<Epi> buscarPorId(Integer id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
            Integer id = movimentacao.getId() != null ? movimentacao.getId() : nextMovId++;
            MovimentacaoEstoque salvo = new MovimentacaoEstoque(
                    id,
                    movimentacao.getEpiId(),
                    movimentacao.getTipo(),
                    movimentacao.getQuantidade(),
                    movimentacao.getDataHora(),
                    movimentacao.getMotivo());
            movimentacoes.add(salvo);
            return salvo;
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return movimentacoes.stream()
                    .filter(m -> m.getEpiId().equals(epiId))
                    .toList();
        }
    }

    @Test
    void deveRejeitarCaVencidoSemSalvarEpi() {
        CadastrarEpiInputDTO input = new CadastrarEpiInputDTO(
                "Capacete", "CA-100", 20, 5,
                LocalDate.MIN, null, null, null);

        assertThatThrownBy(() -> useCase.cadastrarEpi(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Não é permitido cadastrar EPI com CA vencido.");

        assertThat(repository.listar()).isEmpty();
    }

    @Test
    void deveRejeitarCaSemValidadeSemSalvarEpi() {
        CadastrarEpiInputDTO input = new CadastrarEpiInputDTO(
                "Capacete", "CA-100", 20, 5,
                null, null, null, null);

        assertThatThrownBy(() -> useCase.cadastrarEpi(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de validade do CA é obrigatória.");

        assertThat(repository.listar()).isEmpty();
    }

    @Test
    void devePreservarEspecificacaoAoCadastrarEConsultar() {
        CadastrarEpiInputDTO input = new CadastrarEpiInputDTO(
                "Óculos",
                "1234",
                10,
                2,
                LocalDate.now().plusYears(1),
                365,
                "Óculos de proteção com lentes transparentes",
                ClassificacaoEPI.PROTECAO_DE_OLHOS);

        EpiOutputDTO salvo = useCase.cadastrarEpi(input);
        EpiOutputDTO consultado = useCase.buscarPorId(salvo.id());

        assertThat(salvo.descricao()).isEqualTo(input.descricao());
        assertThat(salvo.classificacao()).isEqualTo(input.classificacao());

        assertThat(consultado.descricao()).isEqualTo(input.descricao());
        assertThat(consultado.classificacao()).isEqualTo(input.classificacao());
        assertThat(consultado.estoqueMinimo()).isEqualTo(2);
    }
}
