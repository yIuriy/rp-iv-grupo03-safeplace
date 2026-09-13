package br.edu.safeplace.backend.domain.ocorrencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DataOcorrenciaInvalidaException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

public abstract class Ocorrencia {
    private final Integer idOcorrencia;
    private final String descricao;
    private final LocalDateTime dataOcorrencia;
    private final LocalDateTime dataRegistro;
    private StatusOcorrencia statusOcorrencia;
    private final List<String> testemunhas;
    private final Colaborador colaborador;
    private final List<String> midias;
    private final AreaRisco area;
    private final GestorDeSeguranca gestor;
    private final String local;
    private final PlanoDeAcao planoDeAcao;

    protected Ocorrencia(
            Integer idOcorrencia,
            String descricao,
            LocalDateTime dataOcorrencia,
            LocalDateTime dataRegistro,
            StatusOcorrencia statusOcorrencia,
            List<String> testemunhas,
            Colaborador colaborador,
            List<String> midias,
            AreaRisco area,
            GestorDeSeguranca gestor,
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
        this.testemunhas = testemunhas != null ? List.copyOf(testemunhas) : Collections.emptyList();
        this.colaborador = colaborador;
        this.midias = midias != null ? List.copyOf(midias) : Collections.emptyList();
        this.area = area;
        this.gestor = gestor;
        this.local = local != null && !local.isBlank() ? local.trim() : (area != null ? area.getNome() : null);
        this.planoDeAcao = planoDeAcao;
    }

    /**
     * Construtor de compatibilidade.
     */
    protected Ocorrencia(
            Integer idOcorrencia,
            LocalDateTime dataOcorrencia,
            String local,
            String descricao,
            PlanoDeAcao planoDeAcao) {
        this(
                idOcorrencia,
                descricao,
                dataOcorrencia,
                LocalDateTime.now(),
                StatusOcorrencia.ABERTA,
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                local != null ? AreaRisco.novo(local, null, null) : null,
                null,
                local,
                planoDeAcao
        );
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

    public List<String> getTestemunhas() {
        return testemunhas;
    }

    public Colaborador getColaborador() {
        return colaborador;
    }

    public List<String> getMidias() {
        return midias;
    }

    public AreaRisco getArea() {
        return area;
    }

    public GestorDeSeguranca getGestor() {
        return gestor;
    }

    public String getLocal() {
        return local;
    }

    public PlanoDeAcao getPlanoDeAcao() {
        return planoDeAcao;
    }
}