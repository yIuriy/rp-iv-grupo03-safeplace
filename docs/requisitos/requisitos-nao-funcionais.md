# Requisitos Não-Funcionais

Este documento apresenta a especificação dos Requisitos Não-Funcionais (RNFs) do sistema SafePlace.

Os requisitos a seguir delimitam os critérios de qualidade, restrições técnicas e níveis de serviço que o ecossistema SafePlace deve cumprir.

## RNF01 — Tempo de resposta

O sistema deve responder às interações do usuário em até 3 segundos em condições normais de uso, e em até 1 segundo para consultas simples de estoque e cadastros.

## RNF02 — Capacidade de usuários simultâneos

O sistema deve suportar ao menos 20 usuários simultâneos sem degradação perceptível de desempenho, sendo escalável conforme o crescimento da organização.

## RNF03 — Controle de acesso por perfil

O sistema deve implementar controle de acesso baseado em perfis (RBAC), restringindo funcionalidades conforme o papel do usuário, sendo eles: colaborador, gestor de segurança e supervisor.

## RNF04 — Criptografia de dados sensíveis

Todos os dados sensíveis, como atestados de saúde, Comunicações de Acidente de Trabalho (CATs), históricos de acidentes e mídias anexadas, devem ser armazenados e transmitidos com criptografia AES-256 e protocolo HTTPS com TLS 1.3.

## RNF05 — Rastreabilidade de ações (auditoria)

O sistema deve manter log imutável de todas as operações de criação, edição e exclusão de registros, com identificação do usuário responsável, data, hora e dado alterado, armazenado por no mínimo 5 anos.

## RNF06 — Disponibilidade mínima

O sistema deve garantir disponibilidade de 99,5% (uptime), com janela de manutenção planejada de no máximo 4 horas mensais, preferencialmente fora do horário de expediente.

## RNF07 — Backup e recuperação de dados

O sistema deve realizar backup automático diário dos dados, com capacidade de recuperação (RPO) de até 24 horas e tempo de restauração (RTO) de até 4 horas em caso de falha crítica.

## RNF08 — Funcionamento offline parcial

As funcionalidades críticas de registro de acidentes e incidentes devem operar em modo offline por meio de recursos de Progressive Web App (PWA), armazenando os dados localmente no dispositivo e sincronizando automaticamente com o servidor quando a conexão for restabelecida. Funcionalidades que dependem de processamento em tempo real, como dashboards, ficam indisponíveis durante a ausência de conexão.

## RNF09 — Conformidade com normas regulamentadoras

O sistema deve ser aderente às Normas Regulamentadoras do Ministério do Trabalho e Emprego aplicáveis, especialmente NR-1, NR-6 e NR-9, à Lei Geral de Proteção de Dados (LGPD — Lei 13.709/2018) para dados pessoais de saúde, e à legislação previdenciária aplicável à emissão de CAT.

## RNF10 — Integridade e validade dos documentos gerados

Os documentos gerados pelo sistema, como relatórios, CATs e laudos, devem conter assinatura digital ou hash de integridade para garantir autenticidade perante órgãos fiscalizadores.

## RNF11 — Interface responsiva e acessível

A interface do sistema deve ser responsiva, funcionando adequadamente em dispositivos desktop, tablet e mobile, e deve seguir as diretrizes de acessibilidade WCAG 2.1 nível AA, permitindo uso por pessoas com deficiências visuais ou motoras.

## RNF12 — Facilidade de aprendizado

Um usuário com conhecimento básico de informática deve ser capaz de realizar as tarefas principais do sistema, como registrar um acidente, consultar EPIs e emitir relatórios, após no máximo 2 horas de treinamento.

## RNF13 — Suporte multilíngue

O sistema deve oferecer interface em português do Brasil como idioma padrão, com possibilidade de expansão para espanhol e inglês sem necessidade de refatoração do código-fonte.

## RNF14 — Modularidade e extensibilidade

O sistema deve ser desenvolvido em arquitetura modular, permitindo a adição de novos módulos, como integração com sistemas ERP, sem impacto nas demais funcionalidades já existentes.

## RNF15 — Documentação técnica

O sistema deve possuir documentação técnica atualizada, contemplando API, modelo de dados e arquitetura, de forma que um desenvolvedor externo consiga compreender e realizar manutenções sem suporte da equipe original em até 5 dias úteis.

## RNF16 — Compatibilidade com navegadores e sistemas operacionais

O sistema web deve ser compatível com as duas versões mais recentes dos principais navegadores, sendo eles Google Chrome, Mozilla Firefox, Microsoft Edge e Safari, e deve poder ser implantado em ambientes com sistemas operacionais Linux e Windows Server.

## RNF17 — Integração via API

O sistema deve expor uma API RESTful documentada no padrão OpenAPI 3.0 para integração com sistemas externos, como ERPs e sistemas de recursos humanos, utilizando autenticação OAuth 2.0.
