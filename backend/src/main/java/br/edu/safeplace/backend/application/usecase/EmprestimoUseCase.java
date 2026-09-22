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
import br.edu.safeplace.backend.application.port.out.EmprestimoRepositoryPort;
import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.application.port.out.ImpedimentoCapacitacaoPorta;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepositoryPort;
import br.edu.safeplace.backend.application.port.out.UsuarioRepositorioPorta;
import br.edu.safeplace.backend.domain.comum.LogAuditoria;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.emprestimo.exception.DuplicidadeDePosseException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoNaoEncontradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.epi.exception.CertificadoAprovacaoVencidoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@Service
public class EmprestimoUseCase implements ControlarEmprestimoUseCase {
    private static final String ENTIDADE_AUDITADA = "Emprestimo";

    private final EmprestimoRepositoryPort emprestimos;
    private final EpiRepositoryPort epis;
    private final UsuarioRepositorioPorta usuarios;
    private final ImpedimentoCapacitacaoPorta capacitacoes;
    private final LogAuditoriaRepositoryPort auditoria;

    public EmprestimoUseCase(EmprestimoRepositoryPort emprestimos, EpiRepositoryPort epis,
                             UsuarioRepositorioPorta usuarios, ImpedimentoCapacitacaoPorta capacitacoes) {
        this(emprestimos, epis, usuarios, capacitacoes, null);
    }

    @Autowired
    public EmprestimoUseCase(EmprestimoRepositoryPort emprestimos, EpiRepositoryPort epis,
                             UsuarioRepositorioPorta usuarios, ImpedimentoCapacitacaoPorta capacitacoes,
                             LogAuditoriaRepositoryPort auditoria) {
        this.emprestimos = emprestimos;
        this.epis = epis;
        this.usuarios = usuarios;
        this.capacitacoes = capacitacoes;
        this.auditoria = auditoria;
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
        Emprestimo emprestimo = Emprestimo.registrarEntrega(epi.getId(), beneficiario.getId(), operador.getId(),
                agora, entrada.dataDevolucaoPrevista(), termo, entrada.justificativaSubstituicao());

        MovimentacaoEstoque saida = epi.removerEstoque(Emprestimo.QUANTIDADE_POR_EMPRESTIMO,
                "Empréstimo ao colaborador " + beneficiario.getNome(), operador.getId());
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
        Emprestimo emprestimo = emprestimos.buscarPorId(emprestimoId)
                .orElseThrow(() -> new EmprestimoNaoEncontradoException(emprestimoId));
        Epi epi = epis.buscarPorIdComLock(emprestimo.getEpiId())
                .orElseThrow(() -> new EpiNaoEncontradoException(emprestimo.getEpiId()));
        Colaborador beneficiario = usuarios.buscarPorId(emprestimo.getColaboradorId()).orElse(null);

        EstadoDevolucao estado = entrada.estado();
        emprestimo.devolver(estado, operador.getId(), LocalDateTime.now());

        if (estado.retornaAoEstoque()) {
            MovimentacaoEstoque retorno = epi.adicionarEstoque(Emprestimo.QUANTIDADE_POR_EMPRESTIMO,
                    "Devolução do empréstimo " + emprestimoId + " (item apto)", operador.getId());
            epis.atualizarSaldo(epi);
            epis.salvarMovimentacao(retorno);
        }

        Emprestimo salvo = emprestimos.salvar(emprestimo);
        auditar(LogAuditoria.registrarAlteracao(operadorEmail, ENTIDADE_AUDITADA, salvo.getId(),
                "Devolução do EPI " + epi.getNome() + " com estado " + estado));
        return saida(salvo, epi, beneficiario);
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

    /** UC12, exceção III: mesmo tipo é a classificação da EspecificacaoEPI, conforme o diagrama de classes. */
    private void verificarDuplicidadeDeTipo(Epi epi, Colaborador beneficiario, RegistrarEntregaInputDTO entrada) {
        ClassificacaoEPI tipo = epi.getEspecificacao().getClassificacao();
        if (tipo == null) {
            return;
        }
        Optional<Emprestimo> duplicado = emprestimos.listarAtivosPorColaborador(beneficiario.getId()).stream()
                .filter(ativo -> epis.buscarPorId(ativo.getEpiId())
                        .map(outro -> tipo == outro.getEspecificacao().getClassificacao())
                        .orElse(false))
                .findFirst();
        if (duplicado.isEmpty()) {
            return;
        }
        boolean justificado = entrada.justificativaSubstituicao() != null
                && !entrada.justificativaSubstituicao().isBlank();
        if (!entrada.confirmarSubstituicao() || !justificado) {
            throw new DuplicidadeDePosseException(duplicado.get().getId(), tipo.name());
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
        if (auditoria != null) {
            auditoria.salvar(log);
        }
    }
}
