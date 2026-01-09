# Helpdesk API

API de gerenciamento de chamados (tickets) para um sistema de Help Desk. Desenvolvida em **Spring Boot 3.4.11** com **Java 17** e **PostgreSQL**.

---

## 📋 Pré-requisitos

Escolha uma das opções abaixo:

### Opção 1: Rodar com Docker Compose (Recomendado - Mais Fácil)
- **Docker** instalado ([instalar](https://docs.docker.com/install/))
- **Docker Compose** instalado ([instalar](https://docs.docker.com/compose/install/))

### Opção 2: Rodar Localmente com Maven
- **Java 17** ou superior ([verificar](https://www.oracle.com/java/technologies/downloads/#java17))
- **Maven 3.8+** (já incluído via `./mvnw`)
- **PostgreSQL 15+** instalado e rodando localmente

---

## 🚀 Como Rodar o Projeto

### **OPÇÃO 1: Docker Compose (Recomendado)**

Esta opção sobe automaticamente o banco de dados PostgreSQL + a API em containers.

#### Passo 1: Clonar o repositório
```bash
git clone <url-do-repositorio>
cd helpdesk-api
```

#### Passo 2: Subir os containers
```bash
docker-compose up --build
```

**Ou em background** (sem bloquear o terminal):
```bash
docker-compose up --build -d
```

#### Passo 3: Verificar se está funcionando
- **API estará disponível em**: http://localhost:8080
- **Banco de dados PostgreSQL**: localhost:5432

#### Passo 4: Ver logs (se rodando em background)
```bash
docker-compose logs -f api
```

#### Passo 5: Parar os containers
```bash
docker-compose down
```

**Remover volumes persistentes** (cuidado: deleta dados):
```bash
docker-compose down -v
```

---

### **OPÇÃO 2: Maven Local (Desenvolvimento)**

Esta opção roda a API localmente, mas o banco de dados pode estar em Docker ou localmente.

#### Passo 1: Clonar o repositório
```bash
git clone <url-do-repositorio>
cd helpdesk-api
```

#### Passo 2A: Subir APENAS o banco PostgreSQL via Docker
```bash
docker-compose up -d postgres-db
```

Aguarde alguns segundos para o banco inicializar:
```bash
docker-compose logs -f postgres-db
```

Você verá uma mensagem como: `ready to accept connections`

#### Passo 2B: OU usar PostgreSQL Local
Se você já tem PostgreSQL instalado localmente, certifique-se de que está rodando na porta 5432.

#### Passo 3: Definir variáveis de ambiente

Abra um terminal **na raiz do projeto** e exporte as variáveis (exemplo):

**Linux/macOS:**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/helpti_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export API_SECURITY_TOKEN_SECRET=8f4d3e2c1b9a0f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7c6b5a
export STORAGE_LOCATION=$(pwd)/uploads
```

**Windows (PowerShell):**
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/helpti_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:API_SECURITY_TOKEN_SECRET="8f4d3e2c1b9a0f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7c6b5a"
$env:STORAGE_LOCATION="$(Get-Location)\uploads"
```

#### Passo 4: Criar pasta de uploads
```bash
mkdir -p uploads
```

#### Passo 5: Rodar a aplicação
```bash
./mvnw spring-boot:run
```

Você verá na saída algo como:
```
Tomcat initialized with port 8080 (http)
...
Started HelpdeskApiApplication in X.XXX seconds
```

#### Passo 6: Verificar se está funcionando
- **API estará disponível em**: http://localhost:8080

#### Passo 7: Parar a aplicação
No terminal onde rodou `./mvnw spring-boot:run`, pressione `Ctrl+C`.

---

## 📦 Compilar e Empacotar (JAR)

Se quiser gerar um arquivo `.jar` executável:

```bash
./mvnw clean package -DskipTests
```

O arquivo será gerado em: `target/helpdesk-api-0.0.1-SNAPSHOT.jar`

**Executar o JAR:**
```bash
java -jar target/helpdesk-api-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Executar Testes

```bash
./mvnw test
```

---

## 📁 Estrutura do Projeto

```
helpdesk-api/
├── src/
│   ├── main/
│   │   └── java/br/com/helpTI/helpdeskapi/
│   │       ├── controller/          # Endpoints REST
│   │       ├── domain/              # Modelos de domínio (Entity)
│   │       ├── dto/                 # Data Transfer Objects
│   │       ├── repository/          # Acesso a dados (JPA)
│   │       ├── service/             # Lógica de negócio
│   │       ├── security/            # Autenticação e JWT
│   │       └── HelpdeskApiApplication.java
│   └── test/
│       └── resources/application.properties  # Propriedades de teste
├── docker-compose.yml               # Configuração Docker
├── Dockerfile                       # Build da imagem Docker
├── pom.xml                          # Dependências Maven
├── mvnw                             # Maven Wrapper (Linux/macOS)
├── mvnw.cmd                         # Maven Wrapper (Windows)
└── README.md                        # Este arquivo
```

---

## 🔧 Variáveis de Ambiente

A aplicação exige as seguintes variáveis (ou você pode criar um `src/main/resources/application.properties`):

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `SPRING_DATASOURCE_URL` | URL de conexão PostgreSQL | `jdbc:postgresql://localhost:5432/helpti_db` |
| `SPRING_DATASOURCE_USERNAME` | Usuário PostgreSQL | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Senha PostgreSQL | `postgres` |
| `API_SECURITY_TOKEN_SECRET` | Secret JWT para autenticação | `8f4d3e2c1b9a0f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7c6b5a` |
| `STORAGE_LOCATION` | Diretório para uploads de arquivos | `./uploads` |

---

## 📝 Criar arquivo application.properties (Alternativa)

Se preferir não usar variáveis de ambiente, crie um arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/helpti_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL15Dialect

# Segurança JWT
api.security.token.secret=8f4d3e2c1b9a0f7e6d5c4b3a2f1e0d9c8b7a6f5e4d3c2b1a0f9e8d7c6b5a

# Upload de arquivos
storage.location=./uploads

# Porta
server.port=8080
```

⚠️ **AVISO**: Não comite este arquivo no repositório com dados sensíveis!

---

## 🐛 Troubleshooting

### Erro: "Could not resolve placeholder 'api.security.token.secret'"
**Causa**: Variáveis de ambiente ou application.properties não foi criado.
**Solução**: 
- Linux/macOS: Execute `export SPRING_DATASOURCE_URL=...` conforme o Passo 3
- Windows: Use PowerShell ou crie um `application.properties`

### Erro: "Connection refused" na porta 5432
**Causa**: PostgreSQL não está rodando.
**Solução**: 
- Se usar Docker: `docker-compose up -d postgres-db`
- Se usar local: Verifique se o PostgreSQL está iniciado

### Erro: "Port 8080 already in use"
**Causa**: Outra aplicação está usando a porta 8080.
**Solução**: 
- Encontre o processo: `ss -ltnp | grep 8080` (Linux/macOS) ou `netstat -ano | findstr :8080` (Windows)
- Mate o processo ou mude a porta: `server.port=8081`

### Build falha com "class is duplicate"
**Causa**: Arquivos de teste duplicados.
**Solução**: Verifique se há múltiplas cópias de `HelpdeskApiApplicationTests.java`

---

## 📚 Tecnologias Utilizadas

- **Spring Boot 3.4.11** - Framework web
- **Spring Data JPA** - ORM
- **Spring Security** - Autenticação
- **PostgreSQL 15** - Banco de dados
- **JWT (com java-jwt)** - Autenticação stateless
- **Lombok** - Reduz boilerplate
- **Maven** - Gerenciador de dependências
- **Docker & Docker Compose** - Containerização

---

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório.

---

## 📄 Licença

Este projeto é fornecido como está. Veja o repositório para mais detalhes.
