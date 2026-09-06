# Histórias de usuário do SafePlace

As histórias cobrem os requisitos RF01 a RF23. A indicação de MVP ou backlog segue a [Especificação do MVP](mvp/especificacao-mvp-arquitetura.md) e a [Priorização MoSCoW](requisitos/priorizacao-moscow.md). Backlog identifica funcionalidades previstas para depois da primeira entrega.

O Colaborador não acessa o sistema, não recebe conta ou senha e tem seus relatos registrados pelo Supervisor. Essa definição segue RF16, RF23, RNF03 e UC09, conforme a decisão registrada na [issue #81](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/81).

Os dicionários de dados são propostas de detalhamento. Campos, formatos e regras marcados como "A confirmar" continuam pendentes de decisão; sua presença neste documento não os torna requisitos aprovados. As divergências com o diagrama de classes estão registradas nas [pendências da revisão documental](mvp/especificacao-mvp-arquitetura.md#11-pendências-da-revisão-documental).

# US01 – Acompanhar acidentes relacionados a fatores humanos

Referências: [RF01](requisitos/requisitos-funcionais.md#rf01-o-sistema-deve-gerenciar-os-acidentes-causados-por-um-funcionário) e [UC05](casos-de-uso/casos-de-uso.md#uc05--gerenciar-acidentes-e-incidentes). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** consultar e atualizar os registros de acidentes relacionados a fatores humanos,
> **para** acompanhar as circunstâncias dessas ocorrências e orientar ações de prevenção.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Acidente | Acidente cadastrado. | Identificador de ocorrência | Obrigatório | Registro acompanhado pelo Gestor. |
> | Colaborador envolvido | Colaborador cadastrado. | Identificador de colaborador | Obrigatório | Pessoa relacionada ao fator humano registrado. |
> | Fator humano | Tamanho máximo a confirmar. | Texto | Obrigatório | Descrição do comportamento relacionado ao acidente. |
> | Uso do EPI | Tamanho máximo a confirmar. | Texto | Condicional | Informação sobre uso incorreto ou ausência do EPI. |
> | Data da ocorrência | Formato a confirmar. | Data | Automático | Data em que o acidente aconteceu. |
> | Setor | O setor da ocorrência. | Identificador de setor | Automático | Setor onde o acidente ocorreu. |

## Regra(s) de Negócio:

> 1. O recorte de RF01 considera acidentes relacionados à ação individual do colaborador, sem interferência de outros fatores, como uso incorreto ou ausência de EPI.
> 2. O colaborador associado deve estar previamente cadastrado.
> 3. O Gestor pode consultar e atualizar a ocorrência. O registro inicial pelo Supervisor está descrito na US13.
> 4. Ocorrências não podem ser excluídas permanentemente, apenas arquivadas. Alterações e arquivamentos devem registrar usuário, data, hora e dados alterados.

## Critério(s) de Aceite:

> 1. Ao consultar um acidente dessa categoria, o sistema deve exibir o colaborador, os fatos registrados e as informações sobre uso de EPI, quando houver.
> 2. Ao atualizar a descrição do fator humano e salvar, a alteração deve aparecer na consulta seguinte e no histórico de alterações.
> 3. Ao informar um colaborador inexistente, o sistema deve impedir a associação e informar a necessidade de cadastro prévio.
> 4. Ao arquivar a ocorrência, o sistema deve preservar seus dados e seu histórico. Uma tentativa de exclusão permanente deve ser bloqueada.

# US02 – Acompanhar acidentes relacionados a falhas de EPI

Referências: [RF02](requisitos/requisitos-funcionais.md#rf02-o-sistema-deve-gerenciar-os-acidentes-causados-por-uma-falha-de-equipamento-de-segurança) e [UC05](casos-de-uso/casos-de-uso.md#uc05--gerenciar-acidentes-e-incidentes). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** consultar e atualizar acidentes relacionados a falhas de equipamentos de proteção,
> **para** identificar os EPIs envolvidos e acompanhar as falhas registradas.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Acidente | Acidente cadastrado. | Identificador de ocorrência | Obrigatório | Ocorrência relacionada à falha. |
> | EPIs envolvidos | Os equipamentos relacionados à falha. | Lista de identificadores de EPI | Obrigatório | Equipamentos envolvidos no acidente. |
> | Descrição da falha | Tamanho máximo a confirmar. | Texto | Obrigatório | Falha observada no equipamento de proteção. |
> | Estado do EPI na ocorrência | Tamanho máximo a confirmar. | Texto | Condicional | Condição do equipamento quando ocorreu a falha. |
> | Colaborador envolvido | Cadastro existente. | Identificador de colaborador | Opcional | Colaborador relacionado ao acidente. |

## Regra(s) de Negócio:

> 1. Esta classificação se aplica a acidentes relacionados a falhas de Equipamentos de Proteção Individual, conforme RF02.
> 2. A descrição deve permitir identificar o equipamento e a falha registrada. A investigação de causa raiz é tratada na US15.
> 3. Os registros podem ser consultados, atualizados e arquivados pelo Gestor, preservando seu histórico.
> 4. As ocorrências não podem ser excluídas permanentemente.

## Critério(s) de Aceite:

> 1. Ao abrir uma ocorrência dessa categoria, o sistema deve apresentar os EPIs envolvidos, a descrição da falha e o estado registrado do equipamento.
> 2. Ao salvar uma atualização válida, a consulta seguinte deve apresentar os novos dados, mantendo o registro da alteração.
> 3. Ao consultar ocorrências por data, setor ou colaborador, o sistema deve apresentar os acidentes que correspondam aos filtros informados.
> 4. Ao arquivar o acidente, o sistema deve preservar os vínculos com os equipamentos e bloquear sua exclusão permanente.

# US03 – Controlar o estoque de EPIs

Referências: [RF03](requisitos/requisitos-funcionais.md#rf03-o-sistema-deve-controlar-o-estoque-dos-epis) e [UC06](casos-de-uso/casos-de-uso.md#uc06--controlar-estoque-de-epis). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** consultar o estoque e registrar entradas e saídas de EPIs,
> **para** conhecer a disponibilidade dos equipamentos e manter os saldos atualizados.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | EPI | Equipamento cadastrado. | Identificador de EPI | Obrigatório | Equipamento movimentado ou consultado. |
> | Tipo de movimentação | Não se aplica. | Entrada ou saída | Obrigatório | Operação que altera o estoque. |
> | Quantidade movimentada | Maior que zero; uma saída não pode superar o saldo. | Número inteiro | Obrigatório | Quantidade de unidades da movimentação. |
> | Saldo | Valor mínimo: zero. | Número inteiro | Calculado | Quantidade de unidades em estoque. |
> | Estoque mínimo | Valor mínimo: zero. | Número inteiro | A confirmar | Limite usado para indicar estoque crítico. |
> | Situação do EPI | Lista completa de situações a confirmar. | Situações do cadastro de EPI | Automático | Estado de uso e manutenção do equipamento. |
> | Data e hora da movimentação | Formato a confirmar. | Data e hora | Automático | Momento do registro da entrada ou saída. |
> | Responsável | O usuário autenticado. | Identificador de usuário | Automático | Pessoa que registrou a movimentação. |

## Regra(s) de Negócio:

> 1. Uma entrada aumenta o saldo e uma saída o reduz. Nenhuma movimentação pode gerar saldo negativo.
> 2. Cada movimentação deve permanecer no histórico do equipamento com identificação do responsável, data e hora.
> 3. O sistema deve indicar estoque crítico quando o saldo for igual ou inferior ao estoque mínimo.
> 4. UC06 exige CA válido para o EPI. A consulta externa de validade está fora do MVP; a forma de verificar essa condição na primeira entrega está a confirmar.
> 5. Baixa definitiva por descarte é tratada na US14.

## Critério(s) de Aceite:

> 1. Ao abrir o estoque, o sistema deve exibir nome, quantidade e situação dos EPIs; ao selecionar um item, deve exibir seu histórico.
> 2. Dado um saldo de 10 unidades, ao registrar entrada de 5 unidades, o novo saldo deve ser 15 e a entrada deve aparecer no histórico.
> 3. Dado um saldo de 10 unidades, ao registrar saída de 4 unidades, o novo saldo deve ser 6.
> 4. Ao tentar retirar 11 unidades de um saldo de 10, o sistema deve bloquear a operação, informar o saldo disponível e manter as 10 unidades.
> 5. Quando o saldo atingir o estoque mínimo ou ficar abaixo dele, o item deve receber um alerta visual.

# US04 – Registrar e consultar a manutenção de EPIs

Referências: [RF04](requisitos/requisitos-funcionais.md#rf04-o-sistema-deve-controlar-a-manutenção-dos-epis) e [UC01](casos-de-uso/casos-de-uso.md#uc01--controlar-manutenção-dos-epis). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** registrar manutenções e consultar o histórico dos EPIs,
> **para** acompanhar as condições de proteção dos equipamentos em uso e em estoque.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | EPI | Deve corresponder a equipamento cadastrado. | Identificador de EPI | Obrigatório | Equipamento submetido à manutenção. |
> | Data da manutenção | Formato a confirmar. | Data | Obrigatório | Data da realização da manutenção. |
> | Descrição da manutenção | Tamanho máximo a confirmar. | Texto | Obrigatório | Serviço realizado no equipamento. |
> | Resultado da manutenção | Classificação dos resultados a confirmar. | Texto | Obrigatório | Resultado usado para atualizar a situação do EPI. |
> | Situação do EPI | Correspondência entre resultado e situação a confirmar. | Situações do cadastro de EPI | Automático | Condição atual do equipamento. |
> | Responsável pelo registro | O usuário autenticado. | Identificador de usuário | Automático | Gestor que registrou a manutenção. |

## Regra(s) de Negócio:

> 1. A manutenção deve estar vinculada a um EPI previamente cadastrado.
> 2. O registro deve conter data, descrição e resultado, atualizando o histórico e a situação do equipamento.
> 3. As listagens operacionais devem apresentar apenas EPIs ativos e permitir filtro por situação.
> 4. A validação externa de CA e o descarte definitivo mencionados em UC01 pertencem ao backlog.

## Critério(s) de Aceite:

> 1. Ao selecionar um EPI, o sistema deve exibir seu histórico e as datas de manutenção.
> 2. Ao confirmar uma manutenção com data, descrição e resultado válidos, o sistema deve salvar o registro e atualizar a situação do EPI conforme o resultado informado.
> 3. Ao aplicar um filtro por situação, a listagem deve apresentar apenas os EPIs ativos que correspondam ao filtro.
> 4. Ao consultar um EPI inexistente, o sistema deve informar que o registro não foi encontrado e permitir nova busca.

# US05 – Mapear áreas de risco

Referências: [RF05](requisitos/requisitos-funcionais.md#rf05-o-sistema-deve-mapear-as-áreas-de-risco-do-ambiente-de-trabalho) e [UC03](casos-de-uso/casos-de-uso.md#uc03--mapear-áreas-de-risco). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** cadastrar e atualizar as áreas de risco e suas exigências de proteção,
> **para** disponibilizar ao Supervisor informações sobre os perigos de cada setor.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Código da área | Formato a confirmar. | Texto | Obrigatório; único | Código de identificação da área de risco. |
> | Identificação do setor | Tamanho máximo a confirmar. | Texto | Obrigatório | Nome ou identificação do setor físico. |
> | Agentes de risco | Catálogo de agentes a confirmar. | Lista de agentes de risco | Obrigatório | Perigos identificados no setor. |
> | Limites físicos | Formato de representação a confirmar. | Descrição ou representação de limites | Obrigatório | Delimitação física da área. |
> | Grau de perigo | Dos níveis previstos em UC03. | Baixo, médio, alto ou crítico | A confirmar | Classificação usada na consulta das áreas. |
> | EPIs obrigatórios | Pelo menos um EPI cadastrado. | Lista de identificadores de EPI | Obrigatório | Equipamentos necessários para acesso ao setor. |

## Regra(s) de Negócio:

> 1. O Gestor cadastra e atualiza áreas de risco; o Supervisor consulta as informações disponibilizadas.
> 2. O código de uma área não pode se repetir.
> 3. Toda área de risco deve ter EPIs obrigatórios vinculados, independentemente do grau de perigo.
> 4. Alterações nos agentes de risco ou nos limites devem preservar histórico. A alteração de limites físicos deve registrar auditoria.

## Critério(s) de Aceite:

> 1. Ao cadastrar uma área com código único, identificação, agentes de risco, limites e EPIs, o sistema deve atualizar o mapa e disponibilizar a área para consulta do Supervisor.
> 2. Ao informar um código já utilizado, o sistema deve impedir o cadastro e permitir sua correção.
> 3. Ao tentar salvar uma área sem EPI obrigatório, o sistema deve bloquear o salvamento e solicitar o vínculo.
> 4. Ao alterar agentes de risco ou limites, o sistema deve atualizar o mapa e registrar a alteração no histórico.
> 5. Ao filtrar pelo grau de perigo, o sistema deve apresentar apenas as áreas do nível selecionado.

# US06 – Classificar a periculosidade das tarefas

Referências: [RF06](requisitos/requisitos-funcionais.md#rf06-o-sistema-deve-permitir-classificar-o-nível-de-periculosidade-da-tarefa-a-ser-realizada) e [UC11](casos-de-uso/casos-de-uso.md#uc11--classificar-nível-de-periculosidade). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** atribuir e revisar o nível de periculosidade das tarefas,
> **para** orientar o Supervisor sobre os riscos e as exigências de proteção antes da execução.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Tarefa | Tarefa cadastrada. | Identificador de tarefa | Obrigatório | Atividade que recebe a classificação. |
> | Grau de risco | Dos níveis definidos em UC11. | Leve, moderado, grave ou crítico | Obrigatório | Nível de periculosidade da tarefa. |
> | Exigências normativas | Conteúdo e critérios de classificação a confirmar. | Texto ou lista de referências | A confirmar | Exigências consideradas na classificação da atividade. |
> | Responsável técnico | O responsável técnico pela classificação. | Identificador de usuário | Obrigatório | Gestor responsável pela classificação. |
> | Data e hora da alteração | Formato a confirmar. | Data e hora | Automático | Momento da classificação ou revisão. |

## Regra(s) de Negócio:

> 1. O Gestor define a classificação e o Supervisor pode consultá-la.
> 2. Toda alteração de nível deve registrar data, hora e responsável técnico.
> 3. A alocação em tarefa sem grau de risco cadastrado deve ser bloqueada.
> 4. UC11 prevê classificação orientada pelas NRs indicadas no projeto. Os critérios para atribuir cada nível e recalcular exigências ainda precisam ser detalhados.

## Critério(s) de Aceite:

> 1. Ao selecionar uma tarefa e salvar um grau de risco válido, o sistema deve atualizar a classificação e disponibilizá-la para consulta do Supervisor.
> 2. Ao revisar o grau de risco, o sistema deve registrar os valores alterados, data, hora e responsável.
> 3. Ao tentar alocar um colaborador em tarefa sem classificação, o sistema deve bloquear a alocação e solicitar a definição do grau de risco.
> 4. Ao consultar uma tarefa classificada, o Supervisor deve visualizar seu nível e as exigências registradas.

# US07 – Definir e acompanhar planos de ação

Referências: [RF07](requisitos/requisitos-funcionais.md#rf07-o-sistema-deve-permitir-informar-o-plano-de-ação-para-cada-tipo-de-acidente-ocorrido) e [UC07](casos-de-uso/casos-de-uso.md#uc07--definir-plano-de-ação-por-acidente--incidente). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** definir e acompanhar ações vinculadas a acidentes e incidentes,
> **para** organizar medidas preventivas e corretivas, seus responsáveis e prazos.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Ocorrência | Ocorrência previamente registrada. | Identificador de ocorrência | Obrigatório | Acidente ou incidente que originou o plano. |
> | Descrição da ação | Tamanho máximo a confirmar. | Texto | Obrigatório | Medida a ser executada. |
> | Tipo de ação | Não se aplica. | Preventiva ou corretiva | Obrigatório | Finalidade da medida. |
> | Responsável pela execução | O responsável escolhido. | Identificador de colaborador ou setor | Obrigatório | Pessoa ou setor encarregado da execução. |
> | Prazo | Não pode ser anterior à data atual nem à data da ocorrência. | Data | Obrigatório | Data limite para executar a ação. |
> | Situação da ação | Lista de situações e transições a confirmar. | Situações de acompanhamento | A confirmar | Andamento da execução. |
> | Alerta ativado | Não se aplica. | Sim ou não | A confirmar | Indica se a ação deve gerar aviso de prazo. |
> | Antecedência do alerta | Valor mínimo: zero. | Número inteiro de dias | Condicional | Antecedência escolhida para o aviso. |
> | Justificativa de prorrogação | Tamanho máximo a confirmar. | Texto | Condicional | Motivo técnico da alteração de prazo. |

## Regra(s) de Negócio:

> 1. O plano deve estar vinculado a uma ocorrência cadastrada. Toda ocorrência grave exige ao menos uma ação preventiva.
> 2. Cada ação deve ter descrição, responsável e prazo válido.
> 3. O Gestor pode configurar alertas e sua antecedência; os três dias citados em UC07 são um exemplo, não um valor fixo.
> 4. A prorrogação exige justificativa, preserva o histórico e reajusta os alertas configurados.
> 5. A troca de responsável deve atualizar a ação e emitir notificação ao novo encarregado. O canal dessa notificação está a confirmar, considerando que o Colaborador não acessa o sistema.

## Critério(s) de Aceite:

> 1. Ao salvar ações com responsáveis e prazos válidos, o sistema deve vinculá-las à ocorrência e permitir consultar seu andamento.
> 2. Ao tentar salvar o plano de uma ocorrência grave sem ação preventiva, o sistema deve indicar a pendência.
> 3. Ao informar prazo anterior à data atual ou à ocorrência, o sistema deve bloquear o salvamento e solicitar correção.
> 4. Ao prorrogar o prazo com justificativa, o sistema deve manter o histórico e recalcular a data do alerta conforme a antecedência configurada.
> 5. Ao atingir a antecedência configurada de uma ação com alerta ativado, o sistema deve emitir o aviso correspondente.

# US08 – Receber alertas de comportamento de risco

Referência: [RF08](requisitos/requisitos-funcionais.md#rf08-o-sistema-deve-gerar-alertas-automáticos-quando-um-colaborador-ou-visitante-acumular-ocorrências-de-comportamento-de-risco). Escopo: backlog, Poderia ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** receber alertas quando uma pessoa ultrapassar o limite de ocorrências de comportamento de risco,
> **para** encaminhar a revisão de conduta prevista no sistema.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Pessoa acompanhada | A pessoa acompanhada nos registros. | Identificador de colaborador ou visitante | Obrigatório | Pessoa cujo histórico é analisado. |
> | Tipo de pessoa | Conforme o cadastro consultado. | Colaborador ou visitante | Automático | Distingue a origem do cadastro. |
> | Ocorrências de risco | Critérios de inclusão na contagem a confirmar. | Lista de identificadores de ocorrência | A confirmar | Registros considerados na análise. |
> | Quantidade acumulada | Valor mínimo: zero. | Número inteiro não negativo | Calculado | Total de comportamentos de risco registrados. |
> | Limite de ocorrências | Valor mínimo: zero; valor inicial a confirmar. | Número inteiro não negativo | A confirmar | Limite que dispara o alerta quando ultrapassado. |
> | Período de análise | Janela de contagem a confirmar; RF08 não define esse período. | Intervalo de datas ou histórico completo | A confirmar | Recorte do histórico usado no cálculo. |

## Regra(s) de Negócio:

> 1. O sistema deve considerar o histórico de colaboradores e visitantes identificados nas ocorrências.
> 2. O alerta deve ser gerado quando a quantidade de ocorrências ultrapassar o limite configurado.
> 3. O alerta deve acionar revisão obrigatória de conduta.
> 4. Estão a confirmar o perfil que configura o limite, o destinatário e o canal do alerta, a janela de análise e o tratamento de alertas repetidos. O Gestor é o destinatário proposto nesta história.

## Critério(s) de Aceite:

> 1. Dado um limite configurado de 3 ocorrências, ao contabilizar a quarta ocorrência válida da mesma pessoa, o sistema deve gerar o alerta e indicar a revisão obrigatória.
> 2. Com apenas 3 ocorrências e limite igual a 3, o sistema não deve disparar o alerta de limite ultrapassado.
> 3. O alerta deve identificar a pessoa e permitir identificar os registros considerados na contagem.
> 4. Ocorrências de pessoas diferentes não devem ser somadas como histórico de uma única pessoa.

# US09 – Registrar inspeções de áreas de risco

Referência: [RF09](requisitos/requisitos-funcionais.md#rf09-o-sistema-deve-registrar-o-histórico-de-inspeções-periódicas-das-áreas-de-risco). Escopo: backlog, Deveria ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** registrar e consultar as inspeções periódicas das áreas de risco,
> **para** acompanhar as condições observadas e as conformidades ou não conformidades registradas.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Área de risco | Área de risco cadastrada. | Identificador de área de risco | Obrigatório | Área inspecionada. |
> | Data da inspeção | Formato a confirmar. | Data | Obrigatório | Data em que a inspeção foi realizada. |
> | Norma avaliada | Norma aplicável à área. | Referência a uma norma aplicável | Obrigatório | Referência considerada na inspeção. |
> | Resultado da avaliação | Não se aplica. | Conformidade ou não conformidade | Obrigatório | Resultado observado em relação à referência informada. |
> | Observações da inspeção | Tamanho máximo a confirmar. | Texto | A confirmar | Condições verificadas e descrição das não conformidades. |
> | Responsável pelo registro | Usuário autenticado. | Identificador de usuário | Automático | Pessoa que incluiu a inspeção no histórico. |

## Regra(s) de Negócio:

> 1. Cada inspeção deve estar vinculada a uma área de risco.
> 2. Os resultados devem indicar conformidade ou não conformidade em relação às normas avaliadas.
> 3. Novas inspeções devem compor o histórico da área, preservando os registros anteriores.
> 4. O Gestor é o ator proposto; RF09 não define o responsável pelo lançamento. A periodicidade, os itens de verificação e os critérios de avaliação estão a confirmar.

## Critério(s) de Aceite:

> 1. Ao registrar uma inspeção com área, data, referência e resultado, o sistema deve incluí-la no histórico da área escolhida.
> 2. Ao consultar o histórico de uma área, o sistema deve exibir as inspeções registradas e seus resultados.
> 3. Ao registrar uma segunda inspeção, a primeira deve continuar disponível com seus dados preservados.
> 4. Uma avaliação registrada como não conformidade deve aparecer com essa identificação e com as observações informadas.

# US10 – Acompanhar certificações e treinamentos

Referências: [RF10](requisitos/requisitos-funcionais.md#rf10-o-sistema-deve-controlar-o-vencimento-das-certificações-e-treinamentos-obrigatórios-dos-funcionários) e [UC02](casos-de-uso/casos-de-uso.md#uc02--controlar-certificações-e-treinamentos). Escopo: MVP.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** consultar as certificações, os treinamentos e suas pendências,
> **para** impedir a alocação de colaboradores sem a habilitação exigida para uma tarefa.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Colaborador | Colaborador cadastrado. | Identificador de colaborador | Obrigatório | Pessoa cuja capacitação é consultada. |
> | Certificação ou treinamento | Curso ou certificação cadastrada. | Identificador de capacitação | Obrigatório | Habilitação acompanhada pelo sistema. |
> | Tipo de capacitação | Correspondente ao cadastro. | Certificação ou treinamento | Automático | Natureza da capacitação consultada. |
> | Data de realização | Formato a confirmar. | Data | Condicional | Data de conclusão da capacitação. |
> | Data de vencimento | Formato a confirmar. | Data | Condicional | Limite de validade registrado. |
> | Situação da certificação | Conforme a validade cadastrada. | Em dia, próxima do vencimento ou vencida | Calculado | Situação usada nos filtros e alertas. |
> | Situação do treinamento | Conforme os registros de treinamento. | Realizado ou pendente | Automático | Indica se a capacitação foi concluída. |
> | Tarefa pretendida | Tarefa cadastrada. | Identificador de tarefa | Condicional | Tarefa na qual se pretende alocar o colaborador. |

## Regra(s) de Negócio:

> 1. Gestor e Supervisor podem consultar certificações e treinamentos dos colaboradores.
> 2. O sistema deve alertar quando uma certificação estiver próxima do vencimento, com antecedência de 30 dias, conforme UC02.
> 3. A alocação deve ser bloqueada quando a tarefa exigir uma habilitação vencida ou um treinamento obrigatório pendente.
> 4. A ausência de certificações deve ser apresentada como pendência de integração e treinamentos admissionais, conforme UC02.

## Critério(s) de Aceite:

> 1. Ao selecionar um colaborador, o sistema deve exibir as certificações cadastradas e os treinamentos realizados ou pendentes.
> 2. Dada uma certificação com vencimento em 30 dias, o sistema deve identificá-la como próxima do vencimento e emitir o alerta.
> 3. Ao tentar alocar um colaborador em tarefa que exige uma certificação vencida, o sistema deve bloquear a alocação e identificar a pendência.
> 4. Ao tentar alocar um colaborador sem o treinamento obrigatório concluído, o sistema deve indicar o impedimento.
> 5. Ao filtrar certificações por situação, o sistema deve exibir apenas os colaboradores que correspondam ao filtro; se não houver certificações cadastradas para uma pessoa, deve informar essa ausência.

# US11 – Controlar empréstimos e devoluções de EPIs

Referências: [RF11](requisitos/requisitos-funcionais.md#rf11-o-sistema-deve-controlar-a-rastreabilidade-dos-epis) e [UC12](casos-de-uso/casos-de-uso.md#uc12--controlar-empréstimo-de-epis). Escopo: MVP, limitado a colaboradores.

## Descrição da História:

> **Como** Supervisor,
> **quero** registrar empréstimos e devoluções e consultar os EPIs em posse dos colaboradores,
> **para** saber quais equipamentos estão disponíveis, em uso ou com devolução atrasada.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Colaborador | Colaborador cadastrado. | Identificador ou matrícula de colaborador | Obrigatório | Pessoa que recebe o EPI. |
> | Equipamento entregue | Equipamento cadastrado. | Código ou número de EPI rastreável | Obrigatório | Unidade cuja posse será acompanhada. |
> | Data e hora da entrega | Formato a confirmar. | Data e hora | Obrigatório | Momento da entrega ao colaborador. |
> | Responsável pela entrega | Supervisor ou Gestor autenticado. | Identificador de usuário | Obrigatório | Pessoa que registrou a entrega. |
> | Data prevista de devolução | Regra de definição a confirmar. | Data | Obrigatório | Prazo combinado para devolver o equipamento. |
> | Data e hora da devolução | Formato a confirmar. | Data e hora | Condicional | Momento em que o empréstimo foi encerrado. |
> | Estado na devolução | Não se aplica. | Apto, manutenção ou descarte | Condicional | Condição que orienta o destino do EPI. |
> | Justificativa de substituição | Tamanho máximo a confirmar. | Texto | Condicional | Motivo de novo empréstimo quando já existe item ativo em posse. |

## Regra(s) de Negócio:

> 1. Supervisor e Gestor podem registrar empréstimos e devoluções de EPIs para colaboradores cadastrados.
> 2. A entrega deve registrar data, hora, responsável e termo de cautela, reduzindo o saldo disponível.
> 3. Não é permitida entrega sem saldo, com CA vencido ou com treinamento específico obrigatório vencido.
> 4. A entrega de um segundo EPI do mesmo tipo, sem devolução do primeiro, deve exigir confirmação e justificativa de substituição.
> 5. A devolução encerra o empréstimo e atualiza o histórico e o destino do item conforme seu estado. Um equipamento encaminhado à manutenção não deve aparecer como disponível para novo empréstimo.
> 6. Um empréstimo sem devolução, após a data prevista, deve ser identificado como atrasado. A baixa definitiva de itens para descarte é tratada na US14.

## Critério(s) de Aceite:

> 1. Ao confirmar a entrega de um EPI disponível a um colaborador apto, o sistema deve registrar sua posse, gerar o termo de cautela e reduzir o saldo disponível.
> 2. Ao consultar os empréstimos, o sistema deve identificar o colaborador em posse de cada equipamento e os prazos de devolução.
> 3. Ao ultrapassar a data prevista sem devolução registrada, o empréstimo deve aparecer como atrasado.
> 4. Ao registrar a devolução de um item apto, o sistema deve encerrar o empréstimo e disponibilizar novamente o equipamento; se o estado for manutenção, deve encaminhá-lo para esse destino.
> 5. Ao tentar emprestar um EPI sem saldo, com CA vencido ou com treinamento exigido vencido, o sistema deve bloquear a entrega e informar o motivo.
> 6. Ao tentar entregar outro EPI do mesmo tipo com empréstimo ativo, o sistema deve apresentar o alerta e exigir confirmação e justificativa antes de registrar a substituição.

# US12 – Associar EPIs obrigatórios às tarefas

Referências: [RF12](requisitos/requisitos-funcionais.md#rf12-o-sistema-deve-permitir-a-conexão-entre-as-tarefas-e-os-epis) e [UC08](casos-de-uso/casos-de-uso.md#uc08--interligar-tarefa-ao-epi). Escopo: backlog, Deveria ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** definir os EPIs obrigatórios para cada tarefa,
> **para** orientar a seleção dos equipamentos necessários à execução da atividade.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Tarefa | Tarefa cadastrada. | Identificador de tarefa | Obrigatório | Atividade para a qual se define a proteção. |
> | EPIs obrigatórios | EPIs cadastrados; seleção vazia exige confirmação. | Lista de identificadores de EPI | Condicional | Equipamentos exigidos para executar a tarefa. |
> | Grau de risco | Conforme a classificação da tarefa. | Leve, moderado, grave ou crítico | Automático | Nível usado para parametrizar a proteção. |
> | Classe mínima de proteção | Catálogo e correspondência a confirmar. | Classes de proteção | A confirmar | Proteção sugerida conforme a periculosidade. |
> | Compatibilidade entre EPIs | Critérios de compatibilidade a confirmar. | Resultado da avaliação de compatibilidade | Calculado | Indica se os equipamentos selecionados podem ser usados em conjunto. |

## Regra(s) de Negócio:

> 1. Apenas o Gestor pode alterar os vínculos entre tarefas e EPIs.
> 2. No empréstimo associado a uma tarefa, o sistema deve sugerir os EPIs exigidos por ela.
> 3. A associação de um EPI com CA inválido deve ser bloqueada.
> 4. Uma seleção sem EPIs exige confirmação expressa. EPIs tecnicamente incompatíveis devem gerar alerta para seleção de modelos compatíveis.
> 5. Os critérios de compatibilidade e de classe mínima por nível de perigo estão a confirmar antes de validar essas sugestões automáticas.

## Critério(s) de Aceite:

> 1. Ao selecionar uma tarefa e confirmar EPIs válidos, o sistema deve salvar os vínculos e exibi-los na consulta seguinte.
> 2. Ao iniciar empréstimo para uma tarefa com vínculos registrados, o sistema deve sugerir os EPIs definidos para ela.
> 3. Ao tentar vincular EPI com CA inválido, o sistema deve bloquear a associação e informar o motivo.
> 4. Ao confirmar a associação sem selecionar equipamento, o sistema deve solicitar confirmação expressa.
> 5. Dada uma incompatibilidade cadastrada entre dois modelos selecionados, o sistema deve apresentar alerta técnico.
> 6. Ao tentar alterar os vínculos com perfil de Supervisor, o sistema deve negar a operação.

# US13 – Registrar um acidente de trabalho

Referências: [RF13](requisitos/requisitos-funcionais.md#rf13-o-sistema-deve-permitir-que-um-supervisor-crie-registros-de-acidente), [UC09](casos-de-uso/casos-de-uso.md#uc09--relatar-acidenteincidente) e [UC05](casos-de-uso/casos-de-uso.md#uc05--gerenciar-acidentes-e-incidentes). Escopo: MVP.

## Descrição da História:

> **Como** Supervisor,
> **quero** registrar um acidente de trabalho com os dados da ocorrência,
> **para** comunicar o ocorrido ao Gestor de Segurança e permitir seu acompanhamento.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Tipo de ocorrência | Não se aplica. | Acidente | Obrigatório | Distingue o registro de um incidente. |
> | Data da ocorrência | Formato a confirmar. | Data | Obrigatório | Data em que o acidente aconteceu. |
> | Setor | Setor cadastrado. | Identificador de setor | Obrigatório | Setor onde ocorreu o acidente. |
> | Local | Tamanho máximo a confirmar. | Texto | A confirmar | Local específico da ocorrência. |
> | Colaboradores envolvidos | Colaboradores cadastrados. | Lista de identificadores de colaborador | Condicional | Colaboradores relacionados ao acidente. |
> | Descrição dos fatos | Tamanho máximo a confirmar. | Texto | Obrigatório | Relato do que aconteceu. |
> | Lesão ou dano | Tamanho máximo a confirmar. | Texto | Condicional | Consequências conhecidas do acidente. |
> | EPIs envolvidos | EPIs cadastrados. | Lista de identificadores de EPI | Condicional | Equipamentos relacionados ao acidente. |
> | Protocolo | Formato a confirmar. | Identificador de acompanhamento | Automático | Identificação usada para consultar o relato. |
> | Responsável e momento do registro | Usuário da sessão; data e hora do cadastro. | Usuário, data e hora | Automático | Identifica quem registrou o acidente e quando. |

## Regra(s) de Negócio:

> 1. O Supervisor deve estar autenticado para registrar o acidente, inclusive quando os fatos forem comunicados por um Colaborador.
> 2. Setor e descrição são obrigatórios. Colaboradores informados devem existir no cadastro.
> 3. Após o registro, o sistema deve gerar protocolo e notificar o Gestor de Segurança.
> 4. O sistema deve registrar automaticamente usuário, data e hora do cadastro. A data do cadastro não substitui a data do acidente.
> 5. Anexos, testemunhas, investigação e CAT estão fora desta história do MVP.
> 6. O envio offline previsto em UC09 ultrapassa a consulta offline descrita em RNF08. Sua inclusão e seu funcionamento estão a confirmar.

## Critério(s) de Aceite:

> 1. Ao enviar um relato com dados válidos, o sistema deve salvar o acidente, apresentar o protocolo e notificar o Gestor.
> 2. Ao tentar enviar sem setor ou descrição, o sistema deve bloquear o envio e indicar os campos pendentes.
> 3. Ao informar um colaborador inexistente, o sistema deve impedir o vínculo e solicitar o cadastro prévio.
> 4. Ao consultar o protocolo, o Supervisor deve visualizar o relato e seu acompanhamento pelo Gestor.
> 5. O registro salvo deve conter a data do acidente, além do usuário, da data e da hora do cadastro.

# US14 – Registrar o descarte de EPIs

Referências: [RF14](requisitos/requisitos-funcionais.md#rf14-o-sistema-deve-permitir-a-gestão-de-descarte-dos-epis) e [UC06](casos-de-uso/casos-de-uso.md#uc06--controlar-estoque-de-epis). Escopo: backlog, Deveria ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** registrar o descarte de EPIs danificados, vencidos ou reprovados,
> **para** dar baixa definitiva no estoque e preservar o registro de sua destinação.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Item ou lote | Item ou lote cadastrado. | Código de item ou lote de EPI | Obrigatório | Equipamentos destinados ao descarte. |
> | Quantidade descartada | Maior que zero; menor ou igual ao saldo disponível. | Número inteiro | Obrigatório | Quantidade retirada definitivamente do estoque. |
> | Justificativa técnica | Tamanho máximo a confirmar. | Texto | Obrigatório | Motivo do descarte. |
> | Data e hora do registro | Formato a confirmar. | Data e hora | Automático | Momento do registro da baixa. |
> | Responsável | Gestor autenticado. | Identificador de usuário | Automático | Pessoa que confirmou o descarte. |
> | Termo de destinação | Conteúdo e formato a confirmar. | Documento de descarte | Automático | Comprovante da destinação registrada. |

## Regra(s) de Negócio:

> 1. O descarte deve registrar a identificação do item ou lote, a quantidade e a justificativa técnica.
> 2. A quantidade descartada não pode ultrapassar o saldo disponível.
> 3. A confirmação deve gerar baixa definitiva no inventário e termo de destinação ou descarte.
> 4. O descarte deve preservar o histórico do equipamento e da movimentação.

## Critério(s) de Aceite:

> 1. Dado um saldo de 8 unidades, ao descartar 3 com justificativa, o sistema deve reduzir o saldo para 5 e registrar a baixa definitiva.
> 2. Após a confirmação, o sistema deve disponibilizar o termo de destinação relacionado ao descarte.
> 3. Ao tentar descartar quantidade superior ao saldo, o sistema deve bloquear a operação e informar o máximo disponível.
> 4. Ao tentar confirmar sem justificativa técnica, o sistema deve indicar a pendência e manter o saldo anterior.

# US15 – Registrar a investigação de um acidente

Referências: [RF15](requisitos/requisitos-funcionais.md#rf15-o-sistema-deve-permitir-o-registro-de-investigação-de-acidente) e [UC05](casos-de-uso/casos-de-uso.md#uc05--gerenciar-acidentes-e-incidentes). Escopo: backlog, Poderia ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** registrar a investigação de um acidente e os fatores que contribuíram para sua ocorrência,
> **para** documentar a causa raiz e apoiar a definição das medidas de prevenção.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Acidente | Acidente cadastrado. | Identificador de ocorrência | Obrigatório | Ocorrência investigada. |
> | Metodologia | Ampliação do catálogo a confirmar. | Árvore de Causas ou 5 Porquês | Obrigatório | Método escolhido para conduzir a análise. |
> | Fatores determinantes | Estrutura a confirmar. | Registros de fatores humanos, materiais, organizacionais e ambientais | Obrigatório | Fatores identificados na investigação. |
> | Detalhamento da análise | Formato por metodologia a confirmar. | Texto ou estrutura do método escolhido | Obrigatório | Desenvolvimento da investigação. |
> | Causa raiz | Tamanho máximo a confirmar. | Texto | Condicional | Causa determinada na análise. |
> | Laudo da investigação | Formato a confirmar. | Registro de investigação | Obrigatório | Resultado documentado da investigação. |

## Regra(s) de Negócio:

> 1. A investigação deve permanecer vinculada ao acidente que a originou.
> 2. O registro deve contemplar a metodologia, os fatores analisados e a determinação da causa raiz.
> 3. O sistema registra a análise conduzida pelo Gestor. RF15 não prevê que o sistema determine automaticamente a causa.
> 4. O histórico da ocorrência original deve ser preservado ao incluir a investigação.

## Critério(s) de Aceite:

> 1. Ao selecionar um acidente, informar a metodologia, os fatores e a conclusão, o sistema deve salvar o laudo vinculado à ocorrência.
> 2. Ao consultar novamente o acidente, o Gestor deve conseguir acessar a investigação registrada.
> 3. O laudo deve apresentar o método utilizado, o detalhamento e a causa raiz informada.
> 4. A inclusão da investigação deve manter os dados e o histórico anteriores do acidente.

# US16 – Registrar um incidente

Referências: [RF16](requisitos/requisitos-funcionais.md#rf16-o-sistema-deve-permitir-o-registro-de-incidentes), [UC09](casos-de-uso/casos-de-uso.md#uc09--relatar-acidenteincidente) e [UC05](casos-de-uso/casos-de-uso.md#uc05--gerenciar-acidentes-e-incidentes). Escopo: MVP.

## Descrição da História:

> **Como** Supervisor,
> **quero** registrar situações de risco e incidentes, inclusive os comunicados pelos colaboradores,
> **para** permitir que o Gestor de Segurança avalie medidas de prevenção antes que ocorram lesões.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Tipo de ocorrência | Não se aplica. | Incidente | Obrigatório | Identifica uma ocorrência sem lesão. |
> | Data da ocorrência | Formato a confirmar. | Data | Obrigatório | Data em que o incidente foi observado. |
> | Setor | Setor cadastrado. | Identificador de setor | Obrigatório | Setor relacionado ao incidente. |
> | Colaboradores envolvidos | Colaboradores cadastrados. | Lista de identificadores de colaborador | Condicional | Pessoas relacionadas ao incidente. |
> | Descrição dos fatos | Tamanho máximo a confirmar. | Texto | Obrigatório | Relato da situação de risco. |
> | Potencial de dano | Classificação e tamanho máximo a confirmar. | Texto | A confirmar | Dano que a situação poderia ter causado. |
> | Protocolo | Formato a confirmar. | Identificador de acompanhamento | Automático | Identificação do relato para consulta. |
> | Responsável e momento do registro | Usuário da sessão; data e hora do cadastro. | Usuário, data e hora | Automático | Identifica quem registrou o incidente e quando. |

## Regra(s) de Negócio:

> 1. O incidente representa uma situação de risco que não gerou lesão, mas poderia tê-la gerado.
> 2. O Colaborador comunica os fatos ao Supervisor, que registra o relato com sua própria identificação. O Colaborador não acessa o sistema.
> 3. O Gestor também pode registrar incidentes conforme RF16 e UC05, além de consultar e conduzir a triagem dos relatos recebidos.
> 4. O relato exige setor e descrição. Após o registro, o sistema deve gerar protocolo e notificar o Gestor.
> 5. O registro deve preservar responsável, data e hora. Anexos e investigação formal ficam fora desta história do MVP.
> 6. O funcionamento do envio offline depende da definição indicada na US13.

## Critério(s) de Aceite:

> 1. Ao registrar um incidente com dados válidos, o sistema deve salvar a ocorrência com esse tipo, gerar protocolo e notificar o Gestor.
> 2. Ao registrar fatos comunicados por um colaborador, o sistema deve identificar o Supervisor autenticado como responsável pelo cadastro.
> 3. Ao tentar enviar sem setor ou descrição, o sistema deve bloquear o envio e indicar os campos pendentes.
> 4. Ao consultar o protocolo, o Supervisor deve visualizar o relato e o acompanhamento da triagem pelo Gestor.
> 5. O fluxo não deve exigir conta, senha ou autenticação do Colaborador para que o Supervisor registre o incidente.

# US17 – Consultar indicadores de segurança

Referência: [RF17](requisitos/requisitos-funcionais.md#rf17-o-sistema-deve-permitir-a-geração-de-dashboards-de-segurança). Escopo: backlog, Poderia ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** consultar um painel com indicadores de acidentes e de conformidade dos EPIs,
> **para** acompanhar a situação de segurança a partir dos dados registrados.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Taxa de frequência de acidentes | Fórmula, unidade e período a confirmar. | Número calculado | Calculado | Indicador de frequência dos acidentes. |
> | Indicador de gravidade | Fórmula e classificação a confirmar. | Valor ou distribuição calculada | Calculado | Informação sobre a gravidade dos acidentes registrados. |
> | Conformidade dos EPIs | Critérios de conformidade e agrupamento a confirmar. | Valores agrupados por situação de conformidade | Calculado | Situação dos EPIs apresentada no painel. |
> | Período de referência | Intervalo de referência a confirmar. | Intervalo de datas | A confirmar | Período ao qual os dados exibidos se referem. |
> | Momento da atualização | Formato a confirmar. | Data e hora | Automático | Momento dos dados apresentados. |

## Regra(s) de Negócio:

> 1. O painel deve apresentar indicadores de frequência, gravidade e conformidade de EPIs em gráficos, usando dados registrados no sistema.
> 2. RF17 prevê atualização em tempo real. A frequência técnica de atualização e as fórmulas dos indicadores estão a confirmar.
> 3. O Gestor é o perfil de consulta proposto; a relação completa de perfis autorizados não está definida em RF17.
> 4. A consulta offline prevista em RNF08 utiliza informações previamente sincronizadas e suspende a atualização em tempo real enquanto não houver conexão.

## Critério(s) de Aceite:

> 1. Ao acessar o painel com dados disponíveis, o Gestor deve visualizar gráficos de frequência de acidentes, gravidade e conformidade de EPIs.
> 2. Após atualizar os dados de origem e executar a atualização do painel, os gráficos devem refletir os registros considerados e o período de referência.
> 3. Dado um conjunto de dados de teste, os valores exibidos devem coincidir com os resultados esperados das fórmulas aprovadas. Os valores numéricos desse teste dependem da definição das fórmulas.
> 4. Sem conexão, havendo indicadores previamente sincronizados, o sistema deve permitir consultá-los sem apresentá-los como dados atualizados em tempo real.

# US18 – Emitir relatórios estatísticos

Referência: [RF18](requisitos/requisitos-funcionais.md#rf18-o-sistema-deve-permitir-a-emissão-de-relatórios-estatísticos). Escopo: backlog, Poderia ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** exportar relatórios periódicos de acidentes, treinamentos e áreas de risco,
> **para** analisar os resultados do período e compartilhar as informações necessárias ao acompanhamento da segurança.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Assunto do relatório | Combinação de assuntos a confirmar. | Acidentes, treinamentos realizados ou áreas de maior risco | Obrigatório | Conjunto de informações exportadas. |
> | Periodicidade | Não se aplica. | Mensal ou anual | Obrigatório | Tipo de período previsto em RF18. |
> | Ano de referência | Limites a confirmar. | Ano | Obrigatório | Ano dos dados consultados. |
> | Mês de referência | Valor mínimo: 1; valor máximo: 12. | Número inteiro de 1 a 12 | Condicional | Mês dos dados consultados. |
> | Formato de exportação | Não se aplica. | PDF ou CSV | Obrigatório | Formato do arquivo gerado. |
> | Arquivo do relatório | Colunas e layout a confirmar. | Arquivo PDF ou CSV | Automático | Resultado disponibilizado para exportação. |

## Regra(s) de Negócio:

> 1. O relatório deve usar os dados registrados relativos ao assunto e ao período selecionados.
> 2. O sistema deve oferecer exportação em PDF e CSV e permitir recortes mensais e anuais.
> 3. O Gestor é o perfil de emissão proposto. Perfis autorizados, fórmulas estatísticas e critério para identificar áreas de maior risco estão a confirmar.
> 4. Conforme RNF08, relatórios previamente sincronizados podem ser consultados offline; a geração de novos relatórios depende do restabelecimento da conexão.

## Critério(s) de Aceite:

> 1. Ao selecionar assunto, mês, ano e formato PDF, o sistema deve gerar um relatório referente ao mês escolhido.
> 2. Ao selecionar periodicidade anual e formato CSV, o sistema deve gerar um arquivo referente ao ano escolhido.
> 3. Os dados exportados devem corresponder ao assunto e ao período solicitados, sem incluir registros de outros períodos.
> 4. O arquivo disponibilizado deve abrir no formato escolhido e apresentar os resultados do relatório.
> 5. Sem conexão, um relatório previamente sincronizado deve permanecer acessível; uma nova geração deve informar a necessidade de conexão.

# US19 – Preparar dados para a CAT

Referência: [RF19](requisitos/requisitos-funcionais.md#rf19-o-sistema-deve-permitir-a-geração-de-documentação-legal). Escopo: backlog, Poderia ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** aproveitar os dados de um acidente no preenchimento da Comunicação de Acidente de Trabalho (CAT),
> **para** reduzir a repetição de informações na preparação do documento.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Acidente | Acidente cadastrado. | Identificador de ocorrência | Obrigatório | Registro de origem dos dados. |
> | Dados do colaborador | Campos definidos pelo modelo de CAT; a confirmar. | Dados cadastrais vinculados ao acidente | Automático | Informações existentes sobre a pessoa envolvida. |
> | Dados do acidente | Correspondência com o modelo de CAT a confirmar. | Dados da ocorrência | Automático | Informações sobre data, local e fatos já registrados. |
> | Informações complementares | Formato a confirmar. | Campos do modelo de CAT | A confirmar | Informações necessárias que ainda não constam no cadastro. |
> | Documento preenchido | Modelo, formato e mecanismo de integridade a confirmar. | Documento de CAT | Automático | Resultado da preparação para emissão. |

## Regra(s) de Negócio:

> 1. O preenchimento automático deve aproveitar os dados já existentes do acidente e do colaborador relacionado.
> 2. Os campos exigidos, o modelo do documento e o procedimento de emissão precisam ser definidos antes da implementação desta história.
> 3. RNF09 e RNF10 registram exigências de conformidade e integridade para documentos legais. Esta história não define um modelo oficial nem comprova seu atendimento.
> 4. RF19 não especifica transmissão automática a órgão externo. Esse comportamento não integra os critérios desta história.
> 5. O Gestor é o ator proposto; a autorização para emissão precisa ser confirmada no detalhamento do requisito.

## Critério(s) de Aceite:

> 1. Ao selecionar um acidente para preparar a CAT, o sistema deve preencher os campos correspondentes com os dados existentes no registro, conforme o mapeamento aprovado.
> 2. Os valores preenchidos automaticamente devem corresponder aos dados de origem do acidente selecionado.
> 3. Quando uma informação exigida pelo modelo aprovado não estiver disponível, o sistema deve indicar a pendência para preenchimento.
> 4. A conclusão dos testes de emissão depende da definição do modelo, dos campos obrigatórios e do mecanismo de integridade previsto em RNF10.

# US20 – Cadastrar fornecedores e acompanhar o CA dos EPIs

Referência: [RF20](requisitos/requisitos-funcionais.md#rf20-o-sistema-deve-permitir-a-gestão-de-fornecedores-e-certificado-de-aprovação). Escopo: backlog, Deveria ter.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** registrar os fornecedores dos EPIs e acompanhar os dados de seus Certificados de Aprovação,
> **para** identificar a origem dos equipamentos e receber aviso sobre certificados vencidos.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Fornecedor | Fornecedor cadastrado e vinculado ao EPI. | Identificador de fornecedor | Obrigatório | Fornecedor do equipamento. |
> | Nome do fornecedor | Tamanho máximo a confirmar. | Texto | Obrigatório | Identificação legível do fornecedor. |
> | EPI | EPI cadastrado. | Identificador de EPI | Obrigatório | Equipamento ao qual os dados se referem. |
> | Número do CA | Formato e validação a confirmar. | Texto identificador | Obrigatório | Número do Certificado de Aprovação. |
> | Validade do CA | Formato a confirmar. | Data | Obrigatório | Data de validade cadastrada internamente. |
> | Situação de vencimento | Tratamento da data limite a confirmar. | Vencido ou não vencido | Calculado | Resultado usado para gerar o aviso de vencimento. |

## Regra(s) de Negócio:

> 1. O sistema deve manter os fornecedores cadastrados e os dados de CA relacionados aos equipamentos.
> 2. O vencimento deve ser verificado pela data de validade cadastrada internamente no EPI, conforme RF20.
> 3. O sistema deve alertar quando essa data estiver vencida.
> 4. O Gestor é o ator proposto para a gestão. Dados obrigatórios do fornecedor, regras de duplicidade e canal do alerta estão a confirmar.
> 5. A consulta externa citada em alguns casos de uso não é necessária ao alerta por data interna definido em RF20.

## Critério(s) de Aceite:

> 1. Ao cadastrar um fornecedor e relacioná-lo a um EPI, o sistema deve apresentar esse vínculo na consulta do equipamento.
> 2. Ao salvar número e validade do CA, os dados devem permanecer disponíveis na consulta seguinte.
> 3. Dado um CA cuja validade terminou antes da data atual, o sistema deve identificá-lo como vencido e emitir o alerta correspondente.
> 4. Dado um CA com validade posterior à data atual, o sistema não deve emitir alerta de vencimento para esse certificado.

# US21 – Planejar a substituição de EPIs

Referências: [RF21](requisitos/requisitos-funcionais.md#rf21-o-sistema-deve-gerenciar-o-ciclo-de-vida-e-substituição-inteligente-de-epis) e [UC10](casos-de-uso/casos-de-uso.md#uc10--planejar-substituição-inteligente-de-epi). Escopo: MVP, conforme a especificação e a priorização atuais.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** consultar a previsão de substituição dos EPIs e receber alertas de reposição,
> **para** planejar a troca dos equipamentos antes do desgaste previsto, do vencimento cadastrado do CA ou da falta de estoque.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | EPI | EPI cadastrado com histórico de uso. | Identificador de EPI | Obrigatório | Equipamento analisado. |
> | Frequência de uso | Unidade a confirmar. | Valor numérico ou histórico de utilização | A confirmar | Frequência considerada na estimativa de desgaste. |
> | Durabilidade nominal | Unidade compatível com o cálculo. | Duração | Obrigatório | Vida útil de referência do equipamento. |
> | Condições ambientais | Estrutura e valores aceitos a confirmar. | Dados das condições do setor | Obrigatório | Condições que influenciam o desgaste. |
> | Multiplicadores de risco | Valores e fórmula a confirmar. | Valores numéricos da matriz de risco | A confirmar | Fatores de ajuste da durabilidade. |
> | Validade do CA | Conforme o cadastro interno do EPI. | Data | Automático | Limite considerado pela regra de substituição do projeto. |
> | Saldo atual | Valor mínimo: zero. | Número inteiro não negativo | Automático | Quantidade disponível para reposição. |
> | Estoque mínimo | Valor mínimo: zero. | Número inteiro não negativo | Automático | Referência de disponibilidade mínima. |
> | Data projetada de substituição | Não pode ultrapassar a validade do CA, conforme UC10. | Data | Calculado | Previsão de troca ou descarte do equipamento. |
> | Prioridade de reposição | Critérios de ordenação a confirmar. | Classificação ou ordenação | Calculado | Posição do equipamento na lista de reposição. |

## Regra(s) de Negócio:

> 1. A projeção deve considerar frequência de uso, durabilidade nominal, condições do setor e multiplicadores da matriz de risco.
> 2. Conforme a regra registrada em UC10, a data de validade do CA tem prioridade e não pode ser ultrapassada por uma prorrogação de uso.
> 3. O sistema deve considerar estoque atual e mínimo para gerar alertas preventivos de reposição.
> 4. A fórmula, as unidades, os multiplicadores, o horizonte de previsão e a antecedência dos alertas precisam ser definidos para validar os resultados numéricos.
> 5. UC10 descreve requisição automática de compra, prorrogação por laudo e exportação de custos. A inclusão desses desdobramentos no MVP está a confirmar, pois RF21 e o resultado esperado da primeira entrega explicitam projeções e alertas.

## Critério(s) de Aceite:

> 1. Ao solicitar a análise com os dados necessários disponíveis, o sistema deve apresentar as datas projetadas de substituição e a lista de reposição prioritária.
> 2. Quando o cálculo de desgaste indicar uma data posterior à validade do CA, a projeção deve respeitar o limite definido pela regra de UC10.
> 3. Quando os dados atenderem à condição de reposição configurada, o sistema deve gerar o alerta preventivo correspondente.
> 4. Diante de falha ou inconsistência no cálculo, o sistema deve informar o erro e permitir nova tentativa.
> 5. A validação de datas e prioridades calculadas deve usar exemplos com resultados esperados, definidos após a aprovação da fórmula e dos parâmetros.

# US22 – Gerenciar visitantes e seus EPIs temporários

Referências: [RF22](requisitos/requisitos-funcionais.md#rf22-o-sistema-deve-permitir-a-gerência-de-visitantes) e cenário de visitantes de [UC12](casos-de-uso/casos-de-uso.md#uc12--controlar-empréstimo-de-epis). Escopo: backlog, Deveria ter.

## Descrição da História:

> **Como** Supervisor,
> **quero** registrar visitantes e consultar os EPIs entregues temporariamente a eles,
> **para** identificar quem está em posse dos equipamentos durante a visita.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Visitante | Visitante cadastrado. | Identificador de visitante | Automático | Pessoa externa que recebe os equipamentos. |
> | Nome | Tamanho máximo a confirmar. | Texto | Obrigatório | Nome do visitante. |
> | Documento | Tipo e formato a confirmar. | Texto identificador | A confirmar | Documento usado na identificação do visitante. |
> | Empresa | Tamanho máximo a confirmar. | Texto | A confirmar | Empresa à qual o visitante está vinculado. |
> | EPIs entregues | EPIs cadastrados. | Lista de códigos de EPI rastreável | Obrigatório | Equipamentos em posse do visitante. |
> | Data e hora da entrega | Formato a confirmar. | Data e hora | Obrigatório | Momento da entrega dos equipamentos. |
> | Responsável pela entrega | Supervisor ou Gestor autenticado. | Identificador de usuário | Automático | Pessoa que registrou a entrega. |
> | Termo de cautela temporário | Vinculado ao visitante. | Registro de empréstimo | Obrigatório | Comprovante da entrega temporária. |

## Regra(s) de Negócio:

> 1. Supervisor e Gestor podem registrar visitantes, conforme RF22.
> 2. O empréstimo temporário deve identificar o visitante e os EPIs entregues, gerando termo de cautela vinculado a ele.
> 3. Os equipamentos devem ter disponibilidade para entrega e atender às restrições de empréstimo de UC12, incluindo a validade do CA.
> 4. A devolução deve atualizar a posse dos equipamentos e seu destino conforme o estado informado, seguindo o fluxo de devolução de UC12.
> 5. Os dados de identificação não representam criação de conta de acesso para o visitante.

## Critério(s) de Aceite:

> 1. Ao informar os dados do visitante e confirmar a entrega de equipamentos disponíveis, o sistema deve registrar o visitante e gerar o termo de cautela temporário.
> 2. Ao consultar o visitante, o sistema deve apresentar os EPIs que permanecem em sua posse.
> 3. Ao registrar a devolução de um equipamento, ele deve deixar de constar como empréstimo ativo do visitante, preservando o histórico.
> 4. Ao tentar entregar equipamento indisponível ou com CA vencido, o sistema deve bloquear a entrega e informar o motivo.

# US23 – Gerenciar supervisores

Referência: [RF23](requisitos/requisitos-funcionais.md#rf23-o-sistema-deve-permitir-a-gestão-de-supervisores-e-colaboradores). Escopo: MVP. O UC13 citado na especificação do MVP ainda não possui detalhamento no documento de casos de uso.

## Descrição da História:

> **Como** Gestor de Segurança,
> **quero** cadastrar, consultar e atualizar supervisores,
> **para** manter os responsáveis pelos registros operacionais e permitir seu acesso conforme o perfil.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Supervisor | Formato a confirmar. | Identificador de supervisor | Automático; único | Identificação do cadastro. |
> | Nome | Tamanho máximo a confirmar. | Texto | Obrigatório | Nome do Supervisor. |
> | Identificação de acesso | Formato de identificação a confirmar. | Identificador de acesso | Único | Informação usada pelo Supervisor para entrar no sistema. |
> | Perfil | Não se aplica. | Supervisor | Automático | Perfil que delimita as permissões. |
> | Senha inicial | Política de senha a confirmar. | Credencial de acesso | Automático | Credencial inicial prevista em RF23. |
> | Responsável pelo cadastro | Gestor autenticado. | Identificador de usuário | Automático | Pessoa que cadastrou o Supervisor. |

## Regra(s) de Negócio:

> 1. O Gestor de Segurança gerencia os supervisores.
> 2. No cadastro de um Supervisor, o sistema deve gerar automaticamente a senha inicial, conforme RF23.
> 3. A conta criada deve receber as permissões de Supervisor, sem atribuir permissões de Gestor.
> 4. Cadastro e atualizações devem registrar auditoria conforme RNF05.
> 5. O conjunto completo de dados cadastrais, a entrega da credencial e o comportamento de eventual desativação estão a confirmar. RF23 não detalha essas operações e UC13 ainda não foi especificado.

## Critério(s) de Aceite:

> 1. Ao cadastrar um Supervisor com dados válidos, o sistema deve salvar o cadastro com perfil de Supervisor e gerar automaticamente a senha inicial.
> 2. Ao consultar o Supervisor cadastrado, o Gestor deve visualizar seus dados e conseguir atualizar os campos permitidos.
> 3. Ao salvar uma atualização, os novos dados devem aparecer na consulta seguinte e a operação deve constar na auditoria.
> 4. Ao tentar gerenciar supervisores com perfil de Supervisor, o sistema deve negar a operação.
> 5. A conta criada deve acessar apenas as funcionalidades autorizadas ao perfil de Supervisor.

# US24 – Gerenciar colaboradores

Referência: [RF23](requisitos/requisitos-funcionais.md#rf23-o-sistema-deve-permitir-a-gestão-de-supervisores-e-colaboradores). Escopo: MVP, com cadastro de Colaborador sem acesso ao sistema.

## Descrição da História:

> **Como** Supervisor,
> **quero** cadastrar, consultar e atualizar colaboradores,
> **para** identificá-los nos registros de ocorrências, capacitações e empréstimos de EPIs.

## Dicionário de Dados:

> | Atributos | Propriedade | Domínio | Restrição | Descrição |
> | -------- | ----------- | ------- | --------- | --------- |
> | Colaborador | Formato a confirmar. | Identificador de colaborador | Automático; único | Identificação usada nos vínculos do sistema. |
> | Nome | Tamanho máximo a confirmar. | Texto | Obrigatório | Nome do Colaborador. |
> | Matrícula | Formato a confirmar. | Texto identificador | A confirmar | Identificação funcional do Colaborador. |
> | Setor | Setor cadastrado. | Identificador de setor | A confirmar | Setor relacionado ao Colaborador. |
> | Responsável pelo cadastro | Supervisor autenticado. | Identificador de usuário | Automático | Pessoa que incluiu o cadastro. |
> | Data e hora do cadastro | Formato a confirmar. | Data e hora | Automático | Momento de criação do registro. |

## Regra(s) de Negócio:

> 1. O Supervisor gerencia os cadastros de colaboradores.
> 2. O Colaborador é uma pessoa cadastrada para vinculação aos registros do sistema, sem conta de acesso, perfil de autenticação ou senha.
> 3. Os relatos comunicados pelo Colaborador são registrados pelo Supervisor com a identificação do Supervisor autenticado.
> 4. A atualização cadastral deve preservar os vínculos já existentes com ocorrências, capacitações e empréstimos, registrando a alteração na auditoria.
> 5. Dados cadastrais adicionais, restrições de edição e eventual desativação estão a confirmar no detalhamento de UC13.

## Critério(s) de Aceite:

> 1. Ao cadastrar um Colaborador com dados válidos, o sistema deve salvar o cadastro sem gerar senha, credencial ou conta de acesso.
> 2. O Colaborador cadastrado deve poder ser identificado e vinculado nos registros de ocorrências, capacitações e empréstimos.
> 3. Ao atualizar seus dados, o sistema deve apresentar as alterações na consulta seguinte e preservar os registros já vinculados.
> 4. Um cadastro de Colaborador, por si só, não deve permitir autenticação no SafePlace.
> 5. Ao registrar uma ocorrência relacionada ao Colaborador, o sistema deve manter a identificação do usuário autenticado que realizou o cadastro da ocorrência.
