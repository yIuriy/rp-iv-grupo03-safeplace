/*
 * Tratador global das exceções da API (#234): um único handler por tipo de exceção, para que a resposta não
 * dependa da ordem em que o Spring registra os advices. Os advices com escopo (tarefa e empréstimo) ficam só
 * para os corpos com campos extras (#249); erros que nenhum advice trata caem no ErroInesperadoExceptionHandler.
 * A ordem entre eles está em OrdemAdvices.
 */
package br.edu.safeplace.backend.adapters.in.web;

import static br.edu.safeplace.backend.adapters.in.web.CorpoErro.corpo;

import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoNaoEncontradaException;
import br.edu.safeplace.backend.domain.area_risco.exception.AreaRiscoSemEpiObrigatorioException;
import br.edu.safeplace.backend.domain.area_risco.exception.CodigoAreaRiscoDuplicadoException;
import br.edu.safeplace.backend.domain.capacitacao.exception.ColaboradorNaoAlocavelException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoJaEncerradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EmprestimoNaoEncontradoException;
import br.edu.safeplace.backend.domain.emprestimo.exception.EntregaImpedidaException;
import br.edu.safeplace.backend.domain.epi.exception.EpiIndisponivelParaManutencaoException;
import br.edu.safeplace.backend.domain.epi.exception.EpiNaoEncontradoException;
import br.edu.safeplace.backend.domain.epi.exception.SaldoInsuficienteException;
import br.edu.safeplace.backend.domain.epi.exception.TransicaoStatusEpiInvalidaException;
import br.edu.safeplace.backend.domain.inspecao.exception.InspecaoInvalidaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPlanoNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.ProtocoloCATInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaNaoEncontradaException;
import br.edu.safeplace.backend.domain.tarefa.exception.TarefaSemClassificacaoException;
import br.edu.safeplace.backend.domain.usuario.exception.AcessoNegadoException;
import br.edu.safeplace.backend.domain.usuario.exception.CpfInvalidoException;
import br.edu.safeplace.backend.domain.usuario.exception.CpfJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.CredenciaisInvalidasException;
import br.edu.safeplace.backend.domain.usuario.exception.EmailJaCadastradoException;
import br.edu.safeplace.backend.domain.usuario.exception.UsuarioNaoEncontradoException;

@RestControllerAdvice
@Order(OrdemAdvices.GLOBAL)
public class ApiExceptionHandler {

    private static final String NAO_ENCONTRADO = "Não Encontrado";

    // Requisição

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> requisicaoInvalida(IllegalArgumentException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Requisição Inválida", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> operacaoInvalida(IllegalStateException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Operação Inválida", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacaoFalhou(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .findFirst()
                .orElse("Dados de entrada inválidos.");
        return corpo(HttpStatus.BAD_REQUEST, "Validação Falhou", mensagem);
    }

    // Usuário e acesso

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, Object>> acessoNegado(AcessoNegadoException ex) {
        return corpo(HttpStatus.FORBIDDEN, "Acesso Proibido", ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, Object>> credenciaisInvalidas(CredenciaisInvalidasException ex) {
        return corpo(HttpStatus.UNAUTHORIZED, "Não Autorizado", ex.getMessage());
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> usuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler({CpfJaCadastradoException.class, EmailJaCadastradoException.class})
    public ResponseEntity<Map<String, Object>> conflitoDeDados(RuntimeException ex) {
        return corpo(HttpStatus.CONFLICT, "Conflito de Dados", ex.getMessage());
    }

    @ExceptionHandler(CpfInvalidoException.class)
    public ResponseEntity<Map<String, Object>> cpfInvalido(CpfInvalidoException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "CPF Inválido", ex.getMessage());
    }

    // Área de risco (UC03)

    @ExceptionHandler(AreaRiscoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> areaRiscoNaoEncontrada(AreaRiscoNaoEncontradaException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler(CodigoAreaRiscoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> codigoAreaRiscoDuplicado(CodigoAreaRiscoDuplicadoException ex) {
        return corpo(HttpStatus.CONFLICT, "Código Já Em Uso", ex.getMessage());
    }

    @ExceptionHandler(AreaRiscoSemEpiObrigatorioException.class)
    public ResponseEntity<Map<String, Object>> areaRiscoSemEpiObrigatorio(AreaRiscoSemEpiObrigatorioException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "EPI Obrigatório Ausente", ex.getMessage());
    }

    // EPI

    @ExceptionHandler(EpiNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> epiNaoEncontrado(EpiNaoEncontradoException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> saldoInsuficiente(SaldoInsuficienteException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Saldo Insuficiente", ex.getMessage());
    }

    @ExceptionHandler(EpiIndisponivelParaManutencaoException.class)
    public ResponseEntity<Map<String, Object>> epiIndisponivelParaManutencao(
            EpiIndisponivelParaManutencaoException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "EPI Indisponível para Manutenção", ex.getMessage());
    }

    @ExceptionHandler(TransicaoStatusEpiInvalidaException.class)
    public ResponseEntity<Map<String, Object>> transicaoStatusEpiInvalida(TransicaoStatusEpiInvalidaException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Transição de Status Inválida", ex.getMessage());
    }

    // Tarefa e alocação (UC11, RF10)

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> tarefaNaoEncontrada(TarefaNaoEncontradaException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler(TarefaSemClassificacaoException.class)
    public ResponseEntity<Map<String, Object>> tarefaSemClassificacao(TarefaSemClassificacaoException ex) {
        return corpo(HttpStatus.CONFLICT, "Tarefa Sem Classificação", ex.getMessage());
    }

    @ExceptionHandler(ColaboradorNaoAlocavelException.class)
    public ResponseEntity<Map<String, Object>> colaboradorNaoAlocavel(ColaboradorNaoAlocavelException ex) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "Colaborador Não Alocável", ex.getMessage());
    }

    // Empréstimo (UC12): 404 inexistente, 422 entrega impedida, 409 já encerrado. A duplicidade de posse,
    // que leva o campo emprestimoAtivoId, fica no EmprestimoExceptionHandler.

    @ExceptionHandler(EmprestimoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> emprestimoNaoEncontrado(EmprestimoNaoEncontradoException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler(EntregaImpedidaException.class)
    public ResponseEntity<Map<String, Object>> entregaImpedida(EntregaImpedidaException ex) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "Entrega Impedida", ex.getMessage());
    }

    @ExceptionHandler(EmprestimoJaEncerradoException.class)
    public ResponseEntity<Map<String, Object>> emprestimoJaEncerrado(EmprestimoJaEncerradoException ex) {
        return corpo(HttpStatus.CONFLICT, "Empréstimo Encerrado", ex.getMessage());
    }

    // Inspeção (US09, #134): registro recusado pela regra do domínio.

    @ExceptionHandler(InspecaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> inspecaoInvalida(InspecaoInvalidaException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Inspeção Inválida", ex.getMessage());
    }

    // Ocorrência

    @ExceptionHandler(OcorrenciaNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> ocorrenciaNaoEncontrada(OcorrenciaNaoEncontradaException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler(TransicaoStatusInvalidaException.class)
    public ResponseEntity<Map<String, Object>> transicaoStatusInvalida(TransicaoStatusInvalidaException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Transição de Status Inválida", ex.getMessage());
    }

    @ExceptionHandler(ProtocoloCATInvalidoException.class)
    public ResponseEntity<Map<String, Object>> protocoloCatInvalido(ProtocoloCATInvalidoException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Protocolo CAT Inválido", ex.getMessage());
    }

    @ExceptionHandler(DataOcorrenciaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> dataOcorrenciaInvalida(DataOcorrenciaInvalidaException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Data de Ocorrência Inválida", ex.getMessage());
    }

    @ExceptionHandler(DadosOcorrenciaInvalidosException.class)
    public ResponseEntity<Map<String, Object>> dadosOcorrenciaInvalidos(DadosOcorrenciaInvalidosException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Dados da Ocorrência Inválidos", ex.getMessage());
    }

    // Plano de ação

    @ExceptionHandler(AcaoPlanoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> acaoPlanoNaoEncontrada(AcaoPlanoNaoEncontradaException ex) {
        return corpo(HttpStatus.NOT_FOUND, NAO_ENCONTRADO, ex.getMessage());
    }

    @ExceptionHandler(AcaoPreventivaObrigatoriaException.class)
    public ResponseEntity<Map<String, Object>> acaoPreventivaObrigatoria(AcaoPreventivaObrigatoriaException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Ação Preventiva Obrigatória", ex.getMessage());
    }

    @ExceptionHandler(PlanoDeAcaoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> planoDeAcaoInvalido(PlanoDeAcaoInvalidoException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Plano de Ação Inválido", ex.getMessage());
    }

    @ExceptionHandler(PrazoAcaoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> prazoAcaoInvalido(PrazoAcaoInvalidoException ex) {
        return corpo(HttpStatus.BAD_REQUEST, "Prazo Inválido", ex.getMessage());
    }
}
