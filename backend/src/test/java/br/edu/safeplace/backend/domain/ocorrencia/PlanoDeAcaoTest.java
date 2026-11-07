package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
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
        return Acidente.builder()
                .idOcorrencia(10)
                .descricao("Corte na mão")
                .dataOcorrencia(dataFato)
                .dataRegistro(dataFato)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .causaRaiz(CausaRaizAcidente.FATOR_HUMANO)
                .tipo("Corte")
                .dano("Lesão leve")
                .gravidade(gravidade)
                .build();
    }

    private static Acidente acidente(NivelPerigo gravidade) {
        return acidente(gravidade, LocalDateTime.of(2026, 9, 18, 10, 0));
    }

    private static Incidente incidente() {
        LocalDateTime fato = LocalDateTime.of(2026, 9, 18, 10, 0);
        return Incidente.builder()
                .idOcorrencia(11)
                .descricao("Caixas acima do limite")
                .dataOcorrencia(fato)
                .dataRegistro(fato)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .situacaoRisco("Empilhamento irregular")
                .potencialDano("Queda de material")
                .build();
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
        @DisplayName("Deve aceitar prazo igual à data atual")
        void deveAceitarPrazoNaDataAtual() {
            assertThat(acaoCorretiva(acidente(NivelPerigo.BAIXO), HOJE).getPrazo()).isEqualTo(HOJE);
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
        @DisplayName("Ação concluída não emite alerta")
        void acaoConcluidaNaoAlerta() {
            PlanoDeAcao acao = acaoComAlerta(HOJE.plusDays(1), 1);
            acao.atualizarStatus(StatusPlanoDeAcao.CONCLUIDA);

            assertThat(acao.deveEmitirAlerta(HOJE.plusDays(5))).isFalse();
        }
    }
}
