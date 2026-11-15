/*
 * Porta de entrada "controlar empréstimos" prevista na seção 7.1 da especificação do MVP (RF11,
 * UC12, US11). Entrega, devolução e consulta de posse, pendências e atrasos de EPIs emprestados a
 * colaboradores cadastrados, operados por Supervisor ou Gestor de Segurança.
 */
package br.edu.safeplace.backend.application.port.in;

import java.util.List;

import br.edu.safeplace.backend.application.dto.input.RegistrarDevolucaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarEntregaInputDTO;
import br.edu.safeplace.backend.application.dto.output.EmprestimoOutputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;

public interface ControlarEmprestimoUseCase {

    /** Filtro da consulta de empréstimos (US11, critérios 2 e 3). */
    enum Situacao {
        TODOS,
        /** Empréstimos ativos, dentro ou fora do prazo. */
        PENDENTES,
        /** Empréstimos ativos com a data prevista de devolução ultrapassada. */
        ATRASADOS
    }

    EmprestimoOutputDTO registrarEntrega(RegistrarEntregaInputDTO entrada, String operadorEmail);

    EmprestimoOutputDTO registrarDevolucao(Integer emprestimoId, RegistrarDevolucaoInputDTO entrada,
                                           String operadorEmail);

    EmprestimoOutputDTO buscarPorId(Integer emprestimoId);

    List<EmprestimoOutputDTO> listar(Situacao situacao, Integer colaboradorId);

    /** Passo 4 do UC12: EPIs disponíveis para empréstimo (situação DISPONIVEL, saldo e CA válido). */
    List<EpiOutputDTO> listarEpisDisponiveis();
}
