package br.edu.safeplace.backend.validacao.usuario;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    /** Issue #308: a data de nascimento é comparada com o {@link Clock} do projeto (Strategy Clock, #247). */
    @Autowired
    public ColaboradorValidador(UsuarioRepository usuarios,
            @Qualifier("cpfValidador") Validador<String> cpfValidador, Clock clock) {
        this(usuarios, cpfValidador, () -> LocalDate.now(clock));
    }

    public ColaboradorValidador(UsuarioRepository usuarios, Validador<String> cpfValidador) {
        this(usuarios, cpfValidador, Clock.systemDefaultZone());
    }

    /** {@code hoje} permite testar a regra da data de nascimento sem depender do relógio. */
    public ColaboradorValidador(UsuarioRepository usuarios, Validador<String> cpfValidador, Supplier<LocalDate> hoje) {
        super(montarCadeia(usuarios, cpfValidador, hoje));
    }

    private static EloValidacao<Colaborador> montarCadeia(UsuarioRepository usuarios, Validador<String> cpfValidador,
                                                         Supplier<LocalDate> hoje) {
        EloValidacao<Colaborador> primeiro = new CpfValidoElo(cpfValidador);
        primeiro.encadear(new IdentificacaoDisponivelElo(usuarios))
                .encadear(exigir(p -> isBlankOrNull(p.getNome()), "Nome é obrigatório."))
                .encadear(exigir(p -> p.getDataNascimento() == null, "Data de nascimento é obrigatória."))
                .encadear(exigir(p -> p.getDataNascimento() != null && p.getDataNascimento().isAfter(hoje.get()),
                        "Data de nascimento não pode estar no futuro."))
                .encadear(new CredenciaisDoPerfilElo());
        return primeiro;
    }
}
