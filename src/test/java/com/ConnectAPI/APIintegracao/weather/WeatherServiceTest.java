package com.ConnectAPI.APIintegracao.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.ConnectAPI.APIintegracao.exception.ExternalServiceUnavailableException;
import com.ConnectAPI.APIintegracao.exception.InvalidInputException;
import com.ConnectAPI.APIintegracao.exception.ResourceNotFoundException;

/**
 * Testa o WeatherService simulando o geocoding e a previsão da Open-Meteo.
 */
class WeatherServiceTest {

    private static final String GEOCODING_URL = "https://geocoding.test/v1";
    private static final String FORECAST_URL = "https://forecast.test/v1";

    private static final String MACEIO_GEOCODING = """
            {"results":[{"id":3395981,"name":"Maceió","latitude":-9.66625,"longitude":-35.73528,
                         "country":"Brasil","admin1":"Alagoas"}]}
            """;

    private MockRestServiceServer openMeteo;
    private WeatherService service;

    @BeforeEach
    void setUp() {
        // Um único servidor simulado atende os dois clientes (geocoding e previsão)
        RestClient.Builder builder = RestClient.builder();
        openMeteo = MockRestServiceServer.bindTo(builder).build();
        RestClient geocoding = builder.baseUrl(GEOCODING_URL).build();
        RestClient forecast = builder.baseUrl(FORECAST_URL).build();
        service = new WeatherService(geocoding, forecast);
    }

    @Test
    void deveRetornarClimaQuandoCidadeExiste() {
        openMeteo.expect(requestTo(startsWith(GEOCODING_URL + "/search")))
                .andExpect(queryParam("name", "Maceio"))
                .andExpect(queryParam("count", "1"))
                .andRespond(withSuccess(MACEIO_GEOCODING, MediaType.APPLICATION_JSON));
        openMeteo.expect(requestTo(startsWith(FORECAST_URL + "/forecast")))
                .andExpect(queryParam("latitude", "-9.66625"))
                .andExpect(queryParam("longitude", "-35.73528"))
                .andRespond(withSuccess("""
                        {"latitude":-9.625,"longitude":-35.75,
                         "current":{"time":"2026-11-24T11:00","interval":900,
                                    "temperature_2m":28.5,"relative_humidity_2m":75,"wind_speed_10m":12.4}}
                        """, MediaType.APPLICATION_JSON));

        WeatherResponse response = service.findByCity("Maceio");

        assertThat(response).isEqualTo(new WeatherResponse("Maceió", 28.5, 75, 12.4));
        openMeteo.verify();
    }

    @Test
    void deveRejeitarCidadeVaziaSemConsultarOpenMeteo() {
        assertThatThrownBy(() -> service.findByCity("  "))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("O nome da cidade deve ser informado");
        assertThatThrownBy(() -> service.findByCity(null))
                .isInstanceOf(InvalidInputException.class);
        openMeteo.verify();
    }

    @Test
    void deveLancarNaoEncontradaQuandoGeocodingNaoTemResultados() {
        openMeteo.expect(requestTo(startsWith(GEOCODING_URL + "/search")))
                .andRespond(withSuccess("{\"generationtime_ms\":0.5}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.findByCity("CidadeInexistente"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Cidade não encontrada");
        // A previsão não deve ser consultada
        openMeteo.verify();
    }

    @Test
    void deveLancarIndisponivelQuandoGeocodingFalha() {
        openMeteo.expect(requestTo(startsWith(GEOCODING_URL + "/search")))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> service.findByCity("Maceio"))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("O serviço de previsão do tempo está temporariamente indisponível");
    }

    @Test
    void deveLancarIndisponivelQuandoPrevisaoFalha() {
        openMeteo.expect(requestTo(startsWith(GEOCODING_URL + "/search")))
                .andRespond(withSuccess(MACEIO_GEOCODING, MediaType.APPLICATION_JSON));
        openMeteo.expect(requestTo(startsWith(FORECAST_URL + "/forecast")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.findByCity("Maceio"))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}
