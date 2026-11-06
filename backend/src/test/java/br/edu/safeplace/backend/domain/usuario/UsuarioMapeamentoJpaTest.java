package br.edu.safeplace.backend.domain.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/** A hierarquia de usuários é a própria entidade JPA da tabela usuarios (#256), sem classes espelhadas. */
class UsuarioMapeamentoJpaTest {

    @Test
    void colaboradorEhARaizDaHerancaSingleTableComDiscriminadorPerfil() {
        assertThat(Colaborador.class.getAnnotation(Entity.class)).isNotNull();
        assertThat(Colaborador.class.getAnnotation(Table.class).name()).isEqualTo("usuarios");
        assertThat(Colaborador.class.getAnnotation(Inheritance.class).strategy())
                .isEqualTo(InheritanceType.SINGLE_TABLE);
        assertThat(Colaborador.class.getAnnotation(DiscriminatorColumn.class).name()).isEqualTo("perfil");
    }

    @Test
    void cadaPerfilUsaOValorJaGravadoNoBanco() {
        assertThat(Colaborador.class.getAnnotation(DiscriminatorValue.class).value()).isEqualTo("COLABORADOR");
        assertThat(Supervisor.class.getAnnotation(DiscriminatorValue.class).value())
                .isEqualTo(Perfil.SUPERVISOR.name());
        assertThat(GestorDeSeguranca.class.getAnnotation(DiscriminatorValue.class).value())
                .isEqualTo(Perfil.GESTOR_SEGURANCA.name());
    }

    @Test
    void identidadeEhPeloIdQuandoEleExiste() {
        Colaborador lido = new Colaborador(7, "52998224725", "Ana", LocalDate.of(1990, 5, 10), null, true,
                LocalDateTime.now(), LocalDateTime.now());
        Colaborador atualizado = lido.comDadosAtualizados("Ana Souza", lido.getDataNascimento(), null);
        Colaborador novo = Colaborador.novo("Ana", "52998224725", LocalDate.of(1990, 5, 10), null);

        assertThat(atualizado).isEqualTo(lido).hasSameHashCodeAs(lido);
        assertThat(novo).isNotEqualTo(Colaborador.novo("Ana", "52998224725", LocalDate.of(1990, 5, 10), null));
    }
}
