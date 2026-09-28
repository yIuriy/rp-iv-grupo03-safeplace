package br.edu.safeplace.backend.adapters.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ocorrencias")
@Tag(name = "Ocorrências", description = "Endpoints para registro e acompanhamento de acidentes e incidentes")
public class OcorrenciaController {
    private final RegistrarOcorrenciaUseCase registrarUseCase;
    private final GerenciarOcorrenciaUseCase gerenciarUseCase;

    public OcorrenciaController(
            RegistrarOcorrenciaUseCase registrarUseCase,
            GerenciarOcorrenciaUseCase gerenciarUseCase) {
        this.registrarUseCase = registrarUseCase;
        this.gerenciarUseCase = gerenciarUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma nova ocorrência (ACIDENTE ou INCIDENTE)")
    public OcorrenciaResponse criar(@Valid @RequestBody CriarOcorrenciaRequest request) {
        if ("ACIDENTE".equalsIgnoreCase(request.tipoOcorrencia())) {
            return OcorrenciaResponse.fromOutputDTO(registrarUseCase.registrarAcidente(request.toAcidenteInputDTO()));
        }
        if ("INCIDENTE".equalsIgnoreCase(request.tipoOcorrencia())) {
            return OcorrenciaResponse.fromOutputDTO(registrarUseCase.registrarIncidente(request.toIncidenteInputDTO()));
        }
        throw new IllegalArgumentException("tipoOcorrencia deve ser ACIDENTE ou INCIDENTE.");
    }

    @GetMapping
    @Operation(summary = "Lista ocorrências registradas (filtradas por relator para Supervisor, visão global para Gestor)")
    public List<OcorrenciaResponse> listar() {
        return registrarUseCase
                .listar()
                .stream()
                .map(OcorrenciaResponse::fromOutputDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de uma ocorrência pelo identificador")
    public OcorrenciaResponse buscarPorId(@PathVariable Integer id) {
        return OcorrenciaResponse.fromOutputDTO(gerenciarUseCase.buscarPorId(id));
    }

    @GetMapping("/protocolo/{protocolo}")
    @Operation(summary = "Busca uma ocorrência pelo número de protocolo CAT")
    public OcorrenciaResponse buscarPorProtocolo(@PathVariable String protocolo) {
        return OcorrenciaResponse.fromOutputDTO(gerenciarUseCase.buscarPorProtocolo(protocolo));
    }

    @PatchMapping("/{id}/triagem")
    @Operation(summary = "Envia ocorrência para triagem (exclusivo do Gestor de Segurança)")
    public OcorrenciaResponse enviarParaTriagem(@PathVariable Integer id) {
        return OcorrenciaResponse.fromOutputDTO(gerenciarUseCase.enviarParaTriagem(id));
    }

    @PatchMapping("/{id}/arquivar")
    @Operation(summary = "Arquiva uma ocorrência após análise/tratativa (exclusivo do Gestor de Segurança)")
    public OcorrenciaResponse arquivar(@PathVariable Integer id) {
        return OcorrenciaResponse.fromOutputDTO(gerenciarUseCase.arquivar(id));
    }

    @PatchMapping("/{id}/consolidar-cat")
    @Operation(summary = "Consolida número de protocolo CAT para um acidente (exclusivo do Gestor de Segurança)")
    public OcorrenciaResponse consolidarCAT(@PathVariable Integer id) {
        return OcorrenciaResponse.fromOutputDTO(gerenciarUseCase.consolidarCAT(id));
    }
}
