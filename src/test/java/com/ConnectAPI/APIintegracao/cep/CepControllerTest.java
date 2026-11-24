package com.ConnectAPI.APIintegracao.cep;

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
 * Testa a camada HTTP do CEP: rota, status e formato do JSON.
 * O service é substituído por um mock.
 */
@WebMvcTest(CepController.class)
class CepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CepService cepService;

    @Test
    void deveRetornar200ComEndereco() throws Exception {
        given(cepService.findByCep("01001000")).willReturn(new CepResponse(
                "01001-000", "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP"));

        mockMvc.perform(get("/api/cep/01001000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("01001-000"))
                .andExpect(jsonPath("$.logradouro").value("Praça da Sé"))
                .andExpect(jsonPath("$.localidade").value("São Paulo"))
                .andExpect(jsonPath("$.uf").value("SP"));
    }

    @Test
    void deveRetornar400QuandoCepInvalido() throws Exception {
        given(cepService.findByCep("123"))
                .willThrow(new InvalidInputException("O CEP deve conter 8 números"));

        mockMvc.perform(get("/api/cep/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O CEP deve conter 8 números"));
    }

    @Test
    void deveRetornar404QuandoCepNaoExiste() throws Exception {
        given(cepService.findByCep("99999999"))
                .willThrow(new ResourceNotFoundException("CEP não encontrado"));

        mockMvc.perform(get("/api/cep/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("CEP não encontrado"));
    }

    @Test
    void deveRetornar503QuandoViaCepIndisponivel() throws Exception {
        given(cepService.findByCep("01001000")).willThrow(new ExternalServiceUnavailableException(
                "O serviço de consulta de CEP está temporariamente indisponível", null));

        mockMvc.perform(get("/api/cep/01001000"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro")
                        .value("O serviço de consulta de CEP está temporariamente indisponível"));
    }

    @Test
    void deveRetornar500SemDetalhesQuandoErroInesperado() throws Exception {
        given(cepService.findByCep("01001000")).willThrow(new IllegalStateException("detalhe interno"));

        mockMvc.perform(get("/api/cep/01001000"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.erro").value("Ocorreu um erro interno. Tente novamente mais tarde"));
    }
}
