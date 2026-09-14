package br.edu.safeplace.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.suporte.PostgresDeTeste;

/**
 * Sobe o contexto completo contra um PostgreSQL real, o que executa todas as migrações Flyway e
 * valida o mapeamento JPA de todas as entidades (a aplicação usa {@code ddl-auto: validate}).
 *
 * <p>Ver {@link PostgresDeTeste} para as formas de fornecer o banco.</p>
 */
@SpringBootTest
@EnabledIf("bancoDisponivel")
class BackendApplicationTests {

	static boolean bancoDisponivel() {
		return PostgresDeTeste.disponivel();
	}

	@DynamicPropertySource
	static void configurarDatasource(DynamicPropertyRegistry registry) {
		PostgresDeTeste.registrar(registry);
	}

	@Test
	void contextLoads() {
	}

}
