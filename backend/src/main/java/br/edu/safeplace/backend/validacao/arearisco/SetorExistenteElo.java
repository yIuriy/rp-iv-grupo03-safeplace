package br.edu.safeplace.backend.validacao.arearisco;

import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** UC03, RN1: o setor físico vinculado à área, quando informado, precisa existir. */
class SetorExistenteElo extends EloValidacao<AreaRisco> {

    private final SetorRepository setores;

    SetorExistenteElo(SetorRepository setores) {
        this.setores = setores;
    }

    @Override
    protected void verificar(AreaRisco area, ResultadoValidacao resultado) {
        Integer setorId = area.getSetorId();
        if (setorId != null && setores != null && setores.buscarPorId(setorId).isEmpty()) {
            String mensagem = "Setor não encontrado com id: " + setorId;
            resultado.adicionar(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
        }
    }
}
