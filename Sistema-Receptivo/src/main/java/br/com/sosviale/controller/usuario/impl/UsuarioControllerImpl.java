package br.com.sosviale.controller.usuario.impl;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.controller.usuario.UsuarioController;
import br.com.sosviale.controller.usuario.dto.UsuarioRequest;
import br.com.sosviale.model.User;
import br.com.sosviale.service.UserService;

import java.util.List;

public class UsuarioControllerImpl implements UsuarioController {

    private final UserService userService;

    public UsuarioControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void registrar(UsuarioRequest request) throws AuthenticationException, ValidationException {
        userService.registrar(
                request.nome(),
                request.usuario(),
                request.senha(),
                request.senhaAdmin(),
                request.perfil()
        );
    }

    @Override
    public void atualizar(UsuarioRequest request) throws AuthenticationException, ValidationException {
        userService.atualizar(
                request.usuario(),
                request.nome(),
                request.perfil(),
                request.senhaAdmin()
        );
    }

    @Override
    public void resetarSenha(UsuarioRequest request) throws AuthenticationException, ValidationException {
        userService.resetarSenhaAdmin(
                request.usuario(),
                request.senha(),
                request.senhaAdmin()
        );
    }

    @Override
    public void excluir(String usuario, String senhaAdmin) throws AuthenticationException, ValidationException {
        userService.excluir(usuario);
    }

    @Override
    public List<User> listarTodos() {
        return userService.listarTodos();
    }
}
