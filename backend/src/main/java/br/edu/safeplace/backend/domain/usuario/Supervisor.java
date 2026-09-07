package br.edu.safeplace.backend.domain.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Supervisor extends Colaborador {
    private final String senha;

    public Supervisor(Integer id, String cpf, String nome, LocalDate dataNascimento,
                      String email, String senha, boolean ativo,
                      LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        super(id, cpf, nome, dataNascimento, email, ativo, criadoEm, atualizadoEm);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório para o perfil Supervisor.");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória para o perfil Supervisor.");
        }
        this.senha = senha;
    }

    public static Supervisor novo(String nome, String cpf, LocalDate dataNascimento, String email, String senha) {
        return new Supervisor(null, cpf, nome, dataNascimento, email, senha, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.SUPERVISOR;
    }

    public String getSenha() {
        return senha;
    }
}
