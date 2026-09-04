# Glossário do Sistema SafePlace

Este glossário reúne os principais termos de domínio usados na documentação do SafePlace. O objetivo é padronizar a linguagem do projeto e facilitar a leitura dos requisitos, casos de uso, diagramas UML e documentos arquiteturais.

## Termos

### Acidente de trabalho

Ocorrência relacionada ao ambiente ou à execução do trabalho que gera dano, lesão ou consequência direta ao colaborador. No SafePlace, acidentes podem ser registrados, consultados, atualizados, arquivados e analisados para definição de ações preventivas e corretivas.

### Ação preventiva

Medida planejada para reduzir a chance de novos acidentes ou incidentes. Pode ser vinculada a um plano de ação e deve possuir responsável, prazo e acompanhamento.

### Adaptador

Parte da Arquitetura Hexagonal responsável por conectar o núcleo do sistema a tecnologias externas, como interface web, banco de dados, autenticação, auditoria e serviços externos.

### Agente de risco

Fator presente em uma área, atividade ou processo de trabalho que pode causar dano ao colaborador. Exemplos incluem risco físico, químico, elétrico, mecânico, ambiental ou ergonômico.

### Alerta automático

Aviso gerado pelo sistema quando uma condição importante é identificada, como vencimento de certificação, estoque crítico, EPI com CA vencido, prazo de ação próximo do vencimento ou ocorrência com alto potencial de gravidade.

### Área de risco

Setor físico ou local de trabalho que apresenta perigos mapeados e exige regras específicas de proteção. No SafePlace, áreas de risco podem ser cadastradas, atualizadas, classificadas e associadas a EPIs obrigatórios.

### Arquitetura Hexagonal

Padrão arquitetural adotado pelo SafePlace para separar regras de negócio de detalhes técnicos. Também é chamada de Ports and Adapters e organiza o sistema em domínio, aplicação, portas e adaptadores.

### Arquivamento

Forma de retirar uma ocorrência da operação ativa sem apagá-la definitivamente. No SafePlace, registros de acidentes e incidentes não devem ser excluídos fisicamente, pois precisam manter histórico e rastreabilidade.

### Ator

Pessoa ou perfil que interage com o sistema em um caso de uso. Os principais atores do SafePlace são Gestor de Segurança, Supervisor e Colaborador.

### Auditoria

Registro das ações realizadas no sistema, contendo informações como usuário responsável, data, hora e dado alterado. Serve para garantir rastreabilidade, responsabilidade e conformidade.

### Backlog

Conjunto de funcionalidades documentadas, mas não incluídas na entrega inicial do MVP. Esses itens podem ser retomados em versões futuras.

### Baixa definitiva

Registro de saída permanente de um EPI do estoque, normalmente por descarte, obsolescência, dano ou vencimento do Certificado de Aprovação.

### CAT

Comunicação de Acidente de Trabalho. Documento legal relacionado ao registro formal de acidentes de trabalho. No SafePlace, a geração de CAT aparece como funcionalidade planejada para evolução futura.

### Certificação

Comprovação de que um colaborador possui capacitação ou habilitação necessária para executar determinada tarefa. Certificações podem ter prazo de validade e bloquear alocações quando estiverem vencidas.

### Certificado de Aprovação (CA)

Registro que comprova a aprovação de um EPI para uso conforme normas aplicáveis. No SafePlace, o CA é usado para validar se um equipamento pode ser cadastrado, mantido, emprestado ou utilizado.

### Colaborador

Usuário que atua nas atividades operacionais da organização. Pode estar envolvido em acidentes, incidentes, treinamentos, certificações e empréstimos de EPIs.

### Condição insegura

Situação do ambiente, equipamento ou processo que aumenta a chance de acidente ou incidente. Pode ser usada como fator de análise durante o registro ou investigação de ocorrências.

### Controle de acesso por perfil

Restrição das funcionalidades do sistema conforme o papel do usuário. No SafePlace, os perfis principais são Colaborador, Supervisor e Gestor de Segurança.

### Descarte

Processo de retirada de um EPI de uso ou de estoque quando ele está danificado, vencido, reprovado ou obsoleto. Deve gerar registro de baixa e justificativa quando aplicável.

### Diagrama de atividades

Diagrama UML que representa o fluxo de ações, decisões e caminhos possíveis de um caso de uso. No projeto, cada caso de uso deve possuir um diagrama de atividades correspondente.

### Diagrama de sequência

Diagrama UML que representa a troca de mensagens entre atores, interfaces, controles e demais participantes de um fluxo. No projeto, cada caso de uso deve possuir um diagrama de sequência correspondente.

### Domínio

Parte central do sistema que contém as entidades, regras de negócio e conceitos essenciais do SafePlace, como ocorrências, EPIs, áreas de risco, usuários e auditoria.

### Empréstimo de EPI

Registro da entrega temporária de um EPI a um colaborador, visitante ou terceirizado. Deve indicar data, hora, responsável pela entrega e vínculo com o usuário que está em posse do equipamento.

### EPI

Equipamento de Proteção Individual. É o equipamento usado para proteger o trabalhador contra riscos associados a uma atividade ou área de trabalho, como capacete, óculos, luvas ou protetor auditivo.

### Estoque de EPIs

Controle das quantidades disponíveis, em uso, em manutenção, descartadas ou pendentes de devolução. O estoque deve impedir saldos negativos e apoiar empréstimos, devoluções e reposições.

### Evidência

Arquivo, documento, foto, vídeo ou informação anexada a uma ocorrência para apoiar sua análise. Evidências podem estar ligadas ao relato, à investigação ou ao dossiê de um acidente ou incidente.

### Falha de equipamento

Problema em um EPI ou equipamento de segurança que reduz sua capacidade de proteção. Pode ser causa ou fator relacionado a um acidente.

### Gestor de Segurança

Perfil responsável por controlar EPIs, áreas de risco, acidentes, incidentes, planos de ação, supervisores, colaboradores e regras de segurança no sistema.

### Grau de risco

Classificação usada para indicar a gravidade ou intensidade do risco associado a uma área, atividade ou função. A documentação usa níveis como baixo, médio, alto, crítico, leve, moderado, grave e crítico.

### Histórico de manutenção

Registro das manutenções realizadas em um EPI, incluindo datas, descrições, resultados e alterações de status do equipamento.

### Incidente

Ocorrência de risco que não gerou lesão, mas poderia ter causado acidente ou dano. No SafePlace, incidentes são registrados para permitir acompanhamento preventivo.

### Inspeção periódica

Verificação realizada em áreas de risco ou equipamentos para identificar conformidade, não conformidade e necessidade de ação corretiva ou preventiva.

### Investigação de acidente

Processo de análise de uma ocorrência para identificar fatores determinantes e causa raiz. A documentação cita métodos como Árvore de Causas e 5 Porquês.

### ItemEPI

Unidade física rastreável de um EPI. Enquanto EPI pode representar o tipo ou modelo do equipamento, ItemEPI representa um exemplar específico em estoque, emprestado, em manutenção ou descartado.

### Laudo pericial

Documento técnico produzido durante a investigação de uma ocorrência, registrando análise, fatores determinantes e causa raiz.

### Log imutável

Registro de auditoria que não deve ser alterado depois de gravado. É usado para preservar a confiabilidade histórica das operações realizadas no sistema.

### Manutenção de EPI

Atividade de acompanhamento e registro do estado de conservação de um EPI. Pode atualizar o status do equipamento e indicar se ele continua apto para uso.

### Mapa de riscos

Representação das áreas de risco da organização, seus perigos mapeados, níveis de perigo e EPIs necessários para acesso ou execução de atividades.

### Matriz de periculosidade

Estrutura usada para classificar atividades, funções ou setores conforme o nível de risco e as exigências normativas aplicáveis.

### Matriz de vinculação

Relação entre tarefas e EPIs obrigatórios. Ajuda o sistema a indicar quais equipamentos devem ser usados ou emprestados para determinada atividade.

### Movimentação de estoque

Registro de entrada, saída, baixa, empréstimo, devolução ou ajuste de quantidade de EPIs. Garante histórico e rastreabilidade do estoque.

### MVP

Versão inicial do SafePlace com as funcionalidades essenciais. Na documentação atual, o MVP prioriza controle de EPIs, áreas de risco, ocorrências, empréstimos, devoluções e gestão de usuários.

### Normas Regulamentadoras (NRs)

Normas brasileiras de segurança e saúde no trabalho usadas como referência para regras do domínio. A documentação cita especialmente NR-1, NR-6 e NR-9.

### Ocorrência

Registro genérico de um acidente ou incidente. Pode conter tipo, data, local, setor, colaborador envolvido, descrição, evidências, status, plano de ação e histórico.

### Perfil

Papel associado ao usuário para definir permissões e responsabilidades no sistema. Os perfis documentados são Colaborador, Supervisor e Gestor de Segurança.

### Plano de ação

Conjunto de medidas corretivas ou preventivas vinculadas a uma ocorrência. Deve conter ações, prazos, responsáveis e, quando necessário, alertas automáticos.

### Porta de entrada

Contrato da aplicação que representa uma operação disponível para os atores do sistema, como registrar ocorrência, controlar estoque, controlar empréstimos ou gerenciar áreas de risco.

### Porta de saída

Contrato da aplicação para comunicação com recursos externos ao núcleo, como persistência, autenticação, autorização, auditoria e proteção de dados sensíveis.

### Protocolo

Identificador gerado após o registro de uma ocorrência, permitindo acompanhar e consultar o relato posteriormente.

### Rastreabilidade

Capacidade de acompanhar o histórico de uma informação, ação, EPI ou ocorrência ao longo do tempo. No SafePlace, aplica-se a EPIs, estoque, empréstimos, manutenções, ocorrências e auditoria.

### Relatório estatístico

Documento ou exportação com dados consolidados sobre acidentes, treinamentos, áreas de risco ou outros indicadores. A documentação prevê formatos como PDF e CSV para versões futuras.

### Relato preventivo

Registro de uma situação de risco ou incidente feito antes que ocorra dano mais grave. Pode ser triado e, quando necessário, convertido em investigação formal.

### Requisição automática de compra

Solicitação gerada pelo sistema quando a projeção de reposição identifica necessidade futura de compra de EPIs.

### Responsável pela ação

Pessoa ou setor encarregado de executar uma ação preventiva ou corretiva registrada em um plano de ação.

### Setor

Área física ou organizacional onde ocorrem atividades de trabalho. Pode estar associado a riscos, ocorrências, colaboradores, tarefas e EPIs obrigatórios.

### Status do EPI

Situação atual de um equipamento, como disponível, em uso, em manutenção, vencido, descartado ou pendente de devolução.

### Supervisor

Perfil responsável por acompanhar atividades operacionais, registrar acidentes ou incidentes conforme permissões definidas e apoiar o controle de empréstimos de EPIs.

### Tarefa de risco

Atividade operacional que expõe o colaborador a algum nível de perigo e pode exigir EPIs, certificações ou treinamentos específicos.

### Termo de cautela

Registro formal da entrega temporária de um EPI a uma pessoa. Serve como comprovante de posse, responsabilidade e posterior devolução.

### Testemunha

Pessoa que presenciou ou possui informações relevantes sobre um acidente ou incidente. Pode ser colaborador, visitante, terceirizado ou pessoa externa vinculada ao registro da ocorrência.

### Treinamento obrigatório

Capacitação exigida para que um colaborador execute determinada tarefa ou utilize determinado EPI. Quando vencido ou ausente, pode bloquear alocações ou entregas.

### Triagem

Análise inicial de uma ocorrência ou relato preventivo para definir gravidade, prioridade, necessidade de investigação e próximos encaminhamentos.

### Usuário

Pessoa cadastrada no SafePlace que acessa o sistema por meio de um perfil. Pode ser Colaborador, Supervisor ou Gestor de Segurança.

### Visitante

Pessoa externa à organização que pode receber EPIs temporariamente para acessar uma área ou executar uma atividade específica. A gestão de visitantes está documentada como funcionalidade planejada.

## Fontes consultadas

- [README](../README.md)
- [Requisitos funcionais](requisitos/requisitos-funcionais.md)
- [Requisitos não funcionais](requisitos/requisitos-nao-funcionais.md)
- [Priorização MoSCoW](requisitos/priorizacao-moscow.md)
- [Casos de uso](casos-de-uso/casos-de-uso.md)
- [Especificação do MVP e Arquitetura](mvp/especificacao-mvp-arquitetura.md)
- [Especificação arquitetural](arquitetura/especificacao-arquitetural.md)
