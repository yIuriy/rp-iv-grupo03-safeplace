# Documentação dos Casos de Uso

## UC01 – Controlar Manutenção dos EPI’S

### Resumo

Este caso de uso descreve as etapas percorridas por um gestor de segurança para controlar a manutenção dos Equipamentos de Proteção Individual (EPIs), garantindo que estejam em condições adequadas de uso.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

O gerente deve estar logado no sistema; Os EPIs devem estar previamente cadastrados.

### Pós-condições

Situação dos EPIs verificada;

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o módulo de EPIs
2. Exibir a lista de EPIs cadastrados com seus status
3. Consultar detalhes de um EPI específico
4. Exibir histórico e datas de manutenção
5. Analisar a situação

### Regras de Negócio, Restrições e Validações

1. Deve haver clareza visual nos indicadores de status dos equipamentos;
2. Apenas EPIs ativos devem ser exibidos.

### Cenário de Exceção I - EPI não encontrado

**Ações do Ator / Ações do Sistema**

1. Tentar acessar um EPI
2. Não encontrar o registro e exibir mensagem de erro.

## UC02 – Controlar certificações e treinamentos

### Resumo

Este caso de uso descreve o monitoramento das certificações dos funcionários, bem como os treinamentos realizados ou pendentes. O sistema também deve gerar alertas quando as certificações vencerem ou é necessário treinamento.

**Ator principal:** Gestor de Segurança

**Atores secundários:** Funcionário

### Pré-condições

O Gestor de Segurança deve estar logado no sistema.

Os dados dos funcionários devem estar cadastrados.

### Pós-condições

O sistema exibe as certificações dos funcionários, os treinamentos feitos ou pendentes. O sistema exibe um alerta de vencimento ou pendência são apresentados

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o menu “Certificações e Treinamentos”
2. Carregar tela com listagem de funcionários e suas certificações
3. Selecionar um funcionário
4. Exibir detalhes das certificações e treinamentos do funcionário
5. Visualizar certificações cadastradas
6. Visualizar treinamentos realizados

### Regras de Negócio, Restrições e Validações

1. O sistema deve alertar automaticamente quando uma certificação estiver próxima do vencimento;
2. A interface deve sempre exibir dados atualizados.

### Cenário Alternativo I - O funcionário possui certificados vencidos

**Ações do Ator / Ações do Sistema**

1. Identificar certificações vencidas
2. Destacar certificações que necessitam renovação
3. Emitir alerta que certificados necessitam de renovação

### Cenário Alternativo II - O funcionário possui Treinamentos pendentes

**Ações do Ator / Ações do Sistema**

1. Identificar treinamentos pendentes
2. Destacar treinamento que necessitam renovação
3. Emitir alerta que treinamentos necessitam de renovação

### Cenário Alternativo III - Funcionário sem certificações cadastradas

## UC03 – Monitorar Alerta do Sensor

### Resumo

Este caso de uso permite ao Gestor de Segurança monitorar alertas gerados automaticamente pelo sistema a partir dos dados recebidos dos sensores do ambiente de trabalho, possibilitando identificar situações de risco ou acidentes em andamento.

**Ator principal:** Gestor de Segurança

**Atores secundários:** Sensor (sistema externo)

### Pré-condições

- Os sensores devem estar instalados e conectados ao sistema.
- O sistema deve estar recebendo dados dos sensores.
- O gestor deve estar autenticado no sistema.

### Pós-condições

- Os alertas são visualizados pelo gestor.
- Os eventos ficam registrados no sistema.
- Ações podem ser tomadas a partir dos alertas.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. O sensor detecta uma situação de risco no ambiente.
2. O sensor envia os dados/alerta para o sistema.
3. O sistema recebe e processa os dados do sensor.
4. O sistema identifica uma situação de risco com base nos dados recebidos.
5. O sistema gera automaticamente um alerta.
6. O sistema armazena o alerta gerado no banco de dados.
7. O gestor acessa o sistema.
8. O gestor seleciona a opção de monitorar alertas.
9. O sistema exibe os alertas gerados pelos sensores
10. O gestor analisa os alertas.

### Regras de Negócio, Restrições e Validações

- O sistema deve gerar alertas automaticamente sempre que os dados recebidos dos sensores indicarem situação de risco.
- Apenas gestores autenticados podem visualizar os alertas.
- Todos os alertas devem ser armazenados para auditoria e histórico.
- O sistema deve exibir data, horário, sensor responsável e nível de risco do alerta.
- Caso haja falha na comunicação com o sensor, o sistema deve registrar o erro.

### Cenário Alternativo I - Filtrar Alertas por Critério

**Ações do Ator / Ações do Sistema**

1. O gestor acessa o painel de monitoramento de alertas.
2. Exibe a listagem completa de alertas ativos e histórico recente.
3. O gestor aciona o painel de filtros.
4. Exibe as opções de filtro:

- nível de risco
- sensor
- data/hora de início e fim
- status(ativo/reconhecido/descartado).
5. O gestor seleciona um ou mais critérios e confirma o filtro.
6. Valida a combinação de filtros informada.
7. Atualiza a listagem exibindo apenas os alertas que atendem aos critérios selecionados.

### Cenário de Exceção I - Falha de Comunicação com o Sensor

**Ações do Ator / Ações do Sistema**

1. O sistema detecta ausência de sinal ou timeout na comunicação com um sensor.
2. O sistema registra o evento de falha no log de erros, incluindo identificação do sensor, data e hora.
3. O sistema gera um alerta de falha de comunicação no painel do gestor.
4. O gestor visualiza o alerta de falha de comunicação.
5. O sistema exibe o último dado recebido do sensor e o tempo decorrido desde a última leitura bem-sucedida.
6. O gestor pode optar por tentar reconectar manualmente o sensor.
7. O sistema tenta restabelecer a comunicação e informa o resultado da tentativa.
8. O sistema marca o sensor como indisponível e informa a indisponibilidade ao gestor até que a comunicação seja restabelecida.

## UC04 – Anexar mídias e testemunhas

### Resumo

Permite ao gestor anexar evidências a um acidente registrado, como mídias (fotos, vídeos, documentos) e informações de testemunhas, enriquecendo o registro para análise futura.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

- O acidente já deve estar cadastrado no sistema.
- O gestor deve estar autenticado.

### Pós-condições

- As mídias e/ou testemunhas são vinculadas ao acidente.
- O registro do acidente é atualizado com informações adicionais.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. O sistema exibe os acidentes cadastrados.
2. O gestor acessa um acidente existente.
3. O gestor seleciona a opção de anexar mídias/testemunhas.
4. O sistema apresenta opções de:

- Upload de mídias
- Cadastro de testemunhas
5. O gestor insere os dados desejados.
6. O sistema valida os arquivos e informações.
7. O sistema vincula os dados ao acidente.

### Regras de Negócio, Restrições e Validações

### Cenário Alternativo I - Remover Mídia ou Testemunha Já Anexada

**Ações do Ator / Ações do Sistema**

1. O gestor acessa o acidente já registrado.
2. Exibe a lista de mídias e testemunhas atualmente vinculadas ao acidente.
3. O gestor seleciona o item que deseja remover (mídia ou testemunha).
4. Exibe um diálogo de confirmação solicitando que o gestor confirme a remoção e informe uma justificativa.
5. O gestor confirma a remoção e informa a justificativa.
6. Valida a justificativa (campo obrigatório).
7. Remove o vínculo do item com o acidente e registra no histórico de alterações: item removido, responsável, data, hora e justificativa.
8. Atualiza a lista exibida refletindo a remoção.

### Cenário de Exceção I - Arquivo com Formato ou Tamanho Inválido

**Ações do Ator / Ações do Sistema**

1. O gestor seleciona um arquivo para upload.
2. Valida o formato do arquivo (ex.: aceitos: JPG, PNG, MP4, PDF) e o tamanho máximo permitido.
3. Detecta que o arquivo não atende a um ou mais critérios (formato não suportado ou tamanho excede o limite).
4. Exibe mensagem de erro especificando o motivo da rejeição e os formatos/tamanhos aceitos.
5. Cancela o upload e descarta o arquivo sem vinculá-lo ao acidente.
6. O gestor seleciona um novo arquivo válido ou cancela a operação.

## UC05 – Gerenciar Acidentes

### Resumo

Permite registrar, consultar, atualizar e excluir informações sobre acidentes causados individualmente por um funcionário, especialmente aqueles decorrentes do uso incorreto ou ausência de EPI.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

O Gestor deve estar cadastrado no sistema.

### Pós-condições

O acidente fica registrado no sistema, podendo ser consultado e utilizado para análises futuras.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. O ator acessa a funcionalidade de gerenciamento de acidentes.
2. O sistema apresenta as opções (cadastrar, consultar, editar ou excluir acidente).
3. O ator escolhe cadastrar acidente.
4. O sistema solicita os dados do acidente
5. O ator preenche as informações.
6. O sistema valida os dados.
7. O sistema registra o acidente.
8. Executar o caso de uso “UC04 - Anexar mídias e testemunhas”

### Regras de Negócio, Restrições e Validações

- O acidente deve estar vinculado a um funcionário cadastrado.
- O sistema deve registrar automaticamente data e horário do cadastro.
- Apenas gestores autorizados podem registrar acidentes.
- O registro do acidente não pode ser removido do sistema, apenas arquivado.
- O sistema deve permitir anexar evidências e testemunhas relacionadas ao acidente.

### Cenário Alternativo I - Consultar Acidente Existente

**Ações do Ator / Ações do Sistema**

1. O ator acessa a funcionalidade de gerenciamento de acidentes.
2. O sistema apresenta as opções (cadastrar, consultar, editar ou excluir acidente).
3. O gestor seleciona a opção 'Consultar acidente'.
4. O sistema exibe campo de busca e filtros disponíveis (por funcionário, data, tipo de acidente ou status).
5. O gestor informa um ou mais critérios de busca e confirma.
6. O sistema busca os registros correspondentes.
7. O sistema exibe a lista de acidentes encontrados com dados resumidos: data, funcionário, tipo e status.
8. O gestor seleciona um acidente da lista.
9. O sistema exibe os detalhes completos do acidente: dados do funcionário, descrição, data/hora de registro, mídias e testemunhas vinculadas e plano de ação associado (se houver)

## UC06 – Controlar Estoque de EPIs

### Resumo

Este caso de uso descreve o processo de controle do estoque de Equipamentos de Proteção Individual (EPIs), permitindo ao gestor visualizar, cadastrar, atualizar e monitorar a quantidade disponível de cada item.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

O Gestor de Segurança deve estar autenticado no sistema.

Os EPIs devem estar cadastrados no sistema.

### Pós-condições

O sistema exibe o estoque atualizado de EPIs.

Alterações no estoque são registradas corretamente.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o menu “Estoque de EPIs”
2. Carregar listagem de EPIs disponíveis
3. Visualizar itens do estoque
4. Exibir nome, quantidade e status dos EPIs
5. Selecionar um EPI
6. Exibir detalhes do item
7. Atualizar quantidade
8. Validar dados informados
9. Confirmar atualização
10. Atualizar estoque no sistema

### Regras de Negócio, Restrições e Validações

1. Todo EPI deve possuir um cadastro com: nome, tipo, quantidade e nível mínimo de estoque
2. EPIs com quantidade abaixo do nível mínimo devem ser destacados no sistema
3. Não é permitido que o estoque fique com quantidade negativa
4. Apenas o Gestor de Segurança pode cadastrar, editar ou remover EPIs
5. Campos obrigatórios devem ser preenchidos corretamente

### Cenário Alternativo I - Estoque vazio

**Ações do Ator / Ações do Sistema**

1. Verificar disponibilidade de itens
2. Não encontrar EPIs cadastrados
3. Exibir mensagem: “Nenhum EPI cadastrado no sistema”

### Cenário Execeção I - Falha ao atualizar estoque

**Ações do Ator / Ações do Sistema**

1. Atualizar quantidade de EPI
2. Tentar salvar dados
3. Detectar falha no salvamento
4. Exibir mensagem de erro

## UC07 – Definir plano de ação por acidente

### Resumo

Este caso de uso descreve as etapas percorridas por um gestor de segurança para definir um plano de ação para acidentes ocorridos, estabelecendo medidas corretivas e preventivas.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

O gerente deve estar logado no sistema; O acidente deve estar previamente registrado no sistema;

### Pós-condições

Plano de ação registrado no sistema.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o módulo de acidentes
2. Exibir a lista de acidentes registrados
3. Selecionar um acidente
4. Exibir os detalhes do acidente
5. Definir as ações necessárias e confirmar registro
6. Validar os dados e salvar informações vinculando o plano ao acidente.

### Regras de Negócio, Restrições e Validações

1. O acidente deve existir ### Cenário Alternativo I - Atualizar plano de ação

**Ações do Ator / Ações do Sistema**

1. Acessar o plano existente
2. Realizar alterações
3. Salvar novas alterações

### Cenário de Exceção I - Acidente não encontrado

**Ações do Ator / Ações do Sistema**

1. Tentar acessar um acidente que não existe
2. Não encontrar acidente
3. Exibir erro e retornar lista

## UC08 – Interligar tarefa ao EPI

### Resumo

Este caso de uso descreve as etapas percorridas por um gestor de segurança para associar tarefas ou atividades aos Equipamentos de Proteção Individual (EPIs) necessários.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

O gerente deve estar logado no sistema; Os EPIs devem estar cadastrados; As tarefas/atividades devem estar previamente cadastradas.

### Pós-condições

Tarefa vinculada a um ou mais EPIs

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o módulo de tarefas/EPIs
2. Exibir a lista de tarefas cadastradas
3. Selecionar uma tarefa
4. Apresentar lista de EPIs disponíveis
5. Confirmar a associação
6. Validar os dados e salvar informações.

### Regras de Negócio, Restrições e Validações

Dados devem estar atualizados

### Cenário Alternativo I - alterar EPIs vinculados

**Ações do Ator / Ações do Sistema**

1. Acessar o plano existente
2. Realizar alterações
3. Salvar novas alterações

## UC09 – Simular cenários de emergência

### Resumo

Este caso de uso descreve as etapas percorridas por um gestor de segurança para simular cenários de emergência nas áreas de risco cadastradas para validar algoritmos de evacuação e tempos de resposta.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

- O gerente deve estar logado no sistema;
- As áreas de risco devem estar cadastradas e mapeadas no sistema.

### Pós-condições

Relatório de eficácia e vulnerabilidade gerado para análise técnica.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o módulo de simulação.
2. Carrega o mapa base da empresa mapeada
3. Seleciona o tipo de acidente e local de origem
4. Validar os limites geográficos do setor.
5. Validar as saídas cadastradas no setor.
6. Bloqueia virtualmente as saídas de emergência localizadas na zona de impacto.
7. Executa o algoritmo de busca de caminho para todos os usuários presentes na zona de impacto
8. Projeta as rotas de fuga alternativas e calcula o tempo estimado de evacuação total.
9. Identifica pontos onde a concentração de pessoas excede a capacidade da via de saída.
10. Finaliza a simulação
11. Gera um relatório comparativo entre o cenário ideal e o cenário simulado.

### Regras de Negócio, Restrições e Validações

1. O sistema não deve permitir simulações em setores que não possuam pelo menos uma saída de emergência cadastrada no mapa base.
2. O cálculo de gargalos deve considerar uma velocidade média de deslocamento de 1,2 m/s para adultos em ambiente plano, conforme normas de segurança contra incêndio.
3. O sistema deve validar se o ponto de origem do acidente está dentro dos limites da planta física.
4. O sistema suporta a simulação de até 500 perfis de funcionários simultaneamente por planta para manter a performance.

### Cenário Alternativo I - Alteração de variáveis em tempo real

**Ações do Ator / Ações do Sistema**

6. Escolhe a opção de aplicar bloqueios manuais na planta física.
7. Bloqueia manualmente uma ou mais saídas de emergência no mapa.
8. O sistema aplica o status de bloqueio nas saídas de emergência selecionadas.
9. O sistema recalcula as rotas de fuga disponíveis.
10. O sistema atualiza a projeção gráfica das novas rotas no mapa da interface.

### Cenário de Exceção I - Cenário sem saída

**Ações do Ator / Ações do Sistema**

5. Identifica que o ponto escolhido bloqueia 100% das rotas de fuga existentes.
6. Emite um alerta crítico de falha de projeto na planta física.
7. Sugere a instalação de novas saídas de emergência.

### Cenário de Exceção II - Seleção fora do limite do setor

**Ações do Ator / Ações do Sistema**

4. Identifica que o ponto escolhido está fora dos limites do setor.
5. Notifica o gestor sobre o erro e o redireciona de volta para a seleção de local (retorna ao passo 3 do fluxo principal).

### Cenário de Exceção III - Setor sem saída de emergência cadastrada

**Ações do Ator / Ações do Sistema**

7. Identifica a ausência de portas de fuga no cadastro do setor.
8. Emite um alerta crítico de falha de projeto na planta física.
9. Sugere o cadastro de novas saídas de emergência.

## UC10 – Planejar substituição inteligente de EPI

### Resumo

Este caso de uso descreve as etapas percorridas por um gestor de segurança para gerenciar o ciclo de vida dos EPIs, automatizando o cálculo de desgaste e prevendo a substituição baseada em critérios técnicos e regulatórios.

**Ator principal:** Gestor de Segurança

**Atores secundários:**

### Pré-condições

- Histórico de uso de EPIs atualizado;
- Níveis de estoque registrados.

### Pós-condições

Necessidade de reposição registrada.

### Cenário Principal

**Ações do Ator / Ações do Sistema**

1. Acessar o módulo de histórico dos EPIs
2. Solicita ao sistema a geração da "Análise de Projeção de Desgaste".
3. Analisa a frequência de uso de cada EPI vinculada aos registros de ponto e atividades de risco dos funcionários.
4. Cruza a durabilidade nominal do fabricante com as condições ambientais reportadas
5. Verifica automaticamente a validade do Certificado de Aprovação (CA) diretamente no cadastro do EPI.
6. Gera uma projeção de descarte para a próxima semana.
7. Analisa a lista de projeções geradas
8. Emite uma lista de reposição prioritária para os itens que atingirão o limite de segurança na próxima semana.
9. Compara o estoque atual com o limite mínimo de segurança pré-estabelecido.
10. Gera uma requisição de compra automática (registrando uma nova Movimentação de Estoque pendente) para os itens cujo estoque mínimo foi atingido.

### Regras de Negócio, Restrições e Validações

- A data de expiração do CA (Certificado de Aprovação) tem prioridade absoluta sobre o cálculo de desgaste físico. Se o CA vencer, o EPI deve ser descartado independentemente do estado de conservação.
- O sistema deve aplicar multiplicadores de desgaste (0.5x a 1.5x) baseados na matriz de riscos do setor onde o funcionário está alocado.
- O sistema deve impedir o registo de novos EPIs no estoque caso o número do CA fornecido seja inválido ou esteja cancelado no portal do Ministério do Trabalho.
- O cálculo de projeção de descarte é processado em lote (batch) uma vez por dia para garantir a consistência dos dados de estoque.

### Cenário Alternativo I - Prorrogação por inspeção física

**Ações do Ator / Ações do Sistema**

7. Realizar uma inspeção física e insere um "aval técnico".
8. Estende a data de descarte calculada pelo sistema no cadastro do EPI (respeitando o limite do CA).
9. Vincula o aval técnico como justificativa na projeção atual.

### Cenário Alternativo II - Ajuste de lote

**Ações do Ator / Ações do Sistema**

7. Altera as quantidades da requisição automática.
8. Valida a nova quantidade contra o histórico de consumo médio.
9. Atualiza a quantidade de itens solicitados na requisição de compra (Movimentação de Estoque).

### Cenário de Exceção I - EPI com CA Expirado em Uso

**Ações do Ator / Ações do Sistema**

1. Detecta que um funcionário está utilizando um EPI cujo CA expirou.
2. Gera um alerta de comportamento impeditivo no painel do Gestor e no aplicativo do Funcionário, exigindo a substituição imediata antes do início da jornada.
