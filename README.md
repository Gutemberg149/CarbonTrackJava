# 🌿 CarbonTrack API

## 📌 Sobre o Projeto




**CarbonTrack API** é uma plataforma backend para gestão de créditos de carbono, 
permitindo o cadastro de propriedades rurais, cálculo automático de carbono sequestrado 
e emissão de créditos utilizando dados geoespaciais e imagens de satélite da **Planet Labs**.

### 🎯 Funcionalidades Principais

- ✅ Cadastro de usuários com autenticação JWT
- ✅ Gestão de propriedades rurais (CRUD completo)
- ✅ Cálculo de área em hectares via geometria (WKT/GeoJSON)
- ✅ Integração com **Planet Labs API** para dados reais de biomassa
- ✅ Cálculo de estoque de carbono por tempo de posse e área
- ✅ Emissão automática de créditos de carbono
- ✅ Documentação interativa via Swagger/OpenAPI
- ✅ HATEOAS para navegação entre recursos
- ✅ Múltiplos perfis de ambiente (dev, prod, test, local, mock, docker)

---

## 🔗 Links do Projeto
| Recurso                              | Link                                           |
|--------------------------------------|------------------------------------------------|
| 🚀 **Deploy da API**                 | https://carbontrack-api.onrender.com  |
| 🎥 **Vídeo de Apresentação Codigos** | https://vimeo.com/1199527877?share=copy&fl=sv&fe=ci |
| 🎥 **Vídeo do DashBoard Mobile**     | https://vimeo.com/1193015270?share=copy&fl=sv&fe=ci |
| 🎥 **Vídeo Pitch**                   | https://www.youtube.com/watch?v=qoZpOKsNDUI |
| 📖 **Documentação da API (Swagger)** | https://carbontrack-api.onrender.com/swagger-ui.html |
| 🐙 **Repositório GitHub**            | https://github.com/Gutemberg149/CarbonTrackJava |

---

## 🛠️ Tecnologias Utilizadas
- **Java 17** - Linguagem de programação
- **Spring Boot 3.2** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Spring HATEOAS** - Hypermedia RESTful
- **SpringDoc OpenAPI** - Documentação automática

### Banco de Dados
- **Oracle Database** - Banco de dados relacional (produção)
- **H2 Database** - Banco em memória para testes/desenvolvimento

### Integrações
- **Planet Labs API** - Dados de satélite e biomassa
- **JWT (JSON Web Token)** - Tokens de autenticação
- **RestTemplate** - Consumo de APIs externas

### Geoprocessamento
- **JTS (Java Topology Suite)** - Manipulação de geometrias
- **WKT (Well-Known Text)** - Representação de geometrias
- **GeoJSON** - Troca de dados geoespaciais

### Utilitários
- **Lombok** - Redução de boilerplate code
- **Jackson** - Serialização/deserialização JSON
- **SLF4J + Logback** - Logging estruturado

### DevOps
- **Maven** - Gerenciamento de dependências
- **Git** - Controle de versão
- **Render / Fly.io** - Deploy na nuvem *(ajustar conforme usado)*

---

## 🚀 Instruções de Execução
- **Primeiro obter authorização** - 

POSTMAN

### Login
**POST** `https://carbontrackjava.onrender.com/auth/login`
- **AuthType**: No Auth

- **Body**:
json
{
  "email": "farmer@carbontrack.com",
  "senha": "123456"
}

Depois Copiar o token 
AuthType => Beared Token
Colar o token na caixa ao lado


- **CRUDS** -

USUARIOS
  https://carbontrackjava.onrender.com/usuarios
POST:
{
"nome": "Giovane",
"email": "giovane@email.com",
"senha": "123456"
}

GET:
Todos os usarios
https://carbontrackjava.onrender.com/usuarios

GET:
O usario por ID
https://carbontrackjava.onrender.com/usuarios/{ID}

PUT e DELETE:
https://carbontrackjava.onrender.com/usuarios/{ID}


PROPRIEDADE
https://carbontrackjava.onrender.com/propriedades
POST:
{
"nome": "Fazenda Boa Vista",
"endereco": "Rodovia BR-101, Km 50",
"cidade": "São Paulo",
"estado": "SP",
"cep": "01234567",
"anoAquisicao": 2020,
"mesAquisicao": 5,
"geometriaWkt": "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6433 -23.5605, -46.6433 -23.5505, -46.6333 -23.5505))",
"usuarioId": "d6cf5622-1548-4d97-8ad9-9facf8d0c225" => Aqui tem que pegar um ID de usuario existente
}

- **Caso precise criar novas propriedades, vou deixar mais dois exemplos de geometriaWkt:** -
"geometriaWkt": "POLYGON((-60.0000 -3.0000, -60.0000 -3.1000, -59.9000 -3.1000, -59.9000 -3.0000, -60.0000 -3.0000))"
"geometriaWkt": "POLYGON((-48.5000 -16.0000, -48.5000 -16.2000, -48.2000 -16.2000, -48.2000 -16.0000, -48.5000 -16.0000))"

GET:
Todos as propriedades
https://carbontrackjava.onrender.com/propriedades

GET:
A propriedade por ID
https://carbontrackjava.onrender.com/propriedades/{ID}

PUT e DELETE:
https://carbontrackjava.onrender.com/propriedades/{ID}

CREDITOS

GET:
Todos os creditos
https://carbontrackjava.onrender.com/creditos

GET:
O creditos por ID
https://carbontrackjava.onrender.com/creditos/{ID}

DELETE:
https://carbontrackjava.onrender.com/creditos/{ID}


## 👥 Integrantes do Grupo

> ** Gutemberg Rocha - RM: 562267
> ** Erik Naoki - RM: 565771
> ** Juliana - RM: 561171
> ** Gustavo - RM: 561650
> ** João - RM: 564361

