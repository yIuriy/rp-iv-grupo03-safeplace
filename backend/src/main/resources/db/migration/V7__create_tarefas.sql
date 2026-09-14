-- UC11: a tarefa pode existir sem classificação; o cenário de exceção I prevê justamente a
-- tentativa de alocação em atividade sem grau de risco atribuído. Por isso nivel_perigo é nulo.
CREATE TABLE tarefas (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao TEXT NOT NULL,
    nivel_perigo VARCHAR(20),
    data_classificacao TIMESTAMP
);

CREATE INDEX idx_tarefas_nivel_perigo ON tarefas (nivel_perigo);
