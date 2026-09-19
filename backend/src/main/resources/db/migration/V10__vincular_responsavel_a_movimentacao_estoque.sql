ALTER TABLE movimentacoes_estoque
    ADD CONSTRAINT fk_movimentacoes_estoque_responsavel
    FOREIGN KEY (responsavel_id) REFERENCES usuarios(id);
