package br.com.sosviale.controller.login.impl;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.auth.SessionManager;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.controller.login.LoginController;
import br.com.sosviale.controller.login.dto.LoginRequest;
import br.com.sosviale.controller.login.dto.RegisterRequest;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.offline.OfflineStore;
import br.com.sosviale.offline.dto.OfflineSessionDto;
import br.com.sosviale.service.UserService;

public class LoginControllerImpl implements LoginController {

    private final AuthenticationService authService;
    private final UserService userService;

    public LoginControllerImpl(AuthenticationService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public void login(LoginRequest request) throws AuthenticationException {
        if (request.username().isEmpty() || request.password().isEmpty()) {
            throw new AuthenticationException("Preencha usuário e senha.");
        }
        authService.login(request.username(), request.password());
    }

    @Override
    public void loginOffline(String username) throws AuthenticationException {
        if (username == null || username.isBlank()) {
            throw new AuthenticationException("Informe o usuário para acesso offline.");
        }

        OfflineSessionDto session = OfflineStore.getInstance().loadSession(username)
                .or(() -> OfflineStore.getInstance().loadAnySession())
                .orElse(null);

        if (session == null || !OfflineStore.getInstance().hasSnapshot(session.getUsuario())) {
            throw new AuthenticationException("Nenhum cache offline encontrado para este usuário.");
        }

        SessionManager.getInstance().iniciarSessaoOffline(
                session.getUsuario(),
                session.getNome(),
                Perfil.valueOf(session.getPerfil()),
                session.isAdmin()
        );
    }

    @Override
    public void registrar(RegisterRequest request) throws AuthenticationException, ValidationException {
        if (request.nome().isEmpty() || request.username().isEmpty() || request.password().isEmpty()) {
            throw new ValidationException("Preencha todos os campos obrigatórios.");
        }
        userService.registrar(
                request.nome(),
                request.username(),
                request.password(),
                request.adminPassword(),
                request.perfil()
        );
    }
}
