package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Relato de acidente ou incidente (UC05, UC09). Testemunhas e mídias pertencem a UC04, fora do MVP,
 * e não fazem parte do modelo (issue #195); o vínculo de evidências fica para a extensão futura.
 */
public abstract class Ocorrencia {
    private final Integer idOcorrencia;
    private final String descricao;
    private final LocalDateTime dataOcorrencia;
    private final LocalDateTime dataRegistro;
    private StatusOcorrencia statusOcorrencia;
    private final Colaborador colaborador;
    private final AreaRisco area;

    /**
     * Usuário autenticado que registrou o relato, Gestor de Segurança ou Supervisor (RF13, UC09).
     * Definido só pelo servidor (issue #195); nunca vem da requisição.
     */
    private final Colaborador relator;
    private final PlanoDeAcao planoDeAcao;

    /**
     * Texto livre herdado da modelagem anterior, mantido apenas como rótulo descritivo dos
     * registros legados. A localização estrutural do fato é dada exclusivamente pela associação
     * {@code area : AreaRisco}, conforme a decisão 2.3 da correspondência de classes/DS09, e este
     * texto nunca é promovido a uma {@link AreaRisco}. Fora do MVP: o relato não recebe mais este
     * campo (issue #195), que só é lido nos registros antigos.
     */
    private final String localInformado;

    protected Ocorrencia(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            PlanoDeAcao planoDeAcao) {
        this(idOcorrencia, descricao, dataOcorrencia, dataRegistro, statusOcorrencia,
                colaborador, area, relator, null, planoDeAcao);
    }

    protected Ocorrencia(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            Colaborador colaborador,
            AreaRisco area,
            Colaborador relator,
            String local,
            PlanoDeAcao planoDeAcao) {

        if (dataOcorrencia == null) {
            throw new IllegalArgumentException("Data da ocorrência é obrigatória.");
        }
        if (dataOcorrencia.isAfter(LocalDateTime.now())) {
            throw new DataOcorrenciaInvalidaException("Data da ocorrência não pode ser futura.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória.");
        }

        this.idOcorrencia = idOcorrencia;
        this.descricao = descricao.trim();
        this.dataOcorrencia = dataOcorrencia;
        this.dataRegistro = dataRegistro != null ? dataRegistro : LocalDateTime.now();
        this.statusOcorrencia = statusOcorrencia != null ? statusOcorrencia : StatusOcorrencia.ABERTA;
        this.colaborador = colaborador;
        this.area = area;
        this.relator = relator;
        this.localInformado = local != null && !local.isBlank() ? local.trim() : null;
        this.planoDeAcao = planoDeAcao;
    }

    public void transicionarPara(StatusOcorrencia novoStatus) {
        this.statusOcorrencia.validarTransicao(novoStatus);
        this.statusOcorrencia = novoStatus;
    }

    public void enviarParaTriagem() {
        transicionarPara(StatusOcorrencia.EM_TRIAGEM);
    }

    public void arquivar() {
        transicionarPara(StatusOcorrencia.ARQUIVADA);
    }

    /**
     * Indica se a ocorrência é grave para a regra 1 de UC07 (exige ação preventiva no plano). Só há
     * escala aprovada em {@code Acidente.gravidade}; {@code Incidente.potencialDano} é texto livre e
     * não é convertido em gravidade por suposição, então a regra não se aplica automaticamente a
     * incidentes (decisão 2.4 da correspondência do plano de ação).
     */
    public boolean isGrave() {
        return false;
    }

    public Integer getId() {
        return idOcorrencia;
    }

    public Integer getIdOcorrencia() {
        return idOcorrencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataOcorrencia() {
        return dataOcorrencia;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public StatusOcorrencia getStatusOcorrencia() {
        return statusOcorrencia;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public AreaRisco getArea() {
        return area;
    }

    public Colaborador getRelator() {
        return relator;
    }

    /**
     * Nome do lugar do fato: vem da área de risco associada e, apenas quando não há área, recai
     * sobre o texto livre legado. Nunca se constrói uma {@link AreaRisco} a partir desse texto.
     */
    public String getLocal() {
        return area != null ? area.getNome() : localInformado;
    }

    /**
     * Texto livre legado exatamente como foi informado, sem o fallback para o nome da área.
     */
    public String getLocalInformado() {
        return localInformado;
    }

    public PlanoDeAcao getPlanoDeAcao() {
        return planoDeAcao;
    }
}
