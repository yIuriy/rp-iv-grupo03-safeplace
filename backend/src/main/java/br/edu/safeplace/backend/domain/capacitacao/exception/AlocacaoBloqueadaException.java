package br.edu.safeplace.backend.domain.capacitacao.exception;

import java.util.Collections;
import java.util.List;

/**
 * Exceção de domínio lançada quando uma alocação de colaborador em tarefa é bloqueada
 * por pendências de capacitação (certificações obrigatórias ausentes/vencidas ou
 * treinamentos obrigatórios pendentes/não realizados), conforme RF10, UC02 e UC11.
 */
public class AlocacaoBloqueadaException extends RuntimeException {

    private final List<String> impedimentos;

    public AlocacaoBloqueadaException(String mensagem, List<String> impedimentos) {
        super(mensagem);
        this.impedimentos = impedimentos != null ? List.copyOf(impedimentos) : Collections.emptyList();
    }

    public AlocacaoBloqueadaException(List<String> impedimentos) {
        this("Alocação bloqueada por pendências normativas de capacitação: " + String.join("; ", impedimentos) + ".",
                impedimentos);
    }

    public List<String> getImpedimentos() {
        return impedimentos;
    }
}
