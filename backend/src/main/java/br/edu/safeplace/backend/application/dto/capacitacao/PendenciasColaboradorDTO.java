package br.edu.safeplace.backend.application.dto.capacitacao;

import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.Treinamento;

public record PendenciasColaboradorDTO(
        Integer colaboradorId,
        int totalCertificacoes,
        int certificacoesEmDia,
        int certificacoesProximasVencimento,
        int certificacoesVencidas,
        int totalTreinamentosRealizados,
        boolean pendenteIntegracaoAdmissional,
        List<Certificacao> alertasVencimento,
        List<Certificacao> certificacoesVencidasLista,
        List<Treinamento> treinamentosRealizados
) {
    public boolean possuiPendenciasImpeditivas() {
        return certificacoesVencidas > 0 || pendenteIntegracaoAdmissional;
    }
}
