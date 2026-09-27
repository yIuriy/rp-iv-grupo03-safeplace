package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanoDeAcaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 20);

    private static Colaborador colaborador(Integer id) {
        return new Colaborador(id, "87455877074", "Roberto", LocalDate.of(1992, 3, 10),
                "roberto@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private static Setor setor(Integer id) {
        return Setor.reconstituir(id, "Usinagem", null, "Bloco B", NivelPerigo.ALTO);
    }

    private static Acidente acidente(NivelPerigo gravidade, LocalDateTime dataFato) {
        return new Acidente(10, "Corte na mão", dataFato, dataFato, StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null,
                CausaRaizAcidente.FATOR_HUMANO, "Corte", "Lesão leve", null, null, gravidade);
    }

    private static Acidente acidente(NivelPerigo gravidade) {
        return acidente(gravidade, LocalDateTime.of(2026, 9, 18, 10, 0));
    }

    private static Incidente incidente() {
        LocalDateTime fato = LocalDateTime.of(2026, 9, 18, 10, 0);
        return new Incidente(11, "Caixas acima do limite", fato, fato, StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null, "Empilhamento irregular", "Queda de material");
    }

    private static PlanoDeAcao acaoCorretiva(Ocorrencia ocorrencia, LocalDate prazo) {
        return PlanoDeAcao.definir(ocorrencia, "Isolar máquina", null, prazo,
                colaborador(1), null, false, null, null, HOJE);
    }

    private static PlanoDeAcao acaoComAlerta(LocalDate prazo, int antecedencia) {
        return PlanoDeAcao.definir(acidente(NivelPerigo.BAIXO), "Isolar máquina", "Treinar equipe", prazo,
                colaborador(1), null, true, antecedencia, null, HOJE);
    }

    @Nested
    @DisplayName("Definição da ação")
    class Definicao {

        @Test
        @DisplayName("Deve definir ação aberta vinculada à ocorrência e com responsável colaborador")
        void deveDefinirAcao() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(5));

            assertThat(acao.getIdOcorrencia()).isEqualTo(10);
            assertThat(acao.getStatus()).isEqualTo("ABERTA");
            assertThat(acao.getSituacao()).contains(StatusPlanoDeAcao.ABERTA);
            assertThat(acao.getResponsavelColaborador().getId()).isEqualTo(1);
            assertThat(acao.getResponsavelSetor()).isNull();
            assertThat(acao.possuiMedidaCorretiva()).isTrue();
            assertThat(acao.possuiMedidaPreventiva()).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar setor como responsável, sem conta de acesso")
        void deveAceitarSetorComoResponsavel() {
            PlanoDeAcao acao = PlanoDeAcao.definir(acidente(NivelPerigo.BAIXO), null, "Sinalizar área",
                    HOJE, null, setor(3), false, null, null, HOJE);

            assertThat(acao.getResponsavelSetor().getId()).isEqualTo(3);
            assertThat(acao.getResponsavelColaborador()).isNull();
        }

        @Test
        @DisplayName("Deve exigir exatamente um responsável cadastrado")
        void deveExigirUmResponsavel() {
            Acidente ocorrencia = acidente(NivelPerigo.BAIXO);

            assertThatThrownBy(() -> PlanoDeAcao.definir(ocorrencia, "Medida", null, HOJE,
                    null, null, false, null, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> PlanoDeAcao.definir(ocorrencia, "Medida", null, HOJE,
                    colaborador(1), setor(3), false, null, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> PlanoDeAcao.definir(ocorrencia, "Medida", null, HOJE,
                    colaborador(null), null, false, null, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class)
                    .hasMessage("O responsável pela ação deve estar cadastrado.");
        }

        @Test
        @DisplayName("Deve exigir ocorrência registrada e ao menos uma medida")
        void deveExigirOcorrenciaEMedida() {
            Acidente naoRegistrado = new Acidente(null, "Corte", LocalDateTime.of(2026, 9, 18, 10, 0), null,
                    StatusOcorrencia.ABERTA, null, null, null, null, null, null, null,
                    CausaRaizAcidente.OUTRO, null, null, null, null, NivelPerigo.BAIXO);

            assertThatThrownBy(() -> acaoCorretiva(naoRegistrado, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> PlanoDeAcao.definir(acidente(NivelPerigo.BAIXO), " ", null, HOJE,
                    colaborador(1), null, false, null, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }

        @Test
        @DisplayName("Deve bloquear prazo ausente, anterior à data atual ou à ocorrência")
        void deveBloquearPrazoRetroativo() {
            assertThatThrownBy(() -> acaoCorretiva(acidente(NivelPerigo.BAIXO), null))
                    .isInstanceOf(PrazoAcaoInvalidoException.class);
            assertThatThrownBy(() -> acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.minusDays(1)))
                    .isInstanceOf(PrazoAcaoInvalidoException.class)
                    .hasMessage("O prazo da ação não pode ser anterior à data atual.");

            // Data de referência anterior ao fato isola a regra da data da ocorrência.
            Acidente ocorrencia = acidente(NivelPerigo.BAIXO, LocalDateTime.of(2026, 9, 18, 10, 0));
            assertThatThrownBy(() -> PlanoDeAcao.definir(ocorrencia, "Medida", null, LocalDate.of(2026, 9, 17),
                    colaborador(1), null, false, null, null, LocalDate.of(2026, 9, 16)))
                    .isInstanceOf(PrazoAcaoInvalidoException.class)
                    .hasMessage("O prazo da ação não pode ser anterior à data da ocorrência.");
        }

        @Test
        @DisplayName("Deve aceitar prazo igual à data atual")
        void deveAceitarPrazoNaDataAtual() {
            assertThat(acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE).getPrazo()).isEqualTo(HOJE);
        }
    }

    @Nested
    @DisplayName("Plano da ocorrência e ocorrência grave")
    class ValidacaoDoPlano {

        @Test
        @DisplayName("Ocorrência grave sem ação preventiva deve indicar pendência")
        void ocorrenciaGraveSemPreventiva() {
            Acidente grave = acidente(NivelPerigo.ALTO);

            assertThatThrownBy(() -> PlanoDeAcao.validarPlano(grave, List.of(acaoCorretiva(grave, HOJE))))
                    .isInstanceOf(AcaoPreventivaObrigatoriaException.class);
        }

        @Test
        @DisplayName("Ocorrência grave com ação preventiva deve ser aceita")
        void ocorrenciaGraveComPreventiva() {
            Acidente critico = acidente(NivelPerigo.CRITICO);
            PlanoDeAcao preventiva = PlanoDeAcao.definir(critico, null, "Treinar equipe", HOJE,
                    colaborador(1), null, false, null, null, HOJE);

            PlanoDeAcao.validarPlano(critico, List.of(acaoCorretiva(critico, HOJE), preventiva));
        }

        @Test
        @DisplayName("Ocorrência não grave e incidente não exigem ação preventiva")
        void naoGraveNaoExigePreventiva() {
            Acidente medio = acidente(NivelPerigo.MEDIO);
            Incidente incidente = incidente();

            PlanoDeAcao.validarPlano(medio, List.of(acaoCorretiva(medio, HOJE)));
            PlanoDeAcao.validarPlano(incidente, List.of(acaoCorretiva(incidente, HOJE)));
            assertThat(incidente.isGrave()).isFalse();
        }

        @Test
        @DisplayName("Plano deve conter ações da própria ocorrência")
        void planoDeveConterAcoesDaOcorrencia() {
            Acidente ocorrencia = acidente(NivelPerigo.BAIXO);

            assertThatThrownBy(() -> PlanoDeAcao.validarPlano(ocorrencia, List.of()))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> PlanoDeAcao.validarPlano(ocorrencia, List.of(acaoCorretiva(incidente(), HOJE))))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }
    }

    @Nested
    @DisplayName("Situação da ação")
    class Situacao {

        @Test
        @DisplayName("Deve concluir ação aberta e impedir alterações depois")
        void deveConcluirAcao() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(5));

            assertThat(acao.validarAtualizacaoStatus(StatusPlanoDeAcao.CONCLUIDA)).isTrue();
            acao.atualizarStatus(StatusPlanoDeAcao.CONCLUIDA);

            assertThat(acao.getStatus()).isEqualTo("CONCLUIDA");
            assertThat(acao.validarAtualizacaoStatus(StatusPlanoDeAcao.ABERTA)).isFalse();
            assertThatThrownBy(() -> acao.atualizarStatus(StatusPlanoDeAcao.ABERTA))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> acao.prorrogarPrazo(HOJE.plusDays(9), "Motivo", HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThat(acao.isEmAtraso(HOJE.plusDays(30))).isFalse();
        }

        @Test
        @DisplayName("Atraso é derivado do prazo, sem mudar a situação gravada")
        void atrasoDerivado() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(2));

            assertThat(acao.isEmAtraso(HOJE.plusDays(2))).isFalse();
            assertThat(acao.isEmAtraso(HOJE.plusDays(3))).isTrue();
            assertThat(acao.getStatus()).isEqualTo("ABERTA");
        }

        @Test
        @DisplayName("Situação legada fora do contrato é preservada, mas não é convertida por suposição")
        void situacaoLegadaPreservada() {
            PlanoDeAcao legado = new PlanoDeAcao(5, "Isolar máquina", HOJE, "PENDENTE", "Treinar");

            assertThat(legado.getStatus()).isEqualTo("PENDENTE");
            assertThat(legado.getSituacao()).isEmpty();
            assertThat(legado.validarAtualizacaoStatus(StatusPlanoDeAcao.CONCLUIDA)).isFalse();
            assertThatThrownBy(() -> legado.atualizarResponsavel(colaborador(2), HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class)
                    .hasMessageContaining("não reconhecida (PENDENTE)");
        }
    }

    @Nested
    @DisplayName("Prorrogação de prazo")
    class Prorrogacao {

        @Test
        @DisplayName("Deve prorrogar com justificativa preservando o prazo anterior")
        void deveProrrogar() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(2));

            ProrrogacaoPrazo registro = acao.prorrogarPrazo(HOJE.plusDays(10), "  Aguardando peça  ", HOJE);

            assertThat(registro.prazoAnterior()).isEqualTo(HOJE.plusDays(2));
            assertThat(registro.novoPrazo()).isEqualTo(HOJE.plusDays(10));
            assertThat(registro.justificativa()).isEqualTo("Aguardando peça");
            assertThat(acao.getPrazo()).isEqualTo(HOJE.plusDays(10));
        }

        @Test
        @DisplayName("Deve exigir justificativa e novo prazo posterior ao atual e à data atual")
        void deveValidarProrrogacao() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(2));

            assertThatThrownBy(() -> acao.prorrogarPrazo(HOJE.plusDays(5), " ", HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> acao.prorrogarPrazo(HOJE.plusDays(2), "Motivo", HOJE))
                    .isInstanceOf(PrazoAcaoInvalidoException.class);
            assertThatThrownBy(() -> acao.prorrogarPrazo(HOJE.plusDays(4), "Motivo", HOJE.plusDays(5)))
                    .isInstanceOf(PrazoAcaoInvalidoException.class)
                    .hasMessage("O novo prazo não pode ser anterior à data atual.");
            assertThat(acao.getPrazo()).isEqualTo(HOJE.plusDays(2));
        }

        @Test
        @DisplayName("Prorrogação deve reajustar a data do alerta")
        void prorrogacaoReajustaAlerta() {
            PlanoDeAcao acao = acaoComAlerta(HOJE.plusDays(5), 3);
            assertThat(acao.getDataAlerta()).contains(HOJE.plusDays(2));

            acao.prorrogarPrazo(HOJE.plusDays(15), "Fornecedor atrasou", HOJE);

            assertThat(acao.getDataAlerta()).contains(HOJE.plusDays(12));
            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(2))).isFalse();
        }
    }

    @Nested
    @DisplayName("Responsável e alertas")
    class ResponsavelEAlertas {

        @Test
        @DisplayName("Reatribuição troca o responsável e gera aviso ao novo encarregado")
        void reatribuicao() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(5));

            AvisoAcao aviso = acao.atualizarResponsavel(setor(3), HOJE);

            assertThat(acao.getResponsavelSetor().getId()).isEqualTo(3);
            assertThat(acao.getResponsavelColaborador()).isNull();
            assertThat(aviso.motivo()).isEqualTo(MotivoAvisoAcao.REATRIBUICAO);
            assertThat(aviso.responsavelSetor().getId()).isEqualTo(3);
            assertThat(aviso.idOcorrencia()).isEqualTo(10);

            AvisoAcao outro = acao.atualizarResponsavel(colaborador(2), HOJE);
            assertThat(outro.responsavelColaborador().getId()).isEqualTo(2);
            assertThat(acao.getResponsavelSetor()).isNull();
        }

        @Test
        @DisplayName("Reatribuição ao mesmo responsável deve ser rejeitada")
        void reatribuicaoAoMesmoResponsavel() {
            PlanoDeAcao acao = acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE.plusDays(5));

            assertThatThrownBy(() -> acao.atualizarResponsavel(colaborador(1), HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }

        @Test
        @DisplayName("Alerta segue a antecedência configurada, sem valor fixo")
        void alertaConfiguravel() {
            PlanoDeAcao acao = acaoComAlerta(HOJE.plusDays(10), 7);

            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(2))).isFalse();
            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(3))).isTrue();
            assertThat(acao.emitirAvisoPrazo(HOJE.plusDays(3)))
                    .hasValueSatisfying(aviso -> assertThat(aviso.motivo()).isEqualTo(MotivoAvisoAcao.PRAZO));

            acao.configurarAlerta(true, 0);
            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(9))).isFalse();
            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(10))).isTrue();

            acao.configurarAlerta(false, 5);
            assertThat(acao.getAntecedenciaAlertaDias()).isNull();
            assertThat(acao.emitirAvisoPrazo(HOJE.plusDays(10))).isEmpty();
        }

        @Test
        @DisplayName("Alerta ativo exige antecedência não negativa")
        void alertaExigeAntecedencia() {
            assertThatThrownBy(() -> acaoComAlerta(HOJE, -1)).isInstanceOf(PlanoDeAcaoInvalidoException.class);
            assertThatThrownBy(() -> PlanoDeAcao.definir(acidente(NivelPerigo.BAIXO), "Medida", null, HOJE,
                    colaborador(1), null, true, null, null, HOJE))
                    .isInstanceOf(PlanoDeAcaoInvalidoException.class);
        }

        @Test
        @DisplayName("Ação concluída não emite alerta")
        void acaoConcluidaNaoAlerta() {
            PlanoDeAcao acao = acaoComAlerta(HOJE.plusDays(1), 1);
            acao.atualizarStatus(StatusPlanoDeAcao.CONCLUIDA);

            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(5))).isFalse();
        }
    }
}
