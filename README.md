# SOS VIALE — Sistema Receptivo

Aplicação desktop em **Java Swing** para gestão de receptivo turístico: cadastros (passageiros, motoristas, veículos, pontos de coleta), **transfers**, **ordens de serviço**, relatórios em **PDF**, suporte a **câmbio** e **modo offline** quando o banco não está disponível (desde que exista snapshot local).

---

## Requisitos

| Ferramenta | Versão / observação |
|------------|---------------------|
| **JDK** | 17 (conforme `pom.xml`) |
| **Maven** | 3.8+ |
| **PostgreSQL** | 15+ (recomendado via **Docker Compose** abaixo) |
| **Docker** *(opcional)* | Para subir o banco com um comando |

---

## Estrutura do repositório

```
sistema-receptivo/          ← POM pai (módulos e dependências)
├── Sistema-Receptivo/      ← Aplicação (artefato sistema-receptivo-app)
│   └── src/main/java/br/com/sosviale/
│       ├── App.java        ← Ponto de entrada
│       ├── view/           ← Telas Swing
│       ├── service/        ← Regras de negócio
│       ├── repository/     ← Persistência (Hibernate/JPA)
│       ├── model/          ← Entidades
│       ├── config/         ← Configuração (JDBC, .env)
│       └── ...
│   └── src/main/resources/db/migration/   ← Migrações Flyway
├── docker-compose.yml      ← PostgreSQL para desenvolvimento
├── .env.example            ← Modelo de variáveis sensíveis (JWT, crypto)
└── README.md
```

Arquitetura de chamadas: **View → Service → Repository** (a interface não acessa repositório diretamente).

---

## Configuração rápida

### 1. Variáveis de ambiente (recomendado)

Na **raiz do repositório** (ou em qualquer pasta ancestral do diretório de trabalho ao rodar o `App`), copie o exemplo e ajuste em ambiente de desenvolvimento:

```bash
cp .env.example .env
```

Edite `.env` e defina valores seguros (nunca commite o arquivo `.env`).

Variáveis relevantes no exemplo:

- `RECEPTIVO_JWT_SECRET` — segredo JWT (mínimo ~32 caracteres em produção).
- `RECEPTIVO_CRYPTO_KEY` — chave usada em operações de criptografia de dados sensíveis.

O carregamento é feito por `br.com.sosviale.config.EnvLoader` **antes** da inicialização da aplicação.

### 2. Banco de dados (Docker)

Na raiz do projeto:

```bash
docker compose up -d
```

Isso sobe o PostgreSQL com os valores padrão compatíveis com `DatabaseConfig`:

| Configuração | Valor padrão |
|--------------|----------------|
| Host / porta | `localhost` / **5600** (mapeada para 5432 no container) |
| Banco | `sos_viale_db` |
| Usuário | `viale_user` |
| Senha | `viale_password` |

Você pode sobrescrever com variáveis de ambiente do Compose (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`) — se mudar porta ou credenciais, atualize também o código em `DatabaseConfig.java` (hoje os valores são fixos no fonte).

Aguarde o container ficar saudável (`healthy`) antes de rodar o sistema.

### 3. Migrações

As migrações **Flyway** rodam automaticamente na inicialização de `App` quando a conexão com o banco é bem-sucedida (`tentarConfigurarBanco()`).

---

## Como executar

### Opção A — IDE (recomendado)

1. Importe o projeto como **Maven** (raiz: `pom.xml` pai).
2. Marque o módulo **`Sistema-Receptivo`** (artefato `sistema-receptivo-app`).
3. Execute a classe **`br.com.sosviale.App`**.

Certifique-se de que o **working directory** do run configuration aponte para uma pasta onde o `.env` possa ser encontrado (geralmente a raiz do repositório), se você usar esse arquivo.

### Opção B — Maven (compilar)

```bash
mvn clean compile -pl Sistema-Receptivo -am
```

Em seguida, execute `App` pela IDE com o classpath do módulo, ou configure um **fat JAR** / `exec-maven-plugin` no `pom.xml` se quiser linha de comando sem IDE (não vem configurado no repositório por padrão).

---

## Primeiro acesso (dados de desenvolvimento)

Após as migrações, existe usuário inicial (veja `db/migration/V4__Usuario.sql` e endurecimento em `V10__Security_Hardening.sql`):

- **Usuário:** `admin`  
- **Senha:** `admin123`  

**Troque a senha em produção.** Há também dados de exemplo em `V9__Dados_Inicias_Para_Testes_Rapidos.sql` (motoristas, veículos, transfers, etc.).

A tela de login pré-preenche esses campos apenas para facilitar o desenvolvimento local.

---

## Modo offline

Se o PostgreSQL não estiver acessível **e** o aplicativo já tiver **snapshots** salvos (`OfflineStore`), o sistema pode abrir em **modo offline** com os dados em cache. Na primeira execução, é necessário conectar ao banco ao menos uma vez para sincronizar (ou restaurar um snapshot válido).

---

## Internacionalização

Mensagens e rótulos são carregados via **`LanguageManager`** (mapas em código + recursos). O usuário pode alterar o idioma na interface (cabeçalho da tela de login / dashboard, conforme implementação atual).

---

## Tecnologias principais

- **Java 17**, **Swing**
- **Maven** (multi-módulo)
- **PostgreSQL**, **Jakarta Persistence / Hibernate**
- **Flyway** (versionamento de schema)
- **iText** (PDF)
- **JJWT**, **Jackson**
- **Docker Compose** (banco local)

---

## Solução de problemas

| Problema | O que verificar |
|----------|------------------|
| Erro ao conectar no banco | Docker está `up`? Porta **5600** livre? Credenciais batem com `DatabaseConfig`? |
| Flyway falha | Logs no console; conflito de migração só em bases já manipuladas manualmente |
| Modo offline não abre | Sem snapshot local — conecte uma vez com o banco ou importe dados conforme documentação de offline |
| `.env` não lido | Caminho do diretório de trabalho da execução; ou defina variáveis no sistema |

---

## Licença e equipe

Defina aqui a licença e os autores do trabalho acadêmico ou da organização.

---

## Histórico Git

Mantenha **commits** pequenos e mensagens claras; toda a entrega deve estar versionada no repositório remoto conforme orientação do professor.
