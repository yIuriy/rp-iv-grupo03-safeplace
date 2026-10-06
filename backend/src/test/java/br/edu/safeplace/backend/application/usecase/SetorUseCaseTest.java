package br.edu.safeplace.backend.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.application.dto.input.CadastrarSetorInputDTO;
import br.edu.safeplace.backend.application.dto.output.SetorOutputDTO;
import br.edu.safeplace.backend.application.port.out.SetorRepositoryPort;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.suporte.TrilhaAuditoriaEmMemoria;

/** RNF05 (issue #193): o cadastro de setor passa a ser auditado e a porta de auditoria é obrigatória. */
class SetorUseCaseTest {

    private final TrilhaAuditoriaEmMemoria auditoria = new TrilhaAuditoriaEmMemoria();
    private final SetorRepositoryEmMemoria setores = new SetorRepositoryEmMemoria();

    @Test
    @DisplayName("Cadastro de setor grava LogAuditoria de CADASTRO com o operador")
    void cadastroDeSetorGeraAuditoria() {
        SetorUseCase useCase = new SetorUseCase(setores, auditoria);

        SetorOutputDTO salvo = useCase.cadastrarSetor(
                new CadastrarSetorInputDTO("Usinagem", "Tornos", "Bloco B", NivelPerigo.ALTO), "gestor@empresa.com");

        assertThat(auditoria.listarPorEntidade("Setor", salvo.id())).singleElement().satisfies(log -> {
            assertThat(log.getTipoOperacao()).isEqualTo("CADASTRO");
            assertThat(log.getResponsavel()).isEqualTo("gestor@empresa.com");
            assertThat(log.getJustificativa()).isEqualTo("Cadastro do setor Usinagem");
        });
    }

    @Test
    @DisplayName("Sem porta de auditoria o caso de uso não é criado")
    void portaDeAuditoriaObrigatoria() {
        assertThatThrownBy(() -> new SetorUseCase(setores, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("RNF05");
    }

    private static class SetorRepositoryEmMemoria implements SetorRepositoryPort {
        private final List<Setor> setores = new ArrayList<>();

        @Override
        public Setor salvar(Setor setor) {
            Setor salvo = new Setor(setores.size() + 1, setor.getNome(), setor.getDescricao(),
                    setor.getLocalizacao(), setor.getStatus());
            setores.add(salvo);
            return salvo;
        }

        @Override
        public Optional<Setor> buscarPorId(Integer id) {
            return setores.stream().filter(setor -> setor.getId().equals(id)).findFirst();
        }

        @Override
        public List<Setor> listarTodos() {
            return List.copyOf(setores);
        }

        @Override
        public Optional<Setor> buscarPorNome(String nome) {
            return setores.stream().filter(setor -> setor.getNome().equals(nome)).findFirst();
        }
    }
}
