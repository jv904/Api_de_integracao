package com.ConnectAPI.APIintegracao.weather;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ConnectAPI.APIintegracao.exception.ExternalServiceUnavailableException;
import com.ConnectAPI.APIintegracao.exception.InvalidInputException;
import com.ConnectAPI.APIintegracao.exception.ResourceNotFoundException;

/**
 * Testa a camada HTTP do clima: rota, parâmetro "city", status e formato do JSON.
 */
@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @Test
    void deveRetornar200ComClima() throws Exception {
        given(weatherService.findByCity("Maceio"))
                .willReturn(new WeatherResponse("Maceió", 28.5, 75, 12.4));

        mockMvc.perform(get("/api/weather").param("city", "Maceio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cidade").value("Maceió"))
                .andExpect(jsonPath("$.temperatura").value(28.5))
                .andExpect(jsonPath("$.umidade").value(75))
                .andExpect(jsonPath("$.velocidadeVento").value(12.4));
    }

    @Test
    void deveRetornar400QuandoCidadeNaoInformada() throws Exception {
        given(weatherService.findByCity(null))
                .willThrow(new InvalidInputException("O nome da cidade deve ser informado"));

        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O nome da cidade deve ser informado"));
    }

    @Test
    void deveRetornar404QuandoCidadeNaoExiste() throws Exception {
        given(weatherService.findByCity("CidadeInexistente"))
                .willThrow(new ResourceNotFoundException("Cidade não encontrada"));

        mockMvc.perform(get("/api/weather").param("city", "CidadeInexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Cidade não encontrada"));
    }

    @Test
    void deveRetornar503QuandoOpenMeteoIndisponivel() throws Exception {
        given(weatherService.findByCity("Maceio")).willThrow(new ExternalServiceUnavailableException(
                "O serviço de previsão do tempo está temporariamente indisponível", null));

        mockMvc.perform(get("/api/weather").param("city", "Maceio"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro")
                        .value("O serviço de previsão do tempo está temporariamente indisponível"));
    }
}
