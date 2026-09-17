package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.AtualizarPessoaInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioSaidaDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenhaPorta;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #122: atualização cadastral por papel, sem banco. A persistência real é coberta em
 * {@code UsuarioIntegracaoTest}.
 */
class UsuarioCasoDeUsoAtualizacaoTest {

    private static final String CPF_SUPERVISORA = "52998224725";
    private static final String CPF_OUTRA_SUPERVISORA = "11144477735";
    private static final String CPF_COLABORADOR = "12345678909";

    private RepositorioEmMemoria repositorio;
    private UsuarioCasoDeUso casoDeUso;
    private Integer idSupervisora;
    private Integer idColaborador;

    @BeforeEach
    void setUp() {
        repositorio = new RepositorioEmMemoria();
        casoDeUso = new UsuarioCasoDeUso(repositorio, new CodificadorFake(), () -> "SenhaGerada#1");
        idSupervisora = casoDeUso.cadastrarSupervisor(new CadastrarSupervisorInputDTO(
                "Joana Ribeiro", CPF_SUPERVISORA, LocalDate.of(1988, 4, 20), "joana@safeplace.com")).id();
        idColaborador = casoDeUso.cadastrarColaborador(new CadastrarColaboradorInputDTO(
                "João da Silva", CPF_COLABORADOR, LocalDate.of(1992, 6, 18), null)).id();
    }

    private static AtualizarPessoaInputDTO dados(String nome, String email) {
        return new AtualizarPessoaInputDTO(nome, LocalDate.of(1990, 1, 10), email);
    }

    @Test
    @DisplayName("RF23: Gestor atualiza Supervisor preservando id, CPF, perfil e hash da senha")
    void atualizaSupervisorPreservandoIdentidadeECredencial() {
        UsuarioSaidaDTO saida = casoDeUso.atualizarSupervisor(idSupervisora,
                dados("Joana Ribeiro Souza", "joana.souza@safeplace.com"));

        assertThat(saida.id()).isEqualTo(idSupervisora);
        assertThat(saida.cpf()).isEqualTo(CPF_SUPERVISORA);
        assertThat(saida.perfil()).isEqualTo(Perfil.SUPERVISOR);
        assertThat(saida.nome()).isEqualTo("Joana Ribeiro Souza");
        assertThat(saida.email()).isEqualTo("joana.souza@safeplace.com");
        assertThat(saida.senhaInicial()).isNull();

        Supervisor persistida = (Supervisor) repositorio.buscarPorId(idSupervisora).orElseThrow();
        assertThat(persistida.getSenha()).isEqualTo("hash:SenhaGerada#1");
        assertThat(repositorio.listarTodos()).hasSize(2);
    }

    @Test
    @DisplayName("RF23: Colaborador atualizado continua sem credenciais e sem perfil de acesso")
    void atualizaColaboradorSemCriarCredenciais() {
        UsuarioSaidaDTO saida = casoDeUso.atualizarColaborador(idColaborador,
                dados("João Pedro da Silva", "joao@safeplace.com"));

        assertThat(saida.perfil()).isEqualTo(Perfil.COLABORADOR);
        assertThat(saida.senhaInicial()).isNull();
        Colaborador persistido = repositorio.buscarPorId(idColaborador).orElseThrow();
        assertThat(persistido).isExactlyInstanceOf(Colaborador.class);
        assertThat(persistido.getNome()).isEqualTo("João Pedro da Silva");
    }

    @Test
    @DisplayName("A rota de Supervisor não encontra um Colaborador: a atualização nunca muda o papel")
    void rotaDeSupervisorNaoAceitaColaborador() {
        assertThatThrownBy(() -> casoDeUso.atualizarSupervisor(idColaborador, dados("Outro", "outro@safeplace.com")))
                .isInstanceOf(UsuarioNaoEncontradoException.class)
                .hasMessageContaining("Supervisor");
        assertThatThrownBy(() -> casoDeUso.atualizarColaborador(idSupervisora, dados("Outro", null)))
                .isInstanceOf(UsuarioNaoEncontradoException.class)
                .hasMessageContaining("Colaborador");

        assertThat(repositorio.buscarPorId(idColaborador).orElseThrow().getNome()).isEqualTo("João da Silva");
        assertThat(repositorio.buscarPorId(idSupervisora).orElseThrow().getNome()).isEqualTo("Joana Ribeiro");
    }

    @Test
    @DisplayName("Id inexistente responde como não encontrado")
    void idInexistente() {
        assertThatThrownBy(() -> casoDeUso.atualizarColaborador(999, dados("Alguém", null)))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    @DisplayName("E-mail já usado por outra pessoa é recusado e nada muda")
    void emailDeOutraPessoaEhRecusado() {
        casoDeUso.cadastrarSupervisor(new CadastrarSupervisorInputDTO(
                "Marta Lima", CPF_OUTRA_SUPERVISORA, LocalDate.of(1985, 2, 2), "marta@safeplace.com"));

        assertThatThrownBy(() -> casoDeUso.atualizarSupervisor(idSupervisora, dados("Joana", "Marta@Safeplace.com")))
                .isInstanceOf(EmailJaCadastradoException.class);

        Colaborador intacta = repositorio.buscarPorId(idSupervisora).orElseThrow();
        assertThat(intacta.getNome()).isEqualTo("Joana Ribeiro");
        assertThat(intacta.getEmail()).isEqualTo("joana@safeplace.com");
    }

    @Test
    @DisplayName("Manter o próprio e-mail não conta como duplicidade")
    void manterOProprioEmailNaoEhDuplicidade() {
        UsuarioSaidaDTO saida = casoDeUso.atualizarSupervisor(idSupervisora, dados("Joana R. Souza", "joana@safeplace.com"));

        assertThat(saida.nome()).isEqualTo("Joana R. Souza");
        assertThat(saida.email()).isEqualTo("joana@safeplace.com");
    }

    private static class CodificadorFake implements CodificadorSenhaPorta {
        @Override
        public String codificar(String senha) {
            return "hash:" + senha;
        }

        @Override
        public boolean validar(String senhaPura, String senhaCodificada) {
            return codificar(senhaPura).equals(senhaCodificada);
        }
    }

    /** Guarda por id e substitui no lugar, como um banco faria em um UPDATE. */
    private static class RepositorioEmMemoria implements UsuarioRepositorioPorta {
        private final Map<Integer, Colaborador> porId = new LinkedHashMap<>();
        private int proximoId = 1;

        @Override
        public Colaborador salvar(Colaborador colaborador) {
            Integer id = colaborador.getId() != null ? colaborador.getId() : proximoId++;
            Colaborador salvo = comId(colaborador, id);
            porId.put(id, salvo);
            return salvo;
        }

        private static Colaborador comId(Colaborador c, Integer id) {
            if (c instanceof Supervisor s) {
                return new Supervisor(id, s.getCpf(), s.getNome(), s.getDataNascimento(), s.getEmail(),
                        s.getSenha(), s.isAtivo(), s.getCriadoEm(), s.getAtualizadoEm());
            }
            if (c instanceof GestorDeSeguranca g) {
                return new GestorDeSeguranca(id, g.getCpf(), g.getNome(), g.getDataNascimento(), g.getEmail(),
                        g.getSenha(), g.isAtivo(), g.getCriadoEm(), g.getAtualizadoEm());
            }
            return new Colaborador(id, c.getCpf(), c.getNome(), c.getDataNascimento(), c.getEmail(),
                    c.isAtivo(), c.getCriadoEm(), c.getAtualizadoEm());
        }

        @Override
        public Optional<Colaborador> buscarPorId(Integer id) {
            return Optional.ofNullable(porId.get(id));
        }

        @Override
        public Optional<Colaborador> buscarPorCpf(String cpf) {
            return porId.values().stream().filter(c -> c.getCpf().equals(cpf)).findFirst();
        }

        @Override
        public Optional<Colaborador> buscarPorEmail(String email) {
            return porId.values().stream()
                    .filter(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public List<Colaborador> listarTodos() {
            return List.copyOf(porId.values());
        }

        @Override
        public List<Colaborador> listarColaboradores(String nome, String cpf) {
            return porId.values().stream()
                    .filter(c -> c.getPerfil() == Perfil.COLABORADOR)
                    .filter(c -> nome == null || c.getNome().toLowerCase(Locale.ROOT).contains(nome.toLowerCase(Locale.ROOT)))
                    .filter(c -> cpf == null || c.getCpf().equals(cpf))
                    .toList();
        }
    }
}
