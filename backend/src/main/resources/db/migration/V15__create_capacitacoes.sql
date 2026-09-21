CREATE TABLE certificacoes (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_emissao DATE NOT NULL,
    colaborador_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE treinamentos (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    conteudo TEXT,
    instrutor VARCHAR(255),
    data_realizacao DATE NOT NULL,
    carga_horaria INTEGER
);

CREATE TABLE treinamentos_colaboradores (
    treinamento_id INTEGER NOT NULL REFERENCES treinamentos(id) ON DELETE CASCADE,
    colaborador_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    PRIMARY KEY (treinamento_id, colaborador_id)
);

CREATE TABLE exigencias_capacitacao (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_capacitacao VARCHAR(255) NOT NULL,
    tipo_capacitacao VARCHAR(50) NOT NULL,
    alvo_id INTEGER NOT NULL,
    tipo_alvo VARCHAR(50) NOT NULL
);
