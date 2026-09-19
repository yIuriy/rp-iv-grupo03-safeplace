package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "modelos_epi")
public class ModeloEpiEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private Integer ca;
    private String marca;
    @Column(name = "validade_ca", nullable = false)
    private LocalDate validadeCA;

    protected ModeloEpiEntity() { }
    public ModeloEpiEntity(Integer id, Integer ca, String marca, LocalDate validadeCA) {
        this.id = id; this.ca = ca; this.marca = marca; this.validadeCA = validadeCA;
    }
    public Integer getId() { return id; }
    public Integer getCa() { return ca; }
    public String getMarca() { return marca; }
    public LocalDate getValidadeCA() { return validadeCA; }
}
