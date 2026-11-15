package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.capacitacao.AlertasCapacitacaoOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.CapacitacoesColaboradorOutputDTO;
import br.edu.safeplace.backend.application.dto.capacitacao.ColaboradorCapacitacaoResumoOutputDTO;
import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusTreinamento;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

class AcompanharCapacitacoesServiceTest {

    private static final LocalDate HOJE = LocalDate.now();

    private final Colaborador ana = colaborador(1, "Ana", "11900000164");
    private final Colaborador bruno = colaborador(2, "Bruno", "11900000245");
    private final Colaborador carla = colaborador(3, "Carla", "11900000326");
    private final Colaborador davi = colaborador(4, "Davi", "11900000407");

    private UsuarioRepository usuarioRepository;
    private ConsultarCapacitacaoUseCase consultarCapacitacao;
    private AcompanharCapacitacoesService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        consultarCapacitacao = mock(ConsultarCapacitacaoUseCase.class);
        service = new AcompanharCapacitacoesService(usuarioRepository, consultarCapacitacao);

        // Ana: uma vencida e uma em dia; Bruno: vence exatamente em 30 dias; Carla: só treinamento,
        // sem certificação; Davi: sem histórico algum.
        Certificacao anaVencida = new Certificacao(10, "NR-10", HOJE, HOJE.minusYears(2), ana, HOJE);
        Certificacao anaEmDia = new Certificacao(11, "NR-33", HOJE.plusDays(31), HOJE.minusYears(1), ana, HOJE);
        Certificacao brunoProxima = new Certificacao(12, "NR-35", HOJE.plusDays(30), HOJE.minusYears(1), bruno, HOJE);
        Treinamento carlaTreinamento = new Treinamento(20, "Integração", "Normas internas", "Eng. Paula",
                HOJE.minusDays(5), 8, List.of(carla));

        when(usuarioRepository.listar()).thenReturn(List.of(davi, carla, bruno, ana));
        when(consultarCapacitacao.listarCertificacoes())
                .thenReturn(List.of(anaVencida, brunoProxima, anaEmDia));
        when(consultarCapacitacao.listarTreinamentos()).thenReturn(List.of(carlaTreinamento));
    }

    @Test
    @DisplayName("UC02 passo 2: lista todos os colaboradores por nome, com situação consolidada e ausência de certificação")
    void deveListarColaboradoresComSituacaoConsolidada() {
        List<ColaboradorCapacitacaoResumoOutputDTO> lista = service.listarColaboradores(null);

        assertThat(lista).extracting(ColaboradorCapacitacaoResumoOutputDTO::colaboradorNome)
                .containsExactly("Ana", "Bruno", "Carla", "Davi");
        assertThat(lista).extracting(ColaboradorCapacitacaoResumoOutputDTO::situacaoCertificacoes)
                .containsExactly(StatusCertificacao.VENCIDA, StatusCertificacao.PROXIMA_DO_VENCIMENTO, null, null);
        assertThat(lista).extracting(ColaboradorCapacitacaoResumoOutputDTO::semCertificacoes)
                .containsExactly(false, false, true, true);
        assertThat(lista.get(0).totalCertificacoes()).isEqualTo(2);
        assertThat(lista.get(2).totalTreinamentos()).isEqualTo(1);
        assertThat(lista).extracting(ColaboradorCapacitacaoResumoOutputDTO::pendenteIntegracaoAdmissional)
                .containsExactly(false, false, true, true);
    }

    @Test
    @DisplayName("UC02 cenário alternativo IV: filtro por situação devolve apenas os colaboradores correspondentes")
    void deveFiltrarColaboradoresPorSituacao() {
        assertThat(service.listarColaboradores(StatusCertificacao.VENCIDA))
                .extracting(ColaboradorCapacitacaoResumoOutputDTO::colaboradorId).containsExactly(1);
        assertThat(service.listarColaboradores(StatusCertificacao.PROXIMA_DO_VENCIMENTO))
                .extracting(ColaboradorCapacitacaoResumoOutputDTO::colaboradorId).containsExactly(2);
        assertThat(service.listarColaboradores(StatusCertificacao.EM_DIA)).isEmpty();
    }

    @Test
    @DisplayName("UC02 passos 4 e 5: detalha certificações e treinamentos com os atributos do modelo")
    void deveDetalharCapacitacoesDoColaborador() {
        Certificacao vencida = new Certificacao(10, "NR-10", HOJE.minusDays(1), HOJE.minusYears(2), ana, HOJE);
        Certificacao proxima = new Certificacao(13, "NR-12", HOJE.plusDays(10), HOJE.minusYears(1), ana, HOJE);
        Treinamento treinamento = new Treinamento(21, "NR-10 Básico", "Riscos elétricos", "Eng. Paula",
                HOJE.minusMonths(3), 40, List.of(ana));
        when(usuarioRepository.buscarPorId(1)).thenReturn(Optional.of(ana));
        when(consultarCapacitacao.listarCertificacoes(1)).thenReturn(List.of(vencida, proxima));
        when(consultarCapacitacao.listarTreinamentos(1)).thenReturn(List.of(treinamento));

        CapacitacoesColaboradorOutputDTO detalhe = service.consultarColaborador(1);

        assertThat(detalhe.colaboradorNome()).isEqualTo("Ana");
        assertThat(detalhe.situacaoCertificacoes()).isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(detalhe.certificacoes()).hasSize(2);
        assertThat(detalhe.certificacoesVencidas()).singleElement()
                .satisfies(c -> assertThat(c.nome()).isEqualTo("NR-10"));
        assertThat(detalhe.certificacoesProximasDoVencimento()).singleElement()
                .satisfies(c -> assertThat(c.nome()).isEqualTo("NR-12"));
        assertThat(detalhe.treinamentos()).singleElement().satisfies(t -> {
            assertThat(t.conteudo()).isEqualTo("Riscos elétricos");
            assertThat(t.instrutor()).isEqualTo("Eng. Paula");
            assertThat(t.cargaHoraria()).isEqualTo(40);
            assertThat(t.status()).isEqualTo(StatusTreinamento.REALIZADO);
        });
        assertThat(detalhe.pendenteIntegracaoAdmissional()).isFalse();
        assertThat(detalhe.pendencias()).anyMatch(m -> m.contains("vencida"))
                .anyMatch(m -> m.contains("próxima(s) do vencimento (30 dias)"));
    }

    @Test
    @DisplayName("UC02 cenário alternativo III: colaborador sem histórico recebe pendência de integração")
    void deveApresentarPendenciaDeIntegracao() {
        when(usuarioRepository.buscarPorId(4)).thenReturn(Optional.of(davi));
        when(consultarCapacitacao.listarCertificacoes(4)).thenReturn(List.of());
        when(consultarCapacitacao.listarTreinamentos(4)).thenReturn(List.of());

        CapacitacoesColaboradorOutputDTO detalhe = service.consultarColaborador(4);

        assertThat(detalhe.situacaoCertificacoes()).isNull();
        assertThat(detalhe.pendenteIntegracaoAdmissional()).isTrue();
        assertThat(detalhe.pendencias()).containsExactly(
                "Pendência da trilha inicial de integração", "Pendência de treinamentos admissionais");
    }

    @Test
    @DisplayName("Consulta de colaborador inexistente é recusada")
    void deveRecusarColaboradorInexistente() {
        when(usuarioRepository.buscarPorId(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultarColaborador(99))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    @DisplayName("RF10: alertas incluem treinamentos com validade, nas fronteiras de 30 dias e do vencimento")
    void deveAlertarTreinamentosComValidade() {
        Treinamento proximo = new Treinamento(30, "NR-33", null, null, HOJE.minusMonths(12).plusDays(30), 8, 12, List.of(ana, bruno));
        Treinamento foraDaJanela = new Treinamento(31, "NR-35", null, null, HOJE.minusMonths(12).plusDays(31), 8, 12, List.of(ana));
        Treinamento venceHoje = new Treinamento(32, "NR-10", null, null, HOJE.minusMonths(12), 8, 12, List.of(bruno));
        Treinamento vencido = new Treinamento(33, "NR-12", null, null, HOJE.minusMonths(30), 8, 12, List.of(ana));
        Treinamento semValidade = new Treinamento(34, "Integração", null, null, HOJE.minusYears(10), 8, List.of(carla));
        when(consultarCapacitacao.listarTreinamentos())
                .thenReturn(List.of(proximo, foraDaJanela, venceHoje, vencido, semValidade));

        AlertasCapacitacaoOutputDTO alertas = service.consultarAlertas();

        assertThat(alertas.treinamentosProximosDoVencimento())
                .extracting(a -> a.nome() + "/" + a.colaboradorNome())
                .containsExactly("NR-33/Ana", "NR-33/Bruno");
        assertThat(alertas.treinamentosProximosDoVencimento().get(0).dataVencimento()).isEqualTo(HOJE.plusDays(30));
        assertThat(alertas.treinamentosVencidos())
                .extracting(a -> a.nome() + "/" + a.colaboradorNome())
                .containsExactly("NR-12/Ana", "NR-10/Bruno");
    }

    @Test
    @DisplayName("UC02 regra 1 e cenários I e III: alertas separam fronteiras de 30 dias, vencidas e pendências")
    void deveMontarAlertas() {
        AlertasCapacitacaoOutputDTO alertas = service.consultarAlertas();

        assertThat(alertas.dataReferencia()).isEqualTo(HOJE);
        assertThat(alertas.diasAntecedenciaAlerta()).isEqualTo(30);
        // Vencimento em exatamente 30 dias alerta; em 31 dias, não.
        assertThat(alertas.proximasDoVencimento()).singleElement().satisfies(a -> {
            assertThat(a.nome()).isEqualTo("NR-35");
            assertThat(a.colaboradorNome()).isEqualTo("Bruno");
            assertThat(a.dataVencimento()).isEqualTo(HOJE.plusDays(30));
        });
        // Vencimento na data da consulta já é vencida.
        assertThat(alertas.vencidas()).singleElement().satisfies(a -> {
            assertThat(a.nome()).isEqualTo("NR-10");
            assertThat(a.colaboradorId()).isEqualTo(1);
            assertThat(a.status()).isEqualTo(StatusCertificacao.VENCIDA);
        });
        // Ausência de certificações é pendência mesmo com treinamento registrado (UC02, cenário III).
        assertThat(alertas.pendencias()).extracting(p -> p.colaboradorNome()).containsExactly("Carla", "Davi");
        assertThat(alertas.pendencias().get(0).mensagens()).contains("Pendência da trilha inicial de integração");
    }

    private static Colaborador colaborador(Integer id, String nome, String cpf) {
        return new Colaborador(id, cpf, nome, LocalDate.of(1990, 1, 1), null, true, null, null);
    }
}
