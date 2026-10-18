/*
 * Consulta das notificações internas de novos relatos ao Gestor de Segurança (UC09, passo 12;
 * DS09, VisaoNotificacao; issue #195).
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesOcorrenciaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "Notificações internas ao Gestor de Segurança: novos relatos e avisos das ações do plano")
public class NotificacaoOcorrenciaController {
    private final ConsultarNotificacoesOcorrenciaUseCase consultarUseCase;

    public NotificacaoOcorrenciaController(ConsultarNotificacoesOcorrenciaUseCase consultarUseCase) {
        this.consultarUseCase = consultarUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista as notificações de novos relatos, da mais recente para a mais antiga (exclusivo do Gestor de Segurança)")
    public List<NotificacaoOcorrenciaResponse> listar() {
        return consultarUseCase.listar().stream()
                .map(NotificacaoOcorrenciaResponse::deDominio)
                .toList();
    }
}
