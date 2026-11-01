/*
 * Caso de uso do histórico de inspeções (RF09, US09, issue #134): confere a área, identifica o Gestor
 * de Segurança autenticado como responsável, valida o registro, grava o cadastro na trilha de
 * auditoria (RNF05) e consulta o histórico da área.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarInspecaoUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@Service
public class InspecaoService implements GerenciarInspecaoUseCase {

    private static final String ENTIDADE_LOG = "Inspecao";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final InspecaoRepository inspecaoRepository;
    private final AreaRiscoRepository areaRiscoRepository;
    private final UsuarioRepository usuarioRepositorio;
    private final LogAuditoriaRepository logAuditoria;

    public InspecaoService(InspecaoRepository inspecaoRepository,
            AreaRiscoRepository areaRiscoRepository, UsuarioRepository usuarioRepositorio,
            LogAuditoriaRepository logAuditoria) {
        this.inspecaoRepository = inspecaoRepository;
        this.areaRiscoRepository = areaRiscoRepository;
        this.usuarioRepositorio = usuarioRepositorio;
        this.logAuditoria = logAuditoria;
    }

    @Override
    @Transactional
    public InspecaoOutputDTO registrar(Integer areaRiscoId, RegistrarInspecaoInputDTO inputDTO) {
        AreaRisco area = exigirArea(areaRiscoId);
        GestorDeSeguranca responsavel = resolverGestorAutenticado();
        Inspecao inspecao = Inspecao.registrar(
                areaRiscoId, inputDTO.dataInspecao(), responsavel, converterItens(inputDTO.itens()), LocalDate.now());
        InspecaoOutputDTO salva = InspecaoOutputDTO.deDominio(inspecaoRepository.salvar(inspecao));
        logAuditoria.salvar(LogAuditoria.registrarCadastro(
                responsavel.getEmail(), ENTIDADE_LOG, salva.id(), descreverCadastro(area, salva)));
        return salva;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InspecaoOutputDTO> listarPorArea(Integer areaRiscoId) {
        exigirArea(areaRiscoId);
        return inspecaoRepository.listarPorArea(areaRiscoId).stream()
                .map(InspecaoOutputDTO::deDominio)
                .toList();
    }

    private AreaRisco exigirArea(Integer areaRiscoId) {
        return areaRiscoRepository.buscarPorId(areaRiscoId)
                .orElseThrow(() -> new AreaRiscoNaoEncontradaException(areaRiscoId));
    }

    /** Ex.: "Inspeção de 10/09/2026 na área AR-01 - Cabine de solda: 3 itens verificados, 1 não conformidade." */
    private static String descreverCadastro(AreaRisco area, InspecaoOutputDTO inspecao) {
        int itens = inspecao.itens().size();
        long falhas = inspecao.naoConformidades();
        return "Inspeção de " + DATA.format(inspecao.dataInspecao()) + " na área " + area.getCodigo() + " - "
                + area.getNome() + ": " + itens + (itens == 1 ? " item verificado, " : " itens verificados, ")
                + falhas + (falhas == 1 ? " não conformidade." : " não conformidades.");
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
                    .orElseThrow(InspecaoService::semGestor);
        }
        throw semGestor();
    }

    private static AccessDeniedException semGestor() {
        return new AccessDeniedException(
                "O registro de inspeção exige um Gestor de Segurança autenticado como responsável.");
    }
}
