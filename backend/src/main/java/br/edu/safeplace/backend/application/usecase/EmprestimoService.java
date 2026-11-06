/*
 * Caso de uso de empréstimos de EPI (RF11, UC12, US11, issue #106). Orquestra o domínio Emprestimo
 * com o domínio Epi já existente: a entrega debita uma unidade pelo próprio Epi.removerEstoque e a
 * devolução de item apto credita pelo Epi.adicionarEstoque, gravando a MovimentacaoEstoque em cada
 * caso. Itens devolvidos para manutenção ou descarte não voltam ao saldo disponível e não executam
 * o descarte definitivo (RF14, backlog).
 *
 * Impedimentos de entrega (UC12): situação do EPI diferente de DISPONIVEL, saldo zerado, CA vencido,
 * capacitação obrigatória ausente ou vencida e colaborador não cadastrado ou inativo. Duplicidade de
 * tipo em posse ativa exige confirmação e justificativa, sem bloqueio absoluto.
 */
package br.edu.safeplace.backend.application.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.application.port.in.ControlarEmprestimoUseCase;
import br.edu.safeplace.backend.application.port.out.EmprestimoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.VerificadorImpedimentoCapacitacao;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoNaoEncontradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ManutencaoEpi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.ResultadoManutencao;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.TipoManutencao;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.validacao.Validador;
import br.edu.safeplace.backend.validacao.epi.ManutencaoEpiValidador;
import br.edu.safeplace.backend.validacao.epi.MovimentacaoEstoqueValidador;
import br.edu.safeplace.backend.validacao.emprestimo.EmprestimoValidador;

@Service
public class EmprestimoService implements ControlarEmprestimoUseCase {
    private static final String ENTIDADE_AUDITADA = "Emprestimo";

    private final EmprestimoRepository emprestimos;
    private final EpiRepository epis;
    private final UsuarioRepository usuarios;
    private final VerificadorImpedimentoCapacitacao capacitacoes;
    private final LogAuditoriaRepository auditoria;
    private final ManutencaoEpiRepository manutencoes;
    private final Validador<MovimentacaoEstoque> movimentacaoValidador;
    private final Validador<ManutencaoEpi> manutencaoValidador;
    private final Validador<Emprestimo> emprestimoValidador;

    @Autowired
    public EmprestimoService(EmprestimoRepository emprestimos, EpiRepository epis,
                             UsuarioRepository usuarios, VerificadorImpedimentoCapacitacao capacitacoes,
                             LogAuditoriaRepository auditoria, ManutencaoEpiRepository manutencoes,
                             Validador<MovimentacaoEstoque> movimentacaoValidador,
                             Validador<ManutencaoEpi> manutencaoValidador,
                             Validador<Emprestimo> emprestimoValidador) {
        this.emprestimoValidador = emprestimoValidador;
        this.movimentacaoValidador = movimentacaoValidador;
        this.manutencaoValidador = manutencaoValidador;
        this.emprestimos = emprestimos;
        this.epis = epis;
        this.usuarios = usuarios;
        this.capacitacoes = capacitacoes;
        // RNF05: auditoria obrigatória; nenhuma alteração é gravada sem trilha.
        this.auditoria = java.util.Objects.requireNonNull(
                auditoria, "Porta de auditoria é obrigatória (RNF05).");
        // #241: devolução para manutenção sempre grava o registro; a porta não é opcional.
        this.manutencoes = java.util.Objects.requireNonNull(
                manutencoes, "Repositório de manutenções é obrigatório.");
    }

    /** Usa os validadores padrão; usado pelos testes sem contexto Spring. */
    public EmprestimoService(EmprestimoRepository emprestimos, EpiRepository epis,
                             UsuarioRepository usuarios, VerificadorImpedimentoCapacitacao capacitacoes,
                             LogAuditoriaRepository auditoria, ManutencaoEpiRepository manutencoes) {
        this(emprestimos, epis, usuarios, capacitacoes, auditoria, manutencoes,
                new MovimentacaoEstoqueValidador(), new ManutencaoEpiValidador(), new EmprestimoValidador());
    }

    @Override
    @Transactional
    public EmprestimoOutputDTO registrarEntrega(RegistrarEntregaInputDTO entrada, String operadorEmail) {
        Colaborador operador = operadorAutenticado(operadorEmail);
        Colaborador beneficiario = beneficiarioCadastrado(entrada.colaboradorId());
        Epi epi = epis.buscarPorIdComLock(entrada.epiId())
                .orElseThrow(() -> new EpiNaoEncontradoException(entrada.epiId()));

        validarEntregaPermitida(epi, beneficiario);
        verificarDuplicidadeDeTipo(epi, beneficiario, entrada);

        LocalDateTime agora = LocalDateTime.now();
        String termo = termoDeCautela(epi, beneficiario, operador, agora, entrada.dataDevolucaoPrevista());
        Emprestimo emprestimo = Emprestimo.registrarEntrega(epi, beneficiario, operador,
                agora, entrada.dataDevolucaoPrevista(), termo, entrada.justificativaSubstituicao());
        // Chain of Responsibility (#265): valida antes de baixar o estoque e de gravar.
        emprestimoValidador.validar(emprestimo);

        MovimentacaoEstoque saida = epi.removerEstoque(Emprestimo.QUANTIDADE_POR_EMPRESTIMO,
                "Empréstimo ao colaborador " + beneficiario.getNome(), operador);
        movimentacaoValidador.validar(saida);
        epis.atualizarSaldo(epi);
        epis.salvarMovimentacao(saida);

        Emprestimo salvo = emprestimos.salvar(emprestimo);
        auditar(LogAuditoria.registrarCadastro(operadorEmail, ENTIDADE_AUDITADA, salvo.getId(),
                "Entrega do EPI " + epi.getNome() + " ao colaborador " + beneficiario.getNome()));
        return saida(salvo, epi, beneficiario);
    }

    @Override
    @Transactional
    public EmprestimoOutputDTO registrarDevolucao(Integer emprestimoId, RegistrarDevolucaoInputDTO entrada,
                                                  String operadorEmail) {
        Colaborador operador = operadorAutenticado(operadorEmail);
        Emprestimo emprestimo = emprestimos.buscarPorIdComLock(emprestimoId)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException(emprestimoId));
        if (!emprestimo.estaAtivo()) {
            throw new EmprestimoJaEncerradoException(emprestimoId);
        }
        Epi epi = epis.buscarPorIdComLock(emprestimo.getEpiId())
                .orElseThrow(() -> new EpiNaoEncontradoException(emprestimo.getEpiId()));
        Colaborador beneficiario = usuarios.buscarPorId(emprestimo.getColaboradorId()).orElse(null);

        EstadoDevolucao estado = entrada.estado();
        emprestimo.devolver(estado, operador, LocalDateTime.now());

        if (estado.retornaAoEstoque()) {
            MovimentacaoEstoque retorno = epi.adicionarEstoque(Emprestimo.QUANTIDADE_POR_EMPRESTIMO,
                    "Devolução do empréstimo " + emprestimoId + " (item apto)", operador);
            movimentacaoValidador.validar(retorno);
            epis.atualizarSaldo(epi);
            epis.salvarMovimentacao(retorno);
        } else if (estado == EstadoDevolucao.MANUTENCAO) {
            ManutencaoEpi manutencao = ManutencaoEpi.novo(
                    epi.getId(),
                    LocalDateTime.now(),
                    TipoManutencao.CORRETIVA,
                    "Devolução do empréstimo " + emprestimoId + " encaminhada para manutenção",
                    ResultadoManutencao.REPROVADO,
                    operador.getNome(),
                    operador.getId()
            );
            manutencaoValidador.validar(manutencao);
            manutencoes.salvar(manutencao);
            epi.registrarDevolucaoManutencao(manutencao);
            epis.salvar(epi);
        } else if (estado == EstadoDevolucao.DESCARTE) {
            MovimentacaoEstoque baixaDescarte = new MovimentacaoEstoque(
                    null,
                    epi,
                    epi.getLote(),
                    operador,
                    TipoMovimentacao.SAIDA,
                    Emprestimo.QUANTIDADE_POR_EMPRESTIMO,
                    LocalDateTime.now(),
                    "Baixa por descarte na devolução do empréstimo " + emprestimoId
            );
            movimentacaoValidador.validar(baixaDescarte);
            epis.salvarMovimentacao(baixaDescarte);
            epi.registrarDevolucaoDescarte();
            epis.salvar(epi);
        }

        emprestimoValidador.validar(emprestimo);
        Emprestimo salvo = emprestimos.salvar(emprestimo);
        auditar(LogAuditoria.registrarAlteracao(operadorEmail, ENTIDADE_AUDITADA, salvo.getId(),
                "Devolução do EPI " + epi.getNome() + " com estado " + estado));
        String destinoAprovado = switch (estado) {
            case APTO -> "ESTOQUE";
            case MANUTENCAO -> "MANUTENCAO";
            case DESCARTE -> "DESCARTE";
        };
        return EmprestimoOutputDTO.deDominio(salvo, epi.getNome(),
                beneficiario != null ? beneficiario.getNome() : null,
                LocalDate.now(), destinoAprovado, epi.getQuantidade());
    }

    @Override
    @Transactional(readOnly = true)
    public EmprestimoOutputDTO buscarPorId(Integer emprestimoId) {
        Emprestimo emprestimo = emprestimos.buscarPorId(emprestimoId)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException(emprestimoId));
        return saida(emprestimo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmprestimoOutputDTO> listar(Situacao situacao, Integer colaboradorId) {
        Situacao filtro = situacao != null ? situacao : Situacao.TODOS;
        LocalDate hoje = LocalDate.now();
        List<Emprestimo> base = colaboradorId != null
                ? emprestimos.listarPorColaborador(colaboradorId)
                : emprestimos.listarTodos();
        return base.stream()
                .filter(emprestimo -> switch (filtro) {
                    case TODOS -> true;
                    case PENDENTES -> emprestimo.estaAtivo();
                    case ATRASADOS -> emprestimo.estaAtrasadoEm(hoje);
                })
                .map(this::saida)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpiOutputDTO> listarEpisDisponiveisParaEmprestimo() {
        LocalDate hoje = LocalDate.now();
        return epis.listarTodos().stream()
                .filter(epi -> epi.getStatus() == StatusEpi.DISPONIVEL)
                .filter(epi -> epi.getQuantidade() >= Emprestimo.QUANTIDADE_POR_EMPRESTIMO)
                .filter(epi -> epi.getModelo().verificarCA(hoje))
                .map(EpiOutputDTO::deDominio)
                .toList();
    }

    private void validarEntregaPermitida(Epi epi, Colaborador beneficiario) {
        if (epi.getStatus() != StatusEpi.DISPONIVEL) {
            throw new EntregaImpedidaException("O EPI " + epi.getNome() + " está na situação "
                    + epi.getStatus() + " e não pode ser emprestado.");
        }
        if (epi.getQuantidade() < Emprestimo.QUANTIDADE_POR_EMPRESTIMO) {
            throw new EntregaImpedidaException("O EPI " + epi.getNome()
                    + " está sem saldo disponível em estoque.");
        }
        try {
            epi.validarCaValido();
        } catch (CertificadoAprovacaoVencidoException vencido) {
            throw new EntregaImpedidaException("O EPI " + epi.getNome()
                    + " possui Certificado de Aprovação vencido e não pode ser entregue.");
        }
        List<String> pendencias = capacitacoes.impedimentosParaEntrega(beneficiario.getId(), epi.getId());
        if (!pendencias.isEmpty()) {
            throw new EntregaImpedidaException("Impedimento normativo: o colaborador " + beneficiario.getNome()
                    + " não atende às capacitações exigidas pelo EPI: " + String.join("; ", pendencias) + ".");
        }
    }

    /** UC12, exceção III: mesmo tipo é a classificação da EspecificacaoEPI, com fallback para o mesmo EPI quando ausente. */
    private void verificarDuplicidadeDeTipo(Epi epi, Colaborador beneficiario, RegistrarEntregaInputDTO entrada) {
        ClassificacaoEPI tipo = epi.getEspecificacao() != null ? epi.getEspecificacao().getClassificacao() : null;
        Optional<Emprestimo> duplicado = emprestimos.listarAtivosPorColaborador(beneficiario.getId()).stream()
                .filter(ativo -> epis.buscarPorId(ativo.getEpiId())
                        .map(outro -> {
                            if (tipo != null && outro.getEspecificacao() != null && outro.getEspecificacao().getClassificacao() != null) {
                                return tipo == outro.getEspecificacao().getClassificacao();
                            }
                            return outro.getId().equals(epi.getId());
                        })
                        .orElse(false))
                .findFirst();
        if (duplicado.isEmpty()) {
            return;
        }
        boolean justificado = entrada.justificativaSubstituicao() != null
                && !entrada.justificativaSubstituicao().isBlank();
        if (!entrada.confirmarSubstituicao() || !justificado) {
            String descricaoDoTipo = tipo != null ? tipo.name() : epi.getNome();
            throw new DuplicidadeDePosseException(duplicado.get().getId(), descricaoDoTipo);
        }
    }

    private Colaborador operadorAutenticado(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Operador autenticado é obrigatório.");
        }
        return usuarios.buscarPorEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Operador autenticado não encontrado."));
    }

    private Colaborador beneficiarioCadastrado(Integer colaboradorId) {
        if (colaboradorId == null) {
            throw new IllegalArgumentException("Colaborador é obrigatório.");
        }
        Colaborador pessoa = usuarios.buscarPorId(colaboradorId)
                .orElseThrow(() -> new EntregaImpedidaException("Colaborador " + colaboradorId + " não está cadastrado."));
        if (!pessoa.isAtivo()) {
            throw new EntregaImpedidaException("Colaborador " + pessoa.getNome() + " está inativo.");
        }
        if (!pessoa.podeSerAlocado() || pessoa.getPerfil() != null) {
            throw new EntregaImpedidaException("Apenas colaboradores sem conta de acesso podem receber empréstimo de EPIs.");
        }
        return pessoa;
    }

    /** Termo de cautela do UC12 (passo 6): registro textual da posse e responsabilidade, sem PDF nem assinatura digital. */
    private static String termoDeCautela(Epi epi, Colaborador beneficiario, Colaborador operador,
                                         LocalDateTime dataHora, LocalDate devolucaoPrevista) {
        return "Termo de cautela: o colaborador " + beneficiario.getNome() + " (CPF " + beneficiario.getCpf()
                + ") recebeu 1 unidade do EPI " + epi.getNome() + " (código " + epi.getId() + ", CA "
                + epi.getNumeroCa() + ") em " + dataHora + ", entregue por " + operador.getNome()
                + ", com devolução prevista para " + devolucaoPrevista + ".";
    }

    private EmprestimoOutputDTO saida(Emprestimo emprestimo) {
        Epi epi = epis.buscarPorId(emprestimo.getEpiId()).orElse(null);
        Colaborador beneficiario = usuarios.buscarPorId(emprestimo.getColaboradorId()).orElse(null);
        return saida(emprestimo, epi, beneficiario);
    }

    private EmprestimoOutputDTO saida(Emprestimo emprestimo, Epi epi, Colaborador beneficiario) {
        return EmprestimoOutputDTO.deDominio(emprestimo,
                epi != null ? epi.getNome() : null,
                beneficiario != null ? beneficiario.getNome() : null,
                LocalDate.now());
    }

    private void auditar(LogAuditoria log) {
        auditoria.salvar(log);
    }
}
