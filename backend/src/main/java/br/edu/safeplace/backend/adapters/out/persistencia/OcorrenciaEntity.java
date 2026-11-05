package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
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

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();

    @Column(name = "status_ocorrencia", nullable = false, length = 30)
    private String statusOcorrencia = "ABERTA";

    @Column(name = "local")
    private String local;

    @Column(nullable = false)
    private String descricao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "area_risco_id", nullable = false)
    private AreaRisco area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private ColaboradorEntidade colaborador;

    /** Gestor de Segurança ou Supervisor que registrou o relato (issue #195). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relator_id")
    private ColaboradorEntidade relator;

    /**
     * Plano embutido no relato antigo (decisão 2.8 da correspondência do plano de ação), só para leitura.
     * Sem cascata: gravar a ocorrência não regrava a ação, que é mantida pelo plano de ação (issue #196).
     */
    @ManyToOne
    @JoinColumn(name = "plano_de_acao_id", insertable = false, updatable = false)
    private PlanoDeAcaoEntity planoDeAcao;

    protected OcorrenciaEntity() {
    }

    protected OcorrenciaEntity(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            String statusOcorrencia,
            AreaRisco area,
            ColaboradorEntidade colaborador,
            ColaboradorEntidade relator,
            String local,
            String descricao,
            PlanoDeAcaoEntity planoDeAcao) {
        this.idOcorrencia = idOcorrencia;
        this.dataOcorrencia = dataOcorrencia;
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
        this.statusOcorrencia = statusOcorrencia != null ? statusOcorrencia : "ABERTA";
        this.area = area;
        this.colaborador = colaborador;
        this.relator = relator;
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

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public String getStatusOcorrencia() {
        return statusOcorrencia;
    }

    public AreaRisco getArea() {
        return area;
    }

    public ColaboradorEntidade getColaborador() {
        return colaborador;
    }

    public ColaboradorEntidade getRelator() {
        return relator;
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
