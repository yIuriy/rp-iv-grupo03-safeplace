-- Adicionar coluna gravidade na tabela acidentes
ALTER TABLE acidentes ADD COLUMN gravidade VARCHAR(20) NOT NULL DEFAULT 'BAIXO';

-- Ajustar colunas na tabela ocorrencias
ALTER TABLE ocorrencias ADD COLUMN data_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE ocorrencias ADD COLUMN status_ocorrencia VARCHAR(30) NOT NULL DEFAULT 'ABERTA';
ALTER TABLE ocorrencias ADD COLUMN area_risco_id INTEGER REFERENCES areas_risco (id);
ALTER TABLE ocorrencias ADD COLUMN colaborador_id INTEGER REFERENCES usuarios (id);
ALTER TABLE ocorrencias ADD COLUMN gestor_id INTEGER REFERENCES usuarios (id);

-- Tornar coluna local legada e opcional
ALTER TABLE ocorrencias ALTER COLUMN local DROP NOT NULL;

-- Desacoplar ocorrencias de setores (ocorrência se vincula via area_risco)
ALTER TABLE ocorrencias DROP COLUMN IF EXISTS setor_id;

-- Índices para buscas frequentes
CREATE INDEX idx_ocorrencias_area_risco_id ON ocorrencias (area_risco_id);
CREATE INDEX idx_ocorrencias_colaborador_id ON ocorrencias (colaborador_id);
CREATE INDEX idx_ocorrencias_status ON ocorrencias (status_ocorrencia);
