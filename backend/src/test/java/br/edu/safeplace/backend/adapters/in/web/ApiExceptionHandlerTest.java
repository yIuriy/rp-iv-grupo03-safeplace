package br.edu.safeplace.backend.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.capacitacao.exception.AlocacaoBloqueadaException;
import br.edu.safeplace.backend.domain.capacitacao.exception.ColaboradorNaoAlocavelException;
import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;

class ApiExceptionHandlerTest {

    private static final List<Class<?>> ADVICES = List.of(
            ApiExceptionHandler.class, EmprestimoExceptionHandler.class, TarefaExceptionHandler.class,
            ErroInesperadoExceptionHandler.class);

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
    void corpoAceitaMensagemNula() {
        ResponseEntity<Map<String, Object>> resposta = handler.requisicaoInvalida(new IllegalArgumentException());

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).containsOnlyKeys("timestamp", "status", "error", "message")
                .containsEntry("message", null);
    }

    @Test
    void advicesComEscopoVemAntesDoGlobalEOErroInesperadoPorUltimo() {
        int global = OrderUtils.getOrder(ApiExceptionHandler.class);

        assertThat(OrderUtils.getOrder(EmprestimoExceptionHandler.class)).isLessThan(global);
        assertThat(OrderUtils.getOrder(TarefaExceptionHandler.class)).isLessThan(global);
        assertThat(OrderUtils.getOrder(ErroInesperadoExceptionHandler.class))
                .isGreaterThan(global)
                .isEqualTo(Ordered.LOWEST_PRECEDENCE);
    }

    @Test
    void advicesComEscopoTratamSoOsCorposComCamposExtras() {
        assertThat(handlersDe(EmprestimoExceptionHandler.class)).containsExactly(DuplicidadeDePosseException.class);
        assertThat(handlersDe(TarefaExceptionHandler.class)).containsExactly(AlocacaoBloqueadaException.class);
    }

    @Test
    void escopoDosAdvicesComCamposExtras() {
        assertThat(EmprestimoExceptionHandler.class.getAnnotation(RestControllerAdvice.class).assignableTypes())
                .containsExactly(EmprestimoController.class);
        assertThat(TarefaExceptionHandler.class.getAnnotation(RestControllerAdvice.class).assignableTypes())
                .containsExactly(TarefaController.class);
    }

    @Test
    void campoExtraEntraNoCorpoSemMudarOContrato() {
        ResponseEntity<Map<String, Object>> duplicidade =
                new EmprestimoExceptionHandler().duplicidade(new DuplicidadeDePosseException(4, "luva"));
        ResponseEntity<Map<String, Object>> alocacao =
                new TarefaExceptionHandler().alocacaoBloqueada(new AlocacaoBloqueadaException(List.of("NR-35 ausente")));

        assertThat(duplicidade.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicidade.getBody()).containsOnlyKeys("timestamp", "status", "error", "message", "emprestimoAtivoId")
                .containsEntry("emprestimoAtivoId", 4);
        assertThat(alocacao.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(alocacao.getBody()).containsOnlyKeys("timestamp", "status", "error", "message", "impedimentos")
                .containsEntry("impedimentos", List.of("NR-35 ausente"));
    }

    @Test
    void erroInesperadoDevolveAoSpringAsExcecoesDeSeguranca() {
        ErroInesperadoExceptionHandler inesperado = new ErroInesperadoExceptionHandler();
        AccessDeniedException negado = new AccessDeniedException("negado");
        BadCredentialsException naoAutenticado = new BadCredentialsException("token");

        assertThatThrownBy(() -> inesperado.erroInesperado(negado)).isSameAs(negado);
        assertThatThrownBy(() -> inesperado.erroInesperado(naoAutenticado)).isSameAs(naoAutenticado);
    }

    private static List<Class<? extends Throwable>> handlersDe(Class<?> advice) {
        return Arrays.stream(advice.getDeclaredMethods())
                .map(metodo -> metodo.getAnnotation(ExceptionHandler.class))
                .filter(anotacao -> anotacao != null)
                .flatMap(anotacao -> Stream.of(anotacao.value()))
                .toList();
    }

    @Test
    void preservaORotuloComparadoPeloFrontend() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.colaboradorNaoAlocavel(new ColaboradorNaoAlocavelException("inativo"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resposta.getBody()).containsEntry("error", "Colaborador Não Alocável");
    }
}
