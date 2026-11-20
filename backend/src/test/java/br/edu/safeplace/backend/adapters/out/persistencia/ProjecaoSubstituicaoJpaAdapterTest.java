package br.edu.safeplace.backend.adapters.out.persistencia;

import static br.edu.safeplace.backend.suporte.ReferenciasDeTeste.epi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.domain.epi.ProjecaoSubstituicao;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - ProjecaoSubstituicaoJpaAdapter")
class ProjecaoSubstituicaoJpaAdapterTest {

    @Mock
    private ProjecaoSubstituicaoJpaRepository jpaRepository;

    private ProjecaoSubstituicaoJpaAdapter adapter;
    private final LocalDate hoje = LocalDate.of(2026, 9, 24);

    @BeforeEach
    void setUp() {
        adapter = new ProjecaoSubstituicaoJpaAdapter(jpaRepository);
    }

    private ProjecaoSubstituicao projecaoComId(Integer id, Integer epiId, LocalDate dataProjecao,
                                               LocalDate dataDescarte, String justificativa) {
        ProjecaoSubstituicao projecao = new ProjecaoSubstituicao(epi(epiId), dataProjecao, dataDescarte, justificativa);
        ReflectionTestUtils.setField(projecao, "id", id);
        return projecao;
    }

    @Test
    @DisplayName("Deve salvar projeção delegando diretamente ao JpaRepository da entidade de domínio")
    void deveSalvarProjecao() {
        ProjecaoSubstituicao dominio = new ProjecaoSubstituicao(epi(1), hoje, hoje.plusDays(40), "Desgaste mecânico");
        ProjecaoSubstituicao salva = projecaoComId(10, 1, hoje, hoje.plusDays(40), "Desgaste mecânico");

        when(jpaRepository.save(dominio)).thenReturn(salva);

        ProjecaoSubstituicao resultado = adapter.salvar(dominio);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(10);
        assertThat(resultado.getEpiId()).isEqualTo(1);
        assertThat(resultado.getDataProjecao()).isEqualTo(hoje);
        assertThat(resultado.getDataEstimadaDescarte()).isEqualTo(hoje.plusDays(40));
        assertThat(resultado.getJustificativa()).isEqualTo("Desgaste mecânico");
    }

    @Test
    @DisplayName("Deve buscar última projeção de um EPI por ordem decrescente")
    void deveBuscarUltimaPorEpi() {
        ProjecaoSubstituicao projecao = projecaoComId(20, 2, hoje, hoje.plusDays(60), "Desgaste");

        when(jpaRepository.findFirstByEpiCodigoEPIOrderByDataProjecaoDescIdDesc(2))
                .thenReturn(Optional.of(projecao));

        Optional<ProjecaoSubstituicao> resultado = adapter.buscarUltimaPorEpi(2);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(20);
        assertThat(resultado.get().getEpiId()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve listar projeções de um EPI")
    void deveListarPorEpi() {
        ProjecaoSubstituicao p1 = projecaoComId(1, 3, hoje, hoje.plusDays(30), "Projeção 1");
        ProjecaoSubstituicao p2 = projecaoComId(2, 3, hoje.minusDays(10), hoje.plusDays(20), "Projeção 2");

        when(jpaRepository.findByEpiCodigoEPIOrderByDataProjecaoDescIdDesc(3))
                .thenReturn(List.of(p1, p2));

        List<ProjecaoSubstituicao> lista = adapter.listarPorEpi(3);

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getId()).isEqualTo(1);
        assertThat(lista.get(1).getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve salvar todas as projeções em lote")
    void deveSalvarTodasEmLote() {
        ProjecaoSubstituicao p1 = new ProjecaoSubstituicao(epi(1), hoje, hoje.plusDays(30), "P1");
        ProjecaoSubstituicao p2 = new ProjecaoSubstituicao(epi(2), hoje, hoje.plusDays(45), "P2");

        ProjecaoSubstituicao s1 = projecaoComId(1, 1, hoje, hoje.plusDays(30), "P1");
        ProjecaoSubstituicao s2 = projecaoComId(2, 2, hoje, hoje.plusDays(45), "P2");

        when(jpaRepository.saveAll(any())).thenReturn(List.of(s1, s2));

        List<ProjecaoSubstituicao> salvas = adapter.salvar(List.of(p1, p2));

        assertThat(salvas).hasSize(2);
        verify(jpaRepository).saveAll(any());
    }
}
