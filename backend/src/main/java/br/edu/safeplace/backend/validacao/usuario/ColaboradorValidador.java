package br.edu.safeplace.backend.validacao.usuario;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida o cadastro e a atualização de pessoas: Colaborador, Supervisor e Gestor de Segurança
 * (RF23, UC13). Antes da issue #263, as regras estavam nos construtores das três classes e em
 * {@code UsuarioService} (CPF e e-mail disponíveis).
 *
 * <p>A ordem repete a do cadastro antigo: formato e disponibilidade do CPF, e-mail disponível,
 * dados pessoais e, por último, as credenciais exigidas pelo perfil.</p>
 */
@Component
public class ColaboradorValidador extends ValidadorEmCadeia<Colaborador> {

    @Autowired
    public ColaboradorValidador(UsuarioRepository usuarios, Validador<String> cpfValidador) {
        super(montarCadeia(usuarios, cpfValidador));
    }

    private static EloValidacao<Colaborador> montarCadeia(UsuarioRepository usuarios, Validador<String> cpfValidador) {
        EloValidacao<Colaborador> primeiro = new CpfValidoElo(cpfValidador);
        primeiro.encadear(new IdentificacaoDisponivelElo(usuarios))
                .encadear(exigir(p -> vazio(p.getNome()), "Nome é obrigatório."))
                .encadear(exigir(p -> p.getDataNascimento() == null, "Data de nascimento é obrigatória."))
                .encadear(exigir(p -> p.getDataNascimento() != null && p.getDataNascimento().isAfter(LocalDate.now()),
                        "Data de nascimento não pode estar no futuro."))
                .encadear(new CredenciaisDoPerfilElo());
        return primeiro;
    }
}
