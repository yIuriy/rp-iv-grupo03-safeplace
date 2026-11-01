package br.edu.safeplace.backend.validacao.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Os casos vieram de {@code PlanoDeAcaoTest} com as mesmas asserções (issue #266): antes, quem
 * lançava era {@code PlanoDeAcao.definir}, o construtor e o estático {@code PlanoDeAcao.validarPlano};
 * agora são as cadeias {@link AcaoPlanoValidador} e {@link PlanoValidador}, antes do repository.
 */
@DisplayName("AcaoPlanoValidador e PlanoValidador - plano de ação (Chain of Responsibility)")
class PlanoDeAcaoValidadoresTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 20);

    private final AcaoPlanoValidador acaoValidador = new AcaoPlanoValidador();
    private final PlanoValidador planoValidador = new PlanoValidador();

    private static Colaborador colaborador(Integer id) {
        return new Colaborador(id, "87455877074", "Roberto", LocalDate.of(1992, 3, 10),
                "roberto@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private static Setor setor(Integer id) {
        return Setor.reconstituir(id, "Usinagem", null, "Bloco B", NivelPerigo.ALTO);
    }

    private static Acidente acidente(NivelPerigo gravidade, LocalDateTime dataFato) {
        return new Acidente(10, "Corte na mão", dataFato, dataFato, StatusOcorrencia.ABERTA,
                null, null, null, null, null,
                CausaRaizAcidente.FATOR_HUMANO, "Corte", "Lesão leve", null, null, gravidade);
    }

    private static Acidente acidente(NivelPerigo gravidade) {
        return acidente(gravidade, LocalDateTime.of(2026, 9, 18, 10, 0));
    }

    private static Incidente incidente() {
        LocalDateTime fato = LocalDateTime.of(2026, 9, 18, 10, 0);
        return new Incidente(11, "Caixas acima do limite", fato, fato, StatusOcorrencia.ABERTA,
                null, null, null, null, null, "Empilhamento irregular", "Queda de material");
    }

    private static PlanoDeAcao acaoCorretiva(Ocorrencia ocorrencia, LocalDate prazo) {
        return PlanoDeAcao.definir(ocorrencia, "Isolar máquina", null, prazo,
                colaborador(1), null, false, null, null, HOJE);
    }

    /** O fluxo do caso de uso: definir a ação e validá-la antes de gravar. */
    private void definirEValidar(Ocorrencia ocorrencia, String corretiva, String preventiva, LocalDate prazo,
            Colaborador colaborador, Setor setor, boolean alertaAtivo, Integer antecedencia, LocalDate hoje) {
        PlanoDeAcao acao = PlanoDeAcao.definir(ocorrencia, corretiva, preventiva, prazo,
                colaborador, setor, alertaAtivo, antecedencia, null, hoje);
        acaoValidador.validar(new AcaoDoPlano(acao, ocorrencia, hoje));
    }

    @Nested
    @DisplayName("Definição da ação")
    class Definicao {

        @Test
        @DisplayName("Aceita ação com responsável, medida e prazo na data atual")
        void aceitaAcaoValida() {
            assertThatCode(() -> definirEValidar(acidente(NivelPerigo.BAIXO), "Isolar máquina", null, HOJE,
                    colaborador(1), null, false, null, HOJE)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve exigir exatamente um responsável cadastrado")
        void deveExigirUmResponsavel() {
            Acidente ocorrencia = acidente(NivelPerigo.BAIXO);

            assertThatThrownBy(() -> definirEValidar(ocorrencia, "Medida", null, HOJE,
                    null, null, false, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> definirEValidar(ocorrencia, "Medida", null, HOJE,
                    colaborador(1), setor(3), false, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> definirEValidar(ocorrencia, "Medida", null, HOJE,
                    colaborador(null), null, false, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class)
                    .hasMessage("O responsável pela ação deve estar cadastrado.");
        }

        @Test
        @DisplayName("Deve exigir ocorrência registrada e ao menos uma medida")
        void deveExigirOcorrenciaEMedida() {
            Acidente naoRegistrado = new Acidente(null, "Corte", LocalDateTime.of(2026, 9, 18, 10, 0), null,
                    StatusOcorrencia.ABERTA, null, null, null, null, null,
                    CausaRaizAcidente.OUTRO, null, null, null, null, NivelPerigo.BAIXO);

            assertThatThrownBy(() -> definirEValidar(naoRegistrado, "Isolar máquina", null, HOJE,
                    colaborador(1), null, false, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> definirEValidar(acidente(NivelPerigo.BAIXO), " ", null, HOJE,
                    colaborador(1), null, false, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }

        @Test
        @DisplayName("Deve bloquear prazo ausente, anterior à data atual ou à ocorrência")
        void deveBloquearPrazoRetroativo() {
            assertThatThrownBy(() -> definirEValidar(acidente(NivelPerigo.BAIXO), "Isolar máquina", null, null,
                    colaborador(1), null, false, null, HOJE))
                    .isInstanceOf(PrazoAcaoInvalidoException.class);
            assertThatThrownBy(() -> definirEValidar(acidente(NivelPerigo.BAIXO), "Isolar máquina", null, HOJE.minusDays(1),
                    colaborador(1), null, false, null, HOJE))
                    .isInstanceOf(PrazoAcaoInvalidoException.class)
                    .hasMessage("O prazo da ação não pode ser anterior à data atual.");

            // Data de referência anterior ao fato isola a regra da data da ocorrência.
            Acidente ocorrencia = acidente(NivelPerigo.BAIXO, LocalDateTime.of(2026, 9, 18, 10, 0));
            assertThatThrownBy(() -> definirEValidar(ocorrencia, "Medida", null, LocalDate.of(2026, 9, 17),
                    colaborador(1), null, false, null, LocalDate.of(2026, 9, 16)))
                    .isInstanceOf(PrazoAcaoInvalidoException.class)
                    .hasMessage("O prazo da ação não pode ser anterior à data da ocorrência.");
        }

        @Test
        @DisplayName("Alerta ativo exige antecedência não negativa")
        void alertaExigeAntecedencia() {
            assertThatThrownBy(() -> definirEValidar(acidente(NivelPerigo.BAIXO), "Isolar máquina", "Treinar equipe", HOJE,
                    colaborador(1), null, true, -1, HOJE)).isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> definirEValidar(acidente(NivelPerigo.BAIXO), "Medida", null, HOJE,
                    colaborador(1), null, true, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }

        @Test
        @DisplayName("Lista os problemas na ordem de PlanoDeAcao.definir")
        void listaProblemasNaOrdemAntiga() {
            PlanoDeAcao acao = PlanoDeAcao.definir(acidente(NivelPerigo.BAIXO), " ", null, null,
                    null, null, true, -1, null, HOJE);

            assertThat(acaoValidador.verificar(new AcaoDoPlano(acao, acidente(NivelPerigo.BAIXO), HOJE)).mensagens())
                    .containsExactly(
                            "Informe a medida corretiva ou a medida preventiva da ação.",
                            "Informe um único responsável pela ação: colaborador ou setor.",
                            "O prazo da ação é obrigatório.",
                            "A antecedência do alerta não pode ser negativa.");
        }
    }

    @Nested
    @DisplayName("Plano da ocorrência e ocorrência grave")
    class ValidacaoDoPlano {

        @Test
        @DisplayName("Ocorrência grave sem ação preventiva deve indicar pendência")
        void ocorrenciaGraveSemPreventiva() {
            Acidente grave = acidente(NivelPerigo.ALTO);

            assertThatThrownBy(() -> planoValidador.validar(new PlanoDaOcorrencia(grave, List.of(acaoCorretiva(grave, HOJE)))))
                    .isInstanceOf(AcaoPreventivaObrigatoriaException.class);
        }

        @Test
        @DisplayName("Ocorrência grave com ação preventiva deve ser aceita")
        void ocorrenciaGraveComPreventiva() {
            Acidente critico = acidente(NivelPerigo.CRITICO);
            PlanoDeAcao preventiva = PlanoDeAcao.definir(critico, null, "Treinar equipe", HOJE,
                    colaborador(1), null, false, null, null, HOJE);

            planoValidador.validar(new PlanoDaOcorrencia(critico, List.of(acaoCorretiva(critico, HOJE), preventiva)));
        }

        @Test
        @DisplayName("Ocorrência não grave e incidente não exigem ação preventiva")
        void naoGraveNaoExigePreventiva() {
            Acidente medio = acidente(NivelPerigo.MEDIO);
            Incidente incidente = incidente();

            planoValidador.validar(new PlanoDaOcorrencia(medio, List.of(acaoCorretiva(medio, HOJE))));
            planoValidador.validar(new PlanoDaOcorrencia(incidente, List.of(acaoCorretiva(incidente, HOJE))));
            assertThat(incidente.isGrave()).isFalse();
        }

        @Test
        @DisplayName("Plano deve conter ações da própria ocorrência")
        void planoDeveConterAcoesDaOcorrencia() {
            Acidente ocorrencia = acidente(NivelPerigo.BAIXO);

            assertThatThrownBy(() -> planoValidador.validar(new PlanoDaOcorrencia(ocorrencia, List.of())))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> planoValidador.validar(new PlanoDaOcorrencia(ocorrencia,
                    List.of(acaoCorretiva(incidente(), HOJE)))))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }
    }
}
