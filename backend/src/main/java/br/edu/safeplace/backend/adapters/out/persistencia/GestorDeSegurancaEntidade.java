package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("GESTOR_SEGURANCA")
public class GestorDeSegurancaEntidade extends ColaboradorEntidade {

    @Column
    private String senha;

    public GestorDeSegurancaEntidade() {
    }

    public GestorDeSegurancaEntidade(Integer id, String cpf, String nome, LocalDate dataNascimento,
                                     String email, String senha, boolean ativo,
                                     LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
        super(id, cpf, nome, dataNascimento, email, ativo, criadoEm, atualizadoEm);
        this.senha = senha;
    }

    @Override
    public GestorDeSeguranca paraDominio() {
        return new GestorDeSeguranca(getId(), getCpf(), getNome(), getDataNascimento(),
                getEmail(), senha, isAtivo(), getCriadoEm(), getAtualizadoEm());
    }

    public String getSenha() {
        return senha;
    }
}
