# Issue #91: [35] [MVP][Usuários] Verificar geração e entrega da senha inicial do Supervisor

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/91
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Amanda Dias
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:44Z
- **Updated at:** 2026-09-15T01:01:48Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF23; RNF03; US23/US24.

**Referência do modelo:** Supervisor.senha; GestorDeSeguranca.senha; Colaborador sem senha.

## O que entregar

Concluir o fluxo existente: Gestor cadastra Supervisor, sistema gera senha inicial e armazena somente hash; consulta posterior não revela senha. Reutilizar GeradorSenhaPorta e CodificadorSenhaPorta existentes.

## Critérios de aceite

- [ ] Cadastro de Supervisor gera senha no sistema e permite entrega inicial conforme contrato existente.
- [ ] Consulta/listagem nunca devolvem hash ou senha inicial; Colaborador não recebe credencial.
- [ ] Cadastro seguido de autenticação valida integração com persistência real.
- [ ] Novas migrações preservam V3/V4 e dados existentes; unicidade e nulidade de credenciais são verificadas.

## Dependências técnicas

- [#90](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/90)

## Decisões pendentes e limites

Entrega inicial pode seguir a resposta de criação já implementada; políticas adicionais de tamanho, expiração e troca obrigatória não estão aprovadas como requisitos.

## Motivo da correção

Não fixar mínimo de 10 caracteres nem editar migração já aplicada. Hash de senha não comprova atendimento integral de RNF04.

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
Conforme o **RF23**, quando o Gestor de Segurança cadastra um novo Supervisor, o sistema deve gerar automaticamente uma senha inicial segura e persistir apenas o hash criptográfico dessa senha (atendendo ao **RNF04** e boas práticas de segurança).

Esta issue contempla o serviço de domínio/aplicação de geração de senha, a porta de codificação e seu adaptador de saída usando BCrypt, além de alinhar o esquema de banco via Flyway.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 1 - Fundação de Domínio e Credenciais
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#90: [MVP][Usuários] Definir Domínio e Portas para Gestor, Supervisor e Colaborador)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#92: [MVP][Segurança] Implementar Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#93: [MVP][Usuários] Implementar Controladores Inbound, DTOs e Casos de Uso de Usuários)
  - [?] (#96: [MVP][Ocorrências] Implementar Adaptador de Persistência e Migrações Flyway de Ocorrências)
  - [?] (#107: [MVP][EPI] Implementar Persistência JPA e Migração Flyway para Empréstimos e Devoluções)
  - [?] (#118: [MVP][Capacitações] Implementar Persistência JPA e Migração Flyway para Certificações e Treinamentos)

## 3. O Que Fazer
1. Definir a porta de saída `application.port.out.CodificadorSenhaPorta` com métodos:
   - `String codificar(String senhaPura);`
   - `boolean validar(String senhaPura, String hash);`
2. Implementar o adaptador de saída em `adapters.out.seguranca.BcryptCodificadorSenhaAdaptador` utilizando Spring Security `BCryptPasswordEncoder`.
3. Criar serviço gerador de senhas aleatórias seguras (alfanumérico + caracteres especiais, mínimo 10 caracteres) na camada de aplicação.
4. Ajustar ou complementar a migração Flyway `V3__create_usuarios.sql` para suportar índices únicos para CPF e e-mail, além de garantir que senhas sejam nulas para perfis sem login (`Colaborador`).

## 4. Como Fazer (Arquitetura Hexagonal)
- A porta `CodificadorSenhaPorta` reside na camada de aplicação (`application.port.out`), mantendo o caso de uso desacoplado do framework de segurança.
- O adaptador concreto reside em `adapters.out.seguranca`.
- Na persistência, garantir que `Colaborador` armazene `senha = NULL` e que `perfil` seja restrito via restrições do banco.


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
- [ ] Senha inicial de Supervisor é gerada de forma segura com entropia adequada.
- [ ] Apenas o hash BCrypt é armazenado no banco de dados.
- [ ] Colaboradores são cadastrados sem geração de credenciais ou senha.
- [ ] Testes unitários do gerador e do codificador cobrem geração, validação e integridade de hashes.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

