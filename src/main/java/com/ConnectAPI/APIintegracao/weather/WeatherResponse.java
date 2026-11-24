package com.ConnectAPI.APIintegracao.weather;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Clima atual devolvido ao usuário da ConnectAPI.
 */
@Schema(description = "Condições meteorológicas atuais da cidade")
public record WeatherResponse(
        @Schema(description = "Nome oficial da cidade", example = "Maceió") String cidade,
        @Schema(description = "Temperatura em °C", example = "28.5") Double temperatura,
        @Schema(description = "Umidade relativa do ar em %", example = "75") Integer umidade,
        @Schema(description = "Velocidade do vento em km/h", example = "12.4") Double velocidadeVento) {
}
