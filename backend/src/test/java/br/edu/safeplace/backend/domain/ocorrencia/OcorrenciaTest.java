package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

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
                List<String> testemunhas,
                Colaborador colaborador,
                List<String> midias,
                AreaRisco area,
                GestorDeSeguranca gestor,
                String local,
                PlanoDeAcao planoDeAcao) {
            super(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                    testemunhas, colaborador, midias, area, gestor, local, planoDeAcao);
        }
    }

    @Test
    @DisplayName("Deve instanciar ocorrência com todos os campos válidos")
    void deveInstanciarOcorrenciaComDadosValidos() {
        LocalDateTime dataFato = LocalDateTime.now().minusDays(1);
        LocalDateTime dataRegistro = LocalDateTime.now();
        AreaRisco area = AreaRisco.novo("Caldeiras", "Alta pressão", NivelPerigo.ALTO);
        Colaborador colaborador = Colaborador.novo("Carlos", "87455877074", LocalDate.of(1990, 1, 1), "carlos@empresa.com");
        GestorDeSeguranca gestor = GestorDeSeguranca.novo("Ana", "49216091040", LocalDate.of(1985, 5, 5), "ana@empresa.com", "senha123");

        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                1,
                "Vazamento de vapor detectado",
                dataFato,
                dataRegistro,
                StatusOcorrencia.ABERTA,
                List.of("Joao", "Pedro"),
                colaborador,
                List.of("foto1.png", "video1.mp4"),
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
        assertThat(ocorrencia.getTestemunhas()).containsExactly("Joao", "Pedro");
        assertThat(ocorrencia.getMidias()).containsExactly("foto1.png", "video1.mp4");
        assertThat(ocorrencia.getColaborador()).isEqualTo(colaborador);
        assertThat(ocorrencia.getGestor()).isEqualTo(gestor);
        assertThat(ocorrencia.getArea()).isEqualTo(area);
        assertThat(ocorrencia.getLocal()).isEqualTo("Setor B");
    }

    @Test
    @DisplayName("Deve rejeitar data do fato no futuro")
    void deveRejeitarDataDoFatoFutura() {
        LocalDateTime dataFutura = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> new OcorrenciaConcreta(
                null,
                "Descricao",
                dataFutura,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null
        )).isInstanceOf(DataOcorrenciaInvalidaException.class)
                .hasMessage("Data da ocorrência não pode ser futura.");
    }

    @Test
    @DisplayName("Deve rejeitar data da ocorrência nula")
    void deveRejeitarDataOcorrenciaNula() {
        assertThatThrownBy(() -> new OcorrenciaConcreta(
                null,
                "Descricao",
                null,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data da ocorrência é obrigatória.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Deve rejeitar descrição nula ou em branco")
    void deveRejeitarDescricaoInvalida(String descricaoInvalida) {
        LocalDateTime dataPassada = LocalDateTime.now().minusHours(2);

        assertThatThrownBy(() -> new OcorrenciaConcreta(
                null,
                descricaoInvalida,
                dataPassada,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                null, null, null, null, null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Descrição é obrigatória.");
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
                null, null, null, null, null, null, null
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
                null, null, null, null, null, null, null
        );

        assertThatThrownBy(ocorrencia::arquivar)
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Deve garantir imutabilidade de listas de testemunhas e mídias")
    void deveGarantirImutabilidadeDeListas() {
        List<String> testemunhas = new ArrayList<>();
        testemunhas.add("Lucas");

        List<String> midias = new ArrayList<>();
        midias.add("doc.pdf");

        Ocorrencia ocorrencia = new OcorrenciaConcreta(
                1,
                "Descricao",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                testemunhas, null, midias, null, null, null, null
        );

        testemunhas.add("Novo elemento");
        midias.add("nova_foto.png");

        assertThat(ocorrencia.getTestemunhas()).containsExactly("Lucas");
        assertThat(ocorrencia.getMidias()).containsExactly("doc.pdf");

        assertThatThrownBy(() -> ocorrencia.getTestemunhas().add("Outro"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
