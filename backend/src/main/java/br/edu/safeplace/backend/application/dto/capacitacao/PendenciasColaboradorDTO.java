package br.edu.safeplace.backend.application.dto.capacitacao;

import java.util.ArrayList;
import java.util.List;

import br.edu.safeplace.backend.domain.capacitacao.Certificacao;
import br.edu.safeplace.backend.domain.capacitacao.StatusCertificacao;
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
    /**
     * Classifica as certificações já reavaliadas e identifica a pendência de integração admissional
     * (UC02, cenários alternativos I e III). A ausência de certificações basta para a pendência
     * (UC02, cenário III; US10, regra 4), mesmo que haja treinamentos registrados.
     */
    public static PendenciasColaboradorDTO avaliar(Integer colaboradorId, List<Certificacao> certificacoes,
                                                   List<Treinamento> treinamentos) {
        List<Certificacao> alertasVencimento = new ArrayList<>();
        List<Certificacao> certificacoesVencidasLista = new ArrayList<>();
        int emDia = 0;

        for (Certificacao cert : certificacoes) {
            StatusCertificacao status = cert.getStatus();
            if (status == StatusCertificacao.PROXIMA_DO_VENCIMENTO) {
                alertasVencimento.add(cert);
            } else if (status == StatusCertificacao.VENCIDA) {
                certificacoesVencidasLista.add(cert);
            } else if (status == StatusCertificacao.EM_DIA) {
                emDia++;
            }
        }

        boolean pendenteIntegracaoAdmissional = certificacoes.isEmpty();

        return new PendenciasColaboradorDTO(
                colaboradorId,
                certificacoes.size(),
                emDia,
                alertasVencimento.size(),
                certificacoesVencidasLista.size(),
                treinamentos.size(),
                pendenteIntegracaoAdmissional,
                alertasVencimento,
                certificacoesVencidasLista,
                treinamentos
        );
    }

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
            avisos.add("Colaborador possui " + certificacoesProximasVencimento + " certificação(ões) próxima(s) do vencimento ("
                    + Certificacao.DIAS_ANTECEDENCIA_ALERTA + " dias)");
        }
        return avisos;
    }
}
