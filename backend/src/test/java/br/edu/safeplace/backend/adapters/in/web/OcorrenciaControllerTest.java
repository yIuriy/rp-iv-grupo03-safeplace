/*
 * Testes do OcorrenciaController com MockMvc: relato, consulta por critérios, detalhe, protocolo,
 * triagem e erros, com os casos de uso simulados. Inclui as regras do relato da issue #195: área e
 * colaborador obrigatórios, protocolo, relator e plano fora do request, filtros de tipo e gravidade.
 */
package br.edu.safeplace.backend.adapters.in.web;

import br.edu.safeplace.backend.application.dto.input.AtualizarOcorrenciaInputDTO;
import br.edu.safeplace.backend.application.dto.input.FiltroOcorrenciasDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarAcidenteInputDTO;
import br.edu.safeplace.backend.application.dto.input.RegistrarIncidenteInputDTO;
import br.edu.safeplace.backend.application.dto.output.OcorrenciaOutputDTO;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.in.RegistrarOcorrenciaUseCase;
import br.edu.safeplace.backend.config.SecurityConfig;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.TipoOcorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.exception.DadosOcorrenciaInvalidosException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.OcorrenciaNaoEncontradaException;
import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({OcorrenciaController.class, ApiExceptionHandler.class})
@Import(SecurityConfig.class)
class OcorrenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarOcorrenciaUseCase registrarUseCase;

    @MockitoBean
    private GerenciarOcorrenciaUseCase gerenciarUseCase;

    private static OcorrenciaOutputDTO acidenteDTO(Integer id, String status, String protocolo) {
        return new OcorrenciaOutputDTO(
                id,
                "ACIDENTE",
                LocalDateTime.of(2026, 9, 10, 10, 0),
                LocalDateTime.now(),
                status,
                "Setor Solda",
                "Queimadura por fagulha",
                null,
                "FALHA_EPI",
                "Queimadura",
                "Leve",
                protocolo,
                protocolo,
                "Ambulatorio",
                null,
                null,
                "MEDIO",
                1,
                "Solda",
                2,
                "Joao",
                3,
                "Gestor Seguranca",
                "GESTOR_SEGURANCA"
        );
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve registrar acidente via POST /api/ocorrencias")
    void deveRegistrarAcidente() throws Exception {
        when(registrarUseCase.registrar(any(RegistrarAcidenteInputDTO.class)))
                .thenReturn(acidenteDTO(1, "ABERTA", "CAT-2026-09-0001"));

        String payload = """
                {
                    "tipoOcorrencia": "ACIDENTE",
                    "dataOcorrencia": "2026-09-10T10:00:00",
                    "descricao": "Queimadura por fagulha",
                    "areaId": 1,
                    "colaboradorId": 2,
                    "tipo": "Queimadura",
                    "dano": "Leve",
                    "causaRaiz": "FALHA_EPI",
                    "gravidade": "MEDIO",
                    "destino": "Ambulatorio"
                }
                """;

        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOcorrencia").value(1))
                .andExpect(jsonPath("$.tipoOcorrencia").value("ACIDENTE"))
                .andExpect(jsonPath("$.statusOcorrencia").value("ABERTA"))
                .andExpect(jsonPath("$.numeroProtocoloCAT").value("CAT-2026-09-0001"))
                .andExpect(jsonPath("$.relatorId").value(3))
                .andExpect(jsonPath("$.relatorNome").value("Gestor Seguranca"))
                .andExpect(jsonPath("$.relatorPerfil").value("GESTOR_SEGURANCA"));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Protocolo, gestor, plano e local enviados pelo cliente não chegam ao caso de uso (issue #195)")
    void camposDefinidosPeloServidorNaoChegamAoCasoDeUso() throws Exception {
        when(registrarUseCase.registrar(any(RegistrarAcidenteInputDTO.class)))
                .thenReturn(acidenteDTO(1, "ABERTA", "CAT-2026-09-0001"));

        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoOcorrencia":"ACIDENTE","dataOcorrencia":"2026-09-10T10:00:00",
                                 "descricao":"Queimadura por fagulha","areaId":1,"colaboradorId":2,
                                 "causaRaiz":"FALHA_EPI","gravidade":"MEDIO",
                                 "numeroProtocolo":"CAT-2026-09-9999","gestorId":99,"local":"Texto livre",
                                 "planoDeAcao":{"medidasCorretivas":"Isolar","prazo":"2026-10-01"}}
                                """))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<RegistrarAcidenteInputDTO> captor =
                org.mockito.ArgumentCaptor.forClass(RegistrarAcidenteInputDTO.class);
        verify(registrarUseCase).registrar(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new RegistrarAcidenteInputDTO(
                LocalDateTime.of(2026, 9, 10, 10, 0), "Queimadura por fagulha", "FALHA_EPI", "MEDIO",
                null, null, null, 1, 2));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Relato sem área de risco responde 400 sem chamar o caso de uso (issue #195)")
    void relatoSemAreaDeveResponder400() throws Exception {
        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoOcorrencia":"INCIDENTE","dataOcorrencia":"2026-09-10T10:00:00",
                                 "descricao":"Oleo no piso","colaboradorId":2,
                                 "situacaoRisco":"Piso escorregadio","potencialDano":"Queda"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrarUseCase);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Relato sem colaborador envolvido responde 400 sem chamar o caso de uso (issue #195)")
    void relatoSemColaboradorDeveResponder400() throws Exception {
        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoOcorrencia":"INCIDENTE","dataOcorrencia":"2026-09-10T10:00:00",
                                 "descricao":"Oleo no piso","areaId":1,
                                 "situacaoRisco":"Piso escorregadio","potencialDano":"Queda"}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrarUseCase);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Tipo de ocorrência desconhecido responde 400")
    void tipoDesconhecidoDeveResponder400() throws Exception {
        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoOcorrencia":"QUASE_ACIDENTE","dataOcorrencia":"2026-09-10T10:00:00",
                                 "descricao":"Oleo no piso","areaId":1,"colaboradorId":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tipo de ocorrência inválido: QUASE_ACIDENTE. Use ACIDENTE ou INCIDENTE."));

        verifyNoInteractions(registrarUseCase);
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Gravidade ou causa raiz inválidas recusadas no caso de uso respondem 400 (issue #195)")
    void gravidadeInvalidaDeveResponder400() throws Exception {
        when(registrarUseCase.registrar(any(RegistrarAcidenteInputDTO.class)))
                .thenThrow(new DadosOcorrenciaInvalidosException(
                        "Gravidade inválida: GRAVE. Use BAIXO, MEDIO, ALTO ou CRITICO."));

        mockMvc.perform(post("/api/ocorrencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoOcorrencia":"ACIDENTE","dataOcorrencia":"2026-09-10T10:00:00",
                                 "descricao":"Corte","areaId":1,"colaboradorId":2,
                                 "causaRaiz":"FALHA_EPI","gravidade":"GRAVE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Gravidade inválida: GRAVE. Use BAIXO, MEDIO, ALTO ou CRITICO."));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor deve listar ocorrencias via GET /api/ocorrencias")
    void supervisorDeveListarOcorrencias() throws Exception {
        when(registrarUseCase.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of(acidenteDTO(1, "ABERTA", "CAT-2026-09-0001")));

        mockMvc.perform(get("/api/ocorrencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idOcorrencia").value(1))
                .andExpect(jsonPath("$[0].tipoOcorrencia").value("ACIDENTE"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Filtros de tipo e gravidade chegam ao caso de uso (issue #195)")
    void deveRepassarFiltrosDeTipoEGravidade() throws Exception {
        when(registrarUseCase.listar(any(FiltroOcorrenciasDTO.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/ocorrencias")
                        .param("tipoOcorrencia", "acidente")
                        .param("gravidade", "ALTO")
                        .param("setorId", "3"))
                .andExpect(status().isOk());

        verify(registrarUseCase).listar(new FiltroOcorrenciasDTO(null, null, 3, null, TipoOcorrencia.ACIDENTE, NivelPerigo.ALTO));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Filtro de gravidade ou tipo inválido responde 400")
    void filtroInvalidoDeveResponder400() throws Exception {
        mockMvc.perform(get("/api/ocorrencias").param("gravidade", "GRAVE"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/ocorrencias").param("tipoOcorrencia", "OUTRO"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(registrarUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve buscar ocorrencia por ID via GET /api/ocorrencias/{id}")
    void deveBuscarPorId() throws Exception {
        when(gerenciarUseCase.buscarPorId(1)).thenReturn(acidenteDTO(1, "ABERTA", "CAT-2026-09-0001"));

        mockMvc.perform(get("/api/ocorrencias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOcorrencia").value(1))
                .andExpect(jsonPath("$.numeroProtocoloCAT").value("CAT-2026-09-0001"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve retornar 404 ao buscar por ID inexistente")
    void deveRetornar404AoBuscarPorIdInexistente() throws Exception {
        when(gerenciarUseCase.buscarPorId(999))
                .thenThrow(new OcorrenciaNaoEncontradaException(999));

        mockMvc.perform(get("/api/ocorrencias/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Não Encontrado"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve buscar ocorrencia por protocolo CAT via GET /api/ocorrencias/protocolo/{protocolo}")
    void deveBuscarPorProtocolo() throws Exception {
        when(gerenciarUseCase.buscarPorProtocolo("CAT-2026-09-0001"))
                .thenReturn(acidenteDTO(1, "ABERTA", "CAT-2026-09-0001"));

        mockMvc.perform(get("/api/ocorrencias/protocolo/CAT-2026-09-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOcorrencia").value(1))
                .andExpect(jsonPath("$.numeroProtocoloCAT").value("CAT-2026-09-0001"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve enviar ocorrencia para triagem via PATCH /api/ocorrencias/{id}/triagem")
    void deveEnviarParaTriagem() throws Exception {
        when(gerenciarUseCase.enviarParaTriagem(1))
                .thenReturn(acidenteDTO(1, "EM_TRIAGEM", "CAT-2026-09-0001"));

        mockMvc.perform(patch("/api/ocorrencias/1/triagem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusOcorrencia").value("EM_TRIAGEM"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve retornar 400 em transicao de status invalida ao enviar para triagem")
    void deveRetornar400EmTransicaoInvalida() throws Exception {
        when(gerenciarUseCase.enviarParaTriagem(1))
                .thenThrow(new TransicaoStatusInvalidaException(StatusOcorrencia.ARQUIVADA, StatusOcorrencia.EM_TRIAGEM));

        mockMvc.perform(patch("/api/ocorrencias/1/triagem"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Transição de Status Inválida"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve arquivar ocorrencia via PATCH /api/ocorrencias/{id}/arquivar")
    void deveArquivarOcorrencia() throws Exception {
        when(gerenciarUseCase.arquivar(1))
                .thenReturn(acidenteDTO(1, "ARQUIVADA", "CAT-2026-09-0001"));

        mockMvc.perform(patch("/api/ocorrencias/1/arquivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusOcorrencia").value("ARQUIVADA"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve consolidar CAT via PATCH /api/ocorrencias/{id}/consolidar-cat")
    void deveConsolidarCAT() throws Exception {
        when(gerenciarUseCase.consolidarCAT(1))
                .thenReturn(acidenteDTO(1, "ABERTA", "CAT-2026-09-0001"));

        mockMvc.perform(patch("/api/ocorrencias/1/consolidar-cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroProtocoloCAT").value("CAT-2026-09-0001"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Gestor edita ocorrência pelo PUT e recebe os dados atualizados")
    void gestorDeveEditarOcorrencia() throws Exception {
        when(gerenciarUseCase.atualizar(org.mockito.ArgumentMatchers.eq(1), any(AtualizarOcorrenciaInputDTO.class)))
                .thenReturn(acidenteDTO(1, StatusOcorrencia.EM_TRIAGEM.name(), "CAT-2026-09-0001"));

        mockMvc.perform(put("/api/ocorrencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dataOcorrencia":"2026-09-10T10:00:00","descricao":"Queimadura por fagulha",
                                 "areaId":1,"colaboradorId":2,"causaRaiz":"FALHA_EPI","gravidade":"MEDIO"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOcorrencia").value(1))
                .andExpect(jsonPath("$.statusOcorrencia").value("EM_TRIAGEM"))
                .andExpect(jsonPath("$.numeroProtocoloCAT").value("CAT-2026-09-0001"));
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Edição sem descrição ou data do fato responde 400")
    void edicaoSemCamposObrigatoriosDeveResponder400() throws Exception {
        mockMvc.perform(put("/api/ocorrencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Edição sem área ou colaborador responde 400 (issue #195)")
    void edicaoSemAreaOuColaboradorDeveResponder400() throws Exception {
        mockMvc.perform(put("/api/ocorrencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataOcorrencia\":\"2026-09-10T10:00:00\",\"descricao\":\"Texto\",\"colaboradorId\":2}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/ocorrencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataOcorrencia\":\"2026-09-10T10:00:00\",\"descricao\":\"Texto\",\"areaId\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gerenciarUseCase);
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Edição de ocorrência arquivada responde 400 com a mensagem do domínio")
    void edicaoDeArquivadaDeveResponder400() throws Exception {
        when(gerenciarUseCase.atualizar(org.mockito.ArgumentMatchers.eq(1), any(AtualizarOcorrenciaInputDTO.class)))
                .thenThrow(new IllegalStateException("Ocorrência arquivada não pode ser editada."));

        mockMvc.perform(put("/api/ocorrencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataOcorrencia\":\"2026-09-10T10:00:00\",\"descricao\":\"Texto\",\"areaId\":1,\"colaboradorId\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ocorrência arquivada não pode ser editada."));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Supervisor não edita ocorrência (403)")
    void supervisorNaoDeveEditarOcorrencia() throws Exception {
        mockMvc.perform(put("/api/ocorrencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataOcorrencia\":\"2026-09-10T10:00:00\",\"descricao\":\"Texto\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "GESTOR_SEGURANCA")
    @DisplayName("Deve impedir exclusao permanente (DELETE não permitido)")
    void deveImpedirExclusaoPermanente() throws Exception {
        mockMvc.perform(delete("/api/ocorrencias/1"))
                .andExpect(status().isMethodNotAllowed());
    }
}
