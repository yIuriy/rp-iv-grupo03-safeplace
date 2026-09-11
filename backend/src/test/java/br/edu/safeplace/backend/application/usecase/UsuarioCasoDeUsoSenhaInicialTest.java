package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioEntradaDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.GeradorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Cobre a issue #91: senha inicial gerada automaticamente para Supervisor,
 * apenas o hash persistido, Colaborador sem credencial e Gestor com senha informada.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioCasoDeUsoSenhaInicialTest {

    private static final String CPF = "52998224725";
    private static final LocalDate NASCIMENTO = LocalDate.of(1985, 7, 20);
    private static final String SENHA_GERADA = "Xk7!pQ2#mA9z";
    private static final String HASH = "$2a$10$hashDaSenhaGerada";

    @Mock
    private UsuarioRepositorioPorta repositorioPorta;
    @Mock
    private CodificadorSenhaPorta codificadorSenhaPorta;
    @Mock
    private GeradorSenhaPorta geradorSenhaPorta;

    private UsuarioCasoDeUso casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioCasoDeUso(repositorioPorta, codificadorSenhaPorta, geradorSenhaPorta);
        lenient().when(repositorioPorta.buscarPorCpf(CPF)).thenReturn(Optional.empty());
        lenient().when(repositorioPorta.buscarPorEmail(any())).thenReturn(Optional.empty());
        lenient().when(repositorioPorta.salvar(any(Colaborador.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveGerarSenhaInicialParaSupervisorEPersistirApenasOHash() {
        when(geradorSenhaPorta.gerar()).thenReturn(SENHA_GERADA);
        when(codificadorSenhaPorta.codificar(SENHA_GERADA)).thenReturn(HASH);

        UsuarioSaidaDTO saida = casoDeUso.cadastrarUsuario(
                new CadastrarUsuarioEntradaDTO("Ana Lima", CPF, NASCIMENTO, "ana@empresa.com", null, Perfil.SUPERVISOR));

        ArgumentCaptor<Colaborador> salvo = ArgumentCaptor.forClass(Colaborador.class);
        verify(repositorioPorta).salvar(salvo.capture());
        Supervisor supervisor = assertInstanceOf(Supervisor.class, salvo.getValue());
        assertEquals(HASH, supervisor.getSenha(), "o domínio deve carregar o hash, nunca a senha pura");
        assertNotEquals(SENHA_GERADA, supervisor.getSenha());
        assertEquals(SENHA_GERADA, saida.senhaInicial(), "a senha pura volta uma única vez, na resposta do cadastro");
    }

    @Test
    void deveIgnorarSenhaInformadaNaRequisicaoParaSupervisor() {
        when(geradorSenhaPorta.gerar()).thenReturn(SENHA_GERADA);
        when(codificadorSenhaPorta.codificar(SENHA_GERADA)).thenReturn(HASH);

        UsuarioSaidaDTO saida = casoDeUso.cadastrarUsuario(
                new CadastrarUsuarioEntradaDTO("Ana Lima", CPF, NASCIMENTO, "ana@empresa.com", "escolhida123", Perfil.SUPERVISOR));

        verify(codificadorSenhaPorta, never()).codificar("escolhida123");
        assertEquals(SENHA_GERADA, saida.senhaInicial());
    }

    @Test
    void naoDeveGerarNemCodificarSenhaParaColaborador() {
        UsuarioSaidaDTO saida = casoDeUso.cadastrarUsuario(
                new CadastrarUsuarioEntradaDTO("Carlos Souza", CPF, NASCIMENTO, null, null, Perfil.COLABORADOR));

        verify(geradorSenhaPorta, never()).gerar();
        verify(codificadorSenhaPorta, never()).codificar(any());
        assertNull(saida.senhaInicial());
    }

    @Test
    void gestorDeveUsarASenhaInformadaSemSenhaInicialNaResposta() {
        when(codificadorSenhaPorta.codificar("Segredo@123")).thenReturn(HASH);

        UsuarioSaidaDTO saida = casoDeUso.cadastrarUsuario(
                new CadastrarUsuarioEntradaDTO("Marina Costa", CPF, NASCIMENTO, "marina@empresa.com", "Segredo@123", Perfil.GESTOR_SEGURANCA));

        ArgumentCaptor<Colaborador> salvo = ArgumentCaptor.forClass(Colaborador.class);
        verify(repositorioPorta).salvar(salvo.capture());
        assertInstanceOf(GestorDeSeguranca.class, salvo.getValue());
        verify(geradorSenhaPorta, never()).gerar();
        assertNull(saida.senhaInicial());
    }

    @Test
    void listagemEBuscaNuncaDevemTrazerSenhaInicial() {
        Supervisor persistido = new Supervisor(1, CPF, "Ana Lima", NASCIMENTO, "ana@empresa.com", HASH, true, null, null);
        when(repositorioPorta.buscarPorId(1)).thenReturn(Optional.of(persistido));

        assertNull(casoDeUso.buscarPorId(1).senhaInicial());
    }
}
