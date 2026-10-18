-- Issue #196 (RF07; UC07, US07): situação e responsável das ações do plano garantidos pelo banco e
-- avisos de prazo e de reatribuição gravados para consulta.

-- 1. Situação legada (decisão 2.8 da correspondência do plano de ação). A API anterior ao contrato
-- aceitava qualquer texto em status. Os textos com o mesmo sentido do vocabulário aprovado são
-- normalizados: execução ainda não confirmada vira ABERTA; execução confirmada vira CONCLUIDA.
UPDATE planos_de_acao
SET status = 'ABERTA'
WHERE UPPER(TRIM(status)) IN ('ABERTA', 'ABERTO', 'PENDENTE', 'EM_ANDAMENTO', 'EM ANDAMENTO');

UPDATE planos_de_acao
SET status = 'CONCLUIDA'
WHERE UPPER(TRIM(status)) IN ('CONCLUIDA', 'CONCLUIDO', 'CONCLUÍDA', 'CONCLUÍDO', 'FINALIZADA', 'FINALIZADO');

-- 2. O que não tem correspondência (por exemplo, CANCELADO ou texto vazio) e a ação sem exatamente um
-- responsável não são resolvidos por suposição: a migração para antes de alterar o esquema e informa
-- os ids, para que o Gestor decida a situação ou o responsável de cada um.
DO $$
DECLARE
    situacoes_pendentes TEXT;
    responsaveis_pendentes TEXT;
BEGIN
    SELECT string_agg(format('%s (%s)', id, COALESCE(NULLIF(TRIM(status), ''), 'vazio')), ', ' ORDER BY id)
    INTO situacoes_pendentes
    FROM planos_de_acao
    WHERE status IS NULL OR status NOT IN ('ABERTA', 'CONCLUIDA');

    SELECT string_agg(id::TEXT, ', ' ORDER BY id)
    INTO responsaveis_pendentes
    FROM planos_de_acao
    WHERE (colaborador_id IS NULL) = (setor_id IS NULL);

    IF situacoes_pendentes IS NOT NULL OR responsaveis_pendentes IS NOT NULL THEN
        RAISE EXCEPTION 'Ações do plano que precisam de decisão antes da V33 (issue #196). Situação sem correspondência com ABERTA ou CONCLUIDA: %. Sem exatamente um responsável (colaborador ou setor): %.',
            COALESCE(situacoes_pendentes, 'nenhuma'), COALESCE(responsaveis_pendentes, 'nenhuma');
    END IF;
END $$;

-- 3. Situação restrita ao vocabulário aprovado (decisão 2.3) e exatamente um responsável por ação
-- (decisão 2.2). A restrição da V22 também aceitava os dois responsáveis nulos.
ALTER TABLE planos_de_acao ALTER COLUMN status SET NOT NULL;

ALTER TABLE planos_de_acao
    ADD CONSTRAINT chk_planos_status CHECK (status IN ('ABERTA', 'CONCLUIDA'));

ALTER TABLE planos_de_acao DROP CONSTRAINT chk_planos_responsavel;

ALTER TABLE planos_de_acao
    ADD CONSTRAINT chk_planos_responsavel CHECK ((colaborador_id IS NULL) <> (setor_id IS NULL));

-- Ações acompanhadas pela verificação periódica dos alertas de prazo.
CREATE INDEX idx_planos_de_acao_alerta_aberta ON planos_de_acao (id)
    WHERE alerta_ativo AND status = 'ABERTA';

-- 4. Avisos das ações (UC07, regra 2 e cenário alternativo II; decisão 2.6), entregues ao Gestor de
-- Segurança. O alerta de prazo é emitido uma única vez por ação e prazo: a prorrogação muda o prazo e
-- rearma o alerta. A reatribuição gera um aviso a cada troca de responsável.
CREATE TABLE notificacoes_acao (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    acao_id INTEGER NOT NULL REFERENCES planos_de_acao (id),
    ocorrencia_id INTEGER REFERENCES ocorrencias (id_ocorrencia),
    motivo VARCHAR(20) NOT NULL,
    responsavel_tipo VARCHAR(20) NOT NULL,
    responsavel_id INTEGER NOT NULL,
    responsavel_nome VARCHAR(160),
    prazo DATE NOT NULL,
    data_aviso DATE NOT NULL,
    mensagem TEXT NOT NULL,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notificacoes_acao_motivo CHECK (motivo IN ('PRAZO', 'REATRIBUICAO')),
    CONSTRAINT chk_notificacoes_acao_responsavel CHECK (responsavel_tipo IN ('COLABORADOR', 'SETOR'))
);

CREATE UNIQUE INDEX ux_notificacoes_acao_prazo ON notificacoes_acao (acao_id, prazo)
    WHERE motivo = 'PRAZO';

CREATE INDEX idx_notificacoes_acao_criada_em ON notificacoes_acao (criada_em);
