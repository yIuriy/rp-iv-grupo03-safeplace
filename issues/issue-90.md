# Issue #90: [34] [MVP][Usuários] Alinhar cadastro de pessoas e contas ao modelo de classes

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/90
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Amanda Dias
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:41Z
- **Updated at:** 2026-09-15T01:01:46Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF23; RNF03; US23/US24.

**Referência do modelo:** Colaborador; Supervisor e GestorDeSeguranca herdam de Colaborador; credenciais somente nas subclasses.

## O que entregar

Conferir a base existente e os contratos de cadastro/consulta, preservando a herança do PNG e a distinção entre pessoa cadastrada e conta de acesso. Mapear CPF, nome, data de nascimento, e-mail, ativo e metadados do modelo para os dados persistidos.

## Critérios de aceite

- [ ] Herança e atributos têm correspondência documentada; não remover herança para satisfazer uma descrição antiga da issue.
- [ ] Colaborador comum não recebe senha, token ou login; Supervisor e Gestor mantêm credenciais próprias.
- [ ] Busca por CPF e cadastro/consulta preservam identidade; regras de validação sem fonte são registradas como decisão, não como requisito aprovado.
- [ ] Registrar cobertura textual de RF23/UC13, política cadastral e divergência de criarContaColaborador(), encaminhando atualização para #122.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

UC13, matrícula/setor e eventual desativação continuam pendentes na especificação. Não impor alteração de CPF, dígitos verificadores ou novo perfil sem registrar o contrato aprovado.

## Motivo da correção

A issue antiga omitia a herança do PNG e tratava escolhas de nomenclatura como obrigação. Comentário existente sobre #138 deve ser preservado; conferir implementação antes de refazer.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/application/usecase/UsuarioCasoDeUso.java). Evidência do estado existente, não fonte de requisito.

## Regras de consistência desta revisão

- O modelo aprovado orienta a implementação. Campos e operações em conflito aguardam decisão registrada; código existente não aprova uma regra de negócio.
- Preservar identidade das classes, atributos, associações e semântica das operações. Adaptação para portas/adaptadores deve ter correspondência explícita; diferenças apenas de nomes técnicos não justificam reconstrução do sistema.
- A MoSCoW define o recorte. RF21 e RNF08 continuam obrigatórios mesmo com decisões pendentes. Dicionários marcados como proposta ou "A confirmar" não são requisitos aprovados.
- Reutilizar o que existe e completar o fluxo integrado; teste isolado ou repositório em memória não comprova persistência/entrega funcional. Verificar testes pertinentes, integração com PostgreSQL/API e interface quando aplicável, declarando falhas, skips e limitações.
- Migrações novas usam versão livre e preservam dados existentes; não reescrever migrações aplicadas nem inventar dados históricos ausentes.
- Esta edição corrige escopo e critérios. Não altera responsáveis, comentários, estado ou datas do projeto e não promete nota, prazo ou percentual de cobertura sem fonte acadêmica. O cronograma anterior é preservado no histórico; não foi revalidado como critério técnico nesta revisão.

<details>
<summary>Texto anterior preservado como histórico — não usar como escopo vigente</summary>

## 1. Contexto e Objetivo
No SafePlace, o controle de usuários é dividido estritamente conforme o **RF23** e a [Especificação do MVP](docs/mvp/especificacao-mvp-arquitetura.md):
- **Gestor de Segurança**: possui perfil de acesso administrativo no sistema e gerencia contas de supervisores.
- **Supervisor**: possui perfil de acesso operacional no sistema e gerencia cadastros de colaboradores.
- **Colaborador**: NÃO possui credenciais, senha ou permissão de login no sistema. Seu cadastro serve exclusivamente para identificação e vinculação em ocorrências, empréstimos de EPI e capacitações.

Esta issue estabelece o núcleo puro de domínio (Hexagonal Domain) e as interfaces abstratas (Ports) de entrada e saída para gerenciar essas entidades.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 1 - Fundação de Domínio e Credenciais
- **Dependências diretas (Pré-requisitos)**: Nenhuma (Ponto de partida do domínio de usuários).
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#91: [MVP][Usuários] Implementar Geração Automática de Credenciais e Hash de Senha para Supervisor)
  - [?] (#93: [MVP][Usuários] Implementar Controladores Inbound, DTOs e Casos de Uso de Usuários)
  - [?] (#95: [MVP][Ocorrências] Modelar Domínio e Invariantes para Acidentes, Incidentes e Protocolo Único)
  - [?] (#106: [MVP][EPI] Modelar Domínio de Empréstimo e Devolução de EPIs para Colaboradores)
  - [?] (#116: [MVP][Capacitações] Modelar Domínio de Certificações, Treinamentos e Alerta de 30 Dias)

## 3. O Que Fazer
1. Criar e refinar as entidades puras no pacote `domain.usuario`:
   - `GestorDeSeguranca`: com atributos `id`, `cpf`, `nome`, `email`, `senhaHash`, `ativo`.
   - `Supervisor`: com atributos `id`, `cpf`, `nome`, `email`, `senhaHash`, `ativo`.
   - `Colaborador`: com atributos `id`, `cpf`, `nome`, `dataNascimento`, `ativo` (sem atributos de credenciais ou login).
   - `Perfil`: Enum contendo `GESTOR_SEGURANCA` e `SUPERVISOR`.
2. Implementar invariantes e validações ricas no domínio:
   - Validador de CPF (`CpfValidador`) como Value Object ou serviço de domínio.
   - Proibição absoluta de atribuição de senha ao `Colaborador`.
3. Declarar as portas formais em `application.port`:
   - `port.in.GerenciarUsuarioUseCase`: métodos para cadastrar supervisor, cadastrar colaborador, buscar por ID/CPF e listar.
   - `port.out.UsuarioRepositoryPort`: métodos para salvar, buscar por ID, CPF ou e-mail, e verificar duplicidades.

## 4. Como Fazer (Arquitetura Hexagonal)
- Manter o pacote `domain.usuario` 100% livre de anotações de infraestrutura (sem `@Entity`, sem `@Table`, sem dependências do Spring).
- Utilizar imutabilidade e encapsulamento com métodos expressivos de negócio.
- Escrever testes unitários puros cobrindo criação válida e exceções de negócio (CPF inválido, e-mail duplicado).


## 5. Exigência de Testes Automatizados (Critério Obrigatório para Nota Máxima)
Para garantir pontuação máxima na avaliação semanal do professor, o desenvolvedor deve entregar:
- **Testes Unitários de Domínio**: Cobertura das regras e invariantes usando JUnit 5 e AssertJ, sem mocks de banco ou contexto Spring.
- **Testes de Integração / API**: Validação com Spring `MockMvc` ou `DataJpaTest` cobrindo status HTTP (200, 201, 400, 403, 404, 422), integridade transacional e constraints do PostgreSQL.
- **Documentação Swagger**: Endpoints testados e validados no OpenAPI (`/swagger-ui/index.html`).


## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso uma issue predecessora atrase, o desenvolvedor **NÃO deve ficar bloqueado**. Deve criar uma implementação falsa em memória (In-Memory Fake/Mock) da Porta de Saída correspondente, garantindo que seu Caso de Uso ou Interface funcione e seja testado autonomamente.
- **Regra de Estabilidade de Integração (Trunk Stability)**: Nenhuma alteração pode quebrar a branch principal (`main` / `dev`). Ao final do timebox, a entrega deve ser submetida via Pull Request com testes verdes e build passando.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver risco iminente de estourar o timebox semanal, recursos secundários (como estilos visuais avançados ou filtros secundários) devem ser temporariamente postergados, priorizando o núcleo de domínio, os testes unitários e a API funcional.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Entidades `GestorDeSeguranca`, `Supervisor` e `Colaborador` implementadas e isoladas no domínio.
- [ ] O modelo de `Colaborador` não possui senha, token ou qualquer dado de login.
- [ ] Portas de entrada (`GerenciarUsuarioUseCase`) e saída (`UsuarioRepositoryPort`) definidas com contratos claros.
- [ ] Testes unitários de domínio passando sem necessidade de contexto Spring ou banco de dados.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>


---

## Comments

### Comment by @amandadiasdev on 2026-09-11T03:28:32Z

## Estado da issue em 11/09/2026

**O escopo desta issue já estava na `dev`** antes de eu começar: a @Rafaela-06 implementou o domínio, as portas, o caso de uso, o controller, o adaptador JPA e a migração `V3__create_usuarios.sql` no PR #89 (commits `feat(usuario)` de 06 e 07/09, mesclado em 07/09).

**PR #138** (`feature/issue-90-dominio-usuarios` -> `dev`) fecha a lacuna restante em modo aditivo, sem alterar o código existente:
- `GerenciarUsuarioCasoDeUso.buscarPorCpf(String)` na porta de entrada, com implementação em `UsuarioCasoDeUso` (a issue pedia "buscar por ID/CPF"; só havia por ID).
- Testes de domínio: `ColaboradorSemCredenciaisTest` (prova por reflexão que `Colaborador` não declara campo ou método de senha/hash/token/login), `PerfilTest`, `GestorDeSegurancaTest`, `SupervisorTest`, `UsuarioCasoDeUsoBuscaPorCpfTest`.

**Divergências entre o código da `dev` e o texto desta issue**, registradas para o grupo decidir (não alteradas):
1. `Supervisor` e `GestorDeSeguranca` herdam de `Colaborador`; a issue descrevia `Colaborador` sem herança com as contas. Consequência: uma referência do tipo `Colaborador` pode carregar senha por polimorfismo.
2. `Perfil` tem `COLABORADOR`; a issue pede só `GESTOR_SEGURANCA` e `SUPERVISOR`, e `docs/mvp/especificacao-mvp-arquitetura.md` (linha 290) diz que Colaborador não é perfil.
3. `CpfValidador` não confere dígitos verificadores desde o commit `89b3ce9` (`12345678900` é aceito).
4. Campo `senha` no domínio, embora guarde hash BCrypt (a issue chama `senhaHash`).
5. Porta de entrada com `cadastrarUsuario` único (perfil no DTO) em vez de `cadastrarSupervisor` e `cadastrarColaborador`; nomes das portas em português, diferente do módulo EPI.

Sugiro fechar esta issue quando o #138 for mesclado e abrir uma issue própria para os itens 1 a 5, se o grupo decidir alinhar.

