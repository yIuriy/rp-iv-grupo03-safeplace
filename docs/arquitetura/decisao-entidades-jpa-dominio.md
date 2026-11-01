# Decisão: entidades JPA no domínio

Issue: [#254](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/254). Situação: proposta em novembro de 2026, após a avaliação do professor; aprovada pelo grupo na revisão do PR. Vale para as migrações [#255](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/255) a [#262](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/262).

## 1. Contexto

Na avaliação presencial, o professor pediu, no item "Persistência com JPA":

- usar JPA na persistência e deixar de escrever SQL à mão;
- manter as entidades no domínio (o "model" do MVC);
- substituir os arquivos que estão dentro de `persistencia`.

Pediu também, em outros itens, programação orientada a interface, associações por objeto em vez de ids (`Inspecao.java:21-25`) e validação fora da criação dos objetos de domínio.

### Como estava

- O projeto já usava Spring Data JPA, mas com **duas classes por conceito**: a classe de `domain` e uma cópia anotada com `@Entity` em `adapters/out/persistencia`. Eram 25 classes `*Entity`/`*Entidade`, num pacote de cerca de 3.800 linhas.
- Cada adaptador convertia entre as duas (`toDomain`/`toEntity`, 77 ocorrências). Havia conversões duplicadas e divergentes: a `AreaRisco` lida pela ocorrência vinha sem os EPIs obrigatórios, e o `PlanoDeAcao` lido pela ocorrência vinha sem responsável e sem alerta.
- Havia SQL escrito à mão em quatro lugares: `CapacitacaoJdbcAdapter` (16 comandos), `ImpedimentoCapacitacaoJdbcAdapter`, o `INSERT ... ON CONFLICT` de `NotificacaoAcaoJpaRepository` e o upsert com `RETURNING` de `SequenciaProtocoloCatJpaRepository`.
- O domínio guardava associações como ids soltos: `Inspecao.areaRiscoId`, `AreaRisco.setorId`, `Emprestimo.epiId` e `colaboradorId`, `AlocacaoTarefa.tarefaId` e `colaboradorId`, entre outros.

## 2. Decisão

### 2.1 A classe de domínio é a entidade JPA

As classes de `domain` recebem as anotações JPA. As classes espelhadas de `adapters/out/persistencia` e as conversões entre elas são removidas, módulo a módulo.

O domínio pode importar apenas:

- `jakarta.persistence.*`, que é a especificação padrão do Jakarta EE, e não uma biblioteca do Spring;
- `org.hibernate.annotations.Immutable`, somente nas tabelas que só recebem inserções (auditoria e notificações), como já é feito hoje.

O domínio continua sem importar Spring, Spring Data, Jackson ou classes de `application` e `adapters`.

### 2.2 Convenções de mapeamento

Para que as oito migrações saiam iguais, cada entidade segue as convenções que as classes `*Entity` já usavam:

| Elemento | Convenção | Motivo |
| --- | --- | --- |
| Tabela | `@Table(name = "...")` com o nome atual da tabela | O banco não muda |
| Coluna | `@Column(name = "...")` sempre que o nome do campo for diferente da coluna | Idem |
| Identificador | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` | Já é a estratégia de todas as 19 entidades com id gerado |
| Enumeração | `@Enumerated(EnumType.STRING)` | O banco já guarda o nome da constante (os mapeadores usavam `name()` e `valueOf`) |
| Associação para um | `@ManyToOne(fetch = FetchType.LAZY)` com `@JoinColumn(name = "..._id")` | Associação por objeto, como no diagrama de classes |
| Coleção | `@OneToMany(mappedBy = "...")` ou `@ManyToMany` com `@JoinTable` | Idem |
| Objeto de valor | `@Embeddable` na classe e `@Embedded` no campo, na mesma coluna de hoje | Caso de `NumeroProtocoloCAT` (#230) |
| Herança de ocorrência | `@Inheritance(strategy = InheritanceType.JOINED)` | Tabelas `ocorrencias`, `acidentes` e `incidentes` atuais |
| Herança de usuário | `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` com `@DiscriminatorColumn(name = "perfil")` | Tabela única atual |
| Tabela só de inserção | `@Immutable` | Log de auditoria e notificações (RNF05) |

Um id só continua id quando a referência é polimórfica e não tem tabela única de destino. Os casos conhecidos são `LogAuditoria.entidadeId` (qualquer entidade auditada) e, a decidir em #257, `ExigenciaCapacitacao.alvoId` (tarefa ou EPI). A exceção precisa ser justificada no PR.

### 2.3 Construção dos objetos

- O JPA exige um construtor sem argumentos. Ele fica `protected`, para uso exclusivo do JPA.
- Os campos deixam de ser `final`, porque o JPA os preenche depois de criar o objeto.
- Não há setters públicos. O estado muda pelos métodos de negócio que já existem (`devolver`, `classificar`, `arquivar`...), que preservam o encapsulamento.
- A criação de objetos novos passa pelas fábricas e builders das issues de padrões: `FabricaEpi` (#225), `FabricaRelatoOcorrencia` (#239), Builders de `Emprestimo`, `Tarefa`, `Acidente` e `Incidente` (#226, #240) e `FabricaCapacitacao` (#268). Os métodos estáticos de criação deixam de existir.
- Ler um objeto do banco é papel do JPA, então os métodos `reconstituir` e os construtores de reconstrução são removidos.
- `equals` e `hashCode` comparam pelo id quando ele existe. Não usar `@Data` do Lombok nas entidades, porque ele inclui as coleções preguiçosas e as associações.

### 2.4 Validação

As verificações que hoje estão nos construtores saem do domínio e vão para a camada `validacao`, criada em #228 com o padrão Chain of Responsibility. O caso de uso valida antes de chamar o repository e não salva nada se a validação falhar.

As anotações `@Column(nullable = false, length = ...)` continuam espelhando as restrições do banco, mas não substituem a validação: elas só falham no banco, depois que o caso de uso já decidiu salvar.

### 2.5 Portas e adaptadores

As portas de repositório continuam sendo interfaces em `application/port/out`, com os nomes definidos em #269 (`XxxRepository`). Cada uma tem um adaptador. Exemplo com a porta real de inspeções, como ficará depois de #255:

```java
// application/port/out: o caso de uso conhece só esta interface.
public interface InspecaoRepository {
    Inspecao salvar(Inspecao inspecao);
    List<Inspecao> listarPorArea(Integer areaRiscoId);
}

// adapters/out/persistencia: o adaptador implementa a porta e delega ao Spring Data.
interface InspecaoJpaRepository extends JpaRepository<Inspecao, Integer> {
    List<Inspecao> findByAreaRiscoIdOrderByDataInspecaoDesc(Integer areaRiscoId);
}

@Repository
class InspecaoJpaAdapter implements InspecaoRepository {
    private final InspecaoJpaRepository jpa;
    // salvar e listarPorArea só repassam; não há conversão de tipos.
}
```

Assim:

- os casos de uso não importam Spring Data e continuam testáveis com fakes das portas, sem banco;
- a programação orientada a interface, pedida pelo professor, é mantida;
- trocar a tecnologia de persistência de um módulo afeta só o adaptador.

### 2.6 Carregamento das associações

- Associações `LAZY` por padrão.
- `spring.jpa.open-in-view` já está `false`. Por isso, o caso de uso que lê associações é `@Transactional` e monta o DTO de saída dentro da transação. Os controllers nunca recebem entidades, como já é a regra.
- Listagens que precisam das associações usam `JOIN FETCH` em JPQL ou `@EntityGraph`, para evitar consultas N+1 (o caso conhecido é a listagem de planos de ação, #262).

### 2.7 Esquema do banco

O esquema não muda. O mapeamento segue as tabelas e colunas criadas pelas migrações Flyway atuais (até a V33). A configuração `spring.jpa.hibernate.ddl-auto: validate`, que já está ativa, faz a aplicação recusar a subida se uma anotação não corresponder ao banco, então essa regra é verificada a cada execução.

Se uma migração exigir mudança no banco, ela entra como nova migração Flyway (V34 em diante), justificada no PR.

### 2.8 SQL escrito à mão

Não há SQL escrito à mão nem `JdbcTemplate` nos adaptadores. São permitidos:

- consultas derivadas do nome do método no Spring Data;
- JPQL em `@Query`, sem `nativeQuery = true`.

Os quatro pontos com SQL escrito à mão são substituídos nas issues de migração: capacitação em #257, protocolo CAT em #261 (com bloqueio pessimista, mantendo a garantia de concorrência) e notificações do plano de ação em #262.

## 3. Ordem de migração

Cada módulo é um PR. O piloto define o modelo que os demais copiam.

| Ordem | Issue | Módulo | Depende de |
| ---: | --- | --- | --- |
| 1 | #255 | Área de risco, setor e inspeção (piloto) | #254 |
| 2 | #256 | Usuários e auditoria | #255 |
| 3 | #257 | Capacitação (fim do JDBC) | #256 |
| 4 | #258 | EPI e estoque | #256 |
| 5 | #259 | Manutenção e projeção de EPI | #258 |
| 6 | #260 | Empréstimo, tarefa e alocação | #255, #256, #258 |
| 7 | #261 | Ocorrência e protocolo CAT | #255, #256 |
| 8 | #262 | Plano de ação e notificações | #261 e #238 |

As issues de validação do mesmo módulo (#263 a #266) devem entrar depois da migração correspondente, porque as duas mexem nos construtores das mesmas classes.

## 4. Consequências

**Ganhos:**

- uma classe por conceito, sem as 25 classes espelhadas e as conversões entre elas;
- fim das reconstruções divergentes (as issues de Data Mapper #229 e #242 foram fechadas como substituídas);
- associações por objeto, como no diagrama de classes;
- nenhum SQL escrito à mão.

**Custos assumidos:**

- O domínio passa a depender das anotações de `jakarta.persistence`. É um desvio consciente da pureza hexagonal descrita no guia, aceito para atender a avaliação. O impacto é limitado: as anotações são metadados e não exigem o Spring nem o banco para executar, então os testes de domínio continuam rodando sem contexto.
- Os campos deixam de ser `final`. A imutabilidade de fora para dentro continua garantida pela ausência de setters públicos.

## 5. Alternativas descartadas

- **Manter as classes `*Entity` com Data Mappers (#229, #242):** resolveria as conversões divergentes, mas contraria o pedido explícito do professor.
- **Usar o Spring Data diretamente nos casos de uso, sem porta:** menos código, mas os casos de uso passariam a depender do framework, perderiam os testes com fakes e contrariariam a programação orientada a interface.

## 6. Como verificar cada PR de migração

- [ ] Nenhuma classe `*Entity`/`*Entidade` do módulo continua existindo.
- [ ] A aplicação sobe com `ddl-auto: validate` (o mapeamento corresponde ao banco).
- [ ] Nenhum `JdbcTemplate`, `nativeQuery = true` ou string SQL no módulo.
- [ ] Ids soltos do módulo trocados por associações, ou a exceção justificada.
- [ ] Testes de persistência e de API passando; contrato HTTP inalterado.
