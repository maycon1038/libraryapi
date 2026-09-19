# Library API

API REST para gerenciamento de autores, livros, usuários e clientes OAuth2, construída com Spring Boot, Spring Data JPA, PostgreSQL e Spring Security.

## Requisitos

- Java 21
- Docker e Docker Compose (ou Docker Engine)
- Git

## Instalação

Entre no diretório da aplicação:

```powershell
cd libraryapi
```

Suba o PostgreSQL:

```powershell
docker network create library-network
docker run --name librarydb `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_DB=library `
  -p 5432:5432 `
  -d --network library-network postgres:16.3
```

Se o container `librarydb` já existir, use:

```powershell
docker start librarydb
```

Inicialize um banco vazio com o script [`comandos-sql.txt`](comandos-sql.txt):

```powershell
Get-Content .\comandos-sql.txt -Raw |
  docker exec -i librarydb psql -U postgres -d library -v ON_ERROR_STOP=1
```

O script cria as tabelas `usuario`, `autor`, `livro` e `client`, além dos dados iniciais.

## Execução com Docker

Os comandos abaixo usam PowerShell no Windows. Execute-os a partir da raiz do projeto.

### 1. Criar a rede Docker

```powershell
docker network create library-network
```

Se a rede já existir, o Docker informará que ela já está criada. Nesse caso, continue para o próximo passo.

### 2. Criar e iniciar o PostgreSQL

```powershell
docker run --name librarydb `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_USER=postgres `
  -e POSTGRES_DB=library `
  -p 5432:5432 `
  -d `
  --network library-network `
  postgres:16.3
```

Se o container já existir, inicie-o:

```powershell
docker start librarydb
```

Se o container existir, mas não estiver conectado à rede da aplicação:

```powershell
docker network connect library-network librarydb
```

Verifique o estado do banco:

```powershell
docker ps
docker exec librarydb pg_isready -U postgres -d library
```

Inicialize as tabelas e os dados:

```powershell
Get-Content .\comandos-sql.txt -Raw |
  docker exec -i librarydb psql -U postgres -d library -v ON_ERROR_STOP=1
```

### 3. Construir a imagem da API

O `Dockerfile` compila o projeto com Maven e cria uma imagem baseada no Java 21:

```powershell
docker build -t libraryapi .
```

Listar a imagem criada:

```powershell
docker images libraryapi
```

### 4. Subir o container da API

Dentro de um container, `localhost` aponta para o próprio container. Por isso, a API deve usar `librarydb` como hostname do PostgreSQL:

```powershell
docker run --name libraryapi `
  --network library-network `
  -e DATASOURCE_URL="jdbc:postgresql://librarydb:5432/library" `
  -e DATASOURCE_USERNAME="postgres" `
  -e DATASOURCE_PASSWORD="postgres" `
  -p 8080:8080 `
  -p 9090:9090 `
  libraryapi
```

Em outro terminal, acompanhe os logs:

```powershell
docker logs -f libraryapi
```

A aplicação iniciou corretamente quando aparecer uma mensagem semelhante a:

```text
Started Application
```

A API ficará disponível em `http://localhost:8080` e o Actuator em `http://localhost:9090/actuator`.

### 5. Comandos úteis

Ver todos os containers, inclusive os parados:

```powershell
docker ps -a
```

Verificar a rede e os containers conectados:

```powershell
docker network inspect library-network
```

Parar e iniciar a API:

```powershell
docker stop libraryapi
docker start libraryapi
```

Reiniciar a API:

```powershell
docker restart libraryapi
```

Abrir um shell no container da API:

```powershell
docker exec -it libraryapi sh
```

Remover somente o container da API para recriá-lo:

```powershell
docker rm -f libraryapi
```

Remover a imagem local:

```powershell
docker rmi libraryapi
```

> Se o nome `libraryapi` já estiver em uso, remova o container antigo com `docker rm -f libraryapi` ou use outro nome no parâmetro `--name`. Os dados do PostgreSQL permanecem no container `librarydb`.

### 6. Usar Docker Compose

O arquivo [`docker-compose.yml`](docker-compose.yml) sobe os três serviços do projeto:

- `librarydb`: PostgreSQL na porta `5432`.
- `pgadmin4`: pgAdmin na porta `15432`.
- `libraryapi`: API nas portas `8080` e `9090`.

O Compose cria volumes nomeados para manter os dados do PostgreSQL e do pgAdmin. O script `comandos-sql.txt` é executado automaticamente pelo PostgreSQL somente quando o volume do banco é criado pela primeira vez.

Se você já tiver containers criados manualmente com `docker run`, pare-os antes de executar o Compose para liberar as portas `5432`, `8080`, `9090` e `15432`:

```powershell
docker stop libraryapi librarydb pgadmin4
```

Não remova o container antigo do PostgreSQL sem verificar onde os dados estão armazenados. O Compose usa o volume `libraryapi_librarydb-data`, que é independente do armazenamento do container criado manualmente.

Subir todos os serviços em segundo plano:

```powershell
docker compose up -d
```

O comando também constrói a imagem local `libraryapi:latest` quando necessário.

Verificar o estado dos serviços:

```powershell
docker compose ps
```

Acompanhar os logs da API:

```powershell
docker compose logs -f libraryapi
```

Acompanhar os logs de todos os serviços:

```powershell
docker compose logs -f
```

Parar temporariamente os serviços sem removê-los:

```powershell
docker compose stop
```

Iniciar novamente os serviços que foram parados:

```powershell
docker compose start
```

Recriar a imagem e subir os serviços após alterações no código:

```powershell
docker compose up -d --build
```

Parar e remover os containers e a rede do Compose, mantendo os volumes:

```powershell
docker compose down
```

Depois de `docker compose down`, use `docker compose up -d` para criar e iniciar os containers novamente. O comando `docker compose start` só funciona enquanto os containers ainda existem.

Remover também os volumes e todos os dados persistidos do PostgreSQL e do pgAdmin:

```powershell
docker compose down -v
```

Use `docker compose down -v` somente quando quiser reinicializar o banco do zero. Nesse caso, o `comandos-sql.txt` será executado novamente no próximo `docker compose up -d`.

Testar a API depois da inicialização:

```powershell
curl.exe http://localhost:8080/v3/api-docs
curl.exe http://localhost:9090/actuator
```

## Configuração

Por padrão, a aplicação usa:

```text
DATASOURCE_URL=jdbc:postgresql://localhost:5432/library
DATASOURCE_USERNAME=postgres
DATASOURCE_PASSWORD=postgres
```

Para alterar esses valores no PowerShell:

```powershell
$env:DATASOURCE_URL = "jdbc:postgresql://localhost:5432/library"
$env:DATASOURCE_USERNAME = "postgres"
$env:DATASOURCE_PASSWORD = "postgres"
```

As variáveis `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` só são necessárias para habilitar o login social do Google.

## Start da aplicação

No Windows:

```powershell
.\mvnw spring-boot:run
```

Ou gere o `.jar` e execute:

```powershell
.\mvnw clean package
java -jar .\target\libraryapi-0.0.1-SNAPSHOT.jar
```

A API ficará disponível em:

```text
http://localhost:8080
```

Documentação OpenAPI:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

Actuator:

```text
http://localhost:8080/actuator
```

## Postman

Crie um environment no Postman com:

| Variável | Valor |
|---|---|
| `base_url` | `http://localhost:8080` |
| `access_token` | token JWT obtido no OAuth2 |
| `autor_id` | UUID retornado ao cadastrar um autor |
| `livro_id` | UUID retornado ao cadastrar um livro |

Para os endpoints protegidos, use a aba **Authorization** com:

```text
Type: Bearer Token
Token: {{access_token}}
```

### 1. Cadastrar usuário

Endpoint público para criar um usuário:

```http
POST {{base_url}}/usuarios
Content-Type: application/json
```

Body:

```json
{
  "login": "operador",
  "email": "operador@email.com",
  "senha": "senha123",
  "roles": ["OPERADOR"]
}
```

Para um usuário administrador, utilize a role `GERENTE`.

### 2. Obter token OAuth2

O servidor expõe o endpoint:

```http
POST {{base_url}}/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <client_id>:<client_secret>
```

Para utilizar o fluxo `client_credentials`, configure no body:

```text
grant_type=client_credentials
scope=GERENTE
```

Para testar localmente, o cliente `client-local` usa a senha `senha123`:

```text
Client ID: client-local
Client Secret: senha123
```

No PowerShell, obtenha o token com:

```powershell
curl.exe --location --request POST "http://localhost:8080/oauth2/token" `
  --header "Content-Type: application/x-www-form-urlencoded" `
  --user "client-local:senha123" `
  --data-urlencode "grant_type=client_credentials" `
  --data-urlencode "scope=GERENTE"
```

O parâmetro `--user` envia automaticamente a autenticação Basic. O cliente `client-local` precisa existir na tabela `client`; se necessário, cadastre-o com:

```powershell
$hash = '$2a$10$dpOYlSn9Lu1rh6WDhUIhiOiFDfnHiL79TFmyYmMgHFvebI4HpUSkC'
docker exec librarydb psql -U postgres -d library -c "INSERT INTO client (id, client_id, client_secret, redirect_uri, scope) VALUES (uuid_generate_v4(), 'client-local', '$hash', 'http://localhost:8080/authorized', 'GERENTE');"
```

No Postman, a forma equivalente é:

1. Aba **Authorization**.
2. Type: **Basic Auth**.
3. Username: `client-local`.
4. Password: `senha123`.
5. Aba **Body** → **x-www-form-urlencoded**.
6. Adicione `grant_type` com valor `client_credentials`.
7. Adicione `scope` com valor `GERENTE`.

Copie o campo `access_token` da resposta para a variável `access_token`.

> O segredo armazenado no banco é BCrypt e não pode ser lido de volta. O hash acima corresponde à senha `senha123`. No PowerShell, use aspas simples na variável `$hash` para impedir que os caracteres `$` do BCrypt sejam interpretados.

### 3. Cadastrar autor

```http
POST {{base_url}}/autores
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

Body:

```json
{
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileira"
}
```

Resposta esperada: `201 Created`. O UUID estará no header `Location`.

### 4. Consultar autores

Pesquisar todos:

```http
GET {{base_url}}/autores
Authorization: Bearer {{access_token}}
```

Pesquisar por nome ou nacionalidade:

```http
GET {{base_url}}/autores?nome=Machado
Authorization: Bearer {{access_token}}
```

Obter um autor:

```http
GET {{base_url}}/autores/{{autor_id}}
Authorization: Bearer {{access_token}}
```

Atualizar:

```http
PUT {{base_url}}/autores/{{autor_id}}
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

```json
{
  "nome": "Machado de Assis",
  "dataNascimento": "1839-06-21",
  "nacionalidade": "Brasileira"
}
```

Excluir:

```http
DELETE {{base_url}}/autores/{{autor_id}}
Authorization: Bearer {{access_token}}
```

### 5. Cadastrar livro

```http
POST {{base_url}}/livros
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

Body:

```json
{
  "isbn": "978-8535914849",
  "titulo": "Dom Casmurro",
  "dataPublicacao": "1899-01-01",
  "genero": "ROMANCE",
  "preco": 39.90,
  "idAutor": "{{autor_id}}"
}
```

Gêneros aceitos:

```text
FICCAO, FANTASIA, MISTERIO, ROMANCE, BIOGRAFIA, CIENCIA
```

### 6. Consultar livros

Pesquisar com paginação:

```http
GET {{base_url}}/livros?pagina=0&tamanho-pagina=10
Authorization: Bearer {{access_token}}
```

Filtros disponíveis:

```text
isbn
titulo
nome-autor
genero
ano-publicacao
pagina
tamanho-pagina
```

Exemplo:

```http
GET {{base_url}}/livros?genero=ROMANCE&ano-publicacao=1899
Authorization: Bearer {{access_token}}
```

Obter um livro:

```http
GET {{base_url}}/livros/{{livro_id}}
Authorization: Bearer {{access_token}}
```

Atualizar:

```http
PUT {{base_url}}/livros/{{livro_id}}
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

```json
{
  "isbn": "978-8535914849",
  "titulo": "Dom Casmurro - Edição atualizada",
  "dataPublicacao": "1899-01-01",
  "genero": "ROMANCE",
  "preco": 44.90,
  "idAutor": "{{autor_id}}"
}
```

Excluir:

```http
DELETE {{base_url}}/livros/{{livro_id}}
Authorization: Bearer {{access_token}}
```

## Códigos HTTP comuns

| Código | Significado |
|---|---|
| `200` | Consulta realizada com sucesso |
| `201` | Recurso criado |
| `204` | Atualização ou exclusão realizada |
| `400` | Requisição inválida |
| `401` | Token ausente ou inválido |
| `403` | Usuário sem permissão |
| `404` | Recurso não encontrado |
| `409` | Conflito, como ISBN ou autor duplicado |
| `422` | Erro de validação |

## Testes

Execute a suíte de testes com:

```powershell
.\mvnw test
```

## Para obter um token OAuth2:
```http
curl.exe --location --request POST "http://localhost:8080/oauth2/token" `
  --header "Content-Type: application/x-www-form-urlencoded" `
--header "Authorization: Basic Y2xpZW50LWxvY2FsOnNlbmhhMTIz" `
  --data-urlencode "grant_type=client_credentials" `
--data-urlencode "scope=GERENTE"
```