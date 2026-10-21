# Análise de Padrões de Projeto — base v1.0.0

Análise do código da release [v1.0.0](https://github.com/yIuriy/rp-iv-grupo03-safeplace/releases/tag/v1.0.0) para decidir **quais padrões de projeto trazem ganho real** antes do planejamento das issues de refatoração. Complementa a [linha de base de métricas](../metricas/relatorio-metricas-v1.0.0.md).

## 1. Critério

Um padrão só é recomendado quando cumpre três condições:

1. Resolve um problema **medido no código**, com evidência de arquivo e de quantidade de ocorrências. Não basta "seria elegante".
2. O ganho supera o custo: menos duplicação, uma regra num único lugar, correção de defeito ou testabilidade.
3. Não contradiz a documentação, que é a fonte de verdade: o diagrama de classes, os contratos em `docs/arquitetura/correspondencia-*.md` e a especificação do MVP.

Vereditos usados:

- **Recomendado:** problema real e ganho claro.
- **Talvez:** ganho pequeno, ou depende de decisão.
- **Não recomendado:** exagero para o tamanho do problema, ou conflito com a documentação.

Quando a solução certa não é um padrão GoF, mas uma refatoração simples (extrair método, remover código morto), o relatório diz isso explicitamente.

## 2. Resumo executivo

O código **não precisa de muitos padrões novos**. A base hexagonal já usa os principais:

- Ports & Adapters;
- Repository;
- Specification;
- Static Factory;
- State via enum;
- Value Object.

O maior ganho está em **concentrar responsabilidades transversais que hoje estão copiadas**:

| # | Problema | Solução | Ganho esperado |
| --- | --- | --- | --- |
| 1 | Usuário autenticado resolvido de 8 jeitos, com um defeito | Porta `OperadorAutenticadoPorta` (já existe) + serviço `OperadorAtual` | ~80 linhas a menos; 7 casos de uso sem Spring Security; corrige o defeito |
| 2 | ~25 blocos de auditoria montados à mão | Facade `RegistradorAuditoria` | ~100 linhas a menos; auditoria do plano de ação sai do adaptador |
| 3 | 8 `@RestControllerAdvice` sobrepostos, 5 tratando `IllegalArgumentException` | Um advice global + fábrica de corpo de erro | ~300 linhas a menos; fim da ambiguidade; corrige NPE |
| 4 | Transições de `StatusEpi` espalhadas | State via enum, como `StatusOcorrencia` | Regras de transição num lugar só |
| 5 | 10 construtores e fábricas no `Epi`, dos quais só 2 são necessários | Static Factory (`novo`, `reconstituir`) + Test Data Builder nos testes | API do domínio clara |
| 6 | Frontend com submit, erro HTTP e parsers copiados | Hook `useEnvioFormulario`, fábrica de descritores de falha, primitivas de leitura | ~15–20 linhas a menos por formulário; um contrato de `Epi` só |

## 3. Backend: recomendados

### R1. Usuário autenticado como porta (Port / Adapter)

- **Problema:** o operador é resolvido com `SecurityContextHolder` em 8 lugares, em 7 casos de uso e no `PlanoDeAcaoJpaAdapter`.
  - `OcorrenciaUseCase`, `TarefaUseCase`, `EpiUseCase`, `ControlarManutencaoEpiService`, `CadastrarCapacitacaoService`, `InspecaoUseCase`, `PlanoDeAcaoUseCase` e `PlanoDeAcaoJpaAdapter`. São 27 usos no total.
  - Só o `UsuarioCasoDeUso` usa a porta, que já existe: `application/port/out/OperadorAutenticadoPorta.java`, com o adaptador `adapters/out/seguranca/SpringSecurityOperadorAutenticadoAdaptador.java`.
- **Defeito:** o descarte de `"anonymousUser"` existe só em `OcorrenciaUseCase`, `TarefaUseCase`, `InspecaoUseCase` e `PlanoDeAcaoJpaAdapter`.
  - Nos demais, e no próprio adaptador, a auditoria pode gravar `responsavel = "anonymousUser"` em vez de `SISTEMA`.
- **Regra divergente:** a busca do Gestor pelo e-mail está triplicada com semânticas diferentes. `TarefaUseCase` e `InspecaoUseCase` lançam `AccessDeniedException`; `PlanoDeAcaoUseCase` devolve `null`.
- **Solução:** usar a porta existente e criar um serviço de aplicação `OperadorAtual` com:
  - `emailOuSistema()`;
  - `gestorObrigatorio()`;
  - `possuiPapel(...)`, que a porta já expõe.
  - O filtro de `anonymousUser` fica no adaptador.
- **Benefício:**
  - uma regra única;
  - o defeito corrigido;
  - a violação `application_importa_spring_security` cai de 7 para 0;
  - testes sem precisar montar `SecurityContext`.
- **Custo:** baixo. Muda construtores e os testes que preparam o contexto de segurança.
- **Veredito: Recomendado. Prioridade 1**, porque destrava R2.

### R2. Registrador de auditoria (Facade)

- **Problema:** cerca de 25 blocos "montar `LogAuditoria` + `salvar`", de 4 a 7 linhas cada, espalhados em 11 classes.
  - Há 4 helpers privados equivalentes: `TarefaUseCase.registrarLog`, `EmprestimoUseCase.auditar`, `CadastrarCapacitacaoService.registrarAuditoria` e `TarefaUseCase.registrarClassificacao`.
  - `salva instanceof Acidente ? "Acidente" : "Incidente"` aparece 3 vezes em `OcorrenciaUseCase`.
- **Inconsistência arquitetural:** o plano de ação audita no adaptador de persistência (`PlanoDeAcaoJpaAdapter.salvar` e `salvarProrrogacao`). É o único caso fora da camada de aplicação, o que contraria a separação hexagonal.
- **Solução:** criar o serviço `RegistradorAuditoria`, sobre `LogAuditoriaRepositoryPort` + `OperadorAtual`, com `cadastro(entidade, id, texto)` e `alteracao(entidade, id, texto)`.
  - Cada bloco vira uma linha.
  - A auditoria do plano de ação sobe para `PlanoDeAcaoUseCase`.
  - O formato das mensagens **não muda**, então RNF05 e o histórico exibido continuam iguais.
- **Por que não AOP nem Decorator:** as justificativas dependem de dados do fluxo, como o estado anterior, o protocolo CAT gerado e o termo de cautela. Um aspecto esconderia a regra, acoplaria a aplicação ao Spring e tornaria menos explícita a falha da auditoria dentro da transação.
- **Custo:** médio. O `PlanoDeAcaoJpaAdapter` (320 linhas) cai para cerca de 170, e os testes do adaptador que verificam logs migram para o caso de uso.
- **Veredito: Recomendado. Prioridade 2.**

### R3. Tratamento de exceções centralizado

- **Problema:** há 8 `@RestControllerAdvice` e 37 `@ExceptionHandler`. Seis advices são globais, sem escopo nem `@Order`.
  - `IllegalArgumentException` é tratada em 5 advices: Epi, PlanoDeAcao, Tarefa, Usuario e Emprestimo.
  - `EpiNaoEncontradoException` é tratada em 2: Epi e Tarefa.
  - O handler global de `IllegalStateException` mora no advice de Ocorrência, e o de `MethodArgumentNotValidException` no advice de EPI.
  - O handler que vence depende da ordem dos beans. Hoje o resultado não muda só porque os corpos são idênticos.
- **Defeito latente:** cerca de 30 blocos `body(Map.of(...))`. `Map.of` lança NPE quando `ex.getMessage()` é nulo, e a resposta vira 500.
- **Solução:** um advice global com uma fábrica `corpo(status, erro, mensagem)`. O `EmprestimoExceptionHandler.corpo()` já usa `LinkedHashMap` e serve de modelo.
  - Os advices com escopo (`assignableTypes`) ficam só para os casos com campos extras: `impedimentos` em Tarefa e `emprestimoAtivoId` em Empréstimo.
  - Não é preciso Chain of Responsibility: o Spring já resolve o handler pela hierarquia de exceções.
- **Restrição de contrato:** manter exatamente o formato `{timestamp, status, error, message}`, os rótulos de `error` e os status.
  - O frontend compara `'Colaborador Não Alocável'` e lê `message`.
  - 18 arquivos de teste verificam `$.error` e `$.message`.
  - **Não** adotar `ProblemDetail` (RFC 7807): muda o JSON.
- **Veredito: Recomendado. Prioridade 3.**

### R4. Transições de `StatusEpi` como State via enum

- **Problema:** as transições de situação do EPI são ifs espalhados.
  - No `Epi`: `adicionarEstoque`, `removerEstoque`, `enviarParaManutencao`, `concluirManutencao`, `registrarDevolucaoManutencao` e `atualizarQuantidade`.
  - Na aplicação: `ControlarManutencaoEpiService` e `EmprestimoUseCase`.
  - Há inconsistências reais. `enviarParaManutencao` exige `DISPONIVEL`, mas `registrarDevolucaoManutencao` coloca `EM_MANUTENCAO` sem guarda. `EM_USO` nunca é atribuído.
- **Solução:** repetir o que já funciona em `StatusOcorrencia` e `StatusPlanoDeAcao`: cada constante declara `podeTransicaoPara`, e o `Epi` passa por um único `transicionarPara`.
  - O State GoF clássico, com uma classe por estado, é exagero para 5 estados.
- **Benefício:** as regras de situação ficam num lugar só e as inconsistências aparecem e são resolvidas. Os valores do enum não mudam, então o modelo e o banco também não.
- **Veredito: Recomendado.**

### R5. Construção do `Epi` e da `Tarefa` (Static Factory + Test Data Builder)

- **Problema no `Epi`:** 4 construtores públicos, 5 sobrecargas de `novo` e 1 `reconstituir`, num total de 10 portas de entrada.
  - Em produção, `Epi` é criado em 5 arquivos (`EpiUseCase`, `EpiEntityMapper`, `ControlarManutencaoEpiService`, `EmprestimoUseCase` e `ManutencaoEpi`). Os demais construtores servem a cerca de 20 arquivos de teste.
  - Há semântica escondida: o construtor com `id == null` valida o CA. O construtor reduzido inventa dados: nome `"EPI-"+id`, CA `"1"` e validade de hoje mais 1 ano.
- **Problema na `Tarefa`:** 4 construtores e 3 fábricas. Mistura métodos que alteram o objeto (`classificar`, `associarEpis`) com um que devolve cópia (`atualizar`). Tem aliases sem uso (`associarEPIs`, `verificarEPIsObrigatoriosTarefa`).
- **Solução:** deixar só as fábricas com significado (`novo`, `reconstituir`) e usar `EspecificacaoEPI`, que já existe, como Parameter Object.
  - Nos testes, um **Test Data Builder** ou Object Mother substitui os construtores de conveniência.
  - **Builder dentro do domínio não é recomendado:** permitiria criar objetos inválidos e apagaria a diferença entre "novo" e "reconstituído".
- **Métrica:** `classes_3_ou_mais_construtores` (hoje 8) e `top_mais_construtores`.
- **Veredito: Recomendado.** É o maior volume de alterações em testes, mas cada uma é simples.

### R6. `Ocorrencia` como `sealed` com tipo polimórfico

- **Problema:** `NotificacaoOcorrencia` decide tipo e protocolo com `instanceof`, e `OcorrenciaUseCase` repete `instanceof Acidente` 3 vezes para escolher o nome da entidade auditada.
- **Solução:** usar `sealed abstract class Ocorrencia permits Acidente, Incidente`, com `getTipo()` abstrato que devolve o `TipoOcorrencia` já existente.
  - Java 21 já está em uso, e a solução não altera atributos nem associações do diagrama.
  - Visitor seria exagero com apenas 2 subtipos.
- **Veredito: Recomendado.** Custo baixo.

### R7. Notification no validador de aptidão

- **Problema:** em `ValidadorAptidaoColaboradorService`, `validarAptidaoParaTarefa` e `obterImpedimentos` percorrem o mesmo fluxo de verificação duas vezes (complexidade 13).
- **Solução:** usar o padrão Notification (Fowler).
  - `obterImpedimentos` acumula os impedimentos.
  - `validar` chama `obterImpedimentos` e lança exceção se a lista não estiver vazia.
  - As exceções específicas (`ColaboradorNaoAlocavelException`, `TarefaSemClassificacaoException`) continuam sendo lançadas antes, como hoje.
- **Veredito: Recomendado.**

## 4. Backend: talvez

| Item | Problema | Proposta | Por que "talvez" |
| --- | --- | --- | --- |
| Value Objects | O protocolo CAT tem o VO `NumeroProtocoloCAT`, mas `Acidente` guarda `String`; a prioridade dos alertas é `String` | Guardar o VO internamente e manter o getter em `String`; enum `PrioridadeAlerta` | Ganho de tipagem pequeno. A prioridade em si ainda está "a confirmar" na US21 |
| Mappers de persistência | `AreaRisco` e `PlanoDeAcao` são mapeados duas vezes, de forma diferente, em `OcorrenciaJpaAdapter` e no adaptador próprio | Mapper escrito à mão no estilo de `EpiEntityMapper`, só para esses 2 | Padronizar todos os adaptadores não se paga. MapStruct não se encaixa nos construtores com invariantes nem na herança Acidente/Incidente |
| Descrição de alterações | 4 implementações de "de X para Y" (`UsuarioCasoDeUso`, `AreaRiscoUseCase`, `TarefaUseCase`, `OcorrenciaUseCase`); a de Ocorrência lista só os nomes dos campos | Builder simples `DescricaoAlteracoes.campo(nome, antes, depois).texto()` | Unificar o formato muda o histórico exibido. Confirmar com o DS09 antes |
| Null Object para notificação | `OcorrenciaUseCase` aceita `NotificacaoOcorrenciaPort` nula (`@Autowired(required=false)`) e testa `!= null` | Adaptador no-op | Elimina só 2 `if` e um construtor |
| Construtor único nos casos de uso | Construtores telescópicos que passam `null` em `OcorrenciaUseCase`, `EmprestimoUseCase`, `TarefaUseCase` e `PlanoDeAcaoUseCase` | Um construtor, com fakes nos testes | Correto, mas mexe em muitos testes. Fazer junto com R1 e R2 |
| Facade de consulta | `TarefaController.consultarExigencias` orquestra 3 casos de uso | Serviço de consulta na aplicação | Caso isolado |
| Relógio injetável | 46 chamadas a `now()` no domínio; `PlanoDeAcao` e `Inspecao` já recebem a data como parâmetro | `java.time.Clock` ou data de referência | Ganho de determinismo nos testes, mas a mudança é ampla |
| Operador nos controllers | 3 estilos de extrair o usuário: com fallback `"SISTEMA"`, sem checagem de nulo e com `SecurityContextHolder` | `HandlerMethodArgumentResolver` (`@OperadorAtual`) ou a porta de R1 | Fica resolvido em boa parte por R1 |

## 5. Backend: refatorações sem padrão (fazer, mas não chamar de padrão)

- **Métodos legados do `Epi`:**
  - `buscarEPIs`, `buscarEPI`, `compararComQuantidadeMinima`, `compararComQuantMinima`, `buscarEPIsAbaixoDaQuantidadeMinima`, `validarBaixa`, `darBaixa` e `buscarLote` têm zero usos em produção.
  - **Constam do diagrama de classes**, então removê-los exige decisão documental.
  - Sem mexer no diagrama, o que dá para fazer é eliminar a divergência: `darBaixa` devolve `false` onde `removerEstoque` lança exceção, e `validarBaixa` duplica a validação.
- **`AreaRisco`:** a mesma validação aparece escrita 4 vezes. Extrair `validarInvariantes()` e fazer os métodos booleanos do diagrama delegarem a ele.
- **Métodos longos:** `SubstituicaoInteligenteService.calcularProjecao` (complexidade 21) e `PlanejarSubstituicaoUseCaseImpl.gerarAnalise` pedem só Extract Method e um record de resultado.
- **`OcorrenciaUseCase`:** `enviarParaTriagem` e `arquivar` diferem só na transição. Um `transicionar(id, ação, texto)` privado resolve.
- **`OcorrenciaOutputDTO`:**
  - expõe a entidade de domínio `PlanoDeAcao`, o que viola o uso obrigatório de DTOs. Trocar por `PlanoDeAcaoOutputDTO`;
  - `deDominio` repete 24 argumentos por tipo de ocorrência.
- **Consultas N+1:**
  - `EmprestimoUseCase.listar` faz 2 buscas por item;
  - `PlanoDeAcaoJpaAdapter.listar` faz até 3 `findById` por plano;
  - `CapacitacaoJdbcAdapter.lerTreinamento` faz 1 query por linha.
- **Credenciais duplicadas:** a regra é a mesma em `Supervisor` e `GestorDeSeguranca`. Criar um helper `protected` em `Colaborador`; uma superclasse nova mudaria a hierarquia do diagrama.
- **`SecurityConfig.securityFilterChain` (74 linhas):** extrair métodos privados por módulo, **mantendo a ordem** dos `requestMatchers`.
- **Nomenclatura mista:** `Port`/`Porta`, `UseCase`/`CasoDeUso`/`Service`, `Controller`/`Controlador`, `Adapter`/`Adaptador`. Renomear em PR isolada, sem misturar com lógica.

## 6. Frontend

| Veredito | Item | Evidência | Proposta |
| --- | --- | --- | --- |
| Recomendado | Submit de formulário (Template Method em forma de hook) | `useState` de `enviando` em 20 componentes; o mesmo fluxo de enviar, validar, focar o primeiro inválido e tratar falha; `focarCampo`/`ordemDosCampos` em 12 arquivos | Hook `useEnvioFormulario({ ler, validar, ordem, enviar, descreverFalha })`, opcional para fluxos em etapas |
| Recomendado | Mensagem de erro HTTP (fábrica / tabela por status) | 15 funções `descrever*` com o mesmo esqueleto; o texto de 403 repetido em pelo menos 4 lugares | `criarDescritorDeFalha({ 403: …, 404: …, padrao: … })` em `shared/api/erros.ts`, preservando cada texto |
| Recomendado | Adapters de resposta da API | Dois adapters e **dois tipos `Epi`** para o mesmo `GET /epis` (`epis/api.ts` e `estoque/api.ts`); `textoOuNulo` em 4 lugares com semântica diferente, um sem `trim` | Unificar o `Epi`; primitivas de leitura em `shared/api/leitura.ts` (`texto`, `textoOuNulo`, `numeroOuNulo`, `lista`) |
| Recomendado (sem padrão) | Formatação e busca | `normalizar` copiado 5 vezes; 8 variantes de formatar data, algumas com saída diferente | Módulo `shared/formatos.ts`, mantendo a saída de cada tela |
| Recomendado | Estado de diálogos em `TasksPage` | 5 `useState<Tarefa \| null>` mais flags de abertura | Um estado em união discriminada (`{ tipo, tarefa }`). Nas páginas com 2 diálogos, não compensa |
| Talvez | React Query, react-hook-form e zod | Estão no `package.json`, mas **nenhum arquivo os importa**; o cache é manual (`useConsulta`, bump de versão, evento global) | Decidir entre migrar (40 call sites e o fluxo offline do RNF08) ou remover as dependências. Mantê-las sem uso é a pior opção |
| Talvez | Permissão por perfil | `perfil === 'GESTOR_SEGURANCA'` em 10 lugares | Hook `useEhGestor()`. É só legibilidade: o backend já autoriza |
| Não recomendado | Validação por Strategy | Os 8 `validacao*.ts` já seguem a mesma estrutura | Duplicaria a estrutura sem reduzir código |

Os testes Playwright dependem de labels, papéis e textos, então todas as mudanças de frontend devem manter idênticas as strings visíveis.

## 7. Padrões descartados (seriam só para pontuar)

| Padrão | Onde poderia ser "encaixado" | Por que não |
| --- | --- | --- |
| AOP / Decorator de auditoria | Casos de uso | As justificativas dependem de dados do fluxo; acopla a aplicação ao Spring (R2 resolve) |
| Observer / Domain Events / Event Bus | Notificações | A especificação arquitetural diz que o barramento de eventos **não faz parte do MVP**; há só 3 disparos |
| State GoF (uma classe por estado) | `StatusEpi`, `StatusOcorrencia` | O State via enum já resolve com 5 estados |
| Strategy | Multiplicadores do `NivelPerigo`, visibilidade por perfil, regras de alerta, configuração de segurança | 2 a 4 variantes fixas. Na segurança, ainda arrisca mudar a precedência das rotas |
| Template Method / Command | `registrarAcidente`/`registrarIncidente`, triagem/arquivamento | Só 2 variantes; não há fila nem desfazer |
| Builder em entidades | `Epi`, entidades JPA | Permite estado inválido; cada entidade JPA tem um único ponto de construção |
| Chain of Responsibility | Exceções, validação | O Spring já resolve por hierarquia de exceções |
| MapStruct / Abstract Factory | Mapeamentos, adaptadores | Dependência nova com ganho pequeno; não há famílias de objetos |
| Visitor | `Ocorrencia` | Só 2 subtipos; `sealed` + polimorfismo basta |
| Compound components, Redux, CRUD genérico | Frontend | API de componentes simples; estado local; as telas divergem demais |
| Specification nova na aplicação | Filtros de ocorrência | Já existe no adaptador (`OcorrenciaEspecificacoes`) |

## 8. Padrões já presentes (preservar)

- Ports & Adapters em toda a camada `port/`.
- Repository.
- Specification (`OcorrenciaEspecificacoes`).
- Static Factory: `Epi.novo`, `LogAuditoria.registrarCadastro`, `Emprestimo.registrarEntrega`.
- State via enum (`StatusOcorrencia`, `StatusPlanoDeAcao`).
- Value Object (`NumeroProtocoloCAT`, `EspecificacaoEPI`, `ProrrogacaoPrazo`).
- Domain Service (`SubstituicaoInteligenteService`).
- Strategy dos `RowMapper` no JDBC.
- Filtro JWT.
- No frontend:
  - Port/Adapter de cache (`CacheStoragePort`);
  - Context de sessão;
  - Facade de design system (`shared/components`).

## 9. Sugestão de ordem para as issues

As dependências entre os itens definem a ordem.

1. **R1** (operador). Destrava R2 e o construtor único.
2. **R2** (auditoria), incluindo tirar a auditoria do `PlanoDeAcaoJpaAdapter`.
3. **R3** (exceções). Independente; pode correr em paralelo com R1.
4. **R4 + R6** (domínio: `StatusEpi` e `sealed`).
5. **R5** (construção do `Epi` e da `Tarefa` + Test Data Builder), em conjunto com a limpeza dos legados após a decisão documental.
6. **R7** e as refatorações sem padrão da seção 5, priorizando o DTO que expõe a entidade e as consultas N+1.
7. **Frontend:** primeiro o `Epi` unificado e as primitivas de leitura; depois o hook de submit e a fábrica de erros; depois formatos; depois a decisão sobre React Query.
8. **Nomenclatura**, por último e isolada.

Precisam de decisão do grupo ou de atualização documental **antes** de virar issue:

- remover os métodos legados do `Epi`;
- tipar `PlanoDeAcao.status` como enum (o diagrama usa `String`);
- VO para CA e CPF;
- `LogAuditoria.tipoOperacao` como enum;
- qualquer mudança na hierarquia de usuários.

## 10. Como medir o resultado

Cada recomendação tem uma métrica no coletor (`scripts/metricas/coletar_metricas.py`):

| Recomendação | Métrica | Base v1.0.0 | Direção |
| --- | --- | ---: | --- |
| R1 | `violacoes_camadas.application_importa_spring_security` | 7 | → 0 |
| R1 | `contagens.security_context_holder` | 27 | ↓ |
| R2, R3 | `duplicacao.percentual` | 11,48% | ↓ |
| R3 | `contagens.exception_handlers` / `controller_advices` | 37 / 8 | ↓ |
| R5 | `classes_3_ou_mais_construtores` | 8 | ↓ |
| R5 | `construtores` | 218 | ↓ |
| R7, seção 5 | `complexidade_metodos.acima_de_10` | 12 | ↓ |
| R7, seção 5 | `complexidade_metodos.maxima` | 28 | ↓ |
| Seção 5 | `metodos_longos` | 16 | ↓ |
| Seção 5 | `classes_grandes` | 2 | ↓ |
| Todas | `cobertura_jacoco.percentual.branch` | 68,6% | não cair |
| Todas | `resultado_ultima_execucao.failures` | 0 | 0 |
| Nomenclatura | `nomenclatura_sufixos` | misto | um idioma por papel |

## 11. Limitações desta análise

- A análise foi estática: leitura do código e buscas. Nenhum teste foi executado para ela.
- As contagens de linhas economizadas são estimativas.
- As referências a arquivos e métodos foram conferidas por amostragem. Em itens de domínio, o relatório cita classe e método em vez de número de linha.
