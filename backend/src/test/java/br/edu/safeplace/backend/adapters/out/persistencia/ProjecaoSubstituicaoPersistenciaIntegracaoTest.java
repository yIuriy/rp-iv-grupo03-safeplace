package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarEpiUseCase;
import br.edu.safeplace.backend.application.port.out.ProjecaoSubstituicaoRepositoryPort;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
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
    private ProjecaoSubstituicaoRepositoryPort projecaoRepository;

    @Autowired
    private GerenciarEpiUseCase gerenciarEpiUseCase;

    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

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
        var epiSalvo = gerenciarEpiUseCase.cadastrarEpi(dto);

        // Cria e salva a projeção
        ProjecaoSubstituicao projecao = ProjecaoSubstituicao.nova(
                epiSalvo.id(),
                hoje,
                hoje.plusDays(90),
                "Substituição por desgaste em ambiente BAIXO"
        );

        ProjecaoSubstituicao salva = projecaoRepository.salvar(projecao);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getEpiId()).isEqualTo(epiSalvo.id());
        assertThat(salva.getDataProjecao()).isEqualTo(hoje);
        assertThat(salva.getDataEstimadaDescarte()).isEqualTo(hoje.plusDays(90));

        // Consulta a última
        Optional<ProjecaoSubstituicao> ultima = projecaoRepository.buscarUltimaPorEpi(epiSalvo.id());
        assertThat(ultima).isPresent();
        assertThat(ultima.get().getId()).isEqualTo(salva.getId());

        // Lista por EPI
        List<ProjecaoSubstituicao> lista = projecaoRepository.listarPorEpi(epiSalvo.id());
        assertThat(lista).isNotEmpty();
        assertThat(lista.get(0).getEpiId()).isEqualTo(epiSalvo.id());
    }
}
