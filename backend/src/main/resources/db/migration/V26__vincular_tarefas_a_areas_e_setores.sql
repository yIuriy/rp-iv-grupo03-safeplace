-- #113 / RF05 / RF06: relações aprovadas entre Tarefa, AreaRisco e Setor.
-- EPIs de tarefa pertencem ao RF12/UC08 e não são introduzidos aqui.
ALTER TABLE tarefas
    ADD COLUMN area_risco_id INTEGER REFERENCES areas_risco(id) ON DELETE SET NULL;

CREATE TABLE tarefas_setores (
    tarefa_id INTEGER NOT NULL REFERENCES tarefas(id) ON DELETE CASCADE,
    setor_id INTEGER NOT NULL REFERENCES setores(id) ON DELETE CASCADE,
    PRIMARY KEY (tarefa_id, setor_id)
);

CREATE INDEX idx_tarefas_area_risco_id ON tarefas(area_risco_id);
CREATE INDEX idx_tarefas_setores_setor_id ON tarefas_setores(setor_id);
