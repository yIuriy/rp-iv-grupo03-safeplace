-- RF05 e RF06 (UC03, UC11, US05, US06):
-- Garante integridade referencial e semântica com o vocabulário NivelPerigo
-- (BAIXO, MEDIO, ALTO, CRITICO) nas colunas de periculosidade.

ALTER TABLE areas_risco
    ADD CONSTRAINT chk_areas_risco_nivel_perigo
    CHECK (nivel_perigo IN ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'));

ALTER TABLE tarefas
    ADD CONSTRAINT chk_tarefas_nivel_perigo
    CHECK (nivel_perigo IS NULL OR nivel_perigo IN ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'));

ALTER TABLE setores
    ADD CONSTRAINT chk_setores_status
    CHECK (status IN ('BAIXO', 'MEDIO', 'ALTO', 'CRITICO'));
