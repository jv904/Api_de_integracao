package com.ConnectAPI.APIintegracao.weather;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ConnectAPI.APIintegracao.exception.ExternalServiceUnavailableException;
import com.ConnectAPI.APIintegracao.exception.InvalidInputException;
import com.ConnectAPI.APIintegracao.exception.ResourceNotFoundException;

@Service
public class WeatherService {

    private static final String UNAVAILABLE_MESSAGE =
            "O serviço de previsão do tempo está temporariamente indisponível";

    private final RestClient geocodingClient;
    private final RestClient forecastClient;

    public WeatherService(@Qualifier("geocodingClient") RestClient geocodingClient,
                          @Qualifier("forecastClient") RestClient forecastClient) {
        this.geocodingClient = geocodingClient;
        this.forecastClient = forecastClient;
    }

    public WeatherResponse findByCity(String city) {
        // 1. Validar o nome da cidade
        if (city == null || city.isBlank()) {
            throw new InvalidInputException("O nome da cidade deve ser informado");
        }

        // 2. Localizar a cidade e obter as coordenadas
        GeocodingResponse.Location location = findLocation(city.trim());

        // 3. Consultar o clima atual usando as coordenadas
        ForecastResponse.Current current = findCurrentWeather(location);

        // 4. Organizar os dados para o usuário
        return new WeatherResponse(
                location.name(),
                current.temperature(),
                current.humidity(),
                current.windSpeed());
    }

    private GeocodingResponse.Location findLocation(String city) {
        GeocodingResponse response;
        try {
            response = geocodingClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("name", city)
                            .queryParam("count", 1)
                            .queryParam("language", "pt")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(GeocodingResponse.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceUnavailableException(UNAVAILABLE_MESSAGE, ex);
        }

        if (response == null || response.isEmpty()) {
            throw new ResourceNotFoundException("Cidade não encontrada");
        }
        return response.results().getFirst();
    }

    private ForecastResponse.Current findCurrentWeather(GeocodingResponse.Location location) {
        ForecastResponse response;
        try {
            response = forecastClient.get()
                    .uri(uri -> uri.path("/forecast")
                            .queryParam("latitude", location.latitude())
                            .queryParam("longitude", location.longitude())
                            .queryParam("current", "temperature_2m,relative_humidity_2m,wind_speed_10m")
                            .build())
                    .retrieve()
                    .body(ForecastResponse.class);
        } catch (RestClientException ex) {
            throw new ExternalServiceUnavailableException(UNAVAILABLE_MESSAGE, ex);
        }

        if (response == null || response.current() == null) {
            // Resposta sem os dados esperados: tratamos como falha do serviço externo
            throw new ExternalServiceUnavailableException(UNAVAILABLE_MESSAGE, null);
        }
        return response.current();
    }
}
