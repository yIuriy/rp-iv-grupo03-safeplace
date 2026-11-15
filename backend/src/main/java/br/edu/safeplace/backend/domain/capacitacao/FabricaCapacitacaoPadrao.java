package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * ConcreteCreator da {@link FabricaCapacitacao} (#268), registrado como bean em {@code ServicosDominioConfig}.
 */
public class FabricaCapacitacaoPadrao implements FabricaCapacitacao {

    @Override
    public Certificacao criarCertificacao(
            CadastrarCertificacaoInputDTO entrada,
            Colaborador colaborador,
            LocalDate dataReferencia) {
        if (entrada == null) {
            throw new IllegalArgumentException("Dados de entrada da certificação não podem ser nulos.");
        }
        return new Certificacao(
                null,
                entrada.nome(),
                entrada.dataVencimento(),
                entrada.dataEmissao(),
                colaborador,
                dataReferencia);
    }

    @Override
    public Treinamento criarTreinamento(
            CadastrarTreinamentoInputDTO entrada,
            List<Colaborador> colaboradores) {
        if (entrada == null) {
            throw new IllegalArgumentException("Dados de entrada do treinamento não podem ser nulos.");
        }
        return new Treinamento(
                null,
                entrada.nome(),
                entrada.conteudo(),
                entrada.instrutor(),
                entrada.dataRealizacao(),
                entrada.cargaHoraria(),
                entrada.validadeMeses(),
                colaboradores);
    }

    @Override
    public ExigenciaCapacitacao criarExigencia(DefinirExigenciaInputDTO entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("Dados de entrada da exigência não podem ser nulos.");
        }
        return new ExigenciaCapacitacao(
                null,
                entrada.nomeCapacitacao(),
                entrada.tipoCapacitacao(),
                entrada.alvoId(),
                entrada.alvo());
    }
}
