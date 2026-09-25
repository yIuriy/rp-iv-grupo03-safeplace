-- Criação da tabela de projeções de substituição inteligente de EPIs (RF21, UC10, US21, issue #108)
CREATE TABLE projecoes_substituicao (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    epi_id INTEGER NOT NULL REFERENCES epis(id),
    data_projecao DATE NOT NULL,
    data_estimada_descarte DATE NOT NULL,
    justificativa TEXT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projecoes_substituicao_epi_id ON projecoes_substituicao(epi_id);
CREATE INDEX idx_projecoes_substituicao_data_projecao ON projecoes_substituicao(data_projecao);
