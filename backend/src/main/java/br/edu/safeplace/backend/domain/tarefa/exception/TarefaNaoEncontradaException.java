package br.edu.safeplace.backend.domain.tarefa.exception;

public class TarefaNaoEncontradaException extends RuntimeException {

    public TarefaNaoEncontradaException(Integer id) {
        super("Tarefa não encontrada para o id " + id + ".");
    }
}
