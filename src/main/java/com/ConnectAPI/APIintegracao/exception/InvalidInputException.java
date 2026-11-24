package com.ConnectAPI.APIintegracao.exception;

/**
 * O usuário enviou um dado em formato inválido (ex.: CEP com menos de 8 números).
 * Vira 400 Bad Request.
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
