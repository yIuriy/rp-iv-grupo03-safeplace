CREATE TABLE sequencias_protocolo_cat (
    ano SMALLINT NOT NULL,
    mes SMALLINT NOT NULL,
    ultimo_sequencial BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_sequencias_protocolo_cat PRIMARY KEY (ano, mes),
    CONSTRAINT ck_sequencias_protocolo_cat_mes CHECK (mes BETWEEN 1 AND 12)
);

INSERT INTO sequencias_protocolo_cat (ano, mes, ultimo_sequencial)
SELECT
    CAST(SPLIT_PART(numero_protocolo, '-', 2) AS SMALLINT),
    CAST(SPLIT_PART(numero_protocolo, '-', 3) AS SMALLINT),
    MAX(CAST(SPLIT_PART(numero_protocolo, '-', 4) AS BIGINT))
FROM acidentes
WHERE numero_protocolo ~ '^CAT-[0-9]{4}-[0-9]{2}-[0-9]{4,}$'
GROUP BY 1, 2;

CREATE UNIQUE INDEX ux_acidentes_numero_protocolo
    ON acidentes (numero_protocolo)
    WHERE numero_protocolo IS NOT NULL;

CREATE INDEX idx_ocorrencias_data_ocorrencia ON ocorrencias (data_ocorrencia);
CREATE INDEX idx_ocorrencias_data_registro ON ocorrencias (data_registro);
