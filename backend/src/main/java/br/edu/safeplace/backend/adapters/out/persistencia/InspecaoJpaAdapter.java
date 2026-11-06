/*
 * Adaptador JPA do histórico de inspeções (RF09, issue #134): delega ao InspecaoJpaRepository. O Gestor de
 * Segurança responsável é uma associação da própria entidade (#256).
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.InspecaoRepository;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;

@Repository
public class InspecaoJpaAdapter implements InspecaoRepository {

    private final InspecaoJpaRepository inspecaoRepository;

    public InspecaoJpaAdapter(InspecaoJpaRepository inspecaoRepository) {
        this.inspecaoRepository = inspecaoRepository;
    }

    @Override
    public Inspecao salvar(Inspecao inspecao) {
        return inspecaoRepository.save(inspecao);
    }

    @Override
    public List<Inspecao> listarPorArea(Integer areaRiscoId) {
        return inspecaoRepository.findByAreaRisco_IdOrderByDataInspecaoDescIdDesc(areaRiscoId);
    }
}
