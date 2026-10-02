CREATE TABLE tarefas_alocacoes (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tarefa_id INTEGER NOT NULL REFERENCES tarefas(id) ON DELETE CASCADE,
    colaborador_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    data_alocacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responsavel_alocacao_id INTEGER NOT NULL REFERENCES usuarios(id),
    observacao TEXT
);

CREATE INDEX idx_tarefas_alocacoes_tarefa ON tarefas_alocacoes(tarefa_id);
CREATE INDEX idx_tarefas_alocacoes_colaborador ON tarefas_alocacoes(colaborador_id);
