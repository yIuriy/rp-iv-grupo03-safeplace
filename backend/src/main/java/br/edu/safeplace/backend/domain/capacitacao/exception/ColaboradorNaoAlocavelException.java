package br.edu.safeplace.backend.domain.capacitacao.exception;

/**
 * Exceção de domínio lançada quando se tenta alocar em tarefa uma pessoa que não é colaborador
 * (Gestor de Segurança ou Supervisor), conforme UC02 e UC11.
 */
public class ColaboradorNaoAlocavelException extends RuntimeException {

    public ColaboradorNaoAlocavelException(String nome) {
        super("Somente colaboradores podem ser alocados em tarefas: " + nome + " não possui o perfil de colaborador.");
    }
}
