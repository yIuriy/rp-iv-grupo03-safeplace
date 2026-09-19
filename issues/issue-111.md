# Issue #111: [55] [MVP][Áreas] Alinhar Setor e AreaRisco e completar cadastro e atualização

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/111
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:30Z
- **Updated at:** 2026-09-15T01:02:45Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF05; UC03; US05.

**Referência do modelo:** Setor e AreaRisco são classes distintas no PNG; nome/status e relações devem ser preservados.

## O que entregar

Mapear explicitamente Setor versus ÁreaRisco, seus vínculos e campos de identificação, agentes e limites exigidos pelo UC03. Completar a base sem fundir classes por conveniência.

## Critérios de aceite

- [ ] Registrar representação de código, agentes e limites e cardinalidades antes de criar novos campos obrigatórios.
- [ ] Cadastro rejeita código duplicado e qualquer área sem EPIs obrigatórios, independentemente do nível.
- [ ] Atualização valida agentes/limites, preserva histórico e identifica responsável.
- [ ] Áreas/tarefas usam referências reais aprovadas; ocorrência não cria setor fictício a partir de texto.
- [ ] Operações validarDadosAreaRisco, salvarAreaRisco, validarAlteracoes e buscas têm correspondência rastreável.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Formato de limites/agentes requer decisão; vocabulário de risco em #112. Não depende de manutenção de EPI, apenas catálogo disponível.

## Motivo da correção

Não criar AreaDeRisco/GrauPerigo alternativos nem catálogo de agentes inventado; reutilizar AreaRisco e NivelPerigo do modelo.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/area_risco/AreaRisco.java). Evidência do estado existente, não fonte de requisito.

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
O **RF05** e o **UC03** estabelecem o mapeamento de áreas de risco da empresa:
- Delimitação física e identificação de cada setor (ex: Almoxarifado Químico, Caldeiras, Pátio de Carga).
- Identificação dos agentes de risco presentes (químico, físico, biológico, ergonômico, mecânico).
- Vinculação dos **EPIs de uso obrigatório para acesso à área** (requisito independente do vínculo de tarefas).
- Classificação do grau de perigo (`BAIXO`, `MEDIO`, `ALTO`, `CRITICO`).

Esta issue cria o modelo de domínio puro e as portas da funcionalidade.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 4 - Frontend de Ocorrências, Empréstimos e Áreas de Risco
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#101: [MVP][EPI] Modelar Domínio de Manutenção de EPIs e Transições de Status com Validação de CA)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#112: [MVP][Tarefas] Modelar Domínio e Classificação de Periculosidade de Tarefas)
  - [?] (#113: [MVP][Áreas e Tarefas] Implementar Persistência JPA e Migração Flyway para Áreas e Tarefas)
  - [?] (#114: [MVP][Áreas e Tarefas] Implementar Endpoints REST para Áreas de Risco e Classificação de Tarefas)
  - [?] (#125: [MVP][Áreas de Risco] Implementar Verificação de Conformidade de EPI para Acesso a Setores de Risco)

## 3. O Que Fazer
1. Criar as entidades e enums em `domain.arearisco`:
   - `AreaDeRisco`: `id`, `nome`, `codigoSetor`, `descricao`, `grauPerigo` (`BAIXO`, `MEDIO`, `ALTO`, `CRITICO`), `agentesRisco` (lista de agentes), `episObrigatoriosIds` (conjunto de IDs de EPIs exigidos para acesso).
   - Enum `GrauPerigo` harmonizado com a documentação do MVP.
2. Implementar regras de negócio no domínio:
   - Todo setor cadastrado como área de risco exige o vínculo de ao menos um EPI obrigatório para acesso (**UC03 Regra de Negócio 1**).
   - Validação de código de setor único e não vazio.
3. Declarar as portas formais:
   - `port.in.MapearAreaRiscoUseCase`: cadastrar, atualizar delimitadores e consultar mapa de riscos.
   - `port.out.AreaRiscoRepositoryPort`.

## 4. Como Fazer (Arquitetura Hexagonal)
- Domínio puro livre de frameworks.
- Testes unitários cobrindo validação de EPIs obrigatórios e invariantes de perigo.


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
- [ ] Entidade `AreaDeRisco` implementada com invariantes obrigatórios.
- [ ] Portas de entrada e saída definidas.
- [ ] Regra impeditiva de cadastro sem EPIs obrigatórios testada unitariamente.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

