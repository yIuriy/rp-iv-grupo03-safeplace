/*
 * Inspeção periódica de uma área de risco (RF09, US09, issue #134): data, área, Gestor de Segurança
 * que a registrou e ao menos um item verificado. Uma inspeção registrada não muda depois (US09, regra 3).
 */
package br.edu.safeplace.backend.domain.inspecao;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * O diagrama de classes liga {@code Inspecao} a {@code AreaRisco} e a exatamente um
 * {@code GestorDeSeguranca}. A área é guardada pelo id, como em {@code AreaRisco.setorId}. As
 * multiplicidades de {@code ItemVerificado} seguem a proposta de
 * {@code docs/arquitetura/correspondencia-classes-inspecao-rf09.md}, seção 2.3.
 */
public class Inspecao {

    private final Integer id;
    private final Integer areaRiscoId;
    private final LocalDate dataInspecao;
    private final GestorDeSeguranca responsavel;
    private final List<ItemVerificado> itens;

    /**
     * Reconstitui uma inspeção gravada, sem repetir a validação da data, que vale no dia do registro.
     */
    public Inspecao(Integer id, Integer areaRiscoId, LocalDate dataInspecao, GestorDeSeguranca responsavel,
            List<ItemVerificado> itens) {
        this.id = id;
        this.areaRiscoId = areaRiscoId;
        this.dataInspecao = dataInspecao;
        this.responsavel = responsavel;
        this.itens = itens != null ? List.copyOf(itens) : List.of();
    }

    /**
     * Registro de uma nova inspeção (US09, critério 1). Área, responsável, data e itens são
     * validados por InspecaoValidador antes de salvar (issue #263).
     */
    public static Inspecao registrar(Integer areaRiscoId, LocalDate dataInspecao, GestorDeSeguranca responsavel,
            List<ItemVerificado> itens) {
        return new Inspecao(null, areaRiscoId, dataInspecao, responsavel, itens);
    }

    public long contarNaoConformidades() {
        return itens.stream().filter(item -> !item.isConformidade()).count();
    }

    public Integer getId() {
        return id;
    }

    public Integer getAreaRiscoId() {
        return areaRiscoId;
    }

    public LocalDate getDataInspecao() {
        return dataInspecao;
    }

    public GestorDeSeguranca getResponsavel() {
        return responsavel;
    }

    public List<ItemVerificado> getItens() {
        return itens;
    }
}
