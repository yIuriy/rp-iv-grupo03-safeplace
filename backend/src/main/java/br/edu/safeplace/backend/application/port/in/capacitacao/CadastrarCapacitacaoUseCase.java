package br.edu.safeplace.backend.application.port.in.capacitacao;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.ExigenciaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public interface CadastrarCapacitacaoUseCase {
    Certificacao cadastrarCertificacao(String nome, LocalDate dataVencimento, LocalDate dataEmissao, Integer colaboradorId);
    Treinamento cadastrarTreinamento(String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, List<Integer> colaboradoresIds);
    ExigenciaCapacitacao definirExigenciaTarefa(String nomeCapacitacao, ExigenciaCapacitacao.TipoCapacitacao tipo, Integer tarefaId);
    ExigenciaCapacitacao definirExigenciaEpi(String nomeCapacitacao, ExigenciaCapacitacao.TipoCapacitacao tipo, Integer epiId);
}
