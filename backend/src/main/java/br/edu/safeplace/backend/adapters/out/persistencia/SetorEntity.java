package br.edu.safeplace.backend.adapters.out.persistencia;

import java.time.LocalDateTime;

import br.edu.safeplace.backend.domain.area_risco.Setor;
import br.edu.safeplace.backend.domain.comum.NivelPerigo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "setores")
public class SetorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(length = 255)
    private String localizacao;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public SetorEntity() {
    }

    public SetorEntity(Integer id, String nome, String descricao, String localizacao, String status) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.status = status;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public static SetorEntity deDominio(Setor setor) {
        if (setor == null) {
            return null;
        }
        return new SetorEntity(
                setor.getId(),
                setor.getNome(),
                setor.getDescricao(),
                setor.getLocalizacao(),
                setor.getStatus().name()
        );
    }

    public Setor paraDominio() {
        return Setor.reconstituir(
                id,
                nome,
                descricao,
                localizacao,
                NivelPerigo.valueOf(status)
        );
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public String getStatus() {
        return status;
    }
}
