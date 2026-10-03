package br.edu.safeplace.backend.application.dto.capacitacao;

import java.util.ArrayList;
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

    public List<String> obterMensagensAvisos() {
        List<String> avisos = new ArrayList<>();
        if (pendenteIntegracaoAdmissional) {
            avisos.add("Pendência da trilha inicial de integração");
            avisos.add("Pendência de treinamentos admissionais");
        }
        if (certificacoesVencidas > 0) {
            avisos.add("Colaborador possui " + certificacoesVencidas + " certificação(ões) vencida(s) necessitando de renovação imediata");
        }
        if (certificacoesProximasVencimento > 0) {
            avisos.add("Colaborador possui " + certificacoesProximasVencimento + " certificação(ões) próxima(s) do vencimento (30 dias)");
        }
        return avisos;
    }
}
