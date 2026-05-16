package br.com.sosviale.auth;

// falha no fluxo auth (mensagem volta pra tela assim mesmo)

public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
