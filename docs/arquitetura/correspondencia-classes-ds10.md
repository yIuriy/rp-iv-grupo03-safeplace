# Correspondência entre Diagrama de Classes e DS10 / DS10.1 (Substituição Inteligente de EPIs)

## 1. Visão Geral e Contexto

Este documento formaliza as decisões arquiteturais e a matriz de correspondência entre o **Diagrama de Classes**, o **Diagrama de Sequência DS10 (Planejar Substituição Inteligente de EPI)** e o subdiagrama **DS10.1 (Calcular Projeções de Substituição)** do SafePlace, atendendo às regras de consistência da revisão de escopo do MVP (RF21; UC10; US21; issue #108).

---

## 2. Decisões de Modelo e Parâmetros Aprovados

### 2.1. Origem e Unidade dos Dados
1. **Durabilidade Nominal ($D_{nom}$)**:
   - **Origem**: Atributo `vidaUtilDias : Integer` da classe `EPI` (especificação de durabilidade nominal pelo fabricante).
   - **Unidade**: Dias corridos.
2. **Frequência e Intensidade de Uso ($U$)**:
   - **Origem**: Histórico de empréstimos da classe `Emprestimo` (`dataRetirada`, `dataDevolucaoReal` ou `dataDevolucaoPrevista`).
   - **Unidade / Escala**: Adimensional ($0 < U \le 1.0$), representando a taxa diária de utilização no período de amostragem:
     $$U = \frac{\sum \text{Dias em Empréstimo}}{\text{Dias Totais do Período de Amostragem}}$$
3. **Condições Ambientais do Setor ($F_{amb}$)**:
   - **Origem**: Associação com a classe `Setor`, através do seu atributo `status : NivelPerigo`.
   - **Multiplicadores da Matriz de Risco**:
     - `BAIXO`: **1.0** (ambiente controlado / padrão)
     - `MEDIO`: **1.25**
     - `ALTO`: **1.5**
     - `CRITICO`: **2.0** (ambiente severo com alta agressividade química, abrasiva ou mecânica)
4. **Teto Legal Inviolável de CA ($CA_{val}$)**:
   - **Origem**: Atributo `validadeCA : Date` (ou `LocalDate`) de `ModeloEPI`, vinculado ao lote/EPI.
   - **Regra**: Conforme UC10 regra 1, nenhum EPI pode ter uso projetado após a expiração do CA.

### 2.2. Fórmula Matemática de Projeção e Justificativa
1. **Taxa de Desgaste Diário Efetivo ($TD$)**:
   $$TD = U \times F_{amb}$$
2. **Dias Restantes de Vida Útil Mecânica ($DR$)**:
   $$DR = \frac{D_{nom} - \text{Desgaste Acumulado Prévio}}{TD}$$
3. **Data Física Estimada de Desgaste**:
   $$\text{DataFisica} = T_{ref} + DR \text{ dias}$$
4. **Data Estimada de Descarte e Justificativa**:
   $$\text{dataEstimadaDescarte} = \min(\text{DataFisica}, CA_{val})$$
   - Se $\text{DataFisica} > CA_{val}$:
     *Justificativa*: `"Substituição antecipada pelo vencimento legal do CA em [Data], antes do término da vida útil física projetada ([DataFisica])."`
   - Se $\text{DataFisica} \le CA_{val}$:
     *Justificativa*: `"Substituição por desgaste acelerado em ambiente [NivelPerigo] (fator [Fator]), taxa de uso diário de [U*100]%."`

### 2.3. Tratamento de Inconsistência e Ausência de Dados (UC10 Exceção I)
Caso um EPI não possua durabilidade nominal cadastrada (`vidaUtilDias == null || <= 0`), não possua histórico de empréstimos, não possua setor/risco associado ou não possua CA válido:
- **O sistema não gera estimativas fictícias**.
- A operação registra uma pendência/falha estruturada para aquele equipamento, informando o motivo exato.
- A interface/API disponibiliza a opção de **nova tentativa** (`disponibilizarNovaTentativa()`), permitindo ao gestor retificar os dados e reprocessar.

### 2.4. Antecedência e Alertas Preventivos (Escopo MVP)
- **Alerta de Descarte / CA Próximo**: Emitido quando $\text{dataEstimadaDescarte} \le T_{ref} + 30 \text{ dias}$ ou $\text{validadeCA} \le T_{ref} + 30 \text{ dias}$.
- **Alerta de Reposição Prioritária (Estoque Crítico)**: Emitido quando $\text{quantidade} \le \text{estoqueMinimo}$ (operação `isEstoqueCritico()`).
- **Desdobramentos Mantidos no Backlog**:
  - Geração automática de requisições de compra (`RequisicaoCompra`): passo 2.1.5 do DS10 não é acionado no MVP sem decisão prévia de integração de compras.
  - Prorrogação por laudo técnico pericial (Cenário Alternativo I de UC10): backlog.
  - Exportação de relatório consolidado trimestral em PDF/CSV (Cenário Alternativo III de UC10 / RF18): backlog.

---

## 3. Detalhamento do Subfluxo DS10.1 (Calcular Projeções de Substituição)

O elemento de referência `ref DS10.1 - Calcular Projeções de Substituição(epis)` do DS10 é decomposto nos seguintes passos sequenciais:

1. **Iteração sobre cada EPI** carregado para a análise (`for each epi in epis`).
2. **Consulta de Histórico de Empréstimos** (`EmprestimoRepositoryPort.listarPorEpi(epi.id)`):
   - Recupera os empréstimos concluídos e em andamento.
   - Calcula os dias de uso efetivo no período e a taxa diária de utilização $U$.
   - Se a lista estiver vazia e não houver taxa informada, sinaliza pendência de histórico.
3. **Consulta de Durabilidade Nominal** (`epi.getVidaUtilDias()`):
   - Se for nulo ou menor/igual a zero, sinaliza pendência de durabilidade.
4. **Consulta das Condições Ambientais do Setor** (`SetorRepositoryPort` ou setor vinculado ao EPI):
   - Obtém `setor.getStatus() : NivelPerigo`.
   - Se o setor não puder ser determinado, sinaliza pendência de ambiente.
5. **Avaliação de Consistência (Decisão de Falha)**:
   - Se houver pendências detectadas: instancia registro de erro (`FalhaProjecao`) e prossegue para o próximo EPI (permitindo posterior retificação e nova tentativa).
   - Se todos os dados estiverem consistentes: prossegue para o cálculo.
6. **Consulta de Validade do CA** (`epi.getModelo().getValidadeCa()` ou `epi.getDataValidadeCa()`):
   - Obtém a data limite legal.
7. **Cálculo da Taxa de Desgaste e Data Projetada**:
   - Aplica a fórmula matemática aprovada ($TD = U \times F_{amb}$, $DR = \text{dias restantes}$, $\text{dataEstimadaDescarte} = \min(\text{DataFisica}, CA_{val})$).
8. **Criação da Entidade de Projeção**:
   - Cria instância de `ProjecaoSubstituicao` associada ao EPI com `dataProjecao = T_ref`, `dataEstimadaDescarte` e `justificativa`.
9. **Retorno do Subfluxo**:
   - Retorna o conjunto de projeções calculadas com sucesso e o conjunto de falhas/pendências estruturadas para o controlador `ControleSubstituicaoEPI`.

---

## 4. Matriz de Correspondência: Diagrama de Classes e DS10 vs. Arquitetura Hexagonal

| Elemento no DS10 / Classes | Responsabilidade / Semântica | Componente na Arquitetura Hexagonal |
| :--- | :--- | :--- |
| `: VisaoSubstituicaoEPI` | Interface do usuário e adaptação HTTP dos comandos e telas de histórico/projeção. | `adapters.in.web.SubstituicaoEpiController` e DTOs de entrada/saída. |
| `: ControleSubstituicaoEPI` | Orquestração do caso de uso de planejamento de substituição e emissão de alertas. | `application.port.in.PlanejarSubstituicaoUseCase` e sua implementação `PlanejarSubstituicaoUseCaseImpl`. |
| `ref DS10.1` | Cálculo matemático de taxa de desgaste, validação de regras de CA e ambiente. | `domain.epi.SubstituicaoInteligenteService` (Serviço de Domínio puro). |
| `: ProjecaoSubstituicao` | Entidade do modelo com `dataProjecao`, `dataEstimadaDescarte` e `justificativa`. | Entidade `domain.epi.ProjecaoSubstituicao`, tabela `projecoes_substituicao`, e adaptador `ProjecaoSubstituicaoJpaAdapter`. |
| `EPI.buscarEPIs()` | Obter relação de equipamentos cadastrados para análise. | `application.port.out.EpiRepositoryPort.listarTodos()` (conforme decisão dos métodos legados em [Correspondência de EPIs](correspondencia-classes-epi.md)). |
| `ProjecaoSubstituicao.registrarProjecoes()` (retirada do PNG em 16/10/2026) | Persistir as projeções geradas na base de dados. | `application.port.out.ProjecaoSubstituicaoRepositoryPort.salvarTodas()`. |
| `ProjecaoSubstituicao.emitirListaReposicao()` (retirada do PNG em 16/10/2026) | Filtrar EPIs com alertas preventivos (descarte em até 30 dias ou estoque crítico). | Método de domínio / caso de uso em `PlanejarSubstituicaoUseCaseImpl` gerando `AlertaSubstituicaoEpi`. |
| `EPI.consultarEstoque()` (retirada do PNG em 16/10/2026) / `EPI.compararComQuantMinima()` | Verificar saldo e estoque mínimo (`isEstoqueCritico()`). | Métodos do domínio `Epi.getQuantidade()`, `Epi.getEstoqueMinimo()`, `Epi.isEstoqueCritico()` ([Correspondência de EPIs](correspondencia-classes-epi.md)). |
| `ProjecaoSubstituicao.calcularDiasRestantes(data)` / `isProximoDoDescarte(data, dias)` | Dias até o descarte estimado e verificação da antecedência do alerta. | Métodos do domínio `ProjecaoSubstituicao`. |
| `AlertaSubstituicaoEPI` (`tipoAlerta : TipoAlertaSubstituicao`) | Alerta preventivo de descarte próximo, CA próximo do vencimento ou estoque crítico, com prioridade. | Entidade `domain.epi.AlertaSubstituicaoEpi`, gerada por `SubstituicaoInteligenteService.emitirAlertas`. |
| `exibirErroCalculo()` / `disponibilizarNovaTentativa()` | Apresentar pendências detalhadas por EPI e permitir reprocessamento. | Resposta estruturada `ResultadoAnaliseSubstituicaoDTO` contendo `pendenciasFalhas` e status HTTP 200/422. |
