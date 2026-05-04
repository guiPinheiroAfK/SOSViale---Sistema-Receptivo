package br.com.sosviale.auth;

/**
 * Exceção de autenticação
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
