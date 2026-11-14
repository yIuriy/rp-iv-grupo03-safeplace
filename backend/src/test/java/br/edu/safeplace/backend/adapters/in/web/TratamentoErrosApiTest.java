/*
 * Tratamento de erros da API pela pilha real do Spring MVC (#249): exceção sem mensagem, erro inesperado e as
 * exceções que o próprio Spring traduz. Um controlador de teste lança cada caso; os advices de produção
 * (ApiExceptionHandler e ErroInesperadoExceptionHandler) respondem na ordem definida em OrdemAdvices.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;

class TratamentoErrosApiTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDeTeste())
                .setControllerAdvice(new ErroInesperadoExceptionHandler(), new ApiExceptionHandler())
                .build();
    }

    // Exceção sem mensagem: antes do #234, Map.of(..., "message", null) lançava NPE e a resposta virava 500.

    @Test
    @DisplayName("IllegalArgumentException sem mensagem responde 400 com message nula, não 500")
    void argumentoSemMensagem() throws Exception {
        mockMvc.perform(get("/teste/argumento-sem-mensagem"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Requisição Inválida"))
                .andExpect(jsonPath("$", hasKey("message")))
                .andExpect(jsonPath("$.message").value(nullValue()));
    }

    @Test
    @DisplayName("exceções de domínio sem mensagem mantêm o status do handler (404 e 403)")
    void dominioSemMensagem() throws Exception {
        mockMvc.perform(get("/teste/epi-sem-mensagem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"))
                .andExpect(jsonPath("$.message").value(nullValue()));
        mockMvc.perform(get("/teste/acesso-sem-mensagem"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acesso Proibido"))
                .andExpect(jsonPath("$.message").value(nullValue()));
    }

    // Exception genérica

    @Test
    @DisplayName("erro inesperado responde 500 no contrato da API, sem expor o texto da exceção")
    void erroInesperado() throws Exception {
        mockMvc.perform(get("/teste/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value(ErroInesperadoExceptionHandler.ERRO))
                .andExpect(jsonPath("$.message").value(ErroInesperadoExceptionHandler.MENSAGEM))
                .andExpect(content().string(not(containsString("SELECT"))));
    }

    @Test
    @DisplayName("NullPointerException sem mensagem também responde 500 no contrato da API")
    void nullPointerSemMensagem() throws Exception {
        mockMvc.perform(get("/teste/npe"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(ErroInesperadoExceptionHandler.ERRO));
    }

    @Test
    @DisplayName("IllegalArgumentException do construtor durante a leitura do JSON continua 400 Requisição Inválida")
    void argumentoInvalidoNaLeituraDoCorpo() throws Exception {
        mockMvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Requisição Inválida"))
                .andExpect(jsonPath("$.message").value("Nome é obrigatório."));
    }

    // Exceções que o Spring já traduz: o advice de erro inesperado devolve ao Spring, e o status não muda.

    @Test
    @DisplayName("JSON malformado continua 400")
    void jsonMalformado() throws Exception {
        mockMvc.perform(post("/teste/corpo").contentType(MediaType.APPLICATION_JSON).content("{nome"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(ErroInesperadoExceptionHandler.ERRO))));
    }

    @Test
    @DisplayName("valor de enum inexistente no corpo continua 400")
    void enumInexistente() throws Exception {
        mockMvc.perform(post("/teste/nivel").contentType(MediaType.APPLICATION_JSON).content("{\"nivel\": \"EXTREMO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString(ErroInesperadoExceptionHandler.ERRO))));
    }

    @Test
    @DisplayName("rota inexistente continua 404")
    void rotaInexistente() throws Exception {
        mockMvc.perform(get("/teste/rota-que-nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString(ErroInesperadoExceptionHandler.ERRO))));
    }

    @Test
    @DisplayName("parâmetro de tipo errado e parâmetro obrigatório ausente continuam 400")
    void parametroInvalido() throws Exception {
        mockMvc.perform(get("/teste/numero").param("valor", "abc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/teste/numero"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("método HTTP não suportado continua 405")
    void metodoNaoSuportado() throws Exception {
        mockMvc.perform(post("/teste/numero").param("valor", "1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("ResponseStatusException mantém o próprio status")
    void responseStatusException() throws Exception {
        mockMvc.perform(get("/teste/status"))
                .andExpect(status().isIAmATeapot());
    }

    record Entrada(String nome) {
        Entrada {
            if (nome == null || nome.isBlank()) {
                throw new IllegalArgumentException("Nome é obrigatório.");
            }
        }
    }

    enum Nivel { BAIXO, ALTO }

    record EntradaComNivel(Nivel nivel) {
    }

    @RestController
    static class ControladorDeTeste {

        @PostMapping("/teste/nivel")
        String nivel(@RequestBody EntradaComNivel entrada) {
            return entrada.nivel().name();
        }

        @GetMapping("/teste/argumento-sem-mensagem")
        String argumentoSemMensagem() {
            throw new IllegalArgumentException();
        }

        @GetMapping("/teste/epi-sem-mensagem")
        String epiSemMensagem() {
            throw new EpiNaoEncontradoException((String) null);
        }

        @GetMapping("/teste/acesso-sem-mensagem")
        String acessoSemMensagem() {
            throw new AcessoNegadoException(null);
        }

        @GetMapping("/teste/inesperado")
        String inesperado() {
            throw new RuntimeException("falha interna em SELECT * FROM usuarios");
        }

        @GetMapping("/teste/npe")
        String npe() {
            throw new NullPointerException();
        }

        @PostMapping("/teste/corpo")
        String corpo(@RequestBody Entrada entrada) {
            return entrada.nome();
        }

        @GetMapping("/teste/numero")
        int numero(@RequestParam int valor) {
            return valor;
        }

        @GetMapping("/teste/status")
        String comStatus() {
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT);
        }
    }
}
