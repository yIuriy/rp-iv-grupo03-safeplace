package br.edu.safeplace.backend.validacao.usuario;

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/**
 * RF23: CPF e e-mail não podem pertencer a outra pessoa. A própria pessoa, na atualização, não
 * conta como duplicada. Antes da issue #263, ficava em {@code UsuarioService}.
 */
class IdentificacaoDisponivelElo extends EloValidacao<Colaborador> {

    private final UsuarioRepository usuarios;

    IdentificacaoDisponivelElo(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void verificar(Colaborador pessoa, ResultadoValidacao resultado) {
        if (usuarios == null) {
            return;
        }
        String cpf = pessoa.getCpf();
        usuarios.buscarPorCpf(cpf)
                .filter(outra -> outraPessoa(pessoa, outra))
                .ifPresent(outra -> {
                    CpfJaCadastradoException excecao = new CpfJaCadastradoException("CPF já cadastrado no sistema: " + cpf);
                    resultado.adicionar(Problema.comExcecao(excecao.getMessage(), () -> excecao));
                });
        String email = pessoa.getEmail();
        if (email != null) {
            usuarios.buscarPorEmail(email)
                    .filter(outra -> outraPessoa(pessoa, outra))
                    .ifPresent(outra -> {
                        EmailJaCadastradoException excecao =
                                new EmailJaCadastradoException("Email já cadastrado no sistema: " + email);
                        resultado.adicionar(Problema.comExcecao(excecao.getMessage(), () -> excecao));
                    });
        }
    }

    private static boolean outraPessoa(Colaborador pessoa, Colaborador encontrada) {
        return pessoa.getId() == null || !pessoa.getId().equals(encontrada.getId());
    }
}
