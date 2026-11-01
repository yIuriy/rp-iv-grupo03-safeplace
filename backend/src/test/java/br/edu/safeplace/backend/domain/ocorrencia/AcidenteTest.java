package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcidenteTest {

    @Test
    @DisplayName("Deve criar Acidente completo com campos de ocorrência e específicos de acidente")
    void deveCriarAcidenteCompleto() {
        LocalDateTime dataFato = LocalDateTime.now().minusDays(2);
        Epi epi = new Epi(1, "Bota de Segurança", "CA-123", 10, 2, StatusEpi.DISPONIVEL, LocalDate.now().plusYears(1), 180, "Bota", null);
        AreaRisco area = AreaRisco.cadastrar("USI-01", "Usinagem", "Tornos mecânicos", NivelPerigo.ALTO, List.of(epi));
        Colaborador colaborador = Colaborador.novo("Roberto", "87455877074", LocalDate.of(1992, 3, 10), "roberto@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Mariana", "49216091040", LocalDate.of(1988, 7, 20), "mariana@empresa.com", "senha123");

        Acidente acidente = new Acidente(
                1,
                "Queda de ferramenta sobre o pé do operador",
                dataFato,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                gestor,
                null,
                null,
                CausaRaizAcidente.FALHA_EPI,
                "Fratura",
                "Contusão no pé",
                "CAT-2026-09-0001",
                "Hospital Santa Casa",
                NivelPerigo.ALTO
        );

        assertThat(acidente.getId()).isEqualTo(1);
        assertThat(acidente.getDescricao()).isEqualTo("Queda de ferramenta sobre o pé do operador");
        assertThat(acidente.getCausaRaiz()).isEqualTo(CausaRaizAcidente.FALHA_EPI);
        assertThat(acidente.getTipo()).isEqualTo("Fratura");
        assertThat(acidente.getDano()).isEqualTo("Contusão no pé");
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");
        assertThat(acidente.getNumeroProtocolo()).isEqualTo("CAT-2026-09-0001");
        assertThat(acidente.getDestino()).isEqualTo("Hospital Santa Casa");
        assertThat(acidente.getGravidade()).isEqualTo(NivelPerigo.ALTO);
        assertThat(acidente.getRelator()).isEqualTo(gestor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"BAIXO", "medio", " Alto ", "CRITICO"})
    @DisplayName("Lê gravidade válida sem diferenciar maiúsculas")
    void deveLerGravidadeValida(String valor) {
        assertThat(Acidente.lerGravidade(valor)).isEqualTo(NivelPerigo.valueOf(valor.trim().toUpperCase()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"GRAVE", "LEVE", "xyz"})
    @DisplayName("Recusa gravidade ausente ou inválida, sem converter para BAIXO (issue #195)")
    void deveRecusarGravidadeInvalida(String valor) {
        assertThatThrownBy(() -> Acidente.lerGravidade(valor))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Gravidade");
    }

    @Test
    @DisplayName("Deve consolidar CAT preenchendo numeroProtocoloCAT exclusivo e imutável")
    void deveConsolidarCATComSucesso() {
        LocalDateTime dataFato = LocalDateTime.of(2026, 9, 10, 14, 0);

        Acidente novoAcidente = Acidente.novo(
                "Corte com estilete",
                dataFato,
                AreaRisco.cadastrar("EXP-01", "Expedição", "Embalagem", NivelPerigo.MEDIO, List.of(new Epi(2, "Luva", "CA-456", 5, 1, StatusEpi.DISPONIVEL, LocalDate.now().plusYears(1), 180, "Luva", null))),
                null, null,
                CausaRaizAcidente.FATOR_HUMANO,
                "Corte",
                "Lesão superficial na mão",
                "Ambulatório interno",
                NivelPerigo.MEDIO
        );

        assertThat(novoAcidente.getNumeroProtocoloCAT()).isNull();

        Acidente consolidado = novoAcidente.consolidarCAT(15);

        assertThat(consolidado.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0015");
        assertThat(consolidado.getDescricao()).isEqualTo(novoAcidente.getDescricao());
        assertThat(consolidado.getCausaRaiz()).isEqualTo(CausaRaizAcidente.FATOR_HUMANO);

        // Tentativa de consolidar novamente deve ser proibida (readOnly / imutabilidade)
        assertThatThrownBy(() -> consolidado.consolidarCAT(16))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Protocolo CAT já emitido e não pode ser alterado.");
    }

    @Test
    @DisplayName("Deve identificar corretamente se o acidente é grave para regras de plano de ação")
    void deveIdentificarAcidenteGrave() {
        Acidente acidenteLeve = new Acidente(
                1, "Queda sem lesão", LocalDateTime.now().minusHours(1), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, null, null, null, null, null,
                CausaRaizAcidente.OUTRO, "Queda", "Leve", null, null, NivelPerigo.BAIXO
        );
        assertThat(acidenteLeve.getGravidade()).isEqualTo(NivelPerigo.BAIXO);
        assertThat(acidenteLeve.isGrave()).isFalse();

        Acidente acidenteGrave = new Acidente(
                2, "Queda de altura", LocalDateTime.now().minusHours(1), LocalDateTime.now(),
                StatusOcorrencia.ABERTA, null, null, null, null, null,
                CausaRaizAcidente.FALHA_EPI, "Queda", "Grave", null, null, NivelPerigo.CRITICO
        );
        assertThat(acidenteGrave.getGravidade()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(acidenteGrave.isGrave()).isTrue();
    }
}
