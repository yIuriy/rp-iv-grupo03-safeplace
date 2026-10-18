-- Dados de demonstração do SafePlace para a apresentação (ambiente local).
-- NÃO é migração Flyway: rode manualmente sobre um banco já migrado (V1 a V33), por exemplo:
--   docker exec -i safeplace-postgres-dev psql -U safeplace -d safeplace < scripts/dados-demonstracao.sql
-- Requer as contas de demonstração da V17 (gestor.demo e supervisor.demo, senha "Senha@123").
-- Todas as datas são relativas ao dia da execução, para que alertas e atrasos apareçam.
-- O script é executado uma única vez: se o setor "Galpão de Produção" já existir, nada é inserido.

DO $$
DECLARE
    hoje DATE := CURRENT_DATE;
    agora TIMESTAMP := LOCALTIMESTAMP;
    gestor_email TEXT := 'gestor.demo@safeplace.com';
    sup_email TEXT := 'supervisor.demo@safeplace.com';
    v_gestor INT; v_sup INT; v_sup2 INT;
    c_ana INT; c_bruno INT; c_carlos INT; c_daniela INT; c_eduardo INT; c_fernanda INT; c_gabriel INT; c_helena INT;
    s_prod INT; s_manut INT; s_almox INT; s_pint INT;
    m_cap INT; m_luva INT; m_ocul INT; m_prot INT; m_cinto INT; m_resp INT;
    l_cap INT; l_luva INT; l_ocul INT; l_prot INT; l_cinto INT; l_resp INT;
    e_cap INT; e_luva INT; e_ocul INT; e_prot INT; e_cinto INT; e_resp INT;
    a_prensa INT; a_altura INT; a_eletr INT; a_cabine INT;
    t_altura INT; t_eletr INT; t_prensa INT; t_pint INT; t_inv INT;
    tr_integ INT; tr_cinto INT; tr_brig INT;
    emp INT;
    o_ac1 INT; o_ac2 INT; o_ac3 INT; o_in1 INT; o_in2 INT; o_in3 INT;
    p1 INT; p2 INT; p3 INT; p4 INT;
    seq_mes BIGINT;
BEGIN
    IF EXISTS (SELECT 1 FROM setores WHERE nome = 'Galpão de Produção') THEN
        RAISE NOTICE 'Dados de demonstração já inseridos; nada a fazer.';
        RETURN;
    END IF;

    SELECT id INTO v_gestor FROM usuarios WHERE email = gestor_email;
    SELECT id INTO v_sup FROM usuarios WHERE email = sup_email;
    IF v_gestor IS NULL OR v_sup IS NULL THEN
        RAISE EXCEPTION 'Contas de demonstração da V17 não encontradas.';
    END IF;

    -- ===================== Pessoas (RF23) =====================
    -- Segundo supervisor com a mesma senha de demonstração (Senha@123).
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, senha, perfil)
    SELECT '10433218100', 'Carla Mendes', DATE '1988-03-14', 'carla.mendes@safeplace.com', senha, 'SUPERVISOR'
    FROM usuarios WHERE id = v_sup RETURNING id INTO v_sup2;

    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('96001338914', 'Ana Souza', DATE '1992-05-10', 'ana.souza@email.com', 'COLABORADOR') RETURNING id INTO c_ana;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('08386379499', 'Bruno Lima', DATE '1985-11-22', NULL, 'COLABORADOR') RETURNING id INTO c_bruno;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('02654235114', 'Carlos Pereira', DATE '1979-02-03', 'carlos.pereira@email.com', 'COLABORADOR') RETURNING id INTO c_carlos;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('16155940789', 'Daniela Rocha', DATE '1995-08-30', NULL, 'COLABORADOR') RETURNING id INTO c_daniela;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('81618495950', 'Eduardo Alves', DATE '1990-12-12', 'eduardo.alves@email.com', 'COLABORADOR') RETURNING id INTO c_eduardo;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('31034131656', 'Fernanda Costa', DATE '1987-07-07', NULL, 'COLABORADOR') RETURNING id INTO c_fernanda;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('47525534144', 'Gabriel Martins', DATE '1998-01-25', 'gabriel.martins@email.com', 'COLABORADOR') RETURNING id INTO c_gabriel;
    INSERT INTO usuarios (cpf, nome, data_nascimento, email, perfil) VALUES
        ('92832764851', 'Helena Ribeiro', DATE '1983-04-18', NULL, 'COLABORADOR') RETURNING id INTO c_helena;

    INSERT INTO logs_auditoria (tipo_operacao, data_hora, responsavel, justificativa, entidade, entidade_id) VALUES
        ('CADASTRO', agora - INTERVAL '90 days', gestor_email, 'Cadastro de Supervisor', 'Supervisor', v_sup2),
        ('CADASTRO', agora - INTERVAL '88 days', gestor_email, 'Cadastro de Colaborador', 'Colaborador', c_ana),
        ('CADASTRO', agora - INTERVAL '88 days', gestor_email, 'Cadastro de Colaborador', 'Colaborador', c_bruno),
        ('CADASTRO', agora - INTERVAL '88 days', gestor_email, 'Cadastro de Colaborador', 'Colaborador', c_carlos),
        ('CADASTRO', agora - INTERVAL '87 days', sup_email, 'Cadastro de Colaborador', 'Colaborador', c_daniela),
        ('CADASTRO', agora - INTERVAL '87 days', sup_email, 'Cadastro de Colaborador', 'Colaborador', c_eduardo),
        ('CADASTRO', agora - INTERVAL '86 days', sup_email, 'Cadastro de Colaborador', 'Colaborador', c_fernanda),
        ('CADASTRO', agora - INTERVAL '86 days', gestor_email, 'Cadastro de Colaborador', 'Colaborador', c_gabriel),
        ('CADASTRO', agora - INTERVAL '85 days', gestor_email, 'Cadastro de Colaborador', 'Colaborador', c_helena);

    -- ===================== Setores e áreas de risco (RF05) =====================
    INSERT INTO setores (nome, descricao, localizacao, status) VALUES
        ('Galpão de Produção', 'Linhas de prensagem e montagem', 'Bloco A', 'ALTO') RETURNING id INTO s_prod;
    INSERT INTO setores (nome, descricao, localizacao, status) VALUES
        ('Manutenção Industrial', 'Oficina elétrica e mecânica, serviços em altura', 'Bloco B', 'CRITICO') RETURNING id INTO s_manut;
    INSERT INTO setores (nome, descricao, localizacao, status) VALUES
        ('Almoxarifado', 'Recebimento e guarda de materiais e EPIs', 'Bloco C', 'BAIXO') RETURNING id INTO s_almox;
    INSERT INTO setores (nome, descricao, localizacao, status) VALUES
        ('Cabine de Pintura', 'Pintura automotiva com solventes', 'Bloco A, anexo', 'MEDIO') RETURNING id INTO s_pint;

    -- ===================== EPIs: modelos (CA), lotes e saldos (RF03, RF21) =====================
    INSERT INTO modelos_epi (ca, marca, validade_ca) VALUES (31469, 'MSA', hoje + 700) RETURNING id INTO m_cap;
    INSERT INTO modelos_epi (ca, marca, validade_ca) VALUES (40150, 'Danny', hoje + 400) RETURNING id INTO m_luva;
    INSERT INTO modelos_epi (ca, marca, validade_ca) VALUES (11268, '3M', hoje + 20) RETURNING id INTO m_ocul;      -- CA vencendo
    INSERT INTO modelos_epi (ca, marca, validade_ca) VALUES (29176, '3M', hoje + 900) RETURNING id INTO m_prot;
    INSERT INTO modelos_epi (ca, marca, validade_ca) VALUES (35529, 'Mundial', hoje + 500) RETURNING id INTO m_cinto;
    INSERT INTO modelos_epi (ca, marca, validade_ca) VALUES (38503, '3M', hoje + 600) RETURNING id INTO m_resp;

    INSERT INTO lotes_epi (numero_lote, nota_fiscal, data_fabricacao, validade, quantidade_recebida, modelo_id)
        VALUES ('L-CAP-001', 'NF-10231', hoje - 200, hoje + 1600, 40, m_cap) RETURNING id INTO l_cap;
    INSERT INTO lotes_epi (numero_lote, nota_fiscal, data_fabricacao, validade, quantidade_recebida, modelo_id)
        VALUES ('L-LUV-014', 'NF-10232', hoje - 150, hoje + 700, 100, m_luva) RETURNING id INTO l_luva;
    INSERT INTO lotes_epi (numero_lote, nota_fiscal, data_fabricacao, validade, quantidade_recebida, modelo_id)
        VALUES ('L-OCU-007', 'NF-10233', hoje - 300, hoje + 800, 30, m_ocul) RETURNING id INTO l_ocul;
    INSERT INTO lotes_epi (numero_lote, nota_fiscal, data_fabricacao, validade, quantidade_recebida, modelo_id)
        VALUES ('L-PRO-002', 'NF-10234', hoje - 100, hoje + 1000, 50, m_prot) RETURNING id INTO l_prot;
    INSERT INTO lotes_epi (numero_lote, nota_fiscal, data_fabricacao, validade, quantidade_recebida, modelo_id)
        VALUES ('L-CIN-003', 'NF-10235', hoje - 250, hoje + 1200, 8, m_cinto) RETURNING id INTO l_cinto;
    INSERT INTO lotes_epi (numero_lote, nota_fiscal, data_fabricacao, validade, quantidade_recebida, modelo_id)
        VALUES ('L-RES-005', 'NF-10236', hoje - 120, hoje + 900, 12, m_resp) RETURNING id INTO l_resp;

    -- Saldos já descontam os empréstimos ativos e o descarte inseridos abaixo.
    -- localizacao = nome do setor: é como a projeção de substituição encontra o setor do EPI.
    INSERT INTO epis (nome, quantidade, estoque_minimo, status, vida_util_dias, especificacao_descricao, especificacao_classificacao, localizacao, lote_id)
        VALUES ('Capacete de segurança classe B', 38, 10, 'DISPONIVEL', 365, 'Capacete com jugular, aba frontal', 'PROTECAO_DE_CABECA', 'Galpão de Produção', l_cap) RETURNING id INTO e_cap;
    INSERT INTO epis (nome, quantidade, estoque_minimo, status, vida_util_dias, especificacao_descricao, especificacao_classificacao, localizacao, lote_id)
        VALUES ('Luva de vaqueta', 92, 20, 'DISPONIVEL', 90, 'Luva de couro para manuseio de chapas', 'PROTECAO_DE_MEMBROS_SUPERIORES', 'Galpão de Produção', l_luva) RETURNING id INTO e_luva;
    INSERT INTO epis (nome, quantidade, estoque_minimo, status, vida_util_dias, especificacao_descricao, especificacao_classificacao, localizacao, lote_id)
        VALUES ('Óculos de proteção incolor', 24, 5, 'DISPONIVEL', 180, 'Óculos antiembaçante', 'PROTECAO_DE_OLHOS', 'Cabine de Pintura', l_ocul) RETURNING id INTO e_ocul;
    INSERT INTO epis (nome, quantidade, estoque_minimo, status, vida_util_dias, especificacao_descricao, especificacao_classificacao, localizacao, lote_id)
        VALUES ('Protetor auricular tipo plug', 47, 15, 'DISPONIVEL', 60, 'Plug de silicone com cordão', 'PROTECAO_AUDITIVA', 'Galpão de Produção', l_prot) RETURNING id INTO e_prot;
    INSERT INTO epis (nome, quantidade, estoque_minimo, status, vida_util_dias, especificacao_descricao, especificacao_classificacao, localizacao, lote_id)
        VALUES ('Cinto paraquedista', 3, 4, 'DISPONIVEL', 730, 'Cinto com talabarte duplo', 'PROTECAO_CONTRA_QUEDAS', 'Manutenção Industrial', l_cinto) RETURNING id INTO e_cinto;  -- estoque crítico
    INSERT INTO epis (nome, quantidade, estoque_minimo, status, vida_util_dias, especificacao_descricao, especificacao_classificacao, localizacao, lote_id)
        VALUES ('Respirador semifacial com filtro', 0, 3, 'ESGOTADO', 120, 'Respirador com filtro para vapores orgânicos', 'PROTECAO_RESPIRATORIA', 'Cabine de Pintura', l_resp) RETURNING id INTO e_resp;  -- esgotado

    INSERT INTO movimentacoes_estoque (epi_id, tipo, quantidade, data_hora, motivo, lote_id, responsavel_id) VALUES
        (e_cap, 'ENTRADA', 40, agora - INTERVAL '120 days', 'Recebimento NF-10231', l_cap, v_gestor),
        (e_luva, 'ENTRADA', 100, agora - INTERVAL '120 days', 'Recebimento NF-10232', l_luva, v_gestor),
        (e_ocul, 'ENTRADA', 30, agora - INTERVAL '120 days', 'Recebimento NF-10233', l_ocul, v_gestor),
        (e_prot, 'ENTRADA', 50, agora - INTERVAL '110 days', 'Recebimento NF-10234', l_prot, v_gestor),
        (e_cinto, 'ENTRADA', 8, agora - INTERVAL '110 days', 'Recebimento NF-10235', l_cinto, v_gestor),
        (e_resp, 'ENTRADA', 12, agora - INTERVAL '100 days', 'Recebimento NF-10236', l_resp, v_gestor),
        (e_luva, 'SAIDA', 6, agora - INTERVAL '60 days', 'Distribuição para a linha de montagem', l_luva, v_gestor),
        (e_ocul, 'SAIDA', 4, agora - INTERVAL '45 days', 'Distribuição para a cabine de pintura', l_ocul, v_gestor),
        (e_prot, 'SAIDA', 2, agora - INTERVAL '30 days', 'Troca periódica', l_prot, v_gestor),
        (e_cinto, 'SAIDA', 4, agora - INTERVAL '50 days', 'Baixa por avaria detectada em inspeção', l_cinto, v_gestor),
        (e_resp, 'SAIDA', 12, agora - INTERVAL '15 days', 'Distribuição para pintura de lote especial', l_resp, v_gestor);

    INSERT INTO logs_auditoria (tipo_operacao, data_hora, responsavel, justificativa, entidade, entidade_id) VALUES
        ('CADASTRO', agora - INTERVAL '120 days', gestor_email, 'Cadastro de EPI: Capacete de segurança classe B', 'Epi', e_cap),
        ('CADASTRO', agora - INTERVAL '120 days', gestor_email, 'Cadastro de EPI: Luva de vaqueta', 'Epi', e_luva),
        ('CADASTRO', agora - INTERVAL '120 days', gestor_email, 'Cadastro de EPI: Óculos de proteção incolor', 'Epi', e_ocul),
        ('CADASTRO', agora - INTERVAL '110 days', gestor_email, 'Cadastro de EPI: Protetor auricular tipo plug', 'Epi', e_prot),
        ('CADASTRO', agora - INTERVAL '110 days', gestor_email, 'Cadastro de EPI: Cinto paraquedista', 'Epi', e_cinto),
        ('CADASTRO', agora - INTERVAL '100 days', gestor_email, 'Cadastro de EPI: Respirador semifacial com filtro', 'Epi', e_resp);

    -- Manutenções (RF04)
    INSERT INTO manutencoes_epi (epi_id, data_manutencao, tipo_manutencao, descricao, resultado, responsavel_manutencao, responsavel_id) VALUES
        (e_cinto, agora - INTERVAL '40 days', 'PREVENTIVA', 'Inspeção semestral de costuras e mosquetões', 'APROVADO', 'Gestor Demonstração', v_gestor),
        (e_cap, agora - INTERVAL '20 days', 'CORRETIVA', 'Substituição da carneira', 'APROVADO', 'Gestor Demonstração', v_gestor);

    -- ===================== Áreas de risco (RF05, UC03) =====================
    INSERT INTO areas_risco (codigo, nome, descricao, nivel_perigo, setor_id, agentes_risco, limites)
        VALUES ('AR-PRENSA-01', 'Prensas hidráulicas', 'Linha de prensagem de chapas', 'ALTO', s_prod,
                'Ruído contínuo,Esmagamento,Projeção de partículas', 'Entre as colunas 1 e 6 da linha A') RETURNING id INTO a_prensa;
    INSERT INTO areas_risco (codigo, nome, descricao, nivel_perigo, setor_id, agentes_risco, limites)
        VALUES ('AR-ALTURA-01', 'Plataforma de manutenção elevada', 'Passarela de acesso às pontes rolantes', 'CRITICO', s_manut,
                'Queda de altura,Queda de objetos', 'Passarela a 6 m de altura, acesso pela escada B') RETURNING id INTO a_altura;
    INSERT INTO areas_risco (codigo, nome, descricao, nivel_perigo, setor_id, agentes_risco, limites)
        VALUES ('AR-ELETR-01', 'Subestação e painéis elétricos', 'Quadros de média e baixa tensão', 'CRITICO', s_manut,
                'Choque elétrico,Arco elétrico', 'Sala da subestação, porta com acesso controlado') RETURNING id INTO a_eletr;
    INSERT INTO areas_risco (codigo, nome, descricao, nivel_perigo, setor_id, agentes_risco, limites)
        VALUES ('AR-PINT-01', 'Cabine de pintura', 'Pintura com pistola e secagem', 'MEDIO', s_pint,
                'Vapores orgânicos,Inflamáveis', 'Interior da cabine e área de secagem') RETURNING id INTO a_cabine;

    INSERT INTO areas_risco_epis (area_risco_id, epi_id) VALUES
        (a_prensa, e_cap), (a_prensa, e_luva), (a_prensa, e_prot), (a_prensa, e_ocul),
        (a_altura, e_cap), (a_altura, e_cinto),
        (a_eletr, e_cap), (a_eletr, e_ocul),
        (a_cabine, e_resp), (a_cabine, e_ocul);

    INSERT INTO logs_auditoria (tipo_operacao, data_hora, responsavel, justificativa, entidade, entidade_id) VALUES
        ('CADASTRO', agora - INTERVAL '115 days', gestor_email, 'Cadastro do setor Galpão de Produção', 'Setor', s_prod),
        ('CADASTRO', agora - INTERVAL '115 days', gestor_email, 'Cadastro do setor Manutenção Industrial', 'Setor', s_manut),
        ('CADASTRO', agora - INTERVAL '115 days', gestor_email, 'Cadastro do setor Almoxarifado', 'Setor', s_almox),
        ('CADASTRO', agora - INTERVAL '115 days', gestor_email, 'Cadastro do setor Cabine de Pintura', 'Setor', s_pint),
        ('CADASTRO', agora - INTERVAL '100 days', gestor_email, 'Cadastro da área de risco AR-PRENSA-01 - Prensas hidráulicas', 'AreaRisco', a_prensa),
        ('CADASTRO', agora - INTERVAL '100 days', gestor_email, 'Cadastro da área de risco AR-ALTURA-01 - Plataforma de manutenção elevada', 'AreaRisco', a_altura),
        ('CADASTRO', agora - INTERVAL '100 days', gestor_email, 'Cadastro da área de risco AR-ELETR-01 - Subestação e painéis elétricos', 'AreaRisco', a_eletr),
        ('CADASTRO', agora - INTERVAL '100 days', gestor_email, 'Cadastro da área de risco AR-PINT-01 - Cabine de pintura', 'AreaRisco', a_cabine),
        ('ALTERACAO', agora - INTERVAL '30 days', gestor_email,
         'nível de perigo: de MEDIO para ALTO; limites: de "Entre as colunas 1 e 4 da linha A" para "Entre as colunas 1 e 6 da linha A". Justificativa: ampliação da linha com duas novas prensas.',
         'AreaRisco', a_prensa);

    -- ===================== Tarefas (RF06, UC11) =====================
    INSERT INTO tarefas (descricao, nivel_perigo, data_classificacao, responsavel_classificacao_id, area_risco_id)
        VALUES ('Manutenção de ponte rolante em altura', 'CRITICO', agora - INTERVAL '95 days', v_gestor, a_altura) RETURNING id INTO t_altura;
    INSERT INTO tarefas (descricao, nivel_perigo, data_classificacao, responsavel_classificacao_id, area_risco_id)
        VALUES ('Manobra em painel elétrico de baixa tensão', 'ALTO', agora - INTERVAL '95 days', v_gestor, a_eletr) RETURNING id INTO t_eletr;
    INSERT INTO tarefas (descricao, nivel_perigo, data_classificacao, responsavel_classificacao_id, area_risco_id)
        VALUES ('Operação de prensa hidráulica', 'ALTO', agora - INTERVAL '30 days', v_gestor, a_prensa) RETURNING id INTO t_prensa;
    INSERT INTO tarefas (descricao, nivel_perigo, data_classificacao, responsavel_classificacao_id, area_risco_id)
        VALUES ('Pintura de peças com pistola', 'MEDIO', agora - INTERVAL '90 days', v_gestor, a_cabine) RETURNING id INTO t_pint;
    INSERT INTO tarefas (descricao, area_risco_id)
        VALUES ('Inventário mensal do almoxarifado', NULL) RETURNING id INTO t_inv;   -- sem classificação

    INSERT INTO tarefas_setores (tarefa_id, setor_id) VALUES
        (t_altura, s_manut), (t_eletr, s_manut), (t_prensa, s_prod), (t_pint, s_pint), (t_inv, s_almox);

    INSERT INTO logs_auditoria (tipo_operacao, data_hora, responsavel, justificativa, entidade, entidade_id) VALUES
        ('CADASTRO', agora - INTERVAL '96 days', gestor_email, 'Cadastro da tarefa Manutenção de ponte rolante em altura', 'Tarefa', t_altura),
        ('ALTERACAO', agora - INTERVAL '95 days', gestor_email, 'Classificação de periculosidade da tarefa: de sem classificação para CRITICO; responsável técnico: Gestor Demonstração (id ' || v_gestor || ')', 'Tarefa', t_altura),
        ('CADASTRO', agora - INTERVAL '96 days', gestor_email, 'Cadastro da tarefa Manobra em painel elétrico de baixa tensão', 'Tarefa', t_eletr),
        ('ALTERACAO', agora - INTERVAL '95 days', gestor_email, 'Classificação de periculosidade da tarefa: de sem classificação para ALTO; responsável técnico: Gestor Demonstração (id ' || v_gestor || ')', 'Tarefa', t_eletr),
        ('CADASTRO', agora - INTERVAL '96 days', gestor_email, 'Cadastro da tarefa Operação de prensa hidráulica', 'Tarefa', t_prensa),
        ('ALTERACAO', agora - INTERVAL '95 days', gestor_email, 'Classificação de periculosidade da tarefa: de sem classificação para MEDIO; responsável técnico: Gestor Demonstração (id ' || v_gestor || ')', 'Tarefa', t_prensa),
        ('ALTERACAO', agora - INTERVAL '30 days', gestor_email, 'Classificação de periculosidade da tarefa: de MEDIO para ALTO; responsável técnico: Gestor Demonstração (id ' || v_gestor || ')', 'Tarefa', t_prensa),
        ('CADASTRO', agora - INTERVAL '91 days', gestor_email, 'Cadastro da tarefa Pintura de peças com pistola', 'Tarefa', t_pint),
        ('ALTERACAO', agora - INTERVAL '90 days', gestor_email, 'Classificação de periculosidade da tarefa: de sem classificação para MEDIO; responsável técnico: Gestor Demonstração (id ' || v_gestor || ')', 'Tarefa', t_pint),
        ('CADASTRO', agora - INTERVAL '10 days', gestor_email, 'Cadastro da tarefa Inventário mensal do almoxarifado', 'Tarefa', t_inv);

    -- ===================== Capacitações (RF10, UC02) =====================
    INSERT INTO certificacoes (nome, data_emissao, data_vencimento, colaborador_id) VALUES
        ('NR-35', hoje - 300, hoje + 430, c_ana),        -- em dia
        ('NR-35', hoje - 700, hoje + 15, c_bruno),       -- próxima do vencimento
        ('NR-35', hoje - 760, hoje - 10, c_carlos),      -- vencida
        ('NR-10', hoje - 200, hoje + 530, c_carlos),     -- em dia
        ('NR-10', hoje - 400, hoje + 25, c_eduardo),     -- próxima do vencimento
        ('NR-12', hoje - 100, hoje + 630, c_daniela),
        ('NR-12', hoje - 500, hoje + 230, c_fernanda),
        ('NR-33', hoje - 380, hoje - 5, c_gabriel);       -- vencida
    -- Helena fica sem certificação: aparece como pendente de integração admissional.

    INSERT INTO treinamentos (nome, conteudo, instrutor, data_realizacao, carga_horaria, validade_meses)
        VALUES ('Integração de segurança', 'Normas internas, rotas de fuga, uso de EPIs', 'Marcos Tavares', hoje - 80, 8, 12) RETURNING id INTO tr_integ;
    INSERT INTO treinamentos (nome, conteudo, instrutor, data_realizacao, carga_horaria, validade_meses)
        VALUES ('Uso de cinto paraquedista', 'Inspeção, ajuste e ancoragem do cinto', 'Paula Nogueira', hoje - 120, 4, 12) RETURNING id INTO tr_cinto;
    INSERT INTO treinamentos (nome, conteudo, instrutor, data_realizacao, carga_horaria, validade_meses)
        VALUES ('Uso de cinto paraquedista', 'Inspeção, ajuste e ancoragem do cinto', 'Paula Nogueira', hoje - 400, 4, 12) RETURNING id INTO emp;  -- turma antiga, vencida
    INSERT INTO treinamentos_colaboradores (treinamento_id, colaborador_id) VALUES (emp, c_carlos);
    INSERT INTO treinamentos (nome, conteudo, instrutor, data_realizacao, carga_horaria, validade_meses)
        VALUES ('Brigada de incêndio', 'Combate a princípio de incêndio e primeiros socorros', 'Corpo de Bombeiros', hoje - 60, 16, NULL) RETURNING id INTO tr_brig;

    INSERT INTO treinamentos_colaboradores (treinamento_id, colaborador_id) VALUES
        (tr_integ, c_ana), (tr_integ, c_bruno), (tr_integ, c_carlos), (tr_integ, c_daniela),
        (tr_integ, c_eduardo), (tr_integ, c_fernanda), (tr_integ, c_gabriel),
        (tr_cinto, c_ana), (tr_cinto, c_bruno),
        (tr_brig, c_daniela), (tr_brig, c_fernanda);

    INSERT INTO exigencias_capacitacao (nome_capacitacao, tipo_capacitacao, alvo_id, tipo_alvo) VALUES
        ('NR-35', 'CERTIFICACAO', t_altura, 'TAREFA'),
        ('Uso de cinto paraquedista', 'TREINAMENTO', t_altura, 'TAREFA'),
        ('NR-10', 'CERTIFICACAO', t_eletr, 'TAREFA'),
        ('NR-12', 'CERTIFICACAO', t_prensa, 'TAREFA'),
        ('Uso de cinto paraquedista', 'TREINAMENTO', e_cinto, 'EPI');

    -- Alocações válidas (colaboradores aptos na data da alocação)
    INSERT INTO tarefas_alocacoes (tarefa_id, colaborador_id, data_alocacao, responsavel_alocacao_id, observacao) VALUES
        (t_altura, c_ana, agora - INTERVAL '20 days', v_gestor, 'Revisão das pontes rolantes do galpão B'),
        (t_prensa, c_daniela, agora - INTERVAL '12 days', v_gestor, 'Turno da manhã'),
        (t_eletr, c_carlos, agora - INTERVAL '8 days', v_gestor, 'Troca de disjuntores do painel 3');

    -- ===================== Empréstimos (RF11, UC12) =====================
    -- Devolvidos (histórico de uso, base da projeção de substituição)
    INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista,
                             data_devolucao_real, responsavel_devolucao_id, estado_devolucao, status, termo_cautela) VALUES
        (e_cap, c_daniela, v_sup, agora - INTERVAL '100 days', hoje - 70, agora - INTERVAL '65 days', v_sup, 'APTO', 'DISPONIVEL',
         'Termo de cautela: o colaborador Daniela Rocha recebeu 1 unidade do EPI Capacete de segurança classe B (CA 31469), entregue por Supervisor Demonstração.'),
        (e_luva, c_eduardo, v_sup, agora - INTERVAL '90 days', hoje - 60, agora - INTERVAL '58 days', v_sup, 'APTO', 'DISPONIVEL',
         'Termo de cautela: o colaborador Eduardo Alves recebeu 1 unidade do EPI Luva de vaqueta (CA 40150), entregue por Supervisor Demonstração.'),
        (e_prot, c_fernanda, v_sup2, agora - INTERVAL '80 days', hoje - 50, agora - INTERVAL '45 days', v_sup2, 'APTO', 'DISPONIVEL',
         'Termo de cautela: o colaborador Fernanda Costa recebeu 1 unidade do EPI Protetor auricular tipo plug (CA 29176), entregue por Carla Mendes.'),
        (e_ocul, c_gabriel, v_sup, agora - INTERVAL '70 days', hoje - 40, agora - INTERVAL '38 days', v_sup, 'MANUTENCAO', 'DISPONIVEL',
         'Termo de cautela: o colaborador Gabriel Martins recebeu 1 unidade do EPI Óculos de proteção incolor (CA 11268), entregue por Supervisor Demonstração.');
    -- Ativos dentro do prazo
    INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) VALUES
        (e_cap, c_ana, v_sup, agora - INTERVAL '25 days', hoje + 35, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Ana Souza recebeu 1 unidade do EPI Capacete de segurança classe B (CA 31469), entregue por Supervisor Demonstração.'),
        (e_cinto, c_ana, v_gestor, agora - INTERVAL '20 days', hoje + 40, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Ana Souza recebeu 1 unidade do EPI Cinto paraquedista (CA 35529), entregue por Gestor Demonstração.'),
        (e_cap, c_daniela, v_sup2, agora - INTERVAL '12 days', hoje + 48, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Daniela Rocha recebeu 1 unidade do EPI Capacete de segurança classe B (CA 31469), entregue por Carla Mendes.'),
        (e_luva, c_daniela, v_sup2, agora - INTERVAL '12 days', hoje + 18, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Daniela Rocha recebeu 1 unidade do EPI Luva de vaqueta (CA 40150), entregue por Carla Mendes.'),
        (e_ocul, c_carlos, v_sup, agora - INTERVAL '8 days', hoje + 22, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Carlos Pereira recebeu 1 unidade do EPI Óculos de proteção incolor (CA 11268), entregue por Supervisor Demonstração.');
    -- Ativos em atraso (data prevista já passou)
    INSERT INTO emprestimos (epi_id, colaborador_id, responsavel_entrega_id, data_retirada, data_devolucao_prevista, status, termo_cautela) VALUES
        (e_luva, c_bruno, v_sup, agora - INTERVAL '45 days', hoje - 5, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Bruno Lima recebeu 1 unidade do EPI Luva de vaqueta (CA 40150), entregue por Supervisor Demonstração.'),
        (e_prot, c_helena, v_sup, agora - INTERVAL '40 days', hoje - 10, 'INDISPONIVEL',
         'Termo de cautela: o colaborador Helena Ribeiro recebeu 1 unidade do EPI Protetor auricular tipo plug (CA 29176), entregue por Supervisor Demonstração.');

    -- Movimentações correspondentes aos empréstimos (saldos acima já refletem estes movimentos)
    INSERT INTO movimentacoes_estoque (epi_id, tipo, quantidade, data_hora, motivo, lote_id, responsavel_id) VALUES
        (e_cap, 'SAIDA', 1, agora - INTERVAL '100 days', 'Empréstimo ao colaborador Daniela Rocha', l_cap, v_sup),
        (e_cap, 'ENTRADA', 1, agora - INTERVAL '65 days', 'Devolução de empréstimo (item apto)', l_cap, v_sup),
        (e_luva, 'SAIDA', 1, agora - INTERVAL '90 days', 'Empréstimo ao colaborador Eduardo Alves', l_luva, v_sup),
        (e_luva, 'ENTRADA', 1, agora - INTERVAL '58 days', 'Devolução de empréstimo (item apto)', l_luva, v_sup),
        (e_prot, 'SAIDA', 1, agora - INTERVAL '80 days', 'Empréstimo ao colaborador Fernanda Costa', l_prot, v_sup2),
        (e_prot, 'ENTRADA', 1, agora - INTERVAL '45 days', 'Devolução de empréstimo (item apto)', l_prot, v_sup2),
        (e_ocul, 'SAIDA', 1, agora - INTERVAL '70 days', 'Empréstimo ao colaborador Gabriel Martins', l_ocul, v_sup),
        (e_cap, 'SAIDA', 1, agora - INTERVAL '25 days', 'Empréstimo ao colaborador Ana Souza', l_cap, v_sup),
        (e_cinto, 'SAIDA', 1, agora - INTERVAL '20 days', 'Empréstimo ao colaborador Ana Souza', l_cinto, v_gestor),
        (e_cap, 'SAIDA', 1, agora - INTERVAL '12 days', 'Empréstimo ao colaborador Daniela Rocha', l_cap, v_sup2),
        (e_luva, 'SAIDA', 1, agora - INTERVAL '12 days', 'Empréstimo ao colaborador Daniela Rocha', l_luva, v_sup2),
        (e_ocul, 'SAIDA', 1, agora - INTERVAL '8 days', 'Empréstimo ao colaborador Carlos Pereira', l_ocul, v_sup),
        (e_luva, 'SAIDA', 1, agora - INTERVAL '45 days', 'Empréstimo ao colaborador Bruno Lima', l_luva, v_sup),
        (e_prot, 'SAIDA', 1, agora - INTERVAL '40 days', 'Empréstimo ao colaborador Helena Ribeiro', l_prot, v_sup);
    INSERT INTO manutencoes_epi (epi_id, data_manutencao, tipo_manutencao, descricao, resultado, responsavel_manutencao, responsavel_id) VALUES
        (e_ocul, agora - INTERVAL '38 days', 'CORRETIVA', 'Devolução de empréstimo encaminhada para manutenção: lente riscada', 'REPROVADO', 'Supervisor Demonstração', v_sup);

    -- ===================== Ocorrências (RF01, RF02, RF13, RF16) =====================
    INSERT INTO sequencias_protocolo_cat (ano, mes, ultimo_sequencial)
    VALUES (EXTRACT(YEAR FROM hoje)::SMALLINT, EXTRACT(MONTH FROM hoje)::SMALLINT, 0)
    ON CONFLICT (ano, mes) DO NOTHING;

    -- Acidente 1: fator humano, gravidade ALTA, registrado pelo Supervisor, em triagem
    INSERT INTO ocorrencias (data_ocorrencia, descricao, data_registro, status_ocorrencia, area_risco_id, colaborador_id, relator_id)
        VALUES (agora - INTERVAL '6 days', 'Colaborador operou a prensa sem a proteção auricular e sofreu corte no antebraço ao retirar chapa sem luva.',
                agora - INTERVAL '6 days' + INTERVAL '2 hours', 'EM_TRIAGEM', a_prensa, c_daniela, v_sup) RETURNING id_ocorrencia INTO o_ac1;
    UPDATE sequencias_protocolo_cat SET ultimo_sequencial = ultimo_sequencial + 1
        WHERE ano = EXTRACT(YEAR FROM hoje) AND mes = EXTRACT(MONTH FROM hoje) RETURNING ultimo_sequencial INTO seq_mes;
    INSERT INTO acidentes (id_ocorrencia, causa_raiz, tipo, dano, numero_protocolo, destino, gravidade)
        VALUES (o_ac1, 'FATOR_HUMANO', 'Típico', 'Corte no antebraço esquerdo', format('CAT-%s-%s-%s', to_char(hoje,'YYYY'), to_char(hoje,'MM'), lpad(seq_mes::text, 4, '0')), NULL, 'ALTO');

    -- Acidente 2: falha de EPI, gravidade CRÍTICA, registrado pelo Gestor, aberto
    INSERT INTO ocorrencias (data_ocorrencia, descricao, data_registro, status_ocorrencia, area_risco_id, colaborador_id, relator_id)
        VALUES (agora - INTERVAL '8 days', 'Talabarte do cinto paraquedista rompeu durante deslocamento na passarela; queda contida pela linha de vida.',
                agora - INTERVAL '8 days' + INTERVAL '1 hour', 'ABERTA', a_altura, c_ana, v_gestor) RETURNING id_ocorrencia INTO o_ac2;
    UPDATE sequencias_protocolo_cat SET ultimo_sequencial = ultimo_sequencial + 1
        WHERE ano = EXTRACT(YEAR FROM hoje) AND mes = EXTRACT(MONTH FROM hoje) RETURNING ultimo_sequencial INTO seq_mes;
    INSERT INTO acidentes (id_ocorrencia, causa_raiz, tipo, dano, numero_protocolo, destino, gravidade)
        VALUES (o_ac2, 'FALHA_EPI', 'Típico', 'Escoriações leves no ombro', format('CAT-%s-%s-%s', to_char(hoje,'YYYY'), to_char(hoje,'MM'), lpad(seq_mes::text, 4, '0')), NULL, 'CRITICO');

    -- Acidente 3: outra causa, gravidade MÉDIA, mês anterior, arquivado
    INSERT INTO sequencias_protocolo_cat (ano, mes, ultimo_sequencial)
    VALUES (EXTRACT(YEAR FROM hoje - 40)::SMALLINT, EXTRACT(MONTH FROM hoje - 40)::SMALLINT, 1)
    ON CONFLICT (ano, mes) DO UPDATE SET ultimo_sequencial = sequencias_protocolo_cat.ultimo_sequencial + 1
    RETURNING ultimo_sequencial INTO seq_mes;
    INSERT INTO ocorrencias (data_ocorrencia, descricao, data_registro, status_ocorrencia, area_risco_id, colaborador_id, relator_id)
        VALUES (agora - INTERVAL '40 days', 'Escorregão no piso molhado da cabine de pintura, com torção no tornozelo.',
                agora - INTERVAL '40 days' + INTERVAL '3 hours', 'ARQUIVADA', a_cabine, c_gabriel, v_sup) RETURNING id_ocorrencia INTO o_ac3;
    INSERT INTO acidentes (id_ocorrencia, causa_raiz, tipo, dano, numero_protocolo, destino, gravidade)
        VALUES (o_ac3, 'OUTRO', 'Típico', 'Torção no tornozelo direito', format('CAT-%s-%s-%s', to_char(hoje - 40,'YYYY'), to_char(hoje - 40,'MM'), lpad(seq_mes::text, 4, '0')), NULL, 'MEDIO');

    -- Incidentes (sem protocolo)
    INSERT INTO ocorrencias (data_ocorrencia, descricao, data_registro, status_ocorrencia, area_risco_id, colaborador_id, relator_id)
        VALUES (agora - INTERVAL '1 day', 'Painel elétrico encontrado com a porta aberta e sem sinalização durante a ronda.',
                agora - INTERVAL '1 day' + INTERVAL '30 minutes', 'ABERTA', a_eletr, c_carlos, v_sup) RETURNING id_ocorrencia INTO o_in1;
    INSERT INTO incidentes (id_ocorrencia, situacao_risco, potencial_dano)
        VALUES (o_in1, 'Exposição a partes energizadas', 'Choque elétrico grave');
    INSERT INTO ocorrencias (data_ocorrencia, descricao, data_registro, status_ocorrencia, area_risco_id, colaborador_id, relator_id)
        VALUES (agora - INTERVAL '10 days', 'Chave inglesa caiu da passarela e atingiu o piso a 1 m de um colaborador.',
                agora - INTERVAL '10 days' + INTERVAL '1 hour', 'EM_TRIAGEM', a_altura, c_bruno, v_gestor) RETURNING id_ocorrencia INTO o_in2;
    INSERT INTO incidentes (id_ocorrencia, situacao_risco, potencial_dano)
        VALUES (o_in2, 'Queda de ferramenta sem amarração', 'Traumatismo craniano');
    INSERT INTO ocorrencias (data_ocorrencia, descricao, data_registro, status_ocorrencia, area_risco_id, colaborador_id, relator_id)
        VALUES (agora - INTERVAL '25 days', 'Vazamento de solvente próximo à área de secagem, contido com material absorvente.',
                agora - INTERVAL '25 days' + INTERVAL '2 hours', 'ARQUIVADA', a_cabine, c_fernanda, v_sup) RETURNING id_ocorrencia INTO o_in3;
    INSERT INTO incidentes (id_ocorrencia, situacao_risco, potencial_dano)
        VALUES (o_in3, 'Acúmulo de vapores inflamáveis', 'Incêndio na cabine');

    -- Auditoria dos relatos: é por ela que o Supervisor enxerga os próprios relatos (UC09).
    INSERT INTO logs_auditoria (tipo_operacao, data_hora, responsavel, justificativa, entidade, entidade_id) VALUES
        ('CADASTRO', agora - INTERVAL '6 days' + INTERVAL '2 hours', sup_email, 'Registro de Acidente de Trabalho', 'Acidente', o_ac1),
        ('ALTERACAO', agora - INTERVAL '5 days', gestor_email, 'Envio de ocorrência para triagem', 'Acidente', o_ac1),
        ('CADASTRO', agora - INTERVAL '8 days' + INTERVAL '1 hour', gestor_email, 'Registro de Acidente de Trabalho', 'Acidente', o_ac2),
        ('CADASTRO', agora - INTERVAL '40 days' + INTERVAL '3 hours', sup_email, 'Registro de Acidente de Trabalho', 'Acidente', o_ac3),
        ('ALTERACAO', agora - INTERVAL '38 days', gestor_email, 'Envio de ocorrência para triagem', 'Acidente', o_ac3),
        ('ALTERACAO', agora - INTERVAL '20 days', gestor_email, 'Arquivamento de ocorrência', 'Acidente', o_ac3),
        ('CADASTRO', agora - INTERVAL '1 day' + INTERVAL '30 minutes', sup_email, 'Registro de Incidente', 'Incidente', o_in1),
        ('CADASTRO', agora - INTERVAL '10 days' + INTERVAL '1 hour', gestor_email, 'Registro de Incidente', 'Incidente', o_in2),
        ('ALTERACAO', agora - INTERVAL '9 days', gestor_email, 'Envio de ocorrência para triagem', 'Incidente', o_in2),
        ('CADASTRO', agora - INTERVAL '25 days' + INTERVAL '2 hours', sup_email, 'Registro de Incidente', 'Incidente', o_in3),
        ('ALTERACAO', agora - INTERVAL '24 days', gestor_email, 'Envio de ocorrência para triagem', 'Incidente', o_in3),
        ('ALTERACAO', agora - INTERVAL '15 days', gestor_email, 'Arquivamento de ocorrência', 'Incidente', o_in3);

    -- Notificação ao Gestor de cada relato (UC09, passo 12), como o sistema grava no registro (issue #195).
    INSERT INTO notificacoes_ocorrencia (ocorrencia_id, tipo_ocorrencia, numero_protocolo_cat, area_nome, mensagem, criada_em)
    SELECT o.id_ocorrencia,
           CASE WHEN ac.id_ocorrencia IS NULL THEN 'INCIDENTE' ELSE 'ACIDENTE' END,
           ac.numero_protocolo,
           ar.nome,
           CASE WHEN ac.id_ocorrencia IS NULL
                THEN format('Novo incidente registrado na área %s. Aguarda triagem.', ar.nome)
                ELSE format('Novo acidente registrado (protocolo %s) na área %s. Aguarda triagem.', ac.numero_protocolo, ar.nome)
           END,
           o.data_registro
    FROM ocorrencias o
    JOIN areas_risco ar ON ar.id = o.area_risco_id
    LEFT JOIN acidentes ac ON ac.id_ocorrencia = o.id_ocorrencia
    WHERE o.id_ocorrencia IN (o_ac1, o_ac2, o_ac3, o_in1, o_in2, o_in3)
    ORDER BY o.data_registro;

    -- ===================== Planos de ação (RF07, UC07) =====================
    -- Acidente 1 (grave): preventiva com setor responsável e alerta, corretiva concluída
    INSERT INTO planos_de_acao (medidas_corretivas, medidas_preventivas, prazo, status, ocorrencia_id, colaborador_id, setor_id, gestor_id, alerta_ativo, antecedencia_alerta_dias)
        VALUES (NULL, 'Reforçar treinamento de NR-12 e instalar sensor de presença na prensa 4', hoje + 3, 'ABERTA', o_ac1, NULL, s_prod, v_gestor, TRUE, 5) RETURNING id INTO p1;
    INSERT INTO planos_de_acao (medidas_corretivas, medidas_preventivas, prazo, status, ocorrencia_id, colaborador_id, setor_id, gestor_id, alerta_ativo, antecedencia_alerta_dias)
        VALUES ('Afastar a prensa 4 até a revisão da proteção fixa', NULL, hoje - 2, 'CONCLUIDA', o_ac1, c_daniela, NULL, v_gestor, FALSE, NULL) RETURNING id INTO p2;
    -- Acidente 2 (crítico): preventiva em atraso, já prorrogada uma vez
    INSERT INTO planos_de_acao (medidas_corretivas, medidas_preventivas, prazo, status, ocorrencia_id, colaborador_id, setor_id, gestor_id, alerta_ativo, antecedencia_alerta_dias)
        VALUES ('Recolher e inspecionar todos os cintos do lote L-CIN-003', 'Implantar inspeção mensal documentada dos cintos', hoje - 1, 'ABERTA', o_ac2, NULL, s_manut, v_gestor, TRUE, 2) RETURNING id INTO p3;
    INSERT INTO prorrogacoes_prazo (plano_de_acao_id, prazo_anterior, novo_prazo, justificativa, criado_em)
        VALUES (p3, hoje - 3, hoje - 1, 'Fornecedor atrasou a entrega do kit de inspeção.', agora - INTERVAL '4 days');
    -- Incidente 2: ação com colaborador responsável
    INSERT INTO planos_de_acao (medidas_corretivas, medidas_preventivas, prazo, status, ocorrencia_id, colaborador_id, setor_id, gestor_id, alerta_ativo, antecedencia_alerta_dias)
        VALUES (NULL, 'Obrigar amarração de ferramentas em trabalhos na passarela', hoje + 15, 'ABERTA', o_in2, c_bruno, NULL, v_gestor, TRUE, 3) RETURNING id INTO p4;

    INSERT INTO logs_auditoria (tipo_operacao, data_hora, responsavel, justificativa, entidade, entidade_id) VALUES
        ('CADASTRO', agora - INTERVAL '5 days', gestor_email, 'Cadastro de ação do plano de ação', 'PlanoDeAcao', p1),
        ('CADASTRO', agora - INTERVAL '5 days', gestor_email, 'Cadastro de ação do plano de ação', 'PlanoDeAcao', p2),
        ('ALTERACAO', agora - INTERVAL '2 days', gestor_email, 'Ação concluída: ABERTA para CONCLUIDA', 'PlanoDeAcao', p2),
        ('CADASTRO', agora - INTERVAL '7 days', gestor_email, 'Cadastro de ação do plano de ação', 'PlanoDeAcao', p3),
        ('ALTERACAO', agora - INTERVAL '4 days', gestor_email, 'Prorrogação de prazo. Justificativa: Fornecedor atrasou a entrega do kit de inspeção.', 'PlanoDeAcao', p3),
        ('CADASTRO', agora - INTERVAL '9 days', gestor_email, 'Cadastro de ação do plano de ação', 'PlanoDeAcao', p4);

    RAISE NOTICE 'Dados de demonstração inseridos com sucesso.';
END $$;
