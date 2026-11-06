/*
 * Testes da issue #236 (Adapter, parte 2): os casos de uso e o PlanoDeAcaoJpaAdapter leem o operador
 * pela fachada OperadorAtual, que se apoia na porta OperadorAutenticado. Aqui se verifica que a
 * dependência é obrigatória nos 8 clientes e que a camada de aplicação não conhece mais o framework de
 * segurança: o SecurityContextHolder só aparece em adapters/ e config/.
 */
package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.adapters.out.persistencia.PlanoDeAcaoJpaAdapter;
import br.edu.safeplace.backend.adapters.out.persistencia.PlanoDeAcaoJpaRepository;
import br.edu.safeplace.backend.adapters.out.persistencia.ProrrogacaoPrazoJpaRepository;
import br.edu.safeplace.backend.adapters.out.persistencia.SetorJpaRepository;
import br.edu.safeplace.backend.adapters.out.persistencia.UsuarioJpaRepository;
import br.edu.safeplace.backend.application.port.in.GerenciarOcorrenciaUseCase;
import br.edu.safeplace.backend.application.port.out.AreaRiscoRepository;
import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.application.port.out.LogAuditoriaRepository;
import br.edu.safeplace.backend.application.port.out.ManutencaoEpiRepository;
import br.edu.safeplace.backend.application.port.out.NotificadorAcao;
import br.edu.safeplace.backend.application.port.out.NotificadorOcorrencia;
import br.edu.safeplace.backend.application.port.out.OcorrenciaRepository;
import br.edu.safeplace.backend.application.port.out.PlanoDeAcaoRepository;
import br.edu.safeplace.backend.application.port.out.SetorRepository;
import br.edu.safeplace.backend.application.port.out.TarefaRepository;
import br.edu.safeplace.backend.application.port.out.UsuarioRepository;
import br.edu.safeplace.backend.application.port.out.capacitacao.CapacitacaoRepository;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

@DisplayName("Adapter (#236): clientes do OperadorAtual")
class OperadorAtualClientesTest {

    private static final String MENSAGEM = "Operador atual é obrigatório.";
    private static final Path CODIGO = Path.of("src", "main", "java", "br", "edu", "safeplace", "backend");

    private final LogAuditoriaRepository auditoria = new TrilhaAuditoriaEmMemoria();

    @Test
    @DisplayName("CadastrarCapacitacaoService recusa OperadorAtual null")
    void capacitacaoRecusaOperadorNulo() {
        assertThatThrownBy(() -> new CadastrarCapacitacaoService(mock(CapacitacaoRepository.class), auditoria, null))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("ControlarManutencaoEpiService recusa OperadorAtual null")
    void manutencaoRecusaOperadorNulo() {
        assertThatThrownBy(() -> new ControlarManutencaoEpiService(mock(EpiRepository.class),
                mock(ManutencaoEpiRepository.class), mock(UsuarioRepository.class), auditoria, null))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("EpiService recusa OperadorAtual null")
    void epiRecusaOperadorNulo() {
        assertThatThrownBy(() -> new EpiService(mock(EpiRepository.class), mock(UsuarioRepository.class), auditoria, null))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("InspecaoService recusa OperadorAtual null")
    void inspecaoRecusaOperadorNulo() {
        assertThatThrownBy(() -> new InspecaoService(mock(InspecaoRepository.class), mock(AreaRiscoRepository.class),
                null, auditoria))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("OcorrenciaService recusa OperadorAtual null")
    void ocorrenciaRecusaOperadorNulo() {
        assertThatThrownBy(() -> new OcorrenciaService(mock(OcorrenciaRepository.class), mock(AreaRiscoRepository.class),
                mock(UsuarioRepository.class), auditoria, mock(NotificadorOcorrencia.class), null))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("PlanoDeAcaoService recusa OperadorAtual null")
    void planoRecusaOperadorNulo() {
        assertThatThrownBy(() -> new PlanoDeAcaoService(mock(PlanoDeAcaoRepository.class),
                mock(OcorrenciaRepository.class), mock(UsuarioRepository.class), mock(SetorRepository.class),
                mock(NotificadorAcao.class), mock(GerenciarOcorrenciaUseCase.class), null))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("TarefaService recusa OperadorAtual null")
    void tarefaRecusaOperadorNulo() {
        assertThatThrownBy(() -> new TarefaService(mock(TarefaRepository.class), auditoria, null, null, null,
                mock(EpiRepository.class)))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("PlanoDeAcaoJpaAdapter recusa OperadorAtual null")
    void adaptadorDoPlanoRecusaOperadorNulo() {
        assertThatThrownBy(() -> new PlanoDeAcaoJpaAdapter(mock(PlanoDeAcaoJpaRepository.class),
                mock(ProrrogacaoPrazoJpaRepository.class), mock(UsuarioJpaRepository.class),
                mock(SetorJpaRepository.class), auditoria, null))
                .isInstanceOf(NullPointerException.class).hasMessage(MENSAGEM);
    }

    @Test
    @DisplayName("Nenhuma classe de application importa o Spring Security (application_importa_spring_security = 0)")
    void aplicacaoNaoImportaSpringSecurity() throws IOException {
        List<Path> violacoes = arquivosJava(CODIGO.resolve("application"))
                .filter(arquivo -> conteudo(arquivo).contains("import org.springframework.security"))
                .toList();

        assertThat(violacoes).isEmpty();
    }

    @Test
    @DisplayName("SecurityContextHolder só aparece em adapters/ e config/")
    void securityContextHolderSoEmAdaptadoresEConfiguracao() throws IOException {
        List<String> foraDoLugar = arquivosJava(CODIGO)
                .filter(arquivo -> conteudo(arquivo).contains("SecurityContextHolder"))
                .map(arquivo -> CODIGO.relativize(arquivo).toString().replace('\\', '/'))
                .filter(caminho -> !caminho.startsWith("adapters/") && !caminho.startsWith("config/"))
                .toList();

        assertThat(foraDoLugar).isEmpty();
    }

    private static Stream<Path> arquivosJava(Path raiz) throws IOException {
        assertThat(raiz).isDirectory();
        try (Stream<Path> arquivos = Files.walk(raiz)) {
            return arquivos.filter(arquivo -> arquivo.toString().endsWith(".java")).toList().stream();
        }
    }

    private static String conteudo(Path arquivo) {
        try {
            return Files.readString(arquivo, StandardCharsets.UTF_8);
        } catch (IOException erro) {
            throw new IllegalStateException("Não foi possível ler " + arquivo, erro);
        }
    }
}
