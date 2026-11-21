package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.hibernate.annotations.Immutable;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Aviso interno sobre uma ação do plano, entregue ao Gestor de Segurança (UC07, regra 2 e cenário
 * alternativo II; decisão 2.6 da correspondência do plano de ação) e entidade JPA imutável da tabela
 * {@code notificacoes_acao} (#262, decisão #254).
 *
 * <p>{@code responsavelTipo} e {@code responsavelId} permanecem como referência polimórfica porque o
 * destino pode ser {@code usuarios} (COLABORADOR) ou {@code setores} (SETOR), sem tabela única.</p>
 */
@Entity
@Table(name = "notificacoes_acao")
@Immutable
public class NotificacaoAcao {

    public static final String RESPONSAVEL_COLABORADOR = "COLABORADOR";
    public static final String RESPONSAVEL_SETOR = "SETOR";

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acao_id", nullable = false)
    private PlanoDeAcao acao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id")
    private Ocorrencia ocorrencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoAvisoAcao motivo;

    @Column(name = "responsavel_tipo", nullable = false, length = 20)
    private String responsavelTipo;

    @Column(name = "responsavel_id", nullable = false)
    private Integer responsavelId;

    @Column(name = "responsavel_nome", length = 160)
    private String responsavelNome;

    @Column(nullable = false)
    private LocalDate prazo;

    @Column(name = "data_aviso", nullable = false)
    private LocalDate dataAviso;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    /** Exclusivo do JPA (decisão #254). */
    protected NotificacaoAcao() {
    }

    /** Constrói uma notificação persistível a partir de um {@link AvisoAcao} validado (#262, #266). */
    public NotificacaoAcao(AvisoAcao aviso) {
        // O aviso é validado por AvisoAcaoValidador no adaptador antes de montar e gravar (#266).
        Colaborador colaborador = aviso.responsavelColaborador();
        Setor setor = aviso.responsavelSetor();

        this.acao = aviso.acao();
        this.ocorrencia = aviso.ocorrencia();
        this.motivo = aviso.motivo();
        this.responsavelTipo = colaborador != null ? RESPONSAVEL_COLABORADOR : RESPONSAVEL_SETOR;
        this.responsavelId = colaborador != null ? colaborador.getId() : setor.getId();
        this.responsavelNome = colaborador != null ? colaborador.getNome() : setor.getNome();
        String responsavel = colaborador != null
                ? this.responsavelNome + " (colaborador)"
                : "setor " + this.responsavelNome;
        this.prazo = aviso.prazo();
        this.dataAviso = aviso.dataAviso();
        this.mensagem = montarMensagem(aviso, responsavel);
        this.criadaEm = LocalDateTime.now();
    }

    @PrePersist
    void preencherCriadaEm() {
        if (this.criadaEm == null) {
            this.criadaEm = LocalDateTime.now();
        }
    }

    private static String montarMensagem(AvisoAcao aviso, String responsavel) {
        String acaoTexto = "Ação #" + aviso.idAcao()
                + (aviso.idOcorrencia() != null ? " da ocorrência #" + aviso.idOcorrencia() : "");
        String prazoFormatado = aviso.prazo().format(DATA);
        if (aviso.motivo() == MotivoAvisoAcao.REATRIBUICAO) {
            return acaoTexto + " reatribuída a " + responsavel + ". Prazo: " + prazoFormatado
                    + ". Comunique o novo responsável.";
        }
        if (aviso.prazo().isBefore(aviso.dataAviso())) {
            return acaoTexto + " em atraso: o prazo venceu em " + prazoFormatado
                    + " e a execução não foi confirmada. Responsável: " + responsavel + ".";
        }
        return acaoTexto + " com prazo em " + prazoFormatado + ". Responsável: " + responsavel + ".";
    }

    public Integer getId() {
        return id;
    }

    public PlanoDeAcao getAcao() {
        return acao;
    }

    public Integer getIdAcao() {
        return acao != null ? acao.getId() : null;
    }

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public Integer getIdOcorrencia() {
        return ocorrencia != null ? ocorrencia.getId() : null;
    }

    public MotivoAvisoAcao getMotivo() {
        return motivo;
    }

    public String getResponsavelTipo() {
        return responsavelTipo;
    }

    public Integer getResponsavelId() {
        return responsavelId;
    }

    public String getResponsavelNome() {
        return responsavelNome;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public LocalDate getDataAviso() {
        return dataAviso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof NotificacaoAcao notificacao)) {
            return false;
        }
        return getId() != null && getId().equals(notificacao.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
