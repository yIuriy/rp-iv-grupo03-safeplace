package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PlanoDeAcaoInvalidoException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Classe {@code PlanoDeAcao} do diagrama de classes (RF07, UC07, US07) e entidade JPA da tabela
 * {@code planos_de_acao} (#262, decisão #254).
 *
 * <p>Cada instância é uma ação do plano vinculada à ocorrência ({@code Ocorrencia} 1 para 0..*
 * {@code PlanoDeAcao}), ao responsável único ({@code Colaborador} ou {@code Setor}) e opcionalmente ao
 * {@code GestorDeSeguranca}. {@link #getIdOcorrencia()} serve aos DTOs e validadores que expõem ids.</p>
 */
@Entity
@Table(name = "planos_de_acao")
public class PlanoDeAcao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocorrencia_id")
    private Ocorrencia ocorrencia;

    @Column(name = "medidas_corretivas", columnDefinition = "TEXT")
    private String medidasCorretivas;

    @Column(name = "medidas_preventivas", columnDefinition = "TEXT")
    private String medidasPreventivas;

    private LocalDate prazo;

    @Column(length = 80)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colaborador_id")
    private Colaborador responsavelColaborador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id")
    private Setor responsavelSetor;

    @Column(name = "alerta_ativo", nullable = false)
    private boolean alertaAtivo;

    @Column(name = "antecedencia_alerta_dias")
    private Integer antecedenciaAlertaDias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id")
    private GestorDeSeguranca gestor;

    /** Exclusivo do JPA (decisão #254). */
    protected PlanoDeAcao() {
    }

    /** Construção exclusiva via {@link PlanoDeAcaoBuilder} (#262). */
    PlanoDeAcao(PlanoDeAcaoBuilder builder) {
        // Ocorrência, medidas, responsável único, prazo e alerta são validados por AcaoPlanoValidador (#266).
        this.id = builder.getId();
        this.ocorrencia = builder.getOcorrencia();
        this.medidasCorretivas = normalizar(builder.getMedidasCorretivas());
        this.medidasPreventivas = normalizar(builder.getMedidasPreventivas());
        this.prazo = builder.getPrazo();
        String statusInformado = builder.getStatus();
        this.status = statusInformado != null && !statusInformado.isBlank() ? statusInformado.trim() : null;
        this.responsavelColaborador = builder.getResponsavelColaborador();
        this.responsavelSetor = builder.getResponsavelSetor();
        this.alertaAtivo = builder.isAlertaAtivo();
        this.antecedenciaAlertaDias = builder.isAlertaAtivo() ? builder.getAntecedenciaAlertaDias() : null;
        this.gestor = builder.getGestor();
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
        ProrrogacaoPrazo prorrogacao = new ProrrogacaoPrazo(this, prazo, novoPrazo, justificativa.trim());
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

    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public Integer getIdOcorrencia() {
        return ocorrencia != null ? ocorrencia.getId() : null;
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
        return new AvisoAcao(this, ocorrencia, motivo, responsavelColaborador, responsavelSetor, prazo, dataAviso);
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

    /** Identidade pelo id gerado (decisão #254, seção 2.3); sem id, só o próprio objeto é igual. */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof PlanoDeAcao plano)) {
            return false;
        }
        return getId() != null && getId().equals(plano.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
