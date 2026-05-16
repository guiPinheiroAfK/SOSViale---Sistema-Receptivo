package br.com.sosviale.auth;

// regra simples invalida (usuario curto demais etc)

public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
