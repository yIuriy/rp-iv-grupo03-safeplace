# Correspondência entre Diagrama de Classes e RF12 / UC08 (Tarefa e EPIs Obrigatórios)

## 1. Visão Geral e Contexto

Este documento registra o contrato de vinculação de Equipamentos de Proteção Individual (EPIs) obrigatórios às tarefas do SafePlace. Ele concilia a classe `Tarefa` e sua associação com `EPI` do **Diagrama de Classes** com o **RF12**, o **UC08 (Interligar Tarefa ao EPI)** e a **US12**, atendendo à [issue #135](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/135) (RF12; UC08; US12).

- **Situação**: contrato aprovado e implementado (RF12, UC08, US12, issue #135). RF12 é classificado como "Deveria ter" na MoSCoW. As pendências da seção 6 não foram decididas e não devem ser supridas por suposição.
- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [UC08](../casos-de-uso/casos-de-uso.md#uc08--interligar-tarefa-ao-epi), [US12](../historias-de-usuario.md#us12--associar-epis-obrigatórios-às-tarefas), [RF12](../requisitos/requisitos-funcionais.md#rf12-o-sistema-deve-permitir-a-conexão-entre-as-tarefas-e-os-epis), [MoSCoW](../requisitos/priorizacao-moscow.md), [Especificação do MVP](../mvp/especificacao-mvp-arquitetura.md) e [RNF03](../requisitos/requisitos-nao-funcionais.md).
- **Implementação de referência**: domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/tarefa/` (`Tarefa`) e `domain/epi/` (`Epi`); caso de uso `TarefaUseCase`; persistência `TarefaJpaAdapter` com a migração `V27__criar_tarefas_epis.sql`; API `/api/tarefas/{id}/epis`.

### 1.1. Modelo de referência

O PNG define:

- **`Tarefa`** (revisão de 16/10/2026): atributos `descricao : String`, `nivelPerigo : NivelPerigo` e `dataClassificacao : DateTime`; operações `classificar(nivel : NivelPerigo, responsavel : GestorDeSeguranca)`, `estaClassificada() : boolean`, `associarEPIs(listaIds : List<int>) : boolean` e `verificarEPIsObrigatoriosTarefa() : List<EPI>` (as duas últimas de RF12, fora do MVP). A operação `consultarTarefas()` foi retirada do PNG, pois a consulta pertence à porta de entrada `ClassificarTarefaUseCase.listar`. Na versão anterior, o atributo de nível se chamava `status : NivelPerigo`.
- **`EPI`**: catálogo de equipamentos de proteção individual.
- **Associação entre `Tarefa` e `EPI`**: relação de muitos para muitos (`Tarefa` 0..* para `EPI` 0..*).

---

## 2. Decisões de Modelo Propostas

### 2.1. Associação direta Tarefa–EPI sem `TipoEpi` obrigatório

- **Decisão**: a associação é direta entre `Tarefa` e instâncias de `EPI` cadastradas no catálogo, persistida em tabela associativa `tarefas_epis` (`tarefa_id`, `epi_id`).
- **Fundamento estrutural**: o Diagrama de Classes conecta diretamente `Tarefa` a `EPI` através de `associarEPIs(listaIds : List<int>)` e `verificarEPIsObrigatoriosTarefa() : List<EPI>`. Não existe a classe nem a abstração `TipoEpi` no diagrama aprovado; criar um novo tipo intermediário inventaria estrutura não observada no modelo.

### 2.2. Ator e controle de acesso (RBAC)

- **Quem altera**: o Gestor de Segurança, ator principal de UC08 e US12 (regra 1). Supervisor e demais perfis não podem alterar a matriz de EPIs por tarefa (US12 critério 6).
- **Quem consulta**: tanto Gestor de Segurança quanto Supervisor podem consultar os EPIs obrigatórios vinculados a uma tarefa para orientação operacional e fiscalização.
- **Regra de acesso**: no `SecurityConfig`, `PUT /api/tarefas/{id}/epis` é exclusivo do papel Gestor de Segurança (`GESTOR_SEGURANCA` / `ROLE_GESTOR_SEGURANCA`), retornando HTTP 403 para outros papéis. A consulta `GET /api/tarefas/{id}/epis` é liberada para Gestor e Supervisor.

### 2.3. Desacoplamento de UC03 (EPIs de acesso à área)

- Os EPIs exigidos para executar uma tarefa (UC08 / RF12) são conceitualmente e operacionalmente separados dos EPIs exigidos para acesso físico a uma área de risco (UC03 / RF05).
- A tabela `tarefas_epis` registra a proteção mandatória da atividade laboral em si, enquanto `areas_risco_epis` registra a proteção de adentramento ao espaço geográfico delimitado. Uma tarefa pode ser executada sem depender das restrições espaciais da área onde se encontra.

### 2.4. Resolução dos conflitos de parametrização e regras de negócio

- **Sem matriz normativa nem regras de altura inventadas**: a proposta histórica anterior continha regras arbitrárias (ex.: obrigar cinto paraquedista acima de 2m ou impor matriz normativa automática entre periculosidade e EPIs). Tais regras foram removidas do escopo vigente pois não possuem respaldo nos requisitos aprovados.
- **Parametrização por nível de perigo e classe mínima (UC08 Alternativo I / US12 RN5)**: critérios de classe mínima e sugestão automática por grau de perigo permanecem classificados como "A confirmar" e congelados no backlog. A vinculação de EPIs é explícita e conduzida pelo Gestor de Segurança a partir do catálogo.
- **Incompatibilidade técnica entre modelos de EPI (UC08 Exceção III / US12 RN5)**: os critérios de compatibilidade física entre equipamentos estão pendentes de definição formal de catálogo. O sistema não impõe bloqueios especulativos sem matriz cadastrada.
- **Tarefa sem EPI associado (UC08 Exceção I / US12 CA4)**: uma tarefa pode não exigir nenhum EPI obrigatório específico (lista vazia). A API aceita a lista vazia de IDs de EPI para desassociar ou registrar tarefa que não demande equipamentos específicos.
- **Validação de existência dos EPIs**: todos os IDs informados em `associarEPIs` devem corresponder a EPIs existentes e ativos no catálogo. IDs inexistentes resultam em erro com HTTP 400 ou 404, não alterando os vínculos pré-existentes.
- **Não bloqueio do MVP**: o cadastro/classificação de tarefas (RF06, UC11) e o controle de alocações/capacitações (RF10, UC02) não são bloqueados nem exigem EPIs pré-vinculados para operar.

---

## 3. Matriz de Correspondência: Diagrama de Classes e UC08 vs. Arquitetura Hexagonal

| Elemento no Diagrama / UC08 | Camada Hexagonal e Destino | Papel Arquitetural e Semântica |
| :--- | :--- | :--- |
| `associarEPIs(listaIds : List<int>) : boolean` | Domínio `Tarefa.associarEpis(List<Epi>)`<br>Porta de entrada `GerenciarEpiTarefaUseCase.associarEpis`<br>Porta de saída `TarefaRepositoryPort.salvarVinculosEpis` | Associa a lista de EPIs à tarefa informada, substituindo a lista anterior pelos itens vigentes. |
| `verificarEPIsObrigatoriosTarefa() : List<EPI>` | Domínio `Tarefa.verificarEpisObrigatoriosTarefa()` e `Tarefa.getEpisObrigatorios()`<br>Porta de entrada `GerenciarEpiTarefaUseCase.listarEpisObrigatorios` | Retorna a lista de EPIs vinculados como obrigatórios para a atividade. |
| Associação `Tarefa` 0..* <---> 0..* `EPI` | Persistência JPA: `tarefas_epis`<br>`TarefaEntity.epis` (`@ManyToMany`) | Chaves estrangeiras `tarefa_id` e `epi_id` com deleção em cascata na associação. |
| Endpoint de associação | Adaptador web: `PUT /api/tarefas/{id}/epis` | Exclusivo do Gestor de Segurança (`GESTOR_SEGURANCA` / `ROLE_GESTOR_SEGURANCA`). Recebe `AssociarEpisTarefaRequest` com lista de IDs de EPIs. |
| Endpoint de consulta | Adaptador web: `GET /api/tarefas/{id}/epis` | Disponível para Gestor e Supervisor. Retorna lista de `EpiResponse`. |

Corpo da requisição de associação (`PUT /api/tarefas/{id}/epis`):

```json
{
  "epiIds": [1, 3]
}
```

---

## 4. Verificação

- **Domínio**: `TarefaTest` cobre o cadastro de tarefa com EPIs, a operação `associarEpis`, `verificarEpisObrigatoriosTarefa`, a preservação da lista imutável e a associação com lista vazia.
- **Caso de uso**: `TarefaUseCaseTest` cobre a associação de EPIs pelo caso de uso, validação de IDs de EPI inexistentes e consulta dos EPIs vinculados.
- **API**: `TarefaControllerTest` cobre a rota `PUT /api/tarefas/{id}/epis` com resposta 200 OK para Gestor, recusa com 403 Forbidden para Supervisor, e rota `GET /api/tarefas/{id}/epis` acessível para ambos os perfis.
- **Persistência PostgreSQL**: testes de integração cobrem a persistência dos vínculos em `tarefas_epis`, recarregando a tarefa e seus EPIs após limpeza de contexto de persistência.

---

## 5. Rastreabilidade com Casos de Uso e Requisitos

| Fonte | Item | Atendimento no contrato |
| :--- | :--- | :--- |
| RF12 | O sistema deve permitir a conexão entre as tarefas e os EPIs | Relação N:N direta entre Tarefa e EPI via `tarefas_epis` e métodos de vinculação e consulta (2.1, 3). |
| UC08, passos 1 a 6 | Gestor seleciona tarefa e confirma EPIs de uso obrigatório | Rota `PUT /api/tarefas/{id}/epis` exclusiva para Gestor de Segurança (2.2, 3). |
| UC08, regra 2 / US12, regra 1 e CA6 | Apenas o Gestor de Segurança pode alterar os vínculos | Proteção por perfil `ROLE_GESTOR` no `SecurityConfig` com bloqueio 403 para Supervisor (2.2). |
| UC08, Exceção I / US12, regra 4 | Confirmação de tarefa sem EPI | Permissão de envio de lista vazia de EPIs (2.4). |
| RF06 / RF10 | Classificação de tarefas e capacitações de alocação | Não ficam bloqueados nem dependentes de EPIs vinculados (2.4). |
| RNF03 | Controle de acesso por perfil | Verificação de perfis RBAC em endpoints protegidos (2.2). |

---

## 6. Pendências Não Resolvidas por Este Contrato

| Ponto | Situação |
| :--- | :--- |
| Sugestão automática de EPIs no empréstimo | UC08 regra 1 e UC12: quando um colaborador for retirar EPIs para uma tarefa, o sistema poderá sugerir os EPIs da tarefa. Pertence ao fluxo de empréstimo (UC12) e permanece no backlog. |
| Matriz normativa automatizada por nível de risco | A confirmar. O sistema adota vínculo manual explícito sem deduções automatizadas. |
| Matriz de incompatibilidade técnica física entre EPIs | A confirmar no catálogo de modelos de EPI. Não foram estabelecidas regras técnicas pelo projeto até esta revisão. |
| Atualização textual de UC08 e US12 | Poderá ser formalizada após validação desta proposta pelo grupo. |
