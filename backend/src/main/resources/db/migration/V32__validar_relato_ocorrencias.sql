-- Issue #195 (RF01, RF02, RF13, RF16; UC05, UC09): o backend passa a garantir as regras do relato,
-- sem depender da validação do frontend.

-- 1. Área de risco e colaborador envolvido são obrigatórios (DS09, seções 2.2 e 2.3). Relatos
-- antigos sem esses vínculos não podem ser completados por suposição: a migração para e informa
-- quais são, para que o Gestor os complete antes de aplicá-la.
DO $$
DECLARE
    pendentes TEXT;
BEGIN
    SELECT string_agg(id_ocorrencia::TEXT, ', ' ORDER BY id_ocorrencia)
    INTO pendentes
    FROM ocorrencias
    WHERE area_risco_id IS NULL OR colaborador_id IS NULL;

    IF pendentes IS NOT NULL THEN
        RAISE EXCEPTION 'Ocorrências sem área de risco ou colaborador envolvido: %. Informe area_risco_id e colaborador_id desses registros antes de aplicar a V32 (issue #195).', pendentes;
    END IF;
END $$;

ALTER TABLE ocorrencias ALTER COLUMN area_risco_id SET NOT NULL;
ALTER TABLE ocorrencias ALTER COLUMN colaborador_id SET NOT NULL;

-- 2. Gravidade e causa raiz do acidente não recebem valor padrão nem aceitam valores fora do
-- vocabulário aprovado (NivelPerigo e CausaRaizAcidente).
ALTER TABLE acidentes ALTER COLUMN gravidade DROP DEFAULT;

ALTER TABLE acidentes
    ADD CONSTRAINT chk_acidentes_gravidade
    CHECK (gravidade IN ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'));

ALTER TABLE acidentes
    ADD CONSTRAINT chk_acidentes_causa_raiz
    CHECK (causa_raiz IS NULL OR causa_raiz IN ('FATOR_HUMANO', 'FALHA_EPI', 'OUTRO'));

-- 3. O relator é quem registrou o relato, Gestor de Segurança ou Supervisor, definido pelo servidor.
-- A coluna gestor_id só aceitava gestores e ficava nula nos relatos do Supervisor; passa a se chamar
-- relator_id. Os relatos antigos sem relator são completados pela trilha de auditoria do cadastro.
ALTER TABLE ocorrencias RENAME COLUMN gestor_id TO relator_id;

UPDATE ocorrencias o
SET relator_id = u.id
FROM logs_auditoria l
JOIN usuarios u ON LOWER(u.email) = LOWER(l.responsavel)
WHERE o.relator_id IS NULL
  AND l.tipo_operacao = 'CADASTRO'
  AND l.entidade IN ('Acidente', 'Incidente')
  AND l.entidade_id = o.id_ocorrencia;

CREATE INDEX idx_ocorrencias_relator_id ON ocorrencias (relator_id);

-- 4. UC09, passo 12: a notificação ao Gestor de Segurança fica gravada e pode ser consultada.
CREATE TABLE notificacoes_ocorrencia (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ocorrencia_id INTEGER NOT NULL REFERENCES ocorrencias (id_ocorrencia),
    tipo_ocorrencia VARCHAR(20) NOT NULL,
    numero_protocolo_cat VARCHAR(80),
    area_nome VARCHAR(160),
    mensagem TEXT NOT NULL,
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notificacoes_ocorrencia_tipo CHECK (tipo_ocorrencia IN ('ACIDENTE', 'INCIDENTE'))
);

CREATE INDEX idx_notificacoes_ocorrencia_criada_em ON notificacoes_ocorrencia (criada_em);
CREATE INDEX idx_notificacoes_ocorrencia_ocorrencia_id ON notificacoes_ocorrencia (ocorrencia_id);
