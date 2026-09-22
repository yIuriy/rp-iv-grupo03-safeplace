-- As contas de demonstração da V17 nasceram com CPF de dígitos repetidos, que o domínio
-- (CpfValidador) rejeita ao reconstruir o usuário: o login dessas contas respondia 400 e a
-- listagem de colaboradores falhava inteira. A V18 só corrigia quando os CPFs de destino
-- (52998224725, 11144477735, 12345678909) ainda não existiam; em bancos que já os usavam em
-- outros cadastros, a correção não era aplicada. Esta migração usa CPFs válidos que não aparecem
-- em nenhum teste ou exemplo do repositório e só atua nas contas demo ainda inválidas.

UPDATE usuarios
SET cpf = '96385274128'
WHERE email = 'gestor.demo@safeplace.com'
  AND cpf = '11111111111'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE cpf = '96385274128');

UPDATE usuarios
SET cpf = '12345678062'
WHERE email = 'supervisor.demo@safeplace.com'
  AND cpf = '22222222222'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE cpf = '12345678062');

UPDATE usuarios
SET cpf = '74185296355'
WHERE email = 'colaborador.demo@safeplace.com'
  AND cpf = '33333333333'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE cpf = '74185296355');
