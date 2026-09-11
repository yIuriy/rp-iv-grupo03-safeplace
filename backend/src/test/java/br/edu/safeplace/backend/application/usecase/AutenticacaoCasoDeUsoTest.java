package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.LoginEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.TokenSaidaDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.TokenPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
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
class AutenticacaoCasoDeUsoTest {

    @Mock
    private UsuarioRepositorioPorta usuarioRepositorioPorta;

    @Mock
    private CodificadorSenhaPorta codificadorSenhaPorta;

    @Mock
    private TokenPorta tokenPorta;

    private AutenticacaoCasoDeUso casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new AutenticacaoCasoDeUso(usuarioRepositorioPorta, codificadorSenhaPorta, tokenPorta);
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

        when(usuarioRepositorioPorta.buscarPorEmail(email)).thenReturn(Optional.of(gestor));
        when(codificadorSenhaPorta.validar(senhaPura, hash)).thenReturn(true);
        when(tokenPorta.gerarToken(email, Perfil.GESTOR_SEGURANCA.name())).thenReturn(tokenJwt);

        LoginEntradaDTO entrada = new LoginEntradaDTO(email, senhaPura);
        TokenSaidaDTO resposta = casoDeUso.autenticar(entrada);

        assertNotNull(resposta);
        assertEquals(tokenJwt, resposta.token());
        assertEquals("Bearer", resposta.tipo());
        assertEquals(email, resposta.email());
        assertEquals("Roberto Gestor", resposta.nome());
        assertEquals(Perfil.GESTOR_SEGURANCA.name(), resposta.perfil());

        verify(tokenPorta).gerarToken(email, Perfil.GESTOR_SEGURANCA.name());
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

        when(usuarioRepositorioPorta.buscarPorEmail(email)).thenReturn(Optional.of(supervisor));
        when(codificadorSenhaPorta.validar(senhaPura, hash)).thenReturn(true);
        when(tokenPorta.gerarToken(email, Perfil.SUPERVISOR.name())).thenReturn(tokenJwt);

        LoginEntradaDTO entrada = new LoginEntradaDTO(email, senhaPura);
        TokenSaidaDTO resposta = casoDeUso.autenticar(entrada);

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

        when(usuarioRepositorioPorta.buscarPorEmail(email)).thenReturn(Optional.of(colaborador));

        LoginEntradaDTO entrada = new LoginEntradaDTO(email, "qualquerSenha");
        CredenciaisInvalidasException ex = assertThrows(
                CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(entrada)
        );

        assertTrue(ex.getMessage().contains("Colaborador não possui permissão de login"));
        verifyNoInteractions(codificadorSenhaPorta);
        verifyNoInteractions(tokenPorta);
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

        when(usuarioRepositorioPorta.buscarPorEmail(email)).thenReturn(Optional.of(gestor));
        when(codificadorSenhaPorta.validar("senhaErrada", hash)).thenReturn(false);

        LoginEntradaDTO entrada = new LoginEntradaDTO(email, "senhaErrada");
        assertThrows(CredenciaisInvalidasException.class, () -> casoDeUso.autenticar(entrada));

        verifyNoInteractions(tokenPorta);
    }

    @Test
    @DisplayName("Deve rejeitar login quando usuário não for encontrado")
    void deveRejeitarLoginQuandoUsuarioNaoEncontrado() {
        String email = "inexistente@safeplace.com";
        when(usuarioRepositorioPorta.buscarPorEmail(email)).thenReturn(Optional.empty());

        LoginEntradaDTO entrada = new LoginEntradaDTO(email, "senha123");
        assertThrows(CredenciaisInvalidasException.class, () -> casoDeUso.autenticar(entrada));

        verifyNoInteractions(codificadorSenhaPorta);
        verifyNoInteractions(tokenPorta);
    }

    @Test
    @DisplayName("Deve rejeitar login quando usuário estiver inativo")
    void deveRejeitarLoginQuandoUsuarioInativo() {
        String email = "inativo@safeplace.com";
        GestorDeSeguranca inativo = new GestorDeSeguranca(
                1, "52998224725", "Inativo", LocalDate.of(1980, 5, 10),
                email, "hash", false, LocalDateTime.now(), LocalDateTime.now()
        );

        when(usuarioRepositorioPorta.buscarPorEmail(email)).thenReturn(Optional.of(inativo));

        LoginEntradaDTO entrada = new LoginEntradaDTO(email, "senha123");
        CredenciaisInvalidasException ex = assertThrows(
                CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(entrada)
        );

        assertTrue(ex.getMessage().contains("inativo"));
        verifyNoInteractions(codificadorSenhaPorta);
        verifyNoInteractions(tokenPorta);
    }

    @Test
    @DisplayName("Deve rejeitar login com campos nulos ou em branco")
    void deveRejeitarLoginComCamposInvalidos() {
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginEntradaDTO(null, "senha")));
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginEntradaDTO("   ", "senha")));
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginEntradaDTO("email@teste.com", null)));
        assertThrows(CredenciaisInvalidasException.class,
                () -> casoDeUso.autenticar(new LoginEntradaDTO("email@teste.com", "")));
    }

    @Test
    @DisplayName("Deve rejeitar tentativa de login informando CPF no lugar de email")
    void deveRejeitarLoginComCpf() {
        String cpf = "52998224725";
        when(usuarioRepositorioPorta.buscarPorEmail(cpf)).thenReturn(Optional.empty());

        LoginEntradaDTO entrada = new LoginEntradaDTO(cpf, "senha123");
        assertThrows(CredenciaisInvalidasException.class, () -> casoDeUso.autenticar(entrada));

        verify(usuarioRepositorioPorta).buscarPorEmail(cpf);
        verify(usuarioRepositorioPorta, never()).buscarPorCpf(any());
    }
}
