/*
 * Adaptador somente leitura da porta ImpedimentoCapacitacaoPorta sobre as tabelas do módulo de
 * capacitações (migração V15, issue #116): exigencias_capacitacao, certificacoes, treinamentos e
 * treinamentos_colaboradores. Não altera essas tabelas nem o domínio de capacitações.
 *
 * Regra aplicada (UC12, exceção I): para cada exigência vinculada ao EPI (tipo_alvo = 'EPI'),
 * uma CERTIFICACAO precisa existir para o colaborador com o mesmo nome e data de vencimento não
 * ultrapassada; um TREINAMENTO precisa constar para o colaborador com o mesmo nome. A tabela de
 * treinamentos não possui data de vencimento, então um treinamento só é impedimento quando falta;
 * a revalidação periódica depende de decisão registrada nas issues #116/#117.
 */
package br.edu.safeplace.backend.adapters.out.capacitacao;

import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.safeplace.backend.application.port.in.capacitacao.ConsultarCapacitacaoUseCase;
import br.edu.safeplace.backend.application.port.out.ImpedimentoCapacitacaoPorta;

@Component
public class ImpedimentoCapacitacaoJdbcAdapter implements ImpedimentoCapacitacaoPorta {

    private final ConsultarCapacitacaoUseCase consultarCapacitacaoUseCase;

    public ImpedimentoCapacitacaoJdbcAdapter(ConsultarCapacitacaoUseCase consultarCapacitacaoUseCase) {
        this.consultarCapacitacaoUseCase = consultarCapacitacaoUseCase;
    }

    @Override
    public List<String> impedimentosParaEntrega(Integer colaboradorId, Integer epiId) {
        return consultarCapacitacaoUseCase.verificarImpedimentosParaEpi(colaboradorId, epiId);
    }
}
