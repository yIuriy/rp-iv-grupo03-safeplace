package br.edu.safeplace.backend.application.service;

import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EpiServiceTest {

    private EpiRepositoryFake repository;
    private EpiService service;

    @BeforeEach
    void setUp() {
        repository = new EpiRepositoryFake();
        service = new EpiService(repository);
    }

    @Test
    void deveCadastrarEListarEpis() {
        Epi epi = new Epi(null, "Capacete", "CA-100", 20, 5, StatusEpi.DISPONIVEL, null, null);
        Epi salvo = service.cadastrarEpi(epi);

        assertNotNull(salvo.getId());
        assertEquals(1, salvo.getId());

        List<Epi> todos = service.listar();
        assertEquals(1, todos.size());
        assertEquals("Capacete", todos.get(0).getNome());
    }

    @Test
    void deveBuscarEpiPorIdComSucesso() {
        Epi epi = service.cadastrarEpi(new Epi(null, "Bota de Segurança", "CA-200", 15, 3, StatusEpi.DISPONIVEL, null, null));
        Epi encontrado = service.buscarPorId(epi.getId());

        assertEquals("Bota de Segurança", encontrado.getNome());
    }

    @Test
    void deveLancarExcecaoAoBuscarEpiInexistente() {
        assertThrows(EpiNaoEncontradoException.class, () -> service.buscarPorId(999));
    }

    @Test
    void deveRegistrarEntradaDeEstoque() {
        Epi epi = service.cadastrarEpi(new Epi(null, "Luva Nitrílica", "CA-300", 10, 2, StatusEpi.DISPONIVEL, null, null));

        MovimentacaoEstoque mov = service.registrarMovimentacao(epi.getId(), TipoMovimentacao.ENTRADA, 5, "Compra mensal");

        assertEquals(TipoMovimentacao.ENTRADA, mov.getTipo());
        assertEquals(5, mov.getQuantidade());

        Epi atualizado = service.buscarPorId(epi.getId());
        assertEquals(15, atualizado.getQuantidade());
        assertEquals(1, repository.movimentacoes.size());
    }

    @Test
    void deveRegistrarSaidaDeEstoque() {
        Epi epi = service.cadastrarEpi(new Epi(null, "Óculos", "CA-400", 10, 2, StatusEpi.DISPONIVEL, null, null));

        MovimentacaoEstoque mov = service.registrarMovimentacao(epi.getId(), TipoMovimentacao.SAIDA, 4, "Uso operacional");

        assertEquals(TipoMovimentacao.SAIDA, mov.getTipo());
        assertEquals(4, mov.getQuantidade());

        Epi atualizado = service.buscarPorId(epi.getId());
        assertEquals(6, atualizado.getQuantidade());
    }

    @Test
    void deveImpedirSaidaMaiorQueSaldo() {
        Epi epi = service.cadastrarEpi(new Epi(null, "Protetor", "CA-500", 10, 2, StatusEpi.DISPONIVEL, null, null));

        assertThrows(SaldoInsuficienteException.class, () ->
                service.registrarMovimentacao(epi.getId(), TipoMovimentacao.SAIDA, 11, "Retirada excessiva"));

        Epi atualizado = service.buscarPorId(epi.getId());
        assertEquals(10, atualizado.getQuantidade());
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
                    epi.getVidaUtilDias()
            );
            storage.put(id, salvo);
            return salvo;
        }

        @Override
        public Optional<Epi> buscarPorId(Integer id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Epi> listar() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
            Integer id = movimentacao.getId() != null ? movimentacao.getId() : nextMovId++;
            MovimentacaoEstoque salva = new MovimentacaoEstoque(
                    id,
                    movimentacao.getEpiId(),
                    movimentacao.getTipo(),
                    movimentacao.getQuantidade(),
                    movimentacao.getDataHora(),
                    movimentacao.getMotivo()
            );
            movimentacoes.add(salva);
            return salva;
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return movimentacoes.stream()
                    .filter(m -> m.getEpiId().equals(epiId))
                    .toList();
        }
    }
}
