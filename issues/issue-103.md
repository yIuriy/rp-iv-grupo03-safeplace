# Issue #103: [49] [MVP][Manutenção] Expor registro e consulta de manutenção ao Gestor

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/103
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:21:11Z
- **Updated at:** 2026-09-15T01:02:20Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF04; RNF03; UC01; US04.

**Referência do modelo:** obterHistoricoManutencao e ManutencaoEPI.

## O que entregar

Conectar o serviço existente à API para consultar histórico e registrar manutenção com data, descrição e resultado aprovado, atualizando situação do EPI.

## Critérios de aceite

- [ ] Gestor registra manutenção; papel sem permissão não altera dados.
- [ ] Histórico retorna registros persistidos e filtros operacionais respeitam ativos/status aprovados.
- [ ] EPI inexistente e dados inválidos não alteram histórico/saldo.
- [ ] Não exigir ordem de manutenção prévia ou tipos PREVENTIVA/CORRETIVA como campos obrigatórios sem decisão.
- [ ] Contrato OpenAPI e teste integrado comprovam registro seguido de consulta após reinício.

## Dependências técnicas

- [#102](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/102)

## Decisões pendentes e limites

Status/resultados e comportamento de CA aprovados em #124; não importar exceção futura de UC01.

## Motivo da correção

Issue antiga permitia manutenção ao Supervisor e impunha 422 por CA com regra ainda pendente.

## Fontes verificáveis

- [Diagrama de classes](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png).
- [Requisitos funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-funcionais.md) e [não funcionais](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/requisitos-nao-funcionais.md).
- [MoSCoW](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/requisitos/priorizacao-moscow.md) e [MVP, incluindo pendências da seção 11](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/mvp/especificacao-mvp-arquitetura.md).
- [Casos de uso](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/casos-de-uso/casos-de-uso.md) e [histórias de usuário](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/docs/historias-de-usuario.md).
- [Base técnica consultada](https://github.com/yIuriy/rp-iv-grupo03-safeplace/blob/34d6b6d55516e4a045d8bcb346e9aa5eb34a90b3/backend/src/main/java/br/edu/safeplace/backend/application/usecase/ControlarManutencaoEpiService.java). Evidência do estado existente, não fonte de requisito.

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
Expor endpoints REST para que supervisores e gestores consultem o estado de manutenção dos EPIs, enviem equipamentos para revisão e registrem o resultado das manutenções (**RF04, UC01**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 3 - Frontend de Usuários e Gestão de Manutenção
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#92: [MVP][Segurança] Implementar Autenticação JWT e Filtro de Autorização RBAC)
  - [?] (#102: [MVP][EPI] Implementar Persistência JPA e Migração Flyway para Histórico de Manutenção)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#104: [MVP][Frontend] Construir Visualização e Gestão de Manutenção de EPIs no Frontend)

## 3. O Que Fazer
1. Atualizar ou estender `EpiController`:
   - `POST /api/epis/{id}/manutencoes`: envia para manutenção ou registra uma manutenção concluída.
   - `GET /api/epis/{id}/manutencoes`: lista o histórico cronológico de manutenções do EPI.
   - `GET /api/epis/em-manutencao`: lista todos os EPIs que estão atualmente em reparo/manutenção.
2. Criar DTOs:
   - `RegistrarManutencaoRequest` com validações sintáticas.
   - `ManutencaoResponse` com detalhes formatados.
3. Configurar tratamento de exceções para `CertificadoAprovacaoVencidoException` retornando `422 Unprocessable Entity` com mensagem normativa explicativa.

## 4. Como Fazer (Arquitetura Hexagonal)
- Integrar com `ControlarManutencaoEpiUseCase` via injeção de dependência na camada de aplicação.
- Documentar endpoints no OpenAPI Swagger.


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
- [ ] Endpoints protegidos por autenticação JWT.
- [ ] Registro de manutenção executado e refletido no status do EPI.
- [ ] Tentativa de manutenção em EPI com CA vencido bloqueada com código de erro amigável.
- [ ] Testes de API (MockMvc) validam fluxos de sucesso e cenários de exceção.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

