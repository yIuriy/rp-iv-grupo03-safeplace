// Testa, contra um PostgreSQL real, que criar a linha do mês da sequência CAT nunca zera um contador já emitido.
package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Testes de Integração - SequenciaProtocoloCatInicializador")
class SequenciaProtocoloCatInicializadorIntegracaoTest {

    // Ano fora do intervalo dos outros testes, para não disputar a linha de nenhum deles.
    private static final short ANO = 1999;
    private static final short MES = 1;

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private SequenciaProtocoloCatInicializador inicializador;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void limpar() {
        jdbcTemplate.update("DELETE FROM sequencias_protocolo_cat WHERE ano = ?", ANO);
    }

    @Test
    @DisplayName("Cria a linha do mês com contador zero quando ela ainda não existe")
    void criaLinhaAusente() {
        inicializador.garantirLinhaCriada(ANO, MES);

        assertThat(ultimoSequencial()).isZero();
    }

    @Test
    @DisplayName("Preserva o contador quando outra transação criou a linha antes")
    void preservaContadorDeLinhaJaCriada() {
        // Situação da corrida: a linha surgiu e já emitiu 5 protocolos antes desta chamada gravar.
        jdbcTemplate.update(
                "INSERT INTO sequencias_protocolo_cat (ano, mes, ultimo_sequencial) VALUES (?, ?, 5)", ANO, MES);

        // O conflito pode subir como exceção, que o chamador ignora. O contador tem de continuar em 5.
        catchThrowable(() -> inicializador.garantirLinhaCriada(ANO, MES));

        assertThat(ultimoSequencial()).isEqualTo(5L);
    }

    private Long ultimoSequencial() {
        return jdbcTemplate.queryForObject(
                "SELECT ultimo_sequencial FROM sequencias_protocolo_cat WHERE ano = ? AND mes = ?",
                Long.class, ANO, MES);
    }
}
