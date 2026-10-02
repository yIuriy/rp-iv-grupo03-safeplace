/*
 * Caso de uso do histórico de inspeções (RF09, US09, issue #134): confere a área, identifica o Gestor
 * de Segurança autenticado como responsável, valida o registro e consulta o histórico da área.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarInspecaoUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.InspecaoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@Service
public class InspecaoUseCase implements GerenciarInspecaoUseCase {

    private final InspecaoRepositoryPort inspecaoRepositoryPort;
    private final AreaRiscoRepositoryPort areaRiscoRepositoryPort;
    private final UsuarioRepositorioPorta usuarioRepositorio;

    public InspecaoUseCase(InspecaoRepositoryPort inspecaoRepositoryPort,
            AreaRiscoRepositoryPort areaRiscoRepositoryPort, UsuarioRepositorioPorta usuarioRepositorio) {
        this.inspecaoRepositoryPort = inspecaoRepositoryPort;
        this.areaRiscoRepositoryPort = areaRiscoRepositoryPort;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    @Transactional
    public InspecaoOutputDTO registrar(Integer areaRiscoId, RegistrarInspecaoInputDTO inputDTO) {
        exigirArea(areaRiscoId);
        GestorDeSeguranca responsavel = resolverGestorAutenticado();
        Inspecao inspecao = Inspecao.registrar(
                areaRiscoId, inputDTO.dataInspecao(), responsavel, converterItens(inputDTO.itens()), LocalDate.now());
        return InspecaoOutputDTO.deDominio(inspecaoRepositoryPort.salvar(inspecao));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InspecaoOutputDTO> listarPorArea(Integer areaRiscoId) {
        exigirArea(areaRiscoId);
        return inspecaoRepositoryPort.listarPorArea(areaRiscoId).stream()
                .map(InspecaoOutputDTO::deDominio)
                .toList();
    }

    private void exigirArea(Integer areaRiscoId) {
        areaRiscoRepositoryPort.buscarPorId(areaRiscoId)
                .orElseThrow(() -> new AreaRiscoNaoEncontradaException(areaRiscoId));
    }

    private static List<ItemVerificado> converterItens(List<RegistrarInspecaoInputDTO.Item> itens) {
        if (itens == null) {
            return List.of();
        }
        return itens.stream()
                .map(item -> {
                    if (item == null) {
                        throw new InspecaoInvalidaException("Item verificado vazio no registro da inspeção.");
                    }
                    return ItemVerificado.novo(item.descricao(), item.conformidade(), item.observacoes());
                })
                .toList();
    }

    /**
     * O responsável é o Gestor de Segurança autenticado (associação GestorDeSeguranca 1 para Inspecao).
     * Sem um Gestor identificável, o registro é recusado em vez de ser gravado sem autoria.
     */
    private GestorDeSeguranca resolverGestorAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth != null ? auth.getName() : null;
        if (email != null && !email.isBlank() && !"anonymousUser".equals(email)) {
            return usuarioRepositorio.buscarPorEmail(email.trim())
                    .filter(GestorDeSeguranca.class::isInstance)
                    .map(GestorDeSeguranca.class::cast)
                    .orElseThrow(InspecaoUseCase::semGestor);
        }
        throw semGestor();
    }

    private static AccessDeniedException semGestor() {
        return new AccessDeniedException(
                "O registro de inspeção exige um Gestor de Segurança autenticado como responsável.");
    }
}
