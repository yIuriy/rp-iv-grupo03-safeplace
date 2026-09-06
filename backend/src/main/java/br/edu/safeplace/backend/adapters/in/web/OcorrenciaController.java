package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {
    private final RegistrarOcorrenciaUseCase useCase;

    public OcorrenciaController(RegistrarOcorrenciaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OcorrenciaResponse criar(@Valid @RequestBody CriarOcorrenciaRequest request) {
        if ("ACIDENTE".equals(request.tipoOcorrencia())) {
            Acidente acidente = new Acidente(
                    null,
                    request.dataOcorrencia(),
                    request.local(),
                    request.descricao(),
                    toPlanoDeAcao(request.planoDeAcao()),
                    request.causaRaiz(),
                    request.tipo(),
                    request.dano(),
                    request.numeroProtocolo(),
                    request.destino());

            return OcorrenciaResponse.fromDomain(useCase.registrarAcidente(acidente));
        }
        if ("INCIDENTE".equalsIgnoreCase(request.tipoOcorrencia())) {
            Incidente incidente = new Incidente(
                    null,
                    request.dataOcorrencia(),
                    request.local(),
                    request.descricao(),
                    toPlanoDeAcao(request.planoDeAcao()),
                    request.situacaoRisco(),
                    request.potencialDano());

            return OcorrenciaResponse.fromDomain(useCase.registrarIncidente(incidente));
        }
        throw new IllegalArgumentException("tipoOcorrencia deve ser ACIDENTE ou INCIDENTE.");
    }

    @GetMapping()
    public List<OcorrenciaResponse> listar() {
        return useCase
                .listar()
                .stream()
                .map(OcorrenciaResponse::fromDomain)
                .toList();
    }

    private PlanoDeAcao toPlanoDeAcao(PlanoDeAcaoRequest request) {
        if (request == null)
            return null;

        return new PlanoDeAcao(
                null,
                request.medidasCorretivas(),
                request.prazo(),
                request.status(),
                request.medidasPreventivas());
    }
}
