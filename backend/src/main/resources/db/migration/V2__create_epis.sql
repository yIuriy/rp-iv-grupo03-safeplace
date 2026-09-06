CREATE TABLE epis (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    numero_ca VARCHAR(80) NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    data_validade_ca DATE,
    vida_util_dias INTEGER
);

CREATE TABLE movimentacoes_estoque (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    epi_id INTEGER NOT NULL REFERENCES epis(id),
    tipo VARCHAR(20) NOT NULL,
    quantidade INTEGER NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    motivo TEXT
);
