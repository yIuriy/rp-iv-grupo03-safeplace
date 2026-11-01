package br.edu.safeplace.backend.application.usecase;

import br.edu.safeplace.backend.application.port.out.OperadorAutenticado;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * Fachada de uso da aplicação para identificação e consulta do operador atualmente autenticado,
 * desacoplando os casos de uso dos detalhes de infraestrutura de segurança.
 */
@Service
public class OperadorAtual {

    public static final String SISTEMA = "SISTEMA";
    private static final String MENSAGEM_GESTOR_PADRAO = "Operação exige um Gestor de Segurança autenticado.";

    private final OperadorAutenticado operadorPorta;
    private final UsuarioRepository usuarioRepository;

    public OperadorAtual(OperadorAutenticado operadorPorta, UsuarioRepository usuarioRepository) {
        this.operadorPorta = Objects.requireNonNull(operadorPorta, "operadorPorta não pode ser nula.");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository não pode ser nulo.");
    }

    /**
     * Retorna o identificador (e-mail) do operador autenticado ou "SISTEMA" se anônimo/ausente.
     *
     * @return identificador do operador ou "SISTEMA".
     */
    public String emailOuSistema() {
        return operadorPorta.obterIdentificadorOperador().orElse(SISTEMA);
    }

    /**
     * Retorna o identificador do operador autenticado, se houver.
     *
     * @return Optional contendo o identificador ou vazio.
     */
    public Optional<String> obterIdentificadorOperador() {
        return operadorPorta.obterIdentificadorOperador();
    }

    /**
     * Busca o Gestor de Segurança autenticado, caso exista e possua perfil compatível.
     *
     * @return Optional com o Gestor de Segurança autenticado ou vazio.
     */
    public Optional<GestorDeSeguranca> buscarGestor() {
        return operadorPorta.obterIdentificadorOperador()
                .flatMap(usuarioRepository::buscarPorEmail)
                .filter(GestorDeSeguranca.class::isInstance)
                .map(GestorDeSeguranca.class::cast);
    }

    /**
     * Retorna o Gestor de Segurança autenticado ou lança AcessoNegadoException com mensagem padrão.
     *
     * @return GestorDeSeguranca autenticado.
     * @throws AcessoNegadoException se não houver operador autenticado como Gestor.
     */
    public GestorDeSeguranca gestorObrigatorio() {
        return gestorObrigatorio(MENSAGEM_GESTOR_PADRAO);
    }

    /**
     * Retorna o Gestor de Segurança autenticado ou lança AcessoNegadoException com mensagem informada.
     *
     * @param mensagemErro mensagem personalizada de erro caso não seja gestor autenticado.
     * @return GestorDeSeguranca autenticado.
     * @throws AcessoNegadoException se não houver operador autenticado como Gestor.
     */
    public GestorDeSeguranca gestorObrigatorio(String mensagemErro) {
        return buscarGestor()
                .orElseThrow(() -> new AcessoNegadoException(
                        mensagemErro != null && !mensagemErro.isBlank() ? mensagemErro : MENSAGEM_GESTOR_PADRAO
                ));
    }

    /**
     * Verifica se o operador autenticado possui o papel informado.
     *
     * @param papel nome da autoridade/papel.
     * @return true se possuir o papel, false caso contrário.
     */
    public boolean possuiPapel(String papel) {
        return operadorPorta.possuiPapel(papel);
    }
}
