package br.edu.safeplace.backend.validacao.projecao;

import java.time.LocalDate;

import br.edu.safeplace.backend.domain.epi.ContextoProjecao;
import br.edu.safeplace.backend.validacao.EloValidacao;
import br.edu.safeplace.backend.validacao.ResultadoValidacao;

/** DS10, 2.3: CA ausente ou vencido não gera estimativa. */
class CertificadoAprovacaoElo extends EloValidacao<ContextoProjecao> {

    @Override
    protected void verificar(ContextoProjecao contexto, ResultadoValidacao resultado) {
        LocalDate validadeCa = contexto.epi().getDataValidadeCa();
        if (validadeCa == null) {
            resultado.adicionar(PendenciaProjecao.de(contexto,
                    "não possui Certificado de Aprovação (CA) com validade cadastrada."));
        } else if (validadeCa.isBefore(contexto.dataReferencia())) {
            resultado.adicionar(PendenciaProjecao.de(contexto,
                    "não possui CA válido: o CA venceu em " + validadeCa + "."));
        }
    }
}
