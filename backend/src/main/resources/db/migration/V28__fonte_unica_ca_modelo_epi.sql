-- Issue #197 / decisão #124: o CA tem fonte única em modelos_epi. Os EPIs cadastrados antes da V9
-- (sem lote) recebem modelo e lote a partir das colunas legadas antes de elas serem removidas.
-- Nenhum dado é inventado: registros sem CA numérico ou sem validade bloqueiam a migração.

-- 1. Modelos repetidos com o mesmo CA, validade e marca passam a ser um só.
UPDATE lotes_epi l
SET modelo_id = d.manter
FROM (
    SELECT id, MIN(id) OVER (PARTITION BY ca, validade_ca, marca) AS manter
    FROM modelos_epi
) d
WHERE l.modelo_id = d.id
  AND d.id <> d.manter;

DELETE FROM modelos_epi m
WHERE NOT EXISTS (SELECT 1 FROM lotes_epi l WHERE l.modelo_id = m.id)
  AND EXISTS (
      SELECT 1 FROM modelos_epi o
      WHERE o.ca = m.ca
        AND o.validade_ca = m.validade_ca
        AND o.marca IS NOT DISTINCT FROM m.marca
        AND o.id < m.id
  );

-- 2. EPIs legados sem lote passam a apontar para modelo e lote próprios.
DO $$
DECLARE
    pendentes TEXT;
    registro RECORD;
    ca_numerico INTEGER;
    modelo INTEGER;
    lote INTEGER;
BEGIN
    SELECT string_agg(id::TEXT, ', ' ORDER BY id) INTO pendentes
    FROM epis
    WHERE lote_id IS NULL
      AND (
          data_validade_ca IS NULL
          OR CASE
                 WHEN regexp_replace(btrim(numero_ca), '^CA-', '') ~ '^[0-9]{1,10}$'
                     THEN regexp_replace(btrim(numero_ca), '^CA-', '')::BIGINT NOT BETWEEN 1 AND 2147483647
                 ELSE TRUE
             END
      );

    IF pendentes IS NOT NULL THEN
        RAISE EXCEPTION 'EPIs sem lote com CA ou validade do CA ausente ou inválido (ids: %). Corrija esses registros antes de aplicar a V28.', pendentes;
    END IF;

    FOR registro IN
        SELECT id, numero_ca, data_validade_ca, quantidade
        FROM epis
        WHERE lote_id IS NULL
        ORDER BY id
    LOOP
        ca_numerico := regexp_replace(btrim(registro.numero_ca), '^CA-', '')::INTEGER;
        modelo := NULL;

        SELECT id INTO modelo
        FROM modelos_epi
        WHERE ca = ca_numerico
          AND validade_ca = registro.data_validade_ca
        ORDER BY id
        LIMIT 1;

        IF modelo IS NULL THEN
            INSERT INTO modelos_epi (ca, marca, validade_ca)
            VALUES (ca_numerico, NULL, registro.data_validade_ca)
            RETURNING id INTO modelo;
        END IF;

        -- Mesmo valor que a API já expunha como quantidade recebida desses registros.
        INSERT INTO lotes_epi (quantidade_recebida, modelo_id)
        VALUES (registro.quantidade, modelo)
        RETURNING id INTO lote;

        UPDATE epis SET lote_id = lote WHERE id = registro.id;
    END LOOP;
END $$;

-- 3. O EPI depende do lote e deixa de guardar cópia do CA.
ALTER TABLE epis ALTER COLUMN lote_id SET NOT NULL;
ALTER TABLE epis
    DROP COLUMN numero_ca,
    DROP COLUMN data_validade_ca;

CREATE INDEX idx_modelos_epi_ca ON modelos_epi (ca);

-- 4. US03: toda nova movimentação identifica o usuário responsável. NOT VALID preserva o
-- histórico anterior à V10, cujo responsável não foi registrado e não pode ser reconstituído.
ALTER TABLE movimentacoes_estoque
    ADD CONSTRAINT chk_movimentacoes_estoque_responsavel
    CHECK (responsavel_id IS NOT NULL) NOT VALID;
