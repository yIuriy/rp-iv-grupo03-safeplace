package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planos_de_acao")
public class PlanoDeAcaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ocorrencia_id")
    private Integer ocorrenciaId;

    @Column(name = "medidas_corretivas")
    private String medidasCorretivas;

    private LocalDate prazo;
    private String status;

    @Column(name = "medidas_preventivas")
    private String medidasPreventivas;

    @Column(name = "colaborador_id")
    private Integer colaboradorId;

    @Column(name = "setor_id")
    private Integer setorId;

    @Column(name = "gestor_id")
    private Integer gestorId;

    @Column(name = "alerta_ativo", nullable = false)
    private boolean alertaAtivo;

    @Column(name = "antecedencia_alerta_dias")
    private Integer antecedenciaAlertaDias;

    protected PlanoDeAcaoEntity() {
    }

    public PlanoDeAcaoEntity(
            Integer id,
            String medidasCorretivas,
            LocalDate prazo,
            String status,
            String medidasPreventivas) {
        this(id, null, medidasCorretivas, prazo, status, medidasPreventivas, null, null, null, false, null);
    }

    public PlanoDeAcaoEntity(
            Integer id,
            Integer ocorrenciaId,
            String medidasCorretivas,
            LocalDate prazo,
            String status,
            String medidasPreventivas,
            Integer colaboradorId,
            Integer setorId,
            Integer gestorId,
            boolean alertaAtivo,
            Integer antecedenciaAlertaDias) {
        this.id = id;
        this.ocorrenciaId = ocorrenciaId;
        this.medidasCorretivas = medidasCorretivas;
        this.prazo = prazo;
        this.status = status;
        this.medidasPreventivas = medidasPreventivas;
        this.colaboradorId = colaboradorId;
        this.setorId = setorId;
        this.gestorId = gestorId;
        this.alertaAtivo = alertaAtivo;
        this.antecedenciaAlertaDias = alertaAtivo ? antecedenciaAlertaDias : null;
    }

    public Integer getId() {
        return id;
    }

    public Integer getOcorrenciaId() {
        return ocorrenciaId;
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

    public Integer getColaboradorId() {
        return colaboradorId;
    }

    public Integer getSetorId() {
        return setorId;
    }

    public Integer getGestorId() {
        return gestorId;
    }

    public boolean isAlertaAtivo() {
        return alertaAtivo;
    }

    public Integer getAntecedenciaAlertaDias() {
        return antecedenciaAlertaDias;
    }
}