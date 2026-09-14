CREATE TABLE planos_de_acao (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    medidas_corretivas TEXT,
    prazo DATE,
    status VARCHAR(80),
    medidas_preventivas TEXT
);

CREATE TABLE ocorrencias (
    id_ocorrencia INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data_ocorrencia TIMESTAMP NOT NULL,
    local VARCHAR(160) NOT NULL,
    descricao TEXT NOT NULL,
    plano_de_acao_id INTEGER REFERENCES planos_de_acao (id)
);

CREATE TABLE acidentes (
    id_ocorrencia INTEGER PRIMARY KEY REFERENCES ocorrencias (id_ocorrencia),
    causa_raiz TEXT,
    tipo VARCHAR(80),
    dano TEXT,
    numero_protocolo VARCHAR(80),
    destino VARCHAR(160)
);

CREATE TABLE incidentes (
    id_ocorrencia INTEGER PRIMARY KEY REFERENCES ocorrencias (id_ocorrencia),
    situacao_risco TEXT,
    potencial_dano TEXT
);