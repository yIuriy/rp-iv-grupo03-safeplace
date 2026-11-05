/*
 * Adaptador JPA do histórico de inspeções (RF09, issue #134): delega ao InspecaoJpaRepository e
 * hidrata o Gestor de Segurança responsável (ponte transicional até a issue #256).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.usuario.GestorDeSeguranca;

@Repository
public class InspecaoJpaAdapter implements InspecaoRepository {

    private final InspecaoJpaRepository inspecaoRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public InspecaoJpaAdapter(InspecaoJpaRepository inspecaoRepository, UsuarioJpaRepository usuarioRepository) {
        this.inspecaoRepository = inspecaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Inspecao salvar(Inspecao inspecao) {
        GestorDeSeguranca gestor = carregarGestor(inspecao.getResponsavel().getId());
        inspecao.definirResponsavel(gestor);
        Inspecao salva = inspecaoRepository.save(inspecao);
        salva.definirResponsavel(gestor);
        return salva;
    }

    @Override
    public List<Inspecao> listarPorArea(Integer areaRiscoId) {
        return inspecaoRepository.findByAreaRisco_IdOrderByDataInspecaoDescIdDesc(areaRiscoId).stream()
                .map(this::hidratarResponsavel)
                .toList();
    }

    private Inspecao hidratarResponsavel(Inspecao inspecao) {
        if (inspecao != null && inspecao.getResponsavel() == null && inspecao.getGestorId() != null) {
            inspecao.definirResponsavel(carregarGestor(inspecao.getGestorId()));
        }
        return inspecao;
    }

    private GestorDeSeguranca carregarGestor(Integer gestorId) {
        return usuarioRepository.findById(gestorId)
                .filter(GestorDeSegurancaEntidade.class::isInstance)
                .map(GestorDeSegurancaEntidade.class::cast)
                .map(GestorDeSegurancaEntidade::paraDominio)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Responsável pela inspeção não encontrado entre os gestores de segurança: " + gestorId));
    }
}
