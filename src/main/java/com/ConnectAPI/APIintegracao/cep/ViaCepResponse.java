package com.ConnectAPI.APIintegracao.cep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Formato do JSON devolvido pelo ViaCEP (uso interno).
 * Quando o CEP não existe, o ViaCEP responde 200 OK com {"erro": "true"}
 * — por isso o campo "erro" é Object: aceita tanto true quanto "true".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record ViaCepResponse(
        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        Object erro) {

    boolean notFound() {
        return erro != null && Boolean.parseBoolean(erro.toString());
    }
}
