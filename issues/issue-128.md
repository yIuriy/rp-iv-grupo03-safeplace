# Issue #128: [79] [MVP][Ocorrências] Consultar por protocolo, período, setor e envolvidos

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/128
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:37:01Z
- **Updated at:** 2026-09-15T01:03:31Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF01/RF02/RF13/RF16; UC05/UC09; US01/US02/US13/US16.

**Referência do modelo:** Ocorrencia.consultarOcorrencias e buscarDetalhesOcorrencia.

## O que entregar

Completar pesquisa e detalhe integrado à interface, usando protocolo de acompanhamento aprovado e filtros previstos nos UCs.

## Critérios de aceite

- [ ] Buscar protocolo retorna ocorrência correta, acidente ou incidente, sem confundir com CAT.
- [ ] Período refere-se à data do fato; setor e envolvidos filtram vínculos persistidos.
- [ ] Combinação de filtros, nenhum resultado e registro inexistente têm retorno claro.
- [ ] Detalhes preservam informação e respeitam perfil; listagem acompanha atualização/arquivamento conforme contrato.
- [ ] Teste UI/API/banco demonstra consulta real; não exigir exportação PDF/CSV ou novo sumário independente.

## Dependências técnicas

- [#96](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/96)
- [#99](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/99)

## Decisões pendentes e limites

Protocolo/vínculos dependem #95. Interface integra #100; não é pré-requisito para implementar consulta backend.

## Motivo da correção

Exportação e endpoint de sumário não decorrem dos requisitos citados; RF18 continua backlog.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/adapters/out/persistencia/OcorrenciaJpaAdapter.java). Evidência do estado existente, não fonte de requisito.

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
Atender aos fluxos de consulta e acompanhamento de acidentes e incidentes (**RF13, RF16, UC05**):
- Fornecer busca rápida e precisa pelo número de protocolo único.
- Permitir filtros por período de ocorrência (data do fato), setor e colaboradores envolvidos.
- Gerar sumário textual legível da ocorrência para apresentação e arquivo operacional.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 8 - Frontend Final, Auditoria e Homologação
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#99: [MVP][Ocorrências] Implementar Endpoints REST para Ocorrências, Triagem e Planos de Ação)
  - [?] (#100: [MVP][Frontend] Construir Interface de Registro de Ocorrências, Triagem e Planos de Ação)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#129: [MVP][Transversal] Validar Integridade End-to-End, Documentação Técnica e Homologação do MVP)

## 3. O Que Fazer
1. Implementar filtros dinâmicos na porta de persistência `OcorrenciaRepositoryPort`.
2. Adicionar endpoint REST:
   - `GET /api/ocorrencias/sumario/{protocolo}`: retorna sumário consolidado com dados do fato, envolvidos, EPIs e planos de ação.
3. Criar modal de busca por protocolo no frontend em `OccurrencesPage.tsx`.

## 4. Como Fazer (Arquitetura Hexagonal)
- Consultas otimizadas com índices de banco de dados.
- Mapeamento em DTO de saída especializado.


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
- [ ] Busca por protocolo instantânea via backend e frontend.
- [ ] Sumário consolidado com planos de ação e status da ocorrência.
- [ ] Testes de integração validando busca e filtros temporais.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

