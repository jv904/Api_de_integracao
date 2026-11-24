package com.ConnectAPI.APIintegracao.exception;

/**
 * O dado está correto, mas o serviço externo não encontrou nada (ex.: CEP ou cidade inexistente).
 * Vira 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
