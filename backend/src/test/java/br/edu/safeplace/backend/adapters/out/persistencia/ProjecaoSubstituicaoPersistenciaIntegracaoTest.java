package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepository;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;
import br.edu.safeplace.backend.suporte.PostgresDeTeste;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIf("bancoDisponivel")
@DisplayName("Persistência de Projeções de Substituição no PostgreSQL (Issue #108 / RF21)")
class ProjecaoSubstituicaoPersistenciaIntegracaoTest {

    static boolean bancoDisponivel() {
        return PostgresDeTeste.disponivel();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        PostgresDeTeste.registrar(registry);
    }

    @Autowired
    private ProjecaoSubstituicaoRepository projecaoRepository;

    @Autowired
    private EpiRepository epiRepository;

    @Autowired
    private GerenciarEpiUseCase gerenciarEpiUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    private Integer epiId;

    /**
     * Apaga a projeção e o EPI criados aqui. Sem essa limpeza, a projeção prende o EPI pela chave
     * estrangeira, e o {@code DELETE FROM epis} dos testes de estoque executados depois falha.
     */
    @AfterEach
    void limparDados() {
        if (epiId == null) {
            return;
        }
        jdbcTemplate.update("DELETE FROM projecoes_substituicao WHERE epi_id = ?", epiId);
        jdbcTemplate.update("DELETE FROM movimentacoes_estoque WHERE epi_id = ?", epiId);
        jdbcTemplate.update("DELETE FROM epis WHERE id = ?", epiId);
        jdbcTemplate.execute("DELETE FROM lotes_epi l WHERE NOT EXISTS (SELECT 1 FROM epis e WHERE e.lote_id = l.id) "
                + "AND NOT EXISTS (SELECT 1 FROM movimentacoes_estoque m WHERE m.lote_id = l.id)");
        jdbcTemplate.execute("DELETE FROM modelos_epi o WHERE NOT EXISTS (SELECT 1 FROM lotes_epi l WHERE l.modelo_id = o.id)");
    }

    @Test
    @DisplayName("Deve persistir e consultar projeção de substituição vinculada a EPI")
    void devePersistirEConsultarProjecao() {
        // Cadastra um EPI válido para ter FK correta
        CadastrarEpiInputDTO dto = new CadastrarEpiInputDTO(
                "Luva de Segurança para Projeção",
                "998877",
                10,
                2,
                hoje.plusYears(1),
                180,
                "Descrição",
                ClassificacaoEPI.PROTECAO_DE_MEMBROS_SUPERIORES
        );
        var epiSalvo = gerenciarEpiUseCase.cadastrar(dto);
        epiId = epiSalvo.id();
        Epi epiEntidade = epiRepository.buscarPorId(epiId).orElseThrow();

        // Cria e salva a projeção
        ProjecaoSubstituicao projecao = new ProjecaoSubstituicao(
                epiEntidade,
                hoje,
                hoje.plusDays(90),
                "Substituição por desgaste em ambiente BAIXO"
        );

        ProjecaoSubstituicao salva = projecaoRepository.salvar(projecao);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getEpi().getId()).isEqualTo(epiSalvo.id());
        assertThat(salva.getEpiId()).isEqualTo(epiSalvo.id());
        assertThat(salva.getDataProjecao()).isEqualTo(hoje);
        assertThat(salva.getDataEstimadaDescarte()).isEqualTo(hoje.plusDays(90));
        assertThat(salva.getCriadoEm()).isNotNull();

        // Consulta a última
        Optional<ProjecaoSubstituicao> ultima = projecaoRepository.buscarUltimaPorEpi(epiSalvo.id());
        assertThat(ultima).isPresent();
        assertThat(ultima.get().getId()).isEqualTo(salva.getId());
        assertThat(ultima.get().getEpi().getNome()).isEqualTo("Luva de Segurança para Projeção");

        // Lista por EPI
        List<ProjecaoSubstituicao> lista = projecaoRepository.listarPorEpi(epiSalvo.id());
        assertThat(lista).isNotEmpty();
        assertThat(lista.get(0).getEpiId()).isEqualTo(epiSalvo.id());
        assertThat(lista.get(0).getEpi().getId()).isEqualTo(epiSalvo.id());
    }
}
