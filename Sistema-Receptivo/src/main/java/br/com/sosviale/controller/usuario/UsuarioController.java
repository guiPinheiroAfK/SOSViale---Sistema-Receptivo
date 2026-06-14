package br.com.sosviale.controller.usuario;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.controller.usuario.dto.UsuarioRequest;
import br.com.sosviale.model.User;

import java.util.List;

public interface UsuarioController {

    void registrar(UsuarioRequest request) throws AuthenticationException, ValidationException;

    void atualizar(UsuarioRequest request) throws AuthenticationException, ValidationException;

    void resetarSenha(UsuarioRequest request) throws AuthenticationException, ValidationException;

    void excluir(String usuario, String senhaAdmin) throws AuthenticationException, ValidationException;

    List<User> listarTodos();
}
