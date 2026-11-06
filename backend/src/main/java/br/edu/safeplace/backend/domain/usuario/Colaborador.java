/*
 * Entidade de domínio Colaborador, como no diagrama de classes: pessoa cadastrada sem conta de
 * acesso e superclasse de Supervisor e GestorDeSeguranca. É imutável: comDadosAtualizados devolve
 * uma cópia com nome, data de nascimento e e-mail novos, preservando id, CPF, situação e criação
 * (RF23, issue #122). É também a entidade JPA da tabela usuarios (#256): a herança SINGLE_TABLE usa o
 * discriminador perfil, e esta classe é a linha sem conta de acesso (COLABORADOR).
 */

package br.edu.safeplace.backend.domain.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "perfil", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("COLABORADOR")
public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(unique = true, length = 160)
    private String email;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    /** Exclusivo do JPA (decisão #254). */
    protected Colaborador() {
    }

    public Colaborador(Integer id, String cpf, String nome, LocalDate dataNascimento,
                       String email, boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        // Nome, nascimento e CPF são validados por ColaboradorValidador antes de salvar (issue #263).
        this.id = id;
        this.cpf = Cpf.sanitizar(cpf);
        this.nome = nome != null ? nome.trim() : null;
        this.dataNascimento = dataNascimento;
        this.email = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Colaborador novo(String nome, String cpf, LocalDate dataNascimento, String email) {
        return new Colaborador(null, cpf, nome, dataNascimento, email, true, LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * Devolve uma cópia com os dados cadastrais alterados (RF23, issue #122). Identidade (id e
     * CPF), situação e data de criação são preservadas, e o perfil segue a classe: atualizar nunca
     * muda o papel da pessoa. Alteração de CPF e desativação aguardam decisão do grupo (MVP, 11.1).
     */
    public Colaborador comDadosAtualizados(String nome, LocalDate dataNascimento, String email) {
        return new Colaborador(id, cpf, nome, dataNascimento, email, ativo, criadoEm, LocalDateTime.now());
    }

    public Perfil getPerfil() {
        return null;
    }

    /** Apenas o colaborador sem conta de acesso é alocado em tarefas operacionais (UC02, UC11). */
    public boolean podeSerAlocado() {
        return getPerfil() == null;
    }

    public Integer getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Colaborador colaborador)) {
            return false;
        }
        return getId() != null && getId().equals(colaborador.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
