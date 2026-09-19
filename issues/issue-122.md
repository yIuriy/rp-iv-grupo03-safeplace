# Issue #122: [44] [MVP][Usuários] Atualizar cadastros preservando identidade e vínculos

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/122
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** None
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:36:48Z
- **Updated at:** 2026-09-15T01:03:13Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF23; RNF03/RNF05; US23/US24.

**Referência do modelo:** Dados de Colaborador herdados por Supervisor/Gestor e referências existentes.

## O que entregar

Completar atualização cadastral com autorização por papel e preservação de ocorrências, capacitações e empréstimos associados.

## Critérios de aceite

- [ ] Gestor atualiza supervisores; Supervisor atualiza colaboradores conforme RF23.
- [ ] Cadastro comum não ganha credenciais durante atualização; alteração não muda papel implicitamente.
- [ ] Vínculos e identidade persistem após nova consulta; duplicidade/erro não corrompem dados.
- [ ] Registro de auditoria identifica autoria e alteração.
- [ ] Campos editáveis são os aprovados; desativação/reativação e CPF imutável não entram como requisitos sem decisão.

## Dependências técnicas

- [#90](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/90)

## Decisões pendentes e limites

Política de edição e eventual desativação aguardam registro em #90. Integrar auditoria #121.

## Motivo da correção

Texto anterior inventava imutabilidade de CPF e desativação como exigência de RF23; manter atribuições documentadas.

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
Conforme o **RF23**, o sistema deve permitir a atualização cadastral de supervisores e colaboradores preservando todos os vínculos históricos com ocorrências, empréstimos de EPIs e capacitações. Além disso, colaboradores desligados devem poder ser inativados sem que seus registros passados sejam corrompidos ou excluídos.

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 2 - APIs de Usuários e Persistência de Ocorrências
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#90: [MVP][Usuários] Definir Domínio e Portas para Gestor, Supervisor e Colaborador)
  - [?] (#93: [MVP][Usuários] Implementar Controladores Inbound, DTOs e Casos de Uso de Usuários)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#94: [MVP][Frontend] Implementar Telas de Login, Gestão de Usuários e Cache Offline Local)

## 3. O Que Fazer
1. Adicionar métodos de atualização no domínio `domain.usuario`:
   - Atualizar dados de contato e setor mantendo o CPF imutável.
   - Método `desativar()` e `reativar()` preservando integridade referencial.
2. Atualizar a porta `GerenciarUsuarioUseCase` e sua implementação `UsuarioService`.
3. Criar endpoints REST em `UsuarioControlador`:
   - `PUT /api/usuarios/{id}`: atualiza dados cadastrais.
   - `PATCH /api/usuarios/{id}/status`: altera status de ativo/inativo.
4. Adicionar testes unitários e de integração validando a preservação de vínculos.

## 4. Como Fazer (Arquitetura Hexagonal)
- Entidades de domínio ricas com métodos explícitos de alteração cadastral.
- DTO de atualização com validações sintáticas.


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
- [ ] Atualização cadastral preserva chaves estrangeiras e vínculos de auditoria.
- [ ] Inativação de colaborador impede novos empréstimos sem apagar histórico.
- [ ] Testes de integração passando com sucesso.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

