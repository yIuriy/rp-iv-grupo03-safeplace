/*
 * Consulta dos avisos das ações do plano entregues ao Gestor de Segurança: alertas de prazo (UC07,
 * regra 2) e reatribuições (UC07, cenário alternativo II). Issue #196.
 */
package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.ConsultarNotificacoesAcaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notificacoes/acoes")
@Tag(name = "Notificações", description = "Notificações internas ao Gestor de Segurança: novos relatos e avisos das ações do plano")
public class NotificacaoAcaoController {
    private final ConsultarNotificacoesAcaoUseCase consultarUseCase;

    public NotificacaoAcaoController(ConsultarNotificacoesAcaoUseCase consultarUseCase) {
        this.consultarUseCase = consultarUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista os avisos de prazo e de reatribuição das ações do plano, do mais recente para o mais antigo (exclusivo do Gestor de Segurança)",
            description = "O responsável pela ação, Colaborador ou Setor, não acessa o sistema: o aviso o identifica para "
                    + "que o Gestor o comunique. O alerta de prazo é emitido uma única vez por ação e prazo; uma "
                    + "prorrogação rearma o alerta.")
    public List<NotificacaoAcaoResponse> listar() {
        return consultarUseCase.listar().stream()
                .map(NotificacaoAcaoResponse::deDominio)
                .toList();
    }
}
