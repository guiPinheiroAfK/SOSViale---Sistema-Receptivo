# 🚐 Sistema de Gestão de Receptivo Turístico (CLI)

Um sistema robusto de Interface de Linha de Comando (CLI) para gerenciamento completo de logística, transfers e receptivo turístico. Focado em performance e usabilidade via terminal, o projeto otimiza as rotinas operacionais de agendamento, controle de frota e gestão financeira.

## 🚀 Tecnologias e Ferramentas

O projeto foi construído priorizando performance e execução 100% no console:

- **Backend / Core:** Java 17 (LTS)
- **Banco de Dados:** PostgreSQL
- **Containerização:** Docker & Docker Compose
- **Design de Interface:** Wireframes textuais / Arte ASCII
- **Versionamento:** Git e GitHub (fluxo para equipe de 5 desenvolvedores)
- **IDEs Recomendadas:** IntelliJ IDEA Ultimate / JetBrains

## 👥 Atores e Funcionalidades

O sistema possui controle de acesso com comandos específicos para cada perfil:

- **Atendente:** agendamento de transfers, gerenciamento de passageiros e pontos de coleta.
- **Gerente:** controle de frota, emissão de ordem de serviço (PDF via terminal) e cálculo de custos com câmbio (ideal para regiões de fronteira).
- **Administrador:** acesso global aos módulos financeiros e operacionais, atualização forçada de status.
- **Motorista:** atualização de status em tempo real e sincronização de dados offline (para rotas sem internet).

## 🛠️ Como Executar

**Pré-requisitos:** [Java 17](https://jdk.java.net/17/), [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) instalados.

```bash
git clone https://github.com/sua-equipe/receptivo-cli.git
cd receptivo-cli
```