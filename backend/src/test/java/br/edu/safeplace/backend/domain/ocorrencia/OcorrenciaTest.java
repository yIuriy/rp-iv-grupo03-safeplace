package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcorrenciaTest {

    private static class OcorrenciaConcreta extends Ocorrencia {
        public OcorrenciaConcreta(
                Integer idOcorrencia,
                String descricao,
                LocalDateTime dataOcorrencia,
                LocalDateTime dataRegistro,
                StatusOcorrencia statusOcorrencia,
                Colaborador colaborador,
                AreaRisco area,
                Colaborador relator,
                String local,
                PlanoDeAcao planoDeAcao) {
            super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                    colaborador, area, relator, local, planoDeAcao);
        }
    }

    @Test
    @DisplayName("Deve instanciar ocorrência com todos os campos válidos")
    void deveInstanciarOcorrenciaComDadosValidos() {
        LocalDateTime dataFato = LocalDateTime.now().minusDays(1);
        LocalDateTime dataRegistro = LocalDateTime.now();
        Epi epi = new Epi(1, "Óculos", "CA-123", 10, 2, StatusEpi.DISPONIVEL, LocalDate.now().plusYears(1), 180, "Óculos", null);
        AreaRisco area = AreaRisco.cadastrar("CALD-01", "Caldeiras", "Alta pressão", NivelPerigo.ALTO, List.of(epi));
        Colaborador colaborador = Colaborador.novo("Carlos", "87455877074", LocalDate.of(1990, 1, 1), "carlos@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Ana", "49216091040", LocalDate.of(1985, 5, 5), "ana@empresa.com", "senha123");

        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                1,
                "Vazamento de vapor detectado",
                dataFato,
                dataRegistro,
                StatusOcorrencia.ABERTA,
                colaborador,
                area,
                gestor,
                "Setor B",
                null
        );

        assertThat(ocorrencia.getId()).isEqualTo(1);
        assertThat(ocorrencia.getDescricao()).isEqualTo("Vazamento de vapor detectado");
        assertThat(ocorrencia.getDataOcorrencia()).isEqualTo(dataFato);
        assertThat(ocorrencia.getDataRegistro()).isEqualTo(dataRegistro);
        assertThat(ocorrencia.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);
        assertThat(ocorrencia.getColaborador()).isEqualTo(colaborador);
        assertThat(ocorrencia.getRelator()).isEqualTo(gestor);
        assertThat(ocorrencia.getArea()).isEqualTo(area);
        assertThat(ocorrencia.getLocal()).isEqualTo("Caldeiras");
    }

    @Test
    @DisplayName("Deve transicionar status de ABERTA para EM_TRIAGEM e depois para ARQUIVADA")
    void deveTransicionarStatusComSucesso() {
        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                1,
                "Descricao",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null
        );

        assertThat(ocorrencia.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ABERTA);

        ocorrencia.enviarParaTriagem();
        assertThat(ocorrencia.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM);

        ocorrencia.arquivar();
        assertThat(ocorrencia.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.ARQUIVADA);
    }

    @Test
    @DisplayName("Deve rejeitar transição inválida de ABERTA direto para ARQUIVADA")
    void deveRejeitarTransicaoInvalidaDeAbertaParaArquivada() {
        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                1,
                "Descricao",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null
        );

        assertThatThrownBy(ocorrencia::arquivar)
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Supervisor que registra o relato fica como relator (issue #195)")
    void supervisorDeveFicarComoRelator() {
        Supervisor supervisor = Supervisor.novo("Bruno", "52998224725", LocalDate.of(1988, 3, 3),
                "bruno@empresa.com", "senha123");

        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                1,
                "Descricao",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, supervisor, null, null
        );

        assertThat(ocorrencia.getRelator()).isSameAs(supervisor);
    }

    @Test
    @DisplayName("Texto livre de local não vira área de risco fictícia")
    void naoDeveDerivarAreaFicticiaDoTextoLocal() {
        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                7,
                "Tropeço em cabo solto no pátio",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null,
                null,
                null,
                "Pátio externo",
                null
        );

        assertThat(ocorrencia.getArea()).isNull();
        assertThat(ocorrencia.getLocal()).isEqualTo("Pátio externo");
        assertThat(ocorrencia.getLocalInformado()).isEqualTo("Pátio externo");
    }

    @Test
    @DisplayName("Área de risco associada prevalece sobre o texto livre de local")
    void areaAssociadaPrevaleceSobreTextoLivre() {
        Epi epi = new Epi(1, "Óculos", "CA-123", 10, 2, StatusEpi.DISPONIVEL,
                LocalDate.now().plusYears(1), 180, "Óculos", null);
        AreaRisco area = AreaRisco.cadastrar("CALD-01", "Caldeiras", "Alta pressão", NivelPerigo.ALTO, List.of(epi));

        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                8,
                "Vazamento de vapor",
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null,
                area,
                null,
                "Anotação antiga do relato",
                null
        );

        assertThat(ocorrencia.getArea()).isSameAs(area);
        assertThat(ocorrencia.getLocal()).isEqualTo("Caldeiras");
        assertThat(ocorrencia.getLocalInformado()).isEqualTo("Anotação antiga do relato");
    }
}
