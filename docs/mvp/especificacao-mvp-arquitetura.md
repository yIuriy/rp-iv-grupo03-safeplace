# Especificação do MVP e Arquitetura

## 1. Objetivo

Este documento define o escopo da primeira entrega do SafePlace e relaciona requisitos, casos de uso e decisões arquiteturais. Ele funciona como um índice de entrega: as descrições completas continuam nos documentos de origem.

## 2. Documentos de referência

- [Requisitos funcionais](../requisitos/requisitos-funcionais.md)
- [Requisitos não funcionais](../requisitos/requisitos-nao-funcionais.md)
- [Priorização MoSCoW](../requisitos/priorizacao-moscow.md)
- [Casos de uso](../casos-de-uso/casos-de-uso.md)
- [Histórias de usuário](../historias-de-usuario.md)
- [Glossário](../glossario.md)
- [Especificação arquitetural](../arquitetura/especificacao-arquitetural.md)
- [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png)

## 3. Termos usados na entrega

MVP, MVC e Arquitetura Hexagonal representam decisões diferentes:

- MVP define o conjunto mínimo de funcionalidades da entrega.
- MVC organiza a interface em Model, View e Controller.
- Arquitetura Hexagonal separa o núcleo de negócio das tecnologias externas por meio de portas e adaptadores.

O MVC pode ser usado no adaptador web da Arquitetura Hexagonal. A escolha de um não substitui os demais.

### 3.1. Pessoas cadastradas, fluxo de acesso e ausência de autocadastro

O SafePlace é uma plataforma corporativa fechada de uso estritamente interno. **Não existe tela de autocadastro público na interface**: o ponto de entrada da aplicação é exclusivamente a tela de **Login** (`POST /api/auth/login`), autenticando via `email` e `senha` com emissão de token JWT (validade de 7 dias no MVP).

O primeiro acesso administrativo (Gestor de Segurança) é provisionado por meio de carga inicial (*seed*) no banco de dados. A partir desse usuário, o provisionamento é realizado de forma estritamente interna e hierárquica respeitando o modelo RBAC:

- **Gestor de Segurança**: perfil com acesso ao sistema, autentica-se com e-mail e senha. Gerencia as contas de Supervisores e outros gestores. No cadastro de um Supervisor, o sistema gera automaticamente sua credencial inicial.
- **Supervisor**: perfil com acesso ao sistema, autentica-se com e-mail e senha gerada. Gerencia os cadastros de colaboradores e os registros operacionais sob sua responsabilidade (ocorrências, empréstimos de EPIs). Não possui permissão para cadastrar outros supervisores ou gestores.
- **Colaborador**: não recebe conta de acesso, credencial ou senha, não possuindo permissão de autenticação. Seu registro existe para vinculação aos registros operacionais (ocorrências, treinamentos e empréstimos de EPIs). Seus relatos são registrados no sistema pelo Supervisor responsável.

Essa definição segue RF16, RF23, RNF03 e a decisão registrada na [issue #81](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/81). O cadastro de uma pessoa e sua eventual presença no diagrama de classes não concedem permissão de acesso.

## 4. Critério de escopo

O MVP contém os requisitos classificados como `Deve ter`. Itens `Deveria ter` e `Poderia ter` permanecem no backlog. Requisitos classificados como `Não terá agora` ficam explicitamente fora da versão atual.

A prioridade indica quando o requisito será entregue, não se ele é válido. Por isso, requisitos fora do MVP continuam documentados.

### 4.1. Critérios da Técnica MoSCoW

| Classificação | Significado em inglês | Critério de uso |
| --- | --- | --- |
| Deve ter | Must have | Requisito indispensável para o sistema cumprir seu objetivo principal. |
| Deveria ter | Should have | Requisito importante, mas que pode ser ajustado ou entregue após os itens indispensáveis. |
| Poderia ter | Could have | Requisito desejável, com menor impacto caso não seja implementado inicialmente. |
| Não terá agora | Won't have | Requisito fora do escopo da versão atual, mas que pode ser retomado futuramente. |

### 4.2. Justificativa da priorização

A priorização considera três fatores: a contribuição direta do requisito para o objetivo do SafePlace, as dependências entre funcionalidades e o risco de entregar um MVP sem os controles básicos de segurança e rastreabilidade. As justificativas abaixo preservam a fundamentação da priorização registrada pelo grupo e explicam sua relação com o escopo desta entrega.

#### 4.2.1. Requisitos funcionais

| ID | Requisito | Prioridade | Justificativa |
| --- | --- | --- | --- |
| RF01 | Gerenciar acidentes causados por um funcionário | Deve ter | Permite identificar acidentes relacionados a fatores humanos, informação essencial para registrar e acompanhar as ocorrências. |
| RF02 | Gerenciar acidentes causados por falha de equipamento de segurança | Deve ter | Permite identificar acidentes relacionados a falhas de equipamentos de segurança, informação essencial para acompanhar as ocorrências. |
| RF03 | Controlar o estoque dos EPIs | Deve ter | Sustenta o controle básico dos equipamentos de proteção e é parte central do sistema. |
| RF04 | Controlar a manutenção dos EPIs | Deve ter | Permite acompanhar o estado de uso e conservação dos EPIs. |
| RF05 | Mapear as áreas de risco do ambiente de trabalho | Deve ter | Permite relacionar acidentes, incidentes e inspeções ao contexto de risco do ambiente. |
| RF06 | Classificar o nível de periculosidade da tarefa | Deve ter | Permite classificar o risco das tarefas antes da execução, apoiando a prevenção e a definição das medidas de segurança. |
| RF07 | Informar o plano de ação para cada tipo de acidente | Deve ter | Garante que cada acidente ou incidente tenha medidas corretivas e preventivas para reduzir novas ocorrências. |
| RF08 | Gerar alertas automáticos por comportamento de risco | Poderia ter | Exige regras automáticas e análise de histórico, podendo ser entregue em etapa posterior. |
| RF09 | Registrar histórico de inspeções periódicas das áreas de risco | Deveria ter | Fortalece o acompanhamento preventivo, mas pode ser entregue após o mapeamento das áreas. |
| RF10 | Controlar vencimento de certificações e treinamentos obrigatórios | Deve ter | Evita que colaboradores com certificações ou treinamentos vencidos sejam alocados em tarefas de risco. |
| RF11 | Controlar a rastreabilidade dos EPIs | Deve ter | Permite saber quais EPIs estão disponíveis, em uso ou pendentes de devolução. |
| RF12 | Conectar tarefas e EPIs | Deveria ter | Melhora a consistência do uso de EPIs, mas depende do cadastro de tarefas e equipamentos. |
| RF13 | Criar registros de acidente | Deve ter | Representa uma funcionalidade central para o acompanhamento de acidentes de trabalho. |
| RF14 | Gerenciar descarte dos EPIs | Deveria ter | Complementa o ciclo de vida dos EPIs, mas pode ser entregue após estoque, manutenção e rastreabilidade. |
| RF15 | Registrar investigação de acidente | Poderia ter | Aprofunda a análise do acidente, mas pode ser simplificado ou adiado na primeira versão. |
| RF16 | Registrar incidentes | Deve ter | Permite acompanhar situações de risco antes que gerem acidentes, fortalecendo a prevenção. |
| RF17 | Gerar dashboards de segurança | Poderia ter | Depende dos dados já cadastrados e pode ser implementado após os fluxos principais. |
| RF18 | Emitir relatórios estatísticos | Poderia ter | Depende da consolidação dos dados e pode ser implementado depois do fluxo principal. |
| RF19 | Gerar documentação legal | Poderia ter | É importante, mas exige maior cuidado com regras legais e dados padronizados. |
| RF20 | Gerenciar fornecedores e Certificado de Aprovação | Deveria ter | Apoia o controle dos EPIs, mas pode ser entregue após o cadastro e a rastreabilidade dos equipamentos. |
| RF21 | Gerenciar ciclo de vida e substituição inteligente de EPIs | Deve ter | Permite antecipar substituições antes do vencimento ou do esgotamento dos EPIs, mantendo a continuidade da proteção. |
| RF22 | Gerenciar visitantes | Deveria ter | É importante para controlar a entrega temporária de EPIs a visitantes, mas pode ser entregue após os fluxos essenciais. |
| RF23 | Gerenciar supervisores e colaboradores | Deve ter | É necessário para controlar usuários e responsabilidades nos fluxos principais. |

#### 4.2.2. Requisitos não funcionais

| ID | Requisito | Prioridade | Justificativa |
| --- | --- | --- | --- |
| RNF01 | Tempo de resposta | Deveria ter | O tempo de resposta é importante, mas pode ser refinado após a validação inicial dos fluxos essenciais. |
| RNF02 | Capacidade de usuários simultâneos | Deveria ter | A capacidade de usuários é importante, mas o MVP acadêmico pode começar com uma carga menor e controlada. |
| RNF03 | Controle de acesso por perfil | Deve ter | O controle por perfil protege as funções e os dados conforme a responsabilidade de cada usuário. |
| RNF04 | Criptografia de dados sensíveis | Deveria ter | A proteção dos dados é importante, mas sua implementação completa pode ser concluída após a validação inicial com dados de teste. |
| RNF05 | Rastreabilidade de ações (auditoria) | Deve ter | As alterações em ocorrências, EPIs e usuários precisam ser rastreáveis para preservar a confiabilidade dos registros. |
| RNF06 | Disponibilidade mínima | Deveria ter | A disponibilidade é importante, mas não é o foco principal da demonstração acadêmica. |
| RNF07 | Backup e recuperação de dados | Poderia ter | Backup e recuperação são necessários para um ambiente real, mas podem ser tratados após a validação do núcleo do MVP. |
| RNF08 | Funcionamento offline parcial | Deve ter | Garante a consulta de informações previamente sincronizadas em ambientes com acesso instável à internet. |
| RNF09 | Conformidade com normas regulamentadoras | Deveria ter | A conformidade é importante, mas a validação normativa completa pode ser aprofundada após os fluxos essenciais. |
| RNF10 | Integridade e validade dos documentos gerados | Deveria ter | Torna-se prioritário quando o sistema passar a gerar documentos oficiais e legais. |
| RNF11 | Interface responsiva e acessível | Deve ter | Permite utilizar os fluxos essenciais em diferentes dispositivos e por pessoas com necessidades de acessibilidade. |
| RNF12 | Facilidade de aprendizado | Deveria ter | A facilidade de aprendizado é importante, mas sua avaliação formal pode ocorrer após a implementação dos fluxos essenciais. |
| RNF13 | Suporte multilíngue | Poderia ter | O suporte a outros idiomas amplia o alcance, mas não é necessário para validar o objetivo do MVP. |
| RNF14 | Modularidade e extensibilidade | Deveria ter | A modularidade facilita a evolução do sistema, mas pode ser refinada após a validação inicial das funcionalidades. |
| RNF15 | Documentação técnica | Deve ter | A documentação técnica é parte da própria entrega e permite compreender e manter o projeto. |
| RNF16 | Compatibilidade com navegadores e sistemas operacionais | Deveria ter | A compatibilidade ampla é importante, mas pode ser ampliada após a validação no ambiente inicial. |
| RNF17 | Integração via API | Poderia ter | Integrações externas podem ser analisadas após a validação das funcionalidades internas do MVP. |

## 5. Escopo do MVP

### 5.1. Requisitos funcionais incluídos

| ID | Requisito | Prioridade | Resultado esperado no MVP |
| --- | --- | --- | --- |
| RF01 | Gerenciar acidentes causados por um funcionário | Deve ter | Registrar, consultar e atualizar acidentes relacionados a fatores humanos. |
| RF02 | Gerenciar acidentes causados por falha de equipamento de segurança | Deve ter | Registrar, consultar e atualizar acidentes relacionados a falhas de equipamentos de segurança. |
| RF03 | Controlar o estoque dos EPIs | Deve ter | Consultar estoque e registrar entradas e saídas de EPIs. |
| RF04 | Controlar a manutenção dos EPIs | Deve ter | Consultar e registrar manutenção de EPIs. |
| RF05 | Mapear as áreas de risco do ambiente de trabalho | Deve ter | Cadastrar, consultar e atualizar áreas de risco. |
| RF06 | Classificar o nível de periculosidade da tarefa | Deve ter | Classificar o nível de periculosidade das tarefas. |
| RF07 | Informar o plano de ação para cada tipo de acidente | Deve ter | Definir e acompanhar planos de ação vinculados a acidentes e incidentes. |
| RF10 | Controlar vencimento de certificações e treinamentos obrigatórios | Deve ter | Controlar vencimentos de certificações e treinamentos obrigatórios. |
| RF11 | Controlar a rastreabilidade dos EPIs | Deve ter | Registrar empréstimos, devoluções e itens pendentes. |
| RF13 | Criar registros de acidente | Deve ter | Permitir que o Supervisor registre acidentes. |
| RF16 | Registrar incidentes | Deve ter | Permitir registro pelo Supervisor ou Gestor. Relatos comunicados pelo Colaborador são registrados pelo Supervisor. |
| RF21 | Gerenciar ciclo de vida e substituição inteligente de EPIs | Deve ter | Calcular a previsão de substituição dos EPIs e gerar alertas preventivos. |
| RF23 | Gerenciar supervisores e colaboradores | Deve ter | Gestor gerencia supervisores com conta e senha inicial automática; Supervisor gerencia colaboradores sem conta ou senha, preservando seus vínculos. |

### 5.2. Requisitos não funcionais incluídos

| ID | Requisito | Prioridade | Evidência esperada |
| --- | --- | --- | --- |
| RNF03 | Controle de acesso por perfil | Deve ter | Testes de permissão para Gestor e Supervisor, além de verificação de que o cadastro de Colaborador não cria credenciais nem permite autenticação. |
| RNF05 | Rastreabilidade de ações (auditoria) | Deve ter | Evidência de log imutável e da política de retenção mínima de 5 anos. |
| RNF08 | Funcionamento offline parcial | Deve ter | Demonstração de consulta sem conexão após definir os dados disponíveis e resolver a dependência com indicadores e relatórios fora do MVP. Envio offline de relatos ainda pendente. |
| RNF11 | Interface responsiva e acessível | Deve ter | Verificação em dispositivos desktop, tablet e móvel, com avaliação das diretrizes WCAG 2.1 AA. |
| RNF15 | Documentação técnica | Deve ter | Arquitetura, API e modelo de dados versionados no repositório. |

### 5.3. Casos de uso incluídos

| Caso de uso | Recorte do MVP |
| --- | --- |
| UC01 | Consulta e registro de manutenção. |
| UC02 | Consulta de certificações e treinamentos, com controle de vencimentos. |
| UC03 | Cadastro, consulta e atualização de áreas de risco. |
| UC05 | Registro, consulta, atualização e arquivamento de ocorrências. |
| UC06 | Entrada, saída, saldo e histórico de estoque. |
| UC07 | Definição e acompanhamento de planos de ação. |
| UC09 | Relato de acidente ou incidente pelo Supervisor, sem anexos. Envio offline pendente de decisão. |
| UC10 | Previsão de substituição e alertas preventivos. Requisições de compra e parâmetros de cálculo pendentes de definição. |
| UC11 | Classificação e consulta do nível de periculosidade das tarefas. |
| UC12 | Empréstimo e devolução de EPIs para colaboradores. |
| UC13 (proposto) | Gestão de supervisores e colaboradores prevista em RF23. O fluxo ainda não está especificado no catálogo textual. |

A inclusão de um UC nesta tabela indica escopo previsto, não implementação concluída. UC13 ainda não conta como cobertura textual completa de RF23. As pendências que impedem fechar a revisão estão na seção 11.

## 6. Fora do escopo

Os seguintes grupos permanecem fora do MVP:

- inspeções e vínculo entre tarefas e EPIs: RF09 e RF12;
- alertas de comportamento, investigação, dashboards, relatórios, CAT e visitantes: RF08, RF15, RF17, RF18, RF19 e RF22;
- descarte, fornecedores e Certificado de Aprovação: RF14 e RF20;
- tempo de resposta, capacidade de usuários, criptografia, disponibilidade, backup, conformidade normativa, integridade de documentos, facilidade de aprendizado, suporte multilíngue, modularidade, compatibilidade ampla e integração externa: RNF01, RNF02, RNF04, RNF06, RNF07, RNF09, RNF10, RNF12, RNF13, RNF14, RNF16 e RNF17.

Sensores, IoT, rotas de evacuação e simulações de emergência pertencem a uma versão antiga do projeto. Esses itens não fazem parte do backlog atual porque não possuem requisito vigente.

Os cenários de anexos, testemunhas, investigação, CAT e visitantes continuam registrados nos casos de uso, mas não devem ser implementados como parte do MVP.

### 6.1. Condição para uso com dados reais

O MVP acadêmico pode ser demonstrado com dados sintéticos. Antes de um piloto com dados reais, o grupo deve reavaliar RNF06 e RNF07, referentes à disponibilidade, backup e recuperação. Essa condição é uma proposta de entrega e não altera os requisitos existentes.

## 7. Visão arquitetural

O sistema usa Arquitetura Hexagonal. As regras de negócio ficam no núcleo, enquanto interface, autenticação, persistência e auditoria ficam nos adaptadores.

O núcleo é dividido pelas funcionalidades `usuarios`, `ocorrencias`, `epis`, `areas-de-risco`, `tarefas` e `capacitacoes`. A interface web pode aplicar MVC dentro do adaptador de entrada.

### 7.1. Portas de entrada esperadas

- gerenciar contas de supervisores e cadastros de colaboradores sem acesso;
- registrar e consultar ocorrências;
- controlar estoque;
- controlar manutenção;
- controlar empréstimos;
- gerenciar áreas de risco;
- classificar o nível de periculosidade das tarefas;
- definir e acompanhar planos de ação;
- controlar certificações e treinamentos;
- gerenciar o ciclo de vida e planejar a substituição dos EPIs.

### 7.2. Portas de saída esperadas

- persistir usuários, ocorrências, EPIs, áreas de risco, tarefas, certificações e treinamentos;
- autenticar e autorizar usuários;
- registrar auditoria;
- proteger dados sensíveis.

## 8. Artefatos arquiteturais pendentes

Os diagramas não são criados nem alterados nesta revisão. Esta seção define o que cada artefato deverá mostrar quando o grupo fizer a atualização.

### 8.1. Diagrama de pacotes

Deve mostrar `domain`, `application`, portas de entrada, portas de saída e adaptadores. Dentro do núcleo, deve separar `usuarios`, `ocorrencias`, `epis`, `areas-de-risco`, `tarefas` e `capacitacoes`. As setas de dependência devem apontar para o núcleo.

### 8.2. Diagrama de componentes lógico

Deve mostrar Interface Web, Autenticação e Autorização, Gestão de Usuários, Gestão de Ocorrências, Gestão de EPIs, Gestão de Áreas de Risco, Gestão de Tarefas, Gestão de Capacitações, Persistência e Auditoria. As portas devem aparecer como interfaces entre o núcleo e os adaptadores.

### 8.3. Diagrama de componentes executável

Deve mostrar os elementos que podem ser executados ou implantados: navegador, aplicação SafePlace, banco de dados e os mecanismos de segurança necessários. Portas não devem aparecer como executáveis independentes.

### 8.4. Diagrama de sequência

O fluxo recomendado é o registro de acidente ou incidente. O diagrama deve incluir ator, interface, Controller, porta de entrada, caso de uso, domínio, repositório e auditoria. O fluxo termina com a devolução do protocolo ao usuário.

### 8.5. Diagrama de classes reduzido ao MVP

Deve conter apenas as classes necessárias aos requisitos `Deve ter`: usuário e perfil de acesso, colaborador cadastrado sem acesso, ocorrência, plano de ação, EPI, estoque, movimentação, manutenção, empréstimo, projeção de substituição, área de risco, tarefa, classificação de periculosidade, certificação, treinamento e auditoria.

Como proposta de modelagem, o grupo pode separar `EPI`, representando o tipo de equipamento nessa proposta, de `ItemEPI`, representando uma unidade física rastreável. Essa separação não está aprovada e não define o significado de `EPI` no modelo vigente, que ainda precisa ser esclarecido. A proposta não cria um requisito novo.

## 9. Matriz de rastreabilidade

As situações abaixo descrevem a cobertura documental, não o atendimento pela implementação. A base técnica existente e seus limites estão na [especificação arquitetural](../arquitetura/especificacao-arquitetural.md#7-base-técnica-existente-e-limites).

| ID | Requisito | Caso de uso | Elemento arquitetural | Situação |
| --- | --- | --- | --- | --- |
| RF01 | Gerenciar acidentes causados por um funcionário | UC05 | Gestão de Ocorrências | Coberto pelo registro e gerenciamento de acidentes relacionados a fatores humanos. |
| RF02 | Gerenciar acidentes causados por falha de equipamento de segurança | UC05 | Gestão de Ocorrências e Gestão de EPIs | Coberto pelo registro de acidentes relacionados a falhas de equipamentos de segurança. |
| RF03 | Controlar o estoque dos EPIs | UC06 | Gestão de EPIs, Estoque e Persistência | Coberto pelo fluxo básico. |
| RF04 | Controlar a manutenção dos EPIs | UC01 | Gestão de EPIs, Manutenção e Persistência | Fluxo ajustado para registrar manutenção. |
| RF05 | Mapear as áreas de risco do ambiente de trabalho | UC03 | Gestão de Áreas de Risco e Persistência | Coberto. |
| RF06 | Classificar o nível de periculosidade da tarefa | UC11 | Gestão de Tarefas | Fluxo descrito; vocabulário dos graus de risco pendente de alinhamento com UC03 e classes. |
| RF07 | Informar o plano de ação para cada tipo de acidente | UC07 | Gestão de Ocorrências | Fluxo descrito; representação das ações e notificação de responsável sem acesso ainda pendentes. |
| RF10 | Controlar vencimento de certificações e treinamentos obrigatórios | UC02 | Gestão de Capacitações | Consulta, alertas e bloqueio descritos; fluxo de cadastro das capacitações ainda não identificado. |
| RF11 | Controlar a rastreabilidade dos EPIs | UC12 | Gestão de EPIs, Empréstimo e Estoque | Recorte limitado a colaboradores. |
| RF13 | Criar registros de acidente | UC09 e UC05 | Gestão de Ocorrências | Coberto. |
| RF16 | Registrar incidentes | UC09 e UC05 | Gestão de Ocorrências | Supervisor registra em UC09, inclusive relatos de colaboradores; Gestor registra em UC05. |
| RF21 | Gerenciar ciclo de vida e substituição inteligente de EPIs | UC10 | Gestão de EPIs | Previsão e alertas no MVP; requisições, origem dos dados e parâmetros de cálculo ainda pendentes. |
| RF23 | Gerenciar supervisores e colaboradores | UC13 proposto; US23 e US24 | Gestão de Usuários e Autorização | Responsabilidades descritas nas histórias e no RF; fluxo de UC13 ainda não especificado. |
| RNF03 | Controle de acesso por perfil | UCs do MVP | Autenticação e Autorização | Exige testes para Gestor e Supervisor e cadastro de Colaborador sem credenciais. |
| RNF05 | Rastreabilidade de ações (auditoria) | UCs com alteração | Auditoria | Decisão obrigatória no MVP. |
| RNF08 | Funcionamento offline parcial | Consulta prevista em RNF08; relação com UC09 pendente | Cache local e Sincronização | Dados, dispositivos e alcance do offline ainda precisam ser definidos antes da demonstração. |
| RNF11 | Interface responsiva e acessível | UCs do MVP | Interface Web | Exige verificação responsiva e de acessibilidade. |
| RNF15 | Documentação técnica | Todos | Documentação versionada | Parcial; há instruções de execução, migrações e OpenAPI para ocorrências e estoque de EPIs. Cobertura dos demais fluxos ainda pendente. |

## 10. Critérios de aceite da documentação

A documentação da entrega estará pronta quando:

1. todos os requisitos `Deve ter` apontarem para pelo menos um caso de uso;
2. todos os casos de uso do MVP apontarem para um módulo arquitetural;
3. requisitos fora do MVP não aparecerem como funcionalidades da primeira entrega;
4. sensores e simulações não aparecerem na arquitetura vigente;
5. os cinco diagramas forem atualizados conforme a seção 8;
6. a equipe registrar evidências para os RNFs do MVP.

O item 5 permanece pendente nesta revisão por decisão do grupo.

## 11. Pendências da revisão documental

Esta revisão cobre parte da [issue #81](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/81). A priorização e suas justificativas foram preservadas: RF21 continua `Deve ter` e RNF04 continua `Deveria ter`. Os pontos abaixo dependem de decisão ou implementação e não são considerados resolvidos pelos ajustes de redação.

### 11.1. Escopo e fluxos

| Ponto | Evidência e definição ainda necessária | Destino |
| --- | --- | --- |
| Gestão de pessoas | RF23 e US23/US24 definem os responsáveis, mas UC13 ainda não possui fluxo. Definir também identificação de acesso, dados cadastrais, entrega da senha inicial e eventual desativação, sem criar conta para Colaborador. | UC13, histórias e issue #74. |
| Offline | RNF08 prevê consulta de estatísticas, indicadores e relatórios pelo Gestor; RF17 e RF18 estão no backlog. UC09 prevê criação e envio posterior de relatos. Definir dados consultáveis, perfis, dispositivos e se haverá criação offline. | RNF08, UC09, US13/US16/US17/US18 e arquitetura. |
| Substituição de EPIs | RF21 prevê projeções e alertas. UC10 acrescenta requisições de compra e prorrogação por laudo. Definir esses desdobramentos, origem dos dados, unidades, fórmula, multiplicadores e antecedência dos avisos. A exportação de relatórios segue no backlog de RF18. | UC10, US21 e issue #76. |
| Cadastro de capacitações | UC02 consulta dados já cadastrados; os modelos também contêm cadastro e validação de datas. Identificar o fluxo responsável por essas operações. | UC02, US10 e revisão dos diagramas na issue #81. |
| Plano de ação | UC07 acompanha ações individuais e admite responsável Colaborador ou setor. Definir sua representação e como notificar o responsável sem conceder acesso ao sistema. | UC07, US07 e issue #39. |
| CA no MVP | UC01, UC06, UC10 e UC12 usam informações de CA, enquanto a gestão de fornecedores e CA (RF20) está no backlog. Esclarecer como os dados necessários serão fornecidos no MVP, preservando a prioridade e as regras existentes. | UCs de EPIs, US03/US04/US11/US20/US21 e issue #76. |

### 11.2. Correspondência com o diagrama de classes

O [PNG de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png) foi consultado como referência, sem alterar o desenho nem o modelo Astah. A [issue #39](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/39) concentra sua revisão. As propostas de dados das histórias continuam propostas.

| Ponto | Divergência preservada para decisão |
| --- | --- |
| Unidade de EPI | US11 acompanha a posse de um equipamento, mas `EPI` possui quantidade e o modelo também tem `ModeloEPI` e `LoteEPI`. Definir se cada registro representa tipo, lote ou unidade física antes de aprovar a proposta `ItemEPI`. |
| Resultado de manutenção | US04 propõe resultado textual; `ManutencaoEPI.resultadoManutencao` é booleano no desenho. Definir resultado, classificação e relação com a situação do EPI. |
| Identificadores | US20 propõe CA textual e `ModeloEPI.ca` é inteiro. `Colaborador.cpf` é texto, mas há operações que recebem CPF inteiro. UC12 usa matrícula, proposta em US24 junto ao setor e aos metadados do cadastro. Confirmar os dados e seus tipos. |
| Grau de risco | UC03 e `NivelPerigo` usam baixo/médio/alto/crítico; UC11 e US06 usam leve/moderado/grave/crítico. Definir uma lista comum ou classificações distintas com correspondência explícita. |
| Ocorrências e rastreabilidade | UC09 exige protocolo para acidente e incidente e distingue data do fato de data/hora do cadastro. O PNG coloca protocolo apenas em `Acidente`. Conferir também envolvidos, EPIs, arquivamento, movimentações, responsáveis e auditoria, conforme os pontos da issue #81. |
| Investigação e CAT | RF15 trata investigação e RF19 trata geração de CAT, ambos no backlog. O modelo usa `CAT` em operações de laudo pericial. Os conceitos permanecem distintos no glossário; os contratos precisam de revisão. |

### 11.3. Dependências com a implementação

| Issue | Alinhamento necessário |
| --- | --- |
| [#74: usuários e perfis](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/74) | O texto consultado ainda inclui `COLABORADOR` como perfil. A implementação deve seguir RF23 e RNF03 revisados, separando conta de acesso de cadastro de pessoa. |
| [#75: ocorrências](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/75) | Distinguir o usuário que registra dos colaboradores envolvidos; preservar a data do fato e identificar o momento do cadastro. Conferir protocolo e acompanhamento com UC05/UC09. |
| [#76: EPIs](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/76) | A base já registra EPIs e movimentações com data/hora e motivo, mas não identifica o operador da movimentação. Conferir unidade rastreável, responsáveis e dados de projeção após as definições acima. |
| [#77: áreas de risco e tarefas](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/77) | Alinhar `NivelPerigo` após a decisão de vocabulário e distinguir proteção para acesso à área do vínculo entre tarefa e EPI. |

Essas dependências são registradas aqui para continuidade do trabalho. O ajuste documental não altera o código nem encerra as issues. Os limites técnicos atuais, incluindo autorização, auditoria e configuração da URL da API, estão na [especificação arquitetural](../arquitetura/especificacao-arquitetural.md#7-base-técnica-existente-e-limites).

### 11.4. Verificação desta revisão

Verificações realizadas em 6 de setembro de 2026:

- Links locais, âncoras, tabelas e blocos de código conferidos; `git diff --check` sem erros. As 40 prioridades, as justificativas da MoSCoW, o código e os diagramas foram preservados.
- Frontend: `npm run lint`, `npm run build` e os 11 testes de `npm test` passaram após a instalação do Chromium exigido pelo Playwright.
- Backend: após incorporar a base de EPIs e o endpoint de disponibilidade da `dev`, os 21 testes de `./mvnw -Dtest=OcorrenciaServiceTest,EpiTest,EpiServiceTest,EpiControllerTest,HealthControllerTest test` passaram, compilando com alvo Java 21 em JDK 25. Cobrem domínio, serviços e controladores, sem validar a persistência JPA.
- A execução com Docker Compose e PostgreSQL, o teste que carrega toda a aplicação e a execução em JDK 21 não foram verificados neste ambiente. Docker e PostgreSQL não estavam instalados. Os passos de execução foram conferidos com os arquivos de configuração; a verificação completa desses passos permanece pendente.
