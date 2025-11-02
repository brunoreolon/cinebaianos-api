# 🎬 Cinebaianos API

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)
![Flyway](https://img.shields.io/badge/Flyway-8.6-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Maven](https://img.shields.io/badge/Maven-3.9.4-red)

A **Cinebaianos API** é uma aplicação backend construída em **Java com Spring Boot**, responsável por gerenciar informações de filmes, votos de usuários e autenticação via JWT.
A API se integra com o serviço externo **TMDb (The Movie Database)** para fornecer informações detalhadas sobre filmes.

---

## 🛠 Tecnologias utilizadas

* **Java 21** – Linguagem de programação principal.
* **Spring Boot 3** – Framework para criação de APIs REST.
* **Spring Security + JWT** – Autenticação e autorização.
* **Spring Data JPA + Hibernate** – Persistência de dados com PostgreSQL.
* **Flyway** – Gerenciamento de versionamento de banco de dados.
* **PostgreSQL** – Banco de dados relacional.
* **TMDb API** – API externa para buscar informações de filmes.
* **Maven** – Gerenciamento de dependências e build.
* **Docker** – Containerização da aplicação.

---

## 🌐 Funcionalidades principais

* Cadastro e autenticação de usuários.
* Adicionar filmes pelo ID TMDb ou pelo título/ano.
* Listar filmes e detalhes de cada filme.
* Registrar votos de usuários.
* Gerar rankings de filmes com base nos votos.
* Consulta a filmes via **TMDb API**.
* Configuração de JWT para autenticação segura.
* Suporte a perfis de ambiente (`dev` e `prod`) com variáveis externas.

---

## 🎯 Principais endpoints da API

### 1️⃣ Adicionar um filme pelo ID

* **POST** `/api/movies`
* **Descrição:** Adiciona um filme pelo ID do TMDb escolhido pelo usuário.
* **Body:**

```json
{
  "movie": {
    "id": 120
  },
  "chooser": {
    "discordId": "000000000000000001"
  }
}
```

💡 Obs: O campo `vote` é opcional.

---

### 2️⃣ Buscar e adicionar filmes por título e ano

* **POST** `/api/movies/candidates`
* **Descrição:** Consulta filmes pelo título e ano. Salva o filme se encontrado ou retorna erro se houver múltiplas correspondências.
* **Body:**

```json
{
  "title": "O Senhor dos Anéis",
  "year": 2003,
  "chooser": {
    "discordId": "000000000000000001"
  },
  "vote": {
    "id": 1
  }
}
```

💡 Obs: O campo `vote` é opcional.

---

### 3️⃣ Votar em um filme

* **POST** `/api/votes`
* **Descrição:** Permite que um usuário registre um voto para um filme.
* **Body:**

```json
{
  "voter": {
    "discordId": "000000000000000001"
  },
  "movie": {
    "id": 48
  },
  "vote": 1
}
```

---

### 4️⃣ Listar votos de um filme

* **GET** `/api/votes/{movieId}/votes`
* **Descrição:** Retorna todos os votos que um filme específico recebeu.
* **Exemplo de resposta:**

```json
{
  "movie": {
    "id": 48,
    "title": "O Senhor dos Anéis: A Sociedade do Anel",
    "genre": "Aventura",
    "year": "2001",
    "tmdbId": "120",
    "dateAdded": "2025-11-02T01:43:59.6786",
    "posterPath": "https://image.tmdb.org/t/p/original/p9tiZkjs5A1XdD3m3E15ij9IxCV.jpg",
    "chooser": {
      "discordId": "000000000000000001",
      "name": "Bruno Reolon1"
    }
  },
  "votes": [
    {
      "voter": {
        "discordId": "000000000000000001",
        "name": "Bruno Reolon"
      },
      "vote": {
        "id": 1,
        "description": "Da Hora"
      }
    },
    {
      "voter": {
        "discordId": "000000000000000002",
        "name": "Aʟᴍɪʀ"
      },
      "vote": {
        "id": 1,
        "description": "Da Hora"
      }
    }
  ]
}
```
---

## 📦 Docker

### 1️⃣ Criar a imagem Docker

```bash
docker build -t cinebaianos-api -f docker/Dockerfile .
```

### 2️⃣ Rodar o container

```bash
docker run -d --name cinebaianos-api -p 8080:8080 cinebaianos-api
```

---

## 🔧 Variáveis de ambiente

```bash
# =======================
# Banco de dados
# =======================
DB_URL=jdbc:postgresql://localhost:5432/cinebaianosdb
DB_USER=postgres
DB_PASSWORD=

# =======================
# TMDb API
# =======================
BASE_URL=https://api.themoviedb.org/3
API_KEY=
POSTER_PATH=https://image.tmdb.org/t/p/original

# =======================
# E-mail
# =======================
MAIL_HOST=
MAIL_PORT=
MAIL_USERNAME=
MAIL_PASSWORD=

# =======================
# JWT
# =======================
JWT_KEYSTORE_PATH=jwt.jks
JWT_KEYSTORE_PASSWORD=
JWT_KEY_PASSWORD=
JWT_ALIAS=jwt
```