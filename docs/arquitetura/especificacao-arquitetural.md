# Especificação Arquitetural

## 1. Objetivo

Este documento registra as decisões arquiteturais do SafePlace e delimita a arquitetura da entrega de MVP. O escopo funcional, os itens excluídos e a matriz de rastreabilidade estão em [Especificação do MVP e Arquitetura](../mvp/especificacao-mvp-arquitetura.md).

O SafePlace acompanha acidentes e incidentes de trabalho, áreas de risco e o uso de Equipamentos de Proteção Individual (EPIs). Sensores, Internet das Coisas (IoT), rotas de evacuação e simulações de emergência não fazem parte da versão atual do sistema.

## 2. Conceitos adotados

MVP, MVC e Arquitetura Hexagonal tratam de decisões diferentes:

- MVP define quais funcionalidades serão entregues primeiro.
- MVC organiza a interface em Model, View e Controller.
- Arquitetura Hexagonal organiza as dependências do sistema por meio de portas e adaptadores.

O MVC pode ser usado no adaptador web sem substituir a Arquitetura Hexagonal. Nesse arranjo, os Controllers recebem as ações do usuário e chamam as portas de entrada da aplicação.

## 3. Padrão arquitetural

O SafePlace adota a Arquitetura Hexagonal, também chamada de Ports and Adapters. O objetivo é manter as regras de negócio independentes da interface, do banco de dados e de serviços externos.

A arquitetura é dividida em três partes:

1. Domínio: contém entidades e regras de negócio.
2. Aplicação: coordena os casos de uso e declara as portas.
3. Adaptadores: conectam a aplicação à interface web, à persistência, à autenticação e à auditoria.

As dependências apontam para o núcleo. O domínio não conhece frameworks, banco de dados ou detalhes da interface.

### 3.1. Portas de entrada

As portas de entrada representam as operações disponíveis para os atores do sistema. No MVP, elas devem cobrir:

- gestão das contas de supervisores pelo Gestor e dos cadastros de colaboradores pelo Supervisor;
- registro e consulta de acidentes e incidentes;
- controle de estoque de EPIs;
- controle de manutenção de EPIs;
- empréstimo e devolução de EPIs;
- cadastro e consulta de áreas de risco;
- classificação do nível de periculosidade das tarefas;
- definição e acompanhamento de planos de ação;
- controle de certificações e treinamentos;
- gestão do ciclo de vida e planejamento da substituição dos EPIs.

### 3.2. Portas de saída

As portas de saída representam recursos externos ao núcleo:

- repositórios de usuários, ocorrências, EPIs, áreas de risco, tarefas, certificações e treinamentos;
- autenticação e controle de acesso;
- registro de auditoria;
- criptografia e armazenamento seguro.

## 4. Organização lógica do MVP

O núcleo é organizado pelas funcionalidades `usuarios`, `ocorrencias`, `epis`, `areas-de-risco`, `tarefas` e `capacitacoes`. Cada funcionalidade reúne suas regras e casos de uso. Os adaptadores ficam separados do núcleo para evitar dependência de tecnologia nas regras de negócio.

Essa é a organização lógica prevista, não uma lista de módulos já implementados. Os nomes abaixo identificam responsabilidades; os diretórios efetivos da base atual estão na seção 7.

| Funcionalidade | Responsabilidade no MVP | Referências |
| --- | --- | --- |
| `usuarios` | Gestor cadastra, consulta e atualiza supervisores com conta; Supervisor cadastra, consulta e atualiza colaboradores sem conta, preservando seus vínculos. | RF23, US23 e US24 |
| `ocorrencias` | Registrar e acompanhar acidentes e incidentes, com envolvidos, protocolo, triagem, atualização, arquivamento e planos de ação. | RF01, RF02, RF07, RF13 e RF16; UC05, UC07 e UC09 |
| `epis` | Estoque, movimentações, manutenção, empréstimos, devoluções e previsão de substituição com alertas. | RF03, RF04, RF11 e RF21; UC01, UC06, UC10 e UC12 |
| `areas-de-risco` | Cadastro, consulta e atualização das áreas, agentes de risco, limites e EPIs exigidos para acesso. | RF05; UC03 |
| `tarefas` | Classificar e consultar a periculosidade das tarefas. O vínculo entre tarefa e EPI permanece no backlog de RF12. | RF06; UC11 |
| `capacitacoes` | Consultar certificações e treinamentos, controlar vencimentos e aplicar os impedimentos previstos para alocação e empréstimo. O fluxo de cadastro ainda precisa ser identificado. | RF10; UC02 e UC12 |

A auditoria acompanha as operações que alteram dados em todas essas funcionalidades, conforme RNF05. O registro deve identificar usuário, data, hora e dado alterado, com imutabilidade e retenção mínima de 5 anos. A classe `LogAuditoria` no diagrama não comprova, por si só, a cobertura de todas as operações na implementação.

O armazenamento local previsto para o offline pertence à interface, com a comunicação necessária à sincronização. RNF08 permanece `Deve ter`, mas os dados consultáveis, os dispositivos atendidos e a inclusão do envio de relatos de UC09 continuam pendentes. A consulta de cópias locais não demonstra criação e sincronização de novos registros. A divergência com indicadores e relatórios fora do MVP está registrada na [especificação da entrega](../mvp/especificacao-mvp-arquitetura.md#11-pendências-da-revisão-documental).

Essa organização busca atender ao RNF14, que exige desacoplamento e extensibilidade. A documentação atende parcialmente ao RNF15. A base de ocorrências e estoque de EPIs já contém migrações de banco e configuração para expor a documentação OpenAPI; a cobertura dos demais fluxos ainda depende da implementação.

## 5. Requisitos não funcionais e decisões arquiteturais

| ID | Requisito | Decisão arquitetural |
| --- | --- | --- |
| RNF03 | Controle de acesso por perfil | Verificar as permissões de Gestor ou Supervisor antes de executar cada caso de uso protegido. Cadastro de Colaborador não permite autenticação. |
| RNF05 | Rastreabilidade de ações (auditoria) | Manter log imutável das operações, com usuário, data, hora e retenção mínima de 5 anos. |
| RNF08 | Funcionamento offline parcial | Prevê cópias locais para consulta. O conjunto de dados e a relação com o envio offline de UC09 dependem da decisão de escopo. |
| RNF11 | Interface responsiva e acessível | Construir a interface para diferentes tamanhos de tela e seguir as diretrizes WCAG 2.1 AA. |
| RNF15 | Documentação técnica | Versionar a arquitetura, a API e o modelo de dados junto ao projeto. |

As decisões acima descrevem a solução esperada. O atendimento de cada RNF deve ser comprovado por testes ou evidências da implementação.

## 6. Artefatos arquiteturais

Os diagramas não são alterados nesta revisão. O grupo fará a atualização em uma etapa posterior. O conteúdo esperado de cada artefato está definido na [Especificação do MVP e Arquitetura](../mvp/especificacao-mvp-arquitetura.md#8-artefatos-arquiteturais-pendentes).

O [diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png) é uma referência do domínio, com pendências acompanhadas na [issue #39](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/39). A operação `Supervisor.criarContaColaborador()` ainda precisa ser alinhada ao cadastro sem acesso. Heranças de dados pessoais não definem permissões de autenticação.

O [PlantUML da proposta hexagonal](../diagramas/arquitetura/safeplace-arquitetura-hexagonal-proposta.puml) e seu [PNG](../diagramas/arquitetura/safeplace-arquitetura-hexagonal-proposta.png) são uma proposta anterior que usa Python. Devem ser lidos como referência histórica da separação de responsabilidades; a tecnologia adotada na base atual é Java. Componentes e implantação também precisam ser reconciliados na etapa de diagramas da [issue #81](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/81).

| Artefato | Status |
| --- | --- |
| Diagrama de pacotes | Pendente de atualização |
| Diagrama de componentes lógico | Pendente de atualização |
| Diagrama de componentes executável | Pendente de atualização |
| Diagrama de sequência | Pendente de atualização |
| Diagrama de classes reduzido ao MVP | Pendente de atualização |

## 7. Base técnica existente e limites

| Parte | Tecnologia e evidência no repositório |
| --- | --- |
| Backend | Java 21 e Spring Boot 4.1.1 em [pom.xml](../../backend/pom.xml). |
| Persistência | Spring Data JPA, PostgreSQL e Flyway; conexão em [application.yaml](../../backend/src/main/resources/application.yaml), ocorrências em [V1__create_ocorrencias.sql](../../backend/src/main/resources/db/migration/V1__create_ocorrencias.sql) e estoque de EPIs em [V2__create_epis.sql](../../backend/src/main/resources/db/migration/V2__create_epis.sql). |
| Interface | React 19, TypeScript e Vite 8 em [package.json](../../frontend/package.json). |
| Ambiente local | [Docker Compose](../../docker-compose.yml) com PostgreSQL 16, backend e frontend na mesma máquina. |
| Documentação da API | Springdoc configurado em `application.yaml`: interface em `/docs` e descrição OpenAPI em `/v3/api-docs`. |

O backend está em `backend/src/main/java/br/edu/safeplace/backend/`. O fluxo implementado de ocorrências conecta `adapters/in/web/OcorrenciaController` à porta de entrada `application/port/in/RegistrarOcorrenciaUseCase`, implementada por `application/service/OcorrenciaService`. O serviço usa a porta de saída `application/port/out/OcorrenciaRepositoryPort`; `adapters/out/persistencia/OcorrenciaJpaAdapter` implementa a persistência. As classes de domínio ficam em `domain/ocorrencia/`. A aplicação já usa `@Service` do Spring; a separação descrita nas seções anteriores é a direção arquitetural, não uma declaração de independência técnica completa da implementação atual.

O estoque segue a mesma organização: `EpiController` recebe as requisições, `EpiService` implementa `GerenciarEpiUseCase` e usa `EpiRepositoryPort`, implementada por `EpiJpaAdapter`. O domínio fica em `domain/epi/` e controla entradas, saídas e impedimento de saldo negativo.

| Operação disponível | Endpoint |
| --- | --- |
| Autenticar usuário (obter token JWT) | `POST /api/auth/login` |
| Cadastrar e listar usuários | `POST /api/usuarios` e `GET /api/usuarios` |
| Cadastrar e listar ocorrências | `POST /api/ocorrencias` e `GET /api/ocorrencias` |
| Cadastrar e listar EPIs | `POST /api/epis` e `GET /api/epis` |
| Consultar um EPI | `GET /api/epis/{id}` |
| Registrar entrada ou saída de estoque | `POST /api/epis/{id}/movimentacoes` |
| Verificar disponibilidade da API | `GET /api/health` |

A implementação de EPIs ainda não cobre manutenção, empréstimos, devoluções ou projeções de substituição. Os módulos de áreas de risco, tarefas e capacitações ainda não possuem implementação nessa base. O módulo de usuários e autenticação RBAC está implementado no backend via Arquitetura Hexagonal. O frontend apresenta o catálogo e exemplos de componentes com dados fictícios; não integra os fluxos de negócio com a API.

Os passos de execução e verificação estão no [README principal](../../README.md#executar-com-docker-compose) e no [README do frontend](../../frontend/README.md). O Compose é o ambiente local de desenvolvimento, enquanto os diagramas de implantação descrevem uma proposta de distribuição do sistema.

Pontos ainda não atendidos ou fora da entrega:

- O barramento de eventos não faz parte do MVP.
- Não há adaptadores para sensores ou dispositivos IoT.
- Relatórios, CAT, dashboards e integrações externas permanecem fora do MVP, conforme a priorização.
- RNF08 permanece no MVP, com escopo pendente e sem implementação de cache ou sincronização na interface atual.
- A [configuração de segurança](../../backend/src/main/java/br/edu/safeplace/backend/config/SecurityConfig.java) aplica controle de acesso por perfil (RBAC) e autenticação via JWT (RNF03). Rotas públicas limitam-se a autenticação, documentação Swagger e health check.
- RNF05 ainda precisa de implementação que cubra as operações, os dados auditados, a imutabilidade e a retenção.
- O Compose fornece `VITE_API_URL`, mas o cliente HTTP lê `VITE_URL_API`. A configuração precisa ser alinhada quando a interface for integrada à API; o valor local padrão está documentado no README do frontend.
