package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.ClassificacaoEPI;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.StatusEpi;
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
        return epiRepository.findByNumeroCa(numeroCa)
                .map(this::toDomain);
    }

    @Override
    public Epi atualizarSaldo(Epi epi) {
        return salvar(epi);
    }

    private EpiEntity toEntity(Epi domain) {
        return new EpiEntity(
                domain.getId(),
                domain.getNome(),
                domain.getNumeroCa(),
                domain.getQuantidade(),
                domain.getEstoqueMinimo(),
                domain.getStatus().name(),
                domain.getDataValidadeCa(),
                domain.getVidaUtilDias(),
                domain.getEspecificacao().getDescricao(),
                domain.getEspecificacao().getClassificacao() == null
                        ? null
                        : domain.getEspecificacao().getClassificacao().name());
    }

    private Epi toDomain(EpiEntity entity) {
        return new Epi(
                entity.getId(),
                entity.getNome(),
                entity.getNumeroCa(),
                entity.getQuantidade(),
                entity.getEstoqueMinimo(),
                StatusEpi.valueOf(entity.getStatus()),
                entity.getDataValidadeCa(),
                entity.getVidaUtilDias(),
                entity.getDescricao(),
                entity.getClassificacao() == null
                        ? null
                        : ClassificacaoEPI.valueOf(entity.getClassificacao()));
    }

    private MovimentacaoEstoqueEntity toEntity(MovimentacaoEstoque domain) {
        return new MovimentacaoEstoqueEntity(
                domain.getId(),
                domain.getEpiId(),
                domain.getTipo().name(),
                domain.getQuantidade(),
                domain.getDataHora(),
                domain.getMotivo());
    }

    private MovimentacaoEstoque toDomain(MovimentacaoEstoqueEntity entity) {
        return new MovimentacaoEstoque(
                entity.getId(),
                entity.getEpiId(),
                TipoMovimentacao.valueOf(entity.getTipo()),
                entity.getQuantidade(),
                entity.getDataHora(),
                entity.getMotivo());
    }
}
