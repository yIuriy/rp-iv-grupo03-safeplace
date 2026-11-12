/*
 * Entidade de domínio Emprestimo (RF11, UC12, US11, issue #106). Segue o diagrama de classes:
 * dataRetirada, dataDevolucaoPrevista, dataDevolucaoReal e status: ClassificacaoEmprestimo, com
 * as associações Colaborador (beneficiário) e EPI. UC12 exige ainda data/hora e o responsável pela
 * entrega, o estado na devolução e o termo de cautela; esses dados ficam aqui como campos de apoio.
 *
 * Decisões registradas na issue #106: cada empréstimo retém uma unidade do registro de EPI (a
 * decisão #124 fixa EPI como saldo de um lote/modelo, sem ItemEPI); a data prevista é informada
 * pelo operador; o atraso é calculado na consulta, não gravado.
 *
 * É a entidade JPA da tabela emprestimos (#260): EPI, beneficiário e responsáveis são associações por objeto;
 * os getters de id servem aos DTOs, que continuam expondo ids.
 */
package br.edu.safeplace.backend.domain.emprestimo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;
import br.edu.safeplace.backend.domain.epi.Epi;
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
import jakarta.persistence.Table;

@Entity
@Table(name = "emprestimos")
public class Emprestimo {
    /** Unidades retidas por empréstimo: uma, conforme a decisão registrada na issue #106. */
    public static final int QUANTIDADE_POR_EMPRESTIMO = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "epi_id", nullable = false)
    private Epi epi;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "colaborador_id", nullable = false)
    private Colaborador colaborador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_entrega_id", nullable = false)
    private Colaborador responsavelEntrega;

    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column(name = "termo_cautela", nullable = false, columnDefinition = "TEXT")
    private String termoCautela;

    @Column(name = "justificativa_substituicao", columnDefinition = "TEXT")
    private String justificativaSubstituicao;

    @Column(name = "data_devolucao_real")
    private LocalDateTime dataDevolucaoReal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_devolucao_id")
    private Colaborador responsavelDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_devolucao", length = 20)
    private EstadoDevolucao estadoDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClassificacaoEmprestimo status;

    /** Exclusivo do JPA (decisão #254). */
    protected Emprestimo() {
    }

    private Emprestimo(Builder builder) {
        // Os dados da entrega e a coerência com a devolução são validados por EmprestimoValidador
        // antes do repository (issue #265); o Builder não valida (#226).
        this.epi = builder.epi;
        this.colaborador = builder.beneficiario;
        this.responsavelEntrega = builder.responsavelEntrega;
        this.dataRetirada = builder.dataRetirada;
        this.dataDevolucaoPrevista = builder.dataDevolucaoPrevista;
        this.termoCautela = builder.termoCautela != null ? builder.termoCautela.trim() : null;
        this.justificativaSubstituicao = builder.justificativaSubstituicao;
        this.status = ClassificacaoEmprestimo.INDISPONIVEL;
    }

    /** Cenário alternativo I do UC12: encerra o empréstimo e guarda o estado do item devolvido. */
    public void devolver(EstadoDevolucao estado, Colaborador responsavelDevolucao, LocalDateTime dataHora) {
        if (!estaAtivo()) {
            throw new EmprestimoJaEncerradoException(id);
        }
        exigir(estado != null, "Estado na devolução é obrigatório.");
        exigir(responsavelDevolucao != null, "Responsável pela devolução é obrigatório.");
        exigir(dataHora != null, "Data e hora da devolução são obrigatórias.");
        exigir(!dataHora.isBefore(dataRetirada), "Devolução não pode ser anterior à retirada.");
        this.estadoDevolucao = estado;
        this.responsavelDevolucao = responsavelDevolucao;
        this.dataDevolucaoReal = dataHora;
        this.status = ClassificacaoEmprestimo.DISPONIVEL;
    }

    public boolean estaAtivo() {
        return status.ativo();
    }

    /** US11, regra 6: sem devolução após a data prevista, o empréstimo está atrasado. */
    public boolean estaAtrasadoEm(LocalDate hoje) {
        return estaAtivo() && hoje.isAfter(dataDevolucaoPrevista);
    }

    /** Classificação vigente na data informada; o atraso não é gravado, é calculado na consulta. */
    public ClassificacaoEmprestimo classificarEm(LocalDate hoje) {
        if (estaAtrasadoEm(hoje)) {
            return ClassificacaoEmprestimo.DEVOLUCAO_ATRASADA;
        }
        return status;
    }

    public Integer getId() {
        return id;
    }

    public Epi getEpi() {
        return epi;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public Colaborador getResponsavelEntrega() {
        return responsavelEntrega;
    }

    public Colaborador getResponsavelDevolucao() {
        return responsavelDevolucao;
    }

    public Integer getEpiId() {
        return epi != null ? epi.getId() : null;
    }

    public Integer getColaboradorId() {
        return colaborador != null ? colaborador.getId() : null;
    }

    public Integer getResponsavelEntregaId() {
        return responsavelEntrega != null ? responsavelEntrega.getId() : null;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public LocalDateTime getDataDevolucaoReal() {
        return dataDevolucaoReal;
    }

    public Integer getResponsavelDevolucaoId() {
        return responsavelDevolucao != null ? responsavelDevolucao.getId() : null;
    }

    public EstadoDevolucao getEstadoDevolucao() {
        return estadoDevolucao;
    }

    public ClassificacaoEmprestimo getStatus() {
        return status;
    }

    public String getTermoCautela() {
        return termoCautela;
    }

    public String getJustificativaSubstituicao() {
        return justificativaSubstituicao;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Emprestimo emprestimo)) {
            return false;
        }
        return getId() != null && getId().equals(emprestimo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * Builder (GoF, #226) da entrega do UC12, passo 6: cada dado tem nome, então EPI, beneficiário e responsável
     * não se confundem como os {@code Integer} posicionais de antes. O empréstimo nasce ativo
     * ({@code INDISPONIVEL}); devolução, id e situação gravada vêm do fluxo de devolução e do JPA.
     */
    public static final class Builder {
        private Epi epi;
        private Colaborador beneficiario;
        private Colaborador responsavelEntrega;
        private LocalDateTime dataRetirada;
        private LocalDate dataDevolucaoPrevista;
        private String termoCautela;
        private String justificativaSubstituicao;

        public Builder epi(Epi epi) {
            this.epi = epi;
            return this;
        }

        public Builder beneficiario(Colaborador beneficiario) {
            this.beneficiario = beneficiario;
            return this;
        }

        public Builder responsavelEntrega(Colaborador responsavelEntrega) {
            this.responsavelEntrega = responsavelEntrega;
            return this;
        }

        public Builder retiradaEm(LocalDateTime dataRetirada) {
            this.dataRetirada = dataRetirada;
            return this;
        }

        public Builder devolucaoPrevistaPara(LocalDate dataDevolucaoPrevista) {
            this.dataDevolucaoPrevista = dataDevolucaoPrevista;
            return this;
        }

        public Builder termoCautela(String termoCautela) {
            this.termoCautela = termoCautela;
            return this;
        }

        /** Exigida só quando o colaborador já tem posse do mesmo tipo de EPI (UC12, cenário alternativo II). */
        public Builder justificativaSubstituicao(String justificativaSubstituicao) {
            this.justificativaSubstituicao = justificativaSubstituicao;
            return this;
        }

        public Emprestimo build() {
            return new Emprestimo(this);
        }
    }
}
