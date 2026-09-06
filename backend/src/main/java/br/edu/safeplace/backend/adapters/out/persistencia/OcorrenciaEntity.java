package br.edu.safeplace.backend.adapters.out.persistencia;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencias")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class OcorrenciaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ocorrencia")
    private Integer idOcorrencia;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDateTime dataOcorrencia;

    @Column(nullable = false)
    private String local;

    @Column(nullable = false)
    private String descricao;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plano_de_acao_id")
    private PlanoDeAcaoEntity planoDeAcao;

    protected OcorrenciaEntity() {
    }

    protected OcorrenciaEntity(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local,
            String descricao, PlanoDeAcaoEntity planoDeAcao) {
        this.idOcorrencia = idOcorrencia;
        this.dataOcorrencia = dataOcorrencia;
        this.local = local;
        this.descricao = descricao;
        this.planoDeAcao = planoDeAcao;
    }

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public LocalDateTime getDataOcorrencia() {
        return dataOcorrencia;
    }

    public String getLocal() {
        return local;
    }

    public String getDescricao() {
        return descricao;
    }

    public PlanoDeAcaoEntity getPlanoDeAcao() {
        return planoDeAcao;
    }
}