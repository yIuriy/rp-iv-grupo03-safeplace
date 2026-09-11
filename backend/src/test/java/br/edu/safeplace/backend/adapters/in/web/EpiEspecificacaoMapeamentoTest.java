package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.CadastrarEpiInputDTO;
import br.edu.safeplace.backend.application.dto.output.EpiOutputDTO;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EpiEspecificacaoMapeamentoTest {

    @Test
    void devePreservarEspecificacaoNasConversoes() {
        LocalDate cadastro = LocalDate.of(2026, 9, 11);

        CriarEpiRequest request = new CriarEpiRequest(
                "Óculos",
                "1234",
                10,
                2,
                cadastro.plusYears(1),
                365,
                "Lentes transparentes",
                ClassificacaoEPI.PROTECAO_DE_OLHOS
        );

        CadastrarEpiInputDTO input = request.toInputDTO();

        assertThat(input.descricao()).isEqualTo(request.descricao());
        assertThat(input.classificacao()).isEqualTo(request.classificacao());

        Epi epi = Epi.novo(
                input.nome(),
                input.numeroCa(),
                input.quantidade(),
                input.estoqueMinimo(),
                input.dataValidadeCa(),
                input.vidaUtilDias(),
                cadastro,
                input.descricao(),
                input.classificacao()
        );

        EpiResponse resposta = EpiResponse.fromOutputDTO(
                EpiOutputDTO.deDominio(epi)
        );

        assertThat(resposta.descricao()).isEqualTo(request.descricao());
        assertThat(resposta.classificacao()).isEqualTo(request.classificacao());
        assertThat(EpiResponse.fromDomain(epi)).isEqualTo(resposta);
    }
}