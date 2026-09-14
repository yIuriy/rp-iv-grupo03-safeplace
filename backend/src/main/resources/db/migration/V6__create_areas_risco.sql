CREATE TABLE areas_risco (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nome VARCHAR(160) NOT NULL,
    descricao TEXT,
    nivel_perigo VARCHAR(20) NOT NULL
);

-- UC03, RN1: toda área de risco exige o vínculo dos EPIs obrigatórios para acesso ao setor.
CREATE TABLE areas_risco_epis (
    area_risco_id INTEGER NOT NULL REFERENCES areas_risco (id),
    epi_id INTEGER NOT NULL REFERENCES epis (id),
    PRIMARY KEY (area_risco_id, epi_id)
);

CREATE INDEX idx_areas_risco_nivel_perigo ON areas_risco (nivel_perigo);
