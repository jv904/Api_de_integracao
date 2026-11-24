package com.ConnectAPI.APIintegracao.weather;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ConnectAPI.APIintegracao.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Clima", description = "Consulta do clima atual de uma cidade (via Open-Meteo)")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    @Operation(summary = "Consulta clima",
            description = "Localiza a cidade pelo nome e retorna temperatura, umidade e velocidade do vento atuais")
    @ApiResponse(responseCode = "200", description = "Clima encontrado")
    @ApiResponse(responseCode = "400", description = "Cidade não informada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Cidade não encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Open-Meteo indisponível",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public WeatherResponse findByCity(
            @Parameter(description = "Nome da cidade", example = "Maceio")
            @RequestParam(required = false) String city) {
        // required = false: a validação (e a mensagem de erro) fica no service
        return weatherService.findByCity(city);
    }
}
