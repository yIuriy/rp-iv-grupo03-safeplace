package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.Objects;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Certificação normativa de um colaborador (RF10, UC02, US10).
 * Entidade JPA da tabela certificacoes (#257): o colaborador é associado por objeto;
 * {@link #getColaboradorId()} serve aos DTOs e relatórios que continuam expondo ids.
 */
@Entity
@Table(name = "certificacoes")
public class Certificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Transient
    private StatusCertificacao status;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    /** Exclusivo do JPA (decisão #254). */
    protected Certificacao() {
    }

    public Certificacao(Integer id, String nome, LocalDate dataVencimento, LocalDate dataEmissao,
                        Colaborador colaborador) {
        this(id, nome, dataVencimento, dataEmissao, colaborador, dataEmissao);
    }

    public Certificacao(Integer id, String nome, LocalDate dataVencimento, LocalDate dataEmissao,
                        Colaborador colaborador, LocalDate dataReferencia) {
        this.id = id;
        this.nome = nome != null ? nome.trim() : null;
        this.dataVencimento = dataVencimento;
        this.dataEmissao = dataEmissao;
        this.colaborador = colaborador;
        this.status = (dataVencimento != null && dataReferencia != null) ? calcularStatus(dataReferencia) : null;
    }

    @PostLoad
    void inicializarStatus() {
        if (this.status == null && this.dataVencimento != null && this.dataEmissao != null) {
            this.status = calcularStatus(this.dataEmissao);
        }
    }

    public static boolean validarInconsistenciaCronologica(LocalDate dataEmissao, LocalDate dataValidade) {
        if (dataEmissao == null || dataValidade == null) {
            return false;
        }
        return dataEmissao.isAfter(dataValidade);
    }

    /** Cadastro de certificação; as datas e o vínculo são validados por CertificacaoValidador (#265). */
    public static Certificacao cadastrar(String nome, LocalDate dataVencimento, LocalDate dataEmissao,
                                         Colaborador colaborador) {
        return cadastrar(nome, dataVencimento, dataEmissao, colaborador, dataEmissao);
    }

    public static Certificacao cadastrar(String nome, LocalDate dataVencimento, LocalDate dataEmissao,
                                         Colaborador colaborador, LocalDate dataReferencia) {
        return new Certificacao(null, nome, dataVencimento, dataEmissao, colaborador, dataReferencia);
    }

    public StatusCertificacao calcularStatus(LocalDate dataReferencia) {
        if (PrazoVencimento.estaVencido(dataReferencia, dataVencimento)) {
            return StatusCertificacao.VENCIDA;
        }
        if (PrazoVencimento.estaProximoDoVencimento(dataReferencia, dataVencimento)) {
            return StatusCertificacao.PROXIMA_DO_VENCIMENTO;
        }
        return StatusCertificacao.EM_DIA;
    }

    /**
     * Indica se a certificação habilita o colaborador na data de referência. Segue a regra do
     * status: na própria data de vencimento ela já está vencida (UC02, cenário alternativo I), e a
     * alocação ou entrega que a exige é impedida (UC02, exceção I; UC12, exceção I).
     */
    public boolean estaValidaEm(LocalDate dataReferencia) {
        return calcularStatus(dataReferencia) != StatusCertificacao.VENCIDA;
    }

    public void reavaliarStatus(LocalDate dataReferencia) {
        this.status = calcularStatus(dataReferencia);
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusCertificacao getStatus() {
        return status;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public Integer getColaboradorId() {
        return colaborador != null ? colaborador.getId() : null;
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Certificacao certificacao)) {
            return false;
        }
        return getId() != null && getId().equals(certificacao.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
