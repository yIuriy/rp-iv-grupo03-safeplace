# Issue #116: [67] [MVP][Capacitações] Alinhar modelo e controlar vencimentos documentados

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/116
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:42Z
- **Updated at:** 2026-09-15T01:02:53Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF10; UC02; US10.

**Referência do modelo:** Certificacao: nome,status,dataVencimento,dataEmissao; Treinamento: nome,conteudo,instrutor,dataRealizacao,cargaHoraria; vínculos Colaborador.

## O que entregar

Implementar consulta de certificações/treinamentos e alerta de 30 dias, preservando atributos do PNG. Identificar primeiro fluxo de cadastro e origem de exigências obrigatórias.

## Critérios de aceite

- [ ] Não trocar dataVencimento do modelo nem omitir conteúdo/instrutor; novos campos exigem correspondência aprovada.
- [ ] Alertas de certificação próxima do vencimento em 30 dias e já vencida seguem UC02; testar fronteiras de datas.
- [ ] Ausência de certificações e treinamentos pendentes é apresentada como pendência.
- [ ] Definir onde exigência por tarefa/EPI é registrada e como treinamento vencido é representado sem inventar dataValidade para todo treinamento.
- [ ] Identificar cadastro/validação cronológica e papéis em fluxo textual antes de expor escrita.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Cadastro, representação das exigências e divergências de assinaturas CPF/lista no PNG precisam de decisão; consulta pode ser desenvolvida com dados de teste explicitamente identificados.

## Motivo da correção

30 dias têm respaldo em UC02 e foram mantidos. Entidade emissora, validade e enums prescritos sem modelo deixam de ser obrigações.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/capacitacao). Evidência do estado existente, não fonte de requisito.

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
O **RF10** e o **UC02** estabelecem o controle de certificações e treinamentos obrigatórios dos colaboradores:
- Registrar certificações técnicas e treinamentos normativos (ex.: NR-10, NR-35).
- Controlar prazos de validade e datas de renovação periódica.
- Gerar alertas automáticos preventivos quando uma certificação estiver a **30 dias do vencimento**.
- Identificar certificações já expiradas para ação imediata do supervisor ou gestor.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 6 - APIs de Empréstimo, Áreas e Domínio de Capacitações
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#90: [MVP][Usuários] Definir Domínio e Portas para Gestor, Supervisor e Colaborador)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#117: [MVP][Capacitações] Implementar Regra de Bloqueio Impeditivo de Alocação por Certificação Vencida)
  - [?] (#118: [MVP][Capacitações] Implementar Persistência JPA e Migração Flyway para Certificações e Treinamentos)
  - [?] (#119: [MVP][Capacitações] Implementar Endpoints REST para Certificações, Treinamentos e Validação de Bloqueio)

## 3. O Que Fazer
1. Criar entidades de domínio em `domain.capacitacao`:
   - `Certificacao`: `id`, `colaboradorId`, `nomeCertificacao`, `entidadeEmissora`, `dataEmissao`, `dataValidade`, `status` (`EM_DIA`, `PROXIMA_DO_VENCIMENTO`, `VENCIDA`).
   - `Treinamento`: `id`, `colaboradorId`, `nomeTreinamento`, `cargaHoraria`, `dataRealizacao`, `dataValidade`, `status`.
2. Implementar métodos de cálculo temporal no domínio:
   - Método `calcularStatus(LocalDate hoje)`:
     - Se `hoje > dataValidade`: status = `VENCIDA`.
     - Se `dataValidade - hoje <= 30 dias`: status = `PROXIMA_DO_VENCIMENTO`.
     - Caso contrário: status = `EM_DIA`.
3. Declarar portas em `application.port`:
   - `port.in.ControlarCapacitacaoUseCase`.
   - `port.out.CapacitacaoRepositoryPort`.

## 4. Como Fazer (Arquitetura Hexagonal)
- Manter o domínio puro sem referências a bibliotecas externas.
- Testes unitários cobrindo cálculo de dias para vencimento e disparos de status.


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
- [ ] Entidades de domínio encapsulando regras cronológicas de validade.
- [ ] Alerta de 30 dias gerado estritamente conforme o UC02 Regra 1.
- [ ] Testes unitários com simulação de datas passadas, futuras e margem de 30 dias.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

