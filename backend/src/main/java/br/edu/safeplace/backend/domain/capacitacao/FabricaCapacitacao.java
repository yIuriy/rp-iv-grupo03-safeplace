package br.edu.safeplace.backend.domain.capacitacao;

import java.time.LocalDate;
import java.util.List;

import br.edu.safeplace.backend.application.dto.input.CadastrarCertificacaoInputDTO;
import br.edu.safeplace.backend.application.dto.input.CadastrarTreinamentoInputDTO;
import br.edu.safeplace.backend.application.dto.input.DefinirExigenciaInputDTO;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

/**
 * Creator do Factory Method (#268): expõe um método por produto de capacitação,
 * recebendo o objeto de entrada inteiro sem desmontá-lo no caso de uso.
 * Não executa validações de negócio; estas rodam na camada {@code validacao} antes do repository (#265).
 */
public interface FabricaCapacitacao {

    Certificacao criarCertificacao(
            CadastrarCertificacaoInputDTO entrada,
            Colaborador colaborador,
            LocalDate dataReferencia);

    Treinamento criarTreinamento(
            CadastrarTreinamentoInputDTO entrada,
            List<Colaborador> colaboradores);

    ExigenciaCapacitacao criarExigencia(DefinirExigenciaInputDTO entrada);
}
