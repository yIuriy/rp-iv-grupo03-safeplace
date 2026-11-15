package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarColaboradorInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarSupervisorInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroColaboradorDTO;
import br.edu.safeplace.backend.application.dto.output.UsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.out.CodificadorSenha;
import br.edu.safeplace.backend.application.port.out.GeradorSenha;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Perfil;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobre os cadastros por papel de RF23 (issue #40), sem passar pelo endpoint genérico.
 */
class UsuarioServiceCadastroPorPapelTest {

    /** RNF05: a porta de auditoria é obrigatória nos casos de uso. */
    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();

    private static final String CPF_VALIDO = "52998224725";
    private static final String OUTRO_CPF_VALIDO = "11144477735";

    private UsuarioRepositorioFake repositorio;
    private UsuarioService casoDeUso;
    private boolean geradorDeSenhaFoiChamado;

    @BeforeEach
    void setUp() {
        repositorio = new UsuarioRepositorioFake();
        geradorDeSenhaFoiChamado = false;
        GeradorSenha gerador = () -> {
            geradorDeSenhaFoiChamado = true;
            return "SenhaGerada#1";
        };
        casoDeUso = new UsuarioService(repositorio, new CodificadorSenhaFake(), gerador, auditoria,
                new OperadorAutenticadoNulo());
    }

    /**
     * Hash previsível, para que o teste consiga afirmar que o que foi persistido não é a senha
     * em texto puro.
     */
    private static class CodificadorSenhaFake implements CodificadorSenha {
        @Override
        public String codificar(String senha) {
            return "hash:" + senha;
        }

        @Override
        public boolean validar(String senhaPura, String senhaCodificada) {
            return codificar(senhaPura).equals(senhaCodificada);
        }
    }

    private static CadastrarSupervisorInputDTO supervisor(String cpf, String email) {
        return new CadastrarSupervisorInputDTO("Joana Ribeiro", cpf, LocalDate.of(1988, 4, 20), email);
    }

    private static CadastrarColaboradorInputDTO colaborador(String nome, String cpf, String email) {
        return new CadastrarColaboradorInputDTO(nome, cpf, LocalDate.of(1992, 6, 18), email);
    }

    @Test
    @DisplayName("RF23: cadastro de Supervisor gera senha inicial e persiste apenas o hash")
    void cadastroDeSupervisorGeraSenhaInicialEPersisteHash() {
        UsuarioOutputDTO salvo = casoDeUso.cadastrar(supervisor(CPF_VALIDO, "joana@safeplace.com"));

        assertThat(salvo.perfil()).isEqualTo(Perfil.SUPERVISOR);
        assertThat(salvo.senhaInicial()).isEqualTo("SenhaGerada#1");

        Supervisor persistido = (Supervisor) repositorio.buscarPorCpf(CPF_VALIDO).orElseThrow();
        assertThat(persistido.getSenha()).isEqualTo("hash:SenhaGerada#1");
        assertThat(persistido.getSenha()).isNotEqualTo("SenhaGerada#1");
    }

    @Test
    @DisplayName("A senha inicial aparece só no cadastro; listagem e busca nunca a devolvem")
    void senhaInicialNaoVazaEmConsultas() {
        Integer id = casoDeUso.cadastrar(supervisor(CPF_VALIDO, "joana@safeplace.com")).id();

        assertThat(casoDeUso.buscarPorId(id).senhaInicial()).isNull();
        assertThat(casoDeUso.buscarPorCpf(CPF_VALIDO).senhaInicial()).isNull();
        assertThat(casoDeUso.listar()).allSatisfy(dto -> assertThat(dto.senhaInicial()).isNull());
    }

    @Test
    @DisplayName("RF23: cadastro de Colaborador não gera credenciais")
    void cadastroDeColaboradorNaoGeraCredenciais() {
        UsuarioOutputDTO salvo = casoDeUso.cadastrar(
                colaborador("João da Silva", CPF_VALIDO, "joao@safeplace.com"));

        assertThat(salvo.perfil()).isNull();
        assertThat(salvo.senhaInicial()).isNull();

        Colaborador persistido = repositorio.buscarPorCpf(CPF_VALIDO).orElseThrow();
        assertThat(persistido).isNotInstanceOf(Supervisor.class);
        assertThat(geradorDeSenhaFoiChamado).isFalse();
    }

    @Test
    @DisplayName("Colaborador pode ser cadastrado sem e-mail")
    void colaboradorPodeSerCadastradoSemEmail() {
        UsuarioOutputDTO salvo = casoDeUso.cadastrar(colaborador("Sem Email", CPF_VALIDO, null));

        assertThat(salvo.email()).isNull();
        assertThat(salvo.perfil()).isNull();
    }

    @Test
    @DisplayName("Duplicidade de CPF é rejeitada nos dois cadastros, inclusive com máscara")
    void duplicidadeDeCpfEhRejeitada() {
        casoDeUso.cadastrar(colaborador("João da Silva", CPF_VALIDO, "joao@safeplace.com"));

        assertThatThrownBy(() -> casoDeUso.cadastrar(
                colaborador("Outro Nome", "529.982.247-25", "outro@safeplace.com")))
                .isInstanceOf(CpfJaCadastradoException.class);

        assertThatThrownBy(() -> casoDeUso.cadastrar(
                supervisor(CPF_VALIDO, "novo.supervisor@safeplace.com")))
                .isInstanceOf(CpfJaCadastradoException.class);
    }

    @Test
    @DisplayName("Duplicidade de e-mail é rejeitada sem diferenciar maiúsculas")
    void duplicidadeDeEmailEhRejeitada() {
        casoDeUso.cadastrar(supervisor(CPF_VALIDO, "joana@safeplace.com"));

        assertThatThrownBy(() -> casoDeUso.cadastrar(
                colaborador("Outra Pessoa", OUTRO_CPF_VALIDO, "JOANA@SafePlace.com")))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    @DisplayName("CPF inválido é rejeitado antes de qualquer persistência")
    void cpfInvalidoEhRejeitado() {
        assertThatThrownBy(() -> casoDeUso.cadastrar(colaborador("X", "111", null)))
                .isInstanceOf(CpfInvalidoException.class);

        assertThat(repositorio.listar()).isEmpty();
    }

    @Test
    @DisplayName("Listagem de colaboradores não inclui supervisores")
    void listagemDeColaboradoresNaoIncluiSupervisores() {
        casoDeUso.cadastrar(colaborador("João da Silva", CPF_VALIDO, "joao@safeplace.com"));
        casoDeUso.cadastrar(supervisor(OUTRO_CPF_VALIDO, "joana@safeplace.com"));

        List<UsuarioOutputDTO> colaboradores = casoDeUso.listarColaboradores(FiltroColaboradorDTO.SEM_FILTRO);

        assertThat(colaboradores).extracting(UsuarioOutputDTO::perfil).containsOnlyNulls();
        assertThat(colaboradores).extracting(UsuarioOutputDTO::nome).containsExactly("João da Silva");
    }

    @Test
    @DisplayName("Filtro por nome é parcial e ignora maiúsculas; filtro por CPF aceita máscara")
    void filtrosDeListagemFuncionam() {
        casoDeUso.cadastrar(colaborador("João da Silva", CPF_VALIDO, "joao@safeplace.com"));
        casoDeUso.cadastrar(colaborador("Maria Souza", OUTRO_CPF_VALIDO, "maria@safeplace.com"));

        assertThat(casoDeUso.listarColaboradores(new FiltroColaboradorDTO("silva", null)))
                .extracting(UsuarioOutputDTO::nome).containsExactly("João da Silva");
        assertThat(casoDeUso.listarColaboradores(new FiltroColaboradorDTO(null, "529.982.247-25")))
                .extracting(UsuarioOutputDTO::nome).containsExactly("João da Silva");
        assertThat(casoDeUso.listarColaboradores(new FiltroColaboradorDTO("   ", "  ")))
                .hasSize(2);
        assertThat(casoDeUso.listarColaboradores(null)).hasSize(2);
    }

    /**
     * In-memory fake da porta de saída, como orienta a estratégia anti-bloqueio do cronograma.
     */
    private static class UsuarioRepositorioFake implements UsuarioRepository {
        private final List<Colaborador> armazenados = new ArrayList<>();
        private int proximoId = 1;

        @Override
        public Colaborador salvar(Colaborador colaborador) {
            Integer id = colaborador.getId() != null ? colaborador.getId() : proximoId++;
            Colaborador salvo = colaborador instanceof Supervisor s
                    ? new Supervisor(id, s.getCpf(), s.getNome(), s.getDataNascimento(), s.getEmail(),
                            s.getSenha(), s.isAtivo(), s.getCriadoEm(), LocalDateTime.now())
                    : new Colaborador(id, colaborador.getCpf(), colaborador.getNome(),
                            colaborador.getDataNascimento(), colaborador.getEmail(), colaborador.isAtivo(),
                            colaborador.getCriadoEm(), LocalDateTime.now());
            armazenados.add(salvo);
            return salvo;
        }

        @Override
        public Optional<Colaborador> buscarPorId(Integer id) {
            return armazenados.stream().filter(c -> c.getId().equals(id)).findFirst();
        }

        @Override
        public Optional<Colaborador> buscarPorCpf(String cpf) {
            return armazenados.stream().filter(c -> c.getCpf().equals(cpf)).findFirst();
        }

        @Override
        public Optional<Colaborador> buscarPorEmail(String email) {
            return armazenados.stream()
                    .filter(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public List<Colaborador> listar() {
            return List.copyOf(armazenados);
        }

        @Override
        public List<Colaborador> listarColaboradores(String nome, String cpf) {
            return armazenados.stream()
                    .filter(c -> c.getPerfil() == null)
                    .filter(c -> nome == null || c.getNome().toLowerCase(Locale.ROOT)
                            .contains(nome.toLowerCase(Locale.ROOT)))
                    .filter(c -> cpf == null || c.getCpf().equals(cpf))
                    .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                    .toList();
        }
    }
}
