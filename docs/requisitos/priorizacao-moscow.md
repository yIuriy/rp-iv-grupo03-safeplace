# Priorização de Requisitos - MoSCoW

Este documento registra a prioridade dos requisitos do SafePlace. As descrições completas permanecem nos documentos de [requisitos funcionais](requisitos-funcionais.md) e [requisitos não funcionais](requisitos-nao-funcionais.md).

Para a entrega atual, o MVP é formado pelos requisitos classificados como `Must have`. Os requisitos `Should have` e `Could have` permanecem no backlog. Os requisitos `Won't have` não fazem parte desta versão.

## Critérios da Técnica MoSCoW

| Classificação | Significado em inglês | Critério de uso |
| --- | --- | --- |
| Must have | Deve ter | É indispensável para o MVP cumprir seu objetivo. |
| Should have | Deveria ter | É importante, mas pode ser entregue depois do núcleo do MVP. |
| Could have | Poderia ter | É desejável e possui menor impacto na primeira entrega. |
| Won't have | Não terá agora | Está fora da versão atual e pode ser reavaliado no futuro. |

## Requisitos Funcionais

| Requisito | Priorização |
| --- | --- |
| RF01 - Gerenciar acidentes causados por um funcionário | Should have |
| RF02 - Gerenciar acidentes causados por falha de equipamento de segurança | Should have |
| RF03 - Controlar o estoque dos EPIs | Must have |
| RF04 - Controlar a manutenção dos EPIs | Must have |
| RF05 - Mapear as áreas de risco do ambiente de trabalho | Must have |
| RF06 - Classificar o nível de periculosidade da tarefa | Should have |
| RF07 - Informar o plano de ação para cada tipo de acidente | Should have |
| RF08 - Gerar alertas automáticos por comportamento de risco | Could have |
| RF09 - Registrar o histórico de inspeções periódicas | Should have |
| RF10 - Controlar certificações e treinamentos obrigatórios | Could have |
| RF11 - Controlar a rastreabilidade dos EPIs | Must have |
| RF12 - Conectar tarefas e EPIs | Should have |
| RF13 - Criar registros de acidente | Must have |
| RF14 - Gerenciar o descarte dos EPIs | Should have |
| RF15 - Registrar investigação de acidente | Could have |
| RF16 - Registrar incidentes | Must have |
| RF17 - Gerar dashboards de segurança | Could have |
| RF18 - Emitir relatórios estatísticos | Could have |
| RF19 - Gerar documentação legal | Could have |
| RF20 - Gerenciar fornecedores e Certificado de Aprovação | Should have |
| RF21 - Gerenciar ciclo de vida e substituição inteligente de EPIs | Won't have |
| RF22 - Gerenciar visitantes | Could have |
| RF23 - Gerenciar supervisores e colaboradores | Must have |

## Requisitos Não Funcionais

| Requisito | Priorização |
| --- | --- |
| RNF01 - Tempo de resposta | Must have |
| RNF02 - Capacidade de usuários simultâneos | Should have |
| RNF03 - Controle de acesso por perfil | Must have |
| RNF04 - Criptografia de dados sensíveis | Must have |
| RNF05 - Rastreabilidade de ações (auditoria) | Must have |
| RNF06 - Disponibilidade mínima | Should have |
| RNF07 - Backup e recuperação de dados | Could have |
| RNF08 - Funcionamento offline parcial | Could have |
| RNF09 - Conformidade com normas regulamentadoras | Must have |
| RNF10 - Integridade e validade dos documentos gerados | Should have |
| RNF11 - Interface responsiva e acessível | Should have |
| RNF12 - Facilidade de aprendizado | Must have |
| RNF13 - Suporte multilíngue | Could have |
| RNF14 - Desacoplamento e extensibilidade | Must have |
| RNF15 - Documentação técnica | Must have |
| RNF16 - Compatibilidade com navegadores e sistemas operacionais | Must have |
| RNF17 - Integração via API | Could have |

## Registro da revisão

Esta revisão preserva a priorização funcional definida pelo grupo. Nos requisitos não funcionais, RNF04 e RNF05 passam a `Must have` porque o MVP registra acidentes, incidentes e ações de usuários. Esses dados exigem proteção e rastreabilidade desde a primeira versão.
