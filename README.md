<div align="center">

# 🐾 ByteCare 

**API REST para gestão da saúde do PET**

</div>

---

## 📋 Índice

- [Descrição e Solução](#-descrição-e-solução)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Cronograma de Desenvolvimento](#-cronograma-de-desenvolvimento)
- [Endpoints da API](#-endpoints-da-api)
- [Exemplos de Requisições](#-exemplos-de-requisições)

---

## 📖 Descrição e Solução

O **ByteCare** é uma API REST, desenvolvida em **Java com Spring Boot**, projetada 
para otimizar a gestão da saúde do PET e o acompanhamento domiciliar dos animais. 
A solução centraliza o histórico clínico e de rotina, oferecendo um sistema eficiente para:

- Registro de **tratamentos terapêuticos**
- Gestão de **ações preventivas**
- Acompanhamento de **cuidados diários**

O objetivo é garantir a integridade dos dados e facilitar o acesso a informações essenciais para a 
saúde pet.

---

## ✨ Funcionalidades

| Módulo | Descrição |
|---|---|
| 🐶 **Gestão de Animais** | Cadastro e controle de perfil dos pacientes |
| 💉 **Controle Preventivo** | Agendamento de vacinas e serviços preventivos |
| 🏃 **Bem-Estar** | Monitoramento de atividades físicas e comportamentais |
| 💊 **Terapêutica** | Registro detalhado de tratamentos e medicamentos |
| 🛁 **Cuidados** | Histórico de procedimentos de higiene e cuidados pontuais |

---

## 🛠️ Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA / Hibernate**
- **Maven**
- **PostgreSQL**
- **Postman** (testes de endpoints)

---

## 📅 Cronograma de Desenvolvimento

> Cada desenvolvedor é responsável pelo módulo indicado. As datas marcam o prazo de entrega de cada atividade.

| # | Atividade | Responsável | Data Prevista | Status |
|---|---|---|---|---|
| 1 | Configuração do projeto (Spring Boot, dependências, banco) | **Gutemberg** | 05/05/2026 | ✅ Concluído |
| 2 | Modelagem das entidades e migrations do banco | **Erick** | 08/05/2026 | ✅ Concluído |
| 3 | CRUD de Animais (entidade, repositório, service, controller) | **Gustavo** | 12/05/2026 | ✅ Concluído |
| 4 | CRUD de Ações Preventivas | **Julia** | 14/05/2026 | ✅ Concluído |
| 5 | CRUD de Atividades de Bem-Estar | **João** | 16/05/2026 | ✅ Concluído |
| 6 | CRUD de Registros de Cuidado | **Erick** | 18/05/2026 | ✅ Concluído |
| 7 | CRUD de Tratamentos Terapêuticos | **Gutemberg** | 19/05/2026 | ✅ Concluído |
| 8 | Testes dos endpoints no Postman e ajustes | **Gustavo** | 20/05/2026 | ✅ Concluído |
| 9 | Documentação (README, exportação Postman) | **Julia** | 21/05/2026 | ✅ Concluído |
| 10 | Revisão geral, correções e entrega final | **João** | 22/05/2026 | ✅ Concluído |

---

## 🔗 Endpoints da API

> **Base URL:** `http://localhost:8080`

### 🐶 Animais — `/api/animais`

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/animais` | Lista todos os animais |
| `GET` | `/api/animais/{ID}` | Busca animal por ID |
| `POST` | `/api/animais` | Cadastra novo animal |
| `PUT` | `/api/animais/{ID}` | Atualiza animal |
| `DELETE` | `/api/animais/{ID}` | Remove animal |

---

### 🐶 POST `/api/animais`

```json
{
  "nome": "Bau",
  "tipo": "CACHORRO",
  "raca": "Rusk",
  "dataNascimento": "2020-10-20",
  "observacaoGeral": "Amigo de mais",
  "ativo": true
}
```

> **ENUM `tipo`:** `CACHORRO` | `GATO`

---

### 💉 Ações Preventivas — `/api/acoes-preventivas`

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/acoes-preventivas` | Lista todas as ações |
| `GET` | `/api/acoes-preventivas/{ID}` | Busca ação por ID |
| `POST` | `/api/acoes-preventivas` | Registra nova ação |
| `PUT` | `/api/acoes-preventivas/{ID}` | Atualiza ação |
| `DELETE` | `/api/acoes-preventivas/{ID}` | Remove ação |


### 💉 POST `/api/acoes-preventivas`

```json
{
  "nomeServico": "Vacinação",
  "descricao": "V10",
  "proximoPrevisto": "2026-05-20",
  "observacao": "Manter repouso",
  "categoria": "BEM_ESTAR",
  "dataHoraRegistro": "2026-05-22T13:24:53.696175",
  "idAnimal": "674f8fb1-b57b-49b8-b8d2-224570ba1ace"
}
```

> **ENUM `categoria`:** `BEM_ESTAR` | `PREVENTIVO` | `TERAPEUTICO`

---

### 🏃 Atividades de Bem-Estar — `/api/atividades-bem-estar`

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/atividades-bem-estar` | Lista todas as atividades |
| `GET` | `/api/atividades-bem-estar/{ID}` | Busca atividade por ID |
| `POST` | `/api/atividades-bem-estar` | Registra nova atividade |
| `PUT` | `/api/atividades-bem-estar/{ID}` | Atualiza atividade |
| `DELETE` | `/api/atividades-bem-estar/{ID}` | Remove atividade |

### 🏃 POST `/api/atividades-bem-estar`

```json
{
  "idAnimal": "674f8fb1-b57b-49b8-b8d2-224570ba1ace",
  "nomeAtividade": "Passeio no parque",
  "dataAtividade": "2025-02-23",
  "observacaoAtividade": "Manter ritmo leve",
  "categoria": "Físico",
  "duracao": "40",
  "observacao": "Cachorro sente muita sede."
}
```


### 🛁 Registros de Cuidado — `/api/registros-cuidado`
### Para listar todos os registros tem a palavra "listar no final da url.
| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/registros-cuidado/listar` | Lista todos os registros |
| `GET` | `/api/registros-cuidado/{ID}` | Busca registro por ID |
| `POST` | `/api/registros-cuidado` | Cria novo registro |
| `PUT` | `/api/atividades-bem-estar/{ID}` | Atualiza registro |
| `DELETE` | `/api/atividades-bem-estar/{ID}` | Remove registro |

---

### 🛁 POST `/api/registros-cuidado`

```json
{
  "categoria": "BEM_ESTAR",
  "idAnimal": "674f8fb1-b57b-49b8-b8d2-224570ba1ace",
  "dataHoraRegistro": "20-05-2026",
  "observacao": "Com sabonete neutro"
}

```

> **ENUM `categoria`:** `BEM_ESTAR` | `PREVENTIVO` | `TERAPEUTICO`

---

### 💊 Tratamentos Terapêuticos — `/api/tratamentos-terapeuticos`

| Método | URL | Descrição |
|---|---|---|
| `GET` | `/api/tratamentos-terapeuticos` | Lista todos os tratamentos |
| `GET` | `/api/tratamentos-terapeuticos/{ID}` | Busca tratamento por ID |
| `POST` | `/api/tratamentos-terapeuticos` | Registra novo tratamento |
| `PUT` | `/api/tratamentos-terapeuticos/{ID}` | Atualiza tratamento |
| `DELETE` | `/api/tratamentos-terapeuticos/{ID}` | Remove tratamento |

### 💊 POST `/api/tratamentos-terapeuticos`

```json
{
  "nomeMedicamento": "Xa mate",
  "dosagem": "00mg",
  "frequencia": "10/10h",
  "duracaoTratamento": "5 meses",
  "observacao": "Energia",
  "categoria": "cha",
  "idAnimal": "674f8fb1-b57b-49b8-b8d2-224570ba1ace"
}
```

---

## 📬 Exemplos de Requisições

> ⚠️ Substitua `{ID}` por um UUID válido já cadastrado no banco de dados.

---

<div align="center">

Feito pela equipe **ByteCare**

**Gutemberg Rocha · Erik Naoki  · Gustavo Arthur  · Juliana da Silva · João Henrique**

</div>
