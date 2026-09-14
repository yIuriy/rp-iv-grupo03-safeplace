package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcidenteTest {

    @Test
    @DisplayName("Deve criar Acidente completo com campos de ocorrência e específicos de acidente")
    void deveCriarAcidenteCompleto() {
        LocalDateTime dataFato = LocalDateTime.now().minusDays(2);
        AreaRisco area = AreaRisco.novo("Usinagem", "Tornos mecânicos", NivelPerigo.ALTO);
        Colaborador colaborador = Colaborador.novo("Roberto", "87455877074", LocalDate.of(1992, 3, 10), "roberto@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Mariana", "49216091040", LocalDate.of(1988, 7, 20), "mariana@empresa.com", "senha123");

        Acidente acidente = new Acidente(
                1,
                "Queda de ferramenta sobre o pé do operador",
                dataFato,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                List.of("Marcos"),
                colaborador,
                List.of("foto_pe.png"),
                area,
                gestor,
                "Setor de Usinagem",
                null,
                CausaRaiz.FALHA_EPI,
                "Fratura",
                "Contusão no pé",
                "CAT-2026-09-0001",
                "Hospital Santa Casa"
        );

        assertThat(acidente.getId()).isEqualTo(1);
        assertThat(acidente.getDescricao()).isEqualTo("Queda de ferramenta sobre o pé do operador");
        assertThat(acidente.getCausaRaiz()).isEqualTo(CausaRaiz.FALHA_EPI);
        assertThat(acidente.getTipo()).isEqualTo("Fratura");
        assertThat(acidente.getDano()).isEqualTo("Contusão no pé");
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");
        assertThat(acidente.getNumeroProtocolo()).isEqualTo("CAT-2026-09-0001");
        assertThat(acidente.getDestino()).isEqualTo("Hospital Santa Casa");
    }

    @Test
    @DisplayName("Deve rejeitar criação de acidente sem causa raiz")
    void deveRejeitarAcidenteSemCausaRaiz() {
        assertThatThrownBy(() -> new Acidente(
                null,
                "Descricao",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null,
                null,
                "Tipo",
                "Dano",
                null,
                "Destino"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Causa raiz é obrigatória para acidentes.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALIDO", "12345", "OC-2026-09-0001", "CAT-26-9-1"})
    @DisplayName("Deve rejeitar formato inválido de protocolo CAT")
    void deveRejeitarFormatoInvalidoDeProtocoloCAT(String protocoloInvalido) {
        assertThatThrownBy(() -> new Acidente(
                null,
                "Descricao",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null,
                CausaRaiz.FATOR_HUMANO,
                "Tipo",
                "Dano",
                protocoloInvalido,
                "Destino"
        )).isInstanceOf(ProtocoloCATInvalidoException.class);
    }

    @Test
    @DisplayName("Deve consolidar CAT preenchendo numeroProtocoloCAT exclusivo e imutável")
    void deveConsolidarCATComSucesso() {
        LocalDateTime dataFato = LocalDateTime.of(2026, 9, 10, 14, 0);

        Acidente novoAcidente = Acidente.novo(
                "Corte com estilete",
                dataFato,
                AreaRisco.novo("Expedição", "Embalagem", NivelPerigo.MEDIO),
                null, null, null, null,
                CausaRaiz.FATOR_HUMANO,
                "Corte",
                "Lesão superficial na mão",
                "Ambulatório interno"
        );

        assertThat(novoAcidente.getNumeroProtocoloCAT()).isNull();

        Acidente consolidado = novoAcidente.consolidarCAT(15);

        assertThat(consolidado.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0015");
        assertThat(consolidado.getDescricao()).isEqualTo(novoAcidente.getDescricao());
        assertThat(consolidado.getCausaRaiz()).isEqualTo(CausaRaiz.FATOR_HUMANO);

        // Tentativa de consolidar novamente deve ser proibida (readOnly / imutabilidade)
        assertThatThrownBy(() -> consolidado.consolidarCAT(16))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Protocolo CAT já emitido e não pode ser alterado.");
    }

    @Test
    @DisplayName("Deve aceitar construtor de compatibilidade preexistente")
    void deveAceitarConstrutorDeCompatibilidade() {
        Acidente acidente = new Acidente(
                10,
                LocalDateTime.of(2026, 9, 5, 8, 30),
                "Oficina",
                "Ferimento leve",
                null,
                "FALHA_EPI",
                "Batida",
                "Esmagamento",
                "CAT-2026-09-0005",
                "Pronto Socorro"
        );

        assertThat(acidente.getId()).isEqualTo(10);
        assertThat(acidente.getCausaRaiz()).isEqualTo(CausaRaiz.FALHA_EPI);
        assertThat(acidente.getLocal()).isEqualTo("Oficina");
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0005");
    }
}
