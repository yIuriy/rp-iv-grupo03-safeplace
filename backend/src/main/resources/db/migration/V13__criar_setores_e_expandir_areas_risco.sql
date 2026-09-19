CREATE TABLE setores (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    descricao TEXT,
    localizacao VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE areas_risco ADD COLUMN setor_id INTEGER REFERENCES setores (id);
ALTER TABLE areas_risco ADD COLUMN agentes_risco TEXT;
ALTER TABLE areas_risco ADD COLUMN limites TEXT;

CREATE INDEX idx_areas_risco_setor_id ON areas_risco (setor_id);

CREATE TABLE logs_auditoria (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_operacao VARCHAR(50) NOT NULL,
    data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsavel VARCHAR(160) NOT NULL,
    justificativa TEXT,
    entidade VARCHAR(100),
    entidade_id INTEGER
);

CREATE INDEX idx_logs_auditoria_entidade ON logs_auditoria (entidade, entidade_id);

ALTER TABLE ocorrencias ADD COLUMN setor_id INTEGER REFERENCES setores (id);
