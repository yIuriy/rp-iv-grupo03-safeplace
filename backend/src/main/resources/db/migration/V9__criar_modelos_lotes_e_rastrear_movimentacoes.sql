CREATE TABLE modelos_epi (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ca INTEGER NOT NULL,
    marca VARCHAR(160),
    validade_ca DATE NOT NULL
);

CREATE TABLE lotes_epi (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_lote VARCHAR(100),
    nota_fiscal VARCHAR(100),
    data_fabricacao DATE,
    validade DATE,
    quantidade_recebida INTEGER NOT NULL,
    modelo_id INTEGER NOT NULL REFERENCES modelos_epi(id)
);

ALTER TABLE epis ADD COLUMN lote_id INTEGER REFERENCES lotes_epi(id);
ALTER TABLE movimentacoes_estoque ADD COLUMN lote_id INTEGER REFERENCES lotes_epi(id);
ALTER TABLE movimentacoes_estoque ADD COLUMN responsavel_id INTEGER;
