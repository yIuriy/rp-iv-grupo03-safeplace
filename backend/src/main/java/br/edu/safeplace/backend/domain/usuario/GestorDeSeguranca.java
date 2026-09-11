package br.edu.safeplace.backend.domain.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GestorDeSeguranca extends Colaborador {
    private final String senha;

    public GestorDeSeguranca(Integer id, String cpf, String nome, LocalDate dataNascimento,
                             String email, String senha, boolean ativo,
                             LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        super(id, cpf, nome, dataNascimento, email, ativo, criadoEm, atualizadoEm);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório para o perfil Gestor de Segurança.");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória para o perfil Gestor de Segurança.");
        }
        this.senha = senha;
    }

    public static GestorDeSeguranca novo(String nome, String cpf, LocalDate dataNascimento, String email, String senha) {
        return new GestorDeSeguranca(null, cpf, nome, dataNascimento, email, senha, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.GESTOR_SEGURANCA;
    }

    public String getSenha() {
        return senha;
    }
}
