# Correspondência entre Diagrama de Classes e Módulo de EPIs (Estoque, Manutenção e Métodos Legados)

## 1. Visão Geral e Contexto

Este documento formaliza as decisões arquiteturais e a matriz de correspondência entre as classes `EPI`, `EspecificacaoEPI`, `LoteEPI`, `ModeloEPI`, `MovimentacaoEstoque` e `ManutencaoEPI` do **Diagrama de Classes** e a implementação em Arquitetura Hexagonal do SafePlace (RF03, RF04, RF11, RF14, RF21; UC01, UC06, UC10, UC12; US03, US04, US11, US21; decisão #124).

Em especial, registra:

1. A decisão oficial sobre os **8 métodos legados de `Epi`** que não possuem uso em produção ([#220](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/220)), destravando as issues [#224](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/224) (State em `StatusEpi`) e [#225](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/225) (Factory Method do `Epi`).
2. A **política única de erro para a baixa de estoque**.
3. As **alterações previstas para o Diagrama de Classes (Astah / PNG)** e diagramas de sequência relacionados (DS01, DS06, DS10).

- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [Análise de padrões de projeto (v1.0.0)](../revisões/analise-padroes-projeto-v1.0.0.md), [Padrões de projeto](padroes-de-projeto.md), [Correspondência DS10](correspondencia-classes-ds10.md) e [Especificação do MVP](../mvp/especificacao-mvp-arquitetura.md#112-correspondência-com-o-diagrama-de-classes).
- **Implementação de referência**: domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/epi/`; aplicação em `application/usecase/EpiUseCase.java`, `ControlarManutencaoEpiService.java`, `EmprestimoUseCase.java` e `PlanejarSubstituicaoUseCaseImpl.java`; portas em `application/port/in/GerenciarEpiUseCase.java`, `GerenciarEstoqueUseCase.java` e `application/port/out/EpiRepositoryPort.java`.

---

## 2. Decisão sobre os 8 Métodos Legados de `EPI`

Na versão `v1.0.0`, a classe de domínio `Epi` continha 8 métodos herdados da modelagem inicial e dos diagramas de sequência (DS01, DS06 e DS10) com **zero chamadas em código de produção**, mantidos apenas por testes unitários em `EpiTest`:

- `buscarEPIs()` e `buscarEPI(int codigoEPI)`;
- `compararComQuantidadeMinima()` e `compararComQuantMinima()`;
- `buscarEPIsAbaixoDaQuantidadeMinima()`;
- `validarBaixa(int codigo, int quantidade)` e `darBaixa(int quantidade)`;
- `buscarLote(int codigo)` (junto à lista auxiliar `lotesAdicionais`, `adicionarLote` e `getLotes`).

### 2.1. Problemas identificados na análise (`v1.0.0`)

1. **Conflito de responsabilidade na Arquitetura Hexagonal**: operações de busca em coleção (`buscarEPIs`, `buscarEPI`, `buscarEPIsAbaixoDaQuantidadeMinima`) pertencem à porta de repositório (`EpiRepositoryPort`) e aos casos de uso, não a uma instância individual da entidade `Epi` (onde retornavam `List.of(this)`).
2. **Divergência na política de erro de baixa**: `darBaixa(int)` retornava `false` quando `quantidade <= 0` ou `quantidade > saldo`, enquanto a operação real de produção `removerEstoque(int, String, Integer)` lança `IllegalArgumentException` e `SaldoInsuficienteException`. Além disso, `darBaixa` gravava motivo fixo (`"Baixa de estoque"`) sem identificar o responsável exigido por US03 e RNF05.
3. **Duplicação de validação**: `validarBaixa(int, int)` duplicava as checagens de quantidade positiva e saldo suficiente que já são invariantes atômicas de `removerEstoque`.
4. **Conflito com a decisão #124 sobre lotes**: pela decisão #124, cada registro de `EPI` no MVP representa o saldo vinculado a um único `LoteEPI` (`epi.getLote()`) e a um único `ModeloEPI` (`epi.getModelo()`). A lista `lotesAdicionais` e o método `buscarLote(int)` não possuem persistência nem uso real.

### 2.2. Matriz de Decisão e Mapeamento (Código vs. Diagramas)

A decisão aprovada é **mapear** os 8 métodos na matriz de correspondência abaixo para seus equivalentes reais na Arquitetura Hexagonal e **removê-los de `Epi.java` na issue `#225`** (junto com `lotesAdicionais`, `adicionarLote` e `getLotes`), registrando o que deve ser atualizado no Astah/PNG:

| Método Legado (`Epi.java` / Diagrama) | Decisão | Destino na Arquitetura Hexagonal | O que deve mudar nos Diagramas (Astah / PNG) |
| :--- | :--- | :--- | :--- |
| `buscarEPIs() : List<EPI>` | **Mapear e remover de `Epi.java` (#225)** | Porta de Saída `EpiRepositoryPort.listarTodos()`<br>Porta de Entrada `GerenciarEpiUseCase.listar()` | **Diagrama de Classes:** retirar `buscarEPIs()` da classe `EPI` (listagem geral pertence ao repositório).<br>**DS01 / DS06 / DS10:** a mensagem de listagem corresponde a `EpiRepositoryPort.listarTodos()`. |
| `buscarEPI(codigoEPI : int) : EPI` | **Mapear e remover de `Epi.java` (#225)** | Porta de Saída `EpiRepositoryPort.buscarPorId(id)`<br>Porta de Entrada `GerenciarEpiUseCase.buscarPorId(id)` | **Diagrama de Classes:** retirar `buscarEPI(codigoEPI : int)` da classe `EPI` (busca por identificador pertence ao repositório). |
| `buscarEPIsAbaixoDaQuantidadeMinima() : List<EPI>` | **Mapear e remover de `Epi.java` (#225)** | Filtro sobre `EpiRepositoryPort.listarTodos()` avaliando `Epi.isEstoqueCritico()` (usado em `EpiUseCase` e `PlanejarSubstituicaoUseCaseImpl`) | **Diagrama de Classes:** retirar `buscarEPIsAbaixoDaQuantidadeMinima()` da classe `EPI` (filtragem de coleção pertence ao caso de uso/repositório sobre `isEstoqueCritico()`). |
| `buscarLote(codigo : int) : LoteEPI` | **Mapear e remover de `Epi.java` (#225)** | Domínio `Epi.getLote()` (decisão #124: vínculo `1` para `1` do registro de `EPI` com seu `LoteEPI`) | **Diagrama de Classes:** retirar `buscarLote(codigo : int)` da classe `EPI`; a navegação já está representada pela associação entre `EPI` e `LoteEPI`. |
| `compararComQuantMinima() : boolean` | **Mapear e remover de `Epi.java` (#225)** | Domínio `Epi.isEstoqueCritico()` (`quantidade <= especificacao.getQuantidadeMinima()`) | **Diagrama de Classes:** substituir `compararComQuantMinima() : boolean` por `isEstoqueCritico() : boolean` na classe `EPI`. |
| `compararComQuantidadeMinima() : boolean` | **Mapear e remover de `Epi.java` (#225)** | Domínio `Epi.isEstoqueCritico()` | **Diagrama de Classes:** nenhuma alteração necessária no PNG (este alias já não constava no diagrama vigente). |
| `validarBaixa(codigo : int, quantidade : int) : int` | **Mapear e remover de `Epi.java` (#225)** | Invariante interna de `Epi.removerEstoque(qtd, motivo, responsavelId)` | **Diagrama de Classes e DS06:** retirar `validarBaixa(codigo, quantidade)` da classe `EPI`; a validação de saldo e quantidade ocorre atomicamente dentro de `removerEstoque`. |
| `darBaixa(quantidade : int) : boolean` | **Mapear e remover de `Epi.java` (#225)** | Domínio `Epi.removerEstoque(qtd, motivo, responsavelId)`<br>Porta de Entrada `GerenciarEstoqueUseCase.darBaixaEstoque(epiId, qtd, motivo)` | **Diagrama de Classes e DS06:** retirar `darBaixa(quantidade : int) : boolean` da classe `EPI`, mantendo exclusivamente `removerEstoque(quantidade : int, motivo : String) : MovimentacaoEstoque`. |

---

## 3. Política Única de Erro para Baixa de Estoque

Fica estabelecida uma **política única de validação e falha** para qualquer saída ou baixa de estoque no sistema (seja por movimentação manual em UC06 ou por entrega de empréstimo em UC12):

1. **Ponto único no domínio**: toda redução de saldo de um `Epi` passa obrigatoriamente por `Epi.removerEstoque(int qtd, String motivo, Integer responsavelId)` (ou sua sobrecarga `removerEstoque(int qtd, String motivo)`).
2. **Proibição de retorno booleano silencioso**: falhas de validação de entrada ou de saldo insuficiente **nunca** retornam `false` nem `null`; sempre lançam exceção de domínio descritiva, interrompendo a transação antes de alterar o estado do `Epi` ou gravar `MovimentacaoEstoque` / `LogAuditoria`.
3. **Tabela de Exceções e Contratos HTTP**:

| Condição de Erro na Baixa | Exceção Lançada no Domínio / Aplicação | Status HTTP | Tratamento e Invariante |
| :--- | :--- | :---: | :--- |
| EPI não encontrado pelo `id` | `EpiNaoEncontradoException` | `404 Not Found` | Lançada pelo caso de uso antes de invocar o domínio. |
| `quantidade <= 0` | `IllegalArgumentException("Quantidade a retirar deve ser maior que zero.")` | `400 Bad Request` | Preserva `quantidade` e `status` originais do `Epi`. |
| `motivo == null \|\| motivo.isBlank()` | `IllegalArgumentException("Motivo da movimentação é obrigatório.")` | `400 Bad Request` | Validado na criação de `MovimentacaoEstoque` antes de mutar `this.quantidade` e `this.status`. |
| `quantidade > this.quantidade` (saldo insuficiente) | `SaldoInsuficienteException` | `400 Bad Request` | Informa saldo atual e quantidade solicitada; preserva `quantidade` e `status` originais. |

---

## 4. Matriz Completa de Correspondência da Classe `EPI`

| Operação no Diagrama de Classes (`EPI`) | Situação | Componente na Arquitetura Hexagonal | Observação / Mudança Prevista no Astah/PNG |
| :--- | :--- | :--- | :--- |
| `adicionarEstoque(quantidade : int, motivo : String) : MovimentacaoEstoque` | Mantida (MVP) | Domínio `Epi.adicionarEstoque(qtd, motivo, responsavelId)`<br>Porta `GerenciarEpiUseCase.registrarMovimentacao` / `GerenciarEstoqueUseCase.registrarEntrada` | Permanece no diagrama de classes. Se `status == ESGOTADO`, transita para `DISPONIVEL` (#224). |
| `removerEstoque(quantidade : int, motivo : String) : MovimentacaoEstoque` | Mantida (MVP) | Domínio `Epi.removerEstoque(qtd, motivo, responsavelId)`<br>Porta `GerenciarEpiUseCase.registrarMovimentacao` / `GerenciarEstoqueUseCase.darBaixaEstoque` | Permanece no diagrama de classes como operação única de baixa. Se `novoSaldo == 0` e `status == DISPONIVEL`, transita para `ESGOTADO` (#224). |
| `enviarParaManutencao() : void` | Mantida (MVP) | Domínio `Epi.enviarParaManutencao(dataReferencia)`<br>Caso de uso `ControlarManutencaoEpiService.enviarParaManutencao` | Permanece no diagrama. Exige CA válido e transição válida para `EM_MANUTENCAO` (#224). |
| `concluirManutencao(manutencao : ManutencaoEPI) : void` | Mantida (MVP) | Domínio `Epi.concluirManutencao(manutencao)`<br>Caso de uso `ControlarManutencaoEpiService.concluirManutencao` | Permanece no diagrama. Registra no histórico e, se `APROVADO`, retorna para `DISPONIVEL` (#224). |
| `obterHistoricoManutencao() : List<ManutencaoEPI>` | Mantida (MVP) | Domínio `Epi.obterHistoricoManutencao()`<br>Caso de uso `ControlarManutencaoEpiService.listarHistoricoPorEpi` | Permanece no diagrama. |
| `validarCaValido() : void` | Mantida (MVP) | Domínio `Epi.validarCaValido(dataReferencia)` delegando a `ModeloEPI.verificarCA(data)` | Permanece no diagrama. Lança `CertificadoAprovacaoVencidoException`. |
| `validarVinculoEPIsObrigatorios() : boolean` | Mantida (MVP) | Domínio `Epi.validarVinculoEPIsObrigatorios()` | Permanece no diagrama (verifica `status != DESCARTADO` e CA válido). |
| `atualizarQuantidade(tipo : EspecificacaoEPI, quantidade : int) : void` | Mantida | Domínio `Epi.atualizarQuantidade(especificacao, quantidade)` | Permanece no diagrama; transições de status passam por `StatusEpi` na issue `#224`. |
| `compararComQuantMinima() : boolean` | Mapeada (#225) | Domínio `Epi.isEstoqueCritico()` | Atualizar nome no Astah/PNG para `isEstoqueCritico() : boolean`. |
| `buscarEPIs()`, `buscarEPI(int)`, `buscarEPIsAbaixoDaQuantidadeMinima()`, `buscarLote(int)` | Mapeadas (#225) | `EpiRepositoryPort` / `GerenciarEpiUseCase` / `Epi.getLote()` | Retirar da classe `EPI` no Astah/PNG na revisão de diagramas. |
| `validarBaixa(int, int)`, `darBaixa(int)` | Mapeadas (#225) | `Epi.removerEstoque(...)` / `GerenciarEstoqueUseCase.darBaixaEstoque(...)` | Retirar da classe `EPI` no Astah/PNG e do DS06 na revisão de diagramas. |

---

## 5. Rastreabilidade com as Issues de Padrões de Projeto

- **Issue [#224](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/224) (State via Enum em `StatusEpi`)**: centraliza as regras de transição de estado de `Epi` (`DISPONIVEL`, `EM_USO`, `EM_MANUTENCAO`, `ESGOTADO`, `DESCARTADO`) em `StatusEpi.podeTransicionarPara(novoStatus)` e `Epi.transicionarPara(novoStatus)`, sem depender dos métodos legados `darBaixa` ou `validarBaixa`.
- **Issue [#225](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/225) (Static Factory Method do `Epi` + Test Data Builder)**: consolida a criação de `Epi` em `Epi.novo(...)` e `Epi.reconstituir(...)` usando `EspecificacaoEPI` como *Parameter Object*, introduz builder de teste e **remove de `Epi.java` e `EpiTest.java` os 8 métodos legados** conforme a decisão registrada na seção 2.2 deste documento.
