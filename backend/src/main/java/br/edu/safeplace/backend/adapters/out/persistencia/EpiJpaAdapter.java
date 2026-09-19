package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EpiJpaAdapter implements EpiRepositoryPort {
    private final EpiJpaRepository epiRepository;
    private final MovimentacaoEstoqueJpaRepository movimentacaoRepository;

    public EpiJpaAdapter(EpiJpaRepository epiRepository, MovimentacaoEstoqueJpaRepository movimentacaoRepository) {
        this.epiRepository = epiRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Override
    public Epi salvar(Epi epi) {
        EpiEntity salvo = epiRepository.save(toEntity(epi));
        return toDomain(salvo);
    }

    @Override
    public Optional<Epi> buscarPorId(Integer id) {
        return epiRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Epi> buscarPorIdComLock(Integer id) {
        return epiRepository.findByIdWithLock(id).map(this::toDomain);
    }

    @Override
    public List<Epi> listarTodos() {
        return epiRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public MovimentacaoEstoque salvarMovimentacao(MovimentacaoEstoque movimentacao) {
        MovimentacaoEstoqueEntity salvo = movimentacaoRepository.save(toEntity(movimentacao));
        return toDomain(salvo);
    }

    @Override
    public List<MovimentacaoEstoque> listarMovimentacoesPorEpi(Integer epiId) {
        return movimentacaoRepository.findByEpiIdOrderByDataHoraDesc(epiId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Epi> buscarPorCA(String numeroCa) {
        return listarPorCA(numeroCa).stream().findFirst();
    }

    @Override
    public List<Epi> listarPorCA(String numeroCa) {
        if (numeroCa == null || numeroCa.isBlank()) {
            return List.of();
        }
        String normalizado = numeroCa.strip();
        if (normalizado.toUpperCase().startsWith("CA-")) {
            normalizado = normalizado.substring(3).strip();
        }
        List<Epi> encontrados = epiRepository.findAllByNumeroCa(normalizado)
                .stream()
                .map(this::toDomain)
                .toList();
        if (encontrados.isEmpty() && !normalizado.equals(numeroCa)) {
            encontrados = epiRepository.findAllByNumeroCa(numeroCa)
                    .stream()
                    .map(this::toDomain)
                    .toList();
        }
        return encontrados;
    }

    @Override
    public Epi atualizarSaldo(Epi epi) {
        return salvar(epi);
    }

    private EpiEntity toEntity(Epi domain) {
        return EpiEntityMapper.toEntity(domain);
    }

    private Epi toDomain(EpiEntity entity) {
        return EpiEntityMapper.toDomain(entity);
    }

    private MovimentacaoEstoqueEntity toEntity(MovimentacaoEstoque domain) {
        return new MovimentacaoEstoqueEntity(
                domain.getId(),
                domain.getEpiId(),
                domain.getLoteId(),
                domain.getResponsavelId(),
                domain.getTipo().name(),
                domain.getQuantidade(),
                domain.getDataHora(),
                domain.getMotivo());
    }

    private MovimentacaoEstoque toDomain(MovimentacaoEstoqueEntity entity) {
        return new MovimentacaoEstoque(
                entity.getId(),
                entity.getEpiId(),
                entity.getLoteId(),
                entity.getResponsavelId(),
                TipoMovimentacao.valueOf(entity.getTipo()),
                entity.getQuantidade(),
                entity.getDataHora(),
                entity.getMotivo());
    }
}
