-- V17 já pode estar registrada em bancos existentes. Corrige somente os CPFs
-- das contas de demonstração criadas por ela, preservando demais usuários.

UPDATE usuarios
SET cpf = '52998224725'
WHERE email = 'gestor.demo@safeplace.com'
  AND cpf = '11111111111'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE cpf = '52998224725');

UPDATE usuarios
SET cpf = '11144477735'
WHERE email = 'supervisor.demo@safeplace.com'
  AND cpf = '22222222222'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE cpf = '11144477735');

UPDATE usuarios
SET cpf = '12345678909'
WHERE email = 'colaborador.demo@safeplace.com'
  AND cpf = '33333333333'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE cpf = '12345678909');
