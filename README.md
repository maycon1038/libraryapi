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

No Postman, a forma equivalente é:

1. Aba **Authorization**.
2. Type: **Basic Auth**.
3. Username: `client-production`.
4. Password: o segredo configurado para esse cliente.
5. Aba **Body** → **x-www-form-urlencoded**.
6. Adicione `grant_type` com valor `client_credentials`.
7. Adicione `scope` com valor `GERENTE`.

Copie o campo `access_token` da resposta para a variável `access_token`.

> O segredo armazenado no banco é BCrypt e não pode ser lido de volta. Se o cliente inicial não tiver um segredo conhecido, registre um novo cliente pelo endpoint `POST /clients` após autenticar um usuário com role `GERENTE`, ou substitua o hash no script SQL por um hash BCrypt conhecido antes de inicializar o banco.

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
