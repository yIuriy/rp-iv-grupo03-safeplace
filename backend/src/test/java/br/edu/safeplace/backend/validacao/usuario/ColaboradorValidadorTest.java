package br.edu.safeplace.backend.validacao.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;

/**
 * Os casos de recusa vieram de {@code ColaboradorTest}, {@code GestorDeSegurancaTest},
 * {@code SupervisorTest} e {@code AtualizacaoCadastralTest}, com as mesmas asserções (issue #263):
 * antes, quem lançava eram os construtores; agora é a cadeia, antes do repository.
 */
@DisplayName("ColaboradorValidador - cadeia de validação de pessoas (Chain of Responsibility)")
class ColaboradorValidadorTest {

    private static final String CPF = "52998224725";
    private static final LocalDate NASCIMENTO = LocalDate.of(1985, 7, 20);
    private static final LocalDateTime CRIACAO = LocalDateTime.of(2026, 1, 10, 9, 0);

    private final ColaboradorValidador validador = new ColaboradorValidador(null, new CpfValidador());

    @Test
    @DisplayName("Aceita colaborador, supervisor e gestor completos")
    void aceitaPessoasValidas() {
        assertThatCode(() -> validador.validar(Colaborador.novo("Carlos", CPF, NASCIMENTO, null))).doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(Supervisor.novo("Ana Lima", CPF, NASCIMENTO, "ana@empresa.com", "hash")))
                .doesNotThrowAnyException();
        assertThatCode(() -> validador.validar(GestorDeSeguranca.novo("Marina", CPF, NASCIMENTO, "marina@empresa.com", "hash")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Supervisor exige senha e e-mail")
    void deveRejeitarSupervisorSemSenhaOuSemEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(Supervisor.novo("Ana Lima", CPF, LocalDate.of(1985, 7, 20), "ana@empresa.com", null)));

        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(Supervisor.novo("Ana Lima", CPF, LocalDate.of(1985, 7, 20), "", "hash_senha")));
    }

    @Test
    @DisplayName("Colaborador: nascimento no futuro e CPF inválido são recusados")
    void deveRejeitarColaboradorComDataNoFuturoOuCpfInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(Colaborador.novo("Carlos", CPF, LocalDate.now().plusDays(1), "carlos@empresa.com")));

        assertThrows(CpfInvalidoException.class, () ->
                validador.validar(Colaborador.novo("Carlos", "11111111111", LocalDate.of(1990, 1, 1), "carlos@empresa.com")));
    }

    @Test
    @DisplayName("Gestor exige senha e e-mail")
    void deveRejeitarGestorSemSenhaOuSemEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(GestorDeSeguranca.novo("Marina Costa", CPF, NASCIMENTO, "marina@empresa.com", null)));
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(GestorDeSeguranca.novo("Marina Costa", CPF, NASCIMENTO, "marina@empresa.com", "  ")));
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(GestorDeSeguranca.novo("Marina Costa", CPF, NASCIMENTO, null, "$2a$10$hash")));
    }

    @Test
    @DisplayName("Supervisor e Gestor seguem as mesmas regras de identificação do Colaborador")
    void deveHerdarInvariantesDeIdentificacao() {
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(GestorDeSeguranca.novo(" ", CPF, NASCIMENTO, "marina@empresa.com", "$2a$10$hash")));
        assertThrows(CpfInvalidoException.class, () ->
                validador.validar(GestorDeSeguranca.novo("Marina Costa", "11111111111", NASCIMENTO, "marina@empresa.com", "$2a$10$hash")));
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(Supervisor.novo("", CPF, NASCIMENTO, "ana@empresa.com", "$2a$10$hash")));
        assertThrows(IllegalArgumentException.class, () ->
                validador.validar(Supervisor.novo("Ana Lima", CPF, LocalDate.now().plusDays(1), "ana@empresa.com", "$2a$10$hash")));
        assertThrows(CpfInvalidoException.class, () ->
                validador.validar(Supervisor.novo("Ana Lima", "123", NASCIMENTO, "ana@empresa.com", "$2a$10$hash")));
    }

    @Test
    @DisplayName("Atualização: Supervisor não pode ficar sem e-mail, porque ele é a identificação de acesso")
    void supervisorExigeEmailNaAtualizacao() {
        Supervisor original = new Supervisor(3, "11144477735", "Joana Ribeiro", LocalDate.of(1988, 4, 20),
                "joana@safeplace.com", "hash:abc", true, CRIACAO, CRIACAO);

        assertThatThrownBy(() -> validador.validar(original.comDadosAtualizados("Joana", LocalDate.of(1988, 4, 20), " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    @DisplayName("Atualização: dados inválidos são recusados pelas mesmas regras do cadastro")
    void dadosInvalidosSaoRecusadosNaAtualizacao() {
        Colaborador original = new Colaborador(7, "529.982.247-25", "João da Silva", LocalDate.of(1992, 6, 18),
                null, true, CRIACAO, CRIACAO);
        LocalDate futuro = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> validador.validar(original.comDadosAtualizados(" ", LocalDate.of(1992, 6, 18), null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validador.validar(original.comDadosAtualizados("João", futuro, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("RF23: CPF e e-mail de outra pessoa são recusados; os da própria pessoa, na atualização, não")
    void identificacaoDuplicadaSoQuandoEDeOutraPessoa() {
        Colaborador existente = new Colaborador(7, CPF, "João", NASCIMENTO, "joao@empresa.com", true, CRIACAO, CRIACAO);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        when(usuarios.buscarPorCpf(anyString())).thenReturn(Optional.empty());
        when(usuarios.buscarPorEmail(anyString())).thenReturn(Optional.empty());
        when(usuarios.buscarPorCpf(CPF)).thenReturn(Optional.of(existente));
        when(usuarios.buscarPorEmail("joao@empresa.com")).thenReturn(Optional.of(existente));
        ColaboradorValidador comRepositorio = new ColaboradorValidador(usuarios, new CpfValidador());

        assertThatThrownBy(() -> comRepositorio.validar(Colaborador.novo("Outro", "529.982.247-25", NASCIMENTO, null)))
                .isInstanceOf(CpfJaCadastradoException.class);
        assertThatThrownBy(() -> comRepositorio.validar(Colaborador.novo("Outro", "11144477735", NASCIMENTO, "JOAO@empresa.com")))
                .isInstanceOf(EmailJaCadastradoException.class);
        assertThatCode(() -> comRepositorio.validar(existente.comDadosAtualizados("João Silva", NASCIMENTO, "joao@empresa.com")))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CPF inválido interrompe a cadeia: não consulta cadastro nem dados pessoais")
    void cpfInvalidoInterrompeACadeia() {
        assertThat(validador.verificar(Colaborador.novo(" ", "123", null, null)).mensagens())
                .containsExactly("CPF deve conter exatamente 11 dígitos numéricos.");
    }

    @Test
    @DisplayName("Nascimento é comparado com o relógio injetado (issue #308)")
    void nascimentoUsaORelogioInjetado() {
        LocalDate hoje = LocalDate.of(2026, 1, 10);
        ColaboradorValidador comRelogioFixo = new ColaboradorValidador(null, new CpfValidador(), () -> hoje);

        assertThat(comRelogioFixo.verificar(Colaborador.novo("Carlos", CPF, hoje.plusDays(1), null)).mensagens())
                .containsExactly("Data de nascimento não pode estar no futuro.");
        assertThatCode(() -> comRelogioFixo.validar(Colaborador.novo("Carlos", CPF, hoje, null)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("E-mail de perfil com conta segue o @Email dos Requests (issue #308)")
    void emailDoPerfilComContaPrecisaDeFormatoValido() {
        assertThat(validador.verificar(Supervisor.novo("Ana Lima", CPF, NASCIMENTO, "ana.empresa.com", "hash")).mensagens())
                .containsExactly("Email inválido para o perfil Supervisor.");
        assertThat(validador.verificar(GestorDeSeguranca.novo("Marina", CPF, NASCIMENTO, "marina@", "hash")).mensagens())
                .containsExactly("Email inválido para o perfil Gestor de Segurança.");
        assertThatCode(() -> validador.validar(Supervisor.novo("Ana Lima", CPF, NASCIMENTO, "ana@safeplace.test", "hash")))
                .doesNotThrowAnyException();
    }
}
