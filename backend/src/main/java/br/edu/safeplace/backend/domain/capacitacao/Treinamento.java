package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Treinamento {
    private final Integer id;
    private final String nome;
    private final String conteudo;
    private final String instrutor;
    private final LocalDate dataRealizacao;
    private final Integer cargaHoraria;
    private final Integer validadeMeses;
    private final List<Integer> colaboradoresIds;

    public Treinamento(Integer id, String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, List<Integer> colaboradoresIds) {
        this(id, nome, conteudo, instrutor, dataRealizacao, cargaHoraria, null, colaboradoresIds);
    }

    public Treinamento(Integer id, String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, Integer validadeMeses, List<Integer> colaboradoresIds) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do treinamento é obrigatório.");
        }
        if (dataRealizacao == null) {
            throw new IllegalArgumentException("Data de realização é obrigatória.");
        }
        if (dataRealizacao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de realização não pode ser no futuro.");
        }
        if (validadeMeses != null && validadeMeses < 1) {
            throw new IllegalArgumentException("Validade do treinamento deve ser de ao menos 1 mês.");
        }
        if (colaboradoresIds == null || colaboradoresIds.isEmpty()) {
            throw new IllegalArgumentException("Treinamento deve possuir ao menos um colaborador vinculado.");
        }

        this.id = id;
        this.nome = nome.trim();
        this.conteudo = conteudo;
        this.instrutor = instrutor;
        this.dataRealizacao = dataRealizacao;
        this.cargaHoraria = cargaHoraria;
        this.validadeMeses = validadeMeses;
        this.colaboradoresIds = new ArrayList<>(colaboradoresIds);
    }

    public static Treinamento cadastrar(String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, List<Integer> colaboradoresIds) {
        return cadastrar(nome, conteudo, instrutor, dataRealizacao, cargaHoraria, null, colaboradoresIds);
    }

    public static Treinamento cadastrar(String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, Integer validadeMeses, List<Integer> colaboradoresIds) {
        return new Treinamento(null, nome, conteudo, instrutor, dataRealizacao, cargaHoraria, validadeMeses, colaboradoresIds);
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getInstrutor() {
        return instrutor;
    }

    public LocalDate getDataRealizacao() {
        return dataRealizacao;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public Integer getValidadeMeses() {
        return validadeMeses;
    }

    /** Sem validade o treinamento não vence (RF10; #116). */
    public Optional<LocalDate> getDataVencimento() {
        return Optional.ofNullable(validadeMeses).map(dataRealizacao::plusMonths);
    }

    public StatusTreinamento calcularStatus(LocalDate dataReferencia) {
        if (dataRealizacao.isAfter(dataReferencia)) {
            return StatusTreinamento.PENDENTE;
        }
        Optional<LocalDate> vencimento = getDataVencimento();
        if (vencimento.isPresent()) {
            if (PrazoVencimento.estaVencido(dataReferencia, vencimento.get())) {
                return StatusTreinamento.VENCIDO;
            }
            if (PrazoVencimento.estaProximoDoVencimento(dataReferencia, vencimento.get())) {
                return StatusTreinamento.PROXIMO_DO_VENCIMENTO;
            }
        }
        return StatusTreinamento.REALIZADO;
    }

    public String verificarStatusTreinamentos(List<Treinamento> treinamentos) {
        if (treinamentos == null || treinamentos.isEmpty()) {
            return "PENDENTE";
        }
        LocalDate hoje = LocalDate.now();
        boolean todosRealizados = treinamentos.stream().allMatch(t -> t.calcularStatus(hoje).atendeExigencia());
        return todosRealizados ? "REALIZADO" : "PENDENTE";
    }

    public List<Integer> getColaboradoresIds() {
        return Collections.unmodifiableList(colaboradoresIds);
    }
}
