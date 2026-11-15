package br.edu.safeplace.backend.domain.capacitacao;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoa;
import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.pessoas;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao.TipoAlvo;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao.TipoCapacitacao;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/** Papéis Creator, ConcreteCreator e Products do Factory Method (#268). */
@DisplayName("FabricaCapacitacaoPadrao — Factory Method de capacitações (#268)")
class FabricaCapacitacaoPadraoTest {

    private final FabricaCapacitacao fabrica = new FabricaCapacitacaoPadrao();

    @Test
    @DisplayName("Cria Certificacao nova a partir do CadastrarCertificacaoInputDTO inteiro")
    void criaCertificacaoAPartirDoInputInteiro() {
        LocalDate hoje = LocalDate.of(2026, 11, 15);
        LocalDate emissao = hoje.minusMonths(1);
        LocalDate vencimento = hoje.plusYears(1);
        Colaborador colaborador = pessoa(10);
        CadastrarCertificacaoInputDTO entrada = new CadastrarCertificacaoInputDTO(
                "NR-35 Trabalho em Altura", vencimento, emissao, 10);

        Certificacao cert = fabrica.criarCertificacao(entrada, colaborador, hoje);

        assertThat(cert.getId()).isNull();
        assertThat(cert.getNome()).isEqualTo("NR-35 Trabalho em Altura");
        assertThat(cert.getDataVencimento()).isEqualTo(vencimento);
        assertThat(cert.getDataEmissao()).isEqualTo(emissao);
        assertThat(cert.getColaborador()).isSameAs(colaborador);
        assertThat(cert.getStatus()).isEqualTo(StatusCertificacao.EM_DIA);
    }

    @Test
    @DisplayName("Cria Treinamento novo a partir do CadastrarTreinamentoInputDTO inteiro")
    void criaTreinamentoAPartirDoInputInteiro() {
        LocalDate realizacao = LocalDate.of(2026, 10, 10);
        List<Colaborador> participantes = pessoas(List.of(1, 2));
        CadastrarTreinamentoInputDTO entrada = new CadastrarTreinamentoInputDTO(
                "NR-10 Segurança em Eletricidade",
                "Módulo prático",
                "Carlos Instrutor",
                realizacao,
                40,
                12,
                List.of(1, 2));

        Treinamento treino = fabrica.criarTreinamento(entrada, participantes);

        assertThat(treino.getId()).isNull();
        assertThat(treino.getNome()).isEqualTo("NR-10 Segurança em Eletricidade");
        assertThat(treino.getConteudo()).isEqualTo("Módulo prático");
        assertThat(treino.getInstrutor()).isEqualTo("Carlos Instrutor");
        assertThat(treino.getDataRealizacao()).isEqualTo(realizacao);
        assertThat(treino.getCargaHoraria()).isEqualTo(40);
        assertThat(treino.getValidadeMeses()).isEqualTo(12);
        assertThat(treino.getColaboradoresIds()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("Cria ExigenciaCapacitacao nova a partir do DefinirExigenciaInputDTO inteiro")
    void criaExigenciaAPartirDoInputInteiro() {
        DefinirExigenciaInputDTO entrada = new DefinirExigenciaInputDTO(
                "NR-35 Trabalho em Altura", TipoCapacitacao.CERTIFICACAO, TipoAlvo.TAREFA, 5);

        ExigenciaCapacitacao exigencia = fabrica.criarExigencia(entrada);

        assertThat(exigencia.getId()).isNull();
        assertThat(exigencia.getNomeCapacitacao()).isEqualTo("NR-35 Trabalho em Altura");
        assertThat(exigencia.getTipoCapacitacao()).isEqualTo(TipoCapacitacao.CERTIFICACAO);
        assertThat(exigencia.getAlvo()).isEqualTo(TipoAlvo.TAREFA);
        assertThat(exigencia.getAlvoId()).isEqualTo(5);
    }

    @Test
    @DisplayName("Não valida regras de negócio na criação (delegadas aos validadores em cadeia)")
    void naoValidaRegrasDeNegocioNaCriacao() {
        LocalDate hoje = LocalDate.of(2026, 11, 15);
        Certificacao certVencida = fabrica.criarCertificacao(
                new CadastrarCertificacaoInputDTO(" ", hoje.minusDays(1), hoje.plusDays(1), null),
                null,
                hoje);
        Treinamento treinoInvalido = fabrica.criarTreinamento(
                new CadastrarTreinamentoInputDTO(" ", null, null, hoje.plusDays(5), -1, 0, List.of()),
                List.of());
        ExigenciaCapacitacao exigenciaInvalida = fabrica.criarExigencia(
                new DefinirExigenciaInputDTO(" ", null, null, null));

        assertThat(certVencida.getStatus()).isEqualTo(StatusCertificacao.VENCIDA);
        assertThat(treinoInvalido.getColaboradores()).isEmpty();
        assertThat(exigenciaInvalida.getAlvoId()).isNull();
    }

    @Test
    @DisplayName("Recusa entrada nula nos três métodos de criação")
    void recusaEntradaNula() {
        assertThatThrownBy(() -> fabrica.criarCertificacao(null, pessoa(1), LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fabrica.criarTreinamento(null, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fabrica.criarExigencia(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
