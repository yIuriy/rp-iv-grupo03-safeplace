package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FabricaRelatoAcidente — Abstract Factory de relatos de acidente (#239)")
class FabricaRelatoAcidenteTest {

    private final FabricaRelatoAcidente fabrica = new FabricaRelatoAcidente();

    private static final LocalDateTime FATO = LocalDateTime.of(2026, 11, 5, 14, 0);
    private static final Setor SETOR = new Setor(1, "Produção", null, "Bloco A", NivelPerigo.ALTO);
    private static final AreaRisco AREA = new AreaRisco(1, "PRD-01", "Prensa", "Prensa hidráulica",
            NivelPerigo.ALTO, SETOR, List.of(), null, List.of());
    private static final Colaborador COLABORADOR = Colaborador.novo("João", "87455877074",
            LocalDate.of(1992, 4, 10), "joao@empresa.com");
    private static final GestorDeSeguranca RELATOR = GestorDeSeguranca.novo("Maria", "49216091040",
            LocalDate.of(1985, 8, 20), "maria@empresa.com", "senha123");

    @Test
    @DisplayName("Identifica o tipo como ACIDENTE")
    void tipoEhAcidente() {
        assertThat(fabrica.getTipo()).isEqualTo(TipoOcorrencia.ACIDENTE);
    }

    @Test
    @DisplayName("Cria Acidente completo a partir do InputDTO inteiro com protocolo CAT")
    void criaAcidenteAPartirDoInputDTO() {
        RegistrarAcidenteInputDTO input = new RegistrarAcidenteInputDTO(
                FATO,
                "Esmagamento de dedo na prensa",
                "FATOR_HUMANO",
                "ALTO",
                "Típico",
                "Fratura exposta",
                "Pronto-Socorro",
                1,
                2
        );
        NumeroProtocoloCAT protocolo = NumeroProtocoloCAT.de("CAT-2026-11-0001");

        Acidente acidente = fabrica.criarOcorrencia(input, AREA, COLABORADOR, RELATOR, protocolo);

        assertThat(acidente.getId()).isNull();
        assertThat(acidente.getTipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
        assertThat(acidente.getDescricao()).isEqualTo("Esmagamento de dedo na prensa");
        assertThat(acidente.getDataOcorrencia()).isEqualTo(FATO);
        assertThat(acidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(acidente.getArea()).isSameAs(AREA);
        assertThat(acidente.getColaborador()).isSameAs(COLABORADOR);
        assertThat(acidente.getRelator()).isSameAs(RELATOR);
        assertThat(acidente.getProtocoloCAT()).isSameAs(protocolo);
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-11-0001");
        assertThat(acidente.getCausaRaiz()).isEqualTo(CausaRaizAcidente.FATOR_HUMANO);
        assertThat(acidente.getGravidade()).isEqualTo(NivelPerigo.ALTO);
        assertThat(acidente.isGrave()).isTrue();
    }

    @Test
    @DisplayName("Cria NotificacaoOcorrencia da família contendo dados do acidente e protocolo CAT")
    void criaNotificacaoDeAcidenteComProtocolo() {
        Acidente acidente = new Acidente(
                10, "Queda", FATO, FATO, StatusOcorrencia.ABERTA,
                COLABORADOR, AREA, RELATOR, null, null,
                CausaRaizAcidente.FALHA_EPI, "Queda", "Escoriação",
                NumeroProtocoloCAT.de("CAT-2026-11-0010"), "Ambulatório", NivelPerigo.MEDIO
        );

        NotificacaoOcorrencia notificacao = fabrica.criarNotificacao(acidente);

        assertThat(notificacao.ocorrenciaId()).isEqualTo(10);
        assertThat(notificacao.tipoOcorrencia()).isEqualTo(TipoOcorrencia.ACIDENTE);
        assertThat(notificacao.numeroProtocoloCAT()).isEqualTo(NumeroProtocoloCAT.de("CAT-2026-11-0010"));
        assertThat(notificacao.areaNome()).isEqualTo("Prensa");
        assertThat(notificacao.mensagem())
                .isEqualTo("Novo acidente registrado (protocolo CAT-2026-11-0010) na área Prensa. Aguarda triagem.");
    }

    @Test
    @DisplayName("Produtos de auditoria da família correspondem ao relato de Acidente")
    void produtosAuditoriaDeAcidente() {
        assertThat(fabrica.entidadeAuditoria()).isEqualTo("Acidente");
        assertThat(fabrica.descricaoAuditoria()).isEqualTo("Registro de Acidente de Trabalho");
    }

    @Test
    @DisplayName("Recusa entrada nula")
    void recusaInputNulo() {
        assertThatThrownBy(() -> fabrica.criarOcorrencia(null, AREA, COLABORADOR, RELATOR, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
