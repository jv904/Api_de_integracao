package com.ConnectAPI.APIintegracao.cep;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ConnectAPI.APIintegracao.exception.ExternalServiceUnavailableException;
import com.ConnectAPI.APIintegracao.exception.InvalidInputException;
import com.ConnectAPI.APIintegracao.exception.ResourceNotFoundException;

@Service
public class CepService {

    private final RestClient viaCepClient;

    public CepService(@Qualifier("viaCepClient") RestClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public CepResponse findByCep(String cep) {
        // 1. Validar: aceita "01001000" ou "01001-000"
        String digits = cep == null ? "" : cep.replace("-", "").trim();
        if (!digits.matches("\\d{8}")) {
            // Formato errado: nem chega a consultar o ViaCEP
            throw new InvalidInputException("O CEP deve conter 8 números");
        }

        // 2. Consultar o ViaCEP
        ViaCepResponse response;
        try {
            response = viaCepClient.get()
                    .uri("/{cep}/json/", digits)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException ex) {
            // Timeout, conexão recusada, erro 5xx, resposta ilegível...
            throw new ExternalServiceUnavailableException(
                    "O serviço de consulta de CEP está temporariamente indisponível", ex);
        }

        // 3. CEP com formato válido, mas inexistente
        if (response == null || response.notFound()) {
            throw new ResourceNotFoundException("CEP não encontrado");
        }

        // 4. Devolver apenas os campos que interessam ao usuário
        return CepResponse.from(response);
    }
}
