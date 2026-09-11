package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "perfil", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("COLABORADOR")
public class ColaboradorEntidade {

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
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public ColaboradorEntidade() {
    }

    public ColaboradorEntidade(Integer id, String cpf, String nome, LocalDate dataNascimento,
                              String email, boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static ColaboradorEntidade deDominio(Colaborador c) {
        if (c instanceof Supervisor s) {
            return new SupervisorEntidade(s.getId(), s.getCpf(), s.getNome(), s.getDataNascimento(),
                    s.getEmail(), s.getSenha(), s.isAtivo(), s.getCriadoEm(), s.getAtualizadoEm());
        } else if (c instanceof GestorDeSeguranca g) {
            return new GestorDeSegurancaEntidade(g.getId(), g.getCpf(), g.getNome(), g.getDataNascimento(),
                    g.getEmail(), g.getSenha(), g.isAtivo(), g.getCriadoEm(), g.getAtualizadoEm());
        } else {
            return new ColaboradorEntidade(c.getId(), c.getCpf(), c.getNome(), c.getDataNascimento(),
                    c.getEmail(), c.isAtivo(), c.getCriadoEm(), c.getAtualizadoEm());
        }
    }

    public Colaborador paraDominio() {
        return new Colaborador(id, cpf, nome, dataNascimento, email, ativo, criadoEm, atualizadoEm);
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
}
