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

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro = LocalDateTime.now();

    @Column(name = "status_ocorrencia", nullable = false, length = 30)
    private String statusOcorrencia = "ABERTA";

    @Column(name = "local")
    private String local;

    @Column(nullable = false)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_risco_id")
    private AreaRiscoEntity area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private ColaboradorEntidade colaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id")
    private GestorDeSegurancaEntidade gestor;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "plano_de_acao_id")
    private PlanoDeAcaoEntity planoDeAcao;

    protected OcorrenciaEntity() {
    }

    protected OcorrenciaEntity(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            String statusOcorrencia,
            AreaRiscoEntity area,
            ColaboradorEntidade colaborador,
            GestorDeSegurancaEntidade gestor,
            String local,
            String descricao,
            PlanoDeAcaoEntity planoDeAcao) {
        this.idOcorrencia = idOcorrencia;
        this.dataOcorrencia = dataOcorrencia;
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
        this.statusOcorrencia = statusOcorrencia != null ? statusOcorrencia : "ABERTA";
        this.area = area;
        this.colaborador = colaborador;
        this.gestor = gestor;
        this.local = local;
        this.descricao = descricao;
        this.planoDeAcao = planoDeAcao;
    }

    protected OcorrenciaEntity(Integer idOcorrencia, LocalDateTime dataOcorrencia, String local,
            String descricao, PlanoDeAcaoEntity planoDeAcao) {
        this(idOcorrencia, dataOcorrencia, LocalDateTime.now(), "ABERTA", null, null, null, local, descricao, planoDeAcao);
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

    public AreaRiscoEntity getArea() {
        return area;
    }

    public ColaboradorEntidade getColaborador() {
        return colaborador;
    }

    public GestorDeSegurancaEntidade getGestor() {
        return gestor;
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