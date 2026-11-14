package br.edu.safeplace.backend.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.edu.safeplace.backend.domain.capacitacao.exception.ColaboradorNaoAlocavelException;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;

class ApiExceptionHandlerTest {

    private static final List<Class<?>> ADVICES = List.of(
            ApiExceptionHandler.class, EmprestimoExceptionHandler.class, TarefaExceptionHandler.class);

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void cadaTipoDeExcecaoTemUmUnicoHandlerEntreOsAdvices() {
        Map<Class<?>, Long> handlersPorTipo = ADVICES.stream()
                .flatMap(advice -> Arrays.stream(advice.getDeclaredMethods()))
                .map(metodo -> metodo.getAnnotation(ExceptionHandler.class))
                .filter(anotacao -> anotacao != null)
                .flatMap(anotacao -> Stream.of(anotacao.value()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertThat(handlersPorTipo).isNotEmpty().allSatisfy((tipo, quantidade) -> assertThat(quantidade)
                .as("handlers de %s", tipo.getSimpleName())
                .isEqualTo(1L));
    }

    @Test
    void corpoPreservaOContratoDeErroDaApi() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.epiNaoEncontrado(new EpiNaoEncontradoException("EPI não encontrado"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).containsOnlyKeys("timestamp", "status", "error", "message")
                .containsEntry("status", 404)
                .containsEntry("error", "Não Encontrado")
                .containsEntry("message", "EPI não encontrado");
    }

    @Test
    void preservaORotuloComparadoPeloFrontend() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.colaboradorNaoAlocavel(new ColaboradorNaoAlocavelException("inativo"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resposta.getBody()).containsEntry("error", "Colaborador Não Alocável");
    }
}
