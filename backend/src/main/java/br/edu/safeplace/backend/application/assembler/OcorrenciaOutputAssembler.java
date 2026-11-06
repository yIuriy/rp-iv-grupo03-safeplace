/*
 * Assembler (Fowler, PoEAA) do OcorrenciaOutputDTO (#245): único ponto que conhece o domínio da ocorrência
 * e o formato devolvido pelos casos de uso. A parte comum é montada uma vez; a específica de acidente e a
 * de incidente vêm de métodos separados; o plano de ação sai como PlanoDeAcaoOutputDTO.
 */
package br.edu.safeplace.backend.application.assembler;

import java.util.Optional;
import java.util.function.Function;

import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.dto.output.PlanoDeAcaoOutputDTO;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

public class OcorrenciaOutputAssembler {

    public OcorrenciaOutputDTO montar(Ocorrencia ocorrencia) {
        DadosDoTipo tipo = dadosDoTipo(ocorrencia);
        Colaborador relator = ocorrencia.getRelator();
        return new OcorrenciaOutputDTO(
                ocorrencia.getIdOcorrencia(),
                tipo.tipoOcorrencia(),
                ocorrencia.getDataOcorrencia(),
                ocorrencia.getDataRegistro(),
                se(ocorrencia.getStatusOcorrencia(), Enum::name),
                ocorrencia.getLocal(),
                ocorrencia.getDescricao(),
                PlanoDeAcaoOutputDTO.deDominio(ocorrencia.getPlanoDeAcao()),
                tipo.causaRaiz(),
                tipo.tipo(),
                tipo.dano(),
                tipo.numeroProtocoloCAT(),
                tipo.numeroProtocoloCAT(),
                tipo.destino(),
                tipo.situacaoRisco(),
                tipo.potencialDano(),
                tipo.gravidade(),
                se(ocorrencia.getArea(), AreaRisco::getId),
                se(ocorrencia.getArea(), AreaRisco::getNome),
                se(ocorrencia.getColaborador(), Colaborador::getId),
                se(ocorrencia.getColaborador(), Colaborador::getNome),
                se(relator, Colaborador::getId),
                se(relator, Colaborador::getNome),
                se(relator, r -> se(r.getPerfil(), Enum::name)));
    }

    private static DadosDoTipo dadosDoTipo(Ocorrencia ocorrencia) {
        if (ocorrencia instanceof Acidente acidente) {
            return deAcidente(acidente);
        }
        if (ocorrencia instanceof Incidente incidente) {
            return deIncidente(incidente);
        }
        throw new IllegalArgumentException("Tipo de ocorrência não suportado");
    }

    private static DadosDoTipo deAcidente(Acidente acidente) {
        return new DadosDoTipo("ACIDENTE",
                se(acidente.getCausaRaiz(), Enum::name),
                acidente.getTipo(),
                acidente.getDano(),
                acidente.getNumeroProtocoloCAT(),
                acidente.getDestino(),
                null,
                null,
                se(acidente.getGravidade(), Enum::name));
    }

    private static DadosDoTipo deIncidente(Incidente incidente) {
        return new DadosDoTipo("INCIDENTE", null, null, null, null, null,
                incidente.getSituacaoRisco(),
                incidente.getPotencialDano(),
                null);
    }

    private static <T, R> R se(T valor, Function<T, R> leitura) {
        return Optional.ofNullable(valor).map(leitura).orElse(null);
    }

    /** Campos que só um dos tipos de ocorrência preenche. */
    private record DadosDoTipo(
            String tipoOcorrencia,
            String causaRaiz,
            String tipo,
            String dano,
            String numeroProtocoloCAT,
            String destino,
            String situacaoRisco,
            String potencialDano,
            String gravidade) {
    }
}
