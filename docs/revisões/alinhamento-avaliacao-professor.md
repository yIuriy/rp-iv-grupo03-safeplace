# Alinhamento das Issues (#220–#250) com a Avaliação do Professor

Este documento cruza as **31 issues abertas pós-v1.0.0 (`#220` a `#250`)** — planejadas a partir de [`analise-padroes-projeto-v1.0.0.md`](analise-padroes-projeto-v1.0.0.md) — com as anotações da avaliação presencial do professor, indicando **o que deve ser modificado em cada issue conflitante** e **quais pontos exigem novo escopo** para evitar retrabalho.

---

## 1. Verificação no Código dos Pontos Anotados pelo Professor

A inspeção direta nos arquivos e linhas citados durante a avaliação esclarece inclusive os itens marcados como *"a confirmar"* nas anotações:

| Arquivo e linha anotados | O que existe exatamente no código hoje (`v1.0.0`) | Diagnóstico e solução exigida pelo professor |
| :--- | :--- | :--- |
| `Emprestimo.java` (linha 70) | `public static Emprestimo registrarEntrega(...)` recebe **7 parâmetros soltos** e repassa para um construtor de **12 parâmetros** (linha 35), que executa 10 validações (`exigir(...)`). | **Excesso de parâmetros (*Long Parameter List*) + método estático + validação no construtor.** Encapsular os dados de entrada em um objeto/Builder de produção e extrair a validação para a camada de validação. |
| `SubstituicaoInteligenteService.java` (linha 53) | `public ProjecaoSubstituicao calcularProjecao(Epi epi, NivelPerigo periculosidadeSetor, List<Emprestimo> emprestimos, LocalDate dataReferencia, Integer diasAmostragem)` recebe **5 parâmetros** e executa **4 blocos sequenciais de validação** (linhas 64–101: vida útil, periculosidade, validade do CA e histórico) antes do cálculo. | **Validações encadeadas dentro do método de cálculo + parâmetros soltos.** Encapsular o contexto da projeção em um objeto e aplicar **Chain of Responsibility** para a cadeia de validações pré-cálculo. |
| `Inspecao.java` (linhas 21 a 25) | Linha 22 declara `private final Integer areaRiscoId;`, enquanto a linha 24 declara `private final GestorDeSeguranca responsavel;`. O próprio Javadoc (linha 15) diz: *"A área é guardada pelo id"*. | **Violação de POO.** Confirmado: o problema é guardar o `Integer areaRiscoId` primitivo/wrapper em vez da associação orientada a objetos `AreaRisco areaRisco`. |
| `Acidente.java` (linhas 64 a 92) | Método fábrica estático `public static Acidente novo(...)` com **10 parâmetros soltos** (linhas 64–92) e `public static NivelPerigo lerGravidade(String)` (linha 99). | **Abuso de métodos `static` de criação.** O professor questionou por que a criação é estática em vez de usar polimorfismo/interface via **Abstract Factory** e **Builder**. |
| `AlocarColaboradorTarefaUseCaseImpl.java` / `TarefaUseCase.java` (linha 87) | Em `AlocarColaboradorTarefaUseCaseImpl` (linhas 84–87), `validadorAptidaoService.validarAptidaoParaTarefa(...)` roda na linha 84 e lança exceção **antes** de `alocacaoTarefaRepository.salvar(alocacao)` na linha 87. Já em `TarefaUseCase` (linha 87), `Tarefa.cadastrarComRelacoes(...)` valida dentro do construtor de domínio. | **Fluxo de validação pré-persistência.** Confirmado: toda validação deve ficar em uma **camada de validação própria** (no mesmo nível de `domain`) e ser chamada no caso de uso **antes** de acionar o `repository`, lançando exceção ali se houver erro. |
| `CadastrarCapacitacaoService.java` (linha 40) | `Certificacao nova = Certificacao.cadastrar(entrada.nome(), entrada.dataVencimento(), entrada.dataEmissao(), entrada.colaboradorId());` | **Método estático na entidade + desempacotamento do `InputDTO`.** Em vez de abrir os 4 campos de `entrada` e chamar um método `static` que valida no domínio, validar `entrada` na camada de validação e construir a entidade via fábrica de instância/conversor passando o objeto `entrada`. |

---

## 2. Issues em Conflito e O Que Modificar em Cada Uma

Das 31 issues abertas (`#220`–`#250`), **8 issues** entram em conflito com as diretrizes do professor e devem ter seu escopo ajustado antes da implementação:

### 2.1. Issue `#228` — `[Padrões][Notification] Unificar a validação de aptidão do colaborador`
- **Como está hoje:** Aplica apenas o padrão *Notification* (Fowler) dentro de `domain/capacitacao/ValidadorAptidaoColaboradorService.java`, mantendo as demais validações nos construtores do domínio.
- **Conflito com o professor:**
  1. O professor exigiu obrigatoriamente o padrão GoF **Chain of Responsibility**.
  2. O professor exigiu criar uma **camada de validação própria** (fora dos construtores de `domain`), com uma validação por classe, chamada antes do `repository` (citando exatamente a linha 87 de `AlocarColaboradorTarefaUseCaseImpl`).
- **O que modificar na `#228`:**
  - **Mudar o padrão principal para `Chain of Responsibility` (combinado com o acumulador de erros):**
    - Criar o pacote de camada `br.edu.safeplace.backend.validation` (no mesmo nível de `domain`, `application` e `adapters`).
    - Definir a interface da cadeia (ex.: `RegraValidacao<T>` / `ValidadorHandler<T>`) onde cada elo valida uma condição e delega ao próximo.
    - Aplicar a cadeia tanto na validação de alocação (`AlocarColaboradorTarefaUseCaseImpl` linha 84, antes de `alocacaoTarefaRepository.salvar` na linha 87) quanto nas 4 validações sequenciais de `SubstituicaoInteligenteService.calcularProjecao` (linha 53) e nos validadores de entidades antes do `repository.save`.

---

### 2.2. Issues `#225` e `#239` — `[Padrões][Factory Method 1/2 e 2/2]` (`Epi` e `Ocorrencia`/`Acidente`)
- **Como estão hoje:**
  - `#225`: Consolida a criação de `Epi` em métodos **estáticos** `Epi.novo(...)` e `Epi.reconstituir(...)`, mantendo validações no construtor de `Epi`.
  - `#239`: Cria fábrica para `Acidente`/`Incidente` e torna `Ocorrencia` `sealed`.
- **Conflito com o professor:**
  1. O professor exigiu obrigatoriamente **Abstract Factory** e programar voltado a **interfaces**.
  2. O professor criticou diretamente os métodos `static` de criação em `Acidente.java` (linhas 64–92: `Acidente.novo`) e `CadastrarCapacitacaoService.java` (linha 40: `Certificacao.cadastrar`), além de pedir para passar o objeto inteiro (`InputDTO`) em vez de parâmetros soltos.
  3. O professor pediu para retirar a validação de dentro da criação do objeto em `domain`.
- **O que modificar na `#225` e na `#239`:**
  - **Converter de *Static Factory Method* para `Abstract Factory` orientada a interface:**
    - Na **`#239`**: Definir uma interface `OcorrenciaAbstractFactory` (com implementações concretas `AcidenteFactory` e `IncidenteFactory` injetadas via interface) que receba o objeto de entrada (`RegistrarAcidenteInputDTO` / `RegistrarIncidenteInputDTO`) sem desempacotar dezenas de parâmetros soltos e crie a família de objetos do relato (a entidade `Acidente`/`Incidente`, a `NotificacaoOcorrencia` correspondente e o descritor de auditoria). Remover o método estático `Acidente.novo` (linhas 64–92).
    - Na **`#225`** (e em `CadastrarCapacitacaoService` linha 40): Evitar novas fábricas `static` com listas longas de argumentos; extrair as validações de criação de `Epi`, `Tarefa`, `Certificacao` e `Treinamento` para a camada `validation` (executada antes do `repository`) e construir as entidades a partir do objeto encapsulado (`EspecificacaoEPI` / `InputDTO` / `Builder`).

---

### 2.3. Issues `#226` e `#240` — `[Padrões][Builder 1/2 e 2/2]` (`Epi`, `Tarefa` e `Ocorrencia`)
- **Como estão hoje:** Criam *Test Data Builders* (`EpiBuilder`, `TarefaBuilder`, `AcidenteBuilder`, `IncidenteBuilder`) restritos exclusivamente à pasta de testes (`src/test`).
- **Conflito com o professor:**
  - O professor pediu para avaliar o padrão **Builder no código de produção** para separar a construção de objetos complexos e resolver o excesso de parâmetros apontado em `Emprestimo.java` (linha 70, que recebe 7 parâmetros em `registrarEntrega` e 12 no construtor), além de `Epi`, `Acidente` e `Tarefa`.
- **O que modificar na `#226` e na `#240`:**
  - **Promover os Builders para `src/main` (produção) e incluir `Emprestimo` (`Emprestimo.java` linha 70):**
    - Em vez de criar builders apenas em `src/test`, implementar o padrão **Builder** (GoF criacional) no código de produção para `Epi`, `Emprestimo`, `Tarefa`, `Acidente` e `Incidente` (ou combinar Builder + objeto de parâmetros em `Emprestimo.registrarEntrega`), reutilizando esses mesmos Builders nos testes.

---

### 2.4. Issues `#229` e `#242` — `[Padrões][Data Mapper 1/2 e 2/2]` (`AreaRiscoEntityMapper` e `PlanoDeAcaoEntityMapper`)
- **Como estão hoje:** Criam mapeadores manuais (`AreaRiscoEntityMapper` e `PlanoDeAcaoEntityMapper`) para converter entre as classes de `domain` e as classes `*Entity` de `adapters/out/persistencia`.
- **Conflito com o professor:**
  - O professor anotou:
    1. *"Estudar JPA, adotar e deixar de escrever SQL à mão"* (visível em `CapacitacaoJdbcAdapter.java` e `ImpedimentoCapacitacaoJdbcAdapter.java`, que usam `JdbcTemplate` com `INSERT`/`SELECT`/`UPDATE` manuais).
    2. *"As entidades devem ficar em domínio (entity, o 'model' do MVC)"* e *"Substituir os arquivos que estão dentro de `persistencia`"*.
- **O que modificar na `#229` e na `#242`:**
  1. **Obrigatório e imediato:** Incluir no escopo de persistência a **substituição de `CapacitacaoJdbcAdapter` e `ImpedimentoCapacitacaoJdbcAdapter` por Spring Data JPA** (`CertificacaoJpaRepository`, `TreinamentoJpaRepository`, `ExigenciaCapacitacaoJpaRepository`), eliminando 100% do SQL escrito à mão no projeto.
  2. **Decisão arquitetural para `#229` e `#242` (alinhar com o professor):**
     - **Alternativa A (Atendimento literal ao modelo MVC/JPA do professor):** Mover as anotações `@Entity`/`@Table` para as classes do pacote `domain`, remover as classes `*Entity` espelhadas de `adapters/out/persistencia` e **cancelar os Data Mappers das issues `#229` e `#242`**.
     - **Alternativa B (Manter separação Hexagonal se aceita pelo professor):** Caso o professor aceite manter `@Entity` isolado em `persistencia` após ver que o SQL manual de `CapacitacaoJdbcAdapter` foi convertido para JPA, manter `#229` e `#242`. **Não executar `#229` nem `#242` antes de confirmar entre A e B.**

---

### 2.5. Issue `#230` — `[Padrões][Value Object] Guardar o protocolo CAT como NumeroProtocoloCAT dentro de Acidente`
- **Como está hoje:** Mantém a validação de formato do protocolo CAT dentro do construtor de `Acidente` / `NumeroProtocoloCAT.de(...)` no pacote `domain`.
- **Conflito com o professor:**
  - Se a validação sair da criação de objetos em `domain` para a camada `validation`, o construtor de `Acidente` não deve lançar exceção de validação diretamente.
- **O que modificar na `#230`:**
  - Manter a tipagem forte `NumeroProtocoloCAT` no atributo de `Acidente`, mas acionar a validação do formato do protocolo CAT na **camada de validação** antes da chamada ao `repository`.

---

## 3. Itens Exigidos pelo Professor que Precisam de Nova Issue (Fora das #220–#250)

Estes pontos foram anotados diretamente pelo professor na avaliação e **não estão cobertos por nenhuma issue atual**:

| Novo Escopo / Issue Sugerida | Arquivos Alvo | O que fazer |
| :--- | :--- | :--- |
| **1. Corrigir violação de POO nas associações por ID** | `Inspecao.java` (linhas 21–25) | Substituir `private final Integer areaRiscoId;` por `private final AreaRisco areaRisco;` (associação de objeto, igual a `GestorDeSeguranca responsavel` na linha 24), ajustando `InspecaoUseCase` e `InspecaoJpaAdapter`. Verificar se o professor exige o mesmo para `AreaRisco.setorId` e `Emprestimo.epiId`/`colaboradorId`. |
| **2. Migrar persistência JDBC (SQL manual) para JPA** | `CapacitacaoJdbcAdapter.java`, `ImpedimentoCapacitacaoJdbcAdapter.java` | Eliminar `JdbcTemplate` e todas as strings SQL manuais (`INSERT INTO certificacoes...`, `SELECT ...`), substituindo por repositórios Spring Data JPA (`JpaRepository`). |
| **3. Padronizar nomes de portas e operações** | `application/port/out/*RepositoryPort.java`, `application/usecase/*` | Remover o sufixo `RepositoryPort` / `RepositorioPorta` (padronizar como `Repository` conforme orientação do professor) e padronizar nomes curtos e consistentes nos métodos de repositório e serviço (`save`, `list`, `findById`). |

---

## 4. Resumo: Ordem de Execução Sem Retrabalho

1. **Podem ser executadas imediatamente (sem conflito com o professor):**
   - **Semana 1:** `#220` (decisão documental e métodos mortos de `Epi`), `#222` (porta `OperadorAtual`), `#232` (Strategy HTTP frontend — já concluída), `#244` (primitivas de leitura da API no frontend).
   - **Semanas 2 a 5:** `#236` (`OperadorAtual` nos casos de uso), `#223`/`#237`/`#238` (Facade `RegistradorAuditoria`), `#224` (State em `StatusEpi`), `#227`/`#241` (Null Object), `#231`/`#233`/`#243`/`#246`/`#250` (Frontend), `#234`/`#249` (Exception Handler global), `#245` (DTO Assembler), `#247` (Strategy `Clock`), `#248` (Testcontainers) e `#235` (relatório final de métricas).
2. **Devem ser ajustadas antes de codificar (conforme Seção 2 e Seção 3):**
   - `#228` $\rightarrow$ implementar já com **Chain of Responsibility** + **camada de validação** antes do `repository` (incluindo `AlocarColaboradorTarefaUseCaseImpl` l. 87 e `SubstituicaoInteligenteService` l. 53).
   - `#225` e `#239` $\rightarrow$ implementar com **Abstract Factory** / fábricas orientadas a interface recebendo o objeto inteiro (corrigindo `Acidente.java` l. 64–92 e `CadastrarCapacitacaoService.java` l. 40) e sem validar dentro do construtor de domínio.
   - `#226` e `#240` $\rightarrow$ aplicar **Builder em produção** (`src/main`), incluindo `Emprestimo.java` (l. 70).
   - `#229` e `#242` $\rightarrow$ substituir SQL manual de Capacitação por JPA e alinhar se `@Entity` ficará no próprio `domain`.
