package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.ocorrencia.exception.AcaoPreventivaObrigatoriaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

/**
 * Classe {@code PlanoDeAcao} do diagrama de classes (RF07, UC07, US07).
 *
 * <p>Cada instância é uma ação do plano: o {@code idAcao} recebido pelas operações do diagrama
 * ({@code buscarAcao}, {@code validarProrrogacao}, {@code atualizarPrazo},
 * {@code atualizarResponsavel}) é o identificador desta classe, e o plano de uma ocorrência é o
 * conjunto de instâncias vinculadas a ela ({@code Ocorrencia} 1 para 0..* {@code PlanoDeAcao}). Os atributos
 * {@code medidasCorretivas}, {@code medidasPreventivas}, {@code prazo} e {@code status} são os do
 * modelo; responsável, alerta e vínculo com a ocorrência completam o que UC07 exige. O contrato
 * está em {@code docs/arquitetura/correspondencia-classes-plano-de-acao.md} (issue #97).</p>
 */
public class PlanoDeAcao {
    private final Integer id;
    private final Integer idOcorrencia;
    private final String medidasCorretivas;
    private final String medidasPreventivas;
    private LocalDate prazo;
    private String status;
    private Colaborador responsavelColaborador;
    private Setor responsavelSetor;
    private boolean alertaAtivo;
    private Integer antecedenciaAlertaDias;
    private final GestorDeSeguranca gestor;

    /**
     * Reconstitui uma ação já gravada. Não repete as validações temporais do cadastro: uma ação
     * aberta com prazo vencido continua válida e passa a constar como em atraso.
     */
    public PlanoDeAcao(
            Integer id,
            Integer idOcorrencia,
            String medidasCorretivas,
            String medidasPreventivas,
            LocalDate prazo,
            String status,
            Colaborador responsavelColaborador,
            Setor responsavelSetor,
            boolean alertaAtivo,
            Integer antecedenciaAlertaDias,
            GestorDeSeguranca gestor) {
        if (responsavelColaborador != null && responsavelSetor != null) {
            throw new PlanoDeAcaoInvalidoException(
                    "A ação deve ter um único responsável: colaborador ou setor, não ambos.");
        }
        validarConfiguracaoAlerta(alertaAtivo, antecedenciaAlertaDias);

        this.id = id;
        this.idOcorrencia = idOcorrencia;
        this.medidasCorretivas = normalizar(medidasCorretivas);
        this.medidasPreventivas = normalizar(medidasPreventivas);
        this.prazo = prazo;
        this.status = status != null && !status.isBlank() ? status.trim() : null;
        this.responsavelColaborador = responsavelColaborador;
        this.responsavelSetor = responsavelSetor;
        this.alertaAtivo = alertaAtivo;
        this.antecedenciaAlertaDias = alertaAtivo ? antecedenciaAlertaDias : null;
        this.gestor = gestor;
    }

    /**
     * Forma anterior ao contrato, ainda usada pelo plano embutido no cadastro de ocorrência
     * ({@code ocorrencias.plano_de_acao_id}). Preserva o texto de {@code status} como foi gravado;
     * a transição desse vínculo para {@code Ocorrencia} 1 para 0..* {@code PlanoDeAcao} fica em #98/#99.
     */
    public PlanoDeAcao(Integer id, String medidasCorretivas, LocalDate prazo, String status,
            String medidasPreventivas) {
        this(id, null, medidasCorretivas, medidasPreventivas, prazo, status, null, null, false, null, null);
    }

    /**
     * Fábrica da forma anterior ao contrato; ver o construtor de cinco argumentos.
     */
    public static PlanoDeAcao novo(String medidasCorretivas, LocalDate prazo, String status,
            String medidasPreventivas) {
        return new PlanoDeAcao(null, medidasCorretivas, prazo, status, medidasPreventivas);
    }

    /**
     * Define uma nova ação vinculada a uma ocorrência registrada (UC07, cenário principal, passo 5).
     * Informe exatamente um responsável: {@code responsavelColaborador} ou {@code responsavelSetor}.
     */
    public static PlanoDeAcao definir(
            Ocorrencia ocorrencia,
            String medidasCorretivas,
            String medidasPreventivas,
            LocalDate prazo,
            Colaborador responsavelColaborador,
            Setor responsavelSetor,
            boolean alertaAtivo,
            Integer antecedenciaAlertaDias,
            GestorDeSeguranca gestor,
            LocalDate dataAtual) {
        if (ocorrencia == null || ocorrencia.getId() == null) {
            throw new PlanoDeAcaoInvalidoException("A ação deve estar vinculada a uma ocorrência registrada.");
        }
        if (isVazio(medidasCorretivas) && isVazio(medidasPreventivas)) {
            throw new PlanoDeAcaoInvalidoException("Informe a medida corretiva ou a medida preventiva da ação.");
        }
        validarResponsavel(responsavelColaborador, responsavelSetor);
        validarPrazo(prazo, ocorrencia, dataAtual);

        return new PlanoDeAcao(
                null,
                ocorrencia.getId(),
                medidasCorretivas,
                medidasPreventivas,
                prazo,
                StatusPlanoDeAcao.ABERTA.name(),
                responsavelColaborador,
                responsavelSetor,
                alertaAtivo,
                antecedenciaAlertaDias,
                gestor);
    }

    /**
     * Operação {@code validarPlano} do diagrama: confere o conjunto de ações de uma ocorrência (as já
     * gravadas somadas às que serão salvas). Toda ocorrência grave exige ao menos uma ação
     * preventiva (UC07, regra 1).
     */
    public static void validarPlano(Ocorrencia ocorrencia, List<PlanoDeAcao> acoes) {
        if (ocorrencia == null || ocorrencia.getId() == null) {
            throw new PlanoDeAcaoInvalidoException("O plano deve estar vinculado a uma ocorrência registrada.");
        }
        if (acoes == null || acoes.isEmpty()) {
            throw new PlanoDeAcaoInvalidoException("O plano de ação deve conter ao menos uma ação.");
        }
        for (PlanoDeAcao acao : acoes) {
            if (!ocorrencia.getId().equals(acao.getIdOcorrencia())) {
                throw new PlanoDeAcaoInvalidoException(
                        "Todas as ações do plano devem pertencer à ocorrência " + ocorrencia.getId() + ".");
            }
        }
        if (ocorrencia.isGrave() && acoes.stream().noneMatch(PlanoDeAcao::possuiMedidaPreventiva)) {
            throw new AcaoPreventivaObrigatoriaException(ocorrencia.getId());
        }
    }

    /**
     * Operação {@code validarAtualizacaoStatus} do diagrama.
     */
    public boolean validarAtualizacaoStatus(StatusPlanoDeAcao novoStatus) {
        return novoStatus != null && getSituacao()
                .map(atual -> atual.podeTransicaoPara(novoStatus))
                .orElse(false);
    }

    /**
     * Registra a nova situação da ação, cuja gravação corresponde a {@code salvarStatusMedida}.
     */
    public void atualizarStatus(StatusPlanoDeAcao novoStatus) {
        if (!validarAtualizacaoStatus(novoStatus)) {
            throw new PlanoDeAcaoInvalidoException(String.format(
                    "Transição de situação da ação inválida de %s para %s.", status, novoStatus));
        }
        this.status = novoStatus.name();
    }

    /**
     * Operação {@code validarProrrogacao} do diagrama (UC07, cenário alternativo I).
     */
    public void validarProrrogacao(LocalDate novoPrazo, String justificativa, LocalDate dataAtual) {
        exigirAberta("prorrogar o prazo");
        if (isVazio(justificativa)) {
            throw new PlanoDeAcaoInvalidoException("A prorrogação do prazo exige justificativa técnica.");
        }
        if (prazo == null) {
            throw new PrazoAcaoInvalidoException("A ação não possui prazo definido para ser prorrogado.");
        }
        if (novoPrazo == null || !novoPrazo.isAfter(prazo)) {
            throw new PrazoAcaoInvalidoException("O novo prazo deve ser posterior ao prazo atual (" + prazo + ").");
        }
        if (novoPrazo.isBefore(exigirDataAtual(dataAtual))) {
            throw new PrazoAcaoInvalidoException("O novo prazo não pode ser anterior à data atual.");
        }
    }

    /**
     * Operação {@code atualizarPrazo} do diagrama. Só aceita prorrogação válida e devolve o registro
     * a ser preservado no histórico. A data do alerta é derivada do prazo, logo se reajusta sozinha.
     */
    public ProrrogacaoPrazo prorrogarPrazo(LocalDate novoPrazo, String justificativa, LocalDate dataAtual) {
        validarProrrogacao(novoPrazo, justificativa, dataAtual);
        ProrrogacaoPrazo prorrogacao = new ProrrogacaoPrazo(id, prazo, novoPrazo, justificativa.trim());
        this.prazo = novoPrazo;
        return prorrogacao;
    }

    /**
     * Operação {@code atualizarResponsavel(idAcao, novoResponsavel: Colaborador)} do diagrama
     * (UC07, cenário alternativo II). Devolve o aviso ao novo encarregado.
     */
    public AvisoAcao atualizarResponsavel(Colaborador novoResponsavel, LocalDate dataAtual) {
        exigirAberta("reatribuir o responsável");
        validarResponsavel(novoResponsavel, null);
        if (responsavelColaborador != null && Objects.equals(responsavelColaborador.getId(), novoResponsavel.getId())) {
            throw new PlanoDeAcaoInvalidoException("O colaborador informado já é o responsável pela ação.");
        }
        this.responsavelColaborador = novoResponsavel;
        this.responsavelSetor = null;
        return aviso(MotivoAvisoAcao.REATRIBUICAO, exigirDataAtual(dataAtual));
    }

    /**
     * Reatribuição a um setor, admitida por UC07 e US07 além do Colaborador do diagrama.
     */
    public AvisoAcao atualizarResponsavel(Setor novoResponsavel, LocalDate dataAtual) {
        exigirAberta("reatribuir o responsável");
        validarResponsavel(null, novoResponsavel);
        if (responsavelSetor != null && Objects.equals(responsavelSetor.getId(), novoResponsavel.getId())) {
            throw new PlanoDeAcaoInvalidoException("O setor informado já é o responsável pela ação.");
        }
        this.responsavelSetor = novoResponsavel;
        this.responsavelColaborador = null;
        return aviso(MotivoAvisoAcao.REATRIBUICAO, exigirDataAtual(dataAtual));
    }

    /**
     * Ativa ou desativa o alerta da ação e define sua antecedência em dias (UC07, regra 2). Não há
     * valor padrão: os três dias de UC07 são exemplo.
     */
    public void configurarAlerta(boolean ativo, Integer antecedenciaDias) {
        exigirAberta("configurar o alerta");
        validarConfiguracaoAlerta(ativo, antecedenciaDias);
        this.alertaAtivo = ativo;
        this.antecedenciaAlertaDias = ativo ? antecedenciaDias : null;
    }

    /**
     * Data a partir da qual o alerta é devido: prazo menos a antecedência configurada.
     */
    public Optional<LocalDate> getDataAlerta() {
        if (!alertaAtivo || prazo == null) {
            return Optional.empty();
        }
        return Optional.of(prazo.minusDays(antecedenciaAlertaDias));
    }

    /**
     * Indica se a ação aberta atingiu a antecedência configurada. Continua verdadeiro após o prazo
     * enquanto a execução não for confirmada.
     */
    public boolean deveEmitirAlerta(LocalDate dataReferencia) {
        LocalDate hoje = exigirDataAtual(dataReferencia);
        return isAberta() && getDataAlerta().map(data -> !hoje.isBefore(data)).orElse(false);
    }

    public Optional<AvisoAcao> emitirAvisoPrazo(LocalDate dataReferencia) {
        if (!deveEmitirAlerta(dataReferencia)) {
            return Optional.empty();
        }
        return Optional.of(aviso(MotivoAvisoAcao.PRAZO, dataReferencia));
    }

    public boolean isEmAtraso(LocalDate dataReferencia) {
        LocalDate hoje = exigirDataAtual(dataReferencia);
        return isAberta() && prazo != null && prazo.isBefore(hoje);
    }

    public boolean possuiMedidaCorretiva() {
        return medidasCorretivas != null;
    }

    public boolean possuiMedidaPreventiva() {
        return medidasPreventivas != null;
    }

    /**
     * Situação do atributo {@code status} no vocabulário do contrato. Vazio quando o texto gravado é
     * anterior ao contrato e ainda não foi normalizado (#98).
     */
    public Optional<StatusPlanoDeAcao> getSituacao() {
        return StatusPlanoDeAcao.reconhecer(status);
    }

    public boolean isAberta() {
        return getSituacao().filter(StatusPlanoDeAcao.ABERTA::equals).isPresent();
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public String getMedidasCorretivas() {
        return medidasCorretivas;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public String getStatus() {
        return status;
    }

    public String getMedidasPreventivas() {
        return medidasPreventivas;
    }

    public Colaborador getResponsavelColaborador() {
        return responsavelColaborador;
    }

    public Setor getResponsavelSetor() {
        return responsavelSetor;
    }

    public boolean isAlertaAtivo() {
        return alertaAtivo;
    }

    public Integer getAntecedenciaAlertaDias() {
        return antecedenciaAlertaDias;
    }

    public GestorDeSeguranca getGestor() {
        return gestor;
    }

    private AvisoAcao aviso(MotivoAvisoAcao motivo, LocalDate dataAviso) {
        return new AvisoAcao(id, idOcorrencia, motivo, responsavelColaborador, responsavelSetor, prazo, dataAviso);
    }

    private void exigirAberta(String operacao) {
        if (!isAberta()) {
            String situacao = getSituacao().map(Enum::name)
                    .orElse("não reconhecida (" + status + ")");
            throw new PlanoDeAcaoInvalidoException(
                    "Não é possível " + operacao + " de uma ação com situação " + situacao + ".");
        }
    }

    private static void validarResponsavel(Colaborador colaborador, Setor setor) {
        if ((colaborador == null) == (setor == null)) {
            throw new PlanoDeAcaoInvalidoException(
                    "Informe um único responsável pela ação: colaborador ou setor.");
        }
        Integer idResponsavel = colaborador != null ? colaborador.getId() : setor.getId();
        if (idResponsavel == null) {
            throw new PlanoDeAcaoInvalidoException("O responsável pela ação deve estar cadastrado.");
        }
    }

    private static void validarPrazo(LocalDate prazo, Ocorrencia ocorrencia, LocalDate dataAtual) {
        if (prazo == null) {
            throw new PrazoAcaoInvalidoException("O prazo da ação é obrigatório.");
        }
        if (prazo.isBefore(exigirDataAtual(dataAtual))) {
            throw new PrazoAcaoInvalidoException("O prazo da ação não pode ser anterior à data atual.");
        }
        if (prazo.isBefore(ocorrencia.getDataOcorrencia().toLocalDate())) {
            throw new PrazoAcaoInvalidoException("O prazo da ação não pode ser anterior à data da ocorrência.");
        }
    }

    private static void validarConfiguracaoAlerta(boolean ativo, Integer antecedenciaDias) {
        if (!ativo) {
            return;
        }
        if (antecedenciaDias == null) {
            throw new PlanoDeAcaoInvalidoException("Informe a antecedência do alerta em dias.");
        }
        if (antecedenciaDias < 0) {
            throw new PlanoDeAcaoInvalidoException("A antecedência do alerta não pode ser negativa.");
        }
    }

    private static LocalDate exigirDataAtual(LocalDate dataAtual) {
        if (dataAtual == null) {
            throw new IllegalArgumentException("A data de referência é obrigatória.");
        }
        return dataAtual;
    }

    private static boolean isVazio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static String normalizar(String texto) {
        return isVazio(texto) ? null : texto.trim();
    }
}
