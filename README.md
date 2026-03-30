# 🚐 Sistema de Gestão de Receptivo Turístico (CLI)

Um sistema robusto de Interface de Linha de Comando (CLI) para gerenciamento completo de logística, transfers e receptivo turístico. Focado em performance e usabilidade via terminal, o projeto otimiza as rotinas operacionais de agendamento, controle de frota e gestão financeira.

## 🚀 Tecnologias e Ferramentas

O projeto foi construído priorizando performance e execução 100% no console:

* **Backend / Core:** Java 17 (LTS)
* **Banco de Dados:** PostgreSQL
* **Containerização:** Docker & Docker Compose
* **Design de Interface:** Wireframes textuais / Arte ASCII
* **Versionamento:** Git e GitHub (Fluxo de trabalho para equipe de 5 desenvolvedores)
* **IDEs Recomendadas:** IntelliJ IDEA Ultimate / JetBrains

## 👥 Atores e Funcionalidades (Casos de Uso)

O sistema possui controle de acesso e comandos específicos para diferentes níveis de usuários:

* **Atendente:**
    * Agendamento de Transfers.
    * Gerenciamento de Passageiros.
    * Gerenciamento de Pontos de Coleta.
* **Gerente:**
    * Controle detalhado da Frota.
    * Emissão de Ordem de Serviço (Geração de PDF via terminal).
    * Cálculo de Custos e Câmbio (ideal para operações em regiões de fronteira).
* **Administrador:**
    * Acesso global aos comandos financeiros e operacionais.
    * Atualização forçada do Status do Serviço.
* **Motorista:**
    * Atualização do Status do Serviço em tempo real.
    * Sincronização de Dados Offline (para rotas sem cobertura de internet).

## 🛠️ Como Executar o Projeto

**Pré-requisitos:** Ter o [Java 17](https://jdk.java.net/17/), [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/) instalados na sua máquina.

**1. Clone o repositório:**
```bash
git clone [https://github.com/sua-equipe/receptivo-cli.git](https://github.com/sua-equipe/receptivo-cli.git)
cd receptivo-cli
