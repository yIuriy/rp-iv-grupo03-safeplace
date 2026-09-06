package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "planos_de_acao")
public class PlanoDeAcaoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "medidas_corretivas")
    private String medidasCorretivas;

    private LocalDate prazo;
    private String status;

    @Column(name = "medidas_preventivas")
    private String medidasPreventivas;

    protected PlanoDeAcaoEntity() {
    }

    public PlanoDeAcaoEntity(Integer id, String medidasCorretivas, LocalDate prazo, String status,
            String medidasPreventivas) {
        this.id = id;
        this.medidasCorretivas = medidasCorretivas;
        this.prazo = prazo;
        this.status = status;
        this.medidasPreventivas = medidasPreventivas;
    }

    public Integer getId() {
        return id;
    }

    public String getMedidasCorretivas() {
        return medidasCorretivas;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public String getStatus() {
        return status;
    }

    public String getMedidasPreventivas() {
        return medidasPreventivas;
    }
}