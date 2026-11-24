package com.ConnectAPI.APIintegracao.weather;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Resposta do serviço de geocoding da Open-Meteo (uso interno).
 * Quando a cidade não existe, o campo "results" simplesmente não vem no JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record GeocodingResponse(List<Location> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Location(String name, double latitude, double longitude) {
    }

    boolean isEmpty() {
        return results == null || results.isEmpty();
    }
}
