-- Issue #193 / RNF05: a trilha de auditoria é somente inclusão e o histórico vinculado a pessoas e
-- tarefas não pode ser apagado em cascata.

-- 1. RNF05, item 1: logs_auditoria não aceita UPDATE, DELETE nem TRUNCATE. Sem rotina de expurgo
-- aprovada (item 4), todo registro fica retido, o que cumpre o mínimo de 5 anos (item 2).
CREATE FUNCTION impedir_alteracao_log_auditoria() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'logs_auditoria é somente inclusão (RNF05): % não permitido.', TG_OP
        USING ERRCODE = 'insufficient_privilege';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_logs_auditoria_imutavel
    BEFORE UPDATE OR DELETE ON logs_auditoria
    FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_log_auditoria();

CREATE TRIGGER trg_logs_auditoria_sem_truncate
    BEFORE TRUNCATE ON logs_auditoria
    FOR EACH STATEMENT EXECUTE FUNCTION impedir_alteracao_log_auditoria();

-- 2. RNF05 e RF23 ("preservando seus vínculos"): excluir pessoa, tarefa ou treinamento deixa de
-- apagar certificações, participações em treinamentos e alocações. As chaves passam a restringir.
DO $$
DECLARE
    alvo RECORD;
    restricao TEXT;
BEGIN
    FOR alvo IN
        SELECT * FROM (VALUES
            ('certificacoes', 'colaborador_id', 'usuarios'),
            ('treinamentos_colaboradores', 'treinamento_id', 'treinamentos'),
            ('treinamentos_colaboradores', 'colaborador_id', 'usuarios'),
            ('tarefas_alocacoes', 'tarefa_id', 'tarefas'),
            ('tarefas_alocacoes', 'colaborador_id', 'usuarios')
        ) AS v(tabela, coluna, referenciada)
    LOOP
        FOR restricao IN
            SELECT c.conname
            FROM pg_constraint c
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
            WHERE c.contype = 'f'
              AND c.conrelid = alvo.tabela::regclass
              AND c.confrelid = alvo.referenciada::regclass
              AND a.attname = alvo.coluna
        LOOP
            EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', alvo.tabela, restricao);
        END LOOP;

        EXECUTE format(
            'ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES %I (id) ON DELETE RESTRICT',
            alvo.tabela, 'fk_' || alvo.tabela || '_' || alvo.coluna, alvo.coluna, alvo.referenciada);
    END LOOP;
END $$;
