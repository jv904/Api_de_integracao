package com.ConnectAPI.APIintegracao.exception;

/**
 * Corpo padrão de todas as respostas de erro da ConnectAPI.
 * Exemplo: {"erro": "CEP não encontrado"}
 */
public record ErrorResponse(String erro) {
}
