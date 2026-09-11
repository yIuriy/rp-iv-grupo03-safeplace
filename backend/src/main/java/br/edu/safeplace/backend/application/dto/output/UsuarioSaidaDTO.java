package br.edu.safeplace.backend.application.dto.output;

import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.Perfil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioSaidaDTO(
        Integer id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String email,
        Perfil perfil,
        boolean ativo,
        LocalDateTime criadoEm,
        String senhaInicial
) {
    /**
     * Devolve uma cópia com a senha inicial preenchida. Usado apenas no retorno do cadastro
     * de Supervisor (issue #91); listagem e busca mantêm este campo nulo.
     */
    public UsuarioSaidaDTO comSenhaInicial(String senhaInicial) {
        return new UsuarioSaidaDTO(id, nome, cpf, dataNascimento, email, perfil, ativo, criadoEm, senhaInicial);
    }

    public static UsuarioSaidaDTO deDominio(Colaborador colaborador) {
        return new UsuarioSaidaDTO(
                colaborador.getId(),
                colaborador.getNome(),
                colaborador.getCpf(),
                colaborador.getDataNascimento(),
                colaborador.getEmail(),
                colaborador.getPerfil(),
                colaborador.isAtivo(),
                colaborador.getCriadoEm(),
                null
        );
    }
}
