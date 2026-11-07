package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FabricaRelatoIncidente — Abstract Factory de relatos de incidente (#239)")
class FabricaRelatoIncidenteTest {

    private final FabricaRelatoIncidente fabrica = new FabricaRelatoIncidente();

    private static final LocalDateTime FATO = LocalDateTime.of(2026, 11, 5, 15, 30);
    private static final Setor SETOR = new Setor(2, "Logística", null, "Galpão 2", NivelPerigo.MEDIO);
    private static final AreaRisco AREA = new AreaRisco(2, "ALM-01", "Almoxarifado", "Estocagem vertical",
            NivelPerigo.MEDIO, SETOR, List.of(), null, List.of());
    private static final Colaborador COLABORADOR = Colaborador.novo("Carlos", "87455877074",
            LocalDate.of(1990, 1, 1), "carlos@empresa.com");
    private static final GestorDeSeguranca RELATOR = GestorDeSeguranca.novo("Ana", "49216091040",
            LocalDate.of(1985, 5, 5), "ana@empresa.com", "senha123");

    @Test
    @DisplayName("Identifica o tipo como INCIDENTE")
    void tipoEhIncidente() {
        assertThat(fabrica.getTipo()).isEqualTo(TipoOcorrencia.INCIDENTE);
    }

    @Test
    @DisplayName("Cria Incidente completo a partir do InputDTO inteiro sem protocolo CAT")
    void criaIncidenteAPartirDoInputDTO() {
        RegistrarIncidenteInputDTO input = new RegistrarIncidenteInputDTO(
                FATO,
                "Palete instável quase desabou sobre corredor",
                "Paletes empilhados além da altura máxima permitida",
                "Esmagamento e fraturas múltiplas",
                2,
                1
        );

        Incidente incidente = fabrica.criarOcorrencia(input, AREA, COLABORADOR, RELATOR, null);

        assertThat(incidente.getId()).isNull();
        assertThat(incidente.getTipoOcorrencia()).isEqualTo(TipoOcorrencia.INCIDENTE);
        assertThat(incidente.getDescricao()).isEqualTo("Palete instável quase desabou sobre corredor");
        assertThat(incidente.getDataOcorrencia()).isEqualTo(FATO);
        assertThat(incidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(incidente.getArea()).isSameAs(AREA);
        assertThat(incidente.getColaborador()).isSameAs(COLABORADOR);
        assertThat(incidente.getRelator()).isSameAs(RELATOR);
        assertThat(incidente.getSituacaoRisco()).isEqualTo("Paletes empilhados além da altura máxima permitida");
        assertThat(incidente.getPotencialDano()).isEqualTo("Esmagamento e fraturas múltiplas");
        assertThat(incidente.getGravidade()).isNull();
        assertThat(incidente.isGrave()).isFalse();
    }

    @Test
    @DisplayName("Cria NotificacaoOcorrencia da família sem protocolo CAT")
    void criaNotificacaoDeIncidenteSemProtocolo() {
        Incidente incidente = Incidente.builder()
                .idOcorrencia(15)
                .descricao("Quase colisão")
                .dataOcorrencia(FATO)
                .dataRegistro(FATO)
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(COLABORADOR)
                .area(AREA)
                .relator(RELATOR)
                .situacaoRisco("Falta de sinalização")
                .potencialDano("Trauma leve")
                .build();

        NotificacaoOcorrencia notificacao = fabrica.criarNotificacao(incidente);

        assertThat(notificacao.ocorrenciaId()).isEqualTo(15);
        assertThat(notificacao.tipoOcorrencia()).isEqualTo(TipoOcorrencia.INCIDENTE);
        assertThat(notificacao.numeroProtocoloCAT()).isNull();
        assertThat(notificacao.areaNome()).isEqualTo("Almoxarifado");
        assertThat(notificacao.mensagem())
                .isEqualTo("Novo incidente registrado na área Almoxarifado. Aguarda triagem.");
    }

    @Test
    @DisplayName("Produtos de auditoria da família correspondem ao relato de Incidente")
    void produtosAuditoriaDeIncidente() {
        assertThat(fabrica.entidadeAuditoria()).isEqualTo("Incidente");
        assertThat(fabrica.descricaoAuditoria()).isEqualTo("Registro de Incidente");
    }

    @Test
    @DisplayName("Recusa entrada nula")
    void recusaInputNulo() {
        assertThatThrownBy(() -> fabrica.criarOcorrencia(null, AREA, COLABORADOR, RELATOR, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
