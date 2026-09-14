package br.edu.safeplace.backend.suporte;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Resolve o PostgreSQL usado pelos testes de integração, em duas modalidades:
 *
 * <ol>
 *   <li>um banco externo já em execução, informado por {@code -Dsafeplace.test.datasource.url}
 *       (ou pela variável de ambiente {@code SAFEPLACE_TEST_DATASOURCE_URL});</li>
 *   <li>um container PostgreSQL gerenciado por Testcontainers, quando há Docker na máquina.</li>
 * </ol>
 *
 * <p>A primeira modalidade existe para que a suíte também rode em máquinas sem Docker, que é o
 * caso de parte do ambiente de desenvolvimento do grupo. Quando nenhuma das duas está disponível,
 * {@link #disponivel()} devolve {@code false} e os testes que dependem de banco são ignorados em
 * vez de falhar.</p>
 */
public final class PostgresDeTeste {

    private static final String PROPRIEDADE_URL = "safeplace.test.datasource.url";
    private static final String PROPRIEDADE_USUARIO = "safeplace.test.datasource.username";
    private static final String PROPRIEDADE_SENHA = "safeplace.test.datasource.password";

    private static final String IMAGEM = "postgres:16";

    private static PostgreSQLContainer<?> container;

    private PostgresDeTeste() {
    }

    public static boolean disponivel() {
        return urlExterna() != null || dockerDisponivel();
    }

    /**
     * Registra as propriedades de datasource do contexto de teste.
     */
    public static synchronized void registrar(DynamicPropertyRegistry registry) {
        String urlExterna = urlExterna();

        if (urlExterna != null) {
            registry.add("spring.datasource.url", () -> urlExterna);
            registry.add("spring.datasource.username", () -> valor(PROPRIEDADE_USUARIO, "safeplace"));
            registry.add("spring.datasource.password", () -> valor(PROPRIEDADE_SENHA, "safeplace"));
            return;
        }

        PostgreSQLContainer<?> postgres = container();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static PostgreSQLContainer<?> container() {
        if (container == null) {
            container = new PostgreSQLContainer<>(IMAGEM);
            container.start();
        }
        return container;
    }

    private static String urlExterna() {
        String url = valor(PROPRIEDADE_URL, null);
        return url != null && !url.isBlank() ? url : null;
    }

    private static String valor(String propriedade, String padrao) {
        String doSistema = System.getProperty(propriedade);
        if (doSistema != null && !doSistema.isBlank()) {
            return doSistema;
        }
        String doAmbiente = System.getenv(propriedade.toUpperCase().replace('.', '_'));
        return doAmbiente != null && !doAmbiente.isBlank() ? doAmbiente : padrao;
    }

    private static boolean dockerDisponivel() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable erro) {
            return false;
        }
    }
}
