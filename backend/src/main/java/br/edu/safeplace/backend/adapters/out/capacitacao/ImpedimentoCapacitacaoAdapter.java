/*
 * Adaptador somente leitura da porta VerificadorImpedimentoCapacitacao (#116, #257): delega a
 * ConsultarCapacitacaoUseCase para verificar as exigências normativas de EPI.
 */
package br.edu.safeplace.backend.adapters.out.capacitacao;

import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.VerificadorImpedimentoCapacitacao;

@Component
public class ImpedimentoCapacitacaoAdapter implements VerificadorImpedimentoCapacitacao {

    private final ConsultarCapacitacaoUseCase consultarCapacitacaoUseCase;

    public ImpedimentoCapacitacaoAdapter(ConsultarCapacitacaoUseCase consultarCapacitacaoUseCase) {
        this.consultarCapacitacaoUseCase = consultarCapacitacaoUseCase;
    }

    @Override
    public List<String> listarParaEntrega(Integer colaboradorId, Integer epiId) {
        return consultarCapacitacaoUseCase.listarImpedimentosEpi(colaboradorId, epiId);
    }
}
