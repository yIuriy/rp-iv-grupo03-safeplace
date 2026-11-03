package br.edu.safeplace.backend.validacao.ocorrencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.AvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.MotivoAvisoAcao;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Ocorrência, acidente, incidente e notificações (issue #266). Os casos de recusa vieram de
 * {@code OcorrenciaTest}, {@code AcidenteTest}, {@code IncidenteTest}, {@code NotificacaoAcaoTest} e
 * {@code NotificacaoOcorrenciaTest}, com as mesmas asserções: antes, quem lançava eram os construtores e
 * as fábricas das notificações.
 */
@DisplayName("OcorrenciaValidador e validadores de notificação (Chain of Responsibility)")
class OcorrenciaValidadorTest {

    private final OcorrenciaValidador validador = new OcorrenciaValidador();

    private static Acidente acidente(String descricao, LocalDateTime data, CausaRaizAcidente causa,
            String protocolo, NivelPerigo gravidade) {
        return new Acidente(null, descricao, data, LocalDateTime.now(), StatusOcorrencia.ABERTA,
                null, null, null, null, null, causa, "Tipo", "Dano", NumeroProtocoloCAT.de(protocolo), "Destino",
                gravidade);
    }

    private static Incidente incidente(String descricao, LocalDateTime data, String situacao, String potencial) {
        return new Incidente(null, descricao, data, LocalDateTime.now(), StatusOcorrencia.ABERTA,
                null, null, null, null, null, situacao, potencial);
    }

    @Test
    @DisplayName("Aceita acidente e incidente completos, com ou sem protocolo emitido")
    void aceitaOcorrenciasValidas() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        assertThatCode(() -> validador.validar(acidente("Corte", ontem, CausaRaizAcidente.FATOR_HUMANO,
                null, NivelPerigo.BAIXO))).doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(acidente("Corte", ontem, CausaRaizAcidente.FATOR_HUMANO,
                "cat-2026-09-0001", NivelPerigo.BAIXO))).doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(incidente("Piso molhado", ontem, "Vazamento", "Queda")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve rejeitar data do fato no futuro")
    void deveRejeitarDataDoFatoFutura() {
        assertThatThrownBy(() -> validador.validar(incidente("Descricao", LocalDateTime.now().plusDays(1), "Risco", "Dano")))
                .isInstanceOf(DataOcorrenciaInvalidaException.class)
                .hasMessage("Data da ocorrência não pode ser futura.");
    }

    @Test
    @DisplayName("Deve rejeitar data da ocorrência nula")
    void deveRejeitarDataOcorrenciaNula() {
        assertThatThrownBy(() -> validador.validar(incidente("Descricao", null, "Risco", "Dano")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data da ocorrência é obrigatória.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Deve rejeitar descrição nula ou em branco")
    void deveRejeitarDescricaoInvalida(String descricaoInvalida) {
        assertThatThrownBy(() -> validador.validar(incidente(descricaoInvalida, LocalDateTime.now().minusHours(2), "Risco", "Dano")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Descrição é obrigatória.");
    }

    @Test
    @DisplayName("Deve rejeitar acidente sem causa raiz")
    void deveRejeitarAcidenteSemCausaRaiz() {
        assertThatThrownBy(() -> validador.validar(acidente("Descricao", LocalDateTime.now().minusHours(1),
                null, null, NivelPerigo.BAIXO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Causa raiz é obrigatória para acidentes.");
    }

    @Test
    @DisplayName("Deve rejeitar acidente sem gravidade, sem assumir BAIXO (issue #195)")
    void deveRejeitarAcidenteSemGravidade() {
        assertThatThrownBy(() -> validador.validar(acidente("Descricao", LocalDateTime.now().minusHours(1),
                CausaRaizAcidente.FATOR_HUMANO, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Gravidade é obrigatória para acidentes.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALIDO", "12345", "OC-2026-09-0001", "CAT-26-9-1"})
    @DisplayName("Deve rejeitar formato inválido de protocolo CAT")
    void deveRejeitarFormatoInvalidoDeProtocoloCAT(String protocoloInvalido) {
        assertThatThrownBy(() -> validador.validar(acidente("Descricao", LocalDateTime.now().minusHours(1),
                CausaRaizAcidente.FATOR_HUMANO, protocoloInvalido, NivelPerigo.BAIXO)))
                .isInstanceOf(ProtocoloCATInvalidoException.class)
                .hasMessageContaining("Formato de protocolo CAT inválido");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Deve rejeitar incidente com potencial de dano vazio ou nulo")
    void deveRejeitarPotencialDanoInvalido(String potencialInvalido) {
        assertThatThrownBy(() -> validador.validar(incidente("Descricao", LocalDateTime.now().minusHours(1),
                "Piso escorregadio", potencialInvalido)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Potencial de dano é obrigatório para incidentes.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Deve rejeitar incidente com situação de risco vazia ou nula")
    void deveRejeitarSituacaoRiscoInvalida(String situacaoInvalida) {
        assertThatThrownBy(() -> validador.validar(incidente("Descricao", LocalDateTime.now().minusHours(1),
                situacaoInvalida, "Potencial de corte")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Situação de risco é obrigatória para incidentes.");
    }

    @Test
    @DisplayName("Regras da base vêm antes das do subtipo, como nos construtores")
    void regrasDaBaseAntesDoSubtipo() {
        assertThat(validador.verificar(acidente(" ", null, null, null, null)).mensagens()).containsExactly(
                "Data da ocorrência é obrigatória.",
                "Descrição é obrigatória.",
                "Causa raiz é obrigatória para acidentes.",
                "Gravidade é obrigatória para acidentes.");
    }

    @Test
    @DisplayName("Aviso sem ação gravada, sem prazo ou sem responsável único é recusado (NotificacaoAcao.deAviso)")
    void avisoIncompletoRecusado() {
        AvisoAcaoValidador aviso = new AvisoAcaoValidador();
        LocalDate hoje = LocalDate.of(2026, 10, 18);
        Colaborador maria = new Colaborador(7, "52998224725", "Maria Souza", LocalDate.of(1990, 1, 1),
                null, true, LocalDateTime.now(), LocalDateTime.now());
        Setor manutencao = Setor.reconstituir(3, "Manutenção", null, "Bloco B", NivelPerigo.MEDIO);

        assertThatThrownBy(() -> aviso.validar(new AvisoAcao(null, 5, MotivoAvisoAcao.PRAZO, maria, null, hoje, hoje)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> aviso.validar(new AvisoAcao(12, 5, MotivoAvisoAcao.PRAZO, maria, null, null, hoje)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> aviso.validar(new AvisoAcao(12, 5, MotivoAvisoAcao.PRAZO, null, null, hoje, hoje)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> aviso.validar(new AvisoAcao(12, 5, MotivoAvisoAcao.PRAZO, maria, manutencao, hoje, hoje)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> aviso.validar(null)).hasMessage("O aviso exige uma ação já registrada.");
    }

    @Test
    @DisplayName("Ocorrência ainda não gravada não gera notificação (NotificacaoOcorrencia.deNovoRelato)")
    void deveRecusarOcorrenciaSemId() {
        NotificacaoOcorrenciaValidador notificacao = new NotificacaoOcorrenciaValidador();
        Incidente naoGravado = incidente("Piso molhado", LocalDateTime.of(2026, 9, 10, 10, 0), "Vazamento", "Queda");

        assertThatThrownBy(() -> notificacao.validar(naoGravado))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
