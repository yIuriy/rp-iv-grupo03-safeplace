/*
 * Inspeção periódica de uma área de risco (RF09, US09, issue #134): data, área, Gestor de Segurança
 * que a registrou e ao menos um item verificado. Uma inspeção registrada não muda depois (US09, regra 3).
 */
package br.edu.safeplace.backend.domain.inspecao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/**
 * O diagrama de classes liga {@code Inspecao} a {@code AreaRisco} e a exatamente um
 * {@code GestorDeSeguranca}. As multiplicidades de {@code ItemVerificado} seguem a proposta de
 * {@code docs/arquitetura/correspondencia-classes-inspecao-rf09.md}, seção 2.3.
 */
@Entity
@Table(name = "inspecoes")
public class Inspecao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "area_risco_id", nullable = false)
    private AreaRisco areaRisco;

    @Column(name = "data_inspecao", nullable = false)
    private LocalDate dataInspecao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "gestor_id", nullable = false)
    private GestorDeSeguranca responsavel;

    @OneToMany(mappedBy = "inspecao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ItemVerificado> itens = new ArrayList<>();

    protected Inspecao() {
    }

    /**
     * Reconstitui uma inspeção gravada, sem repetir a validação da data, que vale no dia do registro.
     */
    public Inspecao(Integer id, AreaRisco areaRisco, LocalDate dataInspecao, GestorDeSeguranca responsavel,
            List<ItemVerificado> itens) {
        this.id = id;
        this.areaRisco = areaRisco;
        this.dataInspecao = dataInspecao;
        this.responsavel = responsavel;
        this.itens = new ArrayList<>();
        if (itens != null) {
            for (ItemVerificado item : itens) {
                if (item != null) {
                    item.vincularInspecao(this);
                }
                this.itens.add(item);
            }
        }
    }

    /**
     * Registro de uma nova inspeção (US09, critério 1). Área, responsável, data e itens são
     * validados por InspecaoValidador antes de salvar (issue #263).
     */
    public static Inspecao registrar(AreaRisco areaRisco, LocalDate dataInspecao, GestorDeSeguranca responsavel,
            List<ItemVerificado> itens) {
        return new Inspecao(null, areaRisco, dataInspecao, responsavel, itens);
    }

    public long contarNaoConformidades() {
        return itens.stream().filter(Objects::nonNull).filter(item -> !item.isConformidade()).count();
    }

    public Integer getId() {
        return id;
    }

    public AreaRisco getAreaRisco() {
        return areaRisco;
    }

    public LocalDate getDataInspecao() {
        return dataInspecao;
    }

    public GestorDeSeguranca getResponsavel() {
        return responsavel;
    }

    public List<ItemVerificado> getItens() {
        return itens != null ? Collections.unmodifiableList(itens) : List.of();
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Inspecao inspecao)) {
            return false;
        }
        return id != null && id.equals(inspecao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
