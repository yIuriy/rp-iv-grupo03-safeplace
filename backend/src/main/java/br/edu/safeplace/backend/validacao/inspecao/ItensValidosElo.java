package br.edu.safeplace.backend.validacao.inspecao;

import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.Problema;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** US09, critério 4: cada item precisa da norma avaliada e do resultado (conforme ou não). */
class ItensValidosElo extends EloValidacao<Inspecao> {

    @Override
    protected void verificar(Inspecao inspecao, ResultadoValidacao resultado) {
        for (ItemVerificado item : inspecao.getItens()) {
            if (item.getDescricao() == null || item.getDescricao().isBlank()) {
                adicionar(resultado, "Informe a norma ou referência avaliada em cada item verificado.");
            } else if (item.getConformidade() == null) {
                adicionar(resultado, "Informe se o item \"" + item.getDescricao()
                        + "\" está em conformidade ou não conformidade.");
            }
        }
    }

    private static void adicionar(ResultadoValidacao resultado, String mensagem) {
        resultado.adicionar(Problema.comExcecao(mensagem, () -> new InspecaoInvalidaException(mensagem)));
    }
}
