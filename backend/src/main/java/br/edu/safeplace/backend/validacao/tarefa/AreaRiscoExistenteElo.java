package br.edu.safeplace.backend.validacao.tarefa;

import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.domain.tarefa.Tarefa;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** A área de risco associada à tarefa, quando informada, precisa existir (V26). */
class AreaRiscoExistenteElo extends EloValidacao<Tarefa> {

    private final AreaRiscoRepository areas;

    AreaRiscoExistenteElo(AreaRiscoRepository areas) {
        this.areas = areas;
    }

    @Override
    protected void verificar(Tarefa tarefa, ResultadoValidacao resultado) {
        Integer areaRiscoId = tarefa.getAreaRiscoId();
        if (areaRiscoId != null && areas != null && areas.buscarPorId(areaRiscoId).isEmpty()) {
            String mensagem = "Área de risco não encontrada com id: " + areaRiscoId;
            resultado.adicionar(Problema.comExcecao(mensagem, () -> new IllegalArgumentException(mensagem)));
        }
    }
}
