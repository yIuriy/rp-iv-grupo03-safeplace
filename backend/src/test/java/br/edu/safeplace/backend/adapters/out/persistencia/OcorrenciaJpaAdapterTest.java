package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.domain.area_risco.AreaRisco;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade do OcorrenciaJpaAdapter")
class OcorrenciaJpaAdapterTest {

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
    @DisplayName("Deve salvar acidente mapeando gravidade, area, colaborador e relator")
    void deveSalvarAcidenteComRelacionamentos() {
        AreaRisco area = new AreaRisco(1, "AR-01", "Prensa", "Setor Prensa", NivelPerigo.ALTO, 10, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());
        GestorDeSeguranca gestor = new GestorDeSeguranca(3, "98765432100", "Maria Gestora", LocalDate.of(1985, 5, 5), "maria@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        AreaRiscoEntity areaEntity = new AreaRiscoEntity(1, "AR-01", "Prensa", "Setor Prensa", "ALTO", 10, null, "Galpão", List.of());
        ColaboradorEntidade colaboradorEntity = new ColaboradorEntidade(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());
        GestorDeSegurancaEntidade gestorEntity = new GestorDeSegurancaEntidade(3, "98765432100", "Maria Gestora", LocalDate.of(1985, 5, 5), "maria@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        when(areaRiscoRepository.findById(1)).thenReturn(Optional.of(areaEntity));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(colaboradorEntity));
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(gestorEntity));

        Acidente acidente = Acidente.novo(
                "Acidente com prensa",
                LocalDateTime.of(2026, 9, 10, 14, 0),
                area,
                colaborador,
                gestor,
                CausaRaizAcidente.FALHA_EPI,
                "Esmagamento",
                "Lesão na mão",
                "Hospital",
                NivelPerigo.CRITICO
        );

        when(ocorrenciaRepository.save(any(AcidenteEntity.class))).thenAnswer(invocation -> {
            AcidenteEntity entity = invocation.getArgument(0);
            return new AcidenteEntity(
                    100,
                    entity.getDataOcorrencia(),
                    entity.getDataRegistro(),
                    entity.getStatusOcorrencia(),
                    entity.getArea(),
                    entity.getColaborador(),
                    entity.getRelator(),
                    entity.getLocal(),
                    entity.getDescricao(),
                    entity.getPlanoDeAcao(),
                    entity.getCausaRaiz(),
                    entity.getTipo(),
                    entity.getDano(),
                    entity.getNumeroProtocolo(),
                    entity.getDestino(),
                    entity.getGravidade()
            );
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

        ArgumentCaptor<AcidenteEntity> captor = ArgumentCaptor.forClass(AcidenteEntity.class);
        verify(ocorrenciaRepository).save(captor.capture());
        AcidenteEntity entitySalva = captor.getValue();
        assertThat(entitySalva.getGravidade()).isEqualTo("CRITICO");
        assertThat(entitySalva.getArea().getId()).isEqualTo(1);
        assertThat(entitySalva.getColaborador().getId()).isEqualTo(2);
        assertThat(entitySalva.getRelator().getId()).isEqualTo(3);
    }

    @Test
    @DisplayName("Supervisor que registrou o relato é gravado como relator (issue #195)")
    void deveSalvarSupervisorComoRelator() {
        AreaRisco area = new AreaRisco(1, "AR-01", "Prensa", "Setor Prensa", NivelPerigo.MEDIO, 10, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());
        Supervisor supervisor = new Supervisor(4, "11144477735", "Sérgio Supervisor", LocalDate.of(1987, 6, 6), "sergio@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now());

        when(areaRiscoRepository.findById(1)).thenReturn(Optional.of(
                new AreaRiscoEntity(1, "AR-01", "Prensa", "Setor Prensa", "MEDIO", 10, null, "Galpão", List.of())));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(
                new ColaboradorEntidade(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now())));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(
                new SupervisorEntidade(4, "11144477735", "Sérgio Supervisor", LocalDate.of(1987, 6, 6), "sergio@email.com", "hash", true, LocalDateTime.now(), LocalDateTime.now())));
        when(ocorrenciaRepository.save(any(IncidenteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidente salvo = adapter.salvarIncidente(Incidente.novo("Quase queda", LocalDateTime.of(2026, 9, 10, 10, 0),
                area, colaborador, supervisor, "Piso oleoso", "Queda de mesmo nível"));

        assertThat(salvo.getRelator()).isInstanceOf(Supervisor.class);
        assertThat(salvo.getRelator().getId()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deve salvar incidente mapeando area e colaborador")
    void deveSalvarIncidente() {
        AreaRisco area = new AreaRisco(1, "AR-01", "Prensa", "Setor Prensa", NivelPerigo.MEDIO, 10, List.of(), "Galpão", List.of());
        Colaborador colaborador = new Colaborador(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());

        AreaRiscoEntity areaEntity = new AreaRiscoEntity(1, "AR-01", "Prensa", "Setor Prensa", "MEDIO", 10, null, "Galpão", List.of());
        ColaboradorEntidade colaboradorEntity = new ColaboradorEntidade(2, "12345678901", "João Silva", LocalDate.of(1990, 1, 1), "joao@email.com", true, LocalDateTime.now(), LocalDateTime.now());

        when(areaRiscoRepository.findById(1)).thenReturn(Optional.of(areaEntity));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(colaboradorEntity));

        Incidente incidente = Incidente.novo(
                "Quase queda",
                LocalDateTime.of(2026, 9, 10, 10, 0),
                area,
                colaborador,
                null,
                "Piso oleoso",
                "Queda de mesmo nível"
        );

        when(ocorrenciaRepository.save(any(IncidenteEntity.class))).thenAnswer(invocation -> {
            IncidenteEntity entity = invocation.getArgument(0);
            return new IncidenteEntity(
                    200,
                    entity.getDataOcorrencia(),
                    entity.getDataRegistro(),
                    entity.getStatusOcorrencia(),
                    entity.getArea(),
                    entity.getColaborador(),
                    entity.getRelator(),
                    entity.getLocal(),
                    entity.getDescricao(),
                    entity.getPlanoDeAcao(),
                    entity.getSituacaoRisco(),
                    entity.getPotencialDano()
            );
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
    @DisplayName("Deve buscar ocorrência por ID e mapear corretamente")
    void deveBuscarPorId() {
        AcidenteEntity entity = new AcidenteEntity(
                50,
                LocalDateTime.of(2026, 9, 11, 8, 0),
                LocalDateTime.of(2026, 9, 11, 8, 30),
                "EM_TRIAGEM",
                null,
                null,
                null,
                "Oficina",
                "Corte com ferramenta",
                null,
                "FALHA_EPI",
                "Corte",
                "Dedo",
                NumeroProtocoloCAT.de("CAT-2026-09-0050"),
                "Ambulatório",
                "ALTO"
        );

        when(ocorrenciaRepository.findById(50)).thenReturn(Optional.of(entity));

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
        AcidenteEntity entity = new AcidenteEntity(
                70,
                LocalDateTime.of(2026, 9, 11, 8, 0),
                LocalDateTime.of(2026, 9, 11, 8, 30),
                "ABERTA",
                null,
                null,
                null,
                "Oficina",
                "Corte com ferramenta",
                null,
                "FALHA_EPI",
                "Corte",
                "Dedo",
                null,
                "Ambulatório",
                "BAIXO");

        when(ocorrenciaRepository.findById(70)).thenReturn(Optional.of(entity));

        Acidente acidente = (Acidente) adapter.buscarPorId(70).orElseThrow();

        assertThat(acidente.getArea()).isNull();
        assertThat(acidente.getLocal()).isEqualTo("Oficina");
        assertThat(acidente.getLocalInformado()).isEqualTo("Oficina");
    }
}
