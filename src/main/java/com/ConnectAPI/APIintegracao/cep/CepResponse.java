package com.ConnectAPI.APIintegracao.cep;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Endereço devolvido ao usuário da ConnectAPI.
 */
@Schema(description = "Endereço correspondente ao CEP")
public record CepResponse(
        @Schema(example = "01001-000") String cep,
        @Schema(example = "Praça da Sé") String logradouro,
        @Schema(example = "lado ímpar") String complemento,
        @Schema(example = "Sé") String bairro,
        @Schema(example = "São Paulo") String localidade,
        @Schema(example = "SP") String uf) {

    static CepResponse from(ViaCepResponse viaCep) {
        return new CepResponse(
                viaCep.cep(),
                viaCep.logradouro(),
                viaCep.complemento(),
                viaCep.bairro(),
                viaCep.localidade(),
                viaCep.uf());
    }
}
