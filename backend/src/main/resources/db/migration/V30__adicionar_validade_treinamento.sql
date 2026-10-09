ALTER TABLE treinamentos
    ADD COLUMN validade_meses INTEGER CHECK (validade_meses IS NULL OR validade_meses >= 1);
