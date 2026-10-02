package br.edu.safeplace.backend.application.dto.output;

import java.util.List;

public record ConsultaAptidaoOutputDTO(
        boolean apto,
        Integer tarefaId,
        String tarefaDescricao,
        Integer colaboradorId,
        String colaboradorNome,
        List<String> impedimentos
) {
    public static ConsultaAptidaoOutputDTO apto(Integer tarefaId, String tarefaDescricao,
                                                Integer colaboradorId, String colaboradorNome) {
        return new ConsultaAptidaoOutputDTO(true, tarefaId, tarefaDescricao, colaboradorId, colaboradorNome, List.of());
    }

    public static ConsultaAptidaoOutputDTO inapto(Integer tarefaId, String tarefaDescricao,
                                                  Integer colaboradorId, String colaboradorNome,
                                                  List<String> impedimentos) {
        return new ConsultaAptidaoOutputDTO(false, tarefaId, tarefaDescricao, colaboradorId, colaboradorNome,
                impedimentos != null ? List.copyOf(impedimentos) : List.of());
    }
}
