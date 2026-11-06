package br.edu.safeplace.backend.adapters.out.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.safeplace.backend.domain.ocorrencia.SequenciaProtocoloCat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Unidade - SequenciaProtocoloCatJpaRepository")
class SequenciaProtocoloCatJpaRepositoryTest {

    @Mock
    private SequenciaProtocoloCatSpringDataRepository springDataRepository;

    @Mock
    private SequenciaProtocoloCatInicializador inicializador;

    private SequenciaProtocoloCatJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SequenciaProtocoloCatJpaRepository(springDataRepository, inicializador);
    }

    @Test
    @DisplayName("Quando a linha já existe no mês, recupera com lock, incrementa e salva sem chamar inicializador")
    void quandoLinhaExisteIncrementaESalva() {
        SequenciaProtocoloCat seq = new SequenciaProtocoloCat(2026, 11, 7L);
        when(springDataRepository.findByAnoAndMesComLock((short) 2026, (short) 11))
                .thenReturn(Optional.of(seq));

        long resultado = repository.reservarProximoSequencial(2026, 11);

        assertThat(resultado).isEqualTo(8L);
        assertThat(seq.getUltimoSequencial()).isEqualTo(8L);
        verify(springDataRepository).save(seq);
        verifyNoInteractions(inicializador);
    }

    @Test
    @DisplayName("Quando a linha ainda não existe no mês, garante inicialização antes de travar com lock")
    void quandoLinhaNaoExisteChamaInicializador() {
        SequenciaProtocoloCat seq = new SequenciaProtocoloCat(2026, 11, 0L);
        when(springDataRepository.findByAnoAndMesComLock((short) 2026, (short) 11))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(seq));

        long resultado = repository.reservarProximoSequencial(2026, 11);

        assertThat(resultado).isEqualTo(1L);
        assertThat(seq.getUltimoSequencial()).isEqualTo(1L);

        InOrder inOrder = inOrder(inicializador, springDataRepository);
        inOrder.verify(springDataRepository).findByAnoAndMesComLock((short) 2026, (short) 11);
        inOrder.verify(inicializador).garantirLinhaCriada((short) 2026, (short) 11);
        inOrder.verify(springDataRepository).findByAnoAndMesComLock((short) 2026, (short) 11);
        inOrder.verify(springDataRepository).save(seq);
    }
}
