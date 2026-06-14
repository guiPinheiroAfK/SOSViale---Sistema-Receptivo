package br.com.sosviale.controller.login;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.controller.login.dto.LoginRequest;
import br.com.sosviale.controller.login.dto.RegisterRequest;

public interface LoginController {

    void login(LoginRequest request) throws AuthenticationException;

    void loginOffline(String username) throws AuthenticationException;

    void registrar(RegisterRequest request) throws AuthenticationException, ValidationException;
}
