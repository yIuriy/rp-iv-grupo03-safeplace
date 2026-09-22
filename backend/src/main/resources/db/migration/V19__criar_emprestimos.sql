-- Empréstimos de EPI a colaboradores (RF11, UC12, US11, issue #106). Cada linha retém uma
-- unidade do registro de EPI (decisão #124: EPI é saldo de lote/modelo). O status guarda apenas
-- INDISPONIVEL (em posse) ou DISPONIVEL (devolvido); o atraso é calculado na consulta.
CREATE TABLE emprestimos (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    epi_id INTEGER NOT NULL REFERENCES epis(id),
    colaborador_id INTEGER NOT NULL REFERENCES usuarios(id),
    responsavel_entrega_id INTEGER NOT NULL REFERENCES usuarios(id),
    data_retirada TIMESTAMP NOT NULL,
    data_devolucao_prevista DATE NOT NULL,
    data_devolucao_real TIMESTAMP,
    responsavel_devolucao_id INTEGER REFERENCES usuarios(id),
    estado_devolucao VARCHAR(20),
    status VARCHAR(30) NOT NULL,
    justificativa_substituicao TEXT,
    termo_cautela TEXT NOT NULL,
    CONSTRAINT chk_emprestimos_status CHECK (status IN ('INDISPONIVEL', 'DISPONIVEL')),
    CONSTRAINT chk_emprestimos_estado_devolucao
        CHECK (estado_devolucao IS NULL OR estado_devolucao IN ('APTO', 'MANUTENCAO', 'DESCARTE')),
    CONSTRAINT chk_emprestimos_devolucao_coerente
        CHECK ((status = 'DISPONIVEL') = (data_devolucao_real IS NOT NULL))
);

CREATE INDEX idx_emprestimos_colaborador_status ON emprestimos (colaborador_id, status);
CREATE INDEX idx_emprestimos_epi ON emprestimos (epi_id);
