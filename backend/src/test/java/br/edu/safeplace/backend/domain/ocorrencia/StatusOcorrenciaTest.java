package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusOcorrenciaTest {

    @Test
    @DisplayName("Deve permitir transição de ABERTA para EM_TRIAGEM")
    void devePermitirTransicaoDeAbertaParaEmTriagem() {
        assertThat(StatusOcorrencia.ABERTA.podeTransicaoPara(StatusOcorrencia.EM_TRIAGEM)).isTrue();
        assertThatCode(() -> StatusOcorrencia.ABERTA.validarTransicao(StatusOcorrencia.EM_TRIAGEM))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve permitir transição de EM_TRIAGEM para ARQUIVADA")
    void devePermitirTransicaoDeEmTriagemParaArquivada() {
        assertThat(StatusOcorrencia.EM_TRIAGEM.podeTransicaoPara(StatusOcorrencia.ARQUIVADA)).isTrue();
        assertThatCode(() -> StatusOcorrencia.EM_TRIAGEM.validarTransicao(StatusOcorrencia.ARQUIVADA))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Não deve permitir transição direta de ABERTA para ARQUIVADA")
    void naoDevePermitirTransicaoDeAbertaParaArquivada() {
        assertThat(StatusOcorrencia.ABERTA.podeTransicaoPara(StatusOcorrencia.ARQUIVADA)).isFalse();
        assertThatThrownBy(() -> StatusOcorrencia.ABERTA.validarTransicao(StatusOcorrencia.ARQUIVADA))
                .isInstanceOf(TransicaoStatusInvalidaException.class)
                .hasMessageContaining("Transição de status inválida de ABERTA para ARQUIVADA");
    }

    @Test
    @DisplayName("Não deve permitir transição a partir de ARQUIVADA")
    void naoDevePermitirTransicaoPartindoDeArquivada() {
        assertThat(StatusOcorrencia.ARQUIVADA.podeTransicaoPara(StatusOcorrencia.ABERTA)).isFalse();
        assertThat(StatusOcorrencia.ARQUIVADA.podeTransicaoPara(StatusOcorrencia.EM_TRIAGEM)).isFalse();

        assertThatThrownBy(() -> StatusOcorrencia.ARQUIVADA.validarTransicao(StatusOcorrencia.ABERTA))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Não deve permitir transição regressiva de EM_TRIAGEM para ABERTA")
    void naoDevePermitirTransicaoRegressivaDeEmTriagemParaAberta() {
        assertThat(StatusOcorrencia.EM_TRIAGEM.podeTransicaoPara(StatusOcorrencia.ABERTA)).isFalse();
        assertThatThrownBy(() -> StatusOcorrencia.EM_TRIAGEM.validarTransicao(StatusOcorrencia.ABERTA))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar transição para status nulo")
    void deveLancarExcecaoParaStatusNulo() {
        assertThatThrownBy(() -> StatusOcorrencia.ABERTA.validarTransicao(null))
                .isInstanceOf(TransicaoStatusInvalidaException.class);
    }
}
