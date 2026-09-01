# Histórias de Usuário do SafePlace

## Objetivo

Este documento apresenta as histórias de usuário dos requisitos vigentes do SafePlace. Ele relaciona requisitos funcionais, requisitos não funcionais, prioridades MoSCoW, casos de uso e o recorte do MVP.

As histórias estão organizadas conforme a prioridade de entrega:

1. MVP, formado pelos requisitos `Must have`;
2. backlog `Should have`;
3. backlog `Could have`;
4. fora da versão atual, para requisitos `Won't have`.

Um requisito fora do MVP continua válido. Sua prioridade indica quando ele será entregue, não se ele deve ser removido.

## Fontes analisadas

- `AGENTS.md`;
- `docs/requisitos/requisitos-funcionais.md`;
- `docs/requisitos/requisitos-nao-funcionais.md`;
- `docs/requisitos/priorizacao-moscow.md`;
- `docs/casos-de-uso/casos-de-uso.md`;
- `docs/mvp/especificacao-mvp-arquitetura.md`.

Os diagramas não foram usados como fonte porque as informações necessárias estavam disponíveis nos documentos textuais.

## 1. Diagnóstico das fontes

### 1.1. Requisitos encontrados

Foram encontrados todos os 23 requisitos funcionais, de RF01 a RF23, e todos os 17 requisitos não funcionais, de RNF01 a RNF17.

Os requisitos funcionais estão distribuídos da seguinte forma:

- `Must have`: RF03, RF04, RF05, RF11, RF13, RF16 e RF23;
- `Should have`: RF01, RF02, RF06, RF07, RF09, RF12, RF14 e RF20;
- `Could have`: RF08, RF10, RF15, RF17, RF18, RF19 e RF22;
- `Won't have`: RF21.

Os requisitos não funcionais estão distribuídos da seguinte forma:

- `Must have`: RNF01, RNF03, RNF04, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16;
- `Should have`: RNF02, RNF06, RNF10 e RNF11;
- `Could have`: RNF07, RNF08, RNF13 e RNF17.

### 1.2. Atores documentados

Os perfis com acesso ao sistema são:

- Gestor de Segurança;
- Supervisor.

O Colaborador é uma pessoa cadastrada no SafePlace, mas não possui conta, senha ou acesso direto ao sistema. Quando um Colaborador comunica ou vivencia uma ocorrência, o Supervisor é responsável pelo registro no sistema.

Visitantes e terceirizados aparecem como pessoas gerenciadas ou destinatárias de empréstimos, mas não estão documentados como usuários autenticados.

### 1.3. Decisão validada pela equipe sobre o Colaborador

A equipe confirmou que o Colaborador não acessa o SafePlace. Essa decisão afeta três fontes atuais:

- RF16 afirma que o Colaborador pode registrar incidentes;
- RNF03 inclui o Colaborador entre os perfis do controle de acesso;
- UC09 afirma que o Colaborador deve estar autenticado e pode relatar incidentes;
- RF23 afirma que uma senha é gerada para o Colaborador.

Neste documento, a decisão validada pela equipe prevalece na definição das histórias: o Supervisor registra acidentes e incidentes, inclusive quando a ocorrência envolve ou foi comunicada por um Colaborador. As fontes conflitantes precisam ser atualizadas em uma revisão própria para recuperar a consistência documental.

### 1.4. Inconsistências e lacunas

1. **UC13 ausente:** a especificação do MVP relaciona RF23 ao UC13, mas `docs/casos-de-uso/casos-de-uso.md` termina no UC12. As operações abrangidas por "gerenciar" permanecem `[A CONFIRMAR]`.
2. **Cadastro de ocorrências pelo Gestor:** RF13 atribui o registro de acidentes ao Supervisor, enquanto UC05 permite ao Gestor cadastrar acidentes e incidentes. A permissão do Gestor está `[A CONFIRMAR]`.
3. **Consulta, atualização e arquivamento de ocorrências:** o MVP e UC05 incluem essas operações, mas RF13 e RF16 tratam do registro. Não foram criadas histórias independentes sem um RF que sustente esses objetivos.
4. **Certificado de Aprovação:** UC01, UC06 e UC12 exigem verificações de CA, mas RF20 é `Should have` e a validação externa está fora do MVP. A disponibilidade dos dados locais de CA está `[A CONFIRMAR]`.
5. **Área de risco e EPI:** UC03 exige vínculo obrigatório de EPIs à área, mas RF05 não explicita essa obrigação. Seu pertencimento ao MVP está `[A CONFIRMAR]`.
6. **Notificação do UC09:** o envio automático ao Gestor não possui RF ou prioridade própria. Ele não foi transformado em critério obrigatório.
7. **Prazo de devolução:** RF11 menciona itens não devolvidos na data determinada, mas UC12 não explica como essa data é definida.
8. **Elementos legados:** sensores, Internet das Coisas, rotas de evacuação e simulações de emergência não são requisitos vigentes e não aparecem nas histórias.

## 2. Matriz geral de cobertura

### 2.1. Requisitos funcionais

| Requisito | Tipo | Prioridade | MVP ou backlog | UC relacionado | Histórias propostas | Situação da fonte |
| --- | --- | --- | --- | --- | --- | --- |
| RF01 | RF | Should have | backlog Should | UC05 | HU-14 | Relação parcial no resumo do UC05 |
| RF02 | RF | Should have | backlog Should | UC05 | HU-15 | Relação parcial no resumo do UC05 |
| RF03 | RF | Must have | MVP | UC06 | HU-01 e HU-02 | Alinhado; dados das movimentações não detalhados |
| RF04 | RF | Must have | MVP | UC01 | HU-03 e HU-04 | Alinhado; CA e descarte pertencem ao backlog |
| RF05 | RF | Must have | MVP | UC03 | HU-05 e HU-06 | Vínculo obrigatório com EPI a confirmar |
| RF06 | RF | Should have | backlog Should | UC11 | HU-16 e HU-17 | Alinhado |
| RF07 | RF | Should have | backlog Should | UC07 | HU-18 | Alertas do UC precisam de confirmação de escopo |
| RF08 | RF | Could have | backlog Could | não identificado | HU-24 | Ator beneficiado não identificado |
| RF09 | RF | Should have | backlog Should | não identificado | HU-19 | Sem UC e sem ator explícito |
| RF10 | RF | Could have | backlog Could | UC02 | HU-25 | Gestor principal e Supervisor secundário |
| RF11 | RF | Must have | MVP | UC12 | HU-07, HU-08 e HU-09 | Prazo de devolução não definido |
| RF12 | RF | Should have | backlog Should | UC08 | HU-20 | Alinhado |
| RF13 | RF | Must have | MVP | UC09 e UC05 | HU-10 | Divergência sobre permissão do Gestor |
| RF14 | RF | Should have | backlog Should | UC01 e UC06 | HU-21 | Fluxo presente como cenário futuro |
| RF15 | RF | Could have | backlog Could | UC05 | HU-26 | Alinhado como extensão futura |
| RF16 | RF | Must have | MVP | UC09 e UC05 | HU-11 | Fontes antigas atribuem acesso ao Colaborador |
| RF17 | RF | Could have | backlog Could | não identificado | HU-27 | Sem UC e sem ator explícito |
| RF18 | RF | Could have | backlog Could | não identificado | HU-28 | Sem UC e sem ator explícito |
| RF19 | RF | Could have | backlog Could | UC05, parcialmente | HU-29 | Sem fluxo completo de emissão de CAT |
| RF20 | RF | Should have | backlog Should | UC01, UC06 e UC08, parcialmente | HU-22 e HU-23 | Sem UC de gestão de fornecedores |
| RF21 | RF | Won't have | fora da versão atual | UC10 | HU-31 e HU-32 | Claramente fora do MVP |
| RF22 | RF | Could have | backlog Could | UC12, parcialmente | HU-30 | UC12 cobre somente empréstimo temporário |
| RF23 | RF | Must have | MVP | UC13 citado, mas ausente | HU-12 e HU-13 | Decisão sobre Colaborador conflita com RF atual |

### 2.2. Requisitos não funcionais

| Requisito | Tipo | Prioridade | MVP ou backlog | UC relacionado | Histórias propostas | Situação da fonte |
| --- | --- | --- | --- | --- | --- | --- |
| RNF01 | RNF | Must have | MVP | UCs do MVP | Restrição transversal | Tempos de 3 e 1 segundo definidos |
| RNF02 | RNF | Should have | backlog Should | transversal | Restrição transversal | Carga mínima de 20 usuários definida |
| RNF03 | RNF | Must have | MVP | UCs com acesso | Histórias de Gestor e Supervisor | Perfil Colaborador conflita com decisão da equipe |
| RNF04 | RNF | Must have | MVP | UC05 e UC09 | HU-10 e HU-11 | AES-256 e TLS 1.3 definidos |
| RNF05 | RNF | Must have | MVP | UCs que alteram registros | Histórias de cadastro e alteração | Retenção mínima de 5 anos definida |
| RNF06 | RNF | Should have | backlog Should | transversal | Restrição transversal | Disponibilidade e manutenção mensuráveis |
| RNF07 | RNF | Could have | backlog Could | transversal | Restrição transversal | Backup, RPO e RTO definidos |
| RNF08 | RNF | Could have | backlog Could | UC09, cenário offline | Restrição transversal | Cenário offline fora do MVP |
| RNF09 | RNF | Must have | MVP | UCs do MVP | Todas as histórias aplicáveis | Regras normativas ainda precisam ser mapeadas |
| RNF10 | RNF | Should have | backlog Should | UC05 e UC10, parcialmente | Histórias que geram documentos | Sem fluxo geral de assinatura ou hash |
| RNF11 | RNF | Should have | backlog Should | UCs de interface | Restrição transversal | WCAG 2.1 AA e dispositivos definidos |
| RNF12 | RNF | Must have | MVP | tarefas principais | Histórias principais | Limite de 2 horas de treinamento definido |
| RNF13 | RNF | Could have | backlog Could | transversal | Restrição transversal | Idiomas e extensibilidade definidos |
| RNF14 | RNF | Must have | MVP | UCs do MVP | Restrição arquitetural | Direção arquitetural definida no MVP |
| RNF15 | RNF | Must have | MVP | todos | Entrega técnica verificável | API e modelo de dados dependem da implementação |
| RNF16 | RNF | Must have | MVP | UCs web | Todas as histórias do MVP | Navegadores e sistemas definidos |
| RNF17 | RNF | Could have | backlog Could | não identificado | Entrega técnica verificável | Sem caso de uso de integração |

## 3. Histórias do MVP

### HU-01 - Consultar o estoque de EPIs

**História**

Como **Gestor de Segurança**, quero **consultar os saldos, os estados e o histórico de movimentações dos EPIs**, para **conhecer a disponibilidade dos equipamentos e tomar decisões com dados atualizados**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF03 |
| Caso de uso | UC06 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O Gestor precisa saber quais equipamentos existem e suas quantidades antes de registrar entregas, reposições ou outras movimentações. A consulta mantém o controle preventivo dos equipamentos usados na proteção dos trabalhadores.

**Pré-condições**

- O Gestor de Segurança deve estar autenticado.
- Os EPIs devem estar previamente cadastrados.

**Regras de negócio**

- A consulta deve apresentar os EPIs disponíveis com nome, quantidade e status (`docs/casos-de-uso/casos-de-uso.md`, UC06).
- O histórico de movimentações deve estar disponível na consulta de um EPI específico (`docs/casos-de-uso/casos-de-uso.md`, UC06).
- Itens com saldo igual ou inferior ao estoque mínimo devem ser destacados visualmente (`docs/casos-de-uso/casos-de-uso.md`, UC06).

**Critérios de aceitação**

Cenário: Consultar o estoque
Dado que o Gestor de Segurança está autenticado
E existem EPIs cadastrados
Quando consultar o estoque
Então o sistema apresenta os EPIs com nome, quantidade e status
E permite consultar os detalhes e o histórico de movimentações de um item.

Cenário: Identificar estoque crítico
Dado que um EPI possui saldo igual ou inferior ao estoque mínimo
Quando o Gestor consultar o estoque
Então o sistema destaca o item como estoque vazio ou crítico.

Cenário: Restringir acesso
Dado que um usuário não possui a permissão necessária
Quando tentar consultar o estoque
Então o sistema impede o acesso.

Cenário: Tempo da consulta
Dado que o sistema está em condições normais de uso
Quando o Gestor realizar uma consulta simples de estoque
Então o resultado é apresentado em até 1 segundo.

**Dependências**

- Cadastro prévio dos EPIs.

**Fora do escopo desta história**

- Registro de entradas e saídas, coberto pela HU-02.
- Empréstimos e devoluções, cobertos pelas HU-07 e HU-08.
- Descarte, fornecedores, gestão de CA e substituição inteligente.

**Dúvidas para validação**

- `[A CONFIRMAR]` Como o estoque mínimo de cada EPI é definido e atualizado?

### HU-02 - Registrar movimentações de estoque

**História**

Como **Gestor de Segurança**, quero **registrar entradas e saídas de EPIs**, para **manter o saldo e o histórico do estoque corretos**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF03 |
| Caso de uso | UC06 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

Entradas e saídas alteram a quantidade disponível. Registrar essas movimentações evita saldos incorretos e fornece um histórico para acompanhamento e auditoria.

**Pré-condições**

- O Gestor de Segurança deve estar autenticado.
- O EPI deve estar previamente cadastrado.

**Regras de negócio**

- O estoque não pode ficar com quantidade negativa (`docs/casos-de-uso/casos-de-uso.md`, UC06).
- Cada movimentação deve atualizar o saldo e permanecer no histórico (`docs/mvp/especificacao-mvp-arquitetura.md`, seção 5).
- A operação deve produzir log imutável mantido por no mínimo 5 anos (`docs/requisitos/requisitos-nao-funcionais.md`, RNF05).

**Critérios de aceitação**

Cenário: Registrar entrada
Dado que o Gestor está autenticado
E o EPI está cadastrado
Quando registrar uma entrada válida
Então o saldo do EPI é aumentado
E a movimentação é adicionada ao histórico.

Cenário: Registrar saída
Dado que existe saldo suficiente
Quando o Gestor registrar uma saída válida
Então o saldo é reduzido
E a movimentação é adicionada ao histórico.

Cenário: Impedir saldo negativo
Dado que a quantidade de saída é superior ao saldo disponível
Quando o Gestor confirmar a movimentação
Então o sistema bloqueia a saída
E informa o saldo máximo disponível.

Cenário: Auditar movimentação
Dado que uma entrada ou saída foi registrada
Quando a operação for concluída
Então o log imutável identifica o usuário, a data, a hora e o dado alterado
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de registro
Dado que o sistema está em condições normais de uso
Quando uma movimentação válida for confirmada
Então o sistema responde em até 3 segundos.

**Dependências**

- Cadastro prévio do EPI.
- Saldo suficiente para uma saída.

**Fora do escopo desta história**

- Empréstimos, devoluções, descarte, fornecedores e validação externa do CA.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais dados são obrigatórios em cada movimentação?
- `[A CONFIRMAR]` A validação local do CA será entregue no MVP ou com o RF20?

### HU-03 - Consultar a manutenção dos EPIs

**História**

Como **Gestor de Segurança**, quero **consultar o estado e o histórico de manutenção de um EPI**, para **verificar se o equipamento está em condições adequadas de uso**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF04 |
| Caso de uso | UC01 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O Gestor precisa acompanhar a conservação dos equipamentos em uso e armazenados. A consulta evita que EPIs sem condições adequadas continuem disponíveis.

**Pré-condições**

- O Gestor de Segurança deve estar autenticado.
- Os EPIs devem estar previamente cadastrados.

**Regras de negócio**

- A listagem operacional deve exibir somente EPIs ativos (`docs/casos-de-uso/casos-de-uso.md`, UC01).
- Os indicadores de status devem possuir clareza visual (`docs/casos-de-uso/casos-de-uso.md`, UC01).
- A consulta deve apresentar histórico e datas de manutenção (`docs/casos-de-uso/casos-de-uso.md`, UC01).

**Critérios de aceitação**

Cenário: Consultar manutenção
Dado que o Gestor está autenticado
E existem EPIs ativos
Quando selecionar um EPI
Então o sistema apresenta o estado atual
E o histórico e as datas de manutenção.

Cenário: Filtrar por status
Dado que existem EPIs com estados diferentes
Quando o Gestor selecionar um filtro
Então o sistema apresenta somente os EPIs correspondentes.

Cenário: EPI não encontrado
Dado que o EPI informado não existe
Quando o Gestor tentar consultá-lo
Então o sistema informa que o registro não foi encontrado
E permite uma nova busca.

Cenário: Tempo da consulta
Dado que o sistema está em condições normais de uso
Quando o Gestor realizar uma consulta simples
Então o resultado é apresentado em até 1 segundo.

**Dependências**

- Cadastro prévio do EPI.

**Fora do escopo desta história**

- Registro de manutenção, descarte e validação externa do CA.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais são todos os status válidos de manutenção?

### HU-04 - Registrar manutenção de EPI

**História**

Como **Gestor de Segurança**, quero **registrar a manutenção realizada em um EPI**, para **manter atualizados seu estado e seu histórico de conservação**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF04 |
| Caso de uso | UC01 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O registro preserva o resultado do serviço realizado e permite acompanhar a qualidade de proteção oferecida pelo EPI.

**Pré-condições**

- O Gestor de Segurança deve estar autenticado.
- O EPI deve estar previamente cadastrado.

**Regras de negócio**

- O registro solicita data, descrição e resultado da manutenção (`docs/casos-de-uso/casos-de-uso.md`, UC01).
- A manutenção atualiza o estado e o histórico do EPI (`docs/casos-de-uso/casos-de-uso.md`, UC01).
- A operação deve produzir log imutável mantido por no mínimo 5 anos (`docs/requisitos/requisitos-nao-funcionais.md`, RNF05).

**Critérios de aceitação**

Cenário: Registrar manutenção
Dado que o Gestor está autenticado
E o EPI existe
Quando informar data, descrição e resultado
E confirmar o registro
Então o sistema registra a manutenção
E atualiza o estado e o histórico do EPI.

Cenário: EPI não encontrado
Dado que o EPI informado não existe
Quando o Gestor tentar registrar sua manutenção
Então o sistema não cria o registro
E informa que o EPI não foi encontrado.

Cenário: Auditar manutenção
Dado que uma manutenção foi registrada
Quando a operação for concluída
Então o log imutável identifica o usuário, a data, a hora e os dados alterados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de registro
Dado que o sistema está em condições normais de uso
Quando uma manutenção válida for confirmada
Então o sistema responde em até 3 segundos.

**Dependências**

- Cadastro prévio do EPI.

**Fora do escopo desta história**

- Consulta da manutenção, descarte definitivo e validação externa do CA.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais campos são obrigatórios e quais resultados são aceitos?
- `[A CONFIRMAR]` O bloqueio por CA vencido pertence ao MVP?

### HU-05 - Manter o cadastro de áreas de risco

**História**

Como **Gestor de Segurança**, quero **cadastrar e atualizar as áreas de risco**, para **manter o mapa de riscos coerente com as condições atuais dos setores**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF05 |
| Caso de uso | UC03 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O mapeamento identifica onde existem riscos de acidente. Cadastro e atualização permanecem juntos porque mantêm o mesmo objeto e entregam o mesmo valor: um mapa vigente.

**Pré-condições**

- O Gestor de Segurança deve estar autenticado.

**Regras de negócio**

- O cadastro representa identificação, agentes de risco e limites do setor (`docs/casos-de-uso/casos-de-uso.md`, UC03).
- Uma área não pode usar código já cadastrado (`docs/casos-de-uso/casos-de-uso.md`, UC03).
- Alterações de limites físicos devem ser auditadas (`docs/casos-de-uso/casos-de-uso.md`, UC03).
- UC03 exige EPIs obrigatórios vinculados à área, mas essa obrigação no MVP está `[A CONFIRMAR]` (`docs/casos-de-uso/casos-de-uso.md`, UC03).

**Critérios de aceitação**

Cenário: Cadastrar área
Dado que o Gestor está autenticado
Quando informar os dados documentados e confirmar
Então o sistema registra a área
E atualiza o mapa de riscos.

Cenário: Atualizar área
Dado que uma área está cadastrada
Quando o Gestor alterar agentes de risco ou limites
Então o sistema salva as alterações
E registra uma nova versão no histórico.

Cenário: Impedir código duplicado
Dado que já existe uma área com o código informado
Quando o Gestor tentar salvar outra área com o mesmo código
Então o sistema bloqueia o cadastro
E informa a duplicidade.

Cenário: Auditar alteração de limite
Dado que o Gestor modificou o limite físico
Quando a atualização for concluída
Então o log imutável identifica usuário, data, hora e dados alterados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo da operação
Dado que o sistema está em condições normais de uso
Quando um cadastro ou atualização válida for confirmado
Então o sistema responde em até 3 segundos.

**Dependências**

- Cadastro de EPIs, caso o vínculo obrigatório seja confirmado.

**Fora do escopo desta história**

- Consulta operacional, inspeções, classificação de tarefas e vínculo entre tarefas e EPIs.

**Dúvidas para validação**

- `[A CONFIRMAR]` O vínculo entre área e EPI pertence ao MVP?
- `[A CONFIRMAR]` Quais dados de identificação e formatos de limite são obrigatórios?

### HU-06 - Consultar áreas de risco

**História**

Como **Supervisor**, quero **consultar as áreas de risco e suas diretrizes de proteção**, para **orientar as atividades conforme os riscos mapeados**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF05 |
| Caso de uso | UC03 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O Supervisor utiliza o mapa atualizado nas atividades do setor. A consulta foi separada da manutenção porque possui outro ator e finalidade operacional.

**Pré-condições**

- O Supervisor deve estar autenticado.
- Deve existir área cadastrada para que haja resultado.

**Regras de negócio**

- O mapa atualizado deve ser disponibilizado ao Supervisor (`docs/casos-de-uso/casos-de-uso.md`, UC03).
- A consulta pode ser filtrada pelos níveis baixo, médio, alto e crítico (`docs/casos-de-uso/casos-de-uso.md`, UC03).

**Critérios de aceitação**

Cenário: Consultar áreas
Dado que o Supervisor está autenticado
E existem áreas cadastradas
Quando acessar o mapa de riscos
Então o sistema apresenta as áreas e suas diretrizes de proteção.

Cenário: Filtrar por grau de perigo
Dado que existem áreas com graus diferentes
Quando o Supervisor selecionar um grau
Então o sistema apresenta somente as áreas correspondentes.

Cenário: Restringir acesso
Dado que um usuário não possui a permissão necessária
Quando tentar acessar o mapa
Então o sistema impede o acesso.

Cenário: Tempo da consulta
Dado que o sistema está em condições normais de uso
Quando o Supervisor realizar uma consulta simples
Então o resultado é apresentado em até 1 segundo.

**Dependências**

- Áreas cadastradas pela HU-05.

**Fora do escopo desta história**

- Cadastro, atualização, inspeções e alteração da classificação de periculosidade.

**Dúvidas para validação**

- Nenhuma dúvida adicional além das registradas na HU-05.

### HU-07 - Registrar empréstimo de EPI

**História**

Como **Supervisor**, quero **registrar a entrega de um EPI a um Colaborador**, para **controlar quem está utilizando cada equipamento e manter sua rastreabilidade**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF11 |
| Caso de uso | UC12 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O empréstimo registra a posse do equipamento e relaciona o EPI ao Colaborador responsável, mesmo que o Colaborador não acesse o sistema.

**Pré-condições**

- O Supervisor deve estar autenticado.
- O Colaborador deve estar cadastrado.
- O EPI deve estar cadastrado e disponível.

**Regras de negócio**

- Não é permitida a entrega de EPI com CA vencido (`docs/casos-de-uso/casos-de-uso.md`, UC12).
- Todo empréstimo deve registrar data, hora e responsável pela entrega (`docs/casos-de-uso/casos-de-uso.md`, UC12).
- O empréstimo deve movimentar o estoque e registrar termo de cautela (`docs/casos-de-uso/casos-de-uso.md`, UC12).
- A operação deve produzir log imutável mantido por no mínimo 5 anos (`docs/requisitos/requisitos-nao-funcionais.md`, RNF05).

**Critérios de aceitação**

Cenário: Registrar empréstimo
Dado que o Supervisor está autenticado
E o Colaborador e o EPI estão cadastrados
E o EPI está disponível
Quando o Supervisor confirmar a entrega
Então o sistema registra o empréstimo e o termo de cautela
E identifica data, hora e responsável
E debita o item do estoque.

Cenário: Impedir empréstimo sem saldo
Dado que não existe saldo disponível
Quando o Supervisor tentar confirmar a entrega
Então o sistema bloqueia o empréstimo
E informa a indisponibilidade.

Cenário: Impedir EPI com CA vencido
Dado que o EPI está registrado com CA vencido
Quando o Supervisor tentar confirmar a entrega
Então o sistema bloqueia o empréstimo
E informa o impedimento normativo.

Cenário: Auditar empréstimo
Dado que o empréstimo foi concluído
Quando o registro for persistido
Então o log imutável identifica usuário, data, hora e dados alterados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de registro
Dado que o sistema está em condições normais de uso
Quando um empréstimo válido for confirmado
Então o sistema responde em até 3 segundos.

**Dependências**

- Cadastro do Colaborador.
- Cadastro do EPI.
- Saldo disponível.

**Fora do escopo desta história**

- Empréstimos para visitantes, bloqueios por treinamento, sugestões automáticas e validação externa do CA.

**Dúvidas para validação**

- `[A CONFIRMAR]` Os dados de CA necessários estarão disponíveis no MVP?
- `[A CONFIRMAR]` Como é definida a data prevista para devolução?

### HU-08 - Registrar devolução de EPI

**História**

Como **Supervisor**, quero **registrar a devolução de um EPI**, para **encerrar a posse do Colaborador e atualizar a situação do equipamento**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF11 |
| Caso de uso | UC12 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A devolução encerra o empréstimo e encaminha o equipamento ao estoque ou à manutenção conforme seu estado.

**Pré-condições**

- O Supervisor deve estar autenticado.
- Deve existir empréstimo ativo para o EPI.

**Regras de negócio**

- A devolução deve encerrar o empréstimo e atualizar o histórico (`docs/casos-de-uso/casos-de-uso.md`, UC12).
- O estado informado pode encaminhar o item ao estoque ou à manutenção (`docs/casos-de-uso/casos-de-uso.md`, UC12).
- A operação deve produzir log imutável mantido por no mínimo 5 anos (`docs/requisitos/requisitos-nao-funcionais.md`, RNF05).

**Critérios de aceitação**

Cenário: Devolver EPI apto
Dado que existe empréstimo ativo
Quando o Supervisor registrar a devolução como apto
Então o sistema encerra o empréstimo
E atualiza o histórico
E devolve o item ao estoque.

Cenário: Encaminhar para manutenção
Dado que existe empréstimo ativo
Quando o Supervisor registrar a devolução como manutenção
Então o sistema encerra o empréstimo
E encaminha o item ao controle de manutenção.

Cenário: Empréstimo não encontrado
Dado que não existe empréstimo ativo para o item
Quando o Supervisor tentar registrar a devolução
Então o sistema não altera o estoque
E informa que não encontrou o empréstimo.

Cenário: Auditar devolução
Dado que a devolução foi concluída
Quando o registro for persistido
Então o log imutável identifica usuário, data, hora e dados alterados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de registro
Dado que o sistema está em condições normais de uso
Quando uma devolução válida for confirmada
Então o sistema responde em até 3 segundos.

**Dependências**

- Empréstimo ativo da HU-07.

**Fora do escopo desta história**

- Baixa definitiva, visitantes e execução da manutenção.

**Dúvidas para validação**

- `[A CONFIRMAR]` O estado "descarte" será selecionável no MVP ou somente com o RF14?

### HU-09 - Consultar a rastreabilidade dos EPIs

**História**

Como **Supervisor**, quero **consultar quais EPIs estão disponíveis, emprestados ou pendentes de devolução**, para **acompanhar a posse e a situação de cada equipamento**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF11 |
| Caso de uso | UC12 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A consulta permite identificar quem está em posse de um item e quais equipamentos ultrapassaram a data determinada para devolução.

**Pré-condições**

- O Supervisor deve estar autenticado.
- Os EPIs devem estar cadastrados.

**Regras de negócio**

- A rastreabilidade distingue equipamentos disponíveis, em uso e não devolvidos na data determinada (`docs/requisitos/requisitos-funcionais.md`, RF11).
- Os empréstimos devem registrar data, hora e responsável (`docs/casos-de-uso/casos-de-uso.md`, UC12).

**Critérios de aceitação**

Cenário: Consultar situação
Dado que existem EPIs cadastrados
Quando o Supervisor consultar a rastreabilidade
Então o sistema identifica os equipamentos disponíveis
E os equipamentos em posse de Colaboradores.

Cenário: Identificar devolução pendente
Dado que um empréstimo ativo ultrapassou a data determinada
Quando o Supervisor consultar a rastreabilidade
Então o sistema identifica o item como pendente
E informa qual Colaborador está em sua posse.

Cenário: Restringir acesso
Dado que um usuário não possui a permissão necessária
Quando tentar consultar a rastreabilidade
Então o sistema impede o acesso.

Cenário: Tempo da consulta
Dado que o sistema está em condições normais de uso
Quando o Supervisor realizar uma consulta simples
Então o resultado é apresentado em até 1 segundo.

**Dependências**

- Empréstimos da HU-07.
- Devoluções da HU-08.

**Fora do escopo desta história**

- Alertas de compra, cálculo preditivo e empréstimos para visitantes.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quem define a data de devolução e em qual momento?
- `[A CONFIRMAR]` O Gestor terá a mesma consulta do Supervisor?

### HU-10 - Registrar acidente

**História**

Como **Supervisor**, quero **registrar um acidente ocorrido no ambiente de trabalho**, para **formalizar a ocorrência e permitir seu acompanhamento e análise**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF13 |
| Caso de uso | UC09 e UC05 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF04, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O registro cria uma fonte confiável para acompanhar acidentes. Quando um Colaborador comunica ou vivencia o acidente, o Supervisor realiza o registro no sistema.

**Pré-condições**

- O Supervisor deve estar autenticado.
- O Colaborador envolvido deve estar cadastrado, quando aplicável.

**Regras de negócio**

- O Supervisor pode registrar acidentes e incidentes (`docs/casos-de-uso/casos-de-uso.md`, UC09).
- Setor e descrição são obrigatórios (`docs/casos-de-uso/casos-de-uso.md`, UC09).
- O registro deve receber protocolo (`docs/casos-de-uso/casos-de-uso.md`, UC09).
- Acidentes não podem ser excluídos fisicamente, somente arquivados (`docs/casos-de-uso/casos-de-uso.md`, UC05).
- Data, hora e usuário responsável devem ser registrados automaticamente (`docs/casos-de-uso/casos-de-uso.md`, UC05).

**Critérios de aceitação**

Cenário: Registrar acidente
Dado que o Supervisor está autenticado
Quando informar os dados documentados e confirmar
Então o sistema registra o acidente
E gera protocolo
E registra data, hora e usuário responsável.

Cenário: Impedir envio incompleto
Dado que setor ou descrição não foi informado
Quando o Supervisor tentar enviar o relato
Então o sistema bloqueia o registro
E indica os dados pendentes.

Cenário: Proteger dados sensíveis
Dado que o acidente contém dados sensíveis
Quando os dados forem armazenados e transmitidos
Então o armazenamento utiliza AES-256
E a transmissão utiliza HTTPS com TLS 1.3.

Cenário: Auditar registro
Dado que o acidente foi registrado
Quando a operação for concluída
Então o log imutável identifica usuário, data, hora e dados criados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de registro
Dado que o sistema está em condições normais de uso
Quando um acidente válido for confirmado
Então o sistema responde em até 3 segundos.

**Dependências**

- Cadastro do Colaborador envolvido, quando aplicável.

**Fora do escopo desta história**

- Anexos, investigação, plano de ação, CAT, envio offline e notificação automática.
- Consulta, atualização e arquivamento como objetivos independentes sem RF específico.

**Dúvidas para validação**

- `[A CONFIRMAR]` O Gestor também pode cadastrar acidentes, como sugere UC05?
- `[A CONFIRMAR]` Quais dados adicionais são obrigatórios?

### HU-11 - Registrar incidente

**História**

Como **Supervisor**, quero **registrar uma situação de risco ou incidente sem lesão**, para **formalizar a ocorrência e apoiar a prevenção de acidentes**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF16 |
| Caso de uso | UC09 e UC05 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF04, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O Supervisor registra situações que poderiam causar lesão, inclusive quando comunicadas por um Colaborador. O Colaborador não acessa o sistema.

**Pré-condições**

- O Supervisor deve estar autenticado.

**Regras de negócio**

- O Supervisor pode registrar incidentes (`docs/casos-de-uso/casos-de-uso.md`, UC09).
- Setor e descrição são obrigatórios (`docs/casos-de-uso/casos-de-uso.md`, UC09).
- O registro deve receber protocolo (`docs/casos-de-uso/casos-de-uso.md`, UC09).
- Data, hora e usuário responsável devem ser registrados automaticamente (`docs/casos-de-uso/casos-de-uso.md`, UC05).

**Critérios de aceitação**

Cenário: Registrar incidente
Dado que o Supervisor está autenticado
Quando informar setor e descrição
E confirmar o relato
Então o sistema registra o incidente
E gera protocolo
E registra data, hora e usuário responsável.

Cenário: Registrar relato comunicado por Colaborador
Dado que um Colaborador comunicou uma situação de risco ao Supervisor
Quando o Supervisor registrar o incidente
Então o sistema registra o Supervisor como usuário responsável
E permite relacionar o Colaborador envolvido, quando aplicável.

Cenário: Impedir envio incompleto
Dado que setor ou descrição não foi informado
Quando o Supervisor tentar enviar o relato
Então o sistema bloqueia o registro
E indica os dados pendentes.

Cenário: Proteger e auditar dados
Dado que o incidente foi registrado
Quando os dados forem armazenados e transmitidos
Então o armazenamento utiliza AES-256
E a transmissão utiliza HTTPS com TLS 1.3
E o log imutável permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de registro
Dado que o sistema está em condições normais de uso
Quando um incidente válido for confirmado
Então o sistema responde em até 3 segundos.

**Dependências**

- Cadastro do Colaborador, quando ele estiver relacionado à ocorrência.

**Fora do escopo desta história**

- Acesso do Colaborador ao sistema, anexos, envio offline, investigação, plano de ação e notificação automática.

**Dúvidas para validação**

- `[A CONFIRMAR]` O Gestor também pode registrar incidentes?
- `[A CONFIRMAR]` Quais dados adicionais são obrigatórios?

### HU-12 - Gerenciar cadastros de Colaboradores

**História**

Como **Supervisor**, quero **gerenciar os cadastros dos Colaboradores**, para **manter as pessoas necessárias aos registros operacionais identificadas no SafePlace**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF23 |
| Caso de uso | UC13 citado no MVP, mas não identificado |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

Colaboradores precisam estar cadastrados para serem relacionados a acidentes, incidentes e empréstimos. Eles não possuem conta, senha ou acesso ao sistema.

**Pré-condições**

- O Supervisor deve estar autenticado.

**Regras de negócio**

- O Supervisor gerencia os cadastros dos Colaboradores (`docs/requisitos/requisitos-funcionais.md`, RF23).
- O Colaborador não recebe credencial de acesso, conforme decisão validada pela equipe.
- O acesso à gestão deve respeitar o perfil Supervisor (`docs/requisitos/requisitos-nao-funcionais.md`, RNF03, ajustado pela decisão da equipe).

**Critérios de aceitação**

Cenário: Cadastrar Colaborador
Dado que o Supervisor está autenticado
Quando registrar um novo Colaborador com os dados aceitos
Então o sistema cria o cadastro
E não cria conta ou senha de acesso para o Colaborador.

Cenário: Restringir acesso
Dado que um usuário não possui permissão para gerenciar Colaboradores
Quando tentar realizar a operação
Então o sistema impede o acesso.

Cenário: Auditar cadastro
Dado que um Colaborador foi cadastrado
Quando a operação for concluída
Então o log imutável identifica usuário, data, hora e dados criados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de cadastro
Dado que o sistema está em condições normais de uso
Quando um cadastro válido for confirmado
Então o sistema responde em até 3 segundos.

**Dependências**

- Nenhuma dependência funcional adicional foi identificada.

**Fora do escopo desta história**

- Conta, senha ou acesso do Colaborador.
- Gestão de supervisores.
- Operações não detalhadas pelo UC13 ausente.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais operações fazem parte de "gerenciar"?
- `[A CONFIRMAR]` Quais dados são obrigatórios?
- `[A CONFIRMAR]` O UC13 será acrescentado?

### HU-13 - Gerenciar supervisores

**História**

Como **Gestor de Segurança**, quero **gerenciar os supervisores**, para **manter os responsáveis pelos fluxos operacionais devidamente registrados**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF23 |
| Caso de uso | UC13 citado no MVP, mas não identificado |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05, RNF09, RNF12, RNF14, RNF15 e RNF16 |
| Prioridade | Must have |
| Situação | MVP |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

Supervisores registram ocorrências e controlam empréstimos. O Gestor precisa manter esses usuários para que responsabilidades e permissões sejam aplicadas.

**Pré-condições**

- O Gestor de Segurança deve estar autenticado.

**Regras de negócio**

- O Gestor gerencia supervisores (`docs/requisitos/requisitos-funcionais.md`, RF23).
- O sistema gera automaticamente a senha do Supervisor (`docs/requisitos/requisitos-funcionais.md`, RF23).
- O acesso deve respeitar o perfil (`docs/requisitos/requisitos-nao-funcionais.md`, RNF03).

**Critérios de aceitação**

Cenário: Cadastrar Supervisor
Dado que o Gestor está autenticado
Quando registrar um novo Supervisor com os dados aceitos
Então o sistema cria o cadastro
E gera uma senha automaticamente.

Cenário: Restringir acesso
Dado que um usuário não possui permissão para gerenciar Supervisores
Quando tentar realizar a operação
Então o sistema impede o acesso.

Cenário: Auditar cadastro
Dado que um Supervisor foi cadastrado
Quando a operação for concluída
Então o log imutável identifica usuário, data, hora e dados criados
E permanece sujeito à retenção mínima de 5 anos.

Cenário: Tempo de cadastro
Dado que o sistema está em condições normais de uso
Quando um cadastro válido for confirmado
Então o sistema responde em até 3 segundos.

**Dependências**

- Nenhuma dependência funcional adicional foi identificada.

**Fora do escopo desta história**

- Gestão de Colaboradores, entrega da senha e operações não detalhadas pelo UC13 ausente.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais operações fazem parte de "gerenciar"?
- `[A CONFIRMAR]` Quais dados são obrigatórios?
- `[A CONFIRMAR]` Como a senha é entregue ao Supervisor?
- `[A CONFIRMAR]` O UC13 será acrescentado?

## 4. Histórias do backlog Should have

### HU-14 - Classificar acidente por ação do Colaborador

**História**

Como **Gestor de Segurança**, quero **identificar quando um acidente foi causado por ação individual de um Colaborador**, para **diferenciar fatores humanos de outras causas durante a análise da ocorrência**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF01 |
| Caso de uso | UC05 |
| Requisitos não funcionais relacionados | RNF03, RNF04, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A classificação permite distinguir acidentes ligados à conduta individual, como o uso incorreto ou a ausência de EPI, sem substituir uma investigação completa de causa raiz.

**Pré-condições**

- O Gestor deve estar autenticado.
- O acidente deve estar registrado.

**Regras de negócio**

- RF01 trata de acidentes causados individualmente pelo Colaborador, sem interferência de outros fatores (`docs/requisitos/requisitos-funcionais.md`, RF01).
- A investigação detalhada de causa raiz pertence ao RF15 (`docs/requisitos/requisitos-funcionais.md`, RF15).

**Critérios de aceitação**

Cenário: Classificar causa humana
Dado que existe acidente registrado
Quando o Gestor identificar ação individual do Colaborador como causa
Então o sistema registra essa classificação vinculada ao acidente.

Cenário: Restringir acesso
Dado que um usuário não possui a permissão necessária
Quando tentar classificar a causa
Então o sistema impede a operação.

**Dependências**

- Registro de acidente da HU-10.

**Fora do escopo desta história**

- Investigação completa, plano de ação e classificação por falha de EPI.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais evidências permitem atribuir a causa exclusivamente ao Colaborador?

### HU-15 - Classificar acidente por falha de EPI

**História**

Como **Gestor de Segurança**, quero **identificar quando um acidente foi causado por falha de EPI**, para **relacionar a ocorrência ao estado inadequado do equipamento**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF02 |
| Caso de uso | UC05 |
| Requisitos não funcionais relacionados | RNF03, RNF04, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A classificação conecta o acidente às condições do EPI e apoia a identificação de falhas de proteção sem inventar uma investigação além da prevista.

**Pré-condições**

- O Gestor deve estar autenticado.
- O acidente e o EPI devem estar registrados.

**Regras de negócio**

- RF02 trata de acidentes causados por EPI em estado inadequado (`docs/requisitos/requisitos-funcionais.md`, RF02).

**Critérios de aceitação**

Cenário: Classificar falha de EPI
Dado que existe acidente e EPI relacionado
Quando o Gestor identificar a falha do equipamento como causa
Então o sistema registra a classificação vinculada ao acidente e ao EPI.

Cenário: EPI não identificado
Dado que nenhum EPI foi relacionado à ocorrência
Quando o Gestor tentar confirmar essa classificação
Então o sistema impede o registro
E informa a necessidade de identificar o equipamento.

**Dependências**

- Registro de acidente.
- Cadastro do EPI.

**Fora do escopo desta história**

- Manutenção, descarte e investigação completa.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais dados comprovam que o estado do EPI causou o acidente?

### HU-16 - Classificar a periculosidade de uma tarefa

**História**

Como **Gestor de Segurança**, quero **classificar o nível de periculosidade de uma tarefa**, para **definir seu grau de risco e suas exigências normativas**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF06 |
| Caso de uso | UC11 |
| Requisitos não funcionais relacionados | RNF03, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A classificação permite representar o perigo associado a uma tarefa e disponibilizar diretrizes para o trabalho operacional.

**Pré-condições**

- O Gestor deve estar autenticado.
- A tarefa deve estar cadastrada.

**Regras de negócio**

- O grau deve usar os níveis Leve, Moderado, Grave ou Crítico (`docs/casos-de-uso/casos-de-uso.md`, UC11).
- Alterações devem registrar data, hora e responsável técnico (`docs/casos-de-uso/casos-de-uso.md`, UC11).
- A classificação deve seguir NR-1, NR-6 e NR-9 (`docs/casos-de-uso/casos-de-uso.md`, UC11).

**Critérios de aceitação**

Cenário: Classificar tarefa
Dado que o Gestor está autenticado
E a tarefa está cadastrada
Quando definir o grau e as exigências normativas
Então o sistema atualiza a matriz de periculosidade
E registra data, hora e responsável.

Cenário: Restringir alteração
Dado que um usuário não possui perfil Gestor
Quando tentar alterar a classificação
Então o sistema impede a operação.

**Dependências**

- Cadastro de tarefas `[A CONFIRMAR]`, pois não há RF específico para esse cadastro.

**Fora do escopo desta história**

- Consulta operacional, vínculo com EPI e alocação de Colaborador.

**Dúvidas para validação**

- `[A CONFIRMAR]` Qual requisito sustenta o cadastro das tarefas?

### HU-17 - Consultar periculosidade de tarefas

**História**

Como **Supervisor**, quero **consultar a periculosidade das tarefas**, para **orientar as atividades conforme o risco e as exigências documentadas**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF06 |
| Caso de uso | UC11 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF09, RNF11 e RNF16 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O Supervisor recebe a classificação definida pelo Gestor e a utiliza como orientação operacional.

**Pré-condições**

- O Supervisor deve estar autenticado.
- A tarefa deve possuir classificação.

**Regras de negócio**

- A classificação deve ser disponibilizada ao Supervisor (`docs/casos-de-uso/casos-de-uso.md`, UC11).

**Critérios de aceitação**

Cenário: Consultar classificação
Dado que a tarefa possui grau definido
Quando o Supervisor consultá-la
Então o sistema apresenta o grau e as exigências normativas.

Cenário: Tarefa sem classificação
Dado que a tarefa não possui grau atribuído
Quando o Supervisor consultá-la
Então o sistema informa que a classificação está pendente.

**Dependências**

- Classificação realizada pela HU-16.

**Fora do escopo desta história**

- Alteração da classificação e alocação de Colaboradores.

**Dúvidas para validação**

- Nenhuma dúvida adicional foi identificada.

### HU-18 - Definir plano de ação

**História**

Como **Gestor de Segurança**, quero **definir um plano de ação para uma ocorrência**, para **registrar medidas corretivas e preventivas que reduzam novas ocorrências**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF07 |
| Caso de uso | UC07 |
| Requisitos não funcionais relacionados | RNF03, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O plano transforma o conhecimento sobre uma ocorrência em medidas preventivas e corretivas acompanháveis.

**Pré-condições**

- O Gestor deve estar autenticado.
- O acidente ou incidente deve estar registrado.

**Regras de negócio**

- Toda ocorrência grave exige ao menos uma ação preventiva (`docs/casos-de-uso/casos-de-uso.md`, UC07).
- O plano pode possuir ações, prazos e responsáveis (`docs/casos-de-uso/casos-de-uso.md`, UC07).
- Prazos retroativos devem ser bloqueados (`docs/casos-de-uso/casos-de-uso.md`, UC07).

**Critérios de aceitação**

Cenário: Definir plano
Dado que existe ocorrência registrada
Quando o Gestor informar ações, prazos e responsáveis válidos
Então o sistema vincula o plano à ocorrência.

Cenário: Ocorrência grave sem ação preventiva
Dado que a ocorrência é grave
Quando o Gestor tentar salvar sem ação preventiva
Então o sistema bloqueia o registro.

Cenário: Prazo inválido
Dado que uma ação possui prazo retroativo
Quando o Gestor tentar salvar
Então o sistema bloqueia a operação
E solicita prazo futuro válido.

**Dependências**

- Acidente ou incidente registrado.

**Fora do escopo desta história**

- Investigação de causa raiz e notificações não sustentadas pelo RF07.

**Dúvidas para validação**

- `[A CONFIRMAR]` Os alertas configuráveis do UC07 fazem parte do RF07 ou exigem requisito próprio?

### HU-19 - Registrar inspeção de área de risco

**História**

Como **[ATOR A CONFIRMAR]**, quero **registrar inspeções periódicas das áreas de risco**, para **acompanhar sua conformidade com as normas aplicáveis**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF09 |
| Caso de uso | não identificado |
| Requisitos não funcionais relacionados | RNF03, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O histórico permite saber quando uma área foi inspecionada e se estava conforme ou não conforme. A fonte não define quem realiza o registro.

**Pré-condições**

- A área de risco deve estar cadastrada.

**Regras de negócio**

- A inspeção deve indicar conformidade ou não conformidade com as NRs aplicáveis (`docs/requisitos/requisitos-funcionais.md`, RF09).

**Critérios de aceitação**

Cenário: Registrar inspeção
Dado que a área está cadastrada
E o ator autorizado foi definido
Quando registrar o resultado da inspeção
Então o sistema adiciona o registro ao histórico da área
E identifica sua situação de conformidade.

Cenário: Área não encontrada
Dado que a área informada não existe
Quando o ator tentar registrar a inspeção
Então o sistema bloqueia a operação.

**Dependências**

- Cadastro de área da HU-05.

**Fora do escopo desta história**

- Criação de novas regras normativas ou definição silenciosa do responsável.

**Dúvidas para validação**

- `[A CONFIRMAR]` Gestor ou Supervisor registra a inspeção?
- `[A CONFIRMAR]` Quais dados formam o registro da inspeção?

### HU-20 - Vincular tarefas e EPIs

**História**

Como **Gestor de Segurança**, quero **vincular uma tarefa aos EPIs obrigatórios**, para **registrar a proteção necessária durante sua execução**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF12 |
| Caso de uso | UC08 |
| Requisitos não funcionais relacionados | RNF03, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O vínculo deixa explícitos os equipamentos exigidos para executar cada tarefa com proteção adequada.

**Pré-condições**

- O Gestor deve estar autenticado.
- A tarefa e os EPIs devem estar cadastrados.

**Regras de negócio**

- Somente o Gestor pode alterar a matriz de EPIs por tarefa (`docs/casos-de-uso/casos-de-uso.md`, UC08).
- EPI com CA vencido ou cancelado não pode ser vinculado (`docs/casos-de-uso/casos-de-uso.md`, UC08).

**Critérios de aceitação**

Cenário: Vincular EPIs
Dado que tarefa e EPIs estão cadastrados
Quando o Gestor selecionar os equipamentos e confirmar
Então o sistema registra a matriz de vinculação.

Cenário: EPI irregular
Dado que o EPI possui CA vencido ou cancelado
Quando o Gestor tentar vinculá-lo
Então o sistema bloqueia a vinculação.

**Dependências**

- Cadastro de tarefas `[A CONFIRMAR]`.
- Cadastro de EPIs.

**Fora do escopo desta história**

- Empréstimo, classificação da tarefa e sugestão automática de equipamentos.

**Dúvidas para validação**

- `[A CONFIRMAR]` Qual requisito sustenta o cadastro das tarefas?
- `[A CONFIRMAR]` A confirmação expressa de tarefa sem EPI é permitida ou o vínculo é obrigatório?

### HU-21 - Registrar descarte de EPI

**História**

Como **Gestor de Segurança**, quero **registrar o descarte definitivo de um EPI danificado ou vencido**, para **dar baixa correta no estoque e preservar o histórico do equipamento**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF14 |
| Caso de uso | UC01 e UC06 |
| Requisitos não funcionais relacionados | RNF03, RNF05, RNF09 e RNF10 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O descarte impede que equipamentos inadequados permaneçam disponíveis e registra a baixa definitiva no estoque.

**Pré-condições**

- O Gestor deve estar autenticado.
- O EPI ou lote deve estar cadastrado.

**Regras de negócio**

- O descarte exige identificação, quantidade e justificativa técnica (`docs/casos-de-uso/casos-de-uso.md`, UC06).
- A quantidade descartada não pode superar o saldo (`docs/casos-de-uso/casos-de-uso.md`, UC06).
- O sistema deve gerar termo de destinação ou descarte (`docs/casos-de-uso/casos-de-uso.md`, UC06).

**Critérios de aceitação**

Cenário: Registrar descarte
Dado que o EPI possui saldo suficiente
Quando o Gestor informar quantidade e justificativa
Então o sistema realiza a baixa definitiva
E gera o termo de destinação.

Cenário: Quantidade superior ao saldo
Dado que a quantidade de descarte supera o saldo
Quando o Gestor confirmar
Então o sistema bloqueia a operação.

**Dependências**

- Estoque e cadastro do EPI.

**Fora do escopo desta história**

- Cálculo preditivo de descarte e requisição automática de compra.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais dados obrigatórios compõem a justificativa e o termo de destinação?

### HU-22 - Gerenciar fornecedores e Certificados de Aprovação

**História**

Como **Gestor de Segurança**, quero **registrar fornecedores e o CA de cada EPI**, para **manter a origem e a conformidade dos equipamentos documentadas**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF20 |
| Caso de uso | UC01, UC06 e UC08, parcialmente |
| Requisitos não funcionais relacionados | RNF03, RNF05 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O cadastro fornece os dados necessários para verificar a validade local do CA nos fluxos de estoque, manutenção, vínculo e empréstimo.

**Pré-condições**

- O Gestor deve estar autenticado.
- O EPI deve estar cadastrado para receber o CA.

**Regras de negócio**

- O sistema deve registrar os fornecedores e o número do CA de cada equipamento (`docs/requisitos/requisitos-funcionais.md`, RF20).

**Critérios de aceitação**

Cenário: Registrar fornecedor e CA
Dado que o Gestor está autenticado
Quando registrar o fornecedor e relacionar um CA ao EPI
Então o sistema preserva os dados e o vínculo.

Cenário: Restringir acesso
Dado que um usuário não possui a permissão necessária
Quando tentar alterar fornecedor ou CA
Então o sistema impede a operação.

**Dependências**

- Cadastro do EPI.

**Fora do escopo desta história**

- Integração externa para consultar CA e alerta de vencimento, coberto pela HU-23.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais dados compõem o cadastro do fornecedor?
- `[A CONFIRMAR]` A validade será informada manualmente ou consultada externamente?

### HU-23 - Alertar sobre CA vencido

**História**

Como **Gestor de Segurança**, quero **ser alertado quando o CA cadastrado de um EPI estiver vencido**, para **impedir o uso de equipamento sem conformidade documentada**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF20 |
| Caso de uso | UC01, UC06 e UC08, parcialmente |
| Requisitos não funcionais relacionados | RNF01, RNF03 e RNF09 |
| Prioridade | Should have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O alerta utiliza a validade cadastrada internamente para indicar que o equipamento não deve seguir nos fluxos operacionais.

**Pré-condições**

- O EPI deve possuir CA e validade cadastrados.

**Regras de negócio**

- O sistema deve alertar quando a validade do CA cadastrado internamente estiver vencida (`docs/requisitos/requisitos-funcionais.md`, RF20).

**Critérios de aceitação**

Cenário: Identificar CA vencido
Dado que a validade cadastrada do CA já passou
Quando o Gestor consultar ou selecionar o EPI em fluxo protegido
Então o sistema apresenta alerta de CA vencido.

Cenário: CA válido
Dado que a validade do CA não venceu
Quando o Gestor consultar o EPI
Então o sistema não o classifica como vencido.

**Dependências**

- Dados cadastrados pela HU-22.

**Fora do escopo desta história**

- Consulta automática a base externa e substituição inteligente.

**Dúvidas para validação**

- `[A CONFIRMAR]` Em quais operações o alerta também deve causar bloqueio?

## 5. Histórias do backlog Could have

### HU-24 - Acompanhar ocorrências de comportamento de risco

**História**

Como **[ATOR A CONFIRMAR]**, quero **ser alertado quando um Colaborador ou visitante acumular ocorrências de comportamento de risco**, para **realizar a revisão obrigatória de conduta**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF08 |
| Caso de uso | não identificado |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF04 e RNF05 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O alerta transforma o histórico de ocorrências em apoio preventivo. A fonte não define se o beneficiário é Gestor ou Supervisor.

**Pré-condições**

- A pessoa deve possuir ocorrências registradas.
- O limite deve estar configurado.

**Regras de negócio**

- O limite de ocorrências deve ser configurável (`docs/requisitos/requisitos-funcionais.md`, RF08).
- Ao ultrapassar o limite, deve ser acionada revisão obrigatória de conduta (`docs/requisitos/requisitos-funcionais.md`, RF08).

**Critérios de aceitação**

Cenário: Ultrapassar limite
Dado que a pessoa atingiu quantidade superior ao limite configurado
Quando o histórico for analisado
Então o sistema gera alerta para o ator responsável
E indica a necessidade de revisão de conduta.

Cenário: Permanecer dentro do limite
Dado que a quantidade não ultrapassou o limite
Quando o histórico for analisado
Então o sistema não gera o alerta de excesso.

**Dependências**

- Histórico de ocorrências.
- Definição do limite.

**Fora do escopo desta história**

- Definição de punições ou novos canais de notificação.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quem recebe o alerta?
- `[A CONFIRMAR]` Quem configura o limite e como a revisão é registrada?

### HU-25 - Controlar certificações e treinamentos

**História**

Como **Gestor de Segurança**, quero **consultar certificações e treinamentos dos Colaboradores**, para **identificar vencimentos e impedir alocações sem habilitação válida**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF10 |
| Caso de uso | UC02 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05 e RNF09 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O controle evita que um Colaborador seja alocado em tarefa que exige certificação ou treinamento vencido.

**Pré-condições**

- O Gestor deve estar autenticado.
- Colaboradores e cursos devem estar cadastrados.

**Regras de negócio**

- A proximidade de vencimento deve ser alertada com 30 dias (`docs/casos-de-uso/casos-de-uso.md`, UC02).
- Certificação vencida deve bloquear alocação em tarefa de risco (`docs/requisitos/requisitos-funcionais.md`, RF10; UC02).

**Critérios de aceitação**

Cenário: Consultar situação
Dado que o Colaborador possui certificações e treinamentos
Quando o Gestor consultar seu cadastro
Então o sistema apresenta itens realizados, pendentes e vencidos.

Cenário: Alertar proximidade
Dado que uma certificação vence em até 30 dias
Quando o sistema analisar sua validade
Então destaca a necessidade de renovação.

Cenário: Bloquear alocação
Dado que a habilitação obrigatória está vencida
Quando houver tentativa de alocação na tarefa correspondente
Então o sistema bloqueia a operação.

**Dependências**

- Cadastro de Colaboradores, cursos, tarefas e certificações.

**Fora do escopo desta história**

- Acesso direto do Colaborador e criação de cursos não documentada.

**Dúvidas para validação**

- `[A CONFIRMAR]` O Supervisor possui somente consulta ou também altera os registros?
- `[A CONFIRMAR]` Qual requisito cobre o cadastro de cursos e tarefas?

### HU-26 - Registrar investigação de acidente

**História**

Como **Gestor de Segurança**, quero **registrar a investigação de causa raiz de um acidente**, para **documentar fatores determinantes além da descrição inicial da ocorrência**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF15 |
| Caso de uso | UC05 |
| Requisitos não funcionais relacionados | RNF03, RNF04, RNF05, RNF09 e RNF10 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A investigação aprofunda a análise e ajuda a identificar causas humanas, materiais, organizacionais e ambientais.

**Pré-condições**

- O Gestor deve estar autenticado.
- O acidente deve estar registrado.

**Regras de negócio**

- A investigação pode utilizar Árvore de Causas ou 5 Porquês (`docs/requisitos/requisitos-funcionais.md`, RF15; UC05).
- O laudo deve permanecer vinculado à ocorrência (`docs/casos-de-uso/casos-de-uso.md`, UC05).

**Critérios de aceitação**

Cenário: Registrar investigação
Dado que existe acidente registrado
Quando o Gestor selecionar a metodologia e informar os fatores
Então o sistema salva o laudo
E o vincula ao acidente.

Cenário: Acidente não encontrado
Dado que o acidente informado não existe
Quando o Gestor tentar iniciar a investigação
Então o sistema impede a operação.

**Dependências**

- Acidente da HU-10.

**Fora do escopo desta história**

- Criação de novas metodologias e plano de ação.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais dados são obrigatórios em cada metodologia?

### HU-27 - Consultar dashboard de segurança

**História**

Como **[ATOR A CONFIRMAR]**, quero **consultar indicadores de segurança em gráficos**, para **acompanhar acidentes, gravidade e conformidade dos EPIs em tempo real**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF17 |
| Caso de uso | não identificado |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF08, RNF11 e RNF16 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O dashboard consolida dados registrados para facilitar o acompanhamento da situação de segurança. A fonte não define quem o consulta.

**Pré-condições**

- Devem existir dados suficientes para calcular os indicadores.

**Regras de negócio**

- Devem ser exibidas taxas de frequência, gravidade e status de conformidade de EPIs em gráficos (`docs/requisitos/requisitos-funcionais.md`, RF17).

**Critérios de aceitação**

Cenário: Consultar indicadores
Dado que existem dados registrados
Quando o ator autorizado acessar o dashboard
Então o sistema apresenta os indicadores documentados em gráficos.

Cenário: Restringir acesso
Dado que um usuário não possui permissão
Quando tentar acessar o dashboard
Então o sistema impede o acesso.

**Dependências**

- Dados de acidentes e EPIs.

**Fora do escopo desta história**

- Novos indicadores não documentados e relatórios exportáveis.

**Dúvidas para validação**

- `[A CONFIRMAR]` Gestor, Supervisor ou ambos consultam o dashboard?
- `[A CONFIRMAR]` Como cada indicador é calculado?

### HU-28 - Emitir relatórios estatísticos

**História**

Como **[ATOR A CONFIRMAR]**, quero **emitir relatórios estatísticos periódicos**, para **analisar acidentes, treinamentos e áreas de maior risco fora da consulta operacional**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF18 |
| Caso de uso | não identificado |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF09, RNF10 e RNF11 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

Os relatórios consolidam informações mensais ou anuais em formatos reutilizáveis para análise.

**Pré-condições**

- Devem existir dados no período selecionado.

**Regras de negócio**

- Os relatórios podem ser mensais ou anuais (`docs/requisitos/requisitos-funcionais.md`, RF18).
- A exportação deve usar PDF ou CSV (`docs/requisitos/requisitos-funcionais.md`, RF18).

**Critérios de aceitação**

Cenário: Emitir relatório
Dado que existem dados no período
Quando o ator autorizado selecionar periodicidade e formato
Então o sistema gera o relatório em PDF ou CSV.

Cenário: Período sem dados
Dado que não existem dados no período
Quando o ator solicitar o relatório
Então o sistema informa a ausência de dados
E não apresenta informações inventadas.

**Dependências**

- Registros de acidentes, treinamentos e áreas.

**Fora do escopo desta história**

- Novos formatos, indicadores ou periodicidades.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quem pode emitir relatórios?
- `[A CONFIRMAR]` Quais informações compõem cada relatório?

### HU-29 - Gerar Comunicação de Acidente de Trabalho

**História**

Como **Gestor de Segurança**, quero **preencher automaticamente uma CAT com os dados já registrados**, para **reduzir a repetição de informações na geração da documentação legal**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF19 |
| Caso de uso | UC05, parcialmente |
| Requisitos não funcionais relacionados | RNF03, RNF04, RNF05, RNF09 e RNF10 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A CAT reaproveita dados do acidente e reduz preenchimento duplicado, preservando a necessidade de conformidade legal.

**Pré-condições**

- O Gestor deve estar autenticado.
- O acidente deve estar registrado com os dados necessários.

**Regras de negócio**

- Os dados da CAT devem ser preenchidos a partir das informações já registradas (`docs/requisitos/requisitos-funcionais.md`, RF19).
- O documento deve atender à legislação previdenciária aplicável (`docs/requisitos/requisitos-nao-funcionais.md`, RNF09).

**Critérios de aceitação**

Cenário: Gerar CAT
Dado que o acidente possui os dados necessários
Quando o Gestor solicitar a CAT
Então o sistema preenche o documento usando os registros existentes.

Cenário: Dados insuficientes
Dado que faltam dados exigidos
Quando o Gestor solicitar a CAT
Então o sistema não conclui a emissão
E identifica as informações pendentes.

**Dependências**

- Acidente da HU-10.

**Fora do escopo desta história**

- Campos legais não documentados, transmissão a órgãos externos e novas integrações.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais campos e validações legais formam a CAT?
- `[A CONFIRMAR]` O sistema apenas gera ou também transmite o documento?

### HU-30 - Gerenciar visitantes

**História**

Como **Supervisor ou Gestor de Segurança**, quero **registrar visitantes e os EPIs temporariamente em sua posse**, para **manter identificadas as pessoas externas e os equipamentos entregues a elas**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF22 |
| Caso de uso | UC12, parcialmente |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF04, RNF05 e RNF09 |
| Prioridade | Could have |
| Situação | backlog |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

O cadastro permite rastrear visitantes e os EPIs entregues temporariamente. As fontes não descrevem diferença de permissão entre Supervisor e Gestor, por isso os dois permanecem na mesma história.

**Pré-condições**

- Supervisor ou Gestor deve estar autenticado.
- Os EPIs devem estar cadastrados e disponíveis.

**Regras de negócio**

- O visitante deve possuir informações que permitam identificá-lo (`docs/requisitos/requisitos-funcionais.md`, RF22).
- O empréstimo temporário deve gerar termo de cautela vinculado ao visitante (`docs/casos-de-uso/casos-de-uso.md`, UC12).

**Critérios de aceitação**

Cenário: Registrar visitante e empréstimo
Dado que o ator autorizado está autenticado
E existem EPIs disponíveis
Quando registrar o visitante e confirmar a entrega
Então o sistema cria o cadastro temporário
E vincula os EPIs e o termo de cautela.

Cenário: EPI indisponível
Dado que o item não possui saldo
Quando o ator tentar confirmar a entrega
Então o sistema bloqueia o empréstimo.

**Dependências**

- Cadastro e estoque de EPIs.

**Fora do escopo desta história**

- Conta de acesso para visitante e campos não documentados.

**Dúvidas para validação**

- `[A CONFIRMAR]` Supervisor e Gestor possuem exatamente as mesmas operações?
- `[A CONFIRMAR]` Quais dados identificam obrigatoriamente o visitante?

## 6. Histórias fora da versão atual

O RF21 permanece válido, mas está classificado como `Won't have`. As histórias desta seção não fazem parte do MVP nem dos backlogs Should e Could da versão atual.

### HU-31 - Projetar o descarte de EPIs

**História**

Como **Gestor de Segurança**, quero **consultar a data estimada de descarte de cada EPI**, para **antecipar equipamentos que se aproximam do fim de seu ciclo de vida**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF21 |
| Caso de uso | UC10 |
| Requisitos não funcionais relacionados | RNF01, RNF03, RNF05 e RNF09 |
| Prioridade | Won't have |
| Situação | fora da versão atual |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A projeção considera frequência de uso e condições ambientais para estimar o descarte. Essa capacidade preditiva foi explicitamente adiada.

**Pré-condições**

- O histórico de uso, o estoque e as condições ambientais devem estar atualizados.

**Regras de negócio**

- A validade do CA prevalece sobre qualquer projeção (`docs/casos-de-uso/casos-de-uso.md`, UC10).
- O cálculo utiliza frequência de uso e condições do ambiente (`docs/requisitos/requisitos-funcionais.md`, RF21).

**Critérios de aceitação**

Cenário: Gerar projeção
Dado que os dados necessários estão disponíveis
Quando o Gestor solicitar a análise
Então o sistema calcula e apresenta a data estimada de descarte.

Cenário: CA vence antes da projeção
Dado que a validade do CA é anterior à data calculada
Quando o sistema gerar a projeção
Então utiliza a validade do CA como limite.

**Dependências**

- Histórico de uso, áreas de risco, estoque e dados do CA.

**Fora do escopo desta história**

- Entrega na versão atual e alertas de reposição, cobertos pela HU-32.

**Dúvidas para validação**

- `[A CONFIRMAR]` Quais fórmulas e multiplicadores serão utilizados?

### HU-32 - Antecipar a reposição de EPIs

**História**

Como **Gestor de Segurança**, quero **receber uma lista de reposição antes do esgotamento ou vencimento dos EPIs**, para **planejar compras sem interromper a disponibilidade dos equipamentos**.

**Rastreabilidade**

| Elemento | Referência |
| --- | --- |
| Requisito funcional | RF21 |
| Caso de uso | UC10 |
| Requisitos não funcionais relacionados | RNF01, RNF03 e RNF05 |
| Prioridade | Won't have |
| Situação | fora da versão atual |
| Fonte principal | `docs/requisitos/requisitos-funcionais.md` |

**Contexto e valor**

A lista utiliza as projeções e os níveis de estoque para antecipar a necessidade de compra. Ela não deve ser confundida com o controle básico de estoque do MVP.

**Pré-condições**

- Deve existir projeção válida e informação de estoque atual e mínimo.

**Regras de negócio**

- O sistema deve gerar alertas antes do esgotamento ou vencimento do CA (`docs/requisitos/requisitos-funcionais.md`, RF21).
- UC10 prevê lista prioritária e requisição automática de compra (`docs/casos-de-uso/casos-de-uso.md`, UC10).

**Critérios de aceitação**

Cenário: Gerar lista de reposição
Dado que a projeção indica necessidade futura
Quando o sistema comparar projeção e estoque
Então apresenta lista de reposição prioritária.

Cenário: Estoque suficiente
Dado que a projeção não indica esgotamento ou vencimento no período
Quando a análise for executada
Então o sistema não inclui o item na lista prioritária.

**Dependências**

- HU-31.
- Controle de estoque e dados do CA.

**Fora do escopo desta história**

- Entrega na versão atual.

**Dúvidas para validação**

- `[A CONFIRMAR]` A requisição automática apenas cria um rascunho ou inicia uma compra?

## 7. Requisitos não funcionais

### RNF01 - Tempo de resposta

**Restrição de qualidade**

O sistema deve responder às interações em até 3 segundos em condições normais e em até 1 segundo para consultas simples de estoque e cadastros.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias interativas, especialmente HU-01, HU-02, HU-03, HU-04, HU-05, HU-06, HU-07, HU-08, HU-09, HU-10, HU-11, HU-12 e HU-13.

**Critérios de aceitação mensuráveis**

- Consultas simples de estoque e cadastros concluem em até 1 segundo.
- As demais interações concluem em até 3 segundos sob condições normais.

**Evidência esperada**

Resultados registrados de testes dos tempos dos fluxos principais, com identificação do fluxo e da condição utilizada.

**Lacunas**

- `[A CONFIRMAR]` O que caracteriza formalmente uma condição normal de uso.

### RNF02 - Capacidade de usuários simultâneos

**Restrição de qualidade**

O sistema deve suportar ao menos 20 usuários simultâneos sem degradação perceptível e permitir crescimento conforme a organização.

**Prioridade**

Should have, mantido no backlog.

**Histórias afetadas**

Todas as histórias executadas por Gestor e Supervisor.

**Critérios de aceitação mensuráveis**

- Ao menos 20 usuários conseguem executar os fluxos definidos simultaneamente.
- Durante a execução, os limites do RNF01 continuam sendo avaliados.

**Evidência esperada**

Resultado de teste com 20 usuários simultâneos e os tempos observados.

**Lacunas**

- “Degradação perceptível” não possui limite numérico próprio além dos tempos do RNF01.
- O comportamento esperado acima de 20 usuários não está definido.

### RNF03 - Controle de acesso por perfil

**Restrição de qualidade**

O sistema deve restringir funcionalidades conforme o perfil. Conforme decisão da equipe, os usuários autenticados são Gestor de Segurança e Supervisor. O Colaborador permanece cadastrado apenas como pessoa sem acesso.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias, conforme o ator indicado em cada uma.

**Critérios de aceitação mensuráveis**

- O Gestor acessa somente as operações atribuídas ao seu perfil.
- O Supervisor acessa somente as operações atribuídas ao seu perfil.
- O sistema não cria credencial de acesso para o Colaborador.
- Uma tentativa sem permissão é bloqueada.

**Evidência esperada**

Matriz de permissões e testes positivos e negativos para Gestor e Supervisor.

**Lacunas**

- RF16, RF23, RNF03 e UC09 precisam ser atualizados para refletir a decisão sobre o Colaborador.
- Algumas permissões do Gestor em UC05 permanecem `[A CONFIRMAR]`.

### RNF04 - Criptografia de dados sensíveis

**Restrição de qualidade**

Dados sensíveis, incluindo atestados de saúde, CATs, históricos de acidentes e mídias, devem ser armazenados com AES-256 e transmitidos por HTTPS com TLS 1.3.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

HU-10, HU-11, HU-14, HU-15, HU-24, HU-26, HU-29 e HU-30, além de futuras histórias de mídias e documentos de saúde.

**Critérios de aceitação mensuráveis**

- Dados sensíveis armazenados utilizam AES-256.
- Toda transmissão desses dados utiliza HTTPS com TLS 1.3.
- Tentativas de conexão sem o protocolo exigido não expõem os dados sensíveis.

**Evidência esperada**

Configuração e resultado verificável que demonstrem AES-256 no armazenamento e TLS 1.3 na transmissão.

**Lacunas**

- A lista completa de campos classificados como sensíveis ainda não foi documentada.

### RNF05 - Rastreabilidade de ações

**Restrição de qualidade**

O sistema deve manter log imutável de criação, edição e exclusão, identificando usuário, data, hora e dado alterado, pelo período mínimo de 5 anos.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias que criam ou alteram registros, especialmente HU-02, HU-04, HU-05, HU-07, HU-08, HU-10, HU-11, HU-12 e HU-13.

**Critérios de aceitação mensuráveis**

- Cada operação de criação, edição ou exclusão gera entrada de log.
- Cada entrada identifica usuário, data, hora e dado alterado.
- O log não pode ser alterado pela operação comum do usuário.
- A política de retenção mínima é de 5 anos.

**Evidência esperada**

Registros de auditoria gerados pelos fluxos principais e documentação da imutabilidade e retenção.

**Lacunas**

- Não está definido quem pode consultar os logs.
- O tratamento de arquivamento não está completamente diferenciado de exclusão.

### RNF06 - Disponibilidade mínima

**Restrição de qualidade**

O sistema deve garantir 99,5% de disponibilidade, com manutenção planejada de no máximo 4 horas mensais, preferencialmente fora do expediente.

**Prioridade**

Should have, mantido no backlog.

**Histórias afetadas**

Todas as histórias que dependem do sistema disponível.

**Critérios de aceitação mensuráveis**

- A disponibilidade medida no período é de pelo menos 99,5%.
- A manutenção planejada não ultrapassa 4 horas no mês.

**Evidência esperada**

Registro de disponibilidade e das janelas de manutenção do período avaliado.

**Lacunas**

- O período usado para calcular os 99,5% não foi definido.
- “Fora do expediente” depende do horário da organização.

### RNF07 - Backup e recuperação de dados

**Restrição de qualidade**

O sistema deve realizar backup automático diário, com RPO de até 24 horas e RTO de até 4 horas em caso de falha crítica.

**Prioridade**

Could have, mantido no backlog.

**Histórias afetadas**

Todas as histórias que armazenam dados.

**Critérios de aceitação mensuráveis**

- Um backup é produzido automaticamente a cada dia.
- A perda máxima de dados recuperáveis é de 24 horas.
- A restauração após falha crítica conclui em até 4 horas.

**Evidência esperada**

Registros dos backups e resultado de uma restauração controlada com medição de RPO e RTO.

**Lacunas**

- Local, retenção e proteção dos backups não foram definidos.

### RNF08 - Funcionamento offline parcial

**Restrição de qualidade**

O sistema deve permitir consulta offline de estatísticas, indicadores e relatórios previamente sincronizados por PWA e cache local. Atualizações em tempo real e novos relatórios ficam suspensos sem conexão.

**Prioridade**

Could have, mantido no backlog.

**Histórias afetadas**

HU-27 e HU-28. O envio offline descrito no UC09 não é sustentado pelo texto do RNF08.

**Critérios de aceitação mensuráveis**

- Dados previamente sincronizados permanecem consultáveis sem conexão.
- Sem conexão, novos relatórios e atualizações em tempo real não são executados.
- Após restabelecimento, o sistema volta a utilizar os dados do servidor.

**Evidência esperada**

Demonstração das consultas antes, durante e depois da interrupção de conexão.

**Lacunas**

- Não está definido quais estatísticas, indicadores e relatórios são considerados principais.
- O relato offline do UC09 precisa ser removido ou receber requisito próprio.

### RNF09 - Conformidade normativa

**Restrição de qualidade**

O sistema deve aderir às normas aplicáveis, especialmente NR-1, NR-6, NR-9, LGPD, Lei 13.709/2018, e legislação previdenciária aplicável à CAT.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias de ocorrências, EPIs, áreas, pessoas e documentos legais.

**Critérios de aceitação mensuráveis**

- Cada regra normativa implementada possui referência à norma que a sustenta.
- Dados pessoais e de saúde possuem tratamento compatível com a LGPD.
- A geração de CAT atende às regras previdenciárias identificadas.

**Evidência esperada**

Matriz que relacione normas, regras implementadas, histórias e evidências de verificação.

**Lacunas**

- As obrigações concretas de cada norma ainda não foram decompostas em regras testáveis.
- A expressão “normas aplicáveis” exige validação especializada.

### RNF10 - Integridade dos documentos gerados

**Restrição de qualidade**

Relatórios, CATs e laudos devem conter assinatura digital ou hash de integridade para garantir autenticidade perante órgãos fiscalizadores.

**Prioridade**

Should have, mantido no backlog.

**Histórias afetadas**

HU-21, HU-26, HU-28, HU-29 e HU-31 quando gerarem documentos.

**Critérios de aceitação mensuráveis**

- Cada documento gerado contém assinatura digital ou hash.
- Uma alteração posterior no conteúdo é detectável pela verificação de integridade.

**Evidência esperada**

Documento gerado e resultado da verificação antes e depois de uma alteração controlada.

**Lacunas**

- Não foi escolhido entre assinatura digital e hash.
- O padrão de assinatura ou algoritmo de hash não foi definido.

### RNF11 - Interface responsiva e acessível

**Restrição de qualidade**

A interface deve funcionar em desktop, tablet e mobile e seguir WCAG 2.1 nível AA para pessoas com deficiências visuais ou motoras.

**Prioridade**

Should have, mantido no backlog.

**Histórias afetadas**

Todas as histórias com interface de usuário.

**Critérios de aceitação mensuráveis**

- Os fluxos funcionam nos três tipos de dispositivo documentados.
- A interface atende aos critérios aplicáveis da WCAG 2.1 nível AA.

**Evidência esperada**

Resultados de inspeção de acessibilidade e execução dos fluxos em desktop, tablet e mobile.

**Lacunas**

- Não foram definidos tamanhos de tela representativos para cada categoria.

### RNF12 - Facilidade de aprendizado

**Restrição de qualidade**

Uma pessoa com conhecimento básico de informática deve realizar as tarefas principais após no máximo 2 horas de treinamento.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Principalmente HU-01, HU-03, HU-07, HU-08, HU-10, HU-11, HU-12 e HU-13, além de HU-28 quando for entregue.

**Critérios de aceitação mensuráveis**

- Participantes com o perfil definido concluem as tarefas principais após treinamento de no máximo 2 horas.
- A conclusão é verificada sem suporte da equipe durante a tarefa avaliada.

**Evidência esperada**

Roteiro, duração do treinamento e resultados da execução das tarefas pelo público esperado.

**Lacunas**

- A quantidade de participantes e o critério de sucesso não foram definidos.

### RNF13 - Suporte multilíngue

**Restrição de qualidade**

A interface deve usar português do Brasil como padrão e permitir expansão para espanhol e inglês sem refatoração do código-fonte.

**Prioridade**

Could have, mantido no backlog.

**Histórias afetadas**

Todas as histórias com textos de interface.

**Critérios de aceitação mensuráveis**

- A interface é apresentada em português do Brasil por padrão.
- Espanhol e inglês podem ser acrescentados sem modificar a lógica das regras de negócio.

**Evidência esperada**

Estrutura de internacionalização demonstrada com inclusão controlada de texto em idioma adicional.

**Lacunas**

- Não está definido quando espanhol e inglês serão efetivamente traduzidos.

### RNF14 - Desacoplamento e extensibilidade

**Restrição de qualidade**

As regras centrais devem ser independentes de infraestrutura, banco de dados, interface e serviços externos. Integrações devem usar contratos de interface padronizados, de forma plugável e sem modificar o núcleo.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias, pois define a organização interna usada para implementá-las.

**Critérios de aceitação mensuráveis**

- O núcleo não depende diretamente de interface, banco de dados ou serviço externo.
- Adaptadores se conectam ao núcleo por portas ou contratos definidos.
- A substituição de um adaptador não exige alteração das regras centrais correspondentes.

**Evidência esperada**

Código e diagramas que mostrem núcleo, portas, adaptadores e direção das dependências.

**Lacunas**

- A conformidade depende da implementação futura, não apenas do desenho arquitetural.

### RNF15 - Documentação técnica

**Restrição de qualidade**

O sistema deve possuir documentação atualizada de API, modelo de dados e arquitetura, permitindo que um desenvolvedor externo realize manutenção sem apoio da equipe original em até 5 dias úteis.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias e entregas técnicas.

**Critérios de aceitação mensuráveis**

- API, modelo de dados e arquitetura estão documentados e atualizados.
- Um desenvolvedor externo consegue compreender e realizar a manutenção proposta em até 5 dias úteis sem suporte da equipe original.

**Evidência esperada**

Documentação versionada e registro de uma avaliação realizada por pessoa externa à equipe original.

**Lacunas**

- A tarefa de manutenção usada na avaliação não foi definida.
- API e modelo de dados dependem da implementação.

### RNF16 - Compatibilidade de navegadores e sistemas operacionais

**Restrição de qualidade**

O sistema web deve funcionar nas duas versões mais recentes de Chrome, Firefox, Edge e Safari, e poder ser implantado em Linux e Windows Server.

**Prioridade**

Must have, incluído no MVP.

**Histórias afetadas**

Todas as histórias web e a implantação do sistema.

**Critérios de aceitação mensuráveis**

- Os fluxos principais funcionam nas duas versões mais recentes dos quatro navegadores.
- A aplicação pode ser implantada em Linux e Windows Server.

**Evidência esperada**

Matriz de compatibilidade com navegador, versão, sistema operacional e resultado dos fluxos avaliados.

**Lacunas**

- As versões concretas mudam ao longo do tempo e devem ser registradas na data do teste.
- As versões de Linux e Windows Server não foram especificadas.

### RNF17 - Integração via API

**Restrição de qualidade**

O sistema deve expor API RESTful documentada em OpenAPI 3.0, usando OAuth 2.0 para integração com sistemas externos, como ERP e recursos humanos.

**Prioridade**

Could have, mantido no backlog.

**Histórias afetadas**

Grupos de histórias cujos dados forem futuramente disponibilizados a sistemas externos.

**Critérios de aceitação mensuráveis**

- A API segue o estilo RESTful documentado.
- A especificação usa OpenAPI 3.0.
- O acesso externo exige OAuth 2.0.

**Evidência esperada**

Especificação OpenAPI 3.0 e demonstração de acesso autorizado e rejeição de acesso sem OAuth 2.0.

**Lacunas**

- Não foram definidos sistemas, dados ou operações que integrarão primeiro.
- Não existe caso de uso de integração.

## 8. Matriz final de rastreabilidade

| História | Ator | RF | RNFs principais | UC | Prioridade | Situação |
| --- | --- | --- | --- | --- | --- | --- |
| HU-01 | Gestor de Segurança | RF03 | RNF01, RNF03, RNF09, RNF12, RNF14, RNF15, RNF16 | UC06 | Must have | MVP |
| HU-02 | Gestor de Segurança | RF03 | RNF01, RNF03, RNF05, RNF09 | UC06 | Must have | MVP |
| HU-03 | Gestor de Segurança | RF04 | RNF01, RNF03, RNF09, RNF12 | UC01 | Must have | MVP |
| HU-04 | Gestor de Segurança | RF04 | RNF01, RNF03, RNF05, RNF09 | UC01 | Must have | MVP |
| HU-05 | Gestor de Segurança | RF05 | RNF01, RNF03, RNF05, RNF09 | UC03 | Must have | MVP |
| HU-06 | Supervisor | RF05 | RNF01, RNF03, RNF09, RNF12 | UC03 | Must have | MVP |
| HU-07 | Supervisor | RF11 | RNF01, RNF03, RNF05, RNF09 | UC12 | Must have | MVP |
| HU-08 | Supervisor | RF11 | RNF01, RNF03, RNF05, RNF09 | UC12 | Must have | MVP |
| HU-09 | Supervisor | RF11 | RNF01, RNF03, RNF09 | UC12 | Must have | MVP |
| HU-10 | Supervisor | RF13 | RNF01, RNF03, RNF04, RNF05, RNF09, RNF12 | UC09, UC05 | Must have | MVP |
| HU-11 | Supervisor | RF16 | RNF01, RNF03, RNF04, RNF05, RNF09, RNF12 | UC09, UC05 | Must have | MVP |
| HU-12 | Supervisor | RF23 | RNF01, RNF03, RNF05, RNF09 | UC13 ausente | Must have | MVP |
| HU-13 | Gestor de Segurança | RF23 | RNF01, RNF03, RNF05, RNF09 | UC13 ausente | Must have | MVP |
| HU-14 | Gestor de Segurança | RF01 | RNF03, RNF04, RNF05, RNF09 | UC05 | Should have | backlog |
| HU-15 | Gestor de Segurança | RF02 | RNF03, RNF04, RNF05, RNF09 | UC05 | Should have | backlog |
| HU-16 | Gestor de Segurança | RF06 | RNF03, RNF05, RNF09 | UC11 | Should have | backlog |
| HU-17 | Supervisor | RF06 | RNF01, RNF03, RNF09, RNF11 | UC11 | Should have | backlog |
| HU-18 | Gestor de Segurança | RF07 | RNF03, RNF05, RNF09 | UC07 | Should have | backlog |
| HU-19 | [ATOR A CONFIRMAR] | RF09 | RNF03, RNF05, RNF09 | não identificado | Should have | backlog |
| HU-20 | Gestor de Segurança | RF12 | RNF03, RNF05, RNF09 | UC08 | Should have | backlog |
| HU-21 | Gestor de Segurança | RF14 | RNF03, RNF05, RNF09, RNF10 | UC01, UC06 | Should have | backlog |
| HU-22 | Gestor de Segurança | RF20 | RNF03, RNF05, RNF09 | parcial | Should have | backlog |
| HU-23 | Gestor de Segurança | RF20 | RNF01, RNF03, RNF09 | parcial | Should have | backlog |
| HU-24 | [ATOR A CONFIRMAR] | RF08 | RNF01, RNF03, RNF04, RNF05 | não identificado | Could have | backlog |
| HU-25 | Gestor de Segurança | RF10 | RNF01, RNF03, RNF05, RNF09 | UC02 | Could have | backlog |
| HU-26 | Gestor de Segurança | RF15 | RNF03, RNF04, RNF05, RNF09, RNF10 | UC05 | Could have | backlog |
| HU-27 | [ATOR A CONFIRMAR] | RF17 | RNF01, RNF03, RNF08, RNF11, RNF16 | não identificado | Could have | backlog |
| HU-28 | [ATOR A CONFIRMAR] | RF18 | RNF01, RNF03, RNF09, RNF10, RNF11 | não identificado | Could have | backlog |
| HU-29 | Gestor de Segurança | RF19 | RNF03, RNF04, RNF05, RNF09, RNF10 | UC05 parcial | Could have | backlog |
| HU-30 | Supervisor ou Gestor de Segurança | RF22 | RNF01, RNF03, RNF04, RNF05, RNF09 | UC12 parcial | Could have | backlog |
| HU-31 | Gestor de Segurança | RF21 | RNF01, RNF03, RNF05, RNF09 | UC10 | Won't have | fora da versão atual |
| HU-32 | Gestor de Segurança | RF21 | RNF01, RNF03, RNF05 | UC10 | Won't have | fora da versão atual |

## 9. Perguntas pendentes para a equipe

### Atores e permissões

1. O Gestor de Segurança pode registrar acidentes e incidentes, ou somente consultar e realizar a triagem?
2. Quem registra as inspeções periódicas do RF09?
3. Quem consulta dashboards e emite relatórios estatísticos?
4. Supervisor e Gestor possuem as mesmas operações na gestão de visitantes?

### Cadastros e dependências

5. Quais operações formam a gestão de Colaboradores e Supervisores?
6. Quais dados são obrigatórios nesses cadastros?
7. Como a senha gerada é entregue ao Supervisor?
8. Qual requisito sustenta o cadastro de tarefas e cursos?
9. O UC13 será acrescentado ao documento de casos de uso?

### EPIs e conformidade

10. Os dados locais de CA estarão disponíveis no MVP?
11. O vínculo obrigatório entre área de risco e EPI pertence ao RF05 no MVP?
12. Como o estoque mínimo e a data prevista de devolução são definidos?
13. Em quais operações um CA vencido gera somente alerta e em quais gera bloqueio?

### Ocorrências e documentos

14. Quais dados adicionais são obrigatórios no registro de acidentes e incidentes?
15. A notificação automática do UC09 deve receber um RF e uma prioridade próprios?
16. Consulta, atualização e arquivamento de ocorrências precisam de um novo RF?
17. Quais campos, validações e forma de transmissão são exigidos para a CAT?

### Atualizações documentais necessárias

18. RF16, RF23, RNF03 e UC09 serão atualizados para registrar que o Colaborador não acessa o sistema?
19. O cenário offline de relato do UC09 será removido ou receberá um requisito próprio?
20. Os alertas configuráveis do UC07 pertencem ao RF07 ou exigem requisito próprio?
