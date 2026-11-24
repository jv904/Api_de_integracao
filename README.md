# ConnectAPI

API REST em **Java 21 + Spring Boot 4** que funciona como intermediária entre o usuário e serviços externos da internet.

Em vez de conhecer e chamar várias APIs diferentes, o usuário utiliza apenas os endpoints da ConnectAPI:

- **Consulta de CEP** → integra com o [ViaCEP](https://viacep.com.br)
- **Clima atual de uma cidade** → integra com o geocoding e a previsão da [Open-Meteo](https://open-meteo.com)

A aplicação valida os dados recebidos, consome os serviços externos, organiza as respostas em JSON e devolve **mensagens de erro claras** quando algo dá errado, inclusive quando um serviço externo está fora do ar.

## Tecnologias

| | |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.2 (Spring MVC) |
| Cliente HTTP | `RestClient` com timeouts configuráveis |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, MockMvc, `MockRestServiceServer` |
| Build | Maven (wrapper incluso) |

## Como executar

Pré-requisito: **JDK 21** instalado. Não é preciso instalar o Maven.

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`. Não há autenticação nesta versão.

## Endpoints

| Método | Endpoint | O que faz |
|---|---|---|
| GET | `/api/cep/{cep}` | Consulta o endereço de um CEP |
| GET | `/api/weather?city={cidade}` | Consulta o clima atual de uma cidade |

### Consultar um CEP

```http
GET http://localhost:8080/api/cep/01001000
```

O CEP pode ser enviado com ou sem hífen (`01001000` ou `01001-000`).

```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP"
}
```

**O que acontece por trás:** a API valida se o CEP tem 8 números, consulta o ViaCEP, converte o JSON recebido em um objeto Java e devolve somente os campos relevantes.

### Consultar o clima

```http
GET http://localhost:8080/api/weather?city=Maceio
```

```json
{
  "cidade": "Maceió",
  "temperatura": 28.5,
  "umidade": 75,
  "velocidadeVento": 12.4
}
```

Temperatura em °C, umidade em % e vento em km/h. Os valores acima são ilustrativos.

**O que acontece por trás:** são feitas **duas** chamadas externas:

1. O nome da cidade é enviado ao serviço de geocoding da Open-Meteo, que devolve latitude e longitude.
2. Com as coordenadas, a API consulta a previsão atual e seleciona temperatura, umidade e vento.

## Tratamento de erros

Todos os erros seguem o mesmo formato:

```json
{ "erro": "mensagem explicando o problema" }
```

| Situação | Status | Mensagem |
|---|---|---|
| CEP sem 8 números (ex.: `/api/cep/123`) | 400 | O CEP deve conter 8 números |
| CEP com formato válido, mas inexistente | 404 | CEP não encontrado |
| ViaCEP fora do ar ou sem responder | 503 | O serviço de consulta de CEP está temporariamente indisponível |
| Cidade não informada | 400 | O nome da cidade deve ser informado |
| Cidade inexistente | 404 | Cidade não encontrada |
| Open-Meteo fora do ar ou sem responder | 503 | O serviço de previsão do tempo está temporariamente indisponível |
| Endpoint inexistente | 404 | Endpoint não encontrado |
| Erro inesperado | 500 | Ocorreu um erro interno. Tente novamente mais tarde |

Quando o CEP tem formato inválido, a API responde imediatamente, **sem** consultar o ViaCEP. Detalhes técnicos de falhas ficam apenas no log do servidor, nunca na resposta ao usuário.

## Documentação interativa (Swagger)

Com a aplicação rodando, acesse:

**http://localhost:8080/swagger-ui/index.html**

Abra um endpoint, clique em **Try it out**, ajuste os parâmetros (já vêm com exemplos) e clique em **Execute**.

## Configuração

Os endereços dos serviços externos e os timeouts ficam em `src/main/resources/application.yaml`:

```yaml
connectapi:
  viacep:
    base-url: https://viacep.com.br/ws
  open-meteo:
    geocoding-url: https://geocoding-api.open-meteo.com/v1
    forecast-url: https://api.open-meteo.com/v1
  http:
    connect-timeout: 3s
    read-timeout: 5s
```

Os timeouts garantem que, se um serviço externo travar, o usuário receba um `503` rapidamente em vez de esperar indefinidamente.

Para simular um serviço fora do ar, basta sobrescrever o endereço ao iniciar:

```bash
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--connectapi.viacep.base-url=http://localhost:9"
```

## Testes

```bash
.\mvnw.cmd test   # Windows
./mvnw test       # Linux / macOS
```

São 22 testes automatizados:

- **Services**: os serviços externos são simulados com `MockRestServiceServer`, sem depender da internet. Cobrem sucesso, dados inválidos, recurso não encontrado e falhas de comunicação (erro 500 e conexão recusada).
- **Controllers**: `@WebMvcTest` verifica rotas, status HTTP e o formato do JSON de erro.

## Estrutura do projeto

```
src/main/java/com/ConnectAPI/APIintegracao/
├── config/      RestClient por serviço externo, propriedades e informações do Swagger
├── cep/         Controller, service e modelos da consulta de CEP
├── weather/     Controller, service e modelos da consulta de clima
└── exception/   Exceções da aplicação e handler global de erros
```
