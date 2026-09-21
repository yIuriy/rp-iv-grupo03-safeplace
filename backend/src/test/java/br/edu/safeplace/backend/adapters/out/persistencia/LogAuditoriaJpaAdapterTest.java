package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import org.hibernate.annotations.Immutable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - LogAuditoriaJpaAdapter e LogAuditoriaEntity")
class LogAuditoriaJpaAdapterTest {

    @Mock
    private LogAuditoriaJpaRepository jpaRepository;

    private LogAuditoriaJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LogAuditoriaJpaAdapter(jpaRepository);
    }

    @Test
    @DisplayName("Entidade LogAuditoriaEntity deve possuir anotacao @Immutable do Hibernate")
    void entidadeDeveSerAnotadaComImmutable() {
        assertThat(LogAuditoriaEntity.class.isAnnotationPresent(Immutable.class))
                .as("LogAuditoriaEntity deve ser imutável no Hibernate")
                .isTrue();
    }

    @Test
    @DisplayName("Deve salvar log de auditoria mapeando corretamente para entidade e retornando dominio")
    void deveSalvarLogAuditoria() {
        LocalDateTime agora = LocalDateTime.now();
        LogAuditoria logDominio = new LogAuditoria(
                null,
                "CADASTRO",
                agora,
                "gestor@safeplace.com",
                "Novo EPI",
                "Epi",
                1
        );

        LogAuditoriaEntity entitySalva = new LogAuditoriaEntity(
                100,
                "CADASTRO",
                agora,
                "gestor@safeplace.com",
                "Novo EPI",
                "Epi",
                1
        );

        when(jpaRepository.save(any(LogAuditoriaEntity.class))).thenReturn(entitySalva);

        LogAuditoria resultado = adapter.salvar(logDominio);

        ArgumentCaptor<LogAuditoriaEntity> captor = ArgumentCaptor.forClass(LogAuditoriaEntity.class);
        verify(jpaRepository).save(captor.capture());

        LogAuditoriaEntity entityEnviada = captor.getValue();
        assertThat(entityEnviada.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(entityEnviada.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(entityEnviada.getJustificativa()).isEqualTo("Novo EPI");
        assertThat(entityEnviada.getEntidade()).isEqualTo("Epi");
        assertThat(entityEnviada.getEntidadeId()).isEqualTo(1);

        assertThat(resultado.getId()).isEqualTo(100);
        assertThat(resultado.getTipoOperacao()).isEqualTo("CADASTRO");
        assertThat(resultado.getResponsavel()).isEqualTo("gestor@safeplace.com");
        assertThat(resultado.getEntidade()).isEqualTo("Epi");
        assertThat(resultado.getEntidadeId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve listar logs por entidade e id ordenados por data decrescente")
    void deveListarLogsPorEntidade() {
        LocalDateTime agora = LocalDateTime.now();
        LogAuditoriaEntity entity1 = new LogAuditoriaEntity(1, "CADASTRO", agora.minusHours(1), "gestor@safeplace.com", null, "AreaRisco", 10);
        LogAuditoriaEntity entity2 = new LogAuditoriaEntity(2, "ALTERACAO", agora, "supervisor@safeplace.com", "Ajuste de limite", "AreaRisco", 10);

        when(jpaRepository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc("AreaRisco", 10))
                .thenReturn(List.of(entity2, entity1));

        List<LogAuditoria> logs = adapter.listarPorEntidade("AreaRisco", 10);

        assertThat(logs).hasSize(2);
        assertThat(logs.get(0).getId()).isEqualTo(2);
        assertThat(logs.get(0).getTipoOperacao()).isEqualTo("ALTERACAO");
        assertThat(logs.get(1).getId()).isEqualTo(1);
        assertThat(logs.get(1).getTipoOperacao()).isEqualTo("CADASTRO");
    }
}
