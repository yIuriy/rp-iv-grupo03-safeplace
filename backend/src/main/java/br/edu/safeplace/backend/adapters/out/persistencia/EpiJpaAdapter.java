package br.edu.safeplace.backend.adapters.out.persistencia;

import br.edu.safeplace.backend.application.port.out.EpiRepositoryPort;
import br.edu.safeplace.backend.domain.epi.Epi;
import br.edu.safeplace.backend.domain.epi.ModeloEPI;
import br.edu.safeplace.backend.domain.epi.MovimentacaoEstoque;
import br.edu.safeplace.backend.domain.epi.TipoMovimentacao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EpiJpaAdapter implements EpiRepositoryPort {
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
        EpiEntity entity = toEntity(epi);
        vincularModeloPersistido(entity.getLote());
        EpiEntity salvo = epiRepository.save(entity);
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
    public Optional<ModeloEPI> buscarModeloPorCa(int ca) {
        return modeloRepository.findFirstByCaOrderByIdAsc(ca)
                .map(modelo -> new ModeloEPI(modelo.getId(), modelo.getCa(), modelo.getMarca(),
                        modelo.getValidadeCA()));
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
        Integer ca = caNumerico(numeroCa);
        if (ca == null) {
            return List.of();
        }
        return epiRepository.findAllByCa(ca)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Epi atualizarSaldo(Epi epi) {
        return salvar(epi);
    }

    /**
     * Um modelo com ID já existe no banco: o lote passa a referenciar a instância gerenciada, em vez
     * de uma cópia destacada que a cascata de persistência recusaria.
     */
    private void vincularModeloPersistido(LoteEpiEntity lote) {
        ModeloEpiEntity convertido = lote.getModelo();
        if (convertido.getId() == null) {
            return;
        }
        ModeloEpiEntity persistido = modeloRepository.findById(convertido.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Modelo de EPI " + convertido.getId() + " não encontrado."));
        persistido.completarMarca(convertido.getMarca());
        lote.vincularModelo(persistido);
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
