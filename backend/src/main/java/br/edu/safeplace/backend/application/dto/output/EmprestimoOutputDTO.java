/*
 * Saída de um empréstimo para a camada de interface: identifica EPI e colaborador (com nomes, para
 * a consulta do UC12 mostrar quem está com cada equipamento), datas, responsáveis, estado na
 * devolução, termo de cautela e a classificação calculada na data da consulta.
 */
package br.edu.safeplace.backend.application.dto.output;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;

public record EmprestimoOutputDTO(
        Integer id,
        Integer epiId,
        String epiNome,
        Integer colaboradorId,
        String colaboradorNome,
        Integer responsavelEntregaId,
        LocalDateTime dataRetirada,
        LocalDate dataDevolucaoPrevista,
        LocalDateTime dataDevolucaoReal,
        Integer responsavelDevolucaoId,
        EstadoDevolucao estadoDevolucao,
        ClassificacaoEmprestimo status,
        boolean atrasado,
        String termoCautela,
        String justificativaSubstituicao,
        String destinoAprovado,
        Integer saldoPersistido) {

    public EmprestimoOutputDTO(
            Integer id,
            Integer epiId,
            String epiNome,
            Integer colaboradorId,
            String colaboradorNome,
            Integer responsavelEntregaId,
            LocalDateTime dataRetirada,
            LocalDate dataDevolucaoPrevista,
            LocalDateTime dataDevolucaoReal,
            Integer responsavelDevolucaoId,
            EstadoDevolucao estadoDevolucao,
            ClassificacaoEmprestimo status,
            boolean atrasado,
            String termoCautela,
            String justificativaSubstituicao) {
        this(id, epiId, epiNome, colaboradorId, colaboradorNome, responsavelEntregaId, dataRetirada,
                dataDevolucaoPrevista, dataDevolucaoReal, responsavelDevolucaoId, estadoDevolucao,
                status, atrasado, termoCautela, justificativaSubstituicao, null, null);
    }

    public static EmprestimoOutputDTO deDominio(Emprestimo emprestimo, String epiNome, String colaboradorNome,
                                                LocalDate hoje) {
        return deDominio(emprestimo, epiNome, colaboradorNome, hoje, null, null);
    }

    public static EmprestimoOutputDTO deDominio(Emprestimo emprestimo, String epiNome, String colaboradorNome,
                                                LocalDate hoje, String destinoAprovado, Integer saldoPersistido) {
        return new EmprestimoOutputDTO(
                emprestimo.getId(),
                emprestimo.getEpiId(),
                epiNome,
                emprestimo.getColaboradorId(),
                colaboradorNome,
                emprestimo.getResponsavelEntregaId(),
                emprestimo.getDataRetirada(),
                emprestimo.getDataDevolucaoPrevista(),
                emprestimo.getDataDevolucaoReal(),
                emprestimo.getResponsavelDevolucaoId(),
                emprestimo.getEstadoDevolucao(),
                emprestimo.classificarEm(hoje),
                emprestimo.estaAtrasadoEm(hoje),
                emprestimo.getTermoCautela(),
                emprestimo.getJustificativaSubstituicao(),
                destinoAprovado,
                saldoPersistido);
    }
}
