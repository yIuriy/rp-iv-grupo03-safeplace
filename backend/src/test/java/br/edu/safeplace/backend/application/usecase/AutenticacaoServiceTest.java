package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.LoginInputDTO;
import br.edu.safeplace.backend.application.dto.output.TokenOutputDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GerenciadorToken;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodificadorSenha codificadorSenha;

    @Mock
    private GerenciadorToken gerenciadorToken;

    private AutenticacaoService casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new AutenticacaoService(usuarioRepository, codificadorSenha, gerenciadorToken);
    }

    @Test
    @DisplayName("Deve autenticar Gestor de Segurança com sucesso e retornar token JWT")
    void deveAutenticarGestorComSucesso() {
        String email = "gestor@safeplace.com";
        String senhaPura = "SenhaForte@123";
        String hash = "$2a$10$hashedPassword";
        String tokenJwt = "mock.jwt.token";

        GestorDeSeguranca gestor = new GestorDeSeguranca(
                1, "52998224725", "Roberto Gestor", LocalDate.of(1980, 5, 10),
                email, hash, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.of(gestor));
        when(codificadorSenha.validar(senhaPura, hash)).thenReturn(true);
        when(gerenciadorToken.gerarToken(email, Perfil.GESTOR_SEGURANCA.name())).thenReturn(tokenJwt);

        LoginInputDTO entrada = new LoginInputDTO(email, senhaPura);
        TokenOutputDTO resposta = casoDeUso.autenticar(entrada);

        assertNotNull(resposta);
        assertEquals(tokenJwt, resposta.token());
        assertEquals("Bearer", resposta.tipo());
        assertEquals(email, resposta.email());
        assertEquals("Roberto Gestor", resposta.nome());
        assertEquals(Perfil.GESTOR_SEGURANCA.name(), resposta.perfil());

        verify(gerenciadorToken).gerarToken(email, Perfil.GESTOR_SEGURANCA.name());
    }

    @Test
    @DisplayName("Deve autenticar Supervisor com sucesso e retornar token JWT")
    void deveAutenticarSupervisorComSucesso() {
        String email = "supervisor@safeplace.com";
        String senhaPura = "Supervisor@123";
        String hash = "$2a$10$hashedPassword";
        String tokenJwt = "mock.jwt.token.supervisor";

        Supervisor supervisor = new Supervisor(
                2, "52998224725", "Ana Supervisora", LocalDate.of(1990, 8, 15),
                email, hash, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.of(supervisor));
        when(codificadorSenha.validar(senhaPura, hash)).thenReturn(true);
        when(gerenciadorToken.gerarToken(email, Perfil.SUPERVISOR.name())).thenReturn(tokenJwt);

        LoginInputDTO entrada = new LoginInputDTO(email, senhaPura);
        TokenOutputDTO resposta = casoDeUso.autenticar(entrada);

        assertNotNull(resposta);
        assertEquals(tokenJwt, resposta.token());
        assertEquals(Perfil.SUPERVISOR.name(), resposta.perfil());
    }

    @Test
    @DisplayName("Deve rejeitar terminantemente tentativa de login de Colaborador (RNF03)")
    void deveRejeitarLoginDeColaborador() {
        String email = "colaborador@safeplace.com";
        Colaborador colaborador = new Colaborador(
                3, "52998224725", "João Operário", LocalDate.of(1995, 3, 20),
                email, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.of(colaborador));

        LoginInputDTO entrada = new LoginInputDTO(email, "qualquerSenha");
        CredenciaisInvalidasException ex = assertThrows(
                CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(entrada)
        );

        assertTrue(ex.getMessage().contains("Colaborador não possui permissão de login"));
        verifyNoInteractions(codificadorSenha);
        verifyNoInteractions(gerenciadorToken);
    }

    @Test
    @DisplayName("Deve rejeitar login quando senha for incorreta")
    void deveRejeitarLoginQuandoSenhaIncorreta() {
        String email = "gestor@safeplace.com";
        String hash = "$2a$10$hashedPassword";

        GestorDeSeguranca gestor = new GestorDeSeguranca(
                1, "52998224725", "Roberto Gestor", LocalDate.of(1980, 5, 10),
                email, hash, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.of(gestor));
        when(codificadorSenha.validar("senhaErrada", hash)).thenReturn(false);

        LoginInputDTO entrada = new LoginInputDTO(email, "senhaErrada");
        assertThrows(CredenciaisInvalidasException.class, () -> casoDeUso.autenticar(entrada));

        verifyNoInteractions(gerenciadorToken);
    }

    @Test
    @DisplayName("Deve rejeitar login quando usuário não for encontrado")
    void deveRejeitarLoginQuandoUsuarioNaoEncontrado() {
        String email = "inexistente@safeplace.com";
        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.empty());

        LoginInputDTO entrada = new LoginInputDTO(email, "senha123");
        assertThrows(CredenciaisInvalidasException.class, () -> casoDeUso.autenticar(entrada));

        verifyNoInteractions(codificadorSenha);
        verifyNoInteractions(gerenciadorToken);
    }

    @Test
    @DisplayName("Deve rejeitar login quando usuário estiver inativo")
    void deveRejeitarLoginQuandoUsuarioInativo() {
        String email = "inativo@safeplace.com";
        GestorDeSeguranca inativo = new GestorDeSeguranca(
                1, "52998224725", "Inativo", LocalDate.of(1980, 5, 10),
                email, "hash", false, LocalDateTime.now(), LocalDateTime.now()
        );

        when(usuarioRepository.buscarPorEmail(email)).thenReturn(Optional.of(inativo));

        LoginInputDTO entrada = new LoginInputDTO(email, "senha123");
        CredenciaisInvalidasException ex = assertThrows(
                CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(entrada)
        );

        assertTrue(ex.getMessage().contains("inativo"));
        verifyNoInteractions(codificadorSenha);
        verifyNoInteractions(gerenciadorToken);
    }

    @Test
    @DisplayName("Deve rejeitar login com campos nulos ou em branco")
    void deveRejeitarLoginComCamposInvalidos() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginInputDTO(null, "senha")));
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginInputDTO("   ", "senha")));
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginInputDTO("email@teste.com", null)));
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginInputDTO("email@teste.com", "")));
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de login informando CPF no lugar de email")
    void deveRejeitarLoginComCpf() {
        String cpf = "52998224725";
        when(usuarioRepository.buscarPorEmail(cpf)).thenReturn(Optional.empty());

        LoginInputDTO entrada = new LoginInputDTO(cpf, "senha123");
        assertThrows(CredenciaisInvalidasException.class, () -> casoDeUso.autenticar(entrada));

        verify(usuarioRepository).buscarPorEmail(cpf);
        verify(usuarioRepository, never()).buscarPorCpf(any());
    }
}
