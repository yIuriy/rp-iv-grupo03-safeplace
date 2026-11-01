package br.edu.safeplace.backend.validacao.arearisco;

import static br.edu.safeplace.backend.validacao.RegraElo.exigir;
import static br.edu.safeplace.backend.validacao.RegraElo.vazio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ValidadorEmCadeia;

/**
 * Valida a área de risco no cadastro e na atualização (RF05, UC03). Substitui as verificações que
 * ficavam em {@code AreaRisco.cadastrar}, no construtor e em {@code AreaRisco.atualizar}, além das
 * consultas de código duplicado e setor existente do caso de uso (issue #263).
 *
 * <p>A ordem repete a do cadastro antigo: código único e setor existente (antes no caso de uso),
 * depois os campos obrigatórios e, por último, o nome (antes no construtor).</p>
 */
@Component
public class AreaRiscoValidador extends ValidadorEmCadeia<AreaRisco> {

    /** As portas podem ser nulas nos testes que não exercitam código e setor. */
    @Autowired
    public AreaRiscoValidador(AreaRiscoRepository areas, SetorRepository setores) {
        super(montarCadeia(areas, setores));
    }

    private static EloValidacao<AreaRisco> montarCadeia(AreaRiscoRepository areas, SetorRepository setores) {
        EloValidacao<AreaRisco> primeiro = new CodigoUnicoElo(areas);
        primeiro.encadear(new SetorExistenteElo(setores))
                .encadear(exigir(a -> vazio(a.getCodigo()), "Código da área de risco é obrigatório."))
                .encadear(exigir(a -> a.getNivelPerigo() == null, "Nível de perigo da área de risco é obrigatório."))
                .encadear(exigir(a -> a.getAgentesRisco().isEmpty(),
                        "Agentes de risco são obrigatórios para a área de risco."))
                .encadear(exigir(a -> vazio(a.getLimites()),
                        "Limites físicos ou operacionais são obrigatórios para a área de risco."))
                .encadear(new EpisObrigatoriosElo())
                .encadear(exigir(a -> vazio(a.getNome()), "Nome da área de risco é obrigatório."));
        return primeiro;
    }
}
