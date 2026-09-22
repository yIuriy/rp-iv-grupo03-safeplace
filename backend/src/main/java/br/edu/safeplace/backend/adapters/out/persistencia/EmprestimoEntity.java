/*
 * Entidade JPA da tabela emprestimos (migração V17). Espelha o domínio Emprestimo; enumeradores
 * são gravados pelo nome.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "emprestimos")
public class EmprestimoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "epi_id", nullable = false)
    private Integer epiId;

    @Column(name = "colaborador_id", nullable = false)
    private Integer colaboradorId;

    @Column(name = "responsavel_entrega_id", nullable = false)
    private Integer responsavelEntregaId;

    @Column(name = "data_retirada", nullable = false)
    private LocalDateTime dataRetirada;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column(name = "data_devolucao_real")
    private LocalDateTime dataDevolucaoReal;

    @Column(name = "responsavel_devolucao_id")
    private Integer responsavelDevolucaoId;

    @Column(name = "estado_devolucao", length = 20)
    private String estadoDevolucao;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "justificativa_substituicao", columnDefinition = "TEXT")
    private String justificativaSubstituicao;

    @Column(name = "termo_cautela", nullable = false, columnDefinition = "TEXT")
    private String termoCautela;

    protected EmprestimoEntity() {
    }

    public EmprestimoEntity(Integer id, Integer epiId, Integer colaboradorId, Integer responsavelEntregaId,
                            LocalDateTime dataRetirada, LocalDate dataDevolucaoPrevista,
                            LocalDateTime dataDevolucaoReal, Integer responsavelDevolucaoId,
                            String estadoDevolucao, String status, String justificativaSubstituicao,
                            String termoCautela) {
        this.id = id;
        this.epiId = epiId;
        this.colaboradorId = colaboradorId;
        this.responsavelEntregaId = responsavelEntregaId;
        this.dataRetirada = dataRetirada;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucaoReal = dataDevolucaoReal;
        this.responsavelDevolucaoId = responsavelDevolucaoId;
        this.estadoDevolucao = estadoDevolucao;
        this.status = status;
        this.justificativaSubstituicao = justificativaSubstituicao;
        this.termoCautela = termoCautela;
    }

    public Integer getId() {
        return id;
    }

    public Integer getEpiId() {
        return epiId;
    }

    public Integer getColaboradorId() {
        return colaboradorId;
    }

    public Integer getResponsavelEntregaId() {
        return responsavelEntregaId;
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
        return responsavelDevolucaoId;
    }

    public String getEstadoDevolucao() {
        return estadoDevolucao;
    }

    public String getStatus() {
        return status;
    }

    public String getJustificativaSubstituicao() {
        return justificativaSubstituicao;
    }

    public String getTermoCautela() {
        return termoCautela;
    }
}
