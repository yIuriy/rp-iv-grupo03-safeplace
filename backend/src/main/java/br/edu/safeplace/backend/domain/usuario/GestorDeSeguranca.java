/*
 * Entidade de domínio GestorDeSeguranca: especializa Colaborador acrescentando a senha (hash) e
 * exigindo e-mail. A atualização cadastral (issue #122) mantém o hash e o perfil.
 */

package br.edu.safeplace.backend.domain.usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GESTOR_SEGURANCA")
public class GestorDeSeguranca extends Colaborador {
    @Column
    private String senha;

    /** Exclusivo do JPA (decisão #254). */
    protected GestorDeSeguranca() {
    }

    public GestorDeSeguranca(Integer id, String cpf, String nome, LocalDate dataNascimento,
                             String email, String senha, boolean ativo,
                             LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        super(id, cpf, nome, dataNascimento, email, ativo, criadoEm, atualizadoEm);
        // E-mail e senha do perfil são validados por ColaboradorValidador (issue #263).
        this.senha = senha;
    }

    public static GestorDeSeguranca novo(String nome, String cpf, LocalDate dataNascimento, String email, String senha) {
        return new GestorDeSeguranca(null, cpf, nome, dataNascimento, email, senha, true, LocalDateTime.now(), LocalDateTime.now());
    }

    /** Mantém o hash da senha: a atualização cadastral não troca credenciais. */
    @Override
    public GestorDeSeguranca comDadosAtualizados(String nome, LocalDate dataNascimento, String email) {
        return new GestorDeSeguranca(getId(), getCpf(), nome, dataNascimento, email, senha, isAtivo(),
                getCriadoEm(), LocalDateTime.now());
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.GESTOR_SEGURANCA;
    }

    public String getSenha() {
        return senha;
    }
}
