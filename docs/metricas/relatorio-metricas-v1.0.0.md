# Métricas de Código — Linha de Base v1.0.0

Retrato do código na release [v1.0.0](https://github.com/yIuriy/rp-iv-grupo03-safeplace/releases/tag/v1.0.0) (commit `2d48648`, branch `dev`), coletado em 21/10/2026, **antes da aplicação de padrões de projeto**. Serve de referência para medir o efeito da refatoração.

Dados brutos: [`metricas-v1.0.0.json`](metricas-v1.0.0.json). Coletor: [`scripts/metricas/coletar_metricas.py`](../../scripts/metricas/coletar_metricas.py).

## 1. Como reproduzir e comparar

Depois da refatoração, no commit a avaliar:

```bash
cd backend && ./mvnw test && cd ..
python scripts/metricas/coletar_metricas.py --frontend-ferramentas --saida docs/metricas/metricas-<versao>.json
python scripts/metricas/coletar_metricas.py --comparar docs/metricas/metricas-v1.0.0.json docs/metricas/metricas-<versao>.json
```

O modo `--comparar` gera uma tabela Markdown com as métricas que mudaram e a variação de cada uma.

Para a comparação ser justa:

- meça só arquivos versionados (o script já usa `git ls-files`);
- use os mesmos limites (seção 7);
- rode os testes no mesmo ambiente. Nesta linha de base, os testes de integração com PostgreSQL foram **ignorados**, porque não havia banco acessível, e a cobertura não os inclui.

## 2. Resumo

| Indicador | Backend (main) | Backend (testes) | Frontend |
| --- | ---: | ---: | ---: |
| Arquivos | 357 | 126 | 119 (TS/TSX) |
| Linhas de código | 15.086 | 18.790 | 9.140 (+731 CSS) |
| Linhas de comentário | 1.478 | 448 | 506 |
| Casos de teste | — | 891 `@Test` + 30 parametrizados | 150 (Playwright) |
| Migrações Flyway | 33 | — | — |
| Endpoints REST | 75 | — | — |

## 3. Backend: estrutura

### 3.1. Tipos e métodos

| Métrica | Valor |
| --- | ---: |
| Classes | 159 |
| Classes abstratas | 2 |
| Interfaces | 62 |
| Records | 123 |
| Enums | 20 |
| Métodos (sem construtores) | 1.189 |
| Construtores | 218 |

### 3.2. Distribuição por camada

| Camada | Arquivos | Linhas de código | % do código |
| --- | ---: | ---: | ---: |
| `domain` | 86 | 3.628 | 24,0% |
| `application` | 126 | 4.847 | 32,1% |
| `adapters` | 140 | 6.414 | 42,5% |
| `config` | 4 | 188 | 1,2% |
| raiz (`BackendApplication`) | 1 | 9 | 0,1% |

### 3.3. Maiores arquivos (linhas de código)

| Arquivo | Linhas | Métodos |
| --- | ---: | ---: |
| `domain/epi/Epi.java` | 416 | 45 |
| `application/usecase/OcorrenciaUseCase.java` | 368 | 22 |
| `domain/ocorrencia/PlanoDeAcao.java` | 289 | 39 |
| `adapters/out/persistencia/PlanoDeAcaoJpaAdapter.java` | 278 | 17 |
| `application/usecase/UsuarioCasoDeUso.java` | 276 | 26 |
| `application/usecase/EmprestimoUseCase.java` | 268 | 15 |
| `domain/area_risco/AreaRisco.java` | 246 | 30 |
| `application/usecase/TarefaUseCase.java` | 245 | 21 |
| `application/usecase/PlanoDeAcaoUseCase.java` | 223 | 13 |
| `adapters/out/persistencia/OcorrenciaJpaAdapter.java` | 219 | 18 |

## 4. Backend: complexidade e tamanho

### 4.1. Complexidade ciclomática

| Métrica | Valor |
| --- | ---: |
| Complexidade total dos métodos (estimativa no código-fonte) | 2.125 |
| Complexidade média por método | 1,79 |
| Complexidade máxima | 28 |
| Métodos com complexidade > 5 | 53 |
| Métodos com complexidade > 10 | 12 |
| Complexidade ciclomática total medida pelo JaCoCo (bytecode) | 2.668 |

### 4.2. Tamanho e parâmetros

| Métrica | Valor |
| --- | ---: |
| Métodos com mais de 30 linhas | 16 |
| Métodos ou construtores com mais de 5 parâmetros | 158 |
| Arquivos com mais de 300 linhas de código | 2 |
| Classes com 3 ou mais construtores | 8 |

### 4.3. Métodos mais complexos

| Método | Complexidade | Linhas |
| --- | ---: | ---: |
| `FiltroOcorrenciasDTO` (construtor compacto) | 28 | 33 |
| `SubstituicaoInteligenteService.calcularProjecao` | 21 | 74 |
| `FiltroOcorrenciasDTO.aceita` | 19 | 16 |
| `OcorrenciaUseCase.descreverAlteracoes` | 17 | 15 |
| `OcorrenciaOutputDTO` (construtor) | 15 | 64 |
| `OcorrenciaOutputDTO.deDominio` | 15 | 63 |
| `ValidadorAptidaoColaboradorService.verificarPendenciasNormativas` | 13 | 23 |
| `NotificacaoAcao` (construtor) | 13 | 30 |
| `AutenticacaoCasoDeUso.autenticar` | 12 | 23 |
| `ConsultarCapacitacaoService.listarColaboradoresPorStatusCertificacao` | 11 | 20 |

### 4.4. Métodos mais longos

| Método | Linhas | Complexidade |
| --- | ---: | ---: |
| `SecurityConfig.securityFilterChain` | 74 | 3 |
| `SubstituicaoInteligenteService.calcularProjecao` | 74 | 21 |
| `OcorrenciaOutputDTO` (construtor) | 64 | 15 |
| `OcorrenciaOutputDTO.deDominio` | 63 | 15 |
| `OcorrenciaUseCase.atualizar` | 55 | 10 |
| `EmprestimoUseCase.registrarDevolucao` | 54 | 11 |
| `PlanoDeAcaoJpaAdapter.salvar` | 52 | 10 |
| `PlanejarSubstituicaoUseCaseImpl.gerarAnalise` | 41 | 10 |

### 4.5. Classes com mais construtores

| Classe | Construtores |
| --- | ---: |
| `Epi` | 5 |
| `Tarefa` | 4 |
| `TarefaEntity` | 4 |
| `AreaRiscoEntity` | 3 |
| `MovimentacaoEstoqueEntity` | 3 |
| `PlanoDeAcaoEntity` | 3 |
| `TarefaOutputDTO` | 3 |
| `TarefaUseCase` | 3 |

### 4.6. Construções condicionais e outras contagens

| Construção | Ocorrências |
| --- | ---: |
| `if (` | 487 |
| Comparações com `null` | 578 |
| `instanceof` | 26 |
| `switch` | 5 |
| `throw new` | 214 |
| `SecurityContextHolder` | 27 |
| `Objects.requireNonNull` | 14 |
| `@Transactional` | 94 |
| `@Service` | 19 |
| `@RestControllerAdvice` | 8 |
| `@ExceptionHandler` | 37 |

## 5. Backend: acoplamento e arquitetura

### 5.1. Regras da arquitetura hexagonal

| Regra | Arquivos em violação |
| --- | ---: |
| `domain` importa framework (Spring, JPA, Validation, Hibernate) | 0 |
| `domain` importa `application` ou `adapters` | 0 |
| `application` importa `adapters` | 0 |
| `adapters/in` importa `adapters/out` | 0 |
| `application` importa Spring Security | **7** |

Os 7 arquivos são `CadastrarCapacitacaoService`, `ControlarManutencaoEpiService`, `EpiUseCase`, `InspecaoUseCase`, `OcorrenciaUseCase`, `PlanoDeAcaoUseCase` e `TarefaUseCase`. Todos leem o usuário autenticado direto do `SecurityContextHolder`, em vez de recebê-lo por uma porta.

### 5.2. Acoplamento por módulo

Ce é o número de módulos internos que o módulo importa. Ca é o número de módulos que importam o módulo. I = Ce / (Ca + Ce).

| Módulo | Ce | Ca | I | Linhas de código | Cobertura de linhas |
| --- | ---: | ---: | ---: | ---: | ---: |
| `domain/comum` | 0 | 11 | 0,00 | 102 | 100,0% |
| `domain/usuario` | 0 | 10 | 0,00 | 213 | 96,5% |
| `domain/emprestimo` | 0 | 7 | 0,00 | 173 | 100,0% |
| `domain/inspecao` | 1 | 5 | 0,17 | 99 | 100,0% |
| `domain/epi` | 2 | 9 | 0,18 | 1.191 | 89,3% |
| `domain/area_risco` | 2 | 7 | 0,22 | 345 | 80,3% |
| `domain/capacitacao` | 2 | 7 | 0,22 | 393 | 92,8% |
| `domain/ocorrencia` | 3 | 8 | 0,27 | 901 | 97,8% |
| `domain/tarefa` | 3 | 6 | 0,33 | 211 | 92,9% |
| `application/dto/capacitacao` | 1 | 3 | 0,25 | 265 | 100,0% |
| `application/dto/input` | 7 | 5 | 0,58 | 412 | 100,0% |
| `application/port/in` | 7 | 4 | 0,64 | 297 | 50,0% |
| `application/port/out` | 10 | 5 | 0,67 | 246 | 60,0% |
| `application/dto/output` | 8 | 3 | 0,73 | 778 | 96,2% |
| `application/usecase` | 14 | 0 | 1,00 | 2.849 | 90,5% |
| `adapters/in/web` | 13 | 0 | 1,00 | 2.898 | 89,1% |
| `adapters/in/agendamento` | 1 | 0 | 1,00 | 22 | 100,0% |
| `adapters/out/persistencia` | 10 | 1 | 0,91 | 2.993 | 49,0% |
| `adapters/out/seguranca` | 1 | 1 | 0,50 | 210 | 77,2% |
| `adapters/out/capacitacao` | 3 | 0 | 1,00 | 211 | 0,0% |
| `adapters/out/notificacao` | 3 | 0 | 1,00 | 80 | 0,0% |
| `config` | 1 | 0 | 1,00 | 188 | 86,1% |

Como ler a tabela:

- O domínio é estável: I fica entre 0,00 e 0,33, e ele não depende de framework. Casos de uso e adaptadores ficam no extremo instável (I perto de 1), como a arquitetura hexagonal espera.
- Ca = 0 em `usecase` e `web` não quer dizer que nada os usa. Eles são ligados pelo Spring por injeção, sem `import`.
- A cobertura baixa em `adapters/out/persistencia`, `capacitacao` e `notificacao` vem dos testes de integração PostgreSQL ignorados (seção 6).

### 5.3. Duplicação

Blocos de 6 linhas normalizadas repetidos dentro de `src/main`:

| Métrica | Valor |
| --- | ---: |
| Linhas normalizadas analisadas | 9.188 |
| Linhas em blocos duplicados | 1.055 (11,48%) |
| Blocos repetidos distintos | 196 |

### 5.4. Nomenclatura mista

O código usa sufixos em inglês e em português para o mesmo papel. Número de tipos por sufixo:

| Papel | Inglês | Português |
| --- | ---: | ---: |
| Porta | `Port`: 15 | `Porta`: 6 |
| Caso de uso | `UseCase`: 31, `UseCaseImpl`: 2, `Service`: 6 | `CasoDeUso`: 5 |
| Controlador | `Controller`: 14 | `Controlador`: 3 |
| Adaptador | `Adapter`: 16 | `Adaptador`: 5 |
| Requisição | `Request`: 21 | `Requisicao`: 5 |
| Resposta | `Response`: 19 | `Resposta`: 2 |
| Tratador de exceções | `ExceptionHandler`: 4 | `TratadorExcecoes`: 5 |
| DTO de entrada | `InputDTO`: 27 | `EntradaDTO`: 2 |
| DTO de saída | `OutputDTO`: 28 | `SaidaDTO`: 2 |
| Entidade JPA | `Entity`: 21 | `Entidade`: 3 |

## 6. Testes e cobertura do backend

| Métrica | Valor |
| --- | ---: |
| Execução (`./mvnw test`) | 1.071 testes, 0 falhas, 0 erros, 131 ignorados, 31,3 s |
| Classes de teste | 123 |
| Classes de integração PostgreSQL | 22 (ignoradas sem banco) |
| Classes `@WebMvcTest` | 23 |
| Classes `@SpringBootTest` | 22 |
| Usos de mock (`mock(`, `@Mock`, `@MockitoBean`) | 138 |

Cobertura JaCoCo, sem os testes de integração PostgreSQL:

| Contador | Cobertura |
| --- | ---: |
| Instruções | 80,9% |
| Linhas | 80,1% |
| Ramos | 68,6% |
| Complexidade | 67,3% |
| Métodos | 79,0% |

## 7. Frontend

| Métrica | Valor |
| --- | ---: |
| Arquivos TS/TSX | 119 (80 TSX) |
| Linhas de código TS/TSX | 9.140 |
| Linhas de código CSS | 731 |
| Features (`src/features`) | 10 |
| Componentes (funções com inicial maiúscula) | 148 |
| Hooks customizados exportados | 6 |
| `useState` | 202 |
| `useEffect` | 15 |
| Tipo `any` | 0 |
| ESLint | 0 erros, 0 avisos |
| `tsc -b` | 0 erros |
| Bundle (`vite build`) | JS 527,1 KB, CSS 42,3 KB, sem compressão |
| Playwright | 18 specs, 150 casos (contados, não executados) |

Maiores arquivos, em linhas de código:

| Arquivo | Linhas |
| --- | ---: |
| `tarefas/TasksPage.tsx` | 321 |
| `capacitacoes/api.ts` | 301 |
| `areas-risco/RiskAreasPage.tsx` | 268 |
| `areas-risco/AreaRiscoFormDialog.tsx` | 261 |
| `epis/EpiPage.tsx` | 248 |
| `ocorrencias/AcaoPlanoItem.tsx` | 238 |
| `ocorrencias/OccurrencesPage.tsx` | 229 |
| `tarefas/TarefaFormDialog.tsx` | 219 |

## 8. Pontos de atenção para a refatoração

Observações tiradas das métricas acima; a escolha dos padrões cabe ao grupo.

| Sinal medido | Onde | O que acompanhar após a refatoração |
| --- | --- | --- |
| Construtores sobrecarregados (até 5 em `Epi`) e 158 assinaturas com mais de 5 parâmetros | `Epi`, `Tarefa`, entidades JPA, DTOs de saída | Classes com 3 ou mais construtores; métodos com muitos parâmetros |
| Usuário autenticado lido no caso de uso | 7 arquivos, 27 usos de `SecurityContextHolder` | Violação `application_importa_spring_security` |
| Lógica condicional concentrada | `FiltroOcorrenciasDTO`, `calcularProjecao`, `descreverAlteracoes`, `OcorrenciaOutputDTO` | Métodos com complexidade > 10; complexidade máxima |
| Mapeamento extenso entre domínio e DTO | `OcorrenciaOutputDTO.deDominio`, `EmprestimoOutputDTO`, `EpiResponse` | Métodos longos; duplicação |
| Auditoria espalhada e no adaptador | `PlanoDeAcaoJpaAdapter.salvar` e chamadas a `LogAuditoria` em cada caso de uso | Duplicação; tamanho dos casos de uso |
| Tratamento de exceções repetido | 8 `@RestControllerAdvice`, 37 `@ExceptionHandler` | Número de handlers |
| Nomenclatura mista | Seção 5.4 | Contagem de sufixos por idioma |
| Validação de nulo manual | 578 comparações com `null` | Comparações com `null` |
| Classes grandes | `Epi` (416 linhas), `OcorrenciaUseCase` (368 linhas) | Arquivos com mais de 300 linhas; maiores arquivos |

## 9. Método e limitações

- **Linhas:** contagem por linha física. Comentário é a linha que começa com `//` ou está dentro de `/* */`.
- **Complexidade no código-fonte:** estimativa de 1 + pontos de decisão (`if`, `for`, `while`, `case`, `catch`, `&&`, `||`, ternário), feita depois de remover comentários e strings. A complexidade do JaCoCo vem do bytecode e é mais precisa, mas só existe se os testes forem executados.
- **Métodos:** identificados por expressão regular. Corpos de lambdas e de classes internas entram no método que os contém. Os registros canônicos sem corpo não entram.
- **Acoplamento:** considera só `import` de pacotes do projeto. Referências dentro do mesmo pacote e ligações por injeção do Spring não aparecem.
- **Duplicação:** janela de 6 linhas após normalizar espaços, descartando chaves, `import`, `package` e anotações. É uma heurística, não equivale ao CPD.
- **Limites usados:** método longo > 30 linhas; classe grande > 300 linhas de código; muitos parâmetros > 5.
- **Ambiente:**
  - JDK 25; testes de integração PostgreSQL ignorados.
  - Playwright não executado: falta o Chromium build 1243 na máquina.
  - O build do frontend gera `frontend/dist`, que está fora do controle de versão.
