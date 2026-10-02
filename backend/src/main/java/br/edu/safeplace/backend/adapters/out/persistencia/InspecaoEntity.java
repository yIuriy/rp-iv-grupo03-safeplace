/*
 * Entidade JPA da tabela inspecoes (V24, issue #134): área, data, Gestor de Segurança que registrou
 * e os itens verificados, gravados em cascata com a inspeção.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "inspecoes")
public class InspecaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "area_risco_id", nullable = false)
    private Integer areaRiscoId;

    @Column(name = "data_inspecao", nullable = false)
    private LocalDate dataInspecao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gestor_id", nullable = false)
    private GestorDeSegurancaEntidade gestor;

    @OneToMany(mappedBy = "inspecao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemVerificadoEntity> itens = new ArrayList<>();

    protected InspecaoEntity() {
    }

    public InspecaoEntity(Integer areaRiscoId, LocalDate dataInspecao, GestorDeSegurancaEntidade gestor) {
        this.areaRiscoId = areaRiscoId;
        this.dataInspecao = dataInspecao;
        this.gestor = gestor;
    }

    public void adicionarItem(String descricao, boolean conformidade, String observacoes) {
        itens.add(new ItemVerificadoEntity(this, descricao, conformidade, observacoes));
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

    public GestorDeSegurancaEntidade getGestor() {
        return gestor;
    }

    public List<ItemVerificadoEntity> getItens() {
        return itens;
    }
}
