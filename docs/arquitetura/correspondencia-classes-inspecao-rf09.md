# Correspondência entre Diagrama de Classes e RF09 (Inspecao e ItemVerificado)

## 1. Visão Geral e Contexto

Este documento registra o contrato do histórico de inspeções periódicas das áreas de risco. Ele concilia as classes `Inspecao` e `ItemVerificado` do **Diagrama de Classes** com o **RF09** e a **US09**, atendendo à [issue #134](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/134).

- **Situação**: proposta de contrato submetida à aprovação do grupo na issue #134. RF09 é "Deveria ter" na MoSCoW e está fora do MVP. A interface ([#136](https://github.com/yIuriy/rp-iv-grupo03-safeplace/issues/136)) deve seguir as decisões aprovadas. As pendências da seção 4 não foram decididas e não devem ser supridas por suposição.
- **Fontes**: [Diagrama de classes](../diagramas/classes/Diagrama%20de%20Classes%20-%20SafePlace.png), [RF09](../requisitos/requisitos-funcionais.md#rf09-o-sistema-deve-registrar-o-histórico-de-inspeções-periódicas-das-áreas-de-risco), [US09](../historias-de-usuario.md#us09--registrar-inspeções-de-áreas-de-risco), [MoSCoW](../requisitos/priorizacao-moscow.md) e [RNF03](../requisitos/requisitos-nao-funcionais.md).
- **Sem caso de uso nem diagrama de sequência**: RF09 não tem UC nem DS próprios. Atores, operações e mensagens abaixo vêm da US09 e das associações do PNG.
- **Implementação de referência**: domínio em `backend/src/main/java/br/edu/safeplace/backend/domain/inspecao/` (`Inspecao`, `ItemVerificado`); caso de uso `InspecaoUseCase`; persistência `InspecaoJpaAdapter` com a migração `V24__criar_inspecoes.sql`; API `/api/areas-risco/{id}/inspecoes`; tela na aba "Inspeções" de `/areas-risco` (`frontend/src/features/areas-risco/InspecoesPainel.tsx`, issue #136).

### 1.1. Modelo de referência

O PNG define:

- **`Inspecao`**: atributo `dataInspecao : Date`. Nenhuma operação declarada.
- **`ItemVerificado`**: atributos `descricao : String`, `conformidade : boolean` e `observacoes : String`. Nenhuma operação declarada.
- **Associações**:

| Associação no PNG                                | Leitura                                                                                    |
| ------------------------------------------------ | ------------------------------------------------------------------------------------------ |
| `GestorDeSeguranca` 1 para `Inspecao` 0..*       | Cada inspeção tem exatamente um Gestor de Segurança; um Gestor tem zero ou mais inspeções. |
| `AreaRisco` (não indicada) para `Inspecao` 0..*  | Uma área tem zero ou mais inspeções. A multiplicidade do lado da área não está indicada.   |
| `Inspecao` 1..* para `ItemVerificado` 0..*       | Uma inspeção tem zero ou mais itens; um item se liga a uma ou mais inspeções.              |
| `Inspecao` (não indicada) para `MidiaAnexo` 0..* | Anexos de mídia, ligados a UC04 (backlog).                                                 |

### 1.2. Dicionário da US09

| Atributo da US09                                          | Restrição na US09                |
| --------------------------------------------------------- | -------------------------------- |
| Área de risco                                             | Obrigatório                      |
| Data da inspeção                                          | Obrigatório; formato a confirmar |
| Norma avaliada                                            | Obrigatório                      |
| Resultado da avaliação (conformidade ou não conformidade) | Obrigatório                      |
| Observações da inspeção                                   | A confirmar                      |
| Responsável pelo registro                                 | Automático (usuário autenticado) |

---

## 2. Decisões de Modelo Propostas

### 2.1. Ator e permissões

- **Proposta**: o Gestor de Segurança registra e consulta inspeções.
- **Fundamento**: a US09 é escrita para o Gestor ("Como Gestor de Segurança, quero registrar e consultar") e sua regra 4 o propõe como ator. O PNG liga cada `Inspecao` a exatamente um `GestorDeSeguranca` (multiplicidade 1).
- **Supervisor**: sem acesso até decisão do grupo (seção 4). A US09 não cita o Supervisor, e o UC03, que dá ao Supervisor a consulta das áreas, não trata de inspeções.
- **Regra de acesso**: `POST` e `GET` em `/api/areas-risco/{id}/inspecoes` são exclusivos do Gestor no `SecurityConfig` (RNF03). Os demais perfis recebem 403.

### 2.2. Correspondência entre US09, modelo e persistência

| US09                      | Modelo (PNG)                                     | Persistência (V24)                                                                                            |
| ------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------------------------------------------- |
| Área de risco             | Associação `AreaRisco` para `Inspecao`           | `inspecoes.area_risco_id`, obrigatório, chave estrangeira para `areas_risco`                                  |
| Data da inspeção          | `Inspecao.dataInspecao : Date`                   | `inspecoes.data_inspecao` (`DATE`), obrigatório                                                               |
| Responsável pelo registro | Associação `GestorDeSeguranca` 1 para `Inspecao` | `inspecoes.gestor_id`, obrigatório, chave estrangeira para `usuarios`, preenchido com o Gestor autenticado    |
| Norma avaliada            | `ItemVerificado.descricao : String`              | `itens_verificados.descricao` (`TEXT`), obrigatório                                                           |
| Resultado da avaliação    | `ItemVerificado.conformidade : boolean`          | `itens_verificados.conformidade` (`BOOLEAN`), obrigatório; `true` é conformidade e `false` é não conformidade |
| Observações da inspeção   | `ItemVerificado.observacoes : String`            | `itens_verificados.observacoes` (`TEXT`), opcional                                                            |

- **Norma avaliada como item**: a US09 registra uma norma e um resultado por avaliação, e o PNG registra uma `descricao` e uma `conformidade` por `ItemVerificado`. Cada item verificado é, portanto, uma referência avaliada com seu resultado e suas observações. Uma inspeção pode avaliar mais de uma referência.
- **Data**: é a data em que a inspeção foi realizada (US09), por isso não pode ser posterior ao dia do registro. O formato de entrada da API é `AAAA-MM-DD`; a interface exibe `DD/MM/AAAA`.
- **Observações**: opcionais, porque a US09 marca a restrição como "a confirmar". O tamanho também não é limitado na coluna.

### 2.3. Multiplicidades divergentes

| Ponto              | PNG  | Proposta     | Motivo                                                                                                                                                                    |
| ------------------ | ---- | ------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Itens por inspeção | 0..* | Ao menos 1   | O critério 1 da US09 exige referência e resultado em todo registro. Uma inspeção sem item não teria resultado.                                                            |
| Inspeções por item | 1..* | Exatamente 1 | `conformidade` e `observacoes` são o resultado de uma inspeção. Um item compartilhado entre inspeções mudaria o resultado das anteriores, contrariando a regra 3 da US09. |

As duas divergências devem ser levadas ao PNG após a aprovação. Esta proposta não altera o diagrama.

### 2.4. Histórico

- Cada inspeção registrada entra no histórico da sua área e não é alterada nem excluída depois (US09, regra 3 e critério 3). A API não oferece `PUT`, `PATCH` nem `DELETE` de inspeção.
- A consulta devolve as inspeções da área da data mais recente para a mais antiga; inspeções do mesmo dia seguem a ordem inversa de registro.
- Uma não conformidade aparece com essa identificação e com as observações informadas (US09, critério 4).

### 2.5. Operações e contrato da API

O PNG não declara operações em `Inspecao`. As operações abaixo atendem aos critérios 1 e 2 da US09 e ficam sob a rota existente de áreas, sem criar `/api/areas-de-risco` nem rota paralela:

| Operação                                   | Endpoint                               | Respostas                                                                                                                                              |
| ------------------------------------------ | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Registrar inspeção de uma área             | `POST /api/areas-risco/{id}/inspecoes` | 201; 400 para data ausente ou futura, nenhum item, item sem descrição ou sem resultado; 403 para perfil diferente de Gestor; 404 para área inexistente |
| Consultar o histórico de inspeções da área | `GET /api/areas-risco/{id}/inspecoes`  | 200 (lista vazia quando não há inspeção); 403; 404                                                                                                     |

Corpo do registro:

```json
{
  "dataInspecao": "2026-10-01",
  "itens": [
    {
      "descricao": "NR-12, item 12.38: proteções fixas das máquinas",
      "conformidade": true
    },
    {
      "descricao": "NR-26: sinalização de segurança",
      "conformidade": false,
      "observacoes": "Faixa de circulação apagada"
    }
  ]
}
```

### 2.6. O que esta proposta não impõe

- Situação da área (liberada, alerta, interditada), nível `EXTREMO`, cores piscantes ou bloqueio automático por resultado de inspeção. Nenhum requisito ou decisão prevê isso.
- Inspeção obrigatória antes de alterar o grau de perigo de uma área. A atualização de áreas do MVP continua independente.
- Periodicidade obrigatória, catálogo de normas ou itens, critérios de avaliação e laudo. A regra 4 da US09 os deixa a confirmar.
- Anexos de mídia (`MidiaAnexo`), que dependem de UC04, no backlog.

---

## 3. Verificação

- **Domínio**: `InspecaoTest` cobre data futura, ausência de itens, de área e de responsável, e item sem descrição.
- **Caso de uso**: `InspecaoUseCaseTest` cobre o Gestor autenticado como responsável, a recusa sem Gestor identificável sem gravar nada e a área inexistente.
- **API**: `InspecaoControllerTest` cobre as respostas da seção 2.5 e a recusa ao Supervisor.
- **PostgreSQL**: `InspecaoPersistenciaIntegracaoTest` aplica as migrações até a V24, grava pela API, reconstrói a inspeção com seus itens depois de limpar o contexto de persistência, confere a ordem do histórico e verifica que um registro recusado não deixa linha em `inspecoes` nem em `itens_verificados`.

---

## 4. Pendências Não Resolvidas por Este Contrato

| Ponto                                       | Situação                                                                                       |
| ------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| Consulta pelo Supervisor                    | Sem decisão. A proposta mantém o acesso só para o Gestor até o grupo decidir.                  |
| Periodicidade das inspeções                 | A confirmar (US09, regra 4). Não há alerta nem prazo.                                          |
| Itens de verificação e critérios            | A confirmar (US09, regra 4). A referência avaliada é texto livre; não se cria catálogo de NRs. |
| Observações em não conformidade             | A US09 marca a restrição como a confirmar. A proposta não as torna obrigatórias.               |
| Anexos de mídia                             | Dependem de UC04 (backlog).                                                                    |
| Correção das multiplicidades no PNG         | Após a aprovação da seção 2.3.                                                                 |
| Caso de uso e diagrama de sequência de RF09 | Inexistentes. Podem ser criados a partir deste contrato quando RF09 for priorizado.            |
