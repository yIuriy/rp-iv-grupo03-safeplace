/*
 * Registra no Spring os serviços de domínio, que não usam anotações do framework. Os casos de uso
 * recebem esses serviços pelas interfaces do domínio (issue #267).
 */
package br.edu.safeplace.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.edu.safeplace.backend.domain.capacitacao.FabricaCapacitacao;
import br.edu.safeplace.backend.domain.capacitacao.FabricaCapacitacaoPadrao;
import br.edu.safeplace.backend.domain.epi.CalculoProjecaoSubstituicao;
import br.edu.safeplace.backend.domain.epi.FabricaEpi;
import br.edu.safeplace.backend.domain.epi.FabricaEpiPadrao;
import br.edu.safeplace.backend.domain.epi.SubstituicaoInteligenteService;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.FabricaRelatoIncidente;

@Configuration
public class ServicosDominioConfig {

    @Bean
    public CalculoProjecaoSubstituicao calculoProjecaoSubstituicao() {
        return new SubstituicaoInteligenteService();
    }

    @Bean
    public FabricaEpi fabricaEpi() {
        return new FabricaEpiPadrao();
    }

    @Bean
    public FabricaCapacitacao fabricaCapacitacao() {
        return new FabricaCapacitacaoPadrao();
    }

    @Bean
    public FabricaRelatoAcidente fabricaRelatoAcidente() {
        return new FabricaRelatoAcidente();
    }

    @Bean
    public FabricaRelatoIncidente fabricaRelatoIncidente() {
        return new FabricaRelatoIncidente();
    }
}
