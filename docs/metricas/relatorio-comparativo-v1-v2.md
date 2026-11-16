# Métricas de Código — Comparativo v1.0.0 × v2.0.0 (prévia)

> **Prévia.** Coletada no commit `0509a65` (branch `Sidnei`, igual à `dev` `bc5ec56` mais o PR #313), em 16/11/2026. Ainda faltam [#259](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/259), [#262](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/262) e [#307](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/307). Depois delas, a coleta é refeita na `dev` e gera `metricas-v2.0.0.json`, e a release `v2.0.0` é criada.

Compara a [linha de base v1.0.0](relatorio-metricas-v1.0.0.md), coletada antes da aplicação dos padrões, com o código depois das issues `padrao-de-projeto`. O objetivo é mostrar o benefício de cada padrão e explicar as métricas que pioraram.

Dados brutos:

- [`metricas-v1.0.0.json`](metricas-v1.0.0.json): linha de base.
- [`metricas-v2.0.0-previa.json`](metricas-v2.0.0-previa.json): mesma condição da base, sem os testes de integração.
- [`metricas-v2.0.0-previa-integracao.json`](metricas-v2.0.0-previa-integracao.json): com os testes de integração ativados pela [#248](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/248).

## 1. Como foi coletado

```bash
# 1. Mesma condição da base: as 22 classes de integração com PostgreSQL ficam de fora.
cd backend
EXCLUIR=$(grep -rl PostgresDeTeste src/test/java | grep -v suporte/PostgresDeTeste.java \
  | xargs -n1 basename | sed 's/\.java$//; s/^/!/' | paste -sd,)
./mvnw clean test "-Dtest=$EXCLUIR"
cd ..
python scripts/metricas/coletar_metricas.py --frontend-ferramentas --saida docs/metricas/metricas-v2.0.0-previa.json

# 2. Com a integração (Testcontainers 1.21.4 sobe o PostgreSQL sozinho).
cd backend && ./mvnw clean test && cd ..
python scripts/metricas/coletar_metricas.py --frontend-ferramentas --saida docs/metricas/metricas-v2.0.0-previa-integracao.json

# Comparativo
python scripts/metricas/coletar_metricas.py --comparar docs/metricas/metricas-v1.0.0.json docs/metricas/metricas-v2.0.0-previa.json
```

- **Mesma condição da base.** Na v1.0.0 os testes de integração foram **ignorados** (131 testes em 22 classes), porque o Testcontainers 1.20.4 não alcançava o Docker. Hoje eles rodam. Para comparar a cobertura de forma justa, a coleta 1 exclui as mesmas 22 classes. Esconder o Docker por variável de ambiente não funciona, porque o Testcontainers guarda a estratégia em `~/.testcontainers.properties`.
- **Ambiente.** JDK 25 (o Lombok não compila no JDK 27), `JAVA_TOOL_OPTIONS=-Duser.timezone=America/Sao_Paulo`, Docker 29.2.1 e Node com as dependências de `npm ci`.
- **Limites.** Os mesmos da base: método longo acima de 30 linhas, classe grande acima de 300 e mais de 5 parâmetros.

## 2. Critérios de aceite da #235

| Critério | Base | Prévia | Situação |
| --- | ---: | ---: | --- |
| Cobertura de ramos, mesma condição da base | 68,6% | **72,2%** | Atendido |
| Cobertura de ramos, com a integração | — | **75,3%** | Atendido |
| Falhas e erros no backend | 0 | **0** (1.257 testes; 1.388 com a integração) | Atendido |
| Tabela por padrão com antes, depois e variação | — | [seção 4](#4-benefício-por-padrão) | Atendido |

Numa das execuções com integração, `OcorrenciaPersistenciaIntegracaoTest.deveEmitirProtocolosDistintosSobConcorrencia` deu erro (`UnexpectedRollbackException`). Nas outras duas execuções completas passou. O teste dispara 8 threads que disputam a sequência da CAT, então a falha é intermitente. Ela não entra na prévia, mas precisa ser investigada antes da coleta final.

## 3. Resumo

| Indicador | v1.0.0 | Prévia | Variação |
| --- | ---: | ---: | ---: |
| Testes do backend executados | 1.071 (131 ignorados) | 1.257 (sem a integração) / 1.388 (com) | +186 / +317 |
| Cobertura de linhas | 80,1% | 87,5% / 93,0% | +7,4 / +12,9 p.p. |
| Cobertura de ramos | 68,6% | 72,2% / 75,3% | +3,6 / +6,7 p.p. |
| Cobertura de linhas em `adapters/out/persistencia` | 49% | 91,8% (com) | +42,8 p.p. |
| Duplicação no backend | 11,48% | 7,04% | −4,44 p.p. |
| Linhas duplicadas | 1.055 | 677 | −378 (−36%) |
| Métodos com complexidade acima de 10 | 12 | 5 | −7 |
| Complexidade média por método | 1,79 | 1,68 | −0,11 |
| `if` no backend | 487 | 410 | −77 |
| `throw new` no backend | 214 | 110 | −104 |
| `controller_advices` / `exception_handlers` | 8 / 37 | 4 / 35 | −4 / −2 |
| `SecurityContextHolder` no código de produção | 27 | 10 | −17 |
| `useState` no frontend | 202 | 169 | −33 |
| Casos de teste Playwright (`test(`) | 150 | 203 | +53 |
| Linhas de código do backend (`main`) | 15.086 | 17.001 | +1.915 |
| Bundle JS do frontend | 527,1 KiB | 611,0 KiB | +83,9 KiB |

As duas últimas linhas pioraram; a [seção 6](#6-métricas-que-não-melhoraram) explica por quê.

## 4. Benefício por padrão

Cada linha traz a métrica-alvo que motivou o padrão. Os valores **antes** vêm da seção do padrão em [`padroes-de-projeto.md`](../arquitetura/padroes-de-projeto.md), que traz também o problema, os papéis, o código antes e depois e as evidências. Os valores **depois** foram conferidos de novo no commit desta prévia. Os diagramas antes × depois de cada padrão estão no link da última coluna.

| Padrão | Issues | PRs | Métrica-alvo | Antes | Depois | Variação | Diagramas |
| --- | --- | --- | --- | ---: | ---: | ---: | --- |
| Strategy (frontend) | #232 | #251 | Comparações `status === 4xx` escritas nas telas | 36 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#strategy--mensagens-de-falha-http) |
| Adapter (operador autenticado) | #222, #236 | #252, #288 | Arquivos de `application/` que importam Spring Security | 7 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#adapter--porta-de-operador-autenticado) |
| Facade (auditoria) | #223, #237, #238 | #289, #293, #294 | Classes que constroem `LogAuditoria` diretamente | 12 | 1 | −92% | [ver](../arquitetura/padroes-de-projeto.md#facade--registrador-de-auditoria) |
| State (backend) | #224 | #289 | Atribuições a `status` no `Epi` fora do construtor | 8 | 1 | −88% | [ver](../arquitetura/padroes-de-projeto.md#state--transicoes-de-statusepi) |
| Factory Method | #225, #268 | #289, #306 | Formas de criar um `Epi` / métodos estáticos de criação em `domain/capacitacao` | 10 / 4 | 1 / 0 | −90% / −100% | [ver](../arquitetura/padroes-de-projeto.md#factory-method--criação-de-epi) |
| Abstract Factory | #239 | #291, #298 | `instanceof Acidente` e `instanceof Incidente` em `domain/` e `application/` | 13 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#abstract-factory--família-de-relato-de-ocorrências) |
| Builder | #226, #240 | #289, #293 | Chamadas a construtores de 12 ou 16 parâmetros | 38 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#builder--emprestimobuilder-tarefabuilder-e-builders-de-ocorrencia) |
| Null Object | #227, #241 | #280, #281 | `this(..., null)` e `@Autowired(required = false)` nos casos de uso | 6 + 2 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#null-object--adaptador-nulo-de-notificacao-e-dependencias-opcionais) |
| Chain of Responsibility | #228, #263 a #267, #297 | #274, #276 a #279, #275, #304 | Maior complexidade nas regras de aptidão / complexidade de `calcularProjecao` | 13 / 21 | 9 / 10 | −31% / −52% | [ver](../arquitetura/padroes-de-projeto.md#chain-of-responsibility--camada-de-validação) |
| Value Object | #230 | #284 | Conversões VO → `String` → VO em produção | 3 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#value-object--protocolo-cat-em-acidente) |
| DTO Assembler | #245 | #289 | Complexidade do método que monta `OcorrenciaOutputDTO` | 15 | 1 (+3 no auxiliar) | −73% | [ver](../arquitetura/padroes-de-projeto.md#dto-assembler--ocorrenciaoutputdto) |
| Strategy (backend, `Clock`) | #247 | #292 | `LocalDate.now()` nas regras de vencimento de `Epi` e `Certificacao` | 6 | 0 | −100% | [ver](../arquitetura/padroes-de-projeto.md#strategy--fonte-de-tempo-clock) |
| Adapter (frontend) | #244, #231 | #285, #287 | Cópias locais de primitivas de leitura da API / tipos `Epi` exportados | 17 / 2 | 0 / 1 | −100% / −50% | [ver](../arquitetura/padroes-de-projeto.md#adapter--leitura-da-api-e-contrato-de-epi) |
| Template Method (hook, frontend) | #233, #243 | #282, #283 | Componentes com `useState` próprio de `enviando` | 19 | 4 | −79% | [ver](../arquitetura/padroes-de-projeto.md#template-method-hook--envio-de-formulario) |
| State (frontend) | #246 | #302 | `useState` de controle de diálogos em `TasksPage` | 6 | 1 | −83% | [ver](../arquitetura/padroes-de-projeto.md#state--dialogos-de-taskspage) |
| Bridge | #299 | #300 | Classes a criar para um 3º formato de relatório (contra herança por combinação) | 3 | 1 | −67% | [ver](../arquitetura/padroes-de-projeto.md#bridge--relatórios-estatísticos-e-formatos-de-arquivo) |
| Template Method (backend) | #299 | #300 | Esqueleto dos relatórios: `Relatorio.gerar` é `final` e cada relatório só implementa os passos | — | 3 relatórios | funcionalidade nova | [ver](../arquitetura/padroes-de-projeto.md#template-method-backend--esqueleto-dos-relatórios-estatísticos) |

Notas da tabela:

- **Bridge e Template Method (backend)** entraram com o RF18, uma funcionalidade nova. Não há código antes. O "antes" do Bridge é a alternativa por herança (3 relatórios × 2 formatos), medida na própria seção.
- **Facade.** A classe que restou é a própria Facade, `RegistradorAuditoria`. O `LogAuditoria` também aparece na busca porque guarda as próprias fábricas.
- **State (backend).** A atribuição que restou é o `transicionarPara`; a do construtor define a situação inicial e não conta como transição.

## 5. Decisão de arquitetura: entidades JPA no domínio

O Data Mapper ([#229](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/229) e [#242](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/242)) saiu do plano. A [decisão #254](../arquitetura/decisao-entidades-jpa-dominio.md) mapeou o próprio domínio com JPA (#255 a #262), então ele é tratado aqui como decisão de arquitetura e não como padrão aplicado.

| Métrica | v1.0.0 | Prévia | Variação |
| --- | ---: | ---: | ---: |
| Classes espelho `*Entity` / `*Entidade` | 21 / 3 | 5 / 0 | −19 |
| Linhas de código em `adapters/` | 6.414 | 5.027 | −1.387 (−22%) |
| Arquivos em `adapters/` | 140 | 129 | −11 |
| Arquivos de `domain/` que importam framework (`jakarta.persistence`) | 0 | 25 | +25 (esperado pela decisão) |

As 5 classes espelho que restam (`ManutencaoEpiEntity`, `ProjecaoSubstituicaoEntity`, `PlanoDeAcaoEntity`, `ProrrogacaoPrazoEntity` e `NotificacaoAcaoEntity`) saem em #259 e #262, que ainda estão abertas.

## 6. Métricas que não melhoraram

| Métrica | v1.0.0 | Prévia | Explicação |
| --- | ---: | ---: | --- |
| Linhas de código do backend (`main`) | 15.086 | 17.001 | Entre as versões entraram funcionalidades novas: os relatórios do RF18 (`relatorio/`, `FormatoPdf` com 264 linhas, `FormatoCsv`), a camada `validacao` (55 arquivos, 1.339 linhas) e as classes dos próprios padrões (fábricas, builders, Facade). Ao mesmo tempo, `adapters/` perdeu 1.387 linhas e a duplicação caiu de 11,5% para 7,0%. |
| Complexidade total dos métodos | 2.125 | 2.358 | Acompanha o código novo. A média por método caiu (1,79 → 1,68), assim como os métodos acima de 10 (12 → 5) e o máximo (28 → 26). |
| Bundle JS do frontend | 527,1 KiB | 611,0 KiB | A página de relatórios do RF18 e as bibliotecas `zod` e `@brazilian-utils` (#308) entraram no bundle. A #250 tirou o `@tanstack/react-query`, que não era usado, e cortou cerca de 1 KiB ao reunir os formatadores. Dividir o código por rota (`React.lazy`) é a próxima medida, fora do escopo dos padrões. |
| Classes com 3 ou mais construtores | 8 | 23 | Três grupos. 8 entidades (`Tarefa`, `Certificacao`, `Treinamento`, `LoteEPI`, `ModeloEPI`, `Ocorrencia`, `AreaRisco`, `PlanoDeAcaoEntity`) têm o construtor protegido sem argumentos que o JPA exige (decisão #254). 6 validadores da Chain of Responsibility têm sobrecargas para receber o `Clock` ou elos de teste. 7 serviços, `RegistradorAuditoria` e `TarefaOutputDTO` mantêm construtores de conveniência usados por testes sem contexto Spring. O pior caso é `CadastrarCapacitacaoService`, com 6: é dívida a declarar. |
| `domain/` importa `application/` | 0 arquivos | 4 | `FabricaCapacitacao`, `FabricaCapacitacaoPadrao` (#268), `FabricaRelatoAcidente` e `FabricaRelatoIncidente` (#239) recebem o `InputDTO` da aplicação. A dependência fica invertida em relação à regra hexagonal. Para corrigir, as fábricas receberiam um objeto de entrada do próprio domínio. Fica como **violação conhecida**. |
| `domain/` importa framework | 0 arquivos | 25 | São as anotações `jakarta.persistence`, permitidas pela decisão #254. Nenhum arquivo de `domain/` importa Spring. |
| `requireNonNull` | 14 | 65 | Intencional: com o Null Object (#241), as dependências dos casos de uso passaram a ser obrigatórias e são conferidas no construtor. |
| `instanceof` | 26 | 33 | Sumiram os 13 de `Acidente` e `Incidente` (#239). Os novos estão no tratamento de erro inesperado (`ErroInesperadoExceptionHandler`, 6, da #249) e na camada `validacao`, que usa *pattern matching* para conferir campos que só o `Acidente` tem e o tipo de perfil do usuário. |
| Comparações com `null` | 578 | 585 | Ficaram estáveis: o Null Object tirou as do notificador e das dependências opcionais (595 → 586 na #241), mas o código novo trouxe as suas. |
| Classes grandes (acima de 300 linhas) | 2 | 3 | `OcorrenciaService` (348), `Epi` (320) e `EmprestimoService` (304). Os três concentram regras dos módulos mais antigos e são candidatos a extrair casos de uso menores. |
| Tempo da suíte com a integração | 31,3 s (sem integração) | 73,8 s | Os 131 testes que antes eram ignorados agora sobem um PostgreSQL. Sem eles, a suíte leva 22,4 s, menos que na base. |

## 7. Métricas globais do backend

Recorte do `--comparar` (mesma condição da base). A saída completa sai do comando da seção 1.

| Métrica | v1.0.0 | Prévia | Δ |
| --- | ---: | ---: | ---: |
| Arquivos (`main`) | 357 | 433 | +76 |
| Métodos | 1.189 | 1.401 | +212 |
| Métodos longos (mais de 30 linhas) | 16 | 12 | −4 |
| Métodos com mais de 5 parâmetros | 158 | 149 | −9 |
| Métodos com complexidade acima de 5 | 53 | 44 | −9 |
| `switch` | 5 | 9 | +4 |
| Endpoints REST | 75 | 77 | +2 |
| Sufixos em português (`Porta`, `CasoDeUso`, `Adaptador` etc.) | 38 | 0 | −38 (#269) |
| Classes de teste | 123 | 177 | +54 |
| Métodos `@Test` | 891 | 1.135 | +244 |
| Linhas de comentário (`main`) | 1.478 | 2.319 | +841 |

## 8. Pendências para a versão final

- [ ] Integrar #259, #262 e #307 na `dev`.
- [ ] Investigar a falha intermitente de `deveEmitirProtocolosDistintosSobConcorrencia`.
- [ ] Refazer as duas coletas na `dev` e gravar `metricas-v2.0.0.json`, com e sem a integração.
- [ ] Atualizar este relatório (seções 2 a 7) e tirar o aviso de prévia.
- [ ] Criar a release `v2.0.0` a partir da `dev`.
