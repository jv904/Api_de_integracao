package com.ConnectAPI.APIintegracao.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Cria um RestClient para cada serviço externo.
 * Todos usam os mesmos tempos limite, mas cada um tem seu próprio endereço base.
 */
@Configuration
@EnableConfigurationProperties(ConnectApiProperties.class)
public class RestClientConfig {

    private final RestClient.Builder builder;
    private final ConnectApiProperties properties;

    public RestClientConfig(RestClient.Builder builder, ConnectApiProperties properties) {
        this.builder = builder;
        this.properties = properties;
    }

    @Bean
    RestClient viaCepClient() {
        return createClient(properties.viacep().baseUrl());
    }

    @Bean
    RestClient geocodingClient() {
        return createClient(properties.openMeteo().geocodingUrl());
    }

    @Bean
    RestClient forecastClient() {
        return createClient(properties.openMeteo().forecastUrl());
    }

    private RestClient createClient(String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.http().connectTimeout());
        requestFactory.setReadTimeout(properties.http().readTimeout());

        // clone() evita que a configuração de um cliente "vaze" para o outro
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
