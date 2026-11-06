package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import br.edu.safeplace.backend.adapters.out.persistencia.InMemoryManutencaoEpiRepository;
import br.edu.safeplace.backend.application.dto.input.ConcluirManutencaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.dto.output.ManutencaoEpiOutputDTO;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
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
import static org.mockito.Mockito.mock;

class ControlarManutencaoEpiServiceTest {

    /** RNF05: a porta de auditoria é obrigatória nos casos de uso. */
    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();

    private EpiRepositoryFake epiRepository;
    private InMemoryManutencaoEpiRepository manutencaoRepository;
    private ControlarManutencaoEpiService service;
    /** Dublê da porta do operador (#236): começa sem sessão. */
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();

    @BeforeEach
    void setUp() {
        epiRepository = new EpiRepositoryFake();
        manutencaoRepository = new InMemoryManutencaoEpiRepository();
        service = new ControlarManutencaoEpiService(epiRepository, manutencaoRepository, null, auditoria,
                operador.operadorAtual(mock(UsuarioRepository.class)));
    }

    @Test
    void adapterAuditoriaDoEnvioRegistraOperadorDaPorta() {
        epiRepository.salvar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comCa("CA-100")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build());
        operador.autenticar("gestora@safeplace.com", "GESTOR_SEGURANCA");

        service.enviarParaManutencao(1);

        assertEquals("gestora@safeplace.com", auditoria.registros().get(0).getResponsavel());
    }

    @Test
    void adapterSemOperadorAutenticadoAuditoriaRegistraSistema() {
        epiRepository.salvar(EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comCa("CA-100")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build());

        service.enviarParaManutencao(1);

        assertEquals(OperadorAtual.SISTEMA, auditoria.registros().get(0).getResponsavel());
    }

    @Test
    void deveEnviarEpiParaManutencaoComSucesso() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Capacete")
                .comCa("CA-100")
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build();
        epiRepository.salvar(epi);

        EpiOutputDTO dto = service.enviarParaManutencao(1);

        assertEquals(StatusEpi.EM_MANUTENCAO, dto.status());
        assertEquals(StatusEpi.EM_MANUTENCAO, epiRepository.buscarPorId(1).orElseThrow().getStatus());
    }

    @Test
    void deveBloquearEnvioParaManutencaoQuandoCaEstiverVencido() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(2)
                .comNome("Luva")
                .comCa("CA-200")
                .comQuantidade(5)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.of(2020, 1, 1))
                .comVidaUtilDias(365)
                .build();
        epiRepository.salvar(epi);

        assertThrows(CertificadoAprovacaoVencidoException.class, () -> service.enviarParaManutencao(2));
    }

    @Test
    void deveLancarExcecaoAoEnviarEpiInexistente() {
        assertThrows(EpiNaoEncontradoException.class, () -> service.enviarParaManutencao(999));
    }

    @Test
    void deveConcluirManutencaoAprovadaEAtualizarEpiParaDisponivel() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(3)
                .comNome("Óculos")
                .comCa("CA-300")
                .comQuantidade(4)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.EM_MANUTENCAO)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build();
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
    void deveConcluirManutencaoReprovadaSemExecutarDescarteDefinitivo() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(4)
                .comNome("Cinto")
                .comCa("CA-400")
                .comQuantidade(2)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.EM_MANUTENCAO)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build();
        epiRepository.salvar(epi);

        LocalDateTime dataManutencao = LocalDateTime.of(2026, 9, 11, 11, 0);
        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                4, dataManutencao, TipoManutencao.CORRETIVA,
                "Costura rompida", ResultadoManutencao.REPROVADO, "Tecnico Roberto"
        );

        ManutencaoEpiOutputDTO resultadoDTO = service.concluirManutencao(input);

        assertEquals(StatusEpi.EM_MANUTENCAO, resultadoDTO.statusAtualEpi());
        Epi atualizado = epiRepository.buscarPorId(4).orElseThrow();
        assertEquals(StatusEpi.EM_MANUTENCAO, atualizado.getStatus());
        assertEquals(2, atualizado.getQuantidade());
    }

    @Test
    void deveBloquearConclusaoDeManutencaoQuandoCaEstiverVencido() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(5)
                .comNome("Máscara")
                .comCa("CA-500")
                .comQuantidade(5)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.EM_MANUTENCAO)
                .comValidadeCa(LocalDate.of(2024, 1, 1))
                .comVidaUtilDias(365)
                .build();
        epiRepository.salvar(epi);

        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                5, LocalDateTime.of(2026, 9, 11, 12, 0), TipoManutencao.CORRETIVA,
                "Reparo de válvula", ResultadoManutencao.APROVADO, "Tecnico Roberto"
        );

        assertThrows(CertificadoAprovacaoVencidoException.class, () -> service.concluirManutencao(input));
    }

    @Test
    void deveLancarExcecaoAoConcluirManutencaoDeEpiEmUso() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(6)
                .comNome("Bota")
                .comCa("CA-600")
                .comQuantidade(5)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.EM_USO)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build();
        epiRepository.salvar(epi);

        ConcluirManutencaoInputDTO input = new ConcluirManutencaoInputDTO(
                6, LocalDateTime.of(2026, 9, 11, 12, 0), TipoManutencao.CORRETIVA,
                "Reparo", ResultadoManutencao.APROVADO, "Tecnico Roberto"
        );

        assertThrows(EpiIndisponivelParaManutencaoException.class, () -> service.concluirManutencao(input));
    }

    @Test
    void deveListarHistoricoDeManutencoesDoEpi() {
        Epi epi = EpiDeTeste.umEpi()
                .comId(7)
                .comNome("Protetor")
                .comCa("CA-700")
                .comQuantidade(3)
                .comEstoqueMinimo(1)
                .comStatus(StatusEpi.EM_MANUTENCAO)
                .comValidadeCa(LocalDate.of(2030, 1, 1))
                .comVidaUtilDias(365)
                .build();
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

    private static class EpiRepositoryFake implements EpiRepository {
        private final Map<Integer, Epi> storage = new HashMap<>();
        private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
        private int nextId = 1;

        @Override
        public Epi salvar(Epi epi) {
            Integer id = epi.getId() != null ? epi.getId() : nextId++;
            Epi salvo = EpiDeTeste.umEpi()
                    .comId(id)
                    .comNome(epi.getNome())
                    .comCa(epi.getNumeroCa())
                    .comQuantidade(epi.getQuantidade())
                    .comEstoqueMinimo(epi.getEstoqueMinimo())
                    .comStatus(epi.getStatus())
                    .comValidadeCa(epi.getDataValidadeCa())
                    .comVidaUtilDias(epi.getVidaUtilDias())
                    .build();
            storage.put(id, salvo);
            return salvo;
        }

        @Override
        public Optional<Epi> buscarPorId(Integer id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Epi> buscarPorCA(String numeroCa) {
            return storage.values().stream()
                    .filter(e -> e.getNumeroCa().equals(numeroCa))
                    .findFirst();
        }

        @Override
        public List<Epi> listarTodos() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public Epi atualizarSaldo(Epi epi) {
            storage.put(epi.getId(), epi);
            return epi;
        }

        @Override
        public List<Epi> listar() {
            return listarTodos();
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
