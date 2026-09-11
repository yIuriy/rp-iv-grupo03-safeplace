-- Issue #91: restrições de credenciais na tabela usuarios.
-- A V3 já garante unicidade de cpf e email (UNIQUE cria índice único no PostgreSQL).
-- Aqui restringimos o perfil aos valores do enum Perfil e garantimos que
-- Colaborador nunca tenha senha e que perfis com acesso sempre tenham o hash.

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_perfil
        CHECK (perfil IN ('COLABORADOR', 'SUPERVISOR', 'GESTOR_SEGURANCA'));

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_senha_por_perfil
        CHECK (
            (perfil = 'COLABORADOR' AND senha IS NULL)
            OR (perfil <> 'COLABORADOR' AND senha IS NOT NULL)
        );
