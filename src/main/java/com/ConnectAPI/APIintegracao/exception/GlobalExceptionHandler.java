package com.ConnectAPI.APIintegracao.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Intercepta as exceções lançadas em qualquer controller e as converte
 * em respostas JSON claras no formato {"erro": "..."}.
 * Assim o usuário nunca recebe um stack trace ou detalhes internos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 — dado inválido
    @ExceptionHandler(InvalidInputException.class)
    ResponseEntity<ErrorResponse> handleInvalidInput(InvalidInputException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 — parâmetro obrigatório ausente (ex.: /api/weather sem ?city=)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getParameterName() + "' deve ser informado");
    }

    // 404 — CEP ou cidade não encontrados
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 404 — endereço que não existe na ConnectAPI (ex.: /api/xyz)
    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Endpoint não encontrado");
    }

    // 503 — serviço externo fora do ar
    @ExceptionHandler(ExternalServiceUnavailableException.class)
    ResponseEntity<ErrorResponse> handleUnavailable(ExternalServiceUnavailableException ex) {
        // O detalhe técnico fica no log do servidor, não na resposta ao usuário
        log.warn("Falha ao acessar serviço externo: {}", ex.getMessage(), ex.getCause());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // 500 — qualquer erro inesperado
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Erro inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno. Tente novamente mais tarde");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}
