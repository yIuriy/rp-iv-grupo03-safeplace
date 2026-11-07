package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import br.edu.safeplace.backend.domain.ocorrencia.Acidente;
import br.edu.safeplace.backend.domain.ocorrencia.CausaRaizAcidente;
import br.edu.safeplace.backend.domain.ocorrencia.Incidente;
import br.edu.safeplace.backend.domain.ocorrencia.NumeroProtocoloCAT;
import br.edu.safeplace.backend.domain.ocorrencia.Ocorrencia;
import br.edu.safeplace.backend.domain.ocorrencia.StatusOcorrencia;
import br.edu.safeplace.backend.domain.usuario.Colaborador;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;
import br.edu.safeplace.backend.domain.usuario.Supervisor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade do OcorrenciaJpaAdapter (#261)")
class OcorrenciaJpaAdapterTest {

    private static final Setor SETOR_10 = new Setor(10, "Estamparia", null, "Galpão 2", NivelPerigo.ALTO);

    @Mock
    private OcorrenciaJpaRepository ocorrenciaRepository;

    @Mock
    private AreaRiscoJpaRepository areaRiscoRepository;

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @Mock
    private SequenciaProtocoloCatJpaRepository sequenciaProtocoloCatRepository;

    private OcorrenciaJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OcorrenciaJpaAdapter(
                ocorrenciaRepository, areaRiscoRepository, usuarioRepository, sequenciaProtocoloCatRepository);
    }

    @Test
    @DisplayName("Deve salvar acidente delegando diretamente ao repositório JPA")
    void deveSalvarAcidenteComRelacionamentos() {
        AreaRisco area = new AreaRisco(1, "AR-01", "Prensa", "Setor Prensa", NivelPerigo.ALTO, SETOR_10, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());
        GestorDeSeguranca gestor = new GestorDeSeguranca(3, "98765432100", "Maria Gestora", LocalDate.of(1985, 5, 5), "maria@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        Acidente acidente = Acidente.builder()
                .descricao("Acidente com prensa")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 10, 14, 0))
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .relator(gestor)
                .causaRaiz(CausaRaizAcidente.FALHA_EPI)
                .tipo("Esmagamento")
                .dano("Lesão na mão")
                .destino("Hospital")
                .gravidade(NivelPerigo.CRITICO)
                .build();

        when(ocorrenciaRepository.save(any(Acidente.class))).thenAnswer(invocation -> {
            Acidente a = invocation.getArgument(0);
            return Acidente.builder()
                    .idOcorrencia(100)
                    .descricao(a.getDescricao())
                    .dataOcorrencia(a.getDataOcorrencia())
                    .dataRegistro(a.getDataRegistro())
                    .statusOcorrencia(a.getStatusOcorrencia())
                    .colaborador(a.getColaborador())
                    .area(a.getArea())
                    .relator(a.getRelator())
                    .localInformado(a.getLocalInformado())
                    .planoDeAcao(a.getPlanoDeAcao())
                    .causaRaiz(a.getCausaRaiz())
                    .tipo(a.getTipo())
                    .dano(a.getDano())
                    .numeroProtocoloCAT(a.getProtocoloCAT())
                    .destino(a.getDestino())
                    .gravidade(a.getGravidade())
                    .build();
        });

        Acidente salvo = adapter.salvarAcidente(acidente);

        assertThat(salvo).isNotNull();
        assertThat(salvo.getIdOcorrencia()).isEqualTo(100);
        assertThat(salvo.getGravidade()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(salvo.getArea()).isNotNull();
        assertThat(salvo.getArea().getNome()).isEqualTo("Prensa");
        assertThat(salvo.getColaborador()).isNotNull();
        assertThat(salvo.getColaborador().getNome()).isEqualTo("João Silva");
        assertThat(salvo.getRelator()).isInstanceOf(GestorDeSeguranca.class);
        assertThat(salvo.getRelator().getNome()).isEqualTo("Maria Gestora");

        ArgumentCaptor<Acidente> captor = ArgumentCaptor.forClass(Acidente.class);
        verify(ocorrenciaRepository).save(captor.capture());
        Acidente entidadeSalva = captor.getValue();
        assertThat(entidadeSalva.getGravidade()).isEqualTo(NivelPerigo.CRITICO);
        assertThat(entidadeSalva.getArea().getId()).isEqualTo(1);
        assertThat(entidadeSalva.getColaborador().getId()).isEqualTo(2);
        assertThat(entidadeSalva.getRelator().getId()).isEqualTo(3);
    }

    @Test
    @DisplayName("Supervisor que registrou o relato é gravado como relator (issue #195)")
    void deveSalvarSupervisorComoRelator() {
        AreaRisco area = new AreaRisco(1, "AR-01", "Prensa", "Setor Prensa", NivelPerigo.MEDIO, SETOR_10, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());
        Supervisor supervisor = new Supervisor(4, "11144477735", "Sérgio Supervisor", LocalDate.of(1987, 6, 6), "sergio@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        when(ocorrenciaRepository.save(any(Incidente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidente salvo = adapter.salvarIncidente(Incidente.builder()
                .descricao("Quase queda")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 10, 10, 0))
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .relator(supervisor)
                .situacaoRisco("Piso oleoso")
                .potencialDano("Queda de mesmo nível")
                .build());

        assertThat(salvo.getRelator()).isInstanceOf(Supervisor.class);
        assertThat(salvo.getRelator().getId()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deve salvar incidente delegando diretamente ao repositório JPA")
    void deveSalvarIncidente() {
        AreaRisco area = new AreaRisco(1, "AR-01", "Prensa", "Setor Prensa", NivelPerigo.MEDIO, SETOR_10, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());

        Incidente incidente = Incidente.builder()
                .descricao("Quase queda")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 10, 10, 0))
                .dataRegistro(LocalDateTime.now())
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .colaborador(colaborador)
                .area(area)
                .situacaoRisco("Piso oleoso")
                .potencialDano("Queda de mesmo nível")
                .build();

        when(ocorrenciaRepository.save(any(Incidente.class))).thenAnswer(invocation -> {
            Incidente inc = invocation.getArgument(0);
            return Incidente.builder()
                    .idOcorrencia(200)
                    .descricao(inc.getDescricao())
                    .dataOcorrencia(inc.getDataOcorrencia())
                    .dataRegistro(inc.getDataRegistro())
                    .statusOcorrencia(inc.getStatusOcorrencia())
                    .colaborador(inc.getColaborador())
                    .area(inc.getArea())
                    .relator(inc.getRelator())
                    .localInformado(inc.getLocalInformado())
                    .planoDeAcao(inc.getPlanoDeAcao())
                    .situacaoRisco(inc.getSituacaoRisco())
                    .potencialDano(inc.getPotencialDano())
                    .build();
        });

        Incidente salvo = adapter.salvarIncidente(incidente);

        assertThat(salvo).isNotNull();
        assertThat(salvo.getIdOcorrencia()).isEqualTo(200);
        assertThat(salvo.getSituacaoRisco()).isEqualTo("Piso oleoso");
        assertThat(salvo.getPotencialDano()).isEqualTo("Queda de mesmo nível");
        assertThat(salvo.getArea().getNome()).isEqualTo("Prensa");
        assertThat(salvo.getColaborador().getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve buscar ocorrência por ID diretamente do repositório JPA")
    void deveBuscarPorId() {
        Acidente acidenteGravado = Acidente.builder()
                .idOcorrencia(50)
                .descricao("Corte com ferramenta")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 11, 8, 0))
                .dataRegistro(LocalDateTime.of(2026, 9, 11, 8, 30))
                .statusOcorrencia(StatusOcorrencia.EM_TRIAGEM)
                .localInformado("Oficina")
                .causaRaiz(CausaRaizAcidente.FALHA_EPI)
                .tipo("Corte")
                .dano("Dedo")
                .numeroProtocoloCAT(NumeroProtocoloCAT.de("CAT-2026-09-0050"))
                .destino("Ambulatório")
                .gravidade(NivelPerigo.ALTO)
                .build();

        when(ocorrenciaRepository.findById(50)).thenReturn(Optional.of(acidenteGravado));

        Optional<Ocorrencia> resultado = adapter.buscarPorId(50);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isInstanceOf(Acidente.class);
        Acidente acidente = (Acidente) resultado.get();
        assertThat(acidente.getIdOcorrencia()).isEqualTo(50);
        assertThat(acidente.getStatusOcorrencia()).isEqualTo(StatusOcorrencia.EM_TRIAGEM);
        assertThat(acidente.getGravidade()).isEqualTo(NivelPerigo.ALTO);
        assertThat(acidente.getNumeroProtocoloCAT()).isEqualTo("CAT-2026-09-0050");
    }

    @Test
    @DisplayName("Deve reservar o sequencial CAT no banco, e não a partir de uma contagem de ocorrências")
    void deveReservarSequencialCATNoBanco() {
        when(sequenciaProtocoloCatRepository.reservarProximoSequencial(2026, 9)).thenReturn(8L);

        long proximo = adapter.proximoSequencialCAT(2026, 9);

        assertThat(proximo).isEqualTo(8L);
        verify(sequenciaProtocoloCatRepository).reservarProximoSequencial(2026, 9);
        verify(ocorrenciaRepository, never()).count();
    }

    @Test
    @DisplayName("Deve reconstruir o local legado sem transformá-lo em área de risco")
    void naoDeveDerivarAreaFicticiaDoTextoLocal() {
        Acidente acidenteGravado = Acidente.builder()
                .idOcorrencia(70)
                .descricao("Corte com ferramenta")
                .dataOcorrencia(LocalDateTime.of(2026, 9, 11, 8, 0))
                .dataRegistro(LocalDateTime.of(2026, 9, 11, 8, 30))
                .statusOcorrencia(StatusOcorrencia.ABERTA)
                .localInformado("Oficina")
                .causaRaiz(CausaRaizAcidente.FALHA_EPI)
                .tipo("Corte")
                .dano("Dedo")
                .destino("Ambulatório")
                .gravidade(NivelPerigo.MEDIO)
                .build();

        when(ocorrenciaRepository.findById(70)).thenReturn(Optional.of(acidenteGravado));

        Acidente acidente = (Acidente) adapter.buscarPorId(70).orElseThrow();

        assertThat(acidente.getArea()).isNull();
        assertThat(acidente.getLocal()).isEqualTo("Oficina");
        assertThat(acidente.getLocalInformado()).isEqualTo("Oficina");
    }
}
