# Issue #94: [46] [MVP][Frontend] Integrar login e gestão de supervisores e colaboradores

- **Status:** Open
- **URL:** https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/94
- **Author:** @sidneicjr28 (Sidnei Jr)
- **Assignees:** Amanda Dias
- **Labels:** None
- **Milestone:** None
- **Created at:** 2026-09-11T01:20:50Z
- **Updated at:** 2026-09-15T01:01:53Z

---

## Description

<!-- safeplace-consistency-review-v1 -->
> **Escopo vigente revisado.** As decisões pendentes abaixo são bloqueios explícitos da parte afetada; não devem ser resolvidas por suposição.

## Escopo e rastreabilidade

**Classificação:** MVP ou consistência documental necessária ao MVP.

**Requisitos e histórias:** RF23; RNF03; RNF11; US23/US24.

**Referência do modelo:** Contas de Supervisor/Gestor separadas do cadastro de Colaborador.

## O que entregar

Entregar interface de login por e-mail/senha e cadastro, consulta e atualização de pessoas pelas operações autorizadas. Integrar API real, reaproveitando layout e componentes existentes.

## Critérios de aceite

- [ ] Gestor gerencia supervisores; Supervisor gerencia colaboradores; interface e API aplicam a mesma matriz.
- [ ] Senha inicial aparece somente no resultado de criação de Supervisor; Colaborador não tem formulário de credenciais.
- [ ] Busca e atualização mostram dados persistidos após recarregar; duplicidades/erros permitem correção.
- [ ] Formulários possuem rótulos, navegação por teclado e apresentação responsiva; validar fluxo completo com backend.

## Dependências técnicas

- [#90](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/90)
- [#91](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/91)
- [#122](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/122)

## Decisões pendentes e limites

Operações básicas já disponíveis podem ser integradas; atualização aguarda #122. Offline depende exclusivamente da decisão em #105.

## Motivo da correção

Retirado cache de colaboradores como exigência presumida de RNF08; não impor localStorage para token nem nova política de autenticação.

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
Criar a interface gráfica para autenticação e gestão de usuários em React 19 / TypeScript, integrando com os endpoints de autenticação e usuários, além de implementar o cache offline local para consulta do catálogo de colaboradores (**RNF08** e **RNF11**).

## 2. Dependências e Ordem Cronológica
- **Fase Cronológica**: Semana 3 - Frontend de Usuários e Gestão de Manutenção
- **Dependências diretas (Pré-requisitos)**:
  - [?] (#93: [MVP][Usuários] Implementar Controladores Inbound, DTOs e Casos de Uso de Usuários)
  - [?] (#122: [MVP][Usuários] Implementar Atualização de Cadastro e Desativação Preservando Histórico)
- **Issues dependentes (Bloqueadas por esta issue)**:
  - [?] (#100: [MVP][Frontend] Construir Interface de Registro de Ocorrências, Triagem e Planos de Ação)
  - [?] (#104: [MVP][Frontend] Construir Visualização e Gestão de Manutenção de EPIs no Frontend)
  - [?] (#115: [MVP][Frontend] Construir Mapa de Áreas de Risco e Painel de Tarefas no Frontend)
  - [?] (#120: [MVP][Frontend] Construir Painel de Certificações, Treinamentos e Indicadores de Bloqueio no Frontend)

## 3. O Que Fazer
1. Implementar o `AuthContext` e tela de Login:
   - Formulário de login acessível com tratamento de feedback visual.
   - Armazenamento seguro de token JWT no navegador.
   - Redirecionamento baseado no perfil logado (`GESTOR_SEGURANCA` vs `SUPERVISOR`).
2. Integrar `UsersPage.tsx`:
   - Listagem paginada de colaboradores e supervisores.
   - Modal de cadastro de Supervisor (visível apenas para Gestor).
   - Modal de cadastro de Colaborador (sem campos de credenciais/senha).
3. Suporte a Cache Offline Local (**RNF08**):
   - Salvar o catálogo de colaboradores no `localStorage` ou `IndexedDB`.
   - Permitir consulta e busca de colaboradores mesmo quando a conexão cair no canteiro de obras.
   - Adicionar badge indicando "Modo Offline" caso a API não responda.
4. Seguir acessibilidade WCAG 2.1 AA (**RNF11**) e o Design System existente em `features/design-system`.

## 4. Como Fazer
- Utilizar o cliente HTTP padronizado em `shared/api/https.ts`.
- Reutilizar componentes do Design System (`Button`, `Fields`, `Dialog`, `Feedback`).
- Isolar as chamadas à API em serviços tipados (`features/usuarios/api.ts`).


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
- [ ] Login funcional com persistência de sessão e expiração amigável.
- [ ] Telas de gestão de usuários respeitam permissões por perfil.
- [ ] Cadastro de colaborador não exibe campos de senha ou login.
- [ ] Catálogo de colaboradores pode ser consultado mesmo sem conexão de rede (cache local).
- [ ] Testes de componentes frontend passando sem erros.
- [ ] Suite de testes automatizados entregue e passando com 100% de sucesso.
- [ ] Código em conformidade com as diretrizes de Arquitetura Hexagonal e Clean Code.
- [ ] Demonstração prática verificável em execução local.
- [ ] Entrega finalizada, testada e integrada via Pull Request dentro do timebox semanal rígido.
- [ ] Software em estado funcional e demonstrável sem dependência de branches não mescladas.

</details>

