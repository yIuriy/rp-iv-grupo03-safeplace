package br.edu.safeplace.backend.domain.ocorrencia;

import br.edu.safeplace.backend.domain.ocorrencia.exception.TransicaoStatusInvalidaException;

public enum StatusOcorrencia {
    ABERTA {
        @Override
        public boolean podeTransicaoPara(StatusOcorrencia novoStatus) {
            return novoStatus == EM_TRIAGEM;
        }
    },
    EM_TRIAGEM {
        @Override
        public boolean podeTransicaoPara(StatusOcorrencia novoStatus) {
            return novoStatus == ARQUIVADA;
        }
    },
    ARQUIVADA {
        @Override
        public boolean podeTransicaoPara(StatusOcorrencia novoStatus) {
            return false;
        }
    };

    public abstract boolean podeTransicaoPara(StatusOcorrencia novoStatus);

    public void validarTransicao(StatusOcorrencia novoStatus) {
        if (novoStatus == null || !podeTransicaoPara(novoStatus)) {
            throw new TransicaoStatusInvalidaException(this, novoStatus);
        }
    }
}
