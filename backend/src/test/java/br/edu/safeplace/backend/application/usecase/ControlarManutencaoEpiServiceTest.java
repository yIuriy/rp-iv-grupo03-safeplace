package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.persistencia.InMemoryManutencaoEpiRepository;
import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ControlarManutencaoEpiServiceTest {

    private EpiRepositoryFake epiRepository;
    private InMemoryManutencaoEpiRepository manutencaoRepository;
    private ControlarManutencaoEpiService service;

    @BeforeEach
    void setUp() {
        epiRepository = new EpiRepositoryFake();
        manutencaoRepository = new InMemoryManutencaoEpiRepository();
        service = new ControlarManutencaoEpiService(epiRepository, manutencaoRepository);
    }

    @Test
    void deveEnviarEpiParaManutencaoComSucesso() {
        Epi epi = new Epi(1, "Capacete", "CA-100", 10, 2, StatusEpi.DISPONIVEL, LocalDate.of(2030, 1, 1), 365);
        epiRepository.salvar(epi);

        EpiOutputDTO dto = service.enviarParaManutencao(1);

        assertEquals(StatusEpi.EM_MANUTENCAO, dto.status());
        assertEquals(StatusEpi.EM_MANUTENCAO, epiRepository.buscarPorId(1).orElseThrow().getStatus());
    }

    @Test
    void deveBloquearEnvioParaManutencaoQuandoCaEstiverVencido() {
        Epi epi = new Epi(2, "Luva", "CA-200", 5, 1, StatusEpi.DISPONIVEL, LocalDate.of(2020, 1, 1), 365);
        epiRepository.salvar(epi);

        assertThrows(CertificadoAprovacaoVencidoException.class, () -> service.enviarParaManutencao(2));
    }

    @Test
    void deveLancarExcecaoAoEnviarEpiInexistente() {
        assertThrows(EpiNaoEncontradoException.class, () -> service.enviarParaManutencao(999));
    }

    @Test
    void deveConcluirManutencaoAprovadaEAtualizarEpiParaDisponivel() {
        Epi epi = new Epi(3, "Óculos", "CA-300", 4, 1, StatusEpi.EM_MANUTENCAO, LocalDate.of(2030, 1, 1), 365);
        epiRepository.salvar(epi);

        LocalDateTime dataManutencao = LocalDateTime.of(2026, 9, 11, 10, 0);
        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                3, dataManutencao, TipoManutencao.PREVENTIVA,
                "Polimento e troca de elastico", ResultadoManutencao.APROVADO, "Tecnico Roberto"
        );

        ManutencaoEpiOutputDTO resultadoDTO = service.concluirManutencao(input);

        assertNotNull(resultadoDTO.id());
        assertEquals(3, resultadoDTO.epiId());
        assertEquals(StatusEpi.DISPONIVEL, resultadoDTO.statusAtualEpi());
        assertEquals(StatusEpi.DISPONIVEL, epiRepository.buscarPorId(3).orElseThrow().getStatus());
        assertEquals(1, manutencaoRepository.listarPorEpiId(3).size());
    }

    @Test
    void deveConcluirManutencaoReprovadaEAtualizarEpiParaDescartadoComDecremento() {
        Epi epi = new Epi(4, "Cinto", "CA-400", 2, 1, StatusEpi.EM_MANUTENCAO, LocalDate.of(2030, 1, 1), 365);
        epiRepository.salvar(epi);

        LocalDateTime dataManutencao = LocalDateTime.of(2026, 9, 11, 11, 0);
        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                4, dataManutencao, TipoManutencao.CORRETIVA,
                "Costura rompida", ResultadoManutencao.REPROVADO, "Tecnico Roberto"
        );

        ManutencaoEpiOutputDTO resultadoDTO = service.concluirManutencao(input);

        assertEquals(StatusEpi.DESCARTADO, resultadoDTO.statusAtualEpi());
        Epi atualizado = epiRepository.buscarPorId(4).orElseThrow();
        assertEquals(StatusEpi.DESCARTADO, atualizado.getStatus());
        assertEquals(1, atualizado.getQuantidade());
    }

    @Test
    void deveBloquearConclusaoDeManutencaoQuandoCaEstiverVencido() {
        Epi epi = new Epi(5, "Máscara", "CA-500", 5, 1, StatusEpi.EM_MANUTENCAO, LocalDate.of(2024, 1, 1), 365);
        epiRepository.salvar(epi);

        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                5, LocalDateTime.of(2026, 9, 11, 12, 0), TipoManutencao.CORRETIVA,
                "Reparo de válvula", ResultadoManutencao.APROVADO, "Tecnico Roberto"
        );

        assertThrows(CertificadoAprovacaoVencidoException.class, () -> service.concluirManutencao(input));
    }

    @Test
    void deveLancarExcecaoAoConcluirManutencaoDeEpiComStatusInvalido() {
        Epi epi = new Epi(6, "Bota", "CA-600", 5, 1, StatusEpi.DISPONIVEL, LocalDate.of(2030, 1, 1), 365);
        epiRepository.salvar(epi);

        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                6, LocalDateTime.of(2026, 9, 11, 12, 0), TipoManutencao.CORRETIVA,
                "Reparo", ResultadoManutencao.APROVADO, "Tecnico Roberto"
        );

        assertThrows(EpiIndisponivelParaManutencaoException.class, () -> service.concluirManutencao(input));
    }

    @Test
    void deveListarHistoricoDeManutencoesDoEpi() {
        Epi epi = new Epi(7, "Protetor", "CA-700", 3, 1, StatusEpi.EM_MANUTENCAO, LocalDate.of(2030, 1, 1), 365);
        epiRepository.salvar(epi);

        service.concluirManutencao(new ConcluirManutencaoInputDTO(
                7, LocalDateTime.of(2026, 8, 1, 10, 0), TipoManutencao.PREVENTIVA,
                "Primeira manutenção", ResultadoManutencao.APROVADO, "Tecnico 1"
        ));

        // Re-enviar para manutenção para simular um segundo ciclo
        service.enviarParaManutencao(7);

        service.concluirManutencao(new ConcluirManutencaoInputDTO(
                7, LocalDateTime.of(2026, 9, 1, 10, 0), TipoManutencao.PREVENTIVA,
                "Segunda manutenção", ResultadoManutencao.APROVADO, "Tecnico 2"
        ));

        List<ManutencaoEpiOutputDTO> historico = service.listarHistoricoPorEpi(7);

        assertEquals(2, historico.size());
        assertEquals("Primeira manutenção", historico.get(0).descricao());
        assertEquals("Segunda manutenção", historico.get(1).descricao());
    }

    private static class EpiRepositoryFake implements EpiRepositoryPort {
        private final Map<Integer, Epi> storage = new HashMap<>();
        private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
        private int nextId = 1;

        @Override
        public Epi salvar(Epi epi) {
            Integer id = epi.getId() != null ? epi.getId() : nextId++;
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
            movimentacoes.add(movimentacao);
            return movimentacao;
        }

        @Override
        public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
            return movimentacoes.stream()
                    .filter(m -> m.getEpiId().equals(epiId))
                    .toList();
        }
    }
}
