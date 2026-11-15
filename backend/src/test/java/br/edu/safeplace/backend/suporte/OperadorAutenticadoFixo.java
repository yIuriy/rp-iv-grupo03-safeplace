package br.edu.safeplace.backend.suporte;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.usecase.OperadorAtual;

/**
 * Dublê da porta OperadorAutenticado (Adapter, issue #236). Substitui a montagem do
 * SecurityContextHolder nos testes dos casos de uso: o teste diz quem opera e com quais papéis, e o
 * caso de uso lê o operador pela porta, como em produção. Começa sem sessão; {@link #autenticar}
 * e {@link #encerrarSessao} trocam o operador no meio do teste.
 */
public final class OperadorAutenticadoFixo implements OperadorAutenticado {

    private static final String PREFIXO_PAPEL = "ROLE_";

    private String identificador;
    private Set<String> papeis = Set.of();

    /** Ninguém autenticado: sem identificador e sem papel, como a sessão anônima. */
    public static OperadorAutenticadoFixo semSessao() {
        return new OperadorAutenticadoFixo();
    }

    /** Operador autenticado com o e-mail e os papéis informados ("SUPERVISOR" ou "ROLE_SUPERVISOR"). */
    public static OperadorAutenticadoFixo autenticado(String email, String... papeis) {
        OperadorAutenticadoFixo operador = new OperadorAutenticadoFixo();
        operador.autenticar(email, papeis);
        return operador;
    }

    /** Fachada de uso montada sobre este dublê, para injetar nos casos de uso. */
    public OperadorAtual operadorAtual(UsuarioRepository usuarioRepository) {
        return new OperadorAtual(this, usuarioRepository);
    }

    public void autenticar(String email, String... papeis) {
        this.identificador = email;
        this.papeis = Arrays.stream(papeis).map(OperadorAutenticadoFixo::normalizar).collect(Collectors.toSet());
    }

    public void encerrarSessao() {
        this.identificador = null;
        this.papeis = Set.of();
    }

    @Override
    public Optional<String> obterIdentificador() {
        return Optional.ofNullable(identificador).filter(email -> !email.isBlank());
    }

    @Override
    public boolean possuiPapel(String papel) {
        return papel != null && !papel.isBlank() && papeis.contains(normalizar(papel));
    }

    private static String normalizar(String papel) {
        return papel.startsWith(PREFIXO_PAPEL) ? papel : PREFIXO_PAPEL + papel;
    }
}
