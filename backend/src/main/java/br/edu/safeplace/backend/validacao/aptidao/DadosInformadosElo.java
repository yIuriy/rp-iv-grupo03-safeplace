package br.edu.safeplace.backend.validacao.aptidao;

import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** Sem tarefa ou sem colaborador não há o que verificar: interrompe a cadeia. */
class DadosInformadosElo extends EloValidacao<AptidaoParaTarefa> {

    @Override
    protected void verificar(AptidaoParaTarefa aptidao, ResultadoValidacao resultado) {
        if (aptidao.tarefa() == null) {
            resultado.interromper(Problema.comExcecao("tarefa não informada",
                    () -> new IllegalArgumentException("Tarefa é obrigatória para validação de aptidão.")));
        } else if (aptidao.colaborador() == null) {
            resultado.interromper(Problema.comExcecao("colaborador não informado",
                    () -> new IllegalArgumentException("Colaborador é obrigatório para validação de aptidão.")));
        }
    }
}
