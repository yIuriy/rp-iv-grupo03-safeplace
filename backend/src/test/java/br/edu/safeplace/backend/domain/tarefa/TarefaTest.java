package br.edu.safeplace.backend.domain.tarefa;

import br.edu.safeplace.backend.suporte.EpiDeTeste;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TarefaTest {

    private static GestorDeSeguranca gestor(Integer id) {
        return new GestorDeSeguranca(id, "49216091040", "Mariana", LocalDate.of(1988, 7, 20),
                "mariana@empresa.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private static Epi epi(Integer id, String nome) {
        return EpiDeTeste.umEpi()
                .comId(id)
                .comNome(nome)
                .comCa("CA-" + id)
                .comQuantidade(10)
                .comEstoqueMinimo(2)
                .comStatus(StatusEpi.DISPONIVEL)
                .comValidadeCa(LocalDate.now().plusYears(1))
                .comVidaUtilDias(365)
                .build();
    }

    @Test
    @DisplayName("Deve cadastrar tarefa já classificada registrando data e responsável técnico")
    void deveCadastrarTarefaClassificada() {
        Tarefa tarefa = Tarefa.cadastrar("Solda em altura", NivelPerigo.ALTO, gestor(7));

        assertThat(tarefa.getId()).isNull();
        assertThat(tarefa.getDescricao()).isEqualTo("Solda em altura");
        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(tarefa.estaClassificada()).isTrue();
        assertThat(tarefa.getDataClassificacao()).isNotNull();
        assertThat(tarefa.getResponsavelClassificacao().getId()).isEqualTo(7);
    }

    @Test
    @DisplayName("UC11: deve permitir tarefa sem classificação prévia, sem responsável")
    void devePermitirTarefaSemClassificacao() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual");

        assertThat(tarefa.estaClassificada()).isFalse();
        assertThat(tarefa.getNivelPerigo()).isNull();
        assertThat(tarefa.getDataClassificacao()).isNull();
        assertThat(tarefa.getResponsavelClassificacao()).isNull();
        assertThat(Tarefa.cadastrar("Inspeção visual", null, null).estaClassificada()).isFalse();
    }

    @Test
    @DisplayName("UC11: deve classificar tarefa ainda sem grau de risco e devolver o registro da alteração")
    void deveClassificarTarefaSemGrauDeRisco() {
        Tarefa tarefa = new Tarefa(3, "Inspeção visual", null, null);

        RegistroClassificacaoTarefa registro = tarefa.classificar(NivelPerigo.MEDIO, gestor(7));

        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.MEDIO);
        assertThat(tarefa.estaClassificada()).isTrue();
        assertThat(tarefa.getDataClassificacao()).isNotNull();
        assertThat(registro.idTarefa()).isEqualTo(3);
        assertThat(registro.nivelAnterior()).isNull();
        assertThat(registro.nivelNovo()).isEqualTo(NivelPerigo.MEDIO);
        assertThat(registro.responsavel().getId()).isEqualTo(7);
        assertThat(registro.dataHora()).isEqualTo(tarefa.getDataClassificacao());
    }

    @Test
    @DisplayName("UC11 alternativo I: reavaliação atualiza grau, data e responsável, preservando o valor anterior")
    void reavaliacaoAtualizaGrauEData() {
        Tarefa tarefa = new Tarefa(1, "Solda em altura", NivelPerigo.CRITICO,
                LocalDateTime.of(2020, 1, 1, 8, 0), gestor(7));

        RegistroClassificacaoTarefa registro = tarefa.classificar(NivelPerigo.BAIXO, gestor(8));

        assertThat(tarefa.getNivelPerigo()).isEqualTo(NivelPerigo.BAIXO);
        assertThat(tarefa.getDataClassificacao()).isAfter(LocalDateTime.of(2020, 1, 1, 8, 0));
        assertThat(tarefa.getResponsavelClassificacao().getId()).isEqualTo(8);
        assertThat(registro.nivelAnterior()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(registro.nivelNovo()).isEqualTo(NivelPerigo.BAIXO);
    }

    @Test
    @DisplayName("Reavaliação para o mesmo nível é aceita e registrada")
    void reavaliacaoParaMesmoNivel() {
        Tarefa tarefa = new Tarefa(1, "Solda em altura", NivelPerigo.ALTO, LocalDateTime.of(2020, 1, 1, 8, 0));

        RegistroClassificacaoTarefa registro = tarefa.classificar(NivelPerigo.ALTO, gestor(7));

        assertThat(registro.nivelAnterior()).isEqualTo(NivelPerigo.ALTO);
        assertThat(registro.nivelNovo()).isEqualTo(NivelPerigo.ALTO);
        assertThat(tarefa.getResponsavelClassificacao().getId()).isEqualTo(7);
    }

    @Test
    @DisplayName("Deve rejeitar classificação com nível nulo ou sem responsável técnico, sem alterar a tarefa")
    void deveRejeitarClassificacaoIncompleta() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual");

        assertThatThrownBy(() -> tarefa.classificar(null, gestor(7)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nível de perigo da tarefa é obrigatório.");
        assertThatThrownBy(() -> tarefa.classificar(NivelPerigo.ALTO, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Responsável técnico pela classificação é obrigatório.");
        assertThatThrownBy(() -> Tarefa.cadastrar("Solda", NivelPerigo.ALTO, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(tarefa.estaClassificada()).isFalse();
    }

    @Test
    @DisplayName("Classificação legada sem autoria é reconstituída sem responsável inventado")
    void classificacaoLegadaSemAutoria() {
        Tarefa legada = new Tarefa(4, "Pintura", NivelPerigo.BAIXO, LocalDateTime.of(2026, 9, 1, 9, 0));

        assertThat(legada.estaClassificada()).isTrue();
        assertThat(legada.getResponsavelClassificacao()).isNull();
    }

    @Test
    @DisplayName("RF12 / UC08: deve associar e verificar EPIs obrigatórios da tarefa")
    void deveAssociarEVerificarEpisObrigatorios() {
        Tarefa tarefa = Tarefa.cadastrar("Trabalho em tubulação");
        Epi capacete = epi(1, "Capacete de Segurança");
        Epi luva = epi(2, "Luva de Vaqueta");

        boolean resultado = tarefa.associarEpis(List.of(capacete, luva));

        assertThat(resultado).isTrue();
        assertThat(tarefa.verificarEpisObrigatoriosTarefa()).containsExactly(capacete, luva);
        assertThat(tarefa.getEpisObrigatorios()).containsExactly(capacete, luva);
    }

    @Test
    @DisplayName("RF12 / UC08: deve permitir tarefa sem EPI associado com lista vazia ou nula")
    void devePermitirTarefaSemEpisAssociados() {
        Tarefa tarefa = Tarefa.cadastrar("Inspeção visual");
        assertThat(tarefa.getEpisObrigatorios()).isEmpty();

        tarefa.associarEpis(null);
        assertThat(tarefa.getEpisObrigatorios()).isEmpty();

        tarefa.associarEpis(List.of());
        assertThat(tarefa.getEpisObrigatorios()).isEmpty();
    }

    @Test
    @DisplayName("RF12 / UC08: deve verificar se determinado EPI é exigido pela tarefa")
    void deveVerificarSeExigeEpi() {
        Tarefa tarefa = Tarefa.cadastrar("Manutenção elétrica");
        Epi bota = epi(10, "Bota Isolante");
        tarefa.associarEpis(List.of(bota));

        assertThat(tarefa.exigeEpi(10)).isTrue();
        assertThat(tarefa.exigeEpi(99)).isFalse();
        assertThat(tarefa.exigeEpi(null)).isFalse();
    }

    @Test
    @DisplayName("RF12 / UC08: lista de EPIs obrigatórios deve ser imutável externamente")
    void listaDeEpisDeveSerImutavelExternamente() {
        Tarefa tarefa = Tarefa.cadastrar("Pintura");
        Epi mascara = epi(5, "Máscara PFF2");
        tarefa.associarEpis(List.of(mascara));

        List<Epi> epis = tarefa.verificarEpisObrigatoriosTarefa();
        assertThatThrownBy(() -> epis.add(epi(6, "Óculos")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
