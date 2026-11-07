/*
 * Caso de uso do histórico de inspeções (RF09, US09, issue #134): confere a área, identifica o Gestor
 * de Segurança autenticado como responsável, valida o registro, grava o cadastro na trilha de
 * auditoria (RNF05) e consulta o histórico da área.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.RegistrarInspecaoInputDTO;
import br.edu.safeplace.backend.application.dto.output.InspecaoOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarInspecaoUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.inspecao.InspecaoValidador;

@Service
public class InspecaoService implements GerenciarInspecaoUseCase {

    private static final String ENTIDADE_LOG = "Inspecao";
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    /**
     * O responsável é o Gestor de Segurança autenticado (associação GestorDeSeguranca 1 para Inspecao).
     * Sem um Gestor identificável, o registro é recusado em vez de ser gravado sem autoria.
     */
    private static final String SEM_GESTOR =
            "O registro de inspeção exige um Gestor de Segurança autenticado como responsável.";

    private final InspecaoRepository inspecaoRepository;
    private final AreaRiscoRepository areaRiscoRepository;
    private final OperadorAtual operadorAtual;
    private final RegistradorAuditoria auditoria;
    private final Validador<Inspecao> validador;

    @Autowired
    public InspecaoService(InspecaoRepository inspecaoRepository,
            AreaRiscoRepository areaRiscoRepository, OperadorAtual operadorAtual,
            RegistradorAuditoria auditoria, Validador<Inspecao> validador) {
        this.inspecaoRepository = inspecaoRepository;
        this.areaRiscoRepository = areaRiscoRepository;
        // Adapter (#236): o Gestor responsável vem da porta OperadorAutenticado.
        this.operadorAtual = java.util.Objects.requireNonNull(operadorAtual, "Operador atual é obrigatório.");
        // Facade (#223, #237): auditoria simplificada sem manipular LogAuditoria diretamente.
        this.auditoria = java.util.Objects.requireNonNull(auditoria, "Registrador de auditoria é obrigatório.");
        this.validador = validador;
    }

    /** Usa o validador padrão, com a data de hoje; usado pelos testes sem contexto Spring. */
    public InspecaoService(InspecaoRepository inspecaoRepository,
            AreaRiscoRepository areaRiscoRepository, OperadorAtual operadorAtual,
            LogAuditoriaRepository logAuditoria) {
        this(inspecaoRepository, areaRiscoRepository, operadorAtual,
                new RegistradorAuditoria(logAuditoria, operadorAtual), new InspecaoValidador());
    }

    @Override
    @Transactional
    public InspecaoOutputDTO registrar(Integer areaRiscoId, RegistrarInspecaoInputDTO inputDTO) {
        AreaRisco area = exigirArea(areaRiscoId);
        GestorDeSeguranca responsavel = operadorAtual.gestorObrigatorio(SEM_GESTOR);
        Inspecao inspecao = Inspecao.registrar(
                area, inputDTO.dataInspecao(), responsavel, converterItens(inputDTO.itens()));
        // Chain of Responsibility (#263): itens, área, responsável e data, antes do repository.
        validador.validar(inspecao);
        InspecaoOutputDTO salva = InspecaoOutputDTO.deDominio(inspecaoRepository.salvar(inspecao));
        auditoria.por(responsavel.getEmail()).cadastro(ENTIDADE_LOG, salva.id(), descreverCadastro(area, salva));
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
}
