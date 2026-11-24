package com.ConnectAPI.APIintegracao.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Lê as propriedades "connectapi.*" do application.yaml.
 * Cada record interno representa um bloco do arquivo.
 */
@ConfigurationProperties(prefix = "connectapi")
public record ConnectApiProperties(
        Viacep viacep,
        OpenMeteo openMeteo,
        Http http) {

    /** connectapi.viacep.* */
    public record Viacep(String baseUrl) {
    }

    /** connectapi.open-meteo.* */
    public record OpenMeteo(String geocodingUrl, String forecastUrl) {
    }

    /** connectapi.http.* */
    public record Http(Duration connectTimeout, Duration readTimeout) {
    }
}
