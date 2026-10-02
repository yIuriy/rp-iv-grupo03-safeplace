/*
 * Adaptador JPA do histórico de inspeções (RF09, issue #134): converte Inspecao em entidade com seus
 * itens, grava e reconstrói as inspeções de uma área com o Gestor de Segurança responsável.
 */
package br.edu.safeplace.backend.adapters.out.persistencia;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.edu.safeplace.backend.application.port.out.InspecaoRepositoryPort;
import br.edu.safeplace.backend.domain.inspecao.Inspecao;
import br.edu.safeplace.backend.domain.inspecao.ItemVerificado;

@Repository
public class InspecaoJpaAdapter implements InspecaoRepositoryPort {

    private final InspecaoJpaRepository inspecaoRepository;
    private final UsuarioJpaRepositorio usuarioRepository;

    public InspecaoJpaAdapter(InspecaoJpaRepository inspecaoRepository, UsuarioJpaRepositorio usuarioRepository) {
        this.inspecaoRepository = inspecaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Inspecao salvar(Inspecao inspecao) {
        InspecaoEntity entidade = new InspecaoEntity(
                inspecao.getAreaRiscoId(), inspecao.getDataInspecao(), carregarGestor(inspecao.getResponsavel().getId()));
        inspecao.getItens().forEach(item ->
                entidade.adicionarItem(item.getDescricao(), item.isConformidade(), item.getObservacoes()));
        return toDomain(inspecaoRepository.save(entidade));
    }

    @Override
    public List<Inspecao> listarPorArea(Integer areaRiscoId) {
        return inspecaoRepository.findByAreaRiscoIdOrderByDataInspecaoDescIdDesc(areaRiscoId).stream()
                .map(this::toDomain)
                .toList();
    }

    private GestorDeSegurancaEntidade carregarGestor(Integer gestorId) {
        return usuarioRepository.findById(gestorId)
                .filter(GestorDeSegurancaEntidade.class::isInstance)
                .map(GestorDeSegurancaEntidade.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Responsável pela inspeção não encontrado entre os gestores de segurança: " + gestorId));
    }

    private Inspecao toDomain(InspecaoEntity entidade) {
        return new Inspecao(
                entidade.getId(),
                entidade.getAreaRiscoId(),
                entidade.getDataInspecao(),
                entidade.getGestor().paraDominio(),
                entidade.getItens().stream()
                        .map(item -> new ItemVerificado(item.getId(), item.getDescricao(),
                                item.isConformidade(), item.getObservacoes()))
                        .toList());
    }
}
