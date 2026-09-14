package br.edu.safeplace.backend.config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.port.out.TokenPorta;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Protege o tratamento de erro HTTP com a aplicação rodando de verdade, com o filtro de segurança
 * ativo e o forward interno para {@code /error}.
 *
 * <p>Regressão que este teste cobre: sem liberar o dispatch {@code ERROR} no
 * {@link SecurityConfig}, o filtro reavaliava o forward como requisição anônima e transformava
 * 400 e 404 legítimos em 401, em todos os módulos da API.</p>
 *
 * <p>Usa um servidor real em porta aleatória porque o MockMvc não reproduz o forward de erro do
 * contêiner, que é justamente o ponto em teste.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIf("bancoDisponivel")
class TratamentoDeErroHttpTest {

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @LocalServerPort
    private int porta;

    @Autowired
    private TokenPorta tokenPorta;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private int status(String metodo, String caminho, String corpo, Perfil perfil) {
        HttpRequest.BodyPublisher publisher = corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, publisher);

        if (perfil != null) {
            builder.header("Authorization", "Bearer "
                    + tokenPorta.gerarToken(perfil.name().toLowerCase() + "@safeplace.test", perfil.name()));
        }

        try {
            return http.send(builder.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (IOException erro) {
            throw new IllegalStateException("Falha ao chamar " + caminho, erro);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Chamada interrompida para " + caminho, erro);
        }
    }

    @Test
    @DisplayName("Corpo JSON malformado deve responder 400, e não 401")
    void jsonMalformadoDeveResponder400() {
        assertThat(status("POST", "/api/epis", "{ isso nao e json }", Perfil.SUPERVISOR))
                .isEqualTo(400);
    }

    @Test
    @DisplayName("Valor de enum inexistente deve responder 400, e não 401")
    void enumInvalidoDeveResponder400() {
        String corpo = """
                {
                    "nome": "Capacete",
                    "numeroCa": "12345",
                    "quantidade": 10,
                    "estoqueMinimo": 2,
                    "classificacao": "PROTECAO_INEXISTENTE"
                }
                """;

        assertThat(status("POST", "/api/epis", corpo, Perfil.SUPERVISOR)).isEqualTo(400);
    }

    @Test
    @DisplayName("Violação de Bean Validation deve responder 400, e não 401")
    void validacaoVioladaDeveResponder400() {
        assertThat(status("POST", "/api/epis", "{}", Perfil.SUPERVISOR)).isEqualTo(400);
    }

    @Test
    @DisplayName("Rota inexistente com token válido deve responder 404, e não 401")
    void rotaInexistenteDeveResponder404() {
        assertThat(status("GET", "/api/rota-que-nao-existe", null, Perfil.GESTOR_SEGURANCA))
                .isEqualTo(404);
    }

    @Test
    @DisplayName("A liberação do dispatch de erro não pode afrouxar a autenticação")
    void semTokenContinua401() {
        assertThat(status("GET", "/api/epis", null, null)).isEqualTo(401);
        assertThat(status("GET", "/api/usuarios", null, null)).isEqualTo(401);
        assertThat(status("GET", "/error", null, null)).isEqualTo(401);
    }

    @Test
    @DisplayName("A liberação do dispatch de erro não pode afrouxar o RBAC")
    void rbacContinuaValendo() {
        String novoSupervisor = """
                {
                    "nome": "Novo Supervisor",
                    "cpf": "52998224725",
                    "dataNascimento": "1988-04-12",
                    "email": "novo.supervisor@safeplace.test",
                    "perfil": "SUPERVISOR"
                }
                """;

        // RF23: apenas o Gestor de Segurança provisiona perfis com acesso.
        assertThat(status("POST", "/api/usuarios", novoSupervisor, Perfil.SUPERVISOR)).isEqualTo(403);
        assertThat(status("GET", "/api/usuarios", null, Perfil.SUPERVISOR)).isEqualTo(200);
    }

    @Test
    @DisplayName("Rota pública continua acessível sem token")
    void rotaPublicaContinuaAcessivel() {
        assertThat(status("GET", "/api/health", null, null)).isEqualTo(200);
    }
}
