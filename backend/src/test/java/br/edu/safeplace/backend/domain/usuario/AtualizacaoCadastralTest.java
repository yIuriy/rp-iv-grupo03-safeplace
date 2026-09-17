package br.edu.safeplace.backend.domain.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #122: a atualização cadastral devolve uma cópia com os dados novos e preserva tudo o que
 * identifica a pessoa e o seu papel.
 */
class AtualizacaoCadastralTest {

    private static final LocalDateTime CRIACAO = LocalDateTime.of(2026, 9, 1, 8, 0);
    private static final LocalDate NASCIMENTO = LocalDate.of(1992, 6, 18);

    private static Colaborador colaborador() {
        return new Colaborador(7, "529.982.247-25", "João da Silva", NASCIMENTO, null, true, CRIACAO, CRIACAO);
    }

    private static Supervisor supervisora() {
        return new Supervisor(3, "11144477735", "Joana Ribeiro", LocalDate.of(1988, 4, 20),
                "joana@safeplace.com", "hash:abc", true, CRIACAO, CRIACAO);
    }

    @Test
    @DisplayName("Colaborador atualizado preserva id, CPF, situação e criação; só os dados cadastrais mudam")
    void colaboradorPreservaIdentidade() {
        Colaborador original = colaborador();

        Colaborador atualizado = original.comDadosAtualizados(
                "João Pedro da Silva", LocalDate.of(1992, 6, 19), " Joao@Safeplace.com ");

        assertThat(atualizado.getId()).isEqualTo(7);
        assertThat(atualizado.getCpf()).isEqualTo("52998224725");
        assertThat(atualizado.getPerfil()).isEqualTo(Perfil.COLABORADOR);
        assertThat(atualizado.isAtivo()).isTrue();
        assertThat(atualizado.getCriadoEm()).isEqualTo(CRIACAO);
        assertThat(atualizado.getAtualizadoEm()).isAfter(CRIACAO);
        assertThat(atualizado.getNome()).isEqualTo("João Pedro da Silva");
        assertThat(atualizado.getDataNascimento()).isEqualTo(LocalDate.of(1992, 6, 19));
        assertThat(atualizado.getEmail()).isEqualTo("joao@safeplace.com");
    }

    @Test
    @DisplayName("O objeto original não muda: a atualização é uma cópia")
    void originalPermaneceIntacto() {
        Colaborador original = colaborador();

        original.comDadosAtualizados("Outro Nome", NASCIMENTO, "outro@safeplace.com");

        assertThat(original.getNome()).isEqualTo("João da Silva");
        assertThat(original.getEmail()).isNull();
        assertThat(original.getAtualizadoEm()).isEqualTo(CRIACAO);
    }

    @Test
    @DisplayName("Supervisor atualizado continua Supervisor e mantém o hash da senha")
    void supervisorPreservaPerfilECredencial() {
        Colaborador atualizado = supervisora().comDadosAtualizados(
                "Joana Ribeiro Souza", LocalDate.of(1988, 4, 20), "joana.souza@safeplace.com");

        assertThat(atualizado).isInstanceOf(Supervisor.class);
        assertThat(atualizado.getPerfil()).isEqualTo(Perfil.SUPERVISOR);
        assertThat(((Supervisor) atualizado).getSenha()).isEqualTo("hash:abc");
        assertThat(atualizado.getId()).isEqualTo(3);
        assertThat(atualizado.getEmail()).isEqualTo("joana.souza@safeplace.com");
    }

    @Test
    @DisplayName("Gestor de Segurança atualizado continua Gestor e mantém o hash da senha")
    void gestorPreservaPerfilECredencial() {
        GestorDeSeguranca original = new GestorDeSeguranca(1, "52998224725", "Gestor", NASCIMENTO,
                "gestor@safeplace.com", "hash:xyz", true, CRIACAO, CRIACAO);

        Colaborador atualizado = original.comDadosAtualizados("Gestora Ana", NASCIMENTO, "gestor@safeplace.com");

        assertThat(atualizado).isInstanceOf(GestorDeSeguranca.class);
        assertThat(atualizado.getPerfil()).isEqualTo(Perfil.GESTOR_SEGURANCA);
        assertThat(((GestorDeSeguranca) atualizado).getSenha()).isEqualTo("hash:xyz");
    }

    @Test
    @DisplayName("Supervisor não pode ficar sem e-mail, porque ele é a identificação de acesso")
    void supervisorExigeEmail() {
        Supervisor original = supervisora();

        assertThatThrownBy(() -> original.comDadosAtualizados("Joana", LocalDate.of(1988, 4, 20), " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("Dados inválidos são recusados pelas mesmas regras do cadastro")
    void dadosInvalidosSaoRecusados() {
        Colaborador original = colaborador();
        LocalDate futuro = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> original.comDadosAtualizados(" ", NASCIMENTO, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> original.comDadosAtualizados("João", futuro, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
