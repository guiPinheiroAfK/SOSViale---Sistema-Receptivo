package br.com.sosviale.auth;

/**
 * Exceção de validação
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
