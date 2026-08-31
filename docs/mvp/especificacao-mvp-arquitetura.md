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

O MVP contém os requisitos classificados como `Must have`. Itens `Should have` e `Could have` permanecem no backlog. O RF21, classificado como `Won't have`, está explicitamente fora da versão atual.

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

| ID | Prioridade | Justificativa |
| --- | --- | --- |
| RF01 | Should have | Complementa o registro de acidentes, mas pode ser tratado como uma classificação após o cadastro principal. |
| RF02 | Should have | Complementa a análise do acidente, mas depende do registro principal e do controle de EPIs. |
| RF03 | Must have | Sustenta o controle básico dos equipamentos de proteção e é parte central do sistema. |
| RF04 | Must have | Permite acompanhar o estado de uso e conservação dos EPIs. |
| RF05 | Must have | Permite relacionar acidentes, incidentes e inspeções ao contexto de risco do ambiente. |
| RF06 | Should have | Ajuda na análise preventiva, mas pode ser implementado após o cadastro das áreas e ocorrências. |
| RF07 | Should have | É importante para a resposta aos acidentes, mas pode evoluir após a estrutura inicial de registros. |
| RF08 | Could have | Exige regras automáticas e análise de histórico, podendo ser entregue em etapa posterior. |
| RF09 | Should have | Fortalece o acompanhamento preventivo, mas pode ser entregue após o mapeamento das áreas. |
| RF10 | Could have | É relevante para prevenção, mas amplia o escopo para a gestão de capacitações. |
| RF11 | Must have | Permite saber quais EPIs estão disponíveis, em uso ou pendentes de devolução. |
| RF12 | Should have | Melhora a consistência do uso de EPIs, mas depende do cadastro de tarefas e equipamentos. |
| RF13 | Must have | Representa uma funcionalidade central para o acompanhamento de acidentes de trabalho. |
| RF14 | Should have | Complementa o ciclo de vida dos EPIs, mas pode ser entregue após estoque, manutenção e rastreabilidade. |
| RF15 | Could have | Aprofunda a análise do acidente, mas pode ser simplificado ou adiado na primeira versão. |
| RF16 | Must have | Permite acompanhar situações de risco antes que gerem acidentes, fortalecendo a prevenção. |
| RF17 | Could have | Depende dos dados já cadastrados e pode ser implementado após os fluxos principais. |
| RF18 | Could have | Depende da consolidação dos dados e pode ser implementado depois do fluxo principal. |
| RF19 | Could have | É importante, mas exige maior cuidado com regras legais e dados padronizados. |
| RF20 | Should have | Apoia o controle dos EPIs, mas pode ser entregue após o cadastro e a rastreabilidade dos equipamentos. |
| RF21 | Won't have | Exige cálculo preditivo, regras de uso e alertas de compra, ficando fora da versão inicial. |
| RF22 | Could have | É útil para segurança, mas não é essencial para o fluxo principal de acidentes e EPIs. |
| RF23 | Must have | É necessário para controlar usuários e responsabilidades nos fluxos principais. |

#### 4.2.2. Requisitos não funcionais

| ID | Prioridade | Justificativa |
| --- | --- | --- |
| RNF01 | Must have | Os fluxos principais precisam responder em tempo adequado para serem úteis aos usuários. |
| RNF02 | Should have | A capacidade de usuários é importante, mas o MVP acadêmico pode começar com uma carga menor e controlada. |
| RNF03 | Must have | O controle por perfil protege as funções e os dados conforme a responsabilidade de cada usuário. |
| RNF04 | Must have | O MVP registra acidentes e incidentes, que podem conter dados sensíveis e precisam de proteção desde a primeira versão. |
| RNF05 | Must have | As alterações em ocorrências, EPIs e usuários precisam ser rastreáveis para preservar a confiabilidade dos registros. |
| RNF06 | Should have | A disponibilidade é importante, mas não é o foco principal da demonstração acadêmica. |
| RNF07 | Could have | Backup e recuperação são necessários para um ambiente real, mas podem ser tratados após a validação do núcleo do MVP. |
| RNF08 | Could have | O modo offline depende de sincronização e armazenamento local, não sendo essencial para os fluxos iniciais. |
| RNF09 | Must have | As regras de segurança do trabalho devem orientar o domínio e os fluxos principais do sistema. |
| RNF10 | Should have | Torna-se prioritário quando o sistema passar a gerar documentos oficiais e legais. |
| RNF11 | Should have | É importante para a qualidade da interface, mas pode ser refinado após a implementação dos fluxos principais. |
| RNF12 | Must have | As tarefas essenciais devem ser compreendidas por usuários com conhecimento básico de informática. |
| RNF13 | Could have | O suporte a outros idiomas amplia o alcance, mas não é necessário para validar o objetivo do MVP. |
| RNF14 | Must have | O desacoplamento permite organizar o projeto e evoluir os módulos sem modificar as regras centrais. |
| RNF15 | Must have | A documentação técnica é parte da própria entrega e permite compreender e manter o projeto. |
| RNF16 | Must have | A compatibilidade definida evita que os fluxos principais dependam de um ambiente específico. |
| RNF17 | Could have | Integrações externas podem ser analisadas após a validação das funcionalidades internas do MVP. |

## 5. Escopo do MVP

### 5.1. Requisitos funcionais incluídos

| Requisito | Prioridade | Resultado esperado no MVP |
| --- | --- | --- |
| RF03 | Must have | Consultar estoque e registrar entradas e saídas de EPIs. |
| RF04 | Must have | Consultar e registrar manutenção de EPIs. |
| RF05 | Must have | Cadastrar, consultar e atualizar áreas de risco. |
| RF11 | Must have | Registrar empréstimos, devoluções e itens pendentes. |
| RF13 | Must have | Permitir que o Supervisor registre acidentes. |
| RF16 | Must have | Permitir o registro de incidentes pelos perfis definidos no requisito. |
| RF23 | Must have | Gerenciar supervisores e colaboradores conforme o perfil do ator. |

### 5.2. Requisitos não funcionais incluídos

| Requisito | Prioridade | Evidência esperada |
| --- | --- | --- |
| RNF01 | Must have | Teste dos tempos de resposta dos fluxos principais. |
| RNF03 | Must have | Testes de permissão para Gestor, Supervisor e Colaborador. |
| RNF04 | Must have | Evidência de TLS 1.3 na transmissão e AES-256 no armazenamento. |
| RNF05 | Must have | Evidência de log imutável e da política de retenção mínima de 5 anos. |
| RNF09 | Must have | Regras normativas identificadas nos casos de uso e no domínio. |
| RNF12 | Must have | Teste das tarefas principais com usuários do público esperado. |
| RNF14 | Must have | Dependências do núcleo separadas por portas e adaptadores. |
| RNF15 | Must have | Arquitetura, API e modelo de dados versionados no repositório. |
| RNF16 | Must have | Verificação nos navegadores e sistemas definidos no requisito. |

### 5.3. Casos de uso incluídos

| Caso de uso | Recorte do MVP |
| --- | --- |
| UC01 | Consulta e registro de manutenção. |
| UC03 | Cadastro, consulta e atualização de áreas de risco. |
| UC05 | Registro, consulta, atualização e arquivamento de ocorrências. |
| UC06 | Entrada, saída, saldo e histórico de estoque. |
| UC09 | Relato de acidente ou incidente, sem anexos. |
| UC12 | Empréstimo e devolução de EPIs para colaboradores. |
| UC13 | Gestão de supervisores e colaboradores. |

## 6. Fora do escopo

Os seguintes grupos permanecem fora do MVP:

- classificação de causas, periculosidade, planos de ação, inspeções e vínculo entre tarefas e EPIs: RF01, RF02, RF06, RF07, RF09 e RF12;
- alertas de comportamento, treinamentos, investigação, dashboards, relatórios, CAT e visitantes: RF08, RF10, RF15, RF17, RF18, RF19 e RF22;
- descarte, fornecedores e Certificado de Aprovação: RF14 e RF20;
- substituição inteligente de EPIs: RF21;
- funcionamento offline, suporte multilíngue e integração externa: RNF08, RNF13 e RNF17.

Sensores, IoT, rotas de evacuação e simulações de emergência pertencem a uma versão antiga do projeto. Esses itens não fazem parte do backlog atual porque não possuem requisito vigente.

Os cenários de anexos, testemunhas, investigação, CAT, visitantes e validações automáticas continuam registrados nos casos de uso, mas não devem ser implementados como parte do MVP.

### 6.1. Condição para uso com dados reais

O MVP acadêmico pode ser demonstrado com dados sintéticos. Antes de um piloto com dados reais, o grupo deve reavaliar RNF06 e RNF07, referentes à disponibilidade, backup e recuperação. Essa condição é uma proposta de entrega e não altera os requisitos existentes.

## 7. Visão arquitetural

O sistema usa Arquitetura Hexagonal. As regras de negócio ficam no núcleo, enquanto interface, autenticação, persistência e auditoria ficam nos adaptadores.

O núcleo é dividido pelas funcionalidades `usuarios`, `ocorrencias`, `epis` e `areas-de-risco`. A interface web pode aplicar MVC dentro do adaptador de entrada.

### 7.1. Portas de entrada esperadas

- gerenciar usuários;
- registrar e consultar ocorrências;
- controlar estoque;
- controlar manutenção;
- controlar empréstimos;
- gerenciar áreas de risco.

### 7.2. Portas de saída esperadas

- persistir usuários, ocorrências, EPIs e áreas de risco;
- autenticar e autorizar usuários;
- registrar auditoria;
- proteger dados sensíveis.

## 8. Artefatos arquiteturais pendentes

Os diagramas não são criados nem alterados nesta revisão. Esta seção define o que cada artefato deverá mostrar quando o grupo fizer a atualização.

### 8.1. Diagrama de pacotes

Deve mostrar `domain`, `application`, portas de entrada, portas de saída e adaptadores. Dentro do núcleo, deve separar `usuarios`, `ocorrencias`, `epis` e `areas-de-risco`. As setas de dependência devem apontar para o núcleo.

### 8.2. Diagrama de componentes lógico

Deve mostrar Interface Web, Autenticação e Autorização, Gestão de Usuários, Gestão de Ocorrências, Gestão de EPIs, Gestão de Áreas de Risco, Persistência e Auditoria. As portas devem aparecer como interfaces entre o núcleo e os adaptadores.

### 8.3. Diagrama de componentes executável

Deve mostrar os elementos que podem ser executados ou implantados: navegador, aplicação SafePlace, banco de dados e os mecanismos de segurança necessários. Portas não devem aparecer como executáveis independentes.

### 8.4. Diagrama de sequência

O fluxo recomendado é o registro de acidente ou incidente. O diagrama deve incluir ator, interface, Controller, porta de entrada, caso de uso, domínio, repositório e auditoria. O fluxo termina com a devolução do protocolo ao usuário.

### 8.5. Diagrama de classes reduzido ao MVP

Deve conter apenas as classes necessárias aos requisitos `Must have`: usuário e perfil, ocorrência, EPI, estoque, movimentação, manutenção, empréstimo, área de risco e auditoria.

Como proposta de modelagem, o grupo pode separar `EPI`, que representa o tipo de equipamento, de `ItemEPI`, que representa uma unidade física rastreável. Essa proposta não cria um requisito novo.

## 9. Matriz de rastreabilidade

| Requisito | Caso de uso | Elemento arquitetural | Situação |
| --- | --- | --- | --- |
| RF03 | UC06 | Gestão de EPIs, Estoque e Persistência | Coberto pelo fluxo básico. |
| RF04 | UC01 | Gestão de EPIs, Manutenção e Persistência | Fluxo ajustado para registrar manutenção. |
| RF05 | UC03 | Gestão de Áreas de Risco e Persistência | Coberto. |
| RF11 | UC12 | Gestão de EPIs, Empréstimo e Estoque | Recorte limitado a colaboradores. |
| RF13 | UC09 e UC05 | Gestão de Ocorrências | Coberto. |
| RF16 | UC09 e UC05 | Gestão de Ocorrências | Atores alinhados ao requisito. |
| RF23 | UC13 | Gestão de Usuários e Autorização | UC13 proposto para fechar a lacuna. |
| RNF01 | UCs do MVP | Interface, Aplicação e Persistência | Exige teste de desempenho. |
| RNF03 | UCs do MVP | Autenticação e Autorização | Exige teste por perfil. |
| RNF04 | UC05 e UC09 | HTTPS, Criptografia e Persistência | Decisão obrigatória no MVP. |
| RNF05 | UCs com alteração | Auditoria | Decisão obrigatória no MVP. |
| RNF09 | UCs do MVP | Regras de Domínio | Exige validação normativa. |
| RNF12 | UCs do MVP | Interface Web | Exige teste de usabilidade. |
| RNF14 | UCs do MVP | Núcleo, Portas e Adaptadores | Atendido pelo desenho arquitetural. |
| RNF15 | Todos | Documentação versionada | Parcial; API e modelo de dados dependem da implementação. |
| RNF16 | UCs do MVP | Interface Web e implantação | Exige matriz de compatibilidade. |

## 10. Critérios de aceite da documentação

A documentação da entrega estará pronta quando:

1. todos os requisitos `Must have` apontarem para pelo menos um caso de uso;
2. todos os casos de uso do MVP apontarem para um módulo arquitetural;
3. RF21 não aparecer como funcionalidade do MVP;
4. sensores e simulações não aparecerem na arquitetura vigente;
5. os cinco diagramas forem atualizados conforme a seção 8;
6. a equipe registrar evidências para os RNFs do MVP.

O item 5 permanece pendente nesta revisão por decisão do grupo.
