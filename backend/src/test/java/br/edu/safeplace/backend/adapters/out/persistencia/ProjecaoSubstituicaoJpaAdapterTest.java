package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    @DisplayName("Deve salvar projeção mapeando corretamente entre domínio e entidade")
    void deveSalvarProjecao() {
        ProjecaoSubstituicao dominio = ProjecaoSubstituicao.nova(1, hoje, hoje.plusDays(40), "Desgaste mecânico");
        ProjecaoSubstituicaoEntity entitySalva = new ProjecaoSubstituicaoEntity(
                10, 1, hoje, hoje.plusDays(40), "Desgaste mecânico");

        when(jpaRepository.save(any(ProjecaoSubstituicaoEntity.class))).thenReturn(entitySalva);

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
        ProjecaoSubstituicaoEntity entity = new ProjecaoSubstituicaoEntity(
                20, 2, hoje, hoje.plusDays(60), "Desgaste");

        when(jpaRepository.findFirstByEpiIdOrderByDataProjecaoDescIdDesc(2))
                .thenReturn(Optional.of(entity));

        Optional<ProjecaoSubstituicao> resultado = adapter.buscarUltimaPorEpi(2);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(20);
        assertThat(resultado.get().getEpiId()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve listar projeções de um EPI")
    void deveListarPorEpi() {
        ProjecaoSubstituicaoEntity entity1 = new ProjecaoSubstituicaoEntity(
                1, 3, hoje, hoje.plusDays(30), "Projeção 1");
        ProjecaoSubstituicaoEntity entity2 = new ProjecaoSubstituicaoEntity(
                2, 3, hoje.minusDays(10), hoje.plusDays(20), "Projeção 2");

        when(jpaRepository.findByEpiIdOrderByDataProjecaoDescIdDesc(3))
                .thenReturn(List.of(entity1, entity2));

        List<ProjecaoSubstituicao> lista = adapter.listarPorEpi(3);

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getId()).isEqualTo(1);
        assertThat(lista.get(1).getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve salvar todas as projeções em lote")
    void deveSalvarTodasEmLote() {
        ProjecaoSubstituicao p1 = ProjecaoSubstituicao.nova(1, hoje, hoje.plusDays(30), "P1");
        ProjecaoSubstituicao p2 = ProjecaoSubstituicao.nova(2, hoje, hoje.plusDays(45), "P2");

        ProjecaoSubstituicaoEntity e1 = new ProjecaoSubstituicaoEntity(1, 1, hoje, hoje.plusDays(30), "P1");
        ProjecaoSubstituicaoEntity e2 = new ProjecaoSubstituicaoEntity(2, 2, hoje, hoje.plusDays(45), "P2");

        when(jpaRepository.saveAll(any())).thenReturn(List.of(e1, e2));

        List<ProjecaoSubstituicao> salvas = adapter.salvarTodas(List.of(p1, p2));

        assertThat(salvas).hasSize(2);
        verify(jpaRepository).saveAll(any());
    }
}
