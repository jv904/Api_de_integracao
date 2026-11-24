package com.ConnectAPI.APIintegracao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Informações exibidas no topo da documentação Swagger
 * (http://localhost:8080/swagger-ui/index.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI connectApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectAPI")
                        .version("1.0.0")
                        .description("""
                                API de integração com serviços externos.

                                Funciona como intermediária entre você e outros serviços da internet:
                                - **CEP** — consulta endereços pelo CEP (ViaCEP)
                                - **Clima** — consulta temperatura, umidade e vento de uma cidade (Open-Meteo)

                                Todas as respostas são em JSON. Em caso de erro, o corpo segue o formato
                                `{"erro": "mensagem"}`.
                                """));
    }
}
