-- RF09 e US09 (issue #134): histórico de inspeções periódicas das áreas de risco, conforme
-- Inspecao.dataInspecao e ItemVerificado (descricao, conformidade, observacoes) do diagrama de classes.
-- Cada inspeção pertence a uma área e ao Gestor de Segurança que a registrou; cada item pertence a
-- uma única inspeção (docs/arquitetura/correspondencia-classes-inspecao-rf09.md, seção 2.3).
CREATE TABLE inspecoes (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    area_risco_id INTEGER NOT NULL REFERENCES areas_risco (id),
    data_inspecao DATE NOT NULL,
    gestor_id INTEGER NOT NULL REFERENCES usuarios (id)
);

CREATE INDEX idx_inspecoes_area_risco_data ON inspecoes (area_risco_id, data_inspecao DESC, id DESC);

CREATE TABLE itens_verificados (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inspecao_id INTEGER NOT NULL REFERENCES inspecoes (id),
    descricao TEXT NOT NULL,
    conformidade BOOLEAN NOT NULL,
    observacoes TEXT
);

CREATE INDEX idx_itens_verificados_inspecao_id ON itens_verificados (inspecao_id);
