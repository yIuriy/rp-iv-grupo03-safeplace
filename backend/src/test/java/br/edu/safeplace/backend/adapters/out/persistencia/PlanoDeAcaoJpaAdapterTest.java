package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcao;
import br.edu.safeplace.backend.domain.ocorrencia.PlanoDeAcaoBuilder;
import br.edu.safeplace.backend.domain.ocorrencia.ProrrogacaoPrazo;
import br.edu.safeplace.backend.domain.ocorrencia.exception.PrazoAcaoInvalidoException;
import br.edu.safeplace.backend.domain.usuario.Colaborador;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - PlanoDeAcaoJpaAdapter")
class PlanoDeAcaoJpaAdapterTest {

    @Mock
    private PlanoDeAcaoJpaRepository planoRepository;

    @Mock
    private ProrrogacaoPrazoJpaRepository prorrogacaoRepository;

    private PlanoDeAcaoJpaAdapter adapter;
    private final LocalDate hoje = LocalDate.of(2026, 9, 20);

    @BeforeEach
    void setUp() {
        adapter = new PlanoDeAcaoJpaAdapter(planoRepository, prorrogacaoRepository);
    }

    private Colaborador colaborador(Integer id) {
        return new Colaborador(id, "87455877074", "Roberto", LocalDate.of(1992, 3, 10),
                "roberto@empresa.com", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Setor setor(Integer id) {
        return Setor.reconstituir(id, "Usinagem", "Setor de usinagem", "Bloco B", NivelPerigo.ALTO);
    }

    private Ocorrencia ocorrencia(Integer id) {
        return Incidente.builder()
                .idOcorrencia(id)
                .descricao("Ocorrência " + id)
                .dataOcorrencia(hoje.atStartOfDay())
                .build();
    }

    @Test
    @DisplayName("Deve salvar ação nova com colaborador e alerta delegando diretamente ao JpaRepository")
    void deveSalvarAcaoNovaComColaborador() {
        PlanoDeAcao plano = new PlanoDeAcaoBuilder()
                .ocorrencia(ocorrencia(10))
                .medidasCorretivas("Isolar prensa")
                .medidasPreventivas("Treinar equipe")
                .prazo(hoje.plusDays(5))
                .responsavelColaborador(colaborador(1))
                .alertaAtivo(true)
                .antecedenciaAlertaDias(3)
                .build();

        when(planoRepository.save(plano)).thenAnswer(inv -> {
            PlanoDeAcao p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 100);
            return p;
        });

        PlanoDeAcao resultado = adapter.salvar(plano);

        assertThat(resultado.getId()).isEqualTo(100);
        assertThat(resultado.getIdOcorrencia()).isEqualTo(10);
        assertThat(resultado.getMedidasCorretivas()).isEqualTo("Isolar prensa");
        assertThat(resultado.getMedidasPreventivas()).isEqualTo("Treinar equipe");
        assertThat(resultado.getStatus()).isEqualTo("ABERTA");
        assertThat(resultado.isAlertaAtivo()).isTrue();
        assertThat(resultado.getAntecedenciaAlertaDias()).isEqualTo(3);
        assertThat(resultado.getResponsavelColaborador()).isNotNull();
        assertThat(resultado.getResponsavelColaborador().getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve salvar ação vinculada a setor como responsável")
    void deveSalvarAcaoComSetor() {
        PlanoDeAcao plano = new PlanoDeAcaoBuilder()
                .ocorrencia(ocorrencia(10))
                .medidasCorretivas("Manutenção corretiva")
                .prazo(hoje.plusDays(7))
                .responsavelSetor(setor(5))
                .build();

        when(planoRepository.save(plano)).thenAnswer(inv -> {
            PlanoDeAcao p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 101);
            return p;
        });

        PlanoDeAcao resultado = adapter.salvar(plano);

        assertThat(resultado.getId()).isEqualTo(101);
        assertThat(resultado.getResponsavelSetor()).isNotNull();
        assertThat(resultado.getResponsavelSetor().getId()).isEqualTo(5);
        assertThat(resultado.getResponsavelColaborador()).isNull();
    }

    @Test
    @DisplayName("Deve rejeitar gravação quando prazo for nulo")
    void deveRejeitarPrazoNulo() {
        PlanoDeAcao plano = new PlanoDeAcaoBuilder()
                .ocorrencia(ocorrencia(10))
                .medidasCorretivas("Medida")
                .responsavelColaborador(colaborador(1))
                .build();

        assertThatThrownBy(() -> adapter.salvar(plano))
                .isInstanceOf(PrazoAcaoInvalidoException.class)
                .hasMessage("O prazo da ação é obrigatório.");
    }

    @Test
    @DisplayName("Deve salvar prorrogação persistindo diretamente no repositório de prorrogações")
    void deveSalvarProrrogacao() {
        PlanoDeAcao acao = new PlanoDeAcaoBuilder()
                .id(100)
                .ocorrencia(ocorrencia(10))
                .medidasCorretivas("Medida")
                .prazo(hoje.plusDays(2))
                .responsavelColaborador(colaborador(1))
                .build();
        ProrrogacaoPrazo prorrogacao = new ProrrogacaoPrazo(
                acao,
                hoje.plusDays(2),
                hoje.plusDays(10),
                "Atraso na entrega de peças"
        );
        when(prorrogacaoRepository.save(prorrogacao)).thenReturn(prorrogacao);

        ProrrogacaoPrazo resultado = adapter.salvar(prorrogacao);

        assertThat(resultado).isSameAs(prorrogacao);
        verify(prorrogacaoRepository).save(prorrogacao);
    }

    @Test
    @DisplayName("Deve listar histórico de prorrogações ordenado")
    void deveListarProrrogacoes() {
        PlanoDeAcao acao = new PlanoDeAcaoBuilder()
                .id(100)
                .ocorrencia(ocorrencia(10))
                .medidasCorretivas("Medida")
                .prazo(hoje.plusDays(10))
                .responsavelColaborador(colaborador(1))
                .build();
        ProrrogacaoPrazo p1 = new ProrrogacaoPrazo(acao, hoje.plusDays(2), hoje.plusDays(5), "Motivo 1");
        ProrrogacaoPrazo p2 = new ProrrogacaoPrazo(acao, hoje.plusDays(5), hoje.plusDays(10), "Motivo 2");

        when(prorrogacaoRepository.listarPorPlanoDeAcao(100))
                .thenReturn(List.of(p1, p2));

        List<ProrrogacaoPrazo> lista = adapter.listarProrrogacoes(100);

        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).getPrazoAnterior()).isEqualTo(hoje.plusDays(2));
        assertThat(lista.get(0).getNovoPrazo()).isEqualTo(hoje.plusDays(5));
        assertThat(lista.get(1).getNovoPrazo()).isEqualTo(hoje.plusDays(10));
    }
}
