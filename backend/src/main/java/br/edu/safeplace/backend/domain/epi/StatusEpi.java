package br.edu.safeplace.backend.domain.epi;

import java.util.EnumSet;
import java.util.Set;

/**
 * Situação do registro de EPI (State, #224). Cada constante declara para onde pode ir; o {@link Epi} muda de
 * situação só por {@link Epi#transicionarPara(StatusEpi)}. Pela decisão #124, a situação vale para o registro
 * inteiro e o empréstimo debita o saldo, sem marcar o registro como {@code EM_USO}: nenhum fluxo do MVP leva
 * a {@code EM_USO}, que só existe em registros antigos e pode sair para qualquer situação ativa.
 */
public enum StatusEpi {
    DISPONIVEL {
        @Override
        Set<StatusEpi> destinos() {
            // UC06 saldo zerado; UC01 envio para manutenção; UC12 descarte na devolução.
            return EnumSet.of(ESGOTADO, EM_MANUTENCAO, DESCARTADO);
        }

        @Override
        public StatusEpi aposMudancaDeSaldo(int saldo) {
            return saldo == 0 ? ESGOTADO : this;
        }
    },
    EM_USO {
        @Override
        Set<StatusEpi> destinos() {
            return EnumSet.of(DISPONIVEL, ESGOTADO, EM_MANUTENCAO, DESCARTADO);
        }
    },
    EM_MANUTENCAO {
        @Override
        Set<StatusEpi> destinos() {
            // Manutenção aprovada (#124) ou descarte na devolução (UC12); a reprovada não descarta.
            return EnumSet.of(DISPONIVEL, DESCARTADO);
        }
    },
    ESGOTADO {
        @Override
        Set<StatusEpi> destinos() {
            // UC06 entrada de estoque; UC12 devolução para manutenção ou descarte com saldo zerado.
            return EnumSet.of(DISPONIVEL, EM_MANUTENCAO, DESCARTADO);
        }

        @Override
        public StatusEpi aposMudancaDeSaldo(int saldo) {
            return saldo > 0 ? DISPONIVEL : this;
        }
    },
    DESCARTADO {
        @Override
        Set<StatusEpi> destinos() {
            return EnumSet.noneOf(StatusEpi.class);
        }
    };

    abstract Set<StatusEpi> destinos();

    public boolean podeTransicaoPara(StatusEpi novoStatus) {
        return novoStatus != null && destinos().contains(novoStatus);
    }

    /**
     * Situação depois que o saldo do registro muda (UC06): só disponível e esgotado acompanham o saldo; as
     * demais situações se mantêm.
     */
    public StatusEpi aposMudancaDeSaldo(int saldo) {
        return this;
    }
}
