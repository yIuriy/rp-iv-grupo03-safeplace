package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Treinamento {
    private final Integer id;
    private final String nome;
    private final String conteudo;
    private final String instrutor;
    private final LocalDate dataRealizacao;
    private final Integer cargaHoraria;
    private final List<Integer> colaboradoresIds;

    public Treinamento(Integer id, String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, List<Integer> colaboradoresIds) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do treinamento é obrigatório.");
        }
        if (dataRealizacao == null) {
            throw new IllegalArgumentException("Data de realização é obrigatória.");
        }
        if (dataRealizacao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de realização não pode ser no futuro.");
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
        this.colaboradoresIds = new ArrayList<>(colaboradoresIds);
    }

    public static Treinamento cadastrar(String nome, String conteudo, String instrutor, LocalDate dataRealizacao, Integer cargaHoraria, List<Integer> colaboradoresIds) {
        return new Treinamento(null, nome, conteudo, instrutor, dataRealizacao, cargaHoraria, colaboradoresIds);
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

    public List<Integer> getColaboradoresIds() {
        return Collections.unmodifiableList(colaboradoresIds);
    }
}
