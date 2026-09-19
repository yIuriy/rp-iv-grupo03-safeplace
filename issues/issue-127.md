# Issue #127: [77] [MVP][Offline] Implementar e verificar o recorte aprovado de RNF08

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/127
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:36:59Z
- **Updated at:** 2026-09-15T01:03:30Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RNF08; RNF03/RNF11; decisão #105.

**Referência do modelo:** Fluxos offline aprovados e sua correspondência com DS09/implantação.

## O que entregar

Implementar somente dados, perfis, dispositivos e operações aprovados em #105, com comunicação clara sobre cópias locais e estado de sincronização.

## Critérios de aceite

- [ ] Perda de conexão permite apenas consultas aprovadas com indicação da última sincronização.
- [ ] Retorno da rede atualiza cópia sem apresentá-la como saldo/estado atual antes da confirmação.
- [ ] Dados locais respeitam conta e permissões definidas; não expor cópia de outro usuário.
- [ ] Criação/envio offline só se aprovado; nesse caso registrar critérios de reenvio, conflitos e duplicidade antes de implementar.
- [ ] Demonstração cobre ausência de cache, perda/retorno da rede e falha de atualização; testes não dependem apenas de navigator.onLine.

## Dependências técnicas

- [#105](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/105)

## Decisões pendentes e limites

Bloqueada até definição de #105; depois depende apenas dos produtores de dados escolhidos, não de todas as telas.

## Motivo da correção

Não impor IndexedDB, TTL, cache global ou fila de escrita como requisito decidido.

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
Aprimorar o suporte offline (**RNF08**) no frontend React, criando uma camada unificada de detecção de rede, persistência no `IndexedDB` e banner global de conectividade com sincronização automática em segundo plano ao restabelecer a conexão.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 8 - Frontend Final, Auditoria e Homologação
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#94: [MVP][Frontend] Implementar Telas de Login, Gestão de Usuários e Cache Offline Local)
  - [?] (#100: [MVP][Frontend] Construir Interface de Registro de Ocorrências, Triagem e Planos de Ação)
  - [?] (#105: [MVP][Frontend] Implementar Cache Local e Suporte Offline para Catálogo e Estoque de EPIs)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#121: [MVP][Transversal] Implementar Trilha de Auditoria Imutável (RNF05))
  - [?] (#129: [MVP][Transversal] Validar Integridade End-to-End, Documentação Técnica e Homologação do MVP)

## 3. O Que Fazer
1. Desenvolver `NetworkStatusProvider` no React monitorando `navigator.onLine` e eventos de rede.
2. Criar banner flutuante no layout principal informando o status da conexão:
   - Verde: "Online e sincronizado".
   - Âmbar: "Modo Offline - Consultando dados locais".
3. Implementar gatilho de revalidação em segundo plano que atualiza o cache local quando a conexão for retomada.
4. Seguir padrões de acessibilidade WCAG 2.1 AA.

## 4. Como Fazer
- Hook global `useNetworkStatus()`.
- Integração com `localStorage` / `IndexedDB`.


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
- [ ] Banner de rede reage em tempo real a quedas de conexão.
- [ ] Cache local permanece íntegro e legível offline em todos os módulos.
- [ ] Testes de componentes frontend cobrindo transições de estado online/offline.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

