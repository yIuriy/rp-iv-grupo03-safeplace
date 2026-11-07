package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;

/**
 * Facade da trilha de auditoria (RNF05, #223). Os casos de uso registram um evento numa chamada
 * ({@code cadastro}, {@code alteracao} ou {@code remocao}) sem montar o {@link LogAuditoria}, resolver o
 * operador nem chamar a porta de persistência.
 *
 * <p>Por padrão o responsável é o operador autenticado (ou {@code SISTEMA}). {@link #por(String)} usa o
 * e-mail informado pela borda e {@link Autoria#em(LocalDateTime)} fixa o instante do evento.
 */
@Service
public class RegistradorAuditoria {

    private final LogAuditoriaRepository repository;
    private final OperadorAtual operadorAtual;

    public RegistradorAuditoria(LogAuditoriaRepository repository, OperadorAtual operadorAtual) {
        this.repository = Objects.requireNonNull(repository, "Porta de auditoria é obrigatória (RNF05).");
        this.operadorAtual = Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
    }

    /** Construtor para testes sem OperadorAtual explícito (assume Null Object OperadorAutenticadoNulo). */
    public RegistradorAuditoria(LogAuditoriaRepository repository) {
        this(repository, new OperadorAtual(
                new br.edu.safeplace.backend.adapters.out.seguranca.OperadorAutenticadoNulo(),
                new br.edu.safeplace.backend.application.port.out.UsuarioRepository() {
                    @Override public java.util.Optional<br.edu.safeplace.backend.domain.usuario.Colaborador> buscarPorEmail(String email) { return java.util.Optional.empty(); }
                    @Override public java.util.Optional<br.edu.safeplace.backend.domain.usuario.Colaborador> buscarPorId(Integer id) { return java.util.Optional.empty(); }
                    @Override public java.util.Optional<br.edu.safeplace.backend.domain.usuario.Colaborador> buscarPorCpf(String cpf) { return java.util.Optional.empty(); }
                    @Override public java.util.List<br.edu.safeplace.backend.domain.usuario.Colaborador> listarTodos() { return java.util.List.of(); }
                    @Override public java.util.List<br.edu.safeplace.backend.domain.usuario.Colaborador> listarColaboradores(String nome, String cpf) { return java.util.List.of(); }
                    @Override public br.edu.safeplace.backend.domain.usuario.Colaborador salvar(br.edu.safeplace.backend.domain.usuario.Colaborador colaborador) { return colaborador; }
                }
        ));
    }

    public void cadastro(String entidade, Integer entidadeId, String justificativa) {
        doOperadorAtual().cadastro(entidade, entidadeId, justificativa);
    }

    public void alteracao(String entidade, Integer entidadeId, String justificativa) {
        doOperadorAtual().alteracao(entidade, entidadeId, justificativa);
    }

    public void remocao(String entidade, Integer entidadeId, String justificativa) {
        doOperadorAtual().remocao(entidade, entidadeId, justificativa);
    }

    public void operacao(String tipoOperacao, String entidade, Integer entidadeId, String justificativa) {
        doOperadorAtual().operacao(tipoOperacao, entidade, entidadeId, justificativa);
    }

    /** Registra em nome do e-mail informado pela borda; sem ele, vale o operador autenticado ou SISTEMA. */
    public Autoria por(String operadorInformado) {
        return new Autoria(operadorAtual.emailInformadoOuSistema(operadorInformado), null);
    }

    private Autoria doOperadorAtual() {
        return new Autoria(operadorAtual.emailOuSistema(), null);
    }

    /** Responsável e instante já resolvidos de um registro; sem instante, vale o momento da gravação. */
    public final class Autoria {

        private final String responsavel;
        private final LocalDateTime dataHora;

        private Autoria(String responsavel, LocalDateTime dataHora) {
            this.responsavel = responsavel;
            this.dataHora = dataHora;
        }

        public Autoria em(LocalDateTime instante) {
            return new Autoria(responsavel, instante);
        }

        public void cadastro(String entidade, Integer entidadeId, String justificativa) {
            gravar(LogAuditoria.CADASTRO, entidade, entidadeId, justificativa);
        }

        public void alteracao(String entidade, Integer entidadeId, String justificativa) {
            gravar(LogAuditoria.ALTERACAO, entidade, entidadeId, justificativa);
        }

        public void remocao(String entidade, Integer entidadeId, String justificativa) {
            gravar(LogAuditoria.REMOCAO, entidade, entidadeId, justificativa);
        }

        public void operacao(String tipoOperacao, String entidade, Integer entidadeId, String justificativa) {
            gravar(tipoOperacao, entidade, entidadeId, justificativa);
        }

        private void gravar(String tipoOperacao, String entidade, Integer entidadeId, String justificativa) {
            repository.salvar(new LogAuditoria(
                    null, tipoOperacao, dataHora, responsavel, justificativa, entidade, entidadeId));
        }
    }
}
