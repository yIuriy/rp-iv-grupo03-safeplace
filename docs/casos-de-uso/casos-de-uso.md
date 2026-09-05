# Documentação dos Casos de Uso

## 1. Introdução

Este documento especifica os casos de uso do SafePlace. Os fluxos incluem funcionalidades do MVP e itens planejados para versões posteriores. A classificação de cada requisito pode ser consultada em [Priorização de Requisitos](../requisitos/priorizacao-moscow.md).

## 2. Recorte do MVP

O MVP contempla os fluxos essenciais dos casos de uso primários: **UC01**, **UC03**, **UC05**, **UC06**, **UC09** e **UC12**.

Os demais casos de uso permanecem documentados como backlog para versões futuras. Dentro dos casos de uso selecionados para o MVP, cenários avançados (como integração externa de CA, emissão automatizada de CAT, gestão de mídias e projeções preditivas) fazem parte do escopo futuro.

---

## UC01 – Controlar Manutenção dos EPI's

No MVP, o fluxo cobre consulta e registro de manutenção. Validação externa de CA e descarte definitivo permanecem no backlog.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC01** |
| **Nome** | Controlar Manutenção dos EPI's |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Nenhum |
| **Resumo** | Descreve as etapas percorridas por um gestor de segurança para controlar a manutenção dos Equipamentos de Proteção Individual (EPIs), garantindo que estejam em condições adequadas de uso. |
| **Pré-condições** | O Gestor de Segurança deve estar logado no sistema;<br>Os EPIs devem estar previamente cadastrados. |
| **Pós-condições** | Situação do EPI e histórico de manutenção atualizados. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o módulo de EPIs | |
| | 2. Exibir a lista de EPIs cadastrados com seus status |
| 3. Consultar detalhes de um EPI específico | |
| | 4. Exibir histórico e datas de manutenção |
| 5. Selecionar a opção de registrar manutenção | |
| | 6. Solicitar data, descrição e resultado da manutenção |
| 7. Preencher os dados e confirmar | |
| | 8. Validar os dados, registrar a manutenção e atualizar o status do EPI |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Deve haver clareza visual nos indicadores de status dos equipamentos. | 2. Apenas EPIs ativos devem ser exibidos nas listagens operacionais. |
| **Cenário Alternativo I - Filtrar EPIs por status** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar filtros da listagem | |
| 2. Selecionar um critério (ex.: vencidos, em manutenção) | |
| | 3. Atualizar a lista exibida conforme os critérios |
| **Cenário de Exceção I - EPI não encontrado** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar acessar um EPI | |
| | 2. Não encontrar o registro e exibir mensagem de erro |
| 3. Permitir nova busca | |
| **Cenário de Exceção II - EPI com Certificado de Aprovação (CA) vencido** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar EPI para registrar manutenção preventiva/corretiva | |
| | 2. Identificar que o CA do equipamento está expirado ou cancelado |
| | 3. Bloquear o registro de manutenção e emitir alerta de conformidade normativa |
| | 4. Exigir o encaminhamento obrigatório do equipamento para descarte definitivo |

---

## UC02 – Controlar Certificações e Treinamentos

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC02** |
| **Nome** | Controlar Certificações e Treinamentos |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Supervisor |
| **Resumo** | Descreve o monitoramento das certificações dos colaboradores, bem como os treinamentos realizados ou pendentes. O sistema gera alertas quando as certificações vencerem ou quando for necessário treinamento obrigatório. |
| **Pré-condições** | O Gestor ou Supervisor deve estar logado no sistema;<br>Os dados dos colaboradores e cursos devem estar cadastrados. |
| **Pós-condições** | Situação das certificações e treinamentos verificada, com emissão de alertas em caso de pendências. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o menu "Certificações e Treinamentos" | |
| | 2. Carregar listagem de colaboradores |
| 3. Selecionar um colaborador | |
| | 4. Exibir detalhes das certificações |
| | 5. Exibir detalhes dos treinamentos |
|6. Visualizar certificações cadastradas | |
| 7. Visualizar treinamentos realizados | |
| **Regras de Negócio, Restrições e Validações** | |
| 1. O sistema deve alertar automaticamente quando uma certificação estiver próxima do vencimento (30 dias). | 2. O sistema deve bloquear a alocação de colaboradores com certificações vencidas em tarefas de risco. |
| **Cenário Alternativo I - O colaborador possui certificados vencidos** | |
| **Ações do Ator** | **Ações do Sistema** |
| | 1. Identificar certificações vencidas |
| | 2. Destacar certificações que necessitam de renovação |
| | 3. Emitir alerta notificando que certificados necessitam de renovação |
| **Cenário Alternativo II - O colaborador possui treinamentos pendentes** | |
| **Ações do Ator** | **Ações do Sistema** |
| | 1. Identificar treinamentos pendentes |
| | 2. Destacar treinamentos obrigatórios não realizados |
| | 3. Emitir alerta de bloqueio preventivo para tarefas de risco |
| **Cenário Alternativo III - Colaborador sem certificações cadastradas** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar colaborador | |
| | 2. Identificar ausência de histórico de certificações |
| | 3. Exibir mensagem indicando pendência da trilha inicial de integração |
| | 4. Exibir mensagem indicando pendência de treinamentos admissionais |
| **Cenário Alternativo IV - Filtrar colaboradores por status de certificação** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar painel de filtros de certificações | |
| 2. Selecionar status desejado (em dia, próximas do vencimento em 30 dias ou vencidas) | |
| | 3. Atualizar a listagem exibindo apenas os colaboradores que atendem ao filtro |
| **Cenário de Exceção I - Bloqueio de alocação por certificação obrigatória vencida** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar alocar colaborador em tarefa de risco (ex.: NR-10, NR-35) | |
| | 2. Detectar que a certificação obrigatória do colaborador está vencida |
| | 3. Bloquear a alocação e emitir alerta impeditivo ao Supervisor/Gestor |

---

## UC03 – Mapear Áreas de Risco 

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC03** |
| **Nome** | Mapear Áreas de Risco |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Supervisor |
| **Resumo** | Permite ao Gestor de Segurança cadastrar, delimitar e gerenciar os setores físicos da empresa, identificando agentes de risco e exigências de proteção, disponibilizando as diretrizes atualizadas para consulta operacional do Supervisor. |
| **Pré-condições** | O Gestor de Segurança deve estar logado no sistema. |
| **Pós-condições** | Áreas de risco cadastradas/atualizadas e mapa de riscos disponibilizado para consulta do Supervisor. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o módulo de Áreas de Risco | |
| | 2. Exibir lista de setores físicos cadastrados |
| 3. Selecionar a opção de cadastrar nova área de risco | |
| | 4. Apresentar formulário com campos de identificação, agentes de risco e limites |
| 5. Preencher dados do setor | |
| 6. Preencher perigos mapeados | |
| 7. Confirmar cadastro | |
| | 8. Validar dados informados |
| | 9. Salvar novo setor de risco|
| | 10. Atualizar o mapa global de riscos da organização |
| | 11. Disponibilizar para consulta do Supervisor |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Todo setor cadastrado como área de risco, independentemente do grau de perigo, exige o vínculo obrigatório dos EPIs necessários para acesso. | 2. Toda alteração de limite físico de área de risco gera registro auditado. |
| **Cenário Alternativo I - Filtrar áreas por grau de perigo** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar filtros do mapa de risco | |
| 2. Selecionar o nível desejado (baixo, médio, alto, crítico) | |
| | 3. Atualizar a listagem exibindo apenas os setores do nível selecionado |
| **Cenário Alternativo II - Atualização de agentes de risco e redefinição de limites** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar setor de risco existente | |
| 2. Alterar os fatores de risco mapeados ou limites físicos | |
| | 3. Validar alterações |
| | 4. Registrar nova versão no histórico |
| | 5. Atualizar mapa de risco|
| **Cenário de Exceção I - Setor com código duplicado** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Confirmar cadastro com código já existente | |
| | 2. Identificar duplicidade |
| | 3. Exibir alerta de código já em uso|
| 4. Corrigir o código | |
| | 5. Reenviar formulário|
| **Cenário de Exceção II - Bloqueio de cadastro sem vínculo de EPIs obrigatórios** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar salvar área de risco sem selecionar nenhum EPI obrigatório de proteção | |
| | 2. Detectar ausência de EPIs vinculados |
| | 3. Bloquear o salvamento |
| | 4. Exigir a inclusão dos EPIs necessários para acesso ao setor|

---

## UC04 – Anexar Mídias e Testemunhas

Este caso de uso está fora do MVP. Ele permanece documentado como extensão futura de UC05 e UC09.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC04** |
| **Nome** | Anexar Mídias e Testemunhas (Extensão de UC05 e UC09) |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Supervisor |
| **Resumo** | Permite anexar evidências documentais, fotos, vídeos e declarações de testemunhas durante o relato de um incidente ou à investigação de um acidente registrado, enriquecendo o dossiê da ocorrência. |
| **Pré-condições** | A ocorrência (acidente ou incidente) deve estar em processo de registro ou previamente cadastrada no sistema;<br>O usuário deve estar autenticado. |
| **Pós-condições** | As mídias e testemunhas ficam vinculadas ao registro da ocorrência (acidente ou incidente). |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar uma ocorrência previamente cadastrada ou em processo de registro | |
| | 2. Exibir dados da ocorrência |
| 3. Selecionar a opção de anexar mídias/testemunhas | |
| | 4. Apresentar opções de upload de mídias |
| | 5. Apresentar cadastro de testemunhas |
| 6. Inserir os dados das testemunhas | |
| | 7. Selecionar arquivos de mídia |
| | 8. Validar formatos |
| | 9. Validar tamanhos dos arquivos|
| | 10. Vincular as evidências ao registro da ocorrência |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Documentos e imagens de acidentes devem ser armazenados de forma segura e criptografada. | 2. Toda remoção de evidência pericial exige justificativa técnica obrigatória. |
| **Cenário Alternativo I - Remover mídia ou testemunha já anexada** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar a lista de evidências vinculadas | |
| 2. Selecionar o item que deseja remover | |
| | 3. Exibir diálogo de confirmação solicitando justificativa |
| 4. Confirmar remoção | |
| 5. Informar justificativa obrigatória | |
| | 6. Validar justificativa |
| | 7. Desvincular o item|
| | 8. Registrar exclusão no log de auditoria|
| | 9. Atualizar a listagem refletindo a remoção |
| **Cenário Alternativo II - Cadastro de testemunha externa** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Identificar que a testemunha é externa (terceirizado/visitante sem matrícula) | |
| 2. Preencher formulário de testemunha avulsa com nome, documento e contato | |
| | 3. Validar dados de contato |
| | 4. Vincular o depoimento avulso à ocorrência|
| **Cenário de Exceção I - Arquivo com formato ou tamanho inválido** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar arquivo para upload | |
| | 2. Detectar formato não suportado ou tamanho acima do limite |
| | 3. Exibir mensagem de erro especificando formatos aceitos (JPG, PNG, MP4, PDF) |
| | 4. Cancelar o upload |
| | 5. Descartar o arquivo inválido |
| **Cenário de Exceção II - Tentativa de remoção sem justificativa obrigatória** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Confirmar exclusão de evidência deixando o campo de justificativa em branco | |
| | 2. Detectar ausência de justificativa |
| | 3. Bloquear a exclusão |
| | 4. Exigir o preenchimento do motivo para fins de auditoria|
| **Cenário de Exceção III - Falha de upload por interrupção de conexão de rede** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Iniciar upload de arquivo pesado | |
| | 2. Detectar queda de conexão antes da conclusão do envio |
| | 3. Alertar sobre a interrupção |
| | 4. Disponibilizar opção de retentar o upload |

---

## UC05 – Gerenciar Acidentes e Incidentes

No MVP, este caso de uso cobre o registro, a consulta, a atualização e o arquivamento de ocorrências. Investigação de causa raiz, plano de ação, CAT e anexos permanecem fora da primeira entrega.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC05** |
| **Nome** | Gerenciar Acidentes e Incidentes |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Nenhum |
| **Resumo** | Permite registrar, consultar, atualizar e conduzir a investigação pericial de acidentes e incidentes de trabalho causados por fatores humanos, condições inseguras ou falha de equipamentos. |
| **Pré-condições** | O Gestor de Segurança deve estar logado no sistema;<br>O colaborador envolvido deve estar cadastrado (se aplicável). |
| **Pós-condições** | Acidente ou incidente formalizado no sistema, gerando histórico para investigação, plano de ação e documentação legal (CAT). |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar a funcionalidade de gerenciamento de acidentes e incidentes | |
| | 2. Apresentar opções (cadastrar, consultar ou editar ocorrência) |
| 3. Escolher cadastrar ocorrência | |
| | 4. Solicitar dados da ocorrência (tipo: acidente ou incidente, data, local, colaborador, tipo de lesão ou potencial de dano, EPIs) |
| 5. Preencher as informações solicitadas | |
| | 6. Validar os dados |
| | 7. Registrar a ocorrência no sistema|
| | 8. Oferecer execução do caso de uso estendido **UC04 - Anexar Mídias e Testemunhas** |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Registros de acidentes e incidentes não podem ser excluídos do sistema, apenas arquivados. | 2. O sistema registra automaticamente data, hora e usuário responsável pelo cadastro. |
| **Cenário Alternativo I - Consultar ocorrência existente** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar a opção 'Consultar ocorrência' | |
| | 2. Exibir campos de busca |
| | 3. Exibir filtros (por tipo: acidente ou incidente, colaborador, data, setor, gravidade) |
| 4. Informar os critérios de busca e confirmar | |
| | 5. Buscar e exibir a lista de ocorrências correspondentes |
| 6. Selecionar uma ocorrência da lista | |
| | 7. Exibir detalhes completos |
| | 8. Exibir evidências anexadas|
| | 9. Exibir status do plano de ação|
| **Cenário Alternativo II - Registro de investigação de causa raiz** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar aba de "Investigação Pericial" da ocorrência | |
| 2. Selecionar metodologia de análise (Árvore de Causas ou 5 Porquês) | |
| 3. Mapear os fatores determinantes (humanos, materiais, organizacionais e ambientais) | |
| | 4. Validar e salvar o laudo pericial com a determinação da causa raiz |
| **Cenário Alternativo III - Triagem e conversão de relato preventivo em investigação formal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar lista de incidentes relatados pelos supervisores | |
| 2. Selecionar relato preventivo | |
| 3. Identificar alto potencial de gravidade| |
| 4. Acionar opção "Converter em Investigação Formal" | |
| | 5. Criar processo de investigação formal |
| | 6. Vincular o histórico do relato original |
| **Cenário de Exceção I - Colaborador não cadastrado** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Informar identificação de colaborador inexistente | |
| | 2. Exibir erro informando que o colaborador precisa estar registrado previamente |
| **Cenário de Exceção II - Tentativa de exclusão física de registro de ocorrência** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar excluir permanentemente um registro de acidente ou incidente | |
| | 2. Detectar tentativa de deleção física |
| | 3. Bloquear a exclusão |
| | 4. Emitir alerta de conformidade legal informando a obrigatoriedade de arquivamento auditado por 5 anos|

---

## UC06 – Controlar Estoque de EPIs

No MVP, este caso de uso cobre entradas, saídas, consulta de saldo e histórico de movimentações. Descarte, gestão de fornecedores e validações externas do Certificado de Aprovação permanecem no backlog.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC06** |
| **Nome** | Controlar Estoque de EPIs |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Nenhum |
| **Resumo** | Descreve o controle de estoque de Equipamentos de Proteção Individual, permitindo visualizar, registrar entradas, ajustar saldos e monitorar níveis mínimos. |
| **Pré-condições** | O Gestor de Segurança deve estar logado no sistema;<br>Os EPIs devem estar cadastrados. |
| **Pós-condições** | Estoque atualizado e movimentações registradas com rastreabilidade. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o menu "Estoque de EPIs" | |
| | 2. Carregar listagem de EPIs disponíveis com nome, quantidade e status |
| 3. Selecionar um EPI específico | |
| | 4. Exibir detalhes do item e histórico de movimentações |
| 5. Clicar em "Registrar nova quantidade/entrada" | |
| 6. Informar dados | |
| | 7. Validar dados informados |
| | 8. Atualizar o saldo de estoque no sistema |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Não é permitido que o estoque fique com quantidade negativa. | 2. Todo EPI cadastrado deve possuir Certificado de Aprovação (CA) válido. |
| **Cenário Alternativo I - Estoque vazio / crítico** | |
| **Ações do Ator** | **Ações do Sistema** |
| | 1. Identificar que o saldo do item está igual ou inferior ao estoque mínimo |
| | 2. Destacar o item em alerta visual no painel do Gestor |
| **Cenário Alternativo II - Baixa definitiva de estoque por descarte ou obsolescência** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar o item ou lote de EPIs danificados, reprovados ou com CA expirado | |
| 2. Clicar em "Registrar Descarte/Baixa Definitiva" | |
| 3. Informar o código do item/lote | |
| 4. Informar quantidade a descartar | |
| 5. Informar justificativa técnica | |
| | 6. Validar se a quantidade informada é menor ou igual ao saldo disponível |
| | 7. Dar baixa na quantidade do inventário |
| | 8. Gerar o termo de destinação/descarte |
| **Cenário de Exceção I - Bloqueio de entrada de lote com CA vencido ou inválido** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Informar número de CA vencido ou cancelado no cadastro de entrada | |
| | 2. Validar status do CA na base normativa |
| | 3. Bloquear o recebimento do lote no estoque |
| | 4. Alertar sobre a irregularidade |
| **Cenário de Exceção II - Tentativa de saída com quantidade superior ao saldo em estoque** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Informar quantidade a descartar superior ao saldo  disponível | |
| | 2. Detectar tentativa de geração de saldo negativo |
| | 3. Bloquear a transação |
| | 4. Exibir alerta informando o saldo máximo permitido|

---

## UC07 – Definir Plano de Ação por Acidente / Incidente

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC07** |
| **Nome** | Definir Plano de Ação por Acidente / Incidente |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Nenhum |
| **Resumo** | Permite definir medidas corretivas e preventivas vinculadas a um acidente ou incidente ocorrido para mitigar novas ocorrências, configurando prazos, responsáveis e parâmetros de alertas automáticos. |
| **Pré-condições** | O Gestor deve estar logado;<br>O acidente/incidente deve estar previamente registrado. |
| **Pós-condições** | Plano de ação formalizado e monitorado pelo sistema. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o módulo de acidentes/incidentes | |
| | 2. Exibir a lista de ocorrências registradas |
| 3. Selecionar uma ocorrência | |
| | 4. Exibir os detalhes da ocorrência |
| 5. Definir as ações necessárias, prazos, responsáveis e ativar/configurar gatilhos de alerta | |
| 6. Confirmar registro do plano | |
| | 7. Validar dados e salvar informações vinculando o plano à ocorrência |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Toda ocorrência grave exige ao menos uma ação preventiva cadastrada. | 2. O Gestor pode parametrizar quais ações emitirão alertas automáticos e a antecedência do aviso (ex.: 3 dias antes do vencimento do prazo). |
| **Cenário Alternativo I - Prorrogação de prazo de ação com justificativa** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar ação com prazo a expirar ou em atraso | |
| 2. Informar nova data limite e preencher justificativa técnica formal | |
| | 3. Validar justificativa, registrar histórico da prorrogação e reajustar gatilhos de alerta |
| **Cenário Alternativo II - Reatribuição de responsável pela ação** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar ação preventiva | |
| 2. Alterar o colaborador/setor responsável pela execução | |
| | 3. Atualizar o responsável e emitir notificação ao novo encarregado |
| **Cenário de Exceção I - Tentativa de cadastro de ação com prazo retroativo** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Inserir prazo de conclusão anterior à data atual ou à data do acidente | |
| | 2. Detectar inconsistência temporal no cronograma |
| | 3. Bloquear o salvamento e solicitar a definição de um prazo futuro válido |

---

## UC08 – Interligar Tarefa ao EPI

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC08** |
| **Nome** | Interligar Tarefa ao EPI |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Nenhum |
| **Resumo** | Descreve as etapas para associar tarefas e atividades operacionais aos Equipamentos de Proteção Individual obrigatórios para sua execução. |
| **Pré-condições** | O Gestor deve estar logado;<br>Os EPIs e as tarefas devem estar cadastrados. |
| **Pós-condições** | Tarefa vinculada a um ou mais EPIs obrigatórios. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o módulo de tarefas/EPIs | |
| | 2. Exibir a lista de tarefas cadastradas |
| 3. Selecionar uma tarefa | |
| | 4. Apresentar lista de EPIs disponíveis no catálogo |
| 5. Marcar os EPIs de uso obrigatório | |
| 6. Confirmar associação | |
| | 7. Validar dados |
| | 8. Verificar compatibilidade |
| | 9. Salvar a matriz de vinculação |
| **Regras de Negócio, Restrições e Validações** | |
| 1. No momento do empréstimo de EPIs, o sistema deve sugerir automaticamente os itens exigidos pela tarefa selecionada. | 2. Apenas o Gestor de Segurança pode alterar a matriz de EPIs por tarefa. |
| **Cenário Alternativo I - Parametrização por nível de periculosidade da tarefa** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar o nível de perigo | |
| | 2. Filtrar |
| | 3. Sugerir classe mínima de proteção |
| 4. Confirmar matriz de proteção parametrizada | |
| | 5. Apresentar lista de EPIs disponíveis no catálogo |
| **Cenário de Exceção I - Tarefa sem EPI associado** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Confirmar associação sem marcar EPI | |
| | 2. Exibir aviso de confirmação expressa |
| **Cenário de Exceção II - Bloqueio de EPI com CA inválido** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Confirmar associação com EPI de CA inválido | |
| | 2. Detectar irregularidade do CA |
| | 3. Bloquear a vinculação |
| | 4. Exibir mensagem de selecionar item com CA válido |
| **Cenário de Exceção III - Alerta de incompatibilidade técnica entre EPIs** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar EPIs que apresentam incompatibilidade de acoplamento físico (ex.: protetor auditivo tipo concha incompatível com modelo de capacete) | |
| | 2. Detectar conflito de compatibilidade entre os equipamentos |
| | 3. Emitir alerta técnico orientando a seleção de modelo compatível |

---

## UC09 – Relatar Acidente/Incidente

No MVP, o Colaborador pode relatar incidentes. O Supervisor pode relatar acidentes e incidentes. O Gestor de Segurança consulta e conduz a triagem dos registros.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC09** |
| **Nome** | Relatar Acidente/Incidente |
| **Ator Principal** | Colaborador ou Supervisor |
| **Atores Secundários** | Gestor de Segurança |
| **Resumo** | Permite o registro rápido de ocorrências de acidentes e incidentes no ambiente de trabalho pelo supervisor, viabilizando a pronta comunicação ao Gestor de Segurança para início de ações preventivas e corretivas. |
| **Pré-condições** | O Colaborador ou Supervisor deve estar logado no sistema. |
| **Pós-condições** | Acidente ou incidente registrado no sistema com notificação enviada ao Gestor de Segurança. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar a funcionalidade "Relatar Acidente/Incidente" | |
| | 2. Exibir formulário de relato rápido de acidentes e incidentes |
| 3. Selecionar o tipo de ocorrência (acidente ou incidente) | |
| 4. Informar setor| |
| 5. Informar colaboradores envolvidos | |
| 6. Informar descrição dos fatos | |
| 7. Acionar opcionalmente o caso de uso estendido **UC04 - Anexar Mídias e Testemunhas** | |
| 8. Confirmar o envio do relato de acidente/incidente | |
| | 9. Validar dados |
| | 10. Registrar o acidente/incidente |
| | 11. Gerar protocolo|
| | 12. Enviar notificação automática da ocorrência ao Gestor de Segurança |
| **Regras de Negócio, Restrições e Validações** | |
| 1. O formulário deve ser objetivo para preenchimento ágil. | 2. O Colaborador pode registrar incidentes; o Supervisor pode registrar acidentes e incidentes. |
| **Cenário Alternativo I - Anexar foto ou evidência ao relato** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar foto ou evidência do acidente/incidente antes de enviar | |
| | 2. Validar formato da mídia |
| | 3. Vincular ao relato do acidente/incidente|
| **Cenário Alternativo II - Consultar acidentes/incidentes relatados** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar listagem de acidentes e incidentes enviados pelo supervisor | |
| | 2. Exibir status de triagem |
| | 3. Exibir acompanhamento das ocorrências pelo Gestor de Segurança |
| **Cenário de Exceção I - Envio de relato em modo offline** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Confirmar envio do relato de acidente/incidente sem conexão à internet | |
| | 2. Armazenar o relato localmente na memória do PWA |
| | 3. Sincronizar automaticamente ao restabelecer sinal |
| **Cenário de Exceção II - Relato com campos obrigatórios não preenchidos** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar submeter relato de acidente/incidente sem informar setor ou descrição | |
| | 2. Detectar campos obrigatórios em branco |
| | 3. Bloquear envio e indicar campos pendentes|

---

## UC10 – Planejar Substituição Inteligente de EPI

Este caso de uso está fora do MVP porque implementa o RF21, classificado como `Won't have` na versão atual.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC10** |
| **Nome** | Planejar Substituição Inteligente de EPI |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Nenhum |
| **Resumo** | Gerencia o ciclo de vida dos EPIs, automatizando o cálculo de desgaste e prevendo substituições baseadas em horas de uso, agressividade do setor e validade do CA. |
| **Pré-condições** | Histórico de uso de EPIs atualizado;<br>Níveis de estoque registrados. |
| **Pós-condições** | Projeção de descarte gerada e requisições preventivas registradas. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o módulo de histórico dos EPIs | |
| 2. Solicitar ao sistema a "Análise de Projeção de Desgaste" | |
| | 3. Consultar frequência de uso |
| | 4. Consultar durabilidade nominal do EPI |
| | 5. Consultar condições ambientais do setor |
| | 6. Cruzar os dados coletados das consultas |
| | 7. Aplicar multiplicadores da matriz de risco |
| | 8. Verificar validade do Certificado de Aprovação (CA) no cadastro do EPI |
| | 9. Gerar projeção de descarte para os próximos períodos |
| 10. Analisar a lista de projeções geradas | |
| | 11. Emitir lista de reposição prioritária |
| | 12. Consultar estoque atual e mínimo |
| | 13. Gerar requisição automática de compra |
| **Regras de Negócio, Restrições e Validações** | |
| 1. A data de expiração do CA é prioritária: nenhum EPI pode ter uso prorrogado se o CA estiver vencido. | 2. O cálculo de projeção aplica multiplicadores baseados na matriz de risco do setor. |
| **Cenário Alternativo I - Prorrogação por inspeção técnica** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Realizar inspeção técnica presencial e atestar bom estado | |
| 2. Inserir laudo técnico | |
| | 3. Estender a data projetada de descarte respeitando o limite legal do CA |
| **Cenário Alternativo II - Ajuste manual do lote de compra** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar requisição automática de compra | |
| 2. Ajustar quantitativos ou adicionar itens complementares | |
| | 3. Atualizar requisição com os novos quantitativos |
| **Cenário Alternativo III - Exportação de relatório preditivo e custos** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Solicitar exportação da análise para o próximo trimestre | |
| | 2. Gerar relatório consolidado em PDF/CSV |
| **Cenário de Exceção I - Falha no processamento do cálculo de projeção** | |
| **Ações do Ator** | **Ações do Sistema** |
| | 1. Detectar falha ou inconsistência no cálculo da projeção |
| | 2. Exibir mensagem de erro e permitir nova tentativa |
| 3. Solicitar nova tentativa | |

---

## UC11 – Classificar Nível de Periculosidade

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC11** |
| **Nome** | Classificar Nível de Periculosidade |
| **Ator Principal** | Gestor de Segurança |
| **Atores Secundários** | Supervisor |
| **Resumo** | Permite ao Gestor de Segurança parametrizar a matriz de periculosidade das atividades e setores segundo as Normas Regulamentadoras (NRs), disponibilizando as diretrizes para visualização e consulta operacional do Supervisor. |
| **Pré-condições** | O Gestor de Segurança deve estar logado no sistema. |
| **Pós-condições** | Níveis de periculosidade configurados e disponibilizados para consulta operacional do Supervisor. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar o módulo "Classificação de Periculosidade" | |
| | 2. Listar funções, cargos e atividades cadastradas |
| 3. Selecionar uma atividade | |
| 4. Definir o grau de risco (Leve, Moderado, Grave ou Crítico) e exigências normativas | |
| 5. Confirmar a parametrização | |
| | 6. Validar dados, atualizar a matriz de periculosidade da organização e disponibilizar para consulta do Supervisor |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Qualquer alteração no nível de risco gera registro com data, hora e responsável técnico. | 2. A classificação deve seguir estritamente as diretrizes da NR-1, NR-6 e NR-9. |
| **Cenário Alternativo I - Reavaliação periódica de risco** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Alterar a classificação de uma função após implantação de melhoria de segurança | |
| | 2. Recalcular automaticamente os requisitos de EPIs e certificações para a função |
| **Cenário de Exceção I - Função sem nível atribuído** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar alocar colaborador em função sem grau de risco cadastrado | |
| | 2. Bloquear alocação e solicitar a classificação prévia da função |

---

## UC12 – Controlar Empréstimo de EPIs

No MVP, este caso de uso cobre empréstimos e devoluções para colaboradores. Empréstimos para visitantes, bloqueios por treinamento e sugestões automáticas de equipamentos permanecem fora da primeira entrega.

| Elemento / Ações do Ator | Detalhes / Ações do Sistema |
| :--- | :--- |
| **Identificador** | **UC12** |
| **Nome** | Controlar Empréstimo de EPIs |
| **Ator Principal** | Supervisor |
| **Atores Secundários** | Gestor de Segurança |
| **Resumo** | Descreve o registro de entrega, posse e devolução de EPIs a colaboradores. O atendimento a visitantes fica reservado para versão posterior. |
| **Pré-condições** | O Supervisor ou Gestor deve estar logado;<br>O colaborador e os EPIs devem estar cadastrados. |
| **Pós-condições** | Empréstimo ou devolução registrado e estoque movimentado. |
| **Cenário Principal** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar a tela de "Empréstimo de EPIs" | |
| 2. Selecionar a operação "Novo Empréstimo" | |
| 3. Informar a matrícula do colaborador | |
| | 4. Exibir os EPIs disponíveis para empréstimo |
| 5. Ler o código/número do EPI disponível e confirmar entrega | |
| | 6. Validar o status do EPI, registrar o termo de cautela e debitar do estoque |
| **Regras de Negócio, Restrições e Validações** | |
| 1. Não é permitida a entrega de EPI com CA vencido. | 2. Todo empréstimo deve conter data, hora e responsável pela entrega. |
| **Cenário Alternativo I - Registro de devolução de EPI** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Acessar lista de EPIs em posse do colaborador | |
| 2. Selecionar o item devolvido e informar o estado (apto, manutenção ou descarte) | |
| | 3. Encerrar o empréstimo, atualizar histórico e creditar item ao estoque/manutenção |
| **Cenário Alternativo II - Empréstimo temporário para visitante ou terceirizado** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar modalidade de empréstimo para visitante avulso | |
| 2. Preencher nome, documento e empresa do visitante | |
| 3. Selecionar kit básico de EPIs (óculos, protetor, capacete) e confirmar entrega | |
| | 4. Gerar termo de cautela temporário vinculado ao visitante |
| **Cenário de Exceção I - Colaborador com treinamento obrigatório vencido** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar entregar EPI que exige treinamento normativo específico | |
| | 2. Detectar certificado vencido do colaborador |
| | 3. Bloquear a entrega e exibir mensagem de impedimento normativo |
| **Cenário de Exceção II - Falta de saldo em estoque** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Selecionar modelo de EPI com estoque físico zerado | |
| | 2. Detectar indisponibilidade de saldo do item |
| | 3. Bloquear a entrega e sugerir modelo alternativo homologado com a mesma classe de proteção |
| **Cenário de Exceção III - Alerta de item duplicado em posse ativa** | |
| **Ações do Ator** | **Ações do Sistema** |
| 1. Tentar emprestar segundo EPI do mesmo tipo a um colaborador que já possui um ativo | |
| | 2. Detectar duplicidade de cautela sem registro de devolução anterior |
| | 3. Exibir alerta de duplicidade e exigir confirmação/justificativa de substituição |

