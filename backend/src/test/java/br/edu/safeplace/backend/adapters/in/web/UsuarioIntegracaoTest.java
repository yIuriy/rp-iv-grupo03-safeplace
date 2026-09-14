package br.edu.safeplace.backend.adapters.in.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.TokenPorta;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integração de ponta a ponta do módulo de usuários (issue #40 / RF23): HTTP real, filtro de
 * segurança ativo, casos de uso, adaptador JPA e PostgreSQL.
 *
 * <p>Cobre os itens exigidos no critério de aceite: criação de supervisor com senha inicial,
 * criação de colaborador sem credenciais e rejeição de duplicidade de CPF.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIf("bancoDisponivel")
class UsuarioIntegracaoTest {

    private static final String CPF_SUPERVISOR = "52998224725";
    private static final String CPF_COLABORADOR = "11144477735";

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

    @Autowired
    private CodificadorSenhaPorta codificadorSenhaPorta;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper json = new ObjectMapper();

    /**
     * Cada teste começa sem as pessoas que ele mesmo cria. O teste roda sem transação porque
     * exercita HTTP real, então a limpeza é explícita e restrita aos CPFs usados aqui.
     */
    @BeforeEach
    void limparUsuariosDoTeste() {
        jdbcTemplate.update("DELETE FROM usuarios WHERE cpf IN (?, ?)", CPF_SUPERVISOR, CPF_COLABORADOR);
    }

    private HttpResponse<String> chamar(String metodo, String caminho, String corpo, Perfil perfil) {
        HttpRequest.BodyPublisher publisher = corpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(corpo, StandardCharsets.UTF_8);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, publisher);

        if (perfil != null) {
            builder.header("Authorization", "Bearer "
                    + tokenPorta.gerarToken(perfil.name().toLowerCase() + "@safeplace.test", perfil.name()));
        }

        try {
            return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException erro) {
            throw new IllegalStateException("Falha ao chamar " + caminho, erro);
        } catch (InterruptedException erro) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Chamada interrompida para " + caminho, erro);
        }
    }

    private JsonNode corpoDe(HttpResponse<String> resposta) {
        try {
            return json.readTree(resposta.body());
        } catch (Exception erro) {
            throw new IllegalStateException("Resposta não é JSON: " + resposta.body(), erro);
        }
    }

    private static String jsonSupervisor(String cpf) {
        return """
                {
                    "nome": "Joana Ribeiro",
                    "cpf": "%s",
                    "dataNascimento": "1988-04-20",
                    "email": "joana.ribeiro@safeplace.test"
                }
                """.formatted(cpf);
    }

    private static String jsonColaborador(String cpf, String nome) {
        return """
                {
                    "nome": "%s",
                    "cpf": "%s",
                    "dataNascimento": "1992-06-18"
                }
                """.formatted(nome, cpf);
    }

    @Test
    @DisplayName("RF23: Gestor cria Supervisor; o banco guarda o hash e nunca a senha em texto puro")
    void gestorCriaSupervisorComSenhaInicialEHashNoBanco() {
        HttpResponse<String> resposta = chamar("POST", "/api/usuarios/supervisores",
                jsonSupervisor(CPF_SUPERVISOR), Perfil.GESTOR_SEGURANCA);

        assertThat(resposta.statusCode()).isEqualTo(201);

        JsonNode corpo = corpoDe(resposta);
        assertThat(corpo.get("perfil").asText()).isEqualTo("SUPERVISOR");
        assertThat(corpo.has("senha")).isFalse();

        String senhaInicial = corpo.get("senhaInicial").asText();
        assertThat(senhaInicial).isNotBlank();

        String hashPersistido = jdbcTemplate.queryForObject(
                "SELECT senha FROM usuarios WHERE cpf = ?", String.class, CPF_SUPERVISOR);
        assertThat(hashPersistido).isNotBlank().isNotEqualTo(senhaInicial);
        assertThat(codificadorSenhaPorta.validar(senhaInicial, hashPersistido)).isTrue();

        String perfilPersistido = jdbcTemplate.queryForObject(
                "SELECT perfil FROM usuarios WHERE cpf = ?", String.class, CPF_SUPERVISOR);
        assertThat(perfilPersistido).isEqualTo("SUPERVISOR");
    }

    @Test
    @DisplayName("RF23: Supervisor cria Colaborador e o banco não guarda nenhuma credencial")
    void supervisorCriaColaboradorSemCredenciais() {
        HttpResponse<String> resposta = chamar("POST", "/api/usuarios/colaboradores",
                jsonColaborador(CPF_COLABORADOR, "João da Silva"), Perfil.SUPERVISOR);

        assertThat(resposta.statusCode()).isEqualTo(201);

        JsonNode corpo = corpoDe(resposta);
        assertThat(corpo.get("perfil").asText()).isEqualTo("COLABORADOR");
        assertThat(corpo.has("senhaInicial")).isFalse();

        String senhaPersistida = jdbcTemplate.queryForObject(
                "SELECT senha FROM usuarios WHERE cpf = ?", String.class, CPF_COLABORADOR);
        assertThat(senhaPersistida).isNull();
    }

    @Test
    @DisplayName("RF23: duplicidade de CPF é rejeitada com 409, mesmo com máscara diferente")
    void duplicidadeDeCpfEhRejeitada() {
        assertThat(chamar("POST", "/api/usuarios/colaboradores",
                jsonColaborador(CPF_COLABORADOR, "João da Silva"), Perfil.SUPERVISOR).statusCode())
                .isEqualTo(201);

        HttpResponse<String> repetido = chamar("POST", "/api/usuarios/colaboradores",
                jsonColaborador("111.444.777-35", "Outro Nome"), Perfil.SUPERVISOR);

        assertThat(repetido.statusCode()).isEqualTo(409);

        Integer total = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM usuarios WHERE cpf = ?", Integer.class, CPF_COLABORADOR);
        assertThat(total).isEqualTo(1);
    }

    @Test
    @DisplayName("RF23: Supervisor não consegue provisionar outro Supervisor")
    void supervisorNaoProvisionaSupervisor() {
        HttpResponse<String> resposta = chamar("POST", "/api/usuarios/supervisores",
                jsonSupervisor(CPF_SUPERVISOR), Perfil.SUPERVISOR);

        assertThat(resposta.statusCode()).isEqualTo(403);

        Integer total = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM usuarios WHERE cpf = ?", Integer.class, CPF_SUPERVISOR);
        assertThat(total).isZero();
    }

    @Test
    @DisplayName("A listagem de colaboradores filtra por nome e CPF e não traz supervisores")
    void listagemDeColaboradoresFiltraENaoTrazSupervisores() {
        chamar("POST", "/api/usuarios/colaboradores",
                jsonColaborador(CPF_COLABORADOR, "João da Silva"), Perfil.SUPERVISOR);
        chamar("POST", "/api/usuarios/supervisores",
                jsonSupervisor(CPF_SUPERVISOR), Perfil.GESTOR_SEGURANCA);

        JsonNode porNome = corpoDe(chamar("GET",
                "/api/usuarios/colaboradores?nome=" + codificar("silva"), null, Perfil.SUPERVISOR));
        assertThat(porNome).hasSize(1);
        assertThat(porNome.get(0).get("nome").asText()).isEqualTo("João da Silva");
        assertThat(porNome.get(0).get("perfil").asText()).isEqualTo("COLABORADOR");

        JsonNode porCpfComMascara = corpoDe(chamar("GET",
                "/api/usuarios/colaboradores?cpf=" + codificar("111.444.777-35"), null, Perfil.SUPERVISOR));
        assertThat(porCpfComMascara).hasSize(1);

        JsonNode buscandoSupervisor = corpoDe(chamar("GET",
                "/api/usuarios/colaboradores?nome=" + codificar("Joana"), null, Perfil.SUPERVISOR));
        assertThat(buscandoSupervisor).isEmpty();
    }

    @Test
    @DisplayName("Nenhuma resposta do módulo de usuários expõe senha ou hash")
    void nenhumaRespostaExpoeCredenciais() {
        JsonNode criado = corpoDe(chamar("POST", "/api/usuarios/colaboradores",
                jsonColaborador(CPF_COLABORADOR, "João da Silva"), Perfil.SUPERVISOR));
        int id = criado.get("id").asInt();

        String detalhe = chamar("GET", "/api/usuarios/" + id, null, Perfil.SUPERVISOR).body();
        String listagem = chamar("GET", "/api/usuarios", null, Perfil.GESTOR_SEGURANCA).body();
        String colaboradores = chamar("GET", "/api/usuarios/colaboradores", null, Perfil.SUPERVISOR).body();

        assertThat(detalhe).doesNotContain("senha").doesNotContain("$2a$");
        assertThat(listagem).doesNotContain("senhaInicial").doesNotContain("$2a$");
        assertThat(colaboradores).doesNotContain("senhaInicial").doesNotContain("$2a$");
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
