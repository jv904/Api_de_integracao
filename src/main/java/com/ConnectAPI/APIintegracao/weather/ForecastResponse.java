package com.ConnectAPI.APIintegracao.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resposta do serviço de previsão da Open-Meteo (uso interno).
 * A Open-Meteo usa nomes em snake_case; @JsonProperty faz a ponte para o Java.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record ForecastResponse(Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Current(
            @JsonProperty("temperature_2m") Double temperature,
            @JsonProperty("relative_humidity_2m") Integer humidity,
            @JsonProperty("wind_speed_10m") Double windSpeed) {
    }
}
