package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.EpiRepository;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Implementa a porta de EPIs delegando aos repositórios JPA das próprias entidades de domínio (#258). */
@Repository
public class EpiJpaAdapter implements EpiRepository {
    private final EpiJpaRepository epiRepository;
    private final MovimentacaoEstoqueJpaRepository movimentacaoRepository;
    private final ModeloEpiJpaRepository modeloRepository;

    public EpiJpaAdapter(EpiJpaRepository epiRepository, MovimentacaoEstoqueJpaRepository movimentacaoRepository,
                         ModeloEpiJpaRepository modeloRepository) {
        this.epiRepository = epiRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.modeloRepository = modeloRepository;
    }

    @Override
    public Epi salvar(Epi epi) {
        return epiRepository.save(epi);
    }

    @Override
    public Optional<Epi> buscarPorId(Integer id) {
        return epiRepository.findById(id);
    }

    @Override
    public Optional<Epi> buscarPorIdComLock(Integer id) {
        return epiRepository.findByIdWithLock(id);
    }

    @Override
    public List<Epi> listarTodos() {
        return epiRepository.findAll();
    }

    @Override
    public Optional<ModeloEPI> buscarModeloPorCa(int ca) {
        return modeloRepository.findFirstByCaOrderByIdAsc(ca);
    }

    @Override
    public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
        return movimentacaoRepository.save(movimentacao);
    }

    @Override
    public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
        return movimentacaoRepository.findByEpiIdOrderByDataHoraDesc(epiId);
    }

    @Override
    public Optional<Epi> buscarPorCA(String numeroCa) {
        return listarPorCA(numeroCa).stream().findFirst();
    }

    @Override
    public List<Epi> listarPorCA(String numeroCa) {
        Integer ca = caNumerico(numeroCa);
        if (ca == null) {
            return List.of();
        }
        return epiRepository.findAllByCa(ca);
    }

    @Override
    public Epi atualizarSaldo(Epi epi) {
        return salvar(epi);
    }

    private static Integer caNumerico(String numeroCa) {
        if (numeroCa == null || numeroCa.isBlank()) {
            return null;
        }
        String normalizado = numeroCa.strip();
        if (normalizado.toUpperCase().startsWith("CA-")) {
            normalizado = normalizado.substring(3).strip();
        }
        if (!normalizado.matches("[0-9]{1,10}")) {
            return null;
        }
        long valor = Long.parseLong(normalizado);
        return valor > Integer.MAX_VALUE ? null : (int) valor;
    }
}
