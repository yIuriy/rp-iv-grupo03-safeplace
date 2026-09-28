-- UC11, regra 1, e US06: cada classificação de periculosidade registra o responsável técnico
-- (Gestor de Segurança), além da data e hora já gravadas em data_classificacao (issue #112).
-- A coluna é opcional: tarefas classificadas antes desta versão não têm autoria registrada na
-- própria tabela e não recebem autor inventado.
ALTER TABLE tarefas
    ADD COLUMN responsavel_classificacao_id INTEGER REFERENCES usuarios (id);

CREATE INDEX idx_tarefas_responsavel_classificacao_id ON tarefas (responsavel_classificacao_id);
