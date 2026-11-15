package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * Treinamento realizado por colaboradores (RF10, UC02, US10).
 * Entidade JPA da tabela treinamentos (#257): os participantes são associados por objeto
 * em relação N:N; {@link #getColaboradoresIds()} serve aos DTOs e relatórios que expõem ids.
 */
@Entity
@Table(name = "treinamentos")
public class Treinamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Column(length = 255)
    private String instrutor;

    @Column(name = "data_realizacao", nullable = false)
    private LocalDate dataRealizacao;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "validade_meses")
    private Integer validadeMeses;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "treinamentos_colaboradores",
            joinColumns = @JoinColumn(name = "treinamento_id"),
            inverseJoinColumns = @JoinColumn(name = "colaborador_id"))
    private List<Colaborador> colaboradores = new ArrayList<>();

    /** Exclusivo do JPA (decisão #254). */
    protected Treinamento() {
    }

    public Treinamento(Integer id, String nome, String conteudo, String instrutor, LocalDate dataRealizacao,
                       Integer cargaHoraria, List<Colaborador> colaboradores) {
        this(id, nome, conteudo, instrutor, dataRealizacao, cargaHoraria, null, colaboradores);
    }

    public Treinamento(Integer id, String nome, String conteudo, String instrutor, LocalDate dataRealizacao,
                       Integer cargaHoraria, Integer validadeMeses, List<Colaborador> colaboradores) {
        this.id = id;
        this.nome = nome != null ? nome.trim() : null;
        this.conteudo = conteudo;
        this.instrutor = instrutor;
        this.dataRealizacao = dataRealizacao;
        this.cargaHoraria = cargaHoraria;
        this.validadeMeses = validadeMeses;
        this.colaboradores = colaboradores != null
                ? new ArrayList<>(colaboradores.stream().filter(Objects::nonNull).distinct().toList())
                : new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getConteudo() {
        return conteudo;
    }

    public String getInstrutor() {
        return instrutor;
    }

    public LocalDate getDataRealizacao() {
        return dataRealizacao;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public Integer getValidadeMeses() {
        return validadeMeses;
    }

    /** Sem validade o treinamento não vence (RF10; #116). */
    public Optional<LocalDate> getDataVencimento() {
        return Optional.ofNullable(validadeMeses).map(dataRealizacao::plusMonths);
    }

    public StatusTreinamento calcularStatus(LocalDate dataReferencia) {
        if (dataRealizacao.isAfter(dataReferencia)) {
            return StatusTreinamento.PENDENTE;
        }
        Optional<LocalDate> vencimento = getDataVencimento();
        if (vencimento.isPresent()) {
            if (PrazoVencimento.estaVencido(dataReferencia, vencimento.get())) {
                return StatusTreinamento.VENCIDO;
            }
            if (PrazoVencimento.estaProximoDoVencimento(dataReferencia, vencimento.get())) {
                return StatusTreinamento.PROXIMO_DO_VENCIMENTO;
            }
        }
        return StatusTreinamento.REALIZADO;
    }

    public String verificarStatusTreinamentos(List<Treinamento> treinamentos) {
        if (treinamentos == null || treinamentos.isEmpty()) {
            return "PENDENTE";
        }
        LocalDate hoje = LocalDate.now();
        boolean todosRealizados = treinamentos.stream().allMatch(t -> t.calcularStatus(hoje).atendeExigencia());
        return todosRealizados ? "REALIZADO" : "PENDENTE";
    }

    public List<Colaborador> getColaboradores() {
        return Collections.unmodifiableList(colaboradores);
    }

    public List<Integer> getColaboradoresIds() {
        return colaboradores.stream()
                .map(Colaborador::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Treinamento treinamento)) {
            return false;
        }
        return getId() != null && getId().equals(treinamento.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
