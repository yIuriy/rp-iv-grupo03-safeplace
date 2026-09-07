package br.edu.safeplace.backend.domain.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Colaborador {
    private final Integer id;
    private final String cpf;
    private final String nome;
    private final LocalDate dataNascimento;
    private final String email;
    private final boolean ativo;
    private final LocalDateTime criadoEm;
    private final LocalDateTime atualizadoEm;

    public Colaborador(Integer id, String cpf, String nome, LocalDate dataNascimento,
                       String email, boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (dataNascimento == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória.");
        }
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode estar no futuro.");
        }
        CpfValidador.validar(cpf);

        this.id = id;
        this.cpf = CpfValidador.sanitizar(cpf);
        this.nome = nome.trim();
        this.dataNascimento = dataNascimento;
        this.email = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static Colaborador novo(String nome, String cpf, LocalDate dataNascimento, String email) {
        return new Colaborador(null, cpf, nome, dataNascimento, email, true, LocalDateTime.now(), LocalDateTime.now());
    }

    public Perfil getPerfil() {
        return Perfil.COLABORADOR;
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
