package com.ConnectAPI.APIintegracao.cep;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ConnectAPI.APIintegracao.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cep")
@Tag(name = "CEP", description = "Consulta de endereços pelo CEP (via ViaCEP)")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    @Operation(summary = "Consulta endereço", description = "Retorna o endereço correspondente a um CEP de 8 números")
    @ApiResponse(responseCode = "200", description = "Endereço encontrado")
    @ApiResponse(responseCode = "400", description = "CEP em formato inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "CEP não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "ViaCEP indisponível",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public CepResponse findByCep(
            @Parameter(description = "CEP com 8 números (com ou sem hífen)", example = "01001000")
            @PathVariable String cep) {
        return cepService.findByCep(cep);
    }
}
