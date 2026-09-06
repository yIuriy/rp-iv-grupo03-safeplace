# Especificação do MVP e Arquitetura

## 1. Objetivo

Este documento define o escopo da primeira entrega do SafePlace e relaciona requisitos, casos de uso e decisões arquiteturais. Ele funciona como um índice de entrega: as descrições completas continuam nos documentos de origem.

## 2. Documentos de referência

- [Requisitos funcionais](../requisitos/requisitos-funcionais.md)
- [Requisitos não funcionais](../requisitos/requisitos-nao-funcionais.md)
- [Priorização MoSCoW](../requisitos/priorizacao-moscow.md)
- [Casos de uso](../casos-de-uso/casos-de-uso.md)
- [Especificação arquitetural](../arquitetura/especificacao-arquitetural.md)

## 3. Termos usados na entrega

MVP, MVC e Arquitetura Hexagonal representam decisões diferentes:

- MVP define o conjunto mínimo de funcionalidades da entrega.
- MVC organiza a interface em Model, View e Controller.
- Arquitetura Hexagonal separa o núcleo de negócio das tecnologias externas por meio de portas e adaptadores.

O MVC pode ser usado no adaptador web da Arquitetura Hexagonal. A escolha de um não substitui os demais.

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
| RF16 | Registrar incidentes | Deve ter | Permitir o registro de incidentes pelos perfis definidos no requisito. |
| RF21 | Gerenciar ciclo de vida e substituição inteligente de EPIs | Deve ter | Calcular a previsão de substituição dos EPIs e gerar alertas preventivos. |
| RF23 | Gerenciar supervisores e colaboradores | Deve ter | Gerenciar supervisores e colaboradores conforme o perfil do ator. |

### 5.2. Requisitos não funcionais incluídos

| ID | Requisito | Prioridade | Evidência esperada |
| --- | --- | --- | --- |
| RNF03 | Controle de acesso por perfil | Deve ter | Testes de permissão para Gestor, Supervisor e Colaborador. |
| RNF05 | Rastreabilidade de ações (auditoria) | Deve ter | Evidência de log imutável e da política de retenção mínima de 5 anos. |
| RNF08 | Funcionamento offline parcial | Deve ter | Demonstração da consulta offline a informações previamente sincronizadas. |
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
| UC09 | Relato de acidente ou incidente, sem anexos. |
| UC10 | Planejamento da substituição de EPIs com base no ciclo de vida. |
| UC11 | Classificação e consulta do nível de periculosidade das tarefas. |
| UC12 | Empréstimo e devolução de EPIs para colaboradores. |
| UC13 | Gestão de supervisores e colaboradores. |

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

- gerenciar usuários;
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

Deve conter apenas as classes necessárias aos requisitos `Deve ter`: usuário e perfil, ocorrência, plano de ação, EPI, estoque, movimentação, manutenção, empréstimo, projeção de substituição, área de risco, tarefa, classificação de periculosidade, certificação, treinamento e auditoria.

Como proposta de modelagem, o grupo pode separar `EPI`, que representa o tipo de equipamento, de `ItemEPI`, que representa uma unidade física rastreável. Essa proposta não cria um requisito novo.

## 9. Matriz de rastreabilidade

| ID | Requisito | Caso de uso | Elemento arquitetural | Situação |
| --- | --- | --- | --- | --- |
| RF01 | Gerenciar acidentes causados por um funcionário | UC05 | Gestão de Ocorrências | Coberto pelo registro e gerenciamento de acidentes relacionados a fatores humanos. |
| RF02 | Gerenciar acidentes causados por falha de equipamento de segurança | UC05 | Gestão de Ocorrências e Gestão de EPIs | Coberto pelo registro de acidentes relacionados a falhas de equipamentos de segurança. |
| RF03 | Controlar o estoque dos EPIs | UC06 | Gestão de EPIs, Estoque e Persistência | Coberto pelo fluxo básico. |
| RF04 | Controlar a manutenção dos EPIs | UC01 | Gestão de EPIs, Manutenção e Persistência | Fluxo ajustado para registrar manutenção. |
| RF05 | Mapear as áreas de risco do ambiente de trabalho | UC03 | Gestão de Áreas de Risco e Persistência | Coberto. |
| RF06 | Classificar o nível de periculosidade da tarefa | UC11 | Gestão de Tarefas | Coberto pela classificação de periculosidade. |
| RF07 | Informar o plano de ação para cada tipo de acidente | UC07 | Gestão de Ocorrências | Coberto pela definição de planos vinculados às ocorrências. |
| RF10 | Controlar vencimento de certificações e treinamentos obrigatórios | UC02 | Gestão de Capacitações | Coberto pelo controle de certificações e treinamentos. |
| RF11 | Controlar a rastreabilidade dos EPIs | UC12 | Gestão de EPIs, Empréstimo e Estoque | Recorte limitado a colaboradores. |
| RF13 | Criar registros de acidente | UC09 e UC05 | Gestão de Ocorrências | Coberto. |
| RF16 | Registrar incidentes | UC09 e UC05 | Gestão de Ocorrências | Atores alinhados ao requisito. |
| RF21 | Gerenciar ciclo de vida e substituição inteligente de EPIs | UC10 | Gestão de EPIs | Coberto pelo planejamento de substituição com base no ciclo de vida. |
| RF23 | Gerenciar supervisores e colaboradores | UC13 | Gestão de Usuários e Autorização | UC13 proposto para fechar a lacuna. |
| RNF03 | Controle de acesso por perfil | UCs do MVP | Autenticação e Autorização | Exige teste por perfil. |
| RNF05 | Rastreabilidade de ações (auditoria) | UCs com alteração | Auditoria | Decisão obrigatória no MVP. |
| RNF08 | Funcionamento offline parcial | UCs de consulta | Cache local e Sincronização | Exige demonstração sem conexão. |
| RNF11 | Interface responsiva e acessível | UCs do MVP | Interface Web | Exige verificação responsiva e de acessibilidade. |
| RNF15 | Documentação técnica | Todos | Documentação versionada | Parcial; API e modelo de dados dependem da implementação. |

## 10. Critérios de aceite da documentação

A documentação da entrega estará pronta quando:

1. todos os requisitos `Deve ter` apontarem para pelo menos um caso de uso;
2. todos os casos de uso do MVP apontarem para um módulo arquitetural;
3. requisitos fora do MVP não aparecerem como funcionalidades da primeira entrega;
4. sensores e simulações não aparecerem na arquitetura vigente;
5. os cinco diagramas forem atualizados conforme a seção 8;
6. a equipe registrar evidências para os RNFs do MVP.

O item 5 permanece pendente nesta revisão por decisão do grupo.
