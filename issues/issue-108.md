# Issue #108: [60] [MVP][Substituição] Definir e implementar projeções com parâmetros aprovados

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/108
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:23Z
- **Updated at:** 2026-09-15T01:02:39Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF21; UC10; US21.

**Referência do modelo:** ProjecaoSubstituicao: dataProjecao, dataEstimadaDescarte, justificativa; DS10/DS10.1.

## O que entregar

Primeiro registrar origem/unidade dos dados, fórmula, multiplicadores ambientais, referência temporal e antecedência dos alertas. Depois implementar projeção e consulta a partir do contrato aprovado.

## Critérios de aceite

- [ ] Não usar min(primeiroUso+vidaUtilDias,validadeCA) como fórmula aprovada apenas porque consta na issue antiga.
- [ ] Cobrir frequência/horas de uso, durabilidade nominal e ambiente previstos em UC10; ausência de dados gera pendência/erro, não estimativa fictícia.
- [ ] Respeitar regra de CA de UC10 e registrar projeção com data/justificativa vinculada ao EPI.
- [ ] Exemplos de entrada/resultado aprovados permitem testar cálculo, falha e nova tentativa.
- [ ] Previsões e alertas preventivos são MVP; compras, prorrogação por laudo e exportação não são incluídas sem decisão.
- [ ] Reconciliar operações de DS10 com classes e disponibilizar DS10.1, sem inventar seu conteúdo.

## Dependências técnicas

- [#124](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/124)

## Decisões pendentes e limites

Decisão necessária antes do algoritmo. Fontes de histórico de #107 são dependência somente se aprovadas como dados de cálculo. RF21 continua obrigatório.

## Motivo da correção

Retirada fórmula arbitrária, consumo mensal sem fonte e enum fixo de prioridade.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/domain/epi/Epi.java). Evidência do estado existente, não fonte de requisito.

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
O **RF21** e o **UC10** estabelecem o gerenciamento inteligente do ciclo de vida dos EPIs:
- Estimar a data recomendada de substituição de cada EPI com base na sua vida útil estimada (`vidaUtilDias`), histórico de empréstimos/uso e validade do Certificado de Aprovação (`dataValidadeCa`).
- Gerar alertas preventivos antes do esgotamento do estoque ou vencimento do CA/vida útil, permitindo ao gestor planejar a compra ou reposição antes da perda da proteção.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 5 - Rastreabilidade, Substituição e Persistência de Áreas
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#101: [MVP][EPI] Modelar Domínio de Manutenção de EPIs e Transições de Status com Validação de CA)
  - [?] (#123: [MVP][EPI] Implementar Alertas Automáticos de Estoque Crítico e Reposição)
  - [?] (#106: [MVP][EPI] Modelar Domínio de Empréstimo e Devolução de EPIs para Colaboradores)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#109: [MVP][EPI] Implementar Endpoints REST para Empréstimos, Devoluções e Projeções de Substituição)
  - [?] (#110: [MVP][Frontend] Construir Interface de Empréstimos, Devoluções e Alertas de Substituição Inteligente)

## 3. O Que Fazer
1. Criar o serviço de domínio `SubstituicaoInteligenteService` em `domain.epi`:
   - Calcular a data estimada de substituição com base na menor data entre: (Data do primeiro uso + `vidaUtilDias`) e a `dataValidadeCa`.
   - Gerar projeção de consumo mensal com base na frequência recente de empréstimos.
2. Criar o Value Object `AlertaSubstituicaoEpi`:
   - Atributos: `epiId`, `nomeEpi`, `tipoAlerta` (`CA_PROXIMO_VENCIMENTO`, `VIDA_UTIL_ESGOTANDO`, `ESTOQUE_CRITICO`), `dataLimiteEstimada`, `diasRestantes`, `prioridade` (`BAIXA`, `MEDIA`, `ALTA`).
3. Definir porta de entrada `PlanejarSubstituicaoEpiUseCase` para consultar as projeções e alertas de todos os EPIs.

## 4. Como Fazer (Arquitetura Hexagonal)
- Lógica de cálculo 100% pura na camada de domínio/aplicação, sem dependências de infraestrutura.
- Testes unitários parametrizados testando múltiplos cenários de vida útil e datas de CA.


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
- [ ] Projeção de data de substituição calculada com precisão matemática.
- [ ] Alertas emitidos quando o CA ou vida útil estiver a menos de 30 dias de expirar.
- [ ] Alertas de estoque crítico emitidos quando saldo <= estoque mínimo.
- [ ] Testes unitários cobrindo todos os tipos de alerta com cobertura total.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

