package com.ConnectAPI.APIintegracao.cep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.ConnectAPI.APIintegracao.exception.ExternalServiceUnavailableException;
import com.ConnectAPI.APIintegracao.exception.InvalidInputException;
import com.ConnectAPI.APIintegracao.exception.ResourceNotFoundException;

/**
 * Testa o CepService simulando o ViaCEP com MockRestServiceServer
 * (nenhuma chamada real à internet é feita).
 */
class CepServiceTest {

    private static final String BASE_URL = "https://viacep.test/ws";

    private MockRestServiceServer viaCep;
    private CepService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        viaCep = MockRestServiceServer.bindTo(builder).build();
        service = new CepService(builder.build());
    }

    @Test
    void deveRetornarEnderecoQuandoCepExiste() {
        viaCep.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"cep":"01001-000","logradouro":"Praça da Sé","complemento":"lado ímpar",
                         "unidade":"","bairro":"Sé","localidade":"São Paulo","uf":"SP","ddd":"11"}
                        """, MediaType.APPLICATION_JSON));

        CepResponse response = service.findByCep("01001000");

        assertThat(response).isEqualTo(new CepResponse(
                "01001-000", "Praça da Sé", "lado ímpar", "Sé", "São Paulo", "SP"));
        viaCep.verify();
    }

    @Test
    void deveAceitarCepComHifen() {
        viaCep.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withSuccess("""
                        {"cep":"01001-000","localidade":"São Paulo","uf":"SP"}
                        """, MediaType.APPLICATION_JSON));

        assertThat(service.findByCep("01001-000").cep()).isEqualTo("01001-000");
        viaCep.verify();
    }

    @Test
    void deveRejeitarCepInvalidoSemConsultarViaCep() {
        assertThatThrownBy(() -> service.findByCep("123"))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("O CEP deve conter 8 números");
        assertThatThrownBy(() -> service.findByCep("abcdefgh"))
                .isInstanceOf(InvalidInputException.class);

        // Nenhuma requisição foi esperada, então verify() falharia se alguma tivesse sido feita
        viaCep.verify();
    }

    @Test
    void deveLancarNaoEncontradoQuandoViaCepRetornaErroComoTexto() {
        viaCep.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": \"true\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.findByCep("99999999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("CEP não encontrado");
    }

    @Test
    void deveLancarNaoEncontradoQuandoViaCepRetornaErroComoBooleano() {
        viaCep.expect(requestTo(BASE_URL + "/99999999/json/"))
                .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.findByCep("99999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveLancarIndisponivelQuandoViaCepRetornaErro500() {
        viaCep.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.findByCep("01001000"))
                .isInstanceOf(ExternalServiceUnavailableException.class)
                .hasMessage("O serviço de consulta de CEP está temporariamente indisponível");
    }

    @Test
    void deveLancarIndisponivelQuandoViaCepNaoResponde() {
        viaCep.expect(requestTo(BASE_URL + "/01001000/json/"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThatThrownBy(() -> service.findByCep("01001000"))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}
