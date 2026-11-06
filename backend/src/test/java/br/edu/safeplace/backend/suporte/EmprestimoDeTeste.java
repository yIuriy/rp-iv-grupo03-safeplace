package br.edu.safeplace.backend.suporte;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.domain.emprestimo.ClassificacaoEmprestimo;
import br.edu.safeplace.backend.domain.emprestimo.Emprestimo;
import br.edu.safeplace.backend.domain.emprestimo.EstadoDevolucao;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Test Data Builder de um empréstimo já gravado (#226). Monta a entrega pelo {@link Emprestimo.Builder} de
 * produção e só nos testes simula o que vem do banco (id, devolução e situação, preenchidos pelo JPA), inclusive
 * combinações incoerentes que a validação precisa recusar.
 */
public final class EmprestimoDeTeste {

    private final Emprestimo.Builder entrega = new Emprestimo.Builder()
            .epi(ReferenciasDeTeste.epi(1))
            .beneficiario(ReferenciasDeTeste.pessoa(10))
            .responsavelEntrega(ReferenciasDeTeste.pessoa(1))
            .retiradaEm(LocalDateTime.now())
            .devolucaoPrevistaPara(LocalDate.now().plusDays(15))
            .termoCautela("Termo de cautela de teste");
    private Integer id;
    private LocalDateTime dataDevolucaoReal;
    private Colaborador responsavelDevolucao;
    private EstadoDevolucao estadoDevolucao;
    private ClassificacaoEmprestimo status;

    private EmprestimoDeTeste() {
    }

    public static EmprestimoDeTeste umEmprestimo() {
        return new EmprestimoDeTeste();
    }

    public EmprestimoDeTeste comId(Integer id) {
        this.id = id;
        return this;
    }

    public EmprestimoDeTeste comEpi(Epi epi) {
        entrega.epi(epi);
        return this;
    }

    public EmprestimoDeTeste comBeneficiario(Colaborador beneficiario) {
        entrega.beneficiario(beneficiario);
        return this;
    }

    public EmprestimoDeTeste comResponsavelEntrega(Colaborador responsavel) {
        entrega.responsavelEntrega(responsavel);
        return this;
    }

    public EmprestimoDeTeste retiradoEm(LocalDateTime dataRetirada) {
        entrega.retiradaEm(dataRetirada);
        return this;
    }

    public EmprestimoDeTeste comDevolucaoPrevistaPara(LocalDate data) {
        entrega.devolucaoPrevistaPara(data);
        return this;
    }

    public EmprestimoDeTeste comTermoCautela(String termo) {
        entrega.termoCautela(termo);
        return this;
    }

    public EmprestimoDeTeste comJustificativaSubstituicao(String justificativa) {
        entrega.justificativaSubstituicao(justificativa);
        return this;
    }

    /** Devolução gravada, sem as regras de {@code devolver}: permite montar registros incoerentes. */
    public EmprestimoDeTeste devolvidoEm(LocalDateTime dataHora, Colaborador responsavel, EstadoDevolucao estado) {
        this.dataDevolucaoReal = dataHora;
        this.responsavelDevolucao = responsavel;
        this.estadoDevolucao = estado;
        return this;
    }

    public EmprestimoDeTeste comStatus(ClassificacaoEmprestimo status) {
        this.status = status;
        return this;
    }

    public Emprestimo build() {
        Emprestimo emprestimo = entrega.build();
        ReflectionTestUtils.setField(emprestimo, "id", id);
        ReflectionTestUtils.setField(emprestimo, "dataDevolucaoReal", dataDevolucaoReal);
        ReflectionTestUtils.setField(emprestimo, "responsavelDevolucao", responsavelDevolucao);
        ReflectionTestUtils.setField(emprestimo, "estadoDevolucao", estadoDevolucao);
        if (status != null) {
            ReflectionTestUtils.setField(emprestimo, "status", status);
        }
        return emprestimo;
    }
}
