/*
 * Caso de uso do histórico do próprio perfil (RNF05): lê da trilha de auditoria as ações registradas
 * com o e-mail da pessoa autenticada, sem receber id, para que cada um veja só o que fez.
 */
package br.edu.safeplace.backend.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.output.AcaoUsuarioOutputDTO;
import br.edu.safeplace.backend.application.port.in.ConsultarHistoricoProprioCasoDeUso;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;

@Service
public class HistoricoProprioUseCase implements ConsultarHistoricoProprioCasoDeUso {

    private final LogAuditoriaRepositoryPort logAuditoria;

    public HistoricoProprioUseCase(LogAuditoriaRepositoryPort logAuditoria) {
        this.logAuditoria = logAuditoria;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcaoUsuarioOutputDTO> listarAcoes(String email, int limite) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        int limiteAjustado = Math.clamp(limite, 1, LIMITE_MAXIMO);
        return logAuditoria.listarPorResponsavel(email.trim(), limiteAjustado).stream()
                .map(AcaoUsuarioOutputDTO::deDominio)
                .toList();
    }
}
