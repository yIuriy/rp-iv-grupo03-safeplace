-- RF12 / UC08: vinculo entre Tarefas e EPIs obrigatorios (#135).
CREATE TABLE tarefas_epis (
    tarefa_id INTEGER NOT NULL REFERENCES tarefas(id) ON DELETE CASCADE,
    epi_id INTEGER NOT NULL REFERENCES epis(id) ON DELETE CASCADE,
    PRIMARY KEY (tarefa_id, epi_id)
);

CREATE INDEX idx_tarefas_epis_epi_id ON tarefas_epis(epi_id);
