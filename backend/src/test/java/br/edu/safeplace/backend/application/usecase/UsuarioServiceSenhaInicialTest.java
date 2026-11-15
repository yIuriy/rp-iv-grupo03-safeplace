package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import br.edu.safeplace.backend.application.dto.input.CadastrarUsuarioInputDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GeradorSenha;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
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
class UsuarioServiceSenhaInicialTest {

    /** RNF05: a porta de auditoria é obrigatória nos casos de uso. */
    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();

    private static final String CPF = "52998224725";
    private static final LocalDate NASCIMENTO = LocalDate.of(1985, 7, 20);
    private static final String SENHA_GERADA = "Xk7!pQ2#mA9z";
    private static final String HASH = "$2a$10$hashDaSenhaGerada";

    @Mock
    private UsuarioRepository repositorioPorta;
    @Mock
    private CodificadorSenha codificadorSenha;
    @Mock
    private GeradorSenha geradorSenha;

    private UsuarioService casoDeUso;

    @BeforeEach
    void setUp() {
        casoDeUso = new UsuarioService(repositorioPorta, codificadorSenha, geradorSenha, auditoria,
                new OperadorAutenticadoNulo());
        lenient().when(repositorioPorta.buscarPorCpf(CPF)).thenReturn(Optional.empty());
        lenient().when(repositorioPorta.buscarPorEmail(any())).thenReturn(Optional.empty());
        lenient().when(repositorioPorta.salvar(any(Colaborador.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void deveGerarSenhaInicialParaSupervisorEPersistirApenasOHash() {
        when(geradorSenha.gerar()).thenReturn(SENHA_GERADA);
        when(codificadorSenha.codificar(SENHA_GERADA)).thenReturn(HASH);

        UsuarioOutputDTO saida = casoDeUso.cadastrar(
                new CadastrarUsuarioInputDTO("Ana Lima", CPF, NASCIMENTO, "ana@empresa.com", null, Perfil.SUPERVISOR));

        ArgumentCaptor<Colaborador> salvo = ArgumentCaptor.forClass(Colaborador.class);
        verify(repositorioPorta).salvar(salvo.capture());
        Supervisor supervisor = assertInstanceOf(Supervisor.class, salvo.getValue());
        assertEquals(HASH, supervisor.getSenha(), "o domínio deve carregar o hash, nunca a senha pura");
        assertNotEquals(SENHA_GERADA, supervisor.getSenha());
        assertEquals(SENHA_GERADA, saida.senhaInicial(), "a senha pura volta uma única vez, na resposta do cadastro");
    }

    @Test
    void deveIgnorarSenhaInformadaNaRequisicaoParaSupervisor() {
        when(geradorSenha.gerar()).thenReturn(SENHA_GERADA);
        when(codificadorSenha.codificar(SENHA_GERADA)).thenReturn(HASH);

        UsuarioOutputDTO saida = casoDeUso.cadastrar(
                new CadastrarUsuarioInputDTO("Ana Lima", CPF, NASCIMENTO, "ana@empresa.com", "escolhida123", Perfil.SUPERVISOR));

        verify(codificadorSenha, never()).codificar("escolhida123");
        assertEquals(SENHA_GERADA, saida.senhaInicial());
    }

    @Test
    void naoDeveGerarNemCodificarSenhaParaColaborador() {
        UsuarioOutputDTO saida = casoDeUso.cadastrar(
                new CadastrarUsuarioInputDTO("Carlos Souza", CPF, NASCIMENTO, null, null, null));

        verify(geradorSenha, never()).gerar();
        verify(codificadorSenha, never()).codificar(any());
        assertNull(saida.senhaInicial());
    }

    @Test
    void gestorDeveUsarASenhaInformadaSemSenhaInicialNaResposta() {
        when(codificadorSenha.codificar("Segredo@123")).thenReturn(HASH);

        UsuarioOutputDTO saida = casoDeUso.cadastrar(
                new CadastrarUsuarioInputDTO("Marina Costa", CPF, NASCIMENTO, "marina@empresa.com", "Segredo@123", Perfil.GESTOR_SEGURANCA));

        ArgumentCaptor<Colaborador> salvo = ArgumentCaptor.forClass(Colaborador.class);
        verify(repositorioPorta).salvar(salvo.capture());
        assertInstanceOf(GestorDeSeguranca.class, salvo.getValue());
        verify(geradorSenha, never()).gerar();
        assertNull(saida.senhaInicial());
    }

    @Test
    void listagemEBuscaNuncaDevemTrazerSenhaInicial() {
        Supervisor persistido = new Supervisor(1, CPF, "Ana Lima", NASCIMENTO, "ana@empresa.com", HASH, true, null, null);
        when(repositorioPorta.buscarPorId(1)).thenReturn(Optional.of(persistido));

        assertNull(casoDeUso.buscarPorId(1).senhaInicial());
    }
}
