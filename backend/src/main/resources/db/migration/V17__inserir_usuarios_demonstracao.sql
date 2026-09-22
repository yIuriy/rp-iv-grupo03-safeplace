-- Contas locais de demonstração. A senha dos perfis com acesso é "Senha@123"
-- e está armazenada exclusivamente como hash BCrypt.
-- A condição evita duplicar ou sobrescrever cadastros existentes ao aplicar a migração.

INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
SELECT '11111111111', 'Gestor Demonstração', DATE '1985-01-15', 'gestor.demo@safeplace.com',
       '$2a$10$7gNSg9IkERgnvHkZe8iTKuaqCseJemktdREjaJXJPnPxHKLCx8gJi', 'GESTOR_SEGURANCA', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios
    WHERE cpf = '11111111111' OR email = 'gestor.demo@safeplace.com'
);

INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
SELECT '22222222222', 'Supervisor Demonstração', DATE '1988-05-20', 'supervisor.demo@safeplace.com',
       '$2a$10$7gNSg9IkERgnvHkZe8iTKuaqCseJemktdREjaJXJPnPxHKLCx8gJi', 'SUPERVISOR', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios
    WHERE cpf = '22222222222' OR email = 'supervisor.demo@safeplace.com'
);

INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil, ativo)
SELECT '33333333333', 'Colaborador Demonstração', DATE '1992-09-10',
       'colaborador.demo@safeplace.com', NULL, 'COLABORADOR', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios
    WHERE cpf = '33333333333' OR email = 'colaborador.demo@safeplace.com'
);
