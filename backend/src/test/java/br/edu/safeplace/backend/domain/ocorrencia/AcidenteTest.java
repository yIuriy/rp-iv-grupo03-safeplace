package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
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
        Epi epi = EpiDeTeste.umEpi()
                .comId(1)
                .comNome("Bota de biqueira de aço")
                .comCa("CA-789")
                .comQuantidade(8)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(180)
                .comDescricao("Bota")
                .build();
        AreaRisco area = AreaRisco.cadastrar("ALMOX-01", "Almoxarifado", "Estocagem pesada", NivelPerigo.ALTO, List.of(epi));
        Colaborador colaborador = Colaborador.novo("José", "63863481079", LocalDate.of(1988, 3, 10), "jose@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Ana", "49216091040", LocalDate.of(1985, 5, 5), "ana@empresa.com", "senha123");

        Acidente acidente = Acidente.builder()
                .idOcorrencia(1)
                .descricao("Queda de ferramenta sobre o pé do operador")
                .dataOcorrencia(dataFato)
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .relator(gestor)
                .causaRaiz(CausaRaizAcidente.FALHA_EPI)
                .tipo("Esmagamento")
                .dano("Fratura no metatarso")
                .numeroProtocoloCAT(NumeroProtocoloCAT.de("CAT-2026-09-0001"))
                .destino("Hospital Municipal")
                .gravidade(NivelPerigo.CRITICO)
                .build();

        assertThat(acidente.getId()).isEqualTo(1);
        assertThat(acidente.getDescricao()).isEqualTo("Queda de ferramenta sobre o pé do operador");
        assertThat(acidente.getCausaRaiz()).isEqualTo(CausaRaizAcidente.FALHA_EPI);
        assertThat(acidente.getTipo()).isEqualTo("Esmagamento");
        assertThat(acidente.getDano()).isEqualTo("Fratura no metatarso");
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0001");
        assertThat(acidente.getDestino()).isEqualTo("Hospital Municipal");
        assertThat(acidente.getGravidade()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(acidente.isGrave()).isTrue();
        assertThat(acidente.getTipo()).isEqualTo("Esmagamento");
        assertThat(acidente.getTipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
    }

    @Test
    @DisplayName("isGrave deve retornar false para gravidade BAIXO e MEDIO")
    void isGraveDeveRetornarFalseParaGravidadeNaoGrave() {
        Acidente acidenteBaixo = Acidente.builder()
                .idOcorrencia(1)
                .descricao("Desc")
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .gravidade(NivelPerigo.BAIXO)
                .build();
        assertThat(acidenteBaixo.isGrave()).isFalse();

        Acidente acidenteMedio = Acidente.builder()
                .idOcorrencia(2)
                .descricao("Desc")
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .gravidade(NivelPerigo.MEDIO)
                .build();
        assertThat(acidenteMedio.isGrave()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"BAIXO", "baixo", "MEDIO", "medio", "ALTO", "alto", "CRITICO", "critico"})
    @DisplayName("lerGravidade deve fazer o parse correto ignorando case")
    void lerGravidadeDeveAceitarValoresValidos(String entrada) {
        NivelPerigo nivel = Acidente.lerGravidade(entrada);
        assertThat(nivel).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "INVALIDO", "GRAVE", "LEVE"})
    @DisplayName("lerGravidade deve rejeitar valor nulo, vazio ou inválido")
    void lerGravidadeDeveRejeitarValoresInvalidos(String entrada) {
        assertThatThrownBy(() -> Acidente.lerGravidade(entrada))
                .isInstanceOf(DadosOcorrenciaInvalidosException.class)
                .hasMessageContaining("Gravidade");
    }

    @Test
    @DisplayName("Deve consolidar CAT preenchendo numeroProtocoloCAT exclusivo e imutável")
    void deveConsolidarCATComSucesso() {
        LocalDateTime dataFato = LocalDateTime.of(2026, 9, 10, 14, 0);

        Acidente novoAcidente = Acidente.builder()
                .descricao("Corte com estilete")
                .dataOcorrencia(dataFato)
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .area(AreaRisco.cadastrar("EXP-01", "Expedição", "Embalagem", NivelPerigo.MEDIO, List.of(EpiDeTeste.umEpi()
                        .comId(2)
                        .comNome("Luva")
                        .comCa("CA-456")
                        .comQuantidade(5)
                        .comEstoqueMinimo(1)
                        .comStatus(StatusEpi.DISPONIVEL)
                        .comValidadeCa(LocalDate.now().plusYears(1))
                        .comVidaUtilDias(180)
                        .comDescricao("Luva")
                        .build())))
                .causaRaiz(CausaRaizAcidente.FATOR_HUMANO)
                .tipo("Corte")
                .dano("Lesão superficial na mão")
                .destino("Ambulatório interno")
                .gravidade(NivelPerigo.MEDIO)
                .build();

        assertThat(novoAcidente.getNumeroProtocoloCAT()).isNull();

        Acidente consolidado = novoAcidente.consolidarCAT(15);

        assertThat(consolidado.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0015");
        assertThat(consolidado.getDescricao()).isEqualTo(novoAcidente.getDescricao());
        assertThat(consolidado.getCausaRaiz()).isEqualTo(CausaRaizAcidente.FATOR_HUMANO);

        // Tentativa de consolidar novamente deve ser proibida (readOnly / imutabilidade)
        assertThatThrownBy(() -> consolidado.consolidarCAT(16))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Protocolo CAT já emitido e não pode ser alterado.");
    }

    @Test
    @DisplayName("Deve identificar tipo como ACIDENTE (#239)")
    void deveIdentificarTipo() {
        Acidente acidente = Acidente.builder()
                .idOcorrencia(1)
                .descricao("Desc")
                .causaRaiz(CausaRaizAcidente.OUTRO)
                .gravidade(NivelPerigo.BAIXO)
                .build();
        assertThat(acidente.getTipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
    }
}
