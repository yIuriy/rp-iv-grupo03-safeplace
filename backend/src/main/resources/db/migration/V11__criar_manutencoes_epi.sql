CREATE TABLE manutencoes_epi (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    epi_id INTEGER NOT NULL REFERENCES epis(id),
    data_manutencao TIMESTAMP NOT NULL,
    tipo_manutencao VARCHAR(30) NOT NULL,
    descricao TEXT NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    responsavel_manutencao VARCHAR(160) NOT NULL,
    responsavel_id INTEGER NOT NULL REFERENCES usuarios(id)
);

CREATE INDEX idx_manutencoes_epi_epi_data
    ON manutencoes_epi (epi_id, data_manutencao DESC);
