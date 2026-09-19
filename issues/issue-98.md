# Issue #98: [47] [MVP][Planos] Persistir ações e alterações preservando histórico da ocorrência

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/98
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:59Z
- **Updated at:** 2026-09-15T01:02:11Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF07; RNF05; UC07; US07.

**Referência do modelo:** PlanoDeAcao e associações aprovadas em #97.

## O que entregar

Completar persistência existente de planos segundo o contrato aprovado, suportando responsáveis, situação, prazo, justificativas e configuração de alertas.

## Critérios de aceite

- [ ] Plano/ações, responsáveis e vínculo com ocorrência sobrevivem à nova consulta.
- [ ] Prazo inválido não grava; mudança de responsável e prorrogação preservam valores e histórico.
- [ ] Arquivamento de ocorrência conserva planos; não introduzir exclusão física em cascata.
- [ ] Migrações evoluem tabela já existente; não recriar planos_de_acao nem reutilizar V5.
- [ ] Transações e testes PostgreSQL verificam plano, histórico e auditoria coerentes.

## Dependências técnicas

- [#97](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/97)
- [#96](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/96)

## Decisões pendentes e limites

Depende do contrato de #97; integração final de auditoria depende da capacidade entregue em #121.

## Motivo da correção

Corrigidos tabela duplicada, FK presumida ocorrencias.id e ON DELETE CASCADE incompatível com preservação.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/ocorrencia/PlanoDeAcao.java). Evidência do estado existente, não fonte de requisito.

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
Criar a infraestrutura de persistência relacional para armazenar os planos de ação atrelados às ocorrências, permitindo o acompanhamento do status e prazos de cada ação preventiva/corretiva.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 3 - Frontend de Usuários e Gestão de Manutenção
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#96: [MVP][Ocorrências] Implementar Adaptador de Persistência e Migrações Flyway de Ocorrências)
  - [?] (#97: [MVP][Ocorrências] Modelar Domínio e Ciclo de Vida de Planos de Ação)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#99: [MVP][Ocorrências] Implementar Endpoints REST para Ocorrências, Triagem e Planos de Ação)
  - [?] (#100: [MVP][Frontend] Construir Interface de Registro de Ocorrências, Triagem e Planos de Ação)

## 3. O Que Fazer
1. Criar migração Flyway `V5__create_planos_de_acao.sql`:
   - Tabela `planos_de_acao` com `id`, `ocorrencia_id` (FK para `ocorrencias.id` com `ON DELETE CASCADE`), `descricao`, `responsavel`, `data_prazo`, `data_conclusao`, `status`, `criado_em`, `atualizado_em`.
2. Criar a entidade JPA `PlanoDeAcaoEntity` em `adapters.out.persistencia`.
3. Implementar `PlanoDeAcaoJpaAdapter` implementando `PlanoAcaoRepositoryPort`:
   - Salvar plano de ação.
   - Buscar por ID.
   - Listar todos os planos vinculados a um `ocorrenciaId`.
4. Implementar serviço de aplicação `PlanoAcaoService` orquestrando a lógica de negócio.

## 4. Como Fazer
- Mapeamento estrito entre `PlanoDeAcaoEntity` e `PlanoDeAcao` do domínio.
- Assegurar integridade referencial com a tabela de ocorrências.


## 5. Exigência de Testes Automatizados (Critério Obrigatório para Nota Máxima)
Para garantir pontuação máxima na avaliação semanal do professor, o desenvolvedor deve entregar:
- **Testes de Componentes React**: Testes automatizados com Vitest e React Testing Library cobrindo renderização, submissão de formulários, estados de carregamento (loading), mensagens de erro e feedback visual.
- **Validação de Acessibilidade (WCAG 2.1 AA)**: Verificação de contraste de cores, labels acessíveis em formulários e navegação por teclado.
- **Resiliência Offline (RNF08)**: Teste comprovando a consulta de dados a partir do cache local quando a rede estiver indisponível.


## 6. Governança do Modelo Orientado a Cronograma (Schedule-Driven Development)
Como o projeto adota o **Modelo Orientado a Cronograma**, o prazo semanal (timebox) é **fixo e inegociável**:
- **Timebox Fixo**: A entrega desta issue deve ser concluída impreterivelmente até o final da semana correspondente para avaliação do professor.
- **Estratégia Anti-Bloqueio (In-Memory Stubs)**: Caso uma issue predecessora atrase, o desenvolvedor **NÃO deve ficar bloqueado**. Deve criar uma implementação falsa em memória (In-Memory Fake/Mock) da Porta de Saída correspondente, garantindo que seu Caso de Uso ou Interface funcione e seja testado autonomamente.
- **Regra de Estabilidade de Integração (Trunk Stability)**: Nenhuma alteração pode quebrar a branch principal (`main` / `dev`). Ao final do timebox, a entrega deve ser submetida via Pull Request com testes verdes e build passando.
- **Diretriz de Redução de Escopo (Scope Pruning)**: Se houver risco iminente de estourar o timebox semanal, recursos secundários (como estilos visuais avançados ou filtros secundários) devem ser temporariamente postergados, priorizando o núcleo de domínio, os testes unitários e a API funcional.

## 7. Critérios de Aceite para Nota Máxima (Final do Timebox)
- [ ] Migração Flyway executada com sucesso.
- [ ] Repositório JPA persiste e recupera planos de ação associados à ocorrência.
- [ ] Exclusão ou arquivamento de ocorrência mantém integridade consistente.
- [ ] Testes de integração de banco cobrem operações CRUD de planos de ação.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

