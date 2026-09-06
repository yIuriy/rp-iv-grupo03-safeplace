package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ocorrencias")
@Tag(name = "Ocorrências", description = "Endpoints para registro e acompanhamento de acidentes e incidentes")
public class OcorrenciaController {
    private final RegistrarOcorrenciaUseCase useCase;

    public OcorrenciaController(RegistrarOcorrenciaUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma nova ocorrência (ACIDENTE ou INCIDENTE)")
    public OcorrenciaResponse criar(@Valid @RequestBody CriarOcorrenciaRequest request) {
        if ("ACIDENTE".equalsIgnoreCase(request.tipoOcorrencia())) {
            return OcorrenciaResponse.fromOutputDTO(useCase.registrarAcidente(request.toAcidenteInputDTO()));
        }
        if ("INCIDENTE".equalsIgnoreCase(request.tipoOcorrencia())) {
            return OcorrenciaResponse.fromOutputDTO(useCase.registrarIncidente(request.toIncidenteInputDTO()));
        }
        throw new IllegalArgumentException("tipoOcorrencia deve ser ACIDENTE ou INCIDENTE.");
    }

    @GetMapping()
    @Operation(summary = "Lista todas as ocorrências registradas")
    public List<OcorrenciaResponse> listar() {
        return useCase
                .listar()
                .stream()
                .map(OcorrenciaResponse::fromOutputDTO)
                .toList();
    }
}
