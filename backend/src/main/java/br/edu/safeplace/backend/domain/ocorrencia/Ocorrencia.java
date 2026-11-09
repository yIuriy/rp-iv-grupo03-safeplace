package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.Objects;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Relato de acidente ou incidente (UC05, UC09). É a raiz da hierarquia de ocorrências,
 * mapeada com JPA JOINED (#254, #261) para as tabelas {@code ocorrencias}, {@code acidentes} e {@code incidentes}.
 */
@Entity
@Table(name = "ocorrencias")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ocorrencia")
    private Integer idOcorrencia;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "data_ocorrencia", nullable = false)
    private LocalDateTime dataOcorrencia;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_ocorrencia", nullable = false, length = 30)
    private StatusOcorrencia statusOcorrencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_risco_id", nullable = false)
    private AreaRisco area;

    /**
     * Usuário autenticado que registrou o relato, Gestor de Segurança ou Supervisor (RF13, UC09).
     * Definido só pelo servidor (issue #195); nunca vem da requisição.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relator_id")
    private Colaborador relator;

    /**
     * Texto livre herdado da modelagem anterior, mantido apenas como rótulo descritivo dos
     * registros legados. A localização estrutural do fato é dada exclusivamente pela associação
     * {@code area : AreaRisco}.
     */
    @Column(name = "local")
    private String localInformado;

    /** Preserva a coluna legada no banco sem cascata de gravação (issue #196). */
    @Column(name = "plano_de_acao_id", insertable = false, updatable = false)
    private Integer planoDeAcaoId;

    @Transient
    private PlanoDeAcao planoDeAcao;

    /** Exclusivo do JPA (decisão #254). */
    protected Ocorrencia() {
    }

    protected Ocorrencia(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            PlanoDeAcao planoDeAcao) {
        this(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                colaborador, area, relator, null, planoDeAcao);
    }

    protected Ocorrencia(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            String local,
            PlanoDeAcao planoDeAcao) {

        // Data e descrição são validadas por OcorrenciaValidador antes de salvar (issue #266).
        this.idOcorrencia = idOcorrencia;
        this.descricao = descricao != null ? descricao.trim() : null;
        this.dataOcorrencia = dataOcorrencia;
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
        this.statusOcorrencia = statusOcorrencia != null ? statusOcorrencia : StatusOcorrencia.ABERTA;
        this.colaborador = colaborador;
        this.area = area;
        this.relator = relator;
        this.localInformado = local != null && !local.isBlank() ? local.trim() : null;
        this.planoDeAcao = planoDeAcao;
        this.planoDeAcaoId = planoDeAcao != null ? planoDeAcao.getId() : null;
    }

    public void transicionarPara(StatusOcorrencia novoStatus) {
        this.statusOcorrencia.validarTransicao(novoStatus);
        this.statusOcorrencia = novoStatus;
    }

    public void enviarParaTriagem() {
        transicionarPara(StatusOcorrencia.EM_TRIAGEM);
    }

    public void arquivar() {
        transicionarPara(StatusOcorrencia.ARQUIVADA);
    }

    /**
     * Indica se a ocorrência é grave para a regra 1 de UC07 (exige ação preventiva no plano). Só há
     * escala aprovada em {@code Acidente.gravidade}; {@code Incidente.potencialDano} é texto livre e
     * não é convertido em gravidade por suposição, então a regra não se aplica automaticamente a
     * incidentes (decisão 2.4 da correspondência do plano de ação).
     */
    public boolean isGrave() {
        return false;
    }

    /** Devolve o tipo específico de ocorrência no domínio (#239). */
    public abstract TipoOcorrencia getTipoOcorrencia();

    /**
     * Nível de perigo da ocorrência quando aplicável (subclasses com gravidade, como {@link Acidente}).
     * Devolve {@code null} quando o tipo não possui gravidade estruturada (#239).
     */
    public NivelPerigo getGravidade() {
        return null;
    }

    public Integer getId() {
        return idOcorrencia;
    }

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataOcorrencia() {
        return dataOcorrencia;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public StatusOcorrencia getStatusOcorrencia() {
        return statusOcorrencia;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public AreaRisco getArea() {
        return area;
    }

    public Colaborador getRelator() {
        return relator;
    }

    /**
     * Nome do lugar do fato: vem da área de risco associada e, apenas quando não há área, recai
     * sobre o texto livre legado. Nunca se constrói uma {@link AreaRisco} a partir desse texto.
     */
    public String getLocal() {
        return area != null ? area.getNome() : localInformado;
    }

    /**
     * Texto livre legado exatamente como foi informado, sem o fallback para o nome da área.
     */
    public String getLocalInformado() {
        return localInformado;
    }

    public PlanoDeAcao getPlanoDeAcao() {
        return planoDeAcao;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Ocorrencia that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
