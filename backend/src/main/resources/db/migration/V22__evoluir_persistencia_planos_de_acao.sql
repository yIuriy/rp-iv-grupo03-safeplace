-- Evolução da tabela planos_de_acao (Issue #98, RF07, RNF05, UC07, US07)
ALTER TABLE planos_de_acao ADD COLUMN ocorrencia_id INTEGER REFERENCES ocorrencias (id_ocorrencia);
ALTER TABLE planos_de_acao ADD COLUMN colaborador_id INTEGER REFERENCES usuarios (id);
ALTER TABLE planos_de_acao ADD COLUMN setor_id INTEGER REFERENCES setores (id);
ALTER TABLE planos_de_acao ADD COLUMN gestor_id INTEGER REFERENCES usuarios (id);
ALTER TABLE planos_de_acao ADD COLUMN alerta_ativo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE planos_de_acao ADD COLUMN antecedencia_alerta_dias INTEGER;

-- Restrições de integridade
ALTER TABLE planos_de_acao ADD CONSTRAINT chk_planos_responsavel
    CHECK ((colaborador_id IS NOT NULL AND setor_id IS NULL)
        OR (colaborador_id IS NULL AND setor_id IS NOT NULL)
        OR (colaborador_id IS NULL AND setor_id IS NULL));

ALTER TABLE planos_de_acao ADD CONSTRAINT chk_planos_antecedencia
    CHECK (antecedencia_alerta_dias IS NULL OR antecedencia_alerta_dias >= 0);

-- Índices para buscas frequentes
CREATE INDEX idx_planos_de_acao_ocorrencia_id ON planos_de_acao (ocorrencia_id);
CREATE INDEX idx_planos_de_acao_colaborador_id ON planos_de_acao (colaborador_id);
CREATE INDEX idx_planos_de_acao_setor_id ON planos_de_acao (setor_id);
CREATE INDEX idx_planos_de_acao_gestor_id ON planos_de_acao (gestor_id);

-- Migração de dados legados: preenche ocorrencia_id a partir do vínculo anterior
UPDATE planos_de_acao p
SET ocorrencia_id = o.id_ocorrencia
FROM ocorrencias o
WHERE o.plano_de_acao_id = p.id AND p.ocorrencia_id IS NULL;

-- Tabela para histórico estruturado de prorrogações de prazo
CREATE TABLE prorrogacoes_prazo (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plano_de_acao_id INTEGER NOT NULL REFERENCES planos_de_acao (id),
    prazo_anterior DATE NOT NULL,
    novo_prazo DATE NOT NULL,
    justificativa TEXT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prorrogacoes_prazo_plano_id ON prorrogacoes_prazo (plano_de_acao_id);
