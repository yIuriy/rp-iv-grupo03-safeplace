/*
 * Testes unitários da consulta de ocorrências no OcorrenciaService (issue #128): o Supervisor só vê,
 * na lista, no detalhe e na busca por protocolo, os relatos que registrou; o Gestor vê todos; os
 * critérios se combinam com essa regra.
 */
package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.notificacao.NotificadorOcorrenciaNulo;
import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.suporte.OperadorAutenticadoFixo;

class OcorrenciaConsultaUseCaseTest {

    private static final String SUPERVISOR = "supervisor@safeplace.com";

    // Relato 1 e 3 foram registrados pelo Supervisor; o 2, por outra pessoa.
    private final Acidente proprio = acidente(1, LocalDateTime.of(2026, 9, 10, 8, 0), "CAT-2026-09-0001");
    private final Acidente alheio = acidente(2, LocalDateTime.of(2026, 9, 12, 8, 0), "CAT-2026-09-0002");
    private final Incidente proprioAntigo = incidente(3, LocalDateTime.of(2026, 8, 5, 8, 0));

    private LogAuditoriaRepository auditoria;
    private OcorrenciaService useCase;
    /** Dublê da porta do operador (#236), no lugar do SecurityContext do Spring. Começa sem sessão. */
    private final OperadorAutenticadoFixo operador = OperadorAutenticadoFixo.semSessao();

    @BeforeEach
    void preparar() {
        auditoria = mock(LogAuditoriaRepository.class);
        when(auditoria.buscarIdsPorResponsavel(SUPERVISOR, "CADASTRO", OcorrenciaService.ENTIDADES_DE_RELATO))
                .thenReturn(List.of(1, 3));
        useCase = new OcorrenciaService(new RepositorioFixo(List.of(proprio, alheio, proprioAntigo)), null, null, auditoria,
                new NotificadorOcorrenciaNulo(), operador.operadorAtual(mock(UsuarioRepository.class)));
    }

    private void autenticar(String email, String papel) {
        operador.autenticar(email, papel);
    }

    @Test
    @DisplayName("Supervisor não abre pelo id um relato que não registrou")
    void supervisorNaoAbreRelatoAlheioPeloId() {
        autenticar(SUPERVISOR, "ROLE_SUPERVISOR");

        assertThat(useCase.buscarPorId(1).idOcorrencia()).isEqualTo(1);
        assertThatThrownBy(() -> useCase.buscarPorId(2)).isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Supervisor não encontra pelo protocolo CAT um acidente que não registrou")
    void supervisorNaoEncontraProtocoloAlheio() {
        autenticar(SUPERVISOR, "ROLE_SUPERVISOR");

        assertThat(useCase.buscarPorProtocolo("CAT-2026-09-0001").idOcorrencia()).isEqualTo(1);
        assertThatThrownBy(() -> useCase.buscarPorProtocolo("CAT-2026-09-0002"))
                .isInstanceOf(OcorrenciaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("A busca de relatos do Supervisor considera só cadastros de Acidente e Incidente")
    void consultaAuditoriaPorTiposDeRelato() {
        autenticar(SUPERVISOR, "ROLE_SUPERVISOR");

        useCase.listar();

        verify(auditoria).buscarIdsPorResponsavel(SUPERVISOR, "CADASTRO", List.of("Acidente", "Incidente"));
    }

    @Test
    @DisplayName("Lista do Supervisor combina os critérios com a regra de visibilidade")
    void listaDoSupervisorCombinaCriteriosEVisibilidade() {
        autenticar(SUPERVISOR, "ROLE_SUPERVISOR");

        assertThat(useCase.listar()).extracting(OcorrenciaOutputDTO::idOcorrencia).containsExactly(1, 3);
        FiltroOcorrenciasDTO setembro = new FiltroOcorrenciasDTO(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null, null);
        assertThat(useCase.listar(setembro)).extracting(OcorrenciaOutputDTO::idOcorrencia).containsExactly(1);
    }

    @Test
    @DisplayName("Gestor vê todos os relatos na lista, no detalhe e no protocolo")
    void gestorVeTodos() {
        autenticar("gestor@safeplace.com", "ROLE_GESTOR_SEGURANCA");

        assertThat(useCase.listar()).extracting(OcorrenciaOutputDTO::idOcorrencia).containsExactly(1, 2, 3);
        assertThat(useCase.buscarPorId(2).idOcorrencia()).isEqualTo(2);
        assertThat(useCase.buscarPorProtocolo("CAT-2026-09-0002").idOcorrencia()).isEqualTo(2);
    }

    private static Acidente acidente(int id, LocalDateTime fato, String protocolo) {
        return Acidente.builder()
                .idOcorrencia(id)
                .descricao("Corte na mão")
                .dataOcorrencia(fato)
                .dataRegistro(fato.plusHours(1))
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .localInformado("Linha 1")
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .numeroProtocoloCAT(NumeroProtocoloCAT.de(protocolo))
                .gravidade(NivelPerigo.BAIXO)
                .build();
    }

    private static Incidente incidente(int id, LocalDateTime fato) {
        return Incidente.builder()
                .idOcorrencia(id)
                .descricao("Piso escorregadio")
                .dataOcorrencia(fato)
                .dataRegistro(fato.plusHours(1))
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .situacaoRisco("Óleo no corredor")
                .potencialDano("Queda")
                .build();
    }

    /** Repositório somente leitura: a consulta filtrada usa a implementação padrão da porta. */
    private record RepositorioFixo(List<Ocorrencia> ocorrencias) implements OcorrenciaRepository {
        @Override
        public Acidente salvarAcidente(Acidente acidente) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Incidente salvarIncidente(Incidente incidente) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Ocorrencia> listar() {
            return ocorrencias;
        }

        @Override
        public Optional<Ocorrencia> buscarPorId(Integer id) {
            return ocorrencias.stream().filter(o -> o.getIdOcorrencia().equals(id)).findFirst();
        }

        @Override
        public Optional<Ocorrencia> buscarPorProtocolo(String protocolo) {
            return ocorrencias.stream()
                    .filter(o -> o instanceof Acidente a && protocolo.equals(a.getNumeroProtocoloCAT()))
                    .findFirst();
        }

        @Override
        public long proximoSequencialCAT(int ano, int mes) {
            throw new UnsupportedOperationException();
        }
    }
}
