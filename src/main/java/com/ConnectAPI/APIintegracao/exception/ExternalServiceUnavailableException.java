package com.ConnectAPI.APIintegracao.exception;

/**
 * Não foi possível falar com o serviço externo (fora do ar, lento demais ou com erro interno).
 * Vira 503 Service Unavailable.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
