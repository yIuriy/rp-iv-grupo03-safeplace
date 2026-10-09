package br.edu.safeplace.backend.domain.capacitacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;
import br.edu.safeplace.backend.domain.capacitacao.exception.ColaboradorNaoAlocavelException;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;

@DisplayName("ValidadorAptidaoColaboradorService - Regras de Domínio de Aptidão e Bloqueio")
class ValidadorAptidaoColaboradorServiceTest {

    private ValidadorAptidaoColaboradorService validador;
    private Colaborador colaborador;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        validador = new ValidadorAptidaoColaboradorService();
        colaborador = new Colaborador(1, "12345678901", "João da Silva", LocalDate.of(1990, 1, 1),
                null, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        hoje = LocalDate.now();
    }

    @Test
    @DisplayName("Bloqueia alocação se a tarefa não estiver classificada com grau de risco (UC11, Exceção I)")
    void deveBloquearAlocacaoEmTarefaSemClassificacao() {
        Tarefa tarefaNaoClassificada = Tarefa.cadastrar("Manutenção em telhado");

        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(colaborador, tarefaNaoClassificada,
                List.of(), List.of(), List.of(), hoje))
                .isInstanceOf(TarefaSemClassificacaoException.class);
    }

    @Test
    @DisplayName("Permite alocação se a tarefa for classificada e não possuir exigências normativas")
    void devePermitirAlocacaoEmTarefaSemExigencias() {
        Tarefa tarefa = new Tarefa(10, "Pintura de meio-fio", NivelPerigo.BAIXO, hoje.atStartOfDay());

        assertThatCode(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(), List.of(), List.of(), hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Bloqueia alocação se a certificação obrigatória exigida estiver ausente (UC02, Exceção I)")
    void deveBloquearAlocacaoComCertificacaoAusente() {
        Tarefa tarefa = new Tarefa(10, "Reparo em linha viva", NivelPerigo.CRITICO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-10 Básico",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);

        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(), List.of(), List.of(exigencia), hoje))
                .isInstanceOf(AlocacaoBloqueadaException.class)
                .hasMessageContaining("NR-10 Básico")
                .hasMessageContaining("ausente ou vencida");
    }

    @Test
    @DisplayName("Bloqueia alocação se a certificação obrigatória estiver vencida (UC02, Exceção I)")
    void deveBloquearAlocacaoComCertificacaoVencida() {
        Tarefa tarefa = new Tarefa(10, "Trabalho em altura", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-35 Altura",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);

        Certificacao certVencida = new Certificacao(1, "NR-35 Altura", hoje.minusDays(1), hoje.minusYears(1), 1);

        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(certVencida), List.of(), List.of(exigencia), hoje))
                .isInstanceOf(AlocacaoBloqueadaException.class)
                .hasMessageContaining("NR-35 Altura")
                .hasMessageContaining("vencida");
    }

    @Test
    @DisplayName("Bloqueia alocação na data exata de vencimento, coerente com o status VENCIDA (UC02, Exceção I)")
    void deveBloquearAlocacaoNaDataDeVencimento() {
        Tarefa tarefa = new Tarefa(10, "Trabalho em altura", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-35 Altura",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);
        Certificacao venceHoje = new Certificacao(1, "NR-35 Altura", hoje, hoje.minusYears(1), 1);
        Certificacao venceAmanha = new Certificacao(2, "NR-35 Altura", hoje.plusDays(1), hoje.minusYears(1), 1);

        assertThat(venceHoje.getStatus()).isEqualTo(StatusCertificacao.VENCIDA);
        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(venceHoje), List.of(), List.of(exigencia), hoje))
                .isInstanceOf(AlocacaoBloqueadaException.class)
                .hasMessageContaining("NR-35 Altura");
        assertThat(validador.obterImpedimentos(colaborador, tarefa,
                List.of(venceHoje), List.of(), List.of(exigencia), hoje))
                .containsExactly("certificação obrigatória 'NR-35 Altura' ausente ou vencida");
        assertThatCode(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(venceAmanha), List.of(), List.of(exigencia), hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Permite alocação se a certificação obrigatória estiver válida na data da alocação")
    void devePermitirAlocacaoComCertificacaoValida() {
        Tarefa tarefa = new Tarefa(10, "Trabalho em altura", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-35 Altura",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);

        Certificacao certValida = new Certificacao(1, "NR-35 Altura", hoje.plusMonths(6), hoje.minusMonths(6), 1);

        assertThatCode(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(certValida), List.of(), List.of(exigencia), hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Bloqueia alocação se o treinamento obrigatório não foi realizado (UC02, Alternativo II)")
    void deveBloquearAlocacaoComTreinamentoNaoRealizado() {
        Tarefa tarefa = new Tarefa(10, "Espaço Confinado", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-33 Vigia",
                ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);

        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(), List.of(), List.of(exigencia), hoje))
                .isInstanceOf(AlocacaoBloqueadaException.class)
                .hasMessageContaining("NR-33 Vigia")
                .hasMessageContaining("pendente ou não realizado");
    }

    @Test
    @DisplayName("Permite alocação se o treinamento obrigatório foi concluído")
    void devePermitirAlocacaoComTreinamentoRealizado() {
        Tarefa tarefa = new Tarefa(10, "Espaço Confinado", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-33 Vigia",
                ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);

        Treinamento treinoRealizado = new Treinamento(1, "NR-33 Vigia", "Prática", "Instrutor A",
                hoje.minusDays(10), 16, List.of(colaborador.getId()));

        assertThatCode(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(), List.of(treinoRealizado), List.of(exigencia), hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("obterImpedimentos retorna lista descritiva completa para consulta prévia sem lançar exceção")
    void deveRetornarImpedimentosNaConsultaSemLancarExcecao() {
        Tarefa tarefaNaoClassificada = new Tarefa(20, "Atividade perigosa", null, null, null);
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-10",
                ExigenciaCapacitacao.TipoCapacitacao.CERTIFICACAO, tarefaNaoClassificada.getId(), ExigenciaCapacitacao.TipoAlvo.TAREFA);

        List<String> impedimentos = validador.obterImpedimentos(colaborador, tarefaNaoClassificada,
                List.of(), List.of(), List.of(exigencia), hoje);

        assertThat(impedimentos).hasSize(2);
        assertThat(impedimentos).anyMatch(msg -> msg.contains("sem grau de periculosidade classificado"));
        assertThat(impedimentos).anyMatch(msg -> msg.contains("NR-10"));
    }

    @Test
    @DisplayName("Bloqueia alocação com treinamento obrigatório vencido e informa a data de vencimento")
    void deveBloquearComTreinamentoVencido() {
        Tarefa tarefa = new Tarefa(10, "Espaço Confinado", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-33 Vigia",
                ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);
        Treinamento vencido = new Treinamento(1, "NR-33 Vigia", "Prática", "Instrutor A",
                hoje.minusMonths(13), 16, 12, List.of(colaborador.getId()));

        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(), List.of(vencido), List.of(exigencia), hoje))
                .isInstanceOf(AlocacaoBloqueadaException.class)
                .hasMessageContaining("NR-33 Vigia")
                .hasMessageContaining("vencido em " + hoje.minusMonths(1).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    @Test
    @DisplayName("Treinamento próximo do vencimento ainda permite alocação")
    void devePermitirAlocacaoComTreinamentoProximoDoVencimento() {
        Tarefa tarefa = new Tarefa(10, "Espaço Confinado", NivelPerigo.ALTO, hoje.atStartOfDay());
        ExigenciaCapacitacao exigencia = new ExigenciaCapacitacao(1, "NR-33 Vigia",
                ExigenciaCapacitacao.TipoCapacitacao.TREINAMENTO, 10, ExigenciaCapacitacao.TipoAlvo.TAREFA);
        Treinamento proximo = new Treinamento(1, "NR-33 Vigia", "Prática", "Instrutor A",
                hoje.minusMonths(12).plusDays(10), 16, 12, List.of(colaborador.getId()));

        assertThatCode(() -> validador.validarAptidaoParaTarefa(colaborador, tarefa,
                List.of(), List.of(proximo), List.of(exigencia), hoje))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Rejeita a alocação de supervisor, antes de avaliar as exigências")
    void deveRejeitarAlocacaoDeSupervisor() {
        Supervisor supervisor = Supervisor.novo("Maria Souza", "98765432100", LocalDate.of(1985, 5, 5), "maria@safeplace.com", "senha");
        Tarefa tarefa = new Tarefa(10, "Espaço Confinado", NivelPerigo.ALTO, hoje.atStartOfDay());

        assertThatThrownBy(() -> validador.validarAptidaoParaTarefa(supervisor, tarefa, List.of(), List.of(), List.of(), hoje))
                .isInstanceOf(ColaboradorNaoAlocavelException.class)
                .hasMessageContaining("Maria Souza");
    }

    @Test
    @DisplayName("Consulta de aptidão informa que gestor não pode ser alocado")
    void deveInformarImpedimentoParaGestorNaConsulta() {
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Carlos Lima", "11122233344", LocalDate.of(1980, 3, 3), "carlos@safeplace.com", "senha");
        Tarefa tarefa = new Tarefa(10, "Espaço Confinado", NivelPerigo.ALTO, hoje.atStartOfDay());

        assertThat(validador.obterImpedimentos(gestor, tarefa, List.of(), List.of(), List.of(), hoje))
                .singleElement().asString().contains("Somente colaboradores");
    }
}
