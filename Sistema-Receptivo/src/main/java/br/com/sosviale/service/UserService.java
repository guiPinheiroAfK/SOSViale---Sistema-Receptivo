package br.com.sosviale.service;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository repository = new UserRepository();

    public void registrar(String nome, String usuario, String senha, String senhaAdmin, Perfil perfil)
            throws AuthenticationException, ValidationException {

        User admin = repository.buscarAdmin();
        if (admin == null || !admin.getSenha().equals(senhaAdmin))
            throw new AuthenticationException("Senha do administrador incorreta");

        if (usuario == null || usuario.trim().isEmpty())
            throw new ValidationException("Usuário não pode estar vazio");
        if (senha == null || senha.length() < 6)
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        if (repository.buscarPorUsuario(usuario) != null)
            throw new ValidationException("Usuário já existe");

        User novoUser = new User(nome, usuario, senha, false);
        novoUser.setPerfil(perfil != null ? perfil : Perfil.ATENDENTE);
        repository.salvar(novoUser);
    }

    public void excluir(String usuario) throws AuthenticationException, ValidationException {
        if ("admin".equals(usuario))
            throw new ValidationException("Não é possível deletar o administrador");
        repository.excluir(usuario);
    }

    public List<User> listarTodos() {
        return repository.listarTodos();
    }
}