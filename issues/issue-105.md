# Issue #105: [58] [MVP][Decisão] Delimitar consulta offline e eventual envio de relatos

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/105
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:16Z
- **Updated at:** 2026-09-15T01:02:23Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RNF08; UC09; US13/US16/US17/US18; MVP §11.1.

**Referência do modelo:** DS09 contém fila offline; classes compartilhadas não aprovam todos os contratos da fila.

## O que entregar

Resolver conflito entre RNF08 (consulta de estatísticas/indicadores/relatórios pelo Gestor), RF17/RF18 fora do MVP e envio posterior de relatos de UC09/DS09.

## Critérios de aceite

- [ ] Registrar dados consultáveis, perfis e dispositivos abrangidos e fonte de cada informação.
- [ ] Decidir expressamente se criação/envio offline integra entrega; consulta não comprova sincronização de escrita.
- [ ] Não promover catálogo de pessoas, EPIs, empréstimos, capacitações ou mapa a cache obrigatório sem aprovação.
- [ ] Conciliar texto, DS09 e implantação, registrando cenários aprovados e pendentes.
- [ ] Definir critérios demonstráveis de perda/retorno de conexão e isolamento de dados por conta para #127.

## Dependências técnicas

Nenhuma dependência técnica obrigatória adicional para iniciar a análise/correção descrita. Observar as decisões e dependências condicionais abaixo.

## Decisões pendentes e limites

Decisão humana necessária. RNF08 continua Deve ter; pendência não o remove do MVP. Implementação é #127.

## Motivo da correção

Substitui cache de EPIs presumido e ator almoxarife sem respaldo. Não escolhe IndexedDB/localStorage, TTL ou fila como requisito de domínio.

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
Garantir o atendimento ao **RNF08 (Funcionamento offline parcial)** no módulo de EPIs:
Permitir que supervisores e almoxarifes consigam consultar a lista de EPIs, saldos atuais de estoque, requisitos de CA e histórico recente de manutenção em locais sem sinal de internet (canteiros remotos ou subsolos).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 5 - Rastreabilidade, Substituição e Persistência de Áreas
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#104: [MVP][Frontend] Construir Visualização e Gestão de Manutenção de EPIs no Frontend)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#127: [MVP][Frontend] Implementar Sincronização Resiliente e Detecção de Conexão Offline)

## 3. O Que Fazer
1. Desenvolver camada de cache local no frontend (utilizando `IndexedDB` ou `localStorage` com política de expiração):
   - Ao carregar dados com sucesso da API `/api/epis`, sincronizar a cópia local no storage do cliente.
   - Ao detectar perda de conexão (`navigator.onLine == false` ou falha de rede), chavear automaticamente para os dados do cache local.
2. Adicionar indicador visual de status de conectividade na tela de EPIs:
   - "Conectado" vs "Modo Offline (Dados sincronizados em: DD/MM/AAAA HH:mm)".
3. Desabilitar amigavelmente botões de escrita que dependam do backend quando desconectado, com tooltip explicativo.

## 4. Como Fazer
- Criar hook customizado `useOfflineCache<T>(key, fetcher)` reutilizável para outros módulos.
- Garantir que nenhum erro de rede trave a interface React.


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
- [ ] Catálogo e saldos de EPIs continuam visíveis mesmo desligando a rede no navegador.
- [ ] Aviso claro de modo offline presente na interface.
- [ ] Reconexão automática sincroniza a versão mais recente dos dados com o servidor.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

